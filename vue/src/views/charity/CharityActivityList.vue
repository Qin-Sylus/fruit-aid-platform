<template>
  <div class="charity-activity-list">
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
        <el-option label="线下活动" value="offline" />
        <el-option label="公益订单" value="donation" />
      </el-select>
      <el-select
          v-model="searchParams.status"
          placeholder="活动状态"
          clearable
          style="width: 120px"
      >
        <el-option label="待开始" :value="1" />
        <el-option label="进行中" :value="2" />
        <el-option label="已结束" :value="3" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="reset">重置</el-button>

      <!-- 管理员发布活动按钮 -->
      <el-button
          v-if="userRole === 'ADMIN'"
          type="success"
          @click="showAddDialog = true"
          style="float: right"
      >
        发布活动
      </el-button>
    </div>

    <div class="activity-list">
      <el-row :gutter="20">
        <el-col
            v-for="activity in activityList"
            :key="activity.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
        >
          <el-card class="activity-card" @click="goToDetail(activity.id)">
            <div class="cover-image">
              <!-- 使用Element Plus的默认图片作为默认封面 -->
              <el-avatar v-if="!activity.coverImage || activity.coverImage === defaultCover" :size="150" shape="square" class="default-cover">
                <img :src="defaultCover" />
              </el-avatar>
              <img v-else :src="activity.coverImage" alt="封面" />
              <div class="status-tag" :class="getStatusClass(activity.status)">
                {{ getStatusText(activity.status) }}
              </div>
            </div>

            <div class="activity-info">
              <h3>{{ activity.title }}</h3>
              <div class="meta-info">
                <span><i class="el-icon-date"></i> {{ formatDate(activity.startTime) }}</span>
                <span v-if="activity.location">
                  <i class="el-icon-location"></i> {{ activity.location }}
                </span>
              </div>

              <div class="stats">
                <span><i class="el-icon-view"></i> {{ activity.viewsCount }}</span>
                <span><i class="el-icon-star-off"></i> {{ activity.likesCount }}</span>
                <span><i class="el-icon-collection"></i> {{ activity.collectsCount }}</span>
              </div>

              <div class="progress-bar" v-if="activity.maxParticipants > 0">
                <el-progress
                    :percentage="getParticipationRate(activity)"
                    :show-text="false"
                />
                <div class="progress-text">
                  已报名：{{ activity.currentParticipants }}/{{ activity.maxParticipants }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 发布活动对话框 -->
    <el-dialog
        v-model="showAddDialog"
        title="发布公益活动"
        width="700px"
        @close="resetDialog"
    >
      <el-form ref="activityFormRef" :model="activityForm" :rules="activityRules" label-width="120px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="activityForm.title" placeholder="请输入活动标题" />
        </el-form-item>

        <el-form-item label="活动类型" prop="activityType">
          <el-radio-group v-model="activityForm.activityType">
            <el-radio label="offline">线下活动</el-radio>
            <el-radio label="donation">公益订单</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 封面图片上传区域 - 使用与个人中心相同的方法 -->
        <el-form-item label="封面图片" prop="coverImage">
          <div class="cover-upload-container">
            <!-- 封面预览 -->
            <div class="cover-preview-section">
              <el-avatar :size="120" shape="square" class="cover-preview">
                <img :src="activityForm.coverImage || defaultCover" alt="封面预览" />
              </el-avatar>
              <div v-if="activityForm.coverFile" class="upload-tip">
                <el-text type="info">新封面已选择，保存后生效</el-text>
              </div>
            </div>

            <!-- 上传组件 -->
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
              :rows="4"
              placeholder="请输入活动详细描述"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
              v-model="activityForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
              v-model="activityForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item v-if="activityForm.activityType === 'offline'" label="活动地点" prop="location">
          <el-input v-model="activityForm.location" placeholder="请输入活动地点" />
        </el-form-item>

        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number
              v-model="activityForm.maxParticipants"
              :min="0"
              :max="10000"
              placeholder="0表示不限人数"
          />
        </el-form-item>

        <el-form-item label="捐赠比例" prop="donationRate">
          <el-input-number
              v-model="activityForm.donationRate"
              :min="0"
              :max="100"
              :step="0.1"
              placeholder="请输入捐赠比例"
          />
          <span style="margin-left: 10px">%</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="submitActivity" :loading="loading">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Upload, Delete, Picture } from '@element-plus/icons-vue'

// 导入API
import { getActivities, addActivity } from '@/api/charity.js'

const router = useRouter()

// ============== 图片处理相关代码（与个人中心保持一致） ==============
// 使用Element Plus的默认图片作为默认封面
const defaultCover = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 加载状态
const loading = ref(false)

// 封面图片上传前校验
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('请上传图片格式文件！');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！');
    return false;
  }
  return isImage && isLt2M;
};

// 封面图片选择处理 - 本地预览
const handleCoverUpload = (file) => {
  console.log('选择封面文件:', file.file);

  // 创建本地预览URL
  const previewUrl = URL.createObjectURL(file.file);
  activityForm.coverImage = previewUrl;
  activityForm.coverFile = file.file;

  ElMessage.success('封面图片已选择，保存后生效');
};

// 移除封面
const removeCover = () => {
  activityForm.coverImage = '';
  activityForm.coverFile = null;
  ElMessage.success('封面已移除');
};

// 将文件转换为base64
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => resolve(e.target.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
};

// 检查base64数据大小
const checkBase64Size = (base64String) => {
  const stringLength = base64String.length - (base64String.includes('base64,') ? base64String.indexOf('base64,') + 7 : 0);
  const sizeInBytes = 4 * Math.ceil((stringLength / 3)) * 0.5624896334383812;
  const sizeInMB = sizeInBytes / (1024 * 1024);

  console.log('封面图片Base64数据大小:', sizeInMB.toFixed(2), 'MB');

  // 如果超过1MB，建议压缩
  if (sizeInMB > 1) {
    ElMessage.warning('封面图片较大，建议使用小于1MB的图片');
    return false;
  }
  return true;
};
// ============== 图片处理代码结束 ==============

// 搜索参数
const searchParams = reactive({
  title: '',
  activityType: '',
  status: null
})

// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 活动列表
const activityList = ref([])

// 用户角色
const userRole = localStorage.getItem('role') || 'USER'

// 对话框显示控制
const showAddDialog = ref(false)

// 表单引用
const activityFormRef = ref(null)

// 活动表单
const activityForm = reactive({
  title: '',
  activityType: 'offline',
  coverImage: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: 0,
  donationRate: 5.00,
  coverFile: null // 新增：上传的文件对象
})

// 表单验证规则
const activityRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// 加载活动列表
const loadActivities = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchParams
    }

    const res = await getActivities(params)

    // 处理响应数据，确保每个活动都有默认封面
    if (res.code === 200) {
      const data = res.data.records || res.data || []

      activityList.value = data.map(activity => ({
        ...activity,
        coverImage: activity.coverImage || defaultCover // 使用默认图片
      }))

      total.value = res.data.total || data.length || 0
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    // 使用模拟数据并添加默认封面
    activityList.value = [
      {
        id: 1,
        title: '水果捐赠公益活动',
        coverImage: defaultCover,
        status: 2,
        startTime: '2024-12-01 10:00:00',
        location: '北京市朝阳区',
        viewsCount: 120,
        likesCount: 45,
        collectsCount: 23,
        currentParticipants: 15,
        maxParticipants: 50
      },
      {
        id: 2,
        title: '爱心果园认养计划',
        coverImage: defaultCover,
        status: 1,
        startTime: '2024-12-10 09:00:00',
        location: '上海市浦东新区',
        viewsCount: 85,
        likesCount: 32,
        collectsCount: 18,
        currentParticipants: 8,
        maxParticipants: 30
      }
    ]
    total.value = activityList.value.length
  }
}

// 搜索
const search = () => {
  pageNum.value = 1
  loadActivities()
}

// 重置搜索
const reset = () => {
  searchParams.title = ''
  searchParams.activityType = ''
  searchParams.status = null
  pageNum.value = 1
  loadActivities()
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

// 跳转到活动详情
const goToDetail = (id) => {
  router.push(`/charity/activity/${id}`)
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

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

// 计算参与率
const getParticipationRate = (activity) => {
  if (activity.maxParticipants === 0) return 0
  return Math.round((activity.currentParticipants / activity.maxParticipants) * 100)
}

// 重置对话框
const resetDialog = () => {
  Object.assign(activityForm, {
    title: '',
    activityType: 'offline',
    coverImage: '',
    description: '',
    startTime: '',
    endTime: '',
    location: '',
    maxParticipants: 0,
    donationRate: 5.00,
    coverFile: null
  })

  if (activityFormRef.value) {
    activityFormRef.value.clearValidate()
  }
}

// 提交活动 - 使用与个人中心相同的处理逻辑
const submitActivity = async () => {
  try {
    loading.value = true

    // 验证表单
    await activityFormRef.value.validate()

    let finalCoverImage = activityForm.coverImage;

    // 如果有新封面文件，处理图片
    if (activityForm.coverFile) {
      try {
        ElMessage.info('正在处理封面图片...');
        finalCoverImage = await fileToBase64(activityForm.coverFile);
        console.log('封面图片转换为base64成功，长度:', finalCoverImage.length);

        // 检查大小
        if (!checkBase64Size(finalCoverImage)) {
          ElMessage.warning('封面图片文件过大，请选择较小的图片');
          return;
        }
      } catch (error) {
        console.error('封面图片处理失败:', error);
        ElMessage.warning('封面图片处理失败，将使用默认图片');
        finalCoverImage = defaultCover;
      }
    } else if (!activityForm.coverImage) {
      // 如果没有选择封面，使用默认图片
      finalCoverImage = defaultCover;
    }

    // 准备提交数据
    const submitData = {
      title: activityForm.title,
      activityType: activityForm.activityType,
      coverImage: finalCoverImage,
      description: activityForm.description,
      startTime: activityForm.startTime,
      endTime: activityForm.endTime,
      location: activityForm.location,
      maxParticipants: activityForm.maxParticipants,
      donationRate: activityForm.donationRate
    };

    console.log('提交活动数据:', submitData);

    // 调用API
    const res = await addActivity(submitData)

    if (res.code === 200) {
      ElMessage.success('活动发布成功')
      showAddDialog.value = false
      resetDialog()
      loadActivities()
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch (error) {
    console.error('提交错误:', error);

    // 处理不同类型的错误
    if (error.errors) {
      // 表单验证错误
      const firstError = Object.values(error.errors)[0][0].message;
      ElMessage.error(firstError);
    } else if (error.message && error.message.includes('Network Error')) {
      ElMessage.error('网络连接错误，请检查网络连接');
    } else {
      ElMessage.error(error.message || '保存失败，请重试');
    }
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.charity-activity-list {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-bar .el-input,
.search-bar .el-select {
  margin-right: 10px;
}

.activity-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
}

.cover-image {
  position: relative;
  height: 150px;
  overflow: hidden;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background-color: #f5f5f5;
}

.default-cover {
  width: 100%;
  height: 150px;
  border-radius: 0;
}

.default-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
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

.activity-info {
  padding: 10px 0;
}

.activity-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 10px;
  color: #666;
  font-size: 12px;
}

.meta-info i {
  margin-right: 5px;
}

.stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #999;
  font-size: 12px;
}

.progress-bar {
  margin-top: 10px;
}

.progress-text {
  font-size: 12px;
  color: #666;
  text-align: center;
  margin-top: 5px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

/* 封面图片上传样式 */
.cover-upload-container {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.cover-preview-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.cover-preview {
  border: 1px solid #f0f0f0;
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

.cover-uploader {
  display: inline-block;
}

.upload-tips {
  margin-top: 5px;
}

/* 适配小屏幕 */
@media (max-width: 768px) {
  .cover-upload-container {
    flex-direction: column;
    align-items: center;
  }

  .upload-controls {
    width: 100%;
    text-align: center;
  }
}
</style>