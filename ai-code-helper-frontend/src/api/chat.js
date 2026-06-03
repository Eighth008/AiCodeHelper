/**
 * SSE 流式聊天服务
 * 使用 Fetch API 读取 Spring Boot ServerSentEvent 流
 */

const BASE_URL = '/api'

/**
 * 生成唯一的会话 memoryId（存储在 sessionStorage 中，每个标签页独立）
 */
export function getMemoryId() {
  let id = sessionStorage.getItem('chatMemoryId')
  if (!id) {
    // 生成 int 范围内的随机数
    id = String(Math.floor(Math.random() * 2147483647))
    sessionStorage.setItem('chatMemoryId', id)
  }
  return parseInt(id, 10)
}

/**
 * 生成新的会话 memoryId（开启新对话）
 */
export function newMemoryId() {
  const id = String(Math.floor(Math.random() * 2147483647))
  sessionStorage.setItem('chatMemoryId', id)
  return parseInt(id, 10)
}

/**
 * 通过 SSE 流式调用聊天接口
 * @param {number} memoryId - 会话 ID
 * @param {string} msg - 用户消息
 * @param {Object} callbacks - 回调函数
 * @param {function} callbacks.onChunk - 收到一个数据块
 * @param {function} callbacks.onDone - 流结束
 * @param {function} callbacks.onError - 发生错误
 * @returns {AbortController} 用于取消请求
 */
export function streamChat(memoryId, msg, { onChunk, onDone, onError }) {
  const abortController = new AbortController()
  const url = `${BASE_URL}/ai/chat?memoryId=${memoryId}&msg=${encodeURIComponent(msg)}`

  fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'text/event-stream'
    },
    signal: abortController.signal
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        // 按行解析 SSE 格式
        const lines = buffer.split('\n')
        // 最后一行可能不完整，保留到下次处理
        buffer = lines.pop() || ''

        for (const line of lines) {
          const trimmed = line.trim()
          if (trimmed.startsWith('data:')) {
            // 提取 data: 后面的内容（去掉开头的 "data:"）
            const data = trimmed.slice(5).trim()
            if (data) {
              onChunk(data)
            } else {
              // LLM 输出的空 chunk（换行符被拆分后到达），保留换行
              onChunk('\n')
            }
          }
          // 空行表示一个 SSE 事件的结束，忽略
        }
      }

      // 处理 buffer 中可能残留的最后一条数据
      if (buffer.trim().startsWith('data:')) {
        const data = buffer.trim().slice(5).trim()
        if (data) {
          onChunk(data)
        } else {
          onChunk('\n')
        }
      }

      onDone()
    })
    .catch((err) => {
      if (err.name === 'AbortError') {
        // 用户主动取消，不算错误
        onDone()
        return
      }
      onError(err)
    })

  return abortController
}
