<template>
  <div class="chat-interface">
    <div class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0" class="empty-state">
        <el-icon class="empty-icon"><chat-dot-round /></el-icon>
        <p>暂无对话记录</p>
        <p class="empty-tip">上传文档后，可以开始向 AI 助手提问</p>
      </div>

      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role]"
      >
        <div class="message-avatar">
          <el-icon v-if="msg.role === 'user'"><user /></el-icon>
          <el-icon v-else><UserFilled   /></el-icon>
        </div>
        <div class="message-content">
          <div class="message-text" v-html="formatMessage(msg.content)"></div>
          <div class="message-time">{{ msg.time }}</div>
        </div>
      </div>

      <div v-if="loading" class="message assistant">
        <div class="message-avatar">
          <el-icon><UserFilled   /></el-icon>
        </div>
        <div class="message-content">
          <div class="message-text loading">
            <span>正在思考中</span>
            <span class="dots">...</span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        placeholder="请输入您的问题..."
        :disabled="loading"
        @keydown.enter.ctrl="sendQuestion"
        resize="none"
      />
      <el-button
        type="primary"
        :loading="loading"
        :disabled="!question.trim()"
        @click="sendQuestion"
      >
        {{ loading ? '回答中...' : '发送' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ChatDotRound, User, UserFilled   } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const messages = ref([])
const question = ref('')
const loading = ref(false)
const messagesRef = ref(null)

const userId = ref('user_' + Math.random().toString(36).substr(2, 9))

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 格式化消息（支持换行）
const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

// 发送问题
const sendQuestion = async () => {
  if (!question.value.trim() || loading.value) return

  const userQuestion = question.value.trim()
  const now = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userQuestion,
    time: now
  })

  question.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const response = await axios.post('/api/knowledge/chat', {
      question: userQuestion,
      userId: userId.value,
      topK: 5
    })

    const answerTime = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

    // 添加 AI 回答
    messages.value.push({
      role: 'assistant',
      content: response.data.answer || '抱歉，未能获取到回答',
      time: answerTime
    })
  } catch (error) {
    ElMessage.error('获取回答失败: ' + (error.response?.data?.msg || error.message))
    messages.value.push({
      role: 'assistant',
      content: '抱歉，处理您的问题时出错了，请稍后再试。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-interface {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafafa;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 20px;
  color: #c0c4cc;
}

.empty-tip {
  font-size: 12px;
  margin-top: 10px;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.message-content {
  max-width: 70%;
  margin: 0 12px;
}

.message.user .message-content {
  align-items: flex-end;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-break: break-word;
}

.message.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-text {
  background: white;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

.loading .dots {
  animation: blink 1.4s infinite both;
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

.chat-input {
  padding: 16px;
  background: white;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input .el-textarea {
  flex: 1;
}
</style>
