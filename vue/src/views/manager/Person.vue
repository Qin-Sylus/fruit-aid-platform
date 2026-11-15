<template>
  <div style="width: 80%">
    <div class="card" style="padding: 30px">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="个人资料" name="profile">
          <el-form :model="data.user" label-width="100px" style="padding-right: 50px">
            <div style="margin: 20px 0; text-align: center">
              <el-upload :show-file-list="false" class="avatar-uploader" action="http://localhost:9090/files/upload" :on-success="handleFileUpload">
                <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </div>
            <el-form-item label="账号">
              <el-input disabled v-model="data.user.username" autocomplete="off" />
            </el-form-item>
            <el-form-item label="名称">
              <el-input v-model="data.user.name" autocomplete="off" />
            </el-form-item>
            <div v-if="data.user.role === 'USER'">
              <el-form-item label="性别" prop="sex">
                <el-radio-group v-model="data.user.sex">
                  <el-radio label="男"></el-radio>
                  <el-radio label="女"></el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="电话" prop="phone">
                <el-input v-model="data.user.phone" autocomplete="off" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="data.user.email" autocomplete="off" />
              </el-form-item>
            </div>
            <div style="text-align: center">
              <el-button type="primary" @click="save">保存</el-button>
            </div>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="data.passwordForm" label-width="100px" style="padding-right: 50px">
            <el-form-item label="原密码">
              <el-input v-model="data.passwordForm.oldPassword" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="data.passwordForm.newPassword" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="data.passwordForm.confirmPassword" show-password />
            </el-form-item>
            <div style="text-align: center">
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </div>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="退出系统" name="logout">
          <div style="text-align: center; padding: 50px">
            <el-button type="danger" size="large" @click="logout">退出系统</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue"
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import router from "@/router";

const activeTab = ref('profile')

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  passwordForm: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
})

const handleFileUpload = (file) => {
  data.user.avatar = file.data
}

const emit = defineEmits(["updateUser"])

// 把当前修改的用户信息存储到后台数据库
const save = () => {
  if (data.user.role === 'ADMIN') {
    request.put('/admin/update', data.user).then(res => {
      if (res.code === '200') {
        ElMessage.success('更新成功')
        //把更新后的用户信息存储到缓存
        localStorage.setItem('system-user', JSON.stringify(data.user))
        emit('updateUser')
      } else {
        ElMessage.error(res.msg)
      }
    })
  } else {
    request.put('/user/update', data.user).then(res => {
      if (res.code === '200') {
        ElMessage.success('更新成功')
        //把更新后的用户信息存储到缓存
        localStorage.setItem('system-user', JSON.stringify(data.user))
        emit('updateUser')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }
}

// 修改密码
const changePassword = () => {
  if (data.passwordForm.newPassword !== data.passwordForm.confirmPassword) {
    ElMessage.error('确认新密码错误')
    return
  }
  
  const passwordData = {
    ...data.user,
    password: data.passwordForm.oldPassword,
    newPassword: data.passwordForm.newPassword,
    confirmPasword: data.passwordForm.confirmPassword
  }
  
  request.put('/updatePassword', passwordData).then(res => {
    if (res.code === '200') {
      ElMessage.success('修改密码成功')
      // 清空缓存
      localStorage.removeItem('system-user')
      router.push('/login')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 退出系统
const logout = () => {
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
  router.push('/login')
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>