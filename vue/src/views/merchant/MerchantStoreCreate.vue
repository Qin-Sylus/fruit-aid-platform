<template>
  <div class="store-create">
    <div class="page-header">
      <h2>创建店铺</h2>
      <p>请填写店铺信息，完成店铺创建</p>
    </div>

    <el-card class="create-card">
      <el-form
          :model="form"
          ref="storeForm"
          :rules="rules"
          label-width="120px"
          class="create-form"
      >
        <el-form-item label="店铺名称" prop="name">
          <el-input
              v-model="form.name"
              placeholder="请输入店铺名称"
              clearable
              maxlength="50"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="店铺Logo" prop="logo">
          <el-upload
              class="logo-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleLogoUpload"
              :before-upload="beforeLogoUpload"
              :headers="uploadHeaders"
          >
            <el-image
                v-if="form.logo"
                :src="form.logo"
                class="logo-preview"
                fit="cover"
            >
              <template #error>
                <div class="logo-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <div>上传Logo</div>
            </div>
          </el-upload>
          <div class="upload-tip">建议上传 300x300 像素的方形图片，大小不超过 2MB</div>
        </el-form-item>

        <el-form-item label="店铺描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请描述您的店铺特色、主营商品等信息"
              maxlength="500"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="店铺类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择店铺类型" style="width: 100%" clearable>
            <el-option label="水果生鲜" value="FRUIT"></el-option>
            <el-option label="农产品" value="AGRICULTURAL"></el-option>
            <el-option label="特产食品" value="SPECIALTY"></el-option>
            <el-option label="有机食品" value="ORGANIC"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input
              v-model="form.phone"
              placeholder="请输入店铺联系电话"
              maxlength="11"
          ></el-input>
        </el-form-item>

        <el-form-item label="店铺地址" prop="address">
          <el-input
              v-model="form.address"
              type="textarea"
              :rows="2"
              placeholder="请输入店铺详细地址"
              maxlength="200"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ submitting ? '创建中...' : '提交创建' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Picture, Plus } from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();

// 状态管理
const submitting = ref(false);
const storeForm = ref(null);

// 表单数据
const form = reactive({
  name: '',
  logo: '',
  description: '',
  type: 'FRUIT',
  phone: '',
  address: '',
  userId: null,
  status: 1
});

// 验证规则
const rules = {
  name: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2-50 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入店铺描述', trigger: 'blur' },
    { min: 10, max: 500, message: '店铺描述长度在 10-500 个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择店铺类型', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入店铺地址', trigger: 'blur' },
    { min: 5, max: 200, message: '地址长度在 5-200 个字符之间', trigger: 'blur' }
  ]
};

// 上传配置
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload';
const uploadHeaders = {
  'Authorization': `Bearer ${localStorage.getItem('token')}`
};

// 页面加载时设置userId
onMounted(() => {
  const user = JSON.parse(localStorage.getItem('system-user') || '{}');
  form.userId = user.id;
});

// 图片上传前的验证
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

// Logo上传成功处理
const handleLogoUpload = (res) => {
  if (res.code === '200' && res.data) {
    form.logo = res.data;
    ElMessage.success('Logo上传成功');
  } else {
    ElMessage.error(res.msg || '图片上传失败');
  }
};

// 提交表单
const submitForm = () => {
  storeForm.value.validate(async (valid) => {
    if (!valid) return;

    submitting.value = true;

    try {
      const user = JSON.parse(localStorage.getItem('system-user') || '{}');
      if (!user.id) {
        ElMessage.error('请先登录');
        router.push('/login');
        return;
      }

      // 准备提交数据
      const storeData = {
        name: form.name,
        logo: form.logo,
        description: form.description,
        type: form.type,
        phone: form.phone,
        address: form.address,
        userId: user.id,
        status: 1
      };

      // 调用接口
      const response = await request.post('/store/add', storeData);

      if (response.code === '200') {
        ElMessage.success(response.msg || '店铺创建成功！');

        await ElMessageBox.confirm(
            '店铺创建成功！是否立即前往店铺管理页面？',
            '创建成功',
            {
              confirmButtonText: '前往管理',
              cancelButtonText: '稍后再说',
              type: 'success'
            }
        ).then(() => {
          router.push('/merchant/store-manage');
        }).catch(() => {
          router.push('/merchant/home');
        });
      } else {
        ElMessage.error(response.msg || '店铺创建失败');
      }
    } catch (error) {
      console.error('创建店铺失败:', error);
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.msg || '创建失败');
      } else {
        ElMessage.error('网络错误，请重试');
      }
    } finally {
      submitting.value = false;
    }
  });
};

// 重置表单
const resetForm = () => {
  storeForm.value.resetFields();
  form.logo = '';
  form.type = 'FRUIT';
  const user = JSON.parse(localStorage.getItem('system-user') || '{}');
  form.userId = user.id;
  ElMessage.success('表单已重置');
};

// 返回上一页
const goBack = () => {
  router.push('/merchant/home');
};
</script>

<style scoped>
.store-create {
  width: 100%;
  max-width: 900px;
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

.create-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.create-form {
  padding: 20px;
}

.logo-uploader {
  width: 150px;
  height: 150px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  font-size: 14px;
}

.upload-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.logo-preview {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.logo-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .store-create {
    padding: 16px;
  }

  .create-card {
    border: none;
    box-shadow: none;
  }

  .create-form {
    padding: 16px 0;
  }

  .logo-uploader {
    width: 120px;
    height: 120px;
  }

  :deep(.el-form-item__label) {
    padding-right: 10px;
  }
}
</style>