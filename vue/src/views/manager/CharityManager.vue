<template>
  <div class="charity-manager">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>慈善活动管理</h2>
      <p>管理员专属：发布、管理慈善活动</p>
    </div>

    <!-- 功能按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        发布新活动
      </el-button>
      <el-button type="success" @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <el-input
          v-model="searchParams.title"
          placeholder="搜索活动标题"
          style="width: 200px"
          @keyup.enter="search"
      />
      <el-select
          v-model="searchParams.activityType"
          placeholder="活动类型"
          clearable
          style="width: 120px"
      >
        <el-option label="全部类型" value="" />
        <el-option label="线下活动" value="offline" />
        <el-option label="公益订单" value="donation" />
      </el-select>
      <el-select
          v-model="searchParams.status"
          placeholder="活动状态"
          clearable
          style="width: 120px"
      >
        <el-option label="全部状态" value="" />
        <el-option label="待开始" :value="1" />
        <el-option label="进行中" :value="2" />
        <el-option label="已结束" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 活动列表表格 -->
    <div class="activity-table">
      <el-table
          :data="activityList"
          stripe
          style="width: 100%"
          v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" width="200">
          <template #default="{ row }">
            <div class="activity-title">
              <el-avatar :size="40" shape="square" :src="row.coverImage || defaultCover" />
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="activityType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.activityType === 'offline' ? 'primary' : 'success'">
              {{ row.activityType === 'offline' ? '线下活动' : '公益订单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="currentParticipants" label="参与人数" width="120">
          <template #default="{ row }">
            {{ row.currentParticipants }}/{{ row.maxParticipants || '不限' }}
          </template>
        </el-table-column>
        <el-table-column prop="donationRate" label="捐赠比例" width="100">
          <template #default="{ row }">
            {{ row.donationRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="viewsCount" label="浏览" width="80" />
        <el-table-column prop="likesCount" label="点赞" width="80" />
        <el-table-column prop="collectsCount" label="收藏" width="80" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
                size="small"
                type="primary"
                @click="editActivity(row)"
                :disabled="row.status === 3 || row.status === 4"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="warning"
                @click="changeStatus(row)"
                :disabled="row.status === 4"
            >
              {{ getStatusActionText(row.status) }}
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="deleteActivity(row.id)"
                :disabled="row.status !== 1 && row.status !== 4"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 发布/编辑活动对话框 -->
    <el-dialog
        v-model="showAddDialog"
        :title="isEditing ? '编辑活动' : '发布新活动'"
        width="800px"
        @close="resetDialog"
    >
      <el-form
          ref="activityFormRef"
          :model="activityForm"
          :rules="activityRules"
          label-width="120px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input
              v-model="activityForm.title"
              placeholder="请输入活动标题"
              maxlength="100"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动类型" prop="activityType">
          <el-radio-group v-model="activityForm.activityType">
            <el-radio label="offline">线下活动</el-radio>
            <el-radio label="donation">公益订单</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 封面图片上传 -->
        <el-form-item label="封面图片" prop="coverImage">
          <div class="cover-upload-container">
            <div class="cover-preview-section">
              <el-avatar :size="120" shape="square" class="cover-preview">
                <img :src="activityForm.coverImage || defaultCover" alt="封面预览" />
              </el-avatar>
              <div v-if="activityForm.coverFile" class="upload-tip">
                <el-text type="info">新封面已选择，保存后生效</el-text>
              </div>
            </div>

            <div class="upload-controls">
              <el-upload
                  class="cover-uploader"
                  :show-file-list="false"
                  :before-upload="beforeCoverUpload"
                  :http-request="handleCoverUpload"
              >
                <el-button type="primary" size="small">
                  <el-icon><Upload /></el-icon>
                  选择封面图片
                </el-button>
              </el-upload>
              <el-button
                  v-if="activityForm.coverImage && activityForm.coverImage !== defaultCover"
                  type="danger"
                  size="small"
                  @click="removeCover"
              >
                <el-icon><Delete /></el-icon>
                移除封面
              </el-button>
              <div class="upload-tips">
                <el-text type="info" size="small">建议尺寸：300×150px，支持 JPG、PNG 格式，不超过 2MB</el-text>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="活动描述" prop="description">
          <el-input
              v-model="activityForm.description"
              type="textarea"
              :rows="6"
              placeholder="请输入活动详细描述，支持富文本格式"
              maxlength="5000"
              show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                  v-model="activityForm.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                  v-model="activityForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="activityForm.activityType === 'offline'" label="活动地点" prop="location">
          <el-input
              v-model="activityForm.location"
              placeholder="请输入活动地点"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxParticipants">
              <el-input-number
                  v-model="activityForm.maxParticipants"
                  :min="0"
                  :max="10000"
                  placeholder="0表示不限人数"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="捐赠比例" prop="donationRate">
              <el-input-number
                  v-model="activityForm.donationRate"
                  :min="0"
                  :max="100"
                  :step="0.1"
                  placeholder="请输入捐赠比例"
                  style="width: 100%"
              />
              <span style="margin-left: 10px">%</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动状态" prop="status" v-if="isEditing">
          <el-select
              v-model="activityForm.status"
              placeholder="请选择活动状态"
              style="width: 100%"
          >
            <el-option label="待开始" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已结束" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number
              v-model="activityForm.sortOrder"
              :min="0"
              :max="999"
              placeholder="数值越大越靠前"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="submitActivity" :loading="submitLoading">
            {{ isEditing ? '保存修改' : '发布活动' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 状态变更确认对话框 -->
    <el-dialog
        v-model="showStatusDialog"
        title="变更活动状态"
        width="400px"
    >
      <div v-if="selectedActivity">
        <p>确定要将活动 <strong>"{{ selectedActivity.title }}"</strong> 的状态变更为：</p>
        <el-select
            v-model="newStatus"
            placeholder="请选择新的状态"
            style="width: 100%"
        >
          <el-option label="待开始" :value="1" />
          <el-option label="进行中" :value="2" />
          <el-option label="已结束" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStatusDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmStatusChange">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Upload,
  Delete
} from '@element-plus/icons-vue'

// 导入API - 使用真实API调用
import {
  getActivities,
  addActivity,
  updateActivity,
  deleteActivity as deleteActivityApi
} from '@/api/charity.js'

// ============== 图片处理方法 ==============
const defaultCover = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 封面图片上传前校验
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('请上传图片格式文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return isImage && isLt2M
}

// 封面图片选择处理 - 本地预览
const handleCoverUpload = (file) => {
  console.log('选择封面文件:', file.file)

  // 创建本地预览URL
  const previewUrl = URL.createObjectURL(file.file)
  activityForm.coverImage = previewUrl
  activityForm.coverFile = file.file

  ElMessage.success('封面图片已选择，保存后生效')
}

// 移除封面
const removeCover = () => {
  activityForm.coverImage = ''
  activityForm.coverFile = null
  ElMessage.success('封面已移除')
}

// 将文件转换为base64
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 检查base64数据大小
const checkBase64Size = (base64String) => {
  const stringLength = base64String.length - (base64String.includes('base64,') ? base64String.indexOf('base64,') + 7 : 0)
  const sizeInBytes = 4 * Math.ceil((stringLength / 3)) * 0.5624896334383812
  const sizeInMB = sizeInBytes / (1024 * 1024)

  console.log('封面图片Base64数据大小:', sizeInMB.toFixed(2), 'MB')

  if (sizeInMB > 1) {
    ElMessage.warning('封面图片较大，建议使用小于1MB的图片')
    return false
  }
  return true
}
// ============== 图片处理代码结束 ==============

// 搜索参数
const searchParams = reactive({
  title: '',
  activityType: '',
  status: ''
})

// 分页参数
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 活动列表
const activityList = ref([])

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 对话框显示控制
const showAddDialog = ref(false)
const showStatusDialog = ref(false)

// 是否为编辑模式
const isEditing = ref(false)

// 表单引用
const activityFormRef = ref(null)

// 选中的活动（用于状态变更）
const selectedActivity = ref(null)
const newStatus = ref(1)

// 活动表单
const activityForm = reactive({
  id: '',
  title: '',
  activityType: 'offline',
  coverImage: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: 0,
  donationRate: 5.00,
  sortOrder: 0,
  status: 1,
  coverFile: null
})

// 表单验证规则
const activityRules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 3, max: 100, message: '标题长度在3-100个字符之间', trigger: 'blur' }
  ],
  activityType: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && activityForm.startTime && new Date(value) <= new Date(activityForm.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  maxParticipants: [
    { type: 'number', min: 0, max: 10000, message: '最大人数必须在0-10000之间', trigger: 'blur' }
  ],
  donationRate: [
    { type: 'number', min: 0, max: 100, message: '捐赠比例必须在0-100%之间', trigger: 'blur' }
  ]
}

// 加载活动列表 - 调用真实API
const loadActivities = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchParams
    }

    // 过滤空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    console.log('调用真实API - 加载活动列表参数:', params)

    const res = await getActivities(params)
    console.log('真实API响应:', res)

    if (res.code === 200) {
      const data = res.data.records || res.data || []

      // 确保每个活动都有默认封面
      activityList.value = data.map(activity => ({
        ...activity,
        coverImage: activity.coverImage || defaultCover
      }))

      total.value = res.data.total || data.length || 0

      console.log('加载到活动数量:', activityList.value.length)
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)

    // 如果API调用失败，显示空列表
    activityList.value = []
    total.value = 0

    ElMessage.error('加载数据失败: ' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 搜索
const search = () => {
  pageNum.value = 1
  loadActivities()
}

// 重置搜索
const resetSearch = () => {
  searchParams.title = ''
  searchParams.activityType = ''
  searchParams.status = ''
  pageNum.value = 1
  loadActivities()
}

// 刷新数据
const refreshData = () => {
  loadActivities()
  ElMessage.success('数据已刷新')
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  loadActivities()
}

// 当前页改变
const handleCurrentChange = (page) => {
  pageNum.value = page
  loadActivities()
}

// 编辑活动
const editActivity = (activity) => {
  isEditing.value = true
  Object.assign(activityForm, activity)
  // 如果是base64图片，设置为默认封面
  if (activity.coverImage && activity.coverImage.startsWith('data:image')) {
    activityForm.coverImage = defaultCover
  }
  showAddDialog.value = true
}

// 删除活动 - 调用真实API
const deleteActivity = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？删除后将无法恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })

    console.log('调用真实API - 删除活动，ID:', id)
    const res = await deleteActivityApi(id)
    console.log('删除活动API响应:', res)

    if (res.code === 200) {
      ElMessage.success('活动删除成功')
      loadActivities()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除活动失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 变更活动状态
const changeStatus = (activity) => {
  selectedActivity.value = activity
  newStatus.value = activity.status
  showStatusDialog.value = true
}

// 确认状态变更
const confirmStatusChange = async () => {
  if (!selectedActivity.value || selectedActivity.value.status === newStatus.value) {
    showStatusDialog.value = false
    return
  }

  try {
    // 这里需要调用更新活动状态的API
    // 暂时先更新本地数据
    const index = activityList.value.findIndex(a => a.id === selectedActivity.value.id)
    if (index !== -1) {
      activityList.value[index].status = newStatus.value
    }

    ElMessage.success('活动状态更新成功')
    showStatusDialog.value = false
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 重置对话框
const resetDialog = () => {
  activityFormRef.value?.clearValidate()
  Object.assign(activityForm, {
    id: '',
    title: '',
    activityType: 'offline',
    coverImage: '',
    description: '',
    startTime: '',
    endTime: '',
    location: '',
    maxParticipants: 0,
    donationRate: 5.00,
    sortOrder: 0,
    status: 1,
    coverFile: null
  })
  isEditing.value = false
}

// 提交活动 - 调用真实API
const submitActivity = async () => {
  try {
    await activityFormRef.value.validate()

    submitLoading.value = true

    let finalCoverImage = activityForm.coverImage

    // 处理封面图片
    if (activityForm.coverFile) {
      try {
        ElMessage.info('正在处理封面图片...')
        finalCoverImage = await fileToBase64(activityForm.coverFile)

        if (!checkBase64Size(finalCoverImage)) {
          return
        }
      } catch (error) {
        console.error('封面图片处理失败:', error)
        ElMessage.warning('封面图片处理失败，将使用默认图片')
        finalCoverImage = defaultCover
      }
    } else if (!activityForm.coverImage || activityForm.coverImage === defaultCover) {
      finalCoverImage = defaultCover
    }

    // 准备提交数据
    const submitData = {
      title: activityForm.title,
      activityType: activityForm.activityType,
      coverImage: finalCoverImage,
      description: activityForm.description,
      startTime: activityForm.startTime,
      endTime: activityForm.endTime,
      location: activityForm.location || '',
      maxParticipants: activityForm.maxParticipants,
      donationRate: activityForm.donationRate,
      status: activityForm.status
    }

    console.log('准备提交的数据:', submitData)
    console.log('调用真实API - 提交活动数据')

    let res
    if (isEditing.value) {
      // 编辑活动 - 调用真实API
      submitData.id = activityForm.id
      console.log('编辑活动，ID:', activityForm.id)
      res = await updateActivity(activityForm.id, submitData)
    } else {
      // 添加活动 - 调用真实API
      console.log('添加活动')
      res = await addActivity(submitData)
    }

    console.log('真实API响应状态码:', res.code)
    console.log('真实API响应消息:', res.msg)
    console.log('真实API响应数据:', res.data)

    if (res.code === 200) {
      ElMessage.success(isEditing.value ? '活动修改成功' : '活动发布成功')
      showAddDialog.value = false
      resetDialog()
      // 重新加载列表
      loadActivities()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交错误详情:', error)
    console.error('错误消息:', error.message)
    console.error('错误堆栈:', error.stack)

    if (error.errors) {
      const firstError = Object.values(error.errors)[0][0].message
      ElMessage.error(firstError)
    } else if (error.message && error.message.includes('Network Error')) {
      ElMessage.error('网络连接失败，请检查后端服务是否正常运行')
    } else if (error.message && error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.response) {
      console.error('服务器错误响应:', error.response)
      ElMessage.error(`服务器错误: ${error.response.status} ${error.response.data?.msg || ''}`)
    } else if (error.request) {
      ElMessage.error('网络请求失败，请检查后端服务状态')
    } else {
      ElMessage.error(`请求失败: ${error.message || '未知错误，请重试'}`)
    }
  } finally {
    submitLoading.value = false
  }
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

// 获取状态变更按钮文本
const getStatusActionText = (status) => {
  const actionMap = {
    1: '开始活动',
    2: '结束活动',
    3: '重新开始',
    4: '恢复活动'
  }
  return actionMap[status] || '变更状态'
}

// 初始化
onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.charity-manager {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.action-buttons {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.action-buttons .el-button {
  margin-right: 10px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-bar .el-input,
.search-bar .el-select {
  margin-right: 10px;
}

.activity-table {
  margin-bottom: 20px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.activity-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.activity-title .el-avatar {
  flex-shrink: 0;
}

.activity-title span {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pagination {
  padding: 20px;
  background: white;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 封面图片上传样式 */
.cover-upload-container {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fafafa;
}

.cover-preview-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.cover-preview {
  border: 1px solid #dcdfe6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.upload-tip {
  text-align: center;
}

.upload-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-tips {
  margin-top: 5px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cover-upload-container {
    flex-direction: column;
    align-items: center;
  }

  .upload-controls {
    width: 100%;
    text-align: center;
  }

  .activity-table {
    overflow-x: auto;
  }
}
</style>