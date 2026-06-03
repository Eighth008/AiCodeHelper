<template>
  <div class="message-wrapper" :class="message.role">
    <!-- 头像 -->
    <div class="avatar">
      <span v-if="message.role === 'user'">👤</span>
      <span v-else>🤖</span>
    </div>
    <!-- 消息体 -->
    <div class="message-body">
      <div class="message-role">{{ message.role === 'user' ? '你' : 'AI 助手' }}</div>
      <div class="message-bubble" :class="message.role">
        <!-- AI 消息用 Markdown 渲染，用户消息纯文本 -->
        <div
          v-if="message.role === 'ai'"
          class="message-text markdown-body"
          v-html="renderedContent"
        ></div>
        <div v-else class="message-text">{{ message.content }}</div>
      </div>
      <div class="message-time">{{ formatTime(message.timestamp) }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

// 配置 marked
marked.setOptions({
  breaks: true,     // 换行符转 <br>
  gfm: true         // GitHub Flavored Markdown（表格、删除线等）
})

const props = defineProps({
  message: {
    type: Object,
    required: true,
    validator: (msg) => ['user', 'ai'].includes(msg.role)
  }
})

const renderedContent = computed(() => {
  if (props.message.role === 'ai') {
    return marked.parse(props.message.content)
  }
  return ''
})

function formatTime(timestamp) {
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}
</script>

<style scoped>
.message-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* AI 消息靠左 */
.message-wrapper.ai {
  flex-direction: row;
}

/* 用户消息靠右 */
.message-wrapper.user {
  flex-direction: row-reverse;
}

/* 头像 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  background: #f0f4ff;
}

.message-wrapper.user .avatar {
  background: #e8f0fe;
}

.message-wrapper.ai .avatar {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
}

/* 消息体 */
.message-body {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-wrapper.ai .message-body {
  align-items: flex-start;
}

.message-wrapper.user .message-body {
  align-items: flex-end;
}

/* 角色名 */
.message-role {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  padding: 0 4px;
}

/* 气泡 */
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

.message-bubble.user {
  background: #e8f0fe;
  color: #1a1a1a;
  border-bottom-right-radius: 4px;
}

/* 文本内容（保留换行） */
.message-text {
  white-space: pre-wrap;
}

/* Markdown 渲染内容（AI 消息专用，覆盖 pre-wrap） */
.message-text.markdown-body {
  white-space: normal;
}

/* ==================== Markdown 元素样式 ==================== */
/* 注意：v-html 内容需要 :deep() 穿透 scoped */

/* 标题 */
:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  margin: 12px 0 6px 0;
  line-height: 1.3;
}
:deep(.markdown-body h1) { font-size: 1.4em; }
:deep(.markdown-body h2) { font-size: 1.25em; }
:deep(.markdown-body h3) { font-size: 1.1em; }

/* 段落 */
:deep(.markdown-body p) {
  margin: 4px 0 8px 0;
}
:deep(.markdown-body p:last-child) {
  margin-bottom: 0;
}

/* 无序列表 & 有序列表 */
:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  padding-left: 20px;
  margin: 6px 0;
}

:deep(.markdown-body li) {
  margin: 2px 0;
}

/* 代码块 */
:deep(.markdown-body pre) {
  background: #1e1e2e;
  border-radius: 8px;
  padding: 12px 14px;
  overflow-x: auto;
  margin: 10px 0;
  font-size: 13px;
  line-height: 1.5;
}

:deep(.markdown-body pre code) {
  background: transparent;
  padding: 0;
  font-family: 'Fira Code', 'Cascadia Code', 'JetBrains Mono', Consolas, monospace;
  color: #cdd6f4;
}

/* 行内代码 */
:deep(.markdown-body code) {
  background: rgba(0, 0, 0, 0.15);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'Fira Code', 'Cascadia Code', 'JetBrains Mono', Consolas, monospace;
}

/* AI 气泡中的行内代码（白色文字背景下需要调整） */
.message-bubble.ai :deep(.markdown-body code) {
  background: rgba(255, 255, 255, 0.18);
  color: #f0f0f0;
}

/* AI 气泡中的链接 */
.message-bubble.ai :deep(.markdown-body a) {
  color: #ffd86e;
}

/* 用户气泡中的代码 */
.message-bubble.user :deep(.markdown-body code) {
  background: rgba(0, 0, 0, 0.08);
  color: #c7254e;
}

.message-bubble.user :deep(.markdown-body pre) {
  background: #f0f0f0;
}

.message-bubble.user :deep(.markdown-body pre code) {
  color: #333;
}

/* 引用块 */
:deep(.markdown-body blockquote) {
  margin: 8px 0;
  padding: 4px 12px;
  border-left: 3px solid rgba(255, 255, 255, 0.4);
  opacity: 0.9;
}

/* 表格 */
:deep(.markdown-body table) {
  border-collapse: collapse;
  margin: 8px 0;
  width: 100%;
  font-size: 13px;
}

:deep(.markdown-body th),
:deep(.markdown-body td) {
  border: 1px solid rgba(255, 255, 255, 0.25);
  padding: 6px 10px;
  text-align: left;
}

:deep(.markdown-body th) {
  background: rgba(255, 255, 255, 0.12);
  font-weight: 600;
}

/* 分割线 */
:deep(.markdown-body hr) {
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.25);
  margin: 12px 0;
}

/* 图片 */
:deep(.markdown-body img) {
  max-width: 100%;
  border-radius: 6px;
}

/* 链接 */
:deep(.markdown-body a) {
  text-decoration: underline;
  text-underline-offset: 2px;
}

/* 加粗 & 斜体 */
:deep(.markdown-body strong) {
  font-weight: 700;
}

:deep(.markdown-body em) {
  font-style: italic;
}

/* 时间 */
.message-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  padding: 0 4px;
}
</style>
