<template>
  <div class="store-manage">
    <div class="page-header">
      <h2>店铺管理</h2>
      <p>管理您的店铺信息和设置</p>
    </div>

    <div class="manage-container">
      <!-- 店铺基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span class="header-title">店铺基本信息</span>
            <el-button
                type="primary"
                size="small"
                @click="editMode = true"
                v-if="!editMode"
            >
              编辑信息
            </el-button>
          </div>
        </template>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="6" animated />
        </div>

        <template v-else>
          <!-- 查看模式 -->
          <div v-if="!editMode" class="view-mode">
            <div class="info-row">
              <span class="info-label">店铺名称：</span>
              <span class="info-value">{{ storeInfo.name || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">店铺Logo：</span>
              <div class="logo-preview">
                <el-image
                    :src="storeInfo.logo || defaultLogo"
                    class="store-logo"
                    fit="cover"
                >
                  <template #error>
                    <div class="logo-placeholder">
                      <el-icon><Shop /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
            </div>
            <div class="info-row">
              <span class="info-label">店铺描述：</span>
              <span class="info-value">{{ storeInfo.description || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">店铺类型：</span>
              <span class="info-value">{{ getTypeName(storeInfo.type) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">联系电话：</span>
              <span class="info-value">{{ storeInfo.phone || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">店铺地址：</span>
              <span class="info-value">{{ storeInfo.address || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">店铺状态：</span>
              <el-tag :type="storeInfo.status === 1 ? 'success' : 'danger'">
                {{ storeInfo.status === 1 ? '正常营业' : '已关闭' }}
              </el-tag>
            </div>
            <div class="info-row">
              <span class="info-label">创建时间：</span>
              <span class="info-value">{{ formatDate(storeInfo.createTime) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">最后更新：</span>
              <span class="info-value">{{ formatDate(storeInfo.updateTime) }}</span>
            </div>
          </div>

          <!-- 编辑模式 -->
          <el-form
              v-else
              :model="editForm"
              ref="editFormRef"
              :rules="editRules"
              label-width="100px"
              class="edit-form"
          >
            <el-form-item label="店铺名称" prop="name">
              <el-input
                  v-model="editForm.name"
                  placeholder="请输入店铺名称"
                  maxlength="50"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item label="店铺Logo" prop="logo">
              <div class="logo-upload-section">
                <el-image
                    v-if="editForm.logo || logoPreviewUrl"
                    :src="logoPreviewUrl || editForm.logo"
                    class="edit-logo"
                    fit="cover"
                >
                  <template #error>
                    <div class="logo-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>

                <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :before-upload="beforeLogoUpload"
                    :http-request="handleLogoUpload"
                >
                  <el-button type="primary" size="small" class="upload-btn">
                    <el-icon><Plus /></el-icon>
                    {{ editForm.logo || logoPreviewUrl ? '更换Logo' : '上传Logo' }}
                  </el-button>
                </el-upload>

                <div v-if="logoFile" class="upload-tip">
                  <el-text type="info">新Logo已选择，保存后生效</el-text>
                </div>
              </div>
              <div class="upload-tip">建议尺寸：300x300px，大小不超过2MB</div>
            </el-form-item>

            <el-form-item label="店铺描述" prop="description">
              <el-input
                  v-model="editForm.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入店铺描述"
                  maxlength="500"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item label="店铺类型" prop="type">
              <el-select v-model="editForm.type" placeholder="请选择店铺类型" style="width: 100%">
                <el-option label="水果生鲜" value="FRUIT"></el-option>
                <el-option label="农产品" value="AGRICULTURAL"></el-option>
                <el-option label="特产食品" value="SPECIALTY"></el-option>
                <el-option label="有机食品" value="ORGANIC"></el-option>
                <el-option label="其他" value="OTHER"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="联系电话" prop="phone">
              <el-input
                  v-model="editForm.phone"
                  placeholder="请输入联系电话"
                  maxlength="11"
              />
            </el-form-item>

            <el-form-item label="店铺地址" prop="address">
              <el-input
                  v-model="editForm.address"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入店铺地址"
                  maxlength="200"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item label="店铺状态" prop="status">
              <el-switch
                  v-model="editForm.status"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="营业中"
                  inactive-text="已关闭"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveEdit" :loading="saving">
                保存修改
              </el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </template>
      </el-card>

      <!-- 店铺统计信息 -->
      <el-card class="stats-card">
        <template #header>
          <span class="header-title">店铺统计</span>
        </template>

        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.goodsCount }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon">
              <el-icon><Tickets /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.pendingOrders }}</div>
              <div class="stat-label">待处理订单</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.monthlyOrders }}</div>
              <div class="stat-label">本月订单</div>
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ stats.salesAmount?.toFixed(2) || '0.00' }}</div>
              <div class="stat-label">总销售额</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 快捷操作 -->
      <el-card class="action-card">
        <template #header>
          <span class="header-title">快捷操作</span>
        </template>

        <div class="action-buttons">
          <el-button
              type="primary"
              @click="$router.push('/merchant/store-goods')"
              class="action-btn"
          >
            <el-icon><Goods /></el-icon>
            管理商品
          </el-button>

          <el-button
              @click="$router.push('/merchant/store-orders')"
              class="action-btn"
          >
            <el-icon><Tickets /></el-icon>
            查看订单
          </el-button>

          <el-button
              @click="$router.push('/merchant/statistics')"
              class="action-btn"
          >
            <el-icon><Histogram /></el-icon>
            销售统计
          </el-button>

          <el-button
              @click="$router.push('/merchant/home')"
              class="action-btn"
          >
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios'; // 直接导入axios
import {
  Shop,
  Goods,
  Tickets,
  Histogram,
  Picture,
  Plus,
  Calendar,
  Money,
  HomeFilled
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();

// 状态管理
const loading = ref(false);
const editMode = ref(false);
const saving = ref(false);
const editFormRef = ref(null);
const logoFile = ref(null);
const logoPreviewUrl = ref('');

// 默认Logo - 使用在线默认图片
const defaultLogo = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 店铺信息
const storeInfo = ref({});
const stats = reactive({
  goodsCount: 0,
  pendingOrders: 0,
  monthlyOrders: 0,
  salesAmount: 0
});

// 编辑表单
const editForm = reactive({
  id: null,
  name: '',
  logo: '',
  description: '',
  type: 'FRUIT',
  phone: '',
  address: '',
  status: 1
});

// 验证规则
const editRules = {
  name: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2-50 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入店铺描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10-500 个字符之间', trigger: 'blur' }
  ]
};

// 店铺类型映射
const typeMap = {
  'FRUIT': '水果生鲜',
  'AGRICULTURAL': '农产品',
  'SPECIALTY': '特产食品',
  'ORGANIC': '有机食品',
  'OTHER': '其他'
};

// 获取店铺信息
const loadStoreInfo = async () => {
  loading.value = true;
  try {
    const user = JSON.parse(localStorage.getItem('system-user') || '{}');
    if (!user.id) {
      ElMessage.error('请先登录');
      router.push('/login');
      return;
    }

    console.log('当前用户ID:', user.id);

    // 获取店铺基本信息
    const storeRes = await request.get('/store/myStore', {
      params: { userId: user.id }
    });

    console.log('店铺基本信息响应:', storeRes);

    if (storeRes.code === '200' && storeRes.data) {
      storeInfo.value = storeRes.data;

      // 填充编辑表单
      Object.assign(editForm, {
        id: storeRes.data.id,
        name: storeRes.data.name || '',
        logo: storeRes.data.logo || '',
        description: storeRes.data.description || '',
        type: storeRes.data.type || 'FRUIT',
        phone: storeRes.data.phone || '',
        address: storeRes.data.address || '',
        status: storeRes.data.status || 1
      });

      // 获取店铺统计信息
      if (storeRes.data.id) {
        console.log('开始加载统计，店铺ID:', storeRes.data.id);
        await loadStoreStats(storeRes.data.id);
      } else {
        console.warn('店铺ID不存在，无法加载统计');
      }
    } else {
      ElMessage.warning('您尚未创建店铺');
      router.push('/merchant/store-create');
    }
  } catch (error) {
    console.error('加载店铺信息失败:', error);
    ElMessage.error('加载店铺信息失败');
  } finally {
    loading.value = false;
  }
};

// 加载店铺统计信息
const loadStoreStats = async (storeId) => {
  try {
    console.log('开始加载店铺统计，storeId:', storeId);

    // 方法1：先测试接口连通性
    try {
      const testRes = await request.get('/store/test');
      console.log('✅ StoreController测试:', testRes);
      if (testRes.code === '200') {
        console.log('StoreController工作正常');
      }
    } catch (testError) {
      console.warn('StoreController测试失败，但继续尝试统计接口');
    }

    // 方法2：使用查询参数方式（主要方式）
    const statsRes = await request.get('/store/stats', {
      params: {
        storeId: storeId
      }
    });

    if (statsRes.code === '200' && statsRes.data) {
      console.log('店铺统计信息获取成功:', statsRes.data);
      Object.assign(stats, statsRes.data);
      return;
    } else {
      console.warn('查询参数方式失败，尝试路径变量方式');

      // 方法3：尝试路径变量方式（备用）
      const pathStatsRes = await request.get(`/store/stats/${storeId}`);
      if (pathStatsRes.code === '200' && pathStatsRes.data) {
        console.log('路径变量方式成功:', pathStatsRes.data);
        Object.assign(stats, pathStatsRes.data);
        return;
      }
    }

    // 如果所有方法都失败，设置默认值
    console.warn('所有API调用失败，使用默认值');
    setDefaultStats();

  } catch (error) {
    console.error('加载店铺统计失败:', error);

    // 设置默认统计值
    setDefaultStats();
  }
};

// 设置默认统计值
const setDefaultStats = () => {
  console.log('设置默认统计值');
  Object.assign(stats, {
    goodsCount: 0,
    pendingOrders: 0,
    monthlyOrders: 0,
    salesAmount: 0.0
  });
};

// 获取类型名称
const getTypeName = (type) => {
  return typeMap[type] || type || '未设置';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知';
  try {
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return dateString;
  }
};

// Logo上传前校验
const beforeLogoUpload = (file) => {
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
  return true;
};

// Logo上传处理 - 本地预览
const handleLogoUpload = (file) => {
  console.log('选择Logo文件:', file.file);

  // 如果有旧的预览URL，先释放它
  if (logoPreviewUrl.value) {
    URL.revokeObjectURL(logoPreviewUrl.value);
  }

  // 创建新的本地预览URL
  const previewUrl = URL.createObjectURL(file.file);
  logoPreviewUrl.value = previewUrl;
  logoFile.value = file.file;

  ElMessage.success('Logo已选择，点击保存后生效');
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
  // 计算大致文件大小（base64会比原始文件大约33%）
  const stringLength = base64String.length - (base64String.includes('base64,') ? base64String.indexOf('base64,') + 7 : 0);
  const sizeInBytes = 4 * Math.ceil((stringLength / 3)) * 0.5624896334383812;
  const sizeInMB = sizeInBytes / (1024 * 1024);

  console.log('Logo Base64数据大小:', sizeInMB.toFixed(2), 'MB');

  // 如果超过1MB，建议压缩
  if (sizeInMB > 1) {
    ElMessage.warning('Logo文件较大，建议使用小于1MB的图片');
    return false;
  }
  return true;
};

// 安全请求包装器
const safeRequest = async (method, url, data) => {
  try {
    const response = await request[method](url, data);
    return response;
  } catch (error) {
    console.error('safeRequest捕获错误:', error);

    // 如果错误是null，转换为Error对象
    if (error === null || error === undefined) {
      console.error('错误是null/undefined，转换为Error对象');
      throw new Error('请求失败：未知错误');
    }

    // 如果错误是字符串，转换为Error对象
    if (typeof error === 'string') {
      console.error('错误是字符串:', error);
      throw new Error(error);
    }

    // 确保是Error对象
    if (!(error instanceof Error)) {
      console.error('错误不是Error对象:', error);
      throw new Error(String(error));
    }

    // 已经是Error对象，直接抛出
    throw error;
  }
};

// 保存编辑 - 使用直接axios，避免request.js问题
const saveEdit = async () => {
  console.log('=== 开始保存编辑 ===');

  try {
    // 表单验证
    await editFormRef.value.validate();
    console.log('表单验证通过');

    saving.value = true;

    // 准备更新数据
    const updateData = {
      id: editForm.id,
      name: editForm.name,
      description: editForm.description,
      type: editForm.type,
      phone: editForm.phone || '',
      address: editForm.address || '',
      status: editForm.status
    };

    console.log('发送店铺更新数据:', updateData);

    // 直接使用axios，避免request.js拦截器问题
    const baseURL = import.meta.env.VITE_BASE_URL || '';
    const user = JSON.parse(localStorage.getItem('system-user') || '{}');
    const token = user.token;

    console.log('基础URL:', baseURL);
    console.log('Token存在:', !!token);
    console.log('完整URL:', `${baseURL}/store/update`);

    // 使用axios直接发送请求
    const response = await axios({
      method: 'put',
      url: `${baseURL}/store/update`,
      data: updateData,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      timeout: 10000
    });

    console.log('axios直接响应:', response);
    console.log('响应数据:', response.data);

    const responseData = response.data;

    if (responseData && responseData.code === '200') {
      ElMessage.success(responseData.msg || '店铺信息更新成功');

      // 更新显示的数据
      Object.assign(storeInfo.value, updateData);

      // 清除临时文件
      if (logoFile.value) {
        URL.revokeObjectURL(logoPreviewUrl.value);
        logoFile.value = null;
        logoPreviewUrl.value = '';
      }

      // 退出编辑模式
      editMode.value = false;

      // 重新加载统计数据
      if (editForm.id) {
        await loadStoreStats(editForm.id);
      }
    } else {
      // 处理业务逻辑错误
      const errorMsg = responseData?.msg || '更新失败';
      console.error('业务逻辑错误:', errorMsg);
      ElMessage.error(errorMsg);
    }
  } catch (error) {
    console.error('保存失败:', error);
    console.error('错误类型:', typeof error);
    console.error('错误是否为Error:', error instanceof Error);
    console.error('错误消息:', error?.message);
    console.error('错误响应:', error?.response);
    console.error('错误请求:', error?.request);

    // 详细错误处理
    if (error.response) {
      // 服务器返回了错误响应
      console.error('响应状态码:', error.response.status);
      console.error('响应数据:', error.response.data);

      let errorMessage = '保存失败';

      if (error.response.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        } else if (error.response.data.msg) {
          errorMessage = error.response.data.msg;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        }
      }

      // 根据状态码添加提示
      switch (error.response.status) {
        case 400:
          errorMessage = '请求参数错误：' + errorMessage;
          break;
        case 401:
          errorMessage = '未授权访问，请重新登录';
          localStorage.removeItem('system-user');
          setTimeout(() => {
            router.push('/login');
          }, 100);
          break;
        case 403:
          errorMessage = '权限不足：' + errorMessage;
          break;
        case 404:
          errorMessage = '接口不存在：' + errorMessage;
          break;
        case 500:
          errorMessage = '服务器内部错误：' + errorMessage;
          break;
        default:
          errorMessage = `请求失败（${error.response.status}）：` + errorMessage;
      }

      ElMessage.error(errorMessage);
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('无响应，请求对象:', error.request);
      ElMessage.error('网络连接失败，请检查后端服务是否正常');
    } else {
      // 请求配置错误或其他错误
      const errorMessage = error.message || '保存失败，请重试';
      console.error('其他错误:', errorMessage);
      ElMessage.error(errorMessage);
    }
  } finally {
    saving.value = false;
  }
};

// 取消编辑
const cancelEdit = async () => {
  // 检查是否有未保存的更改
  const originalData = {
    id: storeInfo.value.id,
    name: storeInfo.value.name || '',
    logo: storeInfo.value.logo || '',
    description: storeInfo.value.description || '',
    type: storeInfo.value.type || 'FRUIT',
    phone: storeInfo.value.phone || '',
    address: storeInfo.value.address || '',
    status: storeInfo.value.status || 1
  };

  const currentData = {
    id: editForm.id,
    name: editForm.name,
    logo: editForm.logo,
    description: editForm.description,
    type: editForm.type,
    phone: editForm.phone,
    address: editForm.address,
    status: editForm.status
  };

  const hasFormChanges = JSON.stringify(originalData) !== JSON.stringify(currentData);
  const hasFileChanges = logoFile.value !== null;

  if (hasFormChanges || hasFileChanges) {
    try {
      await ElMessageBox.confirm(
          '有未保存的修改，确定要取消吗？',
          '确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
      );

      // 恢复原始数据
      Object.assign(editForm, originalData);

      // 清除预览
      if (logoFile.value) {
        URL.revokeObjectURL(logoPreviewUrl.value);
        logoFile.value = null;
        logoPreviewUrl.value = '';
      }

      editMode.value = false;
    } catch {
      // 用户点击取消，不做操作
      console.log('用户取消了取消操作');
    }
  } else {
    editMode.value = false;
  }
};

// 页面卸载时清理预览URL
onUnmounted(() => {
  if (logoPreviewUrl.value) {
    URL.revokeObjectURL(logoPreviewUrl.value);
  }
});

// 页面加载
onMounted(() => {
  console.log('MerchantStoreManage组件已挂载');

  loadStoreInfo();
});
</script>

<style scoped>
.store-manage {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.manage-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.info-card {
  grid-column: 1;
  grid-row: 1 / span 2;
}

.stats-card {
  grid-column: 2;
  grid-row: 1;
}

.action-card {
  grid-column: 2;
  grid-row: 2;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.loading-container {
  padding: 20px 0;
}

.view-mode {
  padding: 10px 0;
}

.info-row {
  display: flex;
  margin-bottom: 16px;
  line-height: 1.6;
}

.info-label {
  min-width: 100px;
  color: #606266;
  font-weight: 500;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #303133;
  word-break: break-word;
}

.logo-preview {
  margin-top: 4px;
}

.store-logo {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.logo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  border-radius: 6px;
}

.edit-form {
  padding: 10px 0;
}

.logo-upload-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 8px;
}

.avatar-uploader {
  margin-top: 10px;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.edit-logo {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: #f8f9fa;
  gap: 12px;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: #eef5ff;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  justify-content: flex-start;
  padding: 12px 16px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .manage-container {
    grid-template-columns: 1fr;
  }

  .info-card,
  .stats-card,
  .action-card {
    grid-column: 1;
  }

  .info-card {
    grid-row: 1;
  }

  .stats-card {
    grid-row: 2;
  }

  .action-card {
    grid-row: 3;
  }

  .logo-upload-section {
    align-items: center;
  }
}

@media (max-width: 576px) {
  .store-manage {
    padding: 16px;
  }

  .info-row {
    flex-direction: column;
    gap: 4px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    gap: 8px;
  }

  .action-btn {
    padding: 10px 14px;
  }
}
</style>