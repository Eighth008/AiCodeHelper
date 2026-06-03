<template>
  <div class="chat-container">
    <!-- 顶部标题栏 -->
    <header class="chat-header">
      <div class="header-left">
        <span class="header-icon">🤖</span>
        <h1 class="header-title">AI 编程助手</h1>
      </div>
      <div class="header-right">
        <span class="session-badge" title="当前会话 ID">
          会话: {{ memoryId }}
        </span>
        <button class="new-chat-btn" @click="handleNewSession" title="开始新对话">
          + 新对话
        </button>
      </div>
    </header>

    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer" @scroll="handleScroll">
      <!-- 空状态欢迎页 -->
      <div v-if="messages.length === 0 && !isLoading" class="welcome-container">
        <div class="welcome-icon">💡</div>
        <h2>欢迎使用 AI 编程助手</h2>
        <p class="welcome-subtitle">
          我可以帮你解答编程学习与求职面试相关的问题
        </p>
        <div class="suggestion-list">
          <button
            v-for="q in suggestions"
            :key="q"
            class="suggestion-chip"
            @click="handleSend(q)"
          >
            {{ q }}
          </button>
        </div>
      </div>

      <!-- 消息列表 -->
      <ChatMessage
        v-for="msg in messages"
        :key="msg.id"
        :message="msg"
      />

      <!-- AI 正在输入指示器 -->
      <div v-if="isLoading && streamingContent === ''" class="typing-indicator">
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
        <span class="typing-dot"></span>
      </div>

      <!-- 流式输出中的临时 AI 消息 -->
      <div v-if="streamingContent" class="message-wrapper ai">
        <div class="avatar"><span>🤖</span></div>
        <div class="message-body">
          <div class="message-role">AI 助手</div>
          <div class="message-bubble ai">
            <div class="message-text">{{ streamingContent }}<span class="cursor-blink">|</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 回到底部按钮 -->
    <transition name="fade">
      <button
        v-if="showScrollToBottom"
        class="scroll-to-bottom"
        @click="scrollToBottom"
      >
        ↓ 回到底部
      </button>
    </transition>

    <!-- 输入区域 -->
    <ChatInput
      ref="chatInputRef"
      :disabled="isLoading"
      @send="handleSend"
    />

    <!-- 错误提示 -->
    <transition name="fade">
      <div v-if="errorMsg" class="error-toast" @click="errorMsg = ''">
        ⚠️ {{ errorMsg }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import { getMemoryId, newMemoryId, streamChat } from './api/chat.js'

// ==================== 状态 ====================

/** 会话 ID */
const memoryId = ref(getMemoryId())

/** 消息列表 */
const messages = ref([])

/** 是否正在等待 AI 回复 */
const isLoading = ref(false)

/** 流式输出中的临时内容 */
const streamingContent = ref('')

/** 错误消息 */
const errorMsg = ref('')

/** 是否显示回到底部按钮 */
const showScrollToBottom = ref(false)

/** 当前 SSE 请求的 AbortController */
let currentAbortController = null

// ==================== 模板引用 ====================

const messagesContainer = ref(null)
const chatInputRef = ref(null)

// ==================== 快捷问题建议 ====================

const suggestions = [
  'Java 中 HashMap 的底层原理是什么？',
  '如何准备大厂的算法面试？',
  'Spring Boot 的核心注解有哪些？',
  '请解释一下 RESTful API 设计规范',
  'Vue3 与 Vue2 的主要区别是什么？'
]

// ==================== 生命周期 ====================

onMounted(() => {
  // 自动聚焦输入框
  nextTick(() => {
    chatInputRef.value?.focus()
  })
  // 从 sessionStorage 恢复消息（同一会话刷新页面后保留）
  restoreMessages()
})

// ==================== 消息持久化 ====================

function getStorageKey() {
  return `chatMessages_${memoryId.value}`
}

function saveMessages() {
  try {
    sessionStorage.setItem(getStorageKey(), JSON.stringify(messages.value))
  } catch {
    // sessionStorage 满了则忽略
  }
}

function restoreMessages() {
  try {
    const saved = sessionStorage.getItem(getStorageKey())
    if (saved) {
      messages.value = JSON.parse(saved)
      nextTick(() => scrollToBottom())
    }
  } catch {
    // 解析失败则忽略
  }
}

// ==================== 发送消息 ====================

async function handleSend(text) {
  if (isLoading.value || !text.trim()) return

  // 清除错误
  errorMsg.value = ''

  // 添加用户消息
  const userMsg = {
    id: generateId(),
    role: 'user',
    content: text.trim(),
    timestamp: Date.now()
  }
  messages.value.push(userMsg)
  saveMessages()
  await nextTick()
  scrollToBottom()

  // 开始请求
  isLoading.value = true
  streamingContent.value = ''

  currentAbortController = streamChat(
    memoryId.value,
    text.trim(),
    {
      onChunk(chunk) {
        streamingContent.value += chunk
        nextTick(() => scrollToBottom())
      },
      onDone() {
        // 将流式内容固化为一条 AI 消息
        if (streamingContent.value) {
          messages.value.push({
            id: generateId(),
            role: 'ai',
            content: streamingContent.value,
            timestamp: Date.now()
          })
          saveMessages()
        }
        streamingContent.value = ''
        isLoading.value = false
        currentAbortController = null
        nextTick(() => {
          scrollToBottom()
          chatInputRef.value?.focus()
        })
      },
      onError(err) {
        console.error('SSE 请求失败:', err)
        // 如果有部分流式内容，也保存下来
        if (streamingContent.value) {
          messages.value.push({
            id: generateId(),
            role: 'ai',
            content: streamingContent.value + '\n\n[回复中断]',
            timestamp: Date.now()
          })
          saveMessages()
        }
        streamingContent.value = ''
        isLoading.value = false
        currentAbortController = null
        errorMsg.value = `请求失败: ${err.message || '网络错误，请稍后重试'}`
        nextTick(() => scrollToBottom())
      }
    }
  )
}

// ==================== 新会话 ====================

function handleNewSession() {
  // 取消进行中的请求
  if (currentAbortController) {
    currentAbortController.abort()
    currentAbortController = null
  }

  // 生成新的会话 ID
  memoryId.value = newMemoryId()

  // 清空消息
  messages.value = []
  streamingContent.value = ''
  isLoading.value = false
  errorMsg.value = ''

  // 聚焦输入框
  nextTick(() => {
    chatInputRef.value?.focus()
  })
}

// ==================== 滚动控制 ====================

function scrollToBottom() {
  const el = messagesContainer.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

function handleScroll() {
  const el = messagesContainer.value
  if (!el) return
  // 距离底部超过 150px 时显示回到底部按钮
  const threshold = 150
  const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight
  showScrollToBottom.value = distanceFromBottom > threshold
}

// ==================== 工具方法 ====================

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
}
</script>

<style>
/* ==================== 全局重置 ==================== */
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial,
    sans-serif;
  background: #f0f2f5;
  color: #333;
}

#app {
  height: 100%;
}
</style>

<style scoped>
/* ==================== 整体布局 ==================== */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 900px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 0 40px rgba(0, 0, 0, 0.06);
  position: relative;
}

/* ==================== 顶部标题栏 ==================== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 28px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.session-badge {
  font-size: 12px;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 10px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.new-chat-btn {
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
  backdrop-filter: blur(4px);
  font-family: inherit;
}

.new-chat-btn:hover {
  background: rgba(255, 255, 255, 0.28);
}

/* ==================== 聊天消息区域 ==================== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  scroll-behavior: smooth;
}

/* 美化滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #b0b0b0;
}

/* ==================== 欢迎页 ==================== */
.welcome-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.welcome-container h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #999;
  margin-bottom: 28px;
}

.suggestion-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  max-width: 500px;
}

.suggestion-chip {
  padding: 10px 18px;
  background: #f5f7fa;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.suggestion-chip:hover {
  background: #e8f0fe;
  border-color: #667eea;
  color: #667eea;
}

/* ==================== 打字指示器 ==================== */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 16px 0;
  margin-left: 52px;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #b0b0b0;
  animation: typingBounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(1) { animation-delay: 0s; }
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

/* ==================== 流式输出光标 ==================== */
.cursor-blink {
  animation: blink 0.8s infinite;
  color: #fff;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* ==================== 回到底部按钮 ==================== */
.scroll-to-bottom {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  padding: 8px 18px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s;
  z-index: 5;
  font-family: inherit;
}

.scroll-to-bottom:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* ==================== 错误提示 ==================== */
.error-toast {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 20px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 10px;
  font-size: 13px;
  color: #ff4d4f;
  cursor: pointer;
  z-index: 20;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.15);
  max-width: 80%;
  text-align: center;
}

/* ==================== 过渡动画 ==================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}

/* ==================== ChatMessage 复用样式（流式消息） ==================== */
.message-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-wrapper.ai {
  flex-direction: row;
}

.message-body {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-wrapper.ai .message-body {
  align-items: flex-start;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
}

.message-role {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  padding: 0 4px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  word-break: break-word;
  font-size: 14px;
}

.message-bubble.ai {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #ffffff;
  border-bottom-left-radius: 4px;
}

.message-text {
  white-space: pre-wrap;
}
</style>
