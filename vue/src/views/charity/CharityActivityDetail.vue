<template>
  <div class="charity-activity-detail">
    <!-- 活动基本信息 -->
    <div class="activity-header">
      <el-row :gutter="20">
        <el-col :span="16">
          <h1>{{ activity.title }}</h1>
          <div class="activity-meta">
            <span class="status-tag" :class="getStatusClass(activity.status)">
              {{ getStatusText(activity.status) }}
            </span>
            <span><i class="el-icon-user"></i> 组织者：{{ activity.organizerName }}</span>
            <span v-if="activity.storeName"><i class="el-icon-shop"></i> 参与店铺：{{ activity.storeName }}</span>
            <span><i class="el-icon-view"></i> 浏览：{{ activity.viewsCount }}</span>
          </div>

          <div class="activity-time">
            <span><i class="el-icon-date"></i> 开始时间：{{ formatDateTime(activity.startTime) }}</span>
            <span><i class="el-icon-date"></i> 结束时间：{{ formatDateTime(activity.endTime) }}</span>
          </div>

          <div v-if="activity.location" class="activity-location">
            <i class="el-icon-location"></i> 活动地点：{{ activity.location }}
          </div>
        </el-col>

        <el-col :span="8" class="action-buttons">
          <!-- 点赞按钮 -->
          <el-button
              :type="activity.liked ? 'primary' : 'default'"
              @click="handleLike"
              :disabled="!isLogin"
          >
            <i class="el-icon-star-off"></i>
            {{ activity.liked ? '已点赞' : '点赞' }} ({{ activity.likesCount }})
          </el-button>

          <!-- 收藏按钮 -->
          <el-button
              :type="activity.collected ? 'warning' : 'default'"
              @click="handleCollect"
              :disabled="!isLogin"
          >
            <i class="el-icon-collection"></i>
            {{ activity.collected ? '已收藏' : '收藏' }} ({{ activity.collectsCount }})
          </el-button>

          <!-- 报名按钮（线下活动） -->
          <el-button
              v-if="activity.activityType === 'offline'"
              type="success"
              @click="showApplyDialog = true"
              :disabled="!isLogin || activity.status !== 2 || isApplied"
          >
            {{ isApplied ? '已报名' : '立即报名' }}
          </el-button>

          <!-- 购买按钮（公益订单） -->
          <el-button
              v-if="activity.activityType === 'donation'"
              type="danger"
              @click="showOrderDialog = true"
              :disabled="!isLogin || activity.status !== 2"
          >
            立即购买
          </el-button>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20">
      <!-- 左侧内容 -->
      <el-col :span="16">
        <!-- 活动封面 -->
        <div class="cover-image">
          <img :src="activity.coverImage || '/default-cover.jpg'" alt="封面" />
        </div>

        <!-- 活动描述 -->
        <div class="activity-description">
          <h3>活动介绍</h3>
          <div v-html="activity.description"></div>
        </div>

        <!-- 捐赠信息 -->
        <div v-if="activity.activityType === 'donation'" class="donation-info">
          <h3>捐赠信息</h3>
          <el-card>
            <div class="donation-stats">
              <div class="stat-item">
                <div class="stat-label">捐赠比例</div>
                <div class="stat-value">{{ activity.donationRate }}%</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">累计捐赠</div>
                <div class="stat-value">¥{{ activity.totalDonation }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">参与人数</div>
                <div class="stat-value">{{ activity.currentParticipants }}人</div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 评论区域 -->
        <div class="comments-section">
          <h3>评论 ({{ activity.commentsCount }})</h3>

          <!-- 发表评论 -->
          <div class="comment-input" v-if="isLogin">
            <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="写下你的评论..."
                maxlength="500"
                show-word-limit
            />
            <div class="comment-actions">
              <el-button type="primary" @click="submitComment">发表评论</el-button>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <el-avatar :src="comment.userAvatar || '/default-avatar.png'" />
                <div class="comment-user">
                  <div class="user-name">{{ comment.userName }}</div>
                  <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                </div>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions">
                <el-button type="text" @click="replyComment(comment)">回复</el-button>
              </div>
            </div>

            <!-- 分页 -->
            <el-pagination
                v-model:current-page="commentPage"
                v-model:page-size="commentPageSize"
                :total="commentTotal"
                layout="prev, pager, next"
                @current-change="loadComments"
                small
            />
          </div>
        </div>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :span="8">
        <!-- 参与信息 -->
        <el-card class="participation-card">
          <template #header>
            <span>参与信息</span>
          </template>

          <div class="participation-stats">
            <div class="stat-item">
              <div class="stat-label">最大人数</div>
              <div class="stat-value">
                {{ activity.maxParticipants === 0 ? '不限' : activity.maxParticipants + '人' }}
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-label">当前人数</div>
              <div class="stat-value">{{ activity.currentParticipants }}人</div>
            </div>

            <!-- 进度条 -->
            <div v-if="activity.maxParticipants > 0" class="participation-progress">
              <el-progress
                  :percentage="getParticipationRate()"
                  :show-text="false"
              />
              <div class="progress-text">
                已报名 {{ activity.currentParticipants }}/{{ activity.maxParticipants }}
              </div>
            </div>
          </div>

          <!-- 报名列表 -->
          <div v-if="activity.activityType === 'offline'" class="apply-list">
            <h4>报名列表</h4>
            <div v-for="apply in applies" :key="apply.id" class="apply-item">
              <div class="apply-user">{{ apply.userName }}</div>
              <div class="apply-time">{{ formatTime(apply.createTime) }}</div>
            </div>
          </div>
        </el-card>

        <!-- 相关活动 -->
        <el-card class="related-activities">
          <template #header>
            <span>推荐活动</span>
          </template>
          <div v-for="item in relatedActivities" :key="item.id" class="related-item">
            <div class="related-title">{{ item.title }}</div>
            <div class="related-meta">
              <span>{{ getStatusText(item.status) }}</span>
              <span>{{ formatDate(item.startTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 报名对话框 -->
    <el-dialog
        v-model="showApplyDialog"
        title="报名参加活动"
        width="500px"
    >
      <el-form ref="applyForm" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="applyForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="applyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="报名理由" prop="applyReason">
          <el-input
              v-model="applyForm.applyReason"
              type="textarea"
              :rows="3"
              placeholder="请简单说明报名理由"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showApplyDialog = false">取消</el-button>
          <el-button type="primary" @click="submitApply">提交报名</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 购买对话框 -->
    <el-dialog
        v-model="showOrderDialog"
        title="购买公益商品"
        width="600px"
    >
      <div v-if="goodsList.length > 0">
        <el-table :data="goodsList" style="width: 100%">
          <el-table-column prop="name" label="商品名称">
            <template #default="{ row }">
              <div class="goods-info">
                <img :src="row.img || '/default-goods.png'" class="goods-img" />
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="store" label="库存" width="80" />
          <el-table-column label="购买数量" width="120">
            <template #default="{ row }">
              <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="row.store"
                  size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.price * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="buyGoods(row)">
                购买
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="no-goods">
        暂无商品可购买
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getActivityDetail,
  likeActivity,
  collectActivity,
  applyActivity,
  getComments,
  addComment,
  getGoodsList,
  createCharityOrder
} from '@/api/charity'

const route = useRoute()
const activityId = route.params.id

// 活动详情
const activity = reactive({
  id: '',
  title: '',
  status: 1,
  activityType: 'offline',
  coverImage: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  organizerName: '',
  storeName: '',
  viewsCount: 0,
  likesCount: 0,
  collectsCount: 0,
  commentsCount: 0,
  maxParticipants: 0,
  currentParticipants: 0,
  donationRate: 5.00,
  totalDonation: 0,
  liked: false,
  collected: false
})

// 登录状态
const isLogin = computed(() => {
  return !!localStorage.getItem('token')
})

// 是否已报名
const isApplied = ref(false)

// 报名列表
const applies = ref([])

// 评论相关
const commentContent = ref('')
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)

// 商品列表
const goodsList = ref([])

// 相关活动
const relatedActivities = ref([])

// 对话框显示控制
const showApplyDialog = ref(false)
const showOrderDialog = ref(false)

// 报名表单
const applyForm = reactive({
  contactName: '',
  contactPhone: '',
  applyReason: ''
})

// 报名验证规则
const applyRules = {
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 加载活动详情
const loadActivityDetail = async () => {
  try {
    const res = await getActivityDetail(activityId)
    Object.assign(activity, res.data)
  } catch (error) {
    console.error('加载活动详情失败:', error)
  }
}

// 加载报名列表
const loadApplies = async () => {
  try {
    const res = await getApplies(activityId)
    applies.value = res.data

    // 检查是否已报名
    const userId = localStorage.getItem('userId')
    isApplied.value = applies.value.some(apply => apply.userId == userId && apply.status === 2)
  } catch (error) {
    console.error('加载报名列表失败:', error)
  }
}

// 加载评论
const loadComments = async () => {
  try {
    const params = {
      pageNum: commentPage.value,
      pageSize: commentPageSize.value
    }
    const res = await getComments(activityId, params)
    comments.value = res.data.records || res.data
    commentTotal.value = res.data.total || res.data.length
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 加载商品列表
const loadGoodsList = async () => {
  if (activity.activityType !== 'donation') return

  try {
    const res = await getGoodsList()
    goodsList.value = res.data.map(goods => ({
      ...goods,
      quantity: 1
    }))
  } catch (error) {
    console.error('加载商品列表失败:', error)
  }
}

// 点赞/取消点赞
const handleLike = async () => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await likeActivity(activityId)
    activity.liked = !activity.liked
    activity.likesCount += activity.liked ? 1 : -1
    ElMessage.success(activity.liked ? '点赞成功' : '取消点赞成功')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 收藏/取消收藏
const handleCollect = async () => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await collectActivity(activityId)
    activity.collected = !activity.collected
    activity.collectsCount += activity.collected ? 1 : -1
    ElMessage.success(activity.collected ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 提交报名
const submitApply = async () => {
  try {
    const params = {
      activityId: activityId,
      applyType: 'user',
      ...applyForm
    }

    await applyActivity(params)
    ElMessage.success('报名成功，等待审核')
    showApplyDialog.value = false
    loadActivityDetail()
    loadApplies()
  } catch (error) {
    ElMessage.error('报名失败：' + error.message)
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const params = {
      activityId: activityId,
      content: commentContent.value,
      parentId: 0
    }

    await addComment(params)
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadComments()
    loadActivityDetail()
  } catch (error) {
    ElMessage.error('评论失败：' + error.message)
  }
}

// 购买商品
const buyGoods = async (goods) => {
  try {
    const res = await createCharityOrder({
      activityId: activityId,
      goodsId: goods.id,
      quantity: goods.quantity
    })

    ElMessage.success('订单创建成功，订单号：' + res.data)
    showOrderDialog.value = false

    // 跳转到支付页面
    router.push(`/order/pay/${res.data}`)
  } catch (error) {
    ElMessage.error('购买失败：' + error.message)
  }
}

// 回复评论
const replyComment = (comment) => {
  commentContent.value = `回复 ${comment.userName}：`
}

// 状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '待开始',
    2: '进行中',
    3: '已结束',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 状态样式类
const getStatusClass = (status) => {
  const classMap = {
    1: 'status-pending',
    2: 'status-active',
    3: 'status-ended',
    4: 'status-cancelled'
  }
  return classMap[status] || ''
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[1].substr(0, 5)
}

// 计算参与率
const getParticipationRate = () => {
  if (activity.maxParticipants === 0) return 0
  return Math.round((activity.currentParticipants / activity.maxParticipants) * 100)
}

// 初始化
onMounted(() => {
  loadActivityDetail()
  loadApplies()
  loadComments()
  loadGoodsList()
})
</script>

<style scoped>
.charity-activity-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.activity-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.activity-header h1 {
  margin: 0 0 15px 0;
  font-size: 24px;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  color: white;
  font-size: 12px;
}

.status-pending {
  background-color: #e6a23c;
}

.status-active {
  background-color: #67c23a;
}

.status-ended {
  background-color: #909399;
}

.status-cancelled {
  background-color: #f56c6c;
}

.activity-time,
.activity-location {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  color: #666;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-buttons .el-button {
  width: 100%;
}

.cover-image {
  margin-bottom: 20px;
}

.cover-image img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.activity-description,
.donation-info,
.comments-section {
  margin-bottom: 30px;
}

.activity-description h3,
.donation-info h3,
.comments-section h3 {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.donation-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-label {
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.comment-input {
  margin-bottom: 20px;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.comment-user {
  margin-left: 10px;
}

.user-name {
  font-weight: bold;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  line-height: 1.6;
}

.participation-card,
.related-activities {
  margin-bottom: 20px;
}

.participation-stats {
  padding: 10px 0;
}

.participation-progress {
  margin-top: 20px;
}

.progress-text {
  text-align: center;
  margin-top: 5px;
  color: #666;
  font-size: 12px;
}

.apply-list {
  margin-top: 20px;
}

.apply-list h4 {
  margin-bottom: 10px;
}

.apply-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.related-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.related-title {
  font-size: 14px;
  margin-bottom: 5px;
}

.related-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.goods-info {
  display: flex;
  align-items: center;
}

.goods-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  margin-right: 10px;
  border-radius: 4px;
}

.no-goods {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>