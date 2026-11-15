<template>
  <div style="width: 100%; max-width: 1200px; margin: 0 auto; padding: 20px;">
    <div class="help-container">
      <!-- 帮助中心标题 -->
      <div class="help-header">
        <h1 style="color: #39bf23; margin-bottom: 10px;">
          <el-icon style="margin-right: 10px;"><QuestionFilled /></el-icon>
          帮助中心
        </h1>
        <p style="color: #666; font-size: 16px;">为您提供全面的使用指南和问题解答</p>
      </div>

      <!-- 快速导航 -->
      <div class="quick-nav">
        <el-row :gutter="20">
          <el-col :span="6" v-for="(item, index) in quickNavItems" :key="index">
            <div class="nav-card" @click="scrollToSection(item.id)">
              <el-icon class="nav-icon" :style="{ color: item.color }">
                <component :is="item.icon" />
              </el-icon>
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 常见问题 -->
      <div class="section" id="faq">
        <h2 class="section-title">
          <el-icon><ChatDotRound /></el-icon>
          常见问题
        </h2>
        <el-collapse v-model="activeNames" accordion>
          <el-collapse-item v-for="(faq, index) in faqs" :key="index" :name="index">
            <template #title>
              <span class="faq-title">{{ faq.question }}</span>
            </template>
            <div class="faq-content">{{ faq.answer }}</div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 使用指南 -->
      <div class="section" id="guide">
        <h2 class="section-title">
          <el-icon><Document /></el-icon>
          使用指南
        </h2>
        <el-timeline>
          <el-timeline-item
              v-for="(guide, index) in guides"
              :key="index"
              :timestamp="guide.step"
              placement="top"
          >
            <el-card>
              <h4>{{ guide.title }}</h4>
              <p>{{ guide.content }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 联系客服 -->
      <div class="section" id="contact">
        <h2 class="section-title">
          <el-icon><Service /></el-icon>
          联系客服
        </h2>
        <div class="contact-info">
          <el-row :gutter="30">
            <el-col :span="8">
              <div class="contact-card">
                <el-icon class="contact-icon"><Phone /></el-icon>
                <h3>客服热线</h3>
                <p>400-123-4567</p>
                <p style="font-size: 12px; color: #999;">工作日 9:00-18:00</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="contact-card">
                <el-icon class="contact-icon"><Message /></el-icon>
                <h3>在线客服</h3>
                <p>点击下方按钮，实时在线沟通</p>
                <el-button type="primary" size="small" @click="startOnlineChat">
                  <el-icon><ChatDotRound /></el-icon>
                  开始对话
                </el-button>
                <p style="font-size: 12px; color: #999; margin-top: 8px;">
                  <el-icon><Clock /></el-icon>
                  服务时间：9:00-22:00
                </p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="contact-card">
                <el-icon class="contact-icon"><Message /></el-icon>
                <h3>邮箱支持</h3>
                <p>support@farm.com</p>
                <p style="font-size: 12px; color: #999;">24小时内回复</p>
              </div>
            </el-col>
          </el-row>

          <!-- 客服统计信息 -->
          <div class="service-stats" style="margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px;">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-item">
                  <el-icon class="stat-icon" style="color: #67C23A;"><User /></el-icon>
                  <div class="stat-content">
                    <div class="stat-number">{{ onlineAdmins.length }}</div>
                    <div class="stat-label">在线客服</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <el-icon class="stat-icon" style="color: #409EFF;"><Clock /></el-icon>
                  <div class="stat-content">
                    <div class="stat-number">{{ responseTime }}</div>
                    <div class="stat-label">平均响应时间</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <el-icon class="stat-icon" style="color: #E6A23C;"><Star /></el-icon>
                  <div class="stat-content">
                    <div class="stat-number">98%</div>
                    <div class="stat-label">满意度</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <el-icon class="stat-icon" style="color: #F56C6C;"><ChatDotSquare /></el-icon>
                  <div class="stat-content">
                    <div class="stat-number">{{ unreadCount }}</div>
                    <div class="stat-label">您的未读消息</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>

      <!-- 意见反馈 -->
      <div class="section" id="feedback">
        <h2 class="section-title">
          <el-icon><Edit /></el-icon>
          意见反馈
        </h2>
        <el-form :model="feedbackForm" label-width="100px" style="max-width: 600px;">
          <el-form-item label="反馈类型">
            <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型">
              <el-option label="功能建议" value="suggestion"></el-option>
              <el-option label="问题反馈" value="bug"></el-option>
              <el-option label="其他" value="other"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="反馈内容">
            <el-input
                v-model="feedbackForm.content"
                type="textarea"
                :rows="4"
                placeholder="请详细描述您的建议或遇到的问题..."
            ></el-input>
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="feedbackForm.contact" placeholder="邮箱或手机号（可选）"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
            <el-button @click="resetFeedback">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 聊天窗口组件 -->
    <ChatWindow ref="chatWindow" @new-message="handleNewMessage" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, markRaw } from 'vue'
import { ElMessage } from 'element-plus'
import ChatWindow from '@/components/ChatWindow.vue'
import request from '@/utils/request'
import {
  QuestionFilled, ChatDotRound, Document, Service,
  Edit, Phone, Message, Clock, User, Star, ChatDotSquare
} from '@element-plus/icons-vue'

// 快速导航数据
const quickNavItems = ref([
  {
    title: '常见问题',
    desc: '快速找到答案',
    icon: markRaw(ChatDotRound), // 使用 markRaw 包装图标组件
    color: '#409EFF',
    id: 'faq'
  },
  {
    title: '使用指南',
    desc: '详细操作说明',
    icon: markRaw(Document),
    color: '#67C23A',
    id: 'guide'
  },
  {
    title: '联系客服',
    desc: '获取专业帮助',
    icon: markRaw(Service),
    color: '#E6A23C',
    id: 'contact'
  },
  {
    title: '意见反馈',
    desc: '帮助我们改进',
    icon: markRaw(Edit),
    color: '#F56C6C',
    id: 'feedback'
  }
])

// 常见问题数据
const faqs = ref([
  {
    question: '如何注册账号？',
    answer: '点击页面右上角的"注册"按钮，填写相关信息即可完成注册。注册时需要提供有效的邮箱地址和手机号码。'
  },
  {
    question: '忘记密码怎么办？',
    answer: '在登录页面点击"忘记密码"，通过邮箱验证码或手机验证码重置密码。'
  },
  {
    question: '如何购买农产品？',
    answer: '登录后进入"农产品购买"页面，选择心仪的商品，点击"加入购物车"或"立即购买"即可。'
  },
  {
    question: '订单如何取消？',
    answer: '在"订单管理"页面找到相应订单，点击"取消订单"按钮。注意：已发货的订单无法取消。'
  },
  {
    question: '如何查看物流信息？',
    answer: '在"订单管理"页面点击订单详情，即可查看物流跟踪信息。'
  },
  {
    question: '支持哪些支付方式？',
    answer: '目前支持支付宝、微信支付、银行卡等多种支付方式，具体以支付页面显示为准。'
  },
  {
    question: '如何联系客服？',
    answer: '您可以通过在线客服、客服热线400-123-4567或发送邮件至support@farm.com联系我们的客服团队。'
  },
  {
    question: '客服工作时间是什么时候？',
    answer: '在线客服工作时间为9:00-22:00，客服热线工作时间为工作日9:00-18:00，邮箱支持24小时内回复。'
  }
])

// 使用指南数据
const guides = ref([
  {
    step: '第一步',
    title: '注册登录',
    content: '使用手机号或邮箱注册账号，登录后即可使用系统功能。'
  },
  {
    step: '第二步',
    title: '浏览商品',
    content: '在首页或农产品购买页面浏览各类农产品，可以按分类筛选。'
  },
  {
    step: '第三步',
    title: '下单购买',
    content: '选择商品加入购物车或直接购买，填写收货信息并完成支付。'
  },
  {
    step: '第四步',
    title: '查看订单',
    content: '在订单管理页面查看订单状态，跟踪物流信息。'
  },
  {
    step: '第五步',
    title: '确认收货',
    content: '收到商品后及时确认收货，如有问题可联系在线客服。'
  }
])

// 反馈表单数据
const feedbackForm = reactive({
  type: '',
  content: '',
  contact: ''
})

// 聊天窗口引用
const chatWindow = ref()
const activeNames = ref([0])
const onlineAdmins = ref([])
const responseTime = ref('30秒')
const unreadCount = ref(0)

// 定时器
let unreadTimer = null

// 页面加载时获取客服信息
onMounted(async () => {
  await fetchOnlineAdmins()
  await fetchUnreadCount()

  // 每30秒刷新一次未读消息数
  unreadTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
  }
})

// 获取在线客服列表
const fetchOnlineAdmins = async () => {
  try {
    // 先尝试调用接口
    const res = await request.get('/chat/admin/online')
    if (res.code === '200') {
      onlineAdmins.value = res.data || []
    }
  } catch (err) {
    console.error('获取在线客服失败:', err)
    // 如果接口失败，使用模拟数据
    onlineAdmins.value = [
      { id: 1, name: '客服小张', online: true },
      { id: 2, name: '客服小李', online: true }
    ]
  }
}

// 获取未读消息数
const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/chat/message/unread/count')
    if (res.code === '200') {
      unreadCount.value = res.data || 0
    }
  } catch (err) {
    console.error('获取未读消息失败:', err)
    // 如果接口失败，设置为0
    unreadCount.value = 0
  }
}

// 滚动到指定区域
const scrollToSection = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 开始在线聊天
const startOnlineChat = () => {
  // 检查用户是否登录
  const user = JSON.parse(localStorage.getItem('system-user') || '{}')
  if (!user.id) {
    ElMessage.warning('请先登录后再联系客服')
    return
  }

  // 启动聊天
  if (chatWindow.value) {
    chatWindow.value.startChat()
  }
}

// 处理新消息通知
const handleNewMessage = (count) => {
  unreadCount.value = count
  if (count > 0) {
    // 可以在这里添加通知逻辑
    console.log(`您有 ${count} 条未读消息`)
  }
}

// 提交反馈
const submitFeedback = () => {
  if (!feedbackForm.type || !feedbackForm.content) {
    ElMessage.warning('请填写完整的反馈信息')
    return
  }

  // 获取当前用户信息
  const user = JSON.parse(localStorage.getItem('system-user') || '{}')
  if (!user.id) {
    ElMessage.error('请先登录')
    return
  }

  // 构建反馈数据
  const feedbackData = {
    userId: user.id,
    username: user.username,
    type: feedbackForm.type,
    content: feedbackForm.content,
    contact: feedbackForm.contact
  }

  // 发送请求到后端
  console.log('提交反馈数据:', feedbackData)
  request.post('/feedback/add', feedbackData).then(res => {
    console.log('反馈提交响应:', res)
    if (res.code === '200') {
      ElMessage.success('反馈提交成功，感谢您的建议！')
      resetFeedback()
    } else {
      ElMessage.error(res.msg || '提交失败，请稍后重试')
    }
  }).catch(err => {
    console.error('反馈提交错误:', err)
    ElMessage.error('提交失败，请稍后重试: ' + (err.message || err))
  })
}

// 重置反馈表单
const resetFeedback = () => {
  feedbackForm.type = ''
  feedbackForm.content = ''
  feedbackForm.contact = ''
}
</script>

<style scoped>
.help-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.help-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.quick-nav {
  margin-bottom: 40px;
}

.nav-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.nav-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.nav-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.nav-card h3 {
  margin: 10px 0 5px 0;
  color: #333;
}

.nav-card p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.section {
  margin-bottom: 40px;
  padding-top: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  color: #39bf23;
  margin-bottom: 20px;
  font-size: 20px;
}

.section-title .el-icon {
  margin-right: 10px;
  font-size: 24px;
}

.faq-title {
  font-weight: 500;
  color: #333;
}

.faq-content {
  color: #666;
  line-height: 1.6;
  padding: 10px 0;
}

.contact-info {
  margin-top: 20px;
}

.contact-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.contact-icon {
  font-size: 32px;
  color: #39bf23;
  margin-bottom: 10px;
}

.contact-card h3 {
  margin: 10px 0;
  color: #333;
}

.contact-card p {
  color: #666;
  margin: 5px 0;
}

.contact-card .el-button {
  margin-top: 10px;
}

.service-stats {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #dee2e6;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 32px;
  margin-right: 15px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: 500;
}

:deep(.el-timeline-item__node) {
  background-color: #39bf23;
}

:deep(.el-timeline-item__tail) {
  border-left-color: #39bf23;
}

:deep(.el-timeline-item__timestamp) {
  color: #39bf23;
  font-weight: 500;
}

:deep(.el-card) {
  border: 1px solid #e6f4e9;
}

:deep(.el-card__body h4) {
  color: #39bf23;
  margin-bottom: 10px;
}

:deep(.el-card__body p) {
  color: #666;
  line-height: 1.6;
}
</style>