<template>
  <div class="chat-input-container">
    <div class="input-wrapper">
      <textarea
        ref="inputRef"
        v-model="inputText"
        class="input-textarea"
        :placeholder="disabled ? 'AI 正在回复中...' : '输入你的问题，按 Enter 发送，Shift+Enter 换行'"
        :disabled="disabled"
        rows="1"
        @keydown="handleKeydown"
        @input="autoResize"
      ></textarea>
      <button
        class="send-button"
        :disabled="disabled || !inputText.trim()"
        @click="handleSend"
        title="发送消息"
      >
        <span v-if="!disabled">➤</span>
        <span v-else class="spinner"></span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send'])

const inputText = ref('')
const inputRef = ref(null)

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function handleKeydown(e) {
  // Enter 发送（Shift+Enter 换行）
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text || props.disabled) return

  emit('send', text)
  inputText.value = ''
  // 重置高度
  const el = inputRef.value
  if (el) {
    el.style.height = 'auto'
  }
}

defineExpose({ focus: () => inputRef.value?.focus() })
</script>

<style scoped>
.chat-input-container {
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  background: #ffffff;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 10px 14px;
  border: 1px solid #e0e0e0;
  transition: border-color 0.2s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-textarea {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  max-height: 120px;
  font-family: inherit;
  color: #333;
}

.input-textarea::placeholder {
  color: #bbb;
}

.input-textarea:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.send-button {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.15s, opacity 0.2s;
}

.send-button:hover:not(:disabled) {
  transform: scale(1.08);
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 加载动画 */
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
