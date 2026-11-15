<template>
  <!-- 聊天窗口 -->
  <div v-if="showChat" class="chat-window">
    <div class="chat-header">
      <div class="chat-title">
        <el-icon><ChatDotRound /></el-icon>
        <span>在线客服</span>
        <el-tag v-if="session && session.adminName" size="small" type="success">
          {{ session.adminName }}
        </el-tag>
        <el-tag v-else size="small" type="warning">等待分配客服</el-tag>
      </div>
      <div class="chat-actions">
        <el-button size="small" @click="toggleMinimize" :icon="isMinimized ? 'FullScreen' : 'Minus'"></el-button>
        <el-button size="small" @click="endChat" :icon="SwitchButton" type="danger"></el-button>
      </div>
    </div>

    <div v-if="!isMinimized" class="chat-content">
      <!-- 消息区域 -->
      <div ref="messageContainer" class="message-container">
        <div v-if="loading" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载更多...
        </div>

        <div v-for="message in messages" :key="message.id"
             :class="['message-item', message.senderType === 1 ? 'user-message' : 'admin-message']">
          <div class="message-avatar">
            <el-avatar :size="32" :src="getAvatar(message)">
              {{ message.senderName ? message.senderName.charAt(0) : 'U' }}
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-sender">{{ message.senderName }}</div>
            <div class="message-bubble">
              <div v-if="message.messageType === 'text'">{{ message.content }}</div>
              <div v-else-if="message.messageType === 'image'" class="message-image">
                <el-image :src="message.imageUrl" :preview-src-list="[message.imageUrl]"
                          style="max-width: 200px; max-height: 200px; cursor: pointer;">
                </el-image>
              </div>
            </div>
            <div class="message-time">{{ formatTime(message.createTime) }}</div>
          </div>
        </div>

        <div v-if="messages.length === 0" class="empty-message">
          <el-icon><ChatLineRound /></el-icon>
          <p>开始和客服对话吧</p>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-toolbar">
          <el-button type="text" @click="showEmoji = !showEmoji" :icon="Sunny"></el-button>
          <el-button type="text" @click="triggerImageUpload" :icon="Picture"></el-button>
          <input type="file" ref="imageInput" @change="handleImageUpload" accept="image/*" style="display: none;">
        </div>

        <div v-if="showEmoji" class="emoji-picker">
          <span v-for="emoji in emojis" :key="emoji" @click="addEmoji(emoji)" class="emoji-item">
            {{ emoji }}
          </span>
        </div>

        <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="sendMessage"
            resize="none"
        ></el-input>

        <div class="input-actions">
          <el-button @click="sendMessage" type="primary" :loading="sending">
            发送 <el-icon><Position /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="chat-minimized" @click="toggleMinimize">
      <el-badge :value="unreadCount" :max="99" v-if="unreadCount > 0">
        <el-icon><ChatDotRound /></el-icon>
      </el-badge>
      <el-icon v-else><ChatDotRound /></el-icon>
      <span>在线客服</span>
    </div>
  </div>

  <!-- 客服入口按钮 -->
  <div v-else class="chat-entry" @click="startChat">
    <el-badge :value="unreadCount" :max="99" v-if="unreadCount > 0">
      <el-button type="primary" circle size="large" :icon="ChatDotRound"></el-button>
    </el-badge>
    <el-button v-else type="primary" circle size="large" :icon="ChatDotRound"></el-button>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import {
  ChatDotRound, ChatLineRound, Position, Picture,
  Sunny, SwitchButton, Loading, FullScreen, Minus
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  // 可以从父组件传入的配置
  autoStart: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['start', 'end', 'new-message'])

// 响应式数据
const showChat = ref(false)
const isMinimized = ref(false)
const session = ref(null)
const messages = ref([])
const inputMessage = ref('')
const showEmoji = ref(false)
const sending = ref(false)
const loading = ref(false)
const unreadCount = ref(0)
const messageContainer = ref(null)
const imageInput = ref(null)

// 轮询定时器
let pollTimer = null
let unreadTimer = null

// 表情列表
const emojis = ['😀', '😂', '😊', '😍', '😎', '👍', '❤️', '🙏', '🎉', '💯']

// 初始化
onMounted(() => {
  // 获取未读消息数
  fetchUnreadCount()

  // 每30秒检查一次未读消息
  unreadTimer = setInterval(fetchUnreadCount, 30000)

  // 如果自动开始，启动聊天
  if (props.autoStart) {
    startChat()
  }
})

onUnmounted(() => {
  // 清理定时器
  if (pollTimer) clearInterval(pollTimer)
  if (unreadTimer) clearInterval(unreadTimer)
})

// 获取未读消息数
const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/chat/message/unread/count')
    if (res.code === '200') {
      unreadCount.value = res.data || 0
      emit('new-message', unreadCount.value)
    }
  } catch (err) {
    console.error('获取未读消息失败:', err)
  }
}

// 开始聊天
const startChat = async () => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在连接客服...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 修改：传递空对象作为参数
    const res = await request.post('/chat/session/create', {})

    loading.close()

    if (res.code === '200') {
      session.value = res.data.session
      showChat.value = true
      isMinimized.value = false

      // 加载历史消息
      await loadMessages()

      // 开始轮询新消息
      startPolling()

      // 标记已读
      if (session.value.id) {
        markAsRead()
      }

      emit('start', session.value)
    } else {
      ElMessage.error(res.msg || '连接客服失败')
    }
  } catch (err) {
    console.error('开始聊天失败:', err)
    ElMessage.error('连接客服失败，请稍后重试')
  }
}

// 加载消息
const loadMessages = async () => {
  if (!session.value?.id) return

  try {
    loading.value = true
    const res = await request.get('/chat/message/history', {
      params: {
        sessionId: session.value.id,
        pageNum: 1,
        pageSize: 50
      }
    })

    if (res.code === '200') {
      messages.value = res.data || []
      scrollToBottom()
    }
  } catch (err) {
    console.error('加载消息失败:', err)
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || !session.value?.id) return

  try {
    sending.value = true

    const res = await request.post('/chat/message/send', {
      sessionId: session.value.id,
      content: content,
      messageType: 'text'
    })

    if (res.code === '200') {
      // 添加到消息列表
      messages.value.push(res.data)
      inputMessage.value = ''
      showEmoji.value = false
      scrollToBottom()

      // 更新会话信息
      if (session.value) {
        session.value.lastMessage = content
        session.value.lastMessageTime = new Date()
      }
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (err) {
    console.error('发送消息失败:', err)
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

// 处理图片上传
const triggerImageUpload = () => {
  imageInput.value.click()
}

const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型和大小
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) { // 5MB限制
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    // 创建FormData
    const formData = new FormData()
    formData.append('file', file)

    // 上传图片
    const uploadRes = await request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (uploadRes.code === '200') {
      const imageUrl = uploadRes.data

      // 发送图片消息
      const res = await request.post('/chat/message/send', {
        sessionId: session.value.id,
        content: '[图片]',
        messageType: 'image',
        imageUrl: imageUrl
      })

      if (res.code === '200') {
        messages.value.push(res.data)
        scrollToBottom()
      }
    }
  } catch (err) {
    console.error('上传图片失败:', err)
    ElMessage.error('图片上传失败')
  }

  // 清空input
  event.target.value = ''
}

// 轮询新消息
const startPolling = () => {
  if (pollTimer) clearInterval(pollTimer)

  pollTimer = setInterval(async () => {
    if (!session.value?.id || isMinimized.value) return

    try {
      const res = await request.get('/chat/message/history', {
        params: {
          sessionId: session.value.id,
          pageNum: 1,
          pageSize: 10
        }
      })

      if (res.code === '200' && res.data.length > 0) {
        // 合并新消息
        const newMessages = res.data.filter(newMsg =>
            !messages.value.some(oldMsg => oldMsg.id === newMsg.id)
        )

        if (newMessages.length > 0) {
          messages.value = [...messages.value, ...newMessages]
          scrollToBottom()

          // 标记已读
          markAsRead()
        }
      }
    } catch (err) {
      console.error('轮询消息失败:', err)
    }
  }, 3000) // 每3秒轮询一次
}

// 标记已读
const markAsRead = async () => {
  if (!session.value?.id) return

  try {
    await request.post('/chat/message/read', {
      sessionId: session.value.id
    })

    // 更新本地未读数
    if (unreadCount.value > 0) {
      fetchUnreadCount()
    }
  } catch (err) {
    console.error('标记已读失败:', err)
  }
}

// 结束聊天
const endChat = async () => {
  if (session.value?.id) {
    try {
      await request.post('/chat/session/end', {
        sessionId: session.value.id
      })
    } catch (err) {
      console.error('结束会话失败:', err)
    }
  }

  // 清理
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }

  session.value = null
  messages.value = []
  showChat.value = false
  unreadCount.value = 0

  emit('end')
}

// 切换最小化
const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

// 添加表情
const addEmoji = (emoji) => {
  inputMessage.value += emoji
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

// 获取头像
const getAvatar = (message) => {
  if (message.senderType === 2) { // 客服
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  return null
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 如果是今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 其他日期
  return date.toLocaleDateString('zh-CN') + ' ' +
      date.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      })
}

// 监听未读数变化
watch(unreadCount, (newVal) => {
  if (newVal > 0 && !showChat.value) {
    // 可以添加桌面通知
    if (Notification.permission === 'granted') {
      new Notification('新消息通知', {
        body: `您有${newVal}条未读消息`,
        icon: '/favicon.ico'
      })
    }
  }
})
// 暴露方法给父组件
defineExpose({
  startChat
})
</script>

<style scoped>
.chat-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 380px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  overflow: hidden;
  transition: all 0.3s ease;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #39bf23, #2a9d1e);
  color: white;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
}

.chat-title .el-icon {
  font-size: 18px;
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.message-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .message-content {
  align-items: flex-end;
}

.user-message .message-bubble {
  background: #39bf23;
  color: white;
  border-radius: 18px 18px 4px 18px;
}

.admin-message .message-bubble {
  background: white;
  color: #333;
  border-radius: 18px 18px 18px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-sender {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.message-bubble {
  padding: 10px 16px;
  word-break: break-word;
  line-height: 1.5;
}

.message-image {
  border-radius: 8px;
  overflow: hidden;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.loading-more {
  text-align: center;
  padding: 10px;
  color: #999;
}

.empty-message {
  text-align: center;
  color: #999;
  padding: 50px 0;
}

.empty-message .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.input-area {
  border-top: 1px solid #eee;
  background: white;
}

.input-toolbar {
  padding: 10px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  gap: 10px;
}

.emoji-picker {
  padding: 10px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  max-height: 120px;
  overflow-y: auto;
}

.emoji-item {
  font-size: 20px;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
}

.emoji-item:hover {
  background: #f0f0f0;
}

:deep(.el-textarea) {
  border: none;
}

:deep(.el-textarea__inner) {
  border: none !important;
  padding: 20px !important;
  font-size: 14px;
}

.input-actions {
  padding: 10px 20px;
  text-align: right;
}

.chat-minimized {
  padding: 15px 20px;
  background: linear-gradient(135deg, #39bf23, #2a9d1e);
  color: white;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chat-minimized:hover {
  opacity: 0.9;
}

.chat-entry {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9998;
}

.chat-entry .el-button {
  box-shadow: 0 4px 12px rgba(57, 191, 35, 0.3);
  transition: all 0.3s ease;
}

.chat-entry .el-button:hover {
  transform: scale(1.1);
}
</style>