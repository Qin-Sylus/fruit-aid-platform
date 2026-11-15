<template>
  <div class="login-container">
    <div style="color: #39bf23; font-size: 50px; font-weight: bold; position: absolute; text-align: center; top: 50px">南充市助农水果销售系统</div>
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #1450aa">欢 迎 注 册</div>
      <el-form :model="data.form" ref="formRef" :rules="data.rules">
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入账号" />
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="请输入密码（6-20位）" show-password />
        </el-form-item>
        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.confirmPassword" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <!-- 角色选择 -->
        <el-form-item prop="role">
          <el-select size="large" style="width: 100%" v-model="data.form.role" placeholder="请选择角色">
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="商家" value="MERCHANT"></el-option>
          </el-select>
        </el-form-item>
        <!-- 注册按钮 -->
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="register" :loading="isLoading">注 册</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: right;">
        已有账号？请 <a href="/login" class="link-text">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

// 加载状态
const isLoading = ref(false);
// 表单引用
const formRef = ref();

// 表单数据（与Login.vue结构保持一致）
const data = reactive({
  form: {
    username: '',
    password: '',
    confirmPassword: '',
    role: 'USER'
  },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 2, max: 20, message: '账号长度为2-20位', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
    ],
    confirmPassword: [
      { validator: (rule, val, cb) => {
          if (!val) cb(new Error('请确认密码'));
          else if (val !== data.form.password) cb(new Error('两次输入密码不一致'));
          else cb();
        }, trigger: 'blur' }
    ],
    role: [{ required: true, message: '请选择角色', trigger: 'change' }]
  }
});

// 注册提交（与Login.vue风格保持一致）
const register = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      isLoading.value = true;
      const { confirmPassword, ...params } = data.form;

      request.post('/user/registerWithRole', params)
          .then(res => {
            if (res.code === '200') {
              ElMessage.success("注册成功，即将跳转登录页");
              setTimeout(() => router.push('/login'), 1500);
            } else {
              ElMessage.error(res.msg);
            }
          })
          .catch(error => {
            console.error(error);
            ElMessage.error('网络错误，请稍后重试');
          })
          .finally(() => {
            isLoading.value = false;
          });
    }
  }).catch(error => {
    console.error(error);
  });
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow:hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("@/assets/imgs/bg.png");
  background-size: cover;
  position: relative;
}

.login-box {
  width: 350px;
  padding: 50px 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0,.1);
  background-color: rgba(255, 255, 255, .8);
}

.link-text {
  color: #39bf23;
  text-decoration: none;
  transition: color 0.3s ease;
}

.link-text:hover {
  color: #2fa31a;
  text-decoration: underline;
}

/* 保持与Login.vue一致的表单元素样式穿透 */
::v-deep(.el-input__wrapper), ::v-deep(.el-select__wrapper) {
  border-radius: 4px !important;
}

::v-deep(.el-form-item__error) {
  font-size: 12px;
  margin-top: 4px;
}
</style>