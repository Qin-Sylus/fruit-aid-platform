<!-- src/views/merchant/StoreApply.vue -->
<template>
  <div class="store-apply">
    <el-card class="apply-card">
      <div class="card-header">
        <h2>店铺活动报名</h2>
        <p>为您的店铺报名参与慈善活动</p>
      </div>

      <div class="apply-content">
        <!-- 活动选择 -->
        <div class="section">
          <h3>选择活动</h3>
          <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="120px">
            <el-form-item label="选择活动" prop="activityId">
              <el-select
                  v-model="applyForm.activityId"
                  placeholder="请选择要报名的活动"
                  style="width: 100%"
                  @change="handleActivityChange"
              >
                <el-option
                    v-for="activity in availableActivities"
                    :key="activity.id"
                    :label="activity.title"
                    :value="activity.id"
                >
                  <div class="activity-option">
                    <span class="activity-title">{{ activity.title }}</span>
                    <span class="activity-status" :class="getStatusClass(activity.status)">
                      {{ getStatusText(activity.status) }}
                    </span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>

            <!-- 活动信息展示 -->
            <div v-if="selectedActivity" class="activity-info-preview">
              <el-card shadow="never">
                <div class="preview-header">
                  <h4>{{ selectedActivity.title }}</h4>
                  <el-tag :type="getStatusTagType(selectedActivity.status)">
                    {{ getStatusText(selectedActivity.status) }}
                  </el-tag>
                </div>
                <div class="preview-content">
                  <p><strong>活动类型：</strong>{{ selectedActivity.activityType === 'offline' ? '线下活动' : '公益订单' }}</p>
                  <p><strong>活动时间：</strong>{{ selectedActivity.startTime }} 至 {{ selectedActivity.endTime }}</p>
                  <p v-if="selectedActivity.location"><strong>活动地点：</strong>{{ selectedActivity.location }}</p>
                  <p><strong>参与人数：</strong>{{ selectedActivity.currentParticipants }}/{{ selectedActivity.maxParticipants || '不限' }}</p>
                  <p><strong>捐赠比例：</strong>{{ selectedActivity.donationRate || 0 }}%</p>
                </div>
              </el-card>
            </div>

            <!-- 店铺信息 -->
            <el-form-item label="参与店铺" prop="storeId">
              <el-select
                  v-model="applyForm.storeId"
                  placeholder="请选择参与店铺"
                  style="width: 100%"
              >
                <el-option
                    v-for="store in userStores"
                    :key="store.id"
                    :label="store.name"
                    :value="store.id"
                >
                  <div class="store-option">
                    <span class="store-name">{{ store.name }}</span>
                    <span class="store-category">{{ store.category }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>

            <!-- 报名信息 -->
            <el-form-item label="参与方式" prop="participationType">
              <el-radio-group v-model="applyForm.participationType">
                <el-radio label="full">全程参与</el-radio>
                <el-radio label="partial">部分参与</el-radio>
                <el-radio label="sponsor">赞助参与</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="参与人数" prop="participantCount">
              <el-input-number
                  v-model="applyForm.participantCount"
                  :min="1"
                  :max="selectedActivity ? (selectedActivity.maxParticipants - selectedActivity.currentParticipants) : 10"
                  placeholder="请输入参与人数"
              />
              <span class="count-tips" v-if="selectedActivity">
                活动剩余名额：{{ selectedActivity.maxParticipants - selectedActivity.currentParticipants }} 人
              </span>
            </el-form-item>

            <el-form-item label="店铺商品" prop="productIds">
              <el-select
                  v-model="applyForm.productIds"
                  multiple
                  placeholder="请选择参与活动的商品"
                  style="width: 100%"
              >
                <el-option
                    v-for="product in storeProducts"
                    :key="product.id"
                    :label="product.name"
                    :value="product.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="备注说明" prop="remarks">
              <el-input
                  v-model="applyForm.remarks"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入报名备注（如特殊需求、联系方式等）"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <el-button
              type="primary"
              @click="submitApply"
              :loading="loading"
              :disabled="!selectedActivity || !applyForm.storeId"
          >
            提交报名
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 我的报名记录 -->
    <div class="my-applications" v-if="myApplications.length > 0">
      <el-card class="applications-card">
        <div class="card-header">
          <h3>我的报名记录</h3>
        </div>
        <el-table :data="myApplications" stripe style="width: 100%">
          <el-table-column prop="activityTitle" label="活动名称" width="200" />
          <el-table-column prop="storeName" label="店铺名称" width="150" />
          <el-table-column prop="status" label="审核状态" width="120">
            <template #default="scope">
              <el-tag :type="getApplyStatusTagType(scope.row.status)">
                {{ getApplyStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180" />
          <el-table-column prop="remarks" label="备注" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                  type="danger"
                  size="small"
                  @click="cancelApply(scope.row.id)"
                  v-if="scope.row.status === 1"
              >
                取消报名
              </el-button>
              <span v-else>已{{ getApplyStatusText(scope.row.status) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 模拟数据
const availableActivities = ref([
  {
    id: 1,
    title: '水果捐赠公益活动',
    activityType: 'offline',
    status: 2,
    startTime: '2024-12-01 10:00:00',
    endTime: '2024-12-31 18:00:00',
    location: '北京市朝阳区',
    currentParticipants: 15,
    maxParticipants: 50,
    donationRate: 5.0
  },
  {
    id: 2,
    title: '爱心果园认养计划',
    activityType: 'offline',
    status: 1,
    startTime: '2024-12-10 09:00:00',
    endTime: '2025-01-10 18:00:00',
    location: '上海市浦东新区',
    currentParticipants: 8,
    maxParticipants: 30,
    donationRate: 3.0
  }
])

const userStores = ref([
  { id: 1, name: '新鲜水果店', category: '水果零售' },
  { id: 2, name: '果园直营店', category: '水果批发' }
])

const storeProducts = ref([
  { id: 1, name: '红富士苹果' },
  { id: 2, name: '赣南脐橙' },
  { id: 3, name: '泰国金枕榴莲' },
  { id: 4, name: '海南椰子' }
])

const myApplications = ref([
  {
    id: 1,
    activityTitle: '水果捐赠公益活动',
    storeName: '新鲜水果店',
    status: 1, // 1: 待审核, 2: 已通过, 3: 已拒绝, 4: 已取消
    applyTime: '2024-11-30 14:30:00',
    remarks: '我们希望捐赠200斤水果'
  }
])

// 表单数据
const applyForm = reactive({
  activityId: '',
  storeId: '',
  participationType: 'full',
  participantCount: 1,
  productIds: [],
  remarks: ''
})

// 表单验证规则
const applyRules = {
  activityId: [{ required: true, message: '请选择活动', trigger: 'change' }],
  storeId: [{ required: true, message: '请选择店铺', trigger: 'change' }],
  participantCount: [
    { required: true, message: '请输入参与人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '参与人数至少1人', trigger: 'blur' }
  ]
}

// 选中的活动
const selectedActivity = ref(null)

// 加载状态
const loading = ref(false)

// 表单引用
const applyFormRef = ref(null)

// 处理活动选择变化
const handleActivityChange = (activityId) => {
  selectedActivity.value = availableActivities.value.find(a => a.id === activityId)
  // 重置参与人数为1
  applyForm.participantCount = 1
}

// 状态文本转换
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

// 状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    1: 'warning',
    2: 'success',
    3: 'info',
    4: 'danger'
  }
  return typeMap[status] || ''
}

// 报名状态文本
const getApplyStatusText = (status) => {
  const statusMap = {
    1: '待审核',
    2: '已通过',
    3: '已拒绝',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 报名状态标签类型
const getApplyStatusTagType = (status) => {
  const typeMap = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  }
  return typeMap[status] || ''
}

// 提交报名
const submitApply = async () => {
  try {
    await applyFormRef.value.validate()

    loading.value = true

    // 这里应该调用API提交报名
    // const res = await applyActivity(applyForm)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('报名提交成功，请等待审核')

    // 添加到我的报名记录
    myApplications.value.unshift({
      id: Date.now(),
      activityTitle: selectedActivity.value.title,
      storeName: userStores.value.find(s => s.id === applyForm.storeId)?.name || '',
      status: 1,
      applyTime: new Date().toLocaleString(),
      remarks: applyForm.remarks
    })

    // 重置表单
    resetForm()

  } catch (error) {
    if (error.errors) {
      const firstError = Object.values(error.errors)[0][0].message
      ElMessage.error(firstError)
    } else {
      ElMessage.error('提交失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 取消报名
const cancelApply = async (applyId) => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？', '提示', {
      type: 'warning'
    })

    // 这里应该调用API取消报名
    // await cancelParticipation(applyId)

    const index = myApplications.value.findIndex(app => app.id === applyId)
    if (index !== -1) {
      myApplications.value[index].status = 4 // 已取消
    }

    ElMessage.success('报名已取消')

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  applyFormRef.value.resetFields()
  selectedActivity.value = null
  applyForm.participantCount = 1
  applyForm.productIds = []
}

// 初始化加载数据
onMounted(() => {
  // 这里可以加载真实数据
  // loadAvailableActivities()
  // loadUserStores()
  // loadMyApplications()
})
</script>

<style scoped>
.store-apply {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.apply-card {
  margin-bottom: 30px;
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.card-header p {
  color: #666;
  font-size: 14px;
}

.section {
  margin-bottom: 30px;
}

.section h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.activity-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-title {
  flex: 1;
}

.activity-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background-color: #f0f0f0;
  color: #666;
}

.status-pending {
  background-color: #e6a23c;
  color: white;
}

.status-active {
  background-color: #67c23a;
  color: white;
}

.status-ended {
  background-color: #909399;
  color: white;
}

.status-cancelled {
  background-color: #f56c6c;
  color: white;
}

.activity-info-preview {
  margin: 15px 0;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.preview-header h4 {
  margin: 0;
  font-size: 16px;
}

.preview-content p {
  margin: 5px 0;
  color: #666;
}

.store-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.store-name {
  flex: 1;
}

.store-category {
  color: #999;
  font-size: 12px;
}

.count-tips {
  margin-left: 10px;
  color: #666;
  font-size: 12px;
}

.submit-section {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.my-applications {
  margin-top: 30px;
}

.applications-card .card-header {
  margin-bottom: 15px;
}

.applications-card .card-header h3 {
  margin: 0;
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .store-apply {
    padding: 10px;
  }

  .activity-option,
  .store-option {
    flex-direction: column;
    align-items: flex-start;
  }

  .activity-status,
  .store-category {
    margin-top: 4px;
  }
}
</style>