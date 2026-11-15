<template>
  <div class="chat-admin">
    <!-- 头部 -->
    <div class="admin-header">
      <div class="header-left">
        <el-icon><ChatDotRound /></el-icon>
        <span>在线客服系统</span>
        <el-tag type="success" size="small">管理员</el-tag>
        <el-badge :value="totalUnread" :max="99" v-if="totalUnread > 0">
          <el-tag type="warning" size="small">未读: {{ totalUnread }}</el-tag>
        </el-badge>
      </div>
      <div class="header-right">
        <el-button type="text" @click="toggleOnlineStatus">
          <el-icon :color="isOnline ? '#67C23A' : '#909399'">
            <CircleCheckFilled v-if="isOnline" />
            <CircleCloseFilled v-else />
          </el-icon>
          {{ isOnline ? '在线' : '离线' }}
        </el-button>
        <el-button type="text" @click="refreshSessions">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="admin-main">
      <!-- 左侧会话列表 -->
      <div class="session-list">
        <div class="list-header">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索用户..."
              clearable
              @input="filterSessions"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="list-content">
          <div
              v-for="session in filteredSessions"
              :key="session.id"
              :class="['session-item', { active: activeSession?.id === session.id }]"
              @click="selectSession(session)"
          >
            <div class="session-avatar">
              <el-avatar :size="40" :src="getUserAvatar(session.userId)">
                {{ session.username?.charAt(0) || 'U' }}
              </el-avatar>
              <el-badge
                  :value="session.unreadCount"
                  :max="99"
                  v-if="session.unreadCount > 0"
                  class="unread-badge"
              />
            </div>
            <div class="session-info">
              <div class="user-name">
                {{ session.username }}
                <el-tag v-if="session.status === 0" size="mini" type="warning">等待</el-tag>
                <el-tag v-if="session.status === 2" size="mini" type="info">已结束</el-tag>
              </div>
              <div class="last-message" :title="session.lastMessage">
                {{ session.lastMessage || '暂无消息' }}
              </div>
              <div class="session-time">
                {{ formatRelativeTime(session.lastMessageTime) }}
              </div>
            </div>
          </div>

          <div v-if="sessions.length === 0" class="empty-sessions">
            <el-empty description="暂无会话" />
          </div>
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-window">
        <div v-if="activeSession" class="chat-container">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="chat-info">
              <el-avatar :size="32" :src="getUserAvatar(activeSession.userId)">
                {{ activeSession.username?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="info-details">
                <div class="user-name">{{ activeSession.username }}</div>
                <div class="session-status">
                  <span :class="['status-dot', getStatusClass(activeSession.status)]"></span>
                  {{ getStatusText(activeSession.status) }}
                </div>
              </div>
            </div>
            <div class="chat-actions">
              <el-tooltip content="转接会话" placement="top">
                <el-button type="text" @click="transferSession" :icon="Connection"></el-button>
              </el-tooltip>
              <el-tooltip content="结束会话" placement="top">
                <el-button type="text" @click="endSession(activeSession)" :icon="SwitchButton"></el-button>
              </el-tooltip>
            </div>
          </div>

          <!-- 消息区域 -->
          <div ref="messageContainer" class="message-container">
            <div v-if="loadingMessages" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
              加载中...
            </div>

            <div v-for="message in messages" :key="message.id"
                 :class="['message-item', message.senderType === 1 ? 'user-message' : 'admin-message']">
              <div class="message-avatar">
                <el-avatar :size="32" :src="getAvatar(message)">
                  {{ getAvatarText(message) }}
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-sender">
                  <el-tag v-if="message.senderType === 1" size="mini" type="primary" class="sender-tag">
                    用户
                  </el-tag>
                  <el-tag v-else-if="message.senderType === 2" size="mini" type="success" class="sender-tag">
                    客服
                  </el-tag>
                  {{ getDisplayName(message) }}
                </div>
                <div class="message-bubble">
                  <div v-if="message.messageType === 'text'">{{ message.content }}</div>
                  <div v-else-if="message.messageType === 'image'" class="message-image">
                    <el-image
                        :src="message.imageUrl"
                        :preview-src-list="[message.imageUrl]"
                        style="max-width: 200px; max-height: 200px; cursor: pointer;"
                    />
                  </div>
                </div>
                <div class="message-time">{{ formatTime(message.createTime) }}</div>
              </div>
            </div>

            <div v-if="messages.length === 0" class="empty-message">
              <el-icon><ChatLineRound /></el-icon>
              <p>暂无消息</p>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <div class="input-toolbar">
              <el-button type="text" @click="showEmoji = !showEmoji" :icon="Sunny"></el-button>
              <el-button type="text" @click="triggerImageUpload" :icon="Picture"></el-button>
              <el-button type="text" @click="insertQuickReply" :icon="Comment">
                <span>快捷回复</span>
              </el-button>
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
            />

            <div class="input-actions">
              <el-button @click="sendMessage" type="primary" :loading="sending">
                发送 <el-icon><Position /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div v-else class="no-session">
          <el-empty description="请选择一个会话开始聊天" />
        </div>
      </div>
    </div>

    <!-- 快捷回复对话框 -->
    <el-dialog v-model="showQuickReplies" title="快捷回复" width="400px">
      <div class="quick-replies">
        <el-button
            v-for="(reply, index) in quickReplies"
            :key="index"
            type="text"
            @click="selectQuickReply(reply)"
            class="reply-item"
        >
          {{ reply }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 转接会话对话框 -->
    <el-dialog v-model="showTransferDialog" title="转接会话" width="500px">
      <el-transfer
          v-model="transferToAdminIds"
          :data="onlineAdmins"
          :titles="['在线客服', '转接给']"
          :props="{
          key: 'id',
          label: 'name'
        }"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTransferDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmTransfer" :loading="transferring">
            确认转接
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import {
  ChatDotRound,
  ChatLineRound,
  Position,
  Picture,
  Sunny,
  SwitchButton,
  Loading,
  Refresh,
  Search,
  Connection,
  Comment,
  CircleCheckFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  adminId: {
    type: Number,
    default: null
  }
})

// 响应式数据
const sessions = ref([])
const filteredSessions = ref([])
const activeSession = ref(null)
const messages = ref([])
const searchKeyword = ref('')
const isOnline = ref(true)
const showEmoji = ref(false)
const inputMessage = ref('')
const sending = ref(false)
const loadingMessages = ref(false)
const showQuickReplies = ref(false)
const showTransferDialog = ref(false)
const transferToAdminIds = ref([])
const transferring = ref(false)
const messageContainer = ref(null)
const imageInput = ref(null)

// 轮询定时器
let pollTimer = null

// 表情列表
const emojis = ['😀', '😂', '😊', '😍', '😎', '👍', '❤️', '🙏', '🎉', '💯']

// 快捷回复
const quickReplies = [
  '您好，有什么可以帮您？',
  '请稍等，我马上为您处理',
  '请问您的问题解决了吗？',
  '感谢您的耐心等待',
  '请问还有其他需要帮助的吗？',
  '请描述一下您遇到的问题',
  '我会尽快为您解决',
  '这是我们的处理流程...',
  '请问您使用的是哪个浏览器？',
  '请您刷新页面重试一下'
]

// 在线客服列表
const onlineAdmins = ref([])

// 计算总未读消息数
const totalUnread = computed(() => {
  return sessions.value.reduce((sum, session) => sum + (session.unreadCount || 0), 0)
})

// 初始化
onMounted(() => {
  loadSessions()
  loadOnlineAdmins()
  startPolling()
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

// 加载会话列表
const loadSessions = async () => {
  try {
    const res = await request.get('/chat/admin/sessions')
    if (res.code === '200') {
      sessions.value = res.data || []
      filteredSessions.value = sessions.value

      // 如果有未读消息，自动选择第一个未读会话
      const firstUnread = sessions.value.find(s => s.unreadCount > 0)
      if (firstUnread && !activeSession.value) {
        selectSession(firstUnread)
      }
    }
  } catch (err) {
    console.error('加载会话失败:', err)
    ElMessage.error('加载会话失败')
  }
}

// 加载在线客服
const loadOnlineAdmins = async () => {
  try {
    const res = await request.get('/chat/admin/online/list')
    if (res.code === '200') {
      onlineAdmins.value = res.data || []
    }
  } catch (err) {
    console.error('加载在线客服失败:', err)
  }
}

// 过滤会话
const filterSessions = () => {
  if (!searchKeyword.value.trim()) {
    filteredSessions.value = sessions.value
    return
  }

  const keyword = searchKeyword.value.toLowerCase()
  filteredSessions.value = sessions.value.filter(session =>
      session.username?.toLowerCase().includes(keyword) ||
      session.lastMessage?.toLowerCase().includes(keyword)
  )
}

// 选择会话
const selectSession = async (session) => {
  if (activeSession.value?.id === session.id) return

  activeSession.value = session
  messages.value = []
  showEmoji.value = false

  // 加载消息历史
  await loadMessages()

  // 标记为已读
  if (session.unreadCount > 0) {
    await markAsRead()
  }
}

// 加载消息 - 修正版
const loadMessages = async () => {
  if (!activeSession.value?.id) return

  try {
    loadingMessages.value = true
    const res = await request.get('/chat/message/history', {
      params: {
        sessionId: activeSession.value.id,
        pageNum: 1,
        pageSize: 50
      }
    })

    if (res.code === '200') {
      // 调试：打印接收到的消息数据
      console.log('原始消息数据:', res.data)

      // 关键修复：正确处理消息的发送者类型
      const processedMessages = (res.data || []).map(msg => {
        console.log('处理前消息:', msg)

        // 根据不同的字段判断发送者类型
        let senderType = 1 // 默认为用户

        // 方案1：如果后端有明确的senderType字段
        if (msg.senderType !== undefined) {
          senderType = msg.senderType
        }
        // 方案2：如果有isAdmin字段
        else if (msg.isAdmin !== undefined) {
          senderType = msg.isAdmin ? 2 : 1
        }
        // 方案3：根据发送者ID判断（假设管理员ID有特定范围）
        else if (msg.senderId !== undefined && props.adminId) {
          senderType = msg.senderId === props.adminId ? 2 : 1
        }
        // 方案4：根据发送者名称判断
        else if (msg.senderName) {
          senderType = msg.senderName === 'admin' || msg.senderName === '管理员' ||
          msg.senderName === '客服' ? 2 : 1
        }

        const displayName = senderType === 1
            ? (msg.senderName || activeSession.value.username || '用户')
            : (msg.senderName || '客服')

        return {
          ...msg,
          senderType: senderType,
          senderName: displayName
        }
      })

      console.log('处理后的消息:', processedMessages)
      messages.value = processedMessages
      scrollToBottom()
    }
  } catch (err) {
    console.error('加载消息失败:', err)
    ElMessage.error('加载消息失败')
  } finally {
    loadingMessages.value = false
  }
}

// 发送消息 - 修正版
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || !activeSession.value?.id) return

  try {
    sending.value = true

    const res = await request.post('/chat/admin/message/send', {
      sessionId: activeSession.value.id,
      content: content,
      messageType: 'text',
      adminId: props.adminId
    })

    if (res.code === '200') {
      // 确保发送的消息被正确标记为管理员消息
      const adminMessage = {
        ...res.data,
        id: Date.now(), // 临时ID
        senderType: 2, // 明确标记为管理员消息
        senderName: '客服', // 管理员发送的消息显示为"客服"
        senderId: props.adminId,
        createTime: new Date().toISOString()
      }

      console.log('发送的消息:', adminMessage)
      messages.value.push(adminMessage)
      inputMessage.value = ''
      showEmoji.value = false
      scrollToBottom()

      // 更新会话最后消息
      updateSessionLastMessage(activeSession.value.id, content)
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (err) {
    console.error('发送消息失败:', err)
    ElMessage.error('发送失败')
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

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const uploadRes = await request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (uploadRes.code === '200') {
      const imageUrl = uploadRes.data

      const res = await request.post('/chat/admin/message/send', {
        sessionId: activeSession.value.id,
        content: '[图片]',
        messageType: 'image',
        imageUrl: imageUrl,
        adminId: props.adminId
      })

      if (res.code === '200') {
        // 确保发送的图片消息被正确标记为管理员消息
        const adminMessage = {
          ...res.data,
          senderType: 2, // 明确标记为管理员消息
          senderName: '客服',
          senderId: props.adminId
        }

        messages.value.push(adminMessage)
        scrollToBottom()
        updateSessionLastMessage(activeSession.value.id, '[图片]')
      }
    }
  } catch (err) {
    console.error('上传图片失败:', err)
    ElMessage.error('图片上传失败')
  }

  event.target.value = ''
}

// 轮询新消息
const startPolling = () => {
  if (pollTimer) clearInterval(pollTimer)

  pollTimer = setInterval(async () => {
    // 刷新会话列表
    await loadSessions()

    // 如果有活动会话，检查新消息
    if (activeSession.value?.id) {
      checkNewMessages()
    }
  }, 5000) // 每5秒轮询一次
}

// 检查新消息 - 修正版
const checkNewMessages = async () => {
  try {
    const res = await request.get('/chat/admin/message/new', {
      params: {
        sessionId: activeSession.value.id,
        lastMessageId: messages.value.length > 0 ? messages.value[messages.value.length - 1].id : 0
      }
    })

    if (res.code === '200' && res.data.length > 0) {
      // 处理新消息格式
      const newMessages = res.data.map(msg => {
        // 根据不同的字段判断发送者类型
        let senderType = 1

        if (msg.senderType !== undefined) {
          senderType = msg.senderType
        } else if (msg.isAdmin !== undefined) {
          senderType = msg.isAdmin ? 2 : 1
        } else if (msg.senderId !== undefined && props.adminId) {
          senderType = msg.senderId === props.adminId ? 2 : 1
        } else if (msg.senderName) {
          senderType = msg.senderName === 'admin' || msg.senderName === '管理员' ||
          msg.senderName === '客服' ? 2 : 1
        }

        const displayName = senderType === 1
            ? (msg.senderName || activeSession.value.username || '用户')
            : (msg.senderName || '客服')

        return {
          ...msg,
          senderType: senderType,
          senderName: displayName
        }
      })

      console.log('接收到的新消息:', newMessages)

      messages.value.push(...newMessages)
      scrollToBottom()

      // 标记为已读
      if (newMessages.some(msg => msg.senderType === 1)) {
        await markAsRead()
      }
    }
  } catch (err) {
    console.error('检查新消息失败:', err)
  }
}

// 标记已读
const markAsRead = async () => {
  if (!activeSession.value?.id) return

  try {
    await request.post('/chat/admin/message/read', {
      sessionId: activeSession.value.id,
      adminId: props.adminId
    })

    // 更新本地会话未读数
    const session = sessions.value.find(s => s.id === activeSession.value.id)
    if (session) {
      session.unreadCount = 0
    }
  } catch (err) {
    console.error('标记已读失败:', err)
  }
}

// 更新会话最后消息
const updateSessionLastMessage = (sessionId, message) => {
  const session = sessions.value.find(s => s.id === sessionId)
  if (session) {
    session.lastMessage = message
    session.lastMessageTime = new Date()
  }
}

// 结束会话
const endSession = async (session) => {
  try {
    await ElMessageBox.confirm('确定要结束这个会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await request.post('/chat/admin/session/end', {
      sessionId: session.id,
      adminId: props.adminId
    })

    if (res.code === '200') {
      ElMessage.success('会话已结束')
      session.status = 2

      // 如果结束的是当前活动会话，清除活动会话
      if (activeSession.value?.id === session.id) {
        activeSession.value = null
        messages.value = []
      }
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('结束会话失败:', err)
      ElMessage.error('结束会话失败')
    }
  }
}

// 转接会话
const transferSession = () => {
  showTransferDialog.value = true
  transferToAdminIds.value = []
}

// 确认转接
const confirmTransfer = async () => {
  if (transferToAdminIds.value.length === 0) {
    ElMessage.warning('请选择要转接的客服')
    return
  }

  try {
    transferring.value = true

    const res = await request.post('/chat/admin/session/transfer', {
      sessionId: activeSession.value.id,
      fromAdminId: props.adminId,
      toAdminId: transferToAdminIds.value[0]
    })

    if (res.code === '200') {
      ElMessage.success('会话转接成功')
      showTransferDialog.value = false

      // 重新加载会话列表
      await loadSessions()
      activeSession.value = null
      messages.value = []
    } else {
      ElMessage.error(res.msg || '转接失败')
    }
  } catch (err) {
    console.error('转接会话失败:', err)
    ElMessage.error('转接失败')
  } finally {
    transferring.value = false
  }
}

// 插入快捷回复
const insertQuickReply = () => {
  showQuickReplies.value = true
}

const selectQuickReply = (reply) => {
  inputMessage.value = reply
  showQuickReplies.value = false
}

// 切换在线状态
const toggleOnlineStatus = async () => {
  try {
    const res = await request.post('/chat/admin/status', {
      adminId: props.adminId,
      online: !isOnline.value
    })

    if (res.code === '200') {
      isOnline.value = !isOnline.value
      ElMessage.success(isOnline.value ? '已上线' : '已下线')
    }
  } catch (err) {
    console.error('切换状态失败:', err)
  }
}

// 刷新会话
const refreshSessions = () => {
  loadSessions()
  ElMessage.success('已刷新')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

// 添加表情
const addEmoji = (emoji) => {
  inputMessage.value += emoji
}

// 获取头像
const getAvatar = (message) => {
  if (message.senderType === 2) {
    // 管理员头像
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  // 用户头像
  return null
}

// 获取头像文本
const getAvatarText = (message) => {
  if (message.senderType === 1) {
    return 'U' // 用户
  } else if (message.senderType === 2) {
    return 'A' // 管理员/客服
  }
  return '?'
}

// 获取显示名称
const getDisplayName = (message) => {
  if (message.senderType === 1) {
    return message.senderName || '用户'
  } else if (message.senderType === 2) {
    return message.senderName || '客服'
  }
  return message.senderName || '未知'
}

const getUserAvatar = (userId) => {
  return null
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化相对时间
const formatRelativeTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const diffMinutes = Math.floor(diff / 60000)
  const diffHours = Math.floor(diff / 3600000)
  const diffDays = Math.floor(diff / 86400000)

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`

  return date.toLocaleDateString('zh-CN')
}

// 获取状态类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'waiting'
    case 1: return 'active'
    case 2: return 'ended'
    default: return 'waiting'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '等待中'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}

// 调试方法：查看消息数据
const debugMessages = () => {
  console.log('=== 消息调试信息 ===')
  console.log('当前所有消息:', messages.value)
  console.log('消息统计:')
  console.log('- 总消息数:', messages.value.length)
  console.log('- 用户消息数:', messages.value.filter(m => m.senderType === 1).length)
  console.log('- 客服消息数:', messages.value.filter(m => m.senderType === 2).length)

  // 检查每条消息的详细数据
  messages.value.forEach((msg, index) => {
    console.log(`消息${index + 1}:`, {
      id: msg.id,
      content: msg.content?.substring(0, 50) + (msg.content?.length > 50 ? '...' : ''),
      senderType: msg.senderType,
      senderName: msg.senderName,
      senderId: msg.senderId,
      isAdmin: msg.isAdmin,
      messageType: msg.messageType,
      createTime: msg.createTime
    })
  })

  // 建议检查的内容
  console.log('=== 建议检查 ===')
  console.log('1. 后端API返回的数据结构')
  console.log('2. senderType字段的值（应该为1=用户，2=管理员）')
  console.log('3. 发送消息时是否传递了adminId参数')
  console.log('4. 后端是否正确处理了发送者类型')
}
</script>

<style scoped>
.chat-admin {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-left .el-icon {
  font-size: 24px;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.session-list {
  width: 320px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
  border-left: 3px solid #409eff;
}

.session-avatar {
  position: relative;
  margin-right: 12px;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #303133;
}

.last-message {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.session-time {
  font-size: 12px;
  color: #c0c4cc;
}

.empty-sessions {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #303133;
}

.session-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot.waiting {
  background-color: #e6a23c;
}

.status-dot.active {
  background-color: #67c23a;
}

.status-dot.ended {
  background-color: #909399;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.message-container {
  flex: 1;
  padding: 24px;
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

/* 用户消息 - 显示在右侧 */
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

/* 客服/管理员消息 - 显示在左侧 */
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

/* 发送者标签样式 */
.sender-tag {
  margin-right: 6px;
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
  min-width: 32px;
  text-align: center;
}

.user-message .sender-tag {
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: white !important;
}

.admin-message .sender-tag {
  background-color: #67c23a !important;
  border-color: #67c23a !important;
  color: white !important;
}

.message-sender {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.message-bubble {
  padding: 10px 16px;
  word-break: break-word;
  line-height: 1.5;
  max-width: 100%;
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

.no-session {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.quick-replies {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reply-item {
  text-align: left;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.reply-item:hover {
  background-color: #f5f7fa;
  border-color: #c6e2ff;
}
</style>