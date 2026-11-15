<template>
  <div>
    <div style="height: 60px; background-color: #E6F3FF; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #39bf23">南充市助农水果销售系统</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
        <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
        <span style="margin-left: 5px">{{ data.user.name }}</span>
      </div>
    </div>

    <div style="display: flex">
      <div style="width: 200px; border-right: 1px solid #ddd; min-height: calc(100vh - 60px)">
        <el-menu
            router
            style="border: none; background-color: #409EFF"
            :default-active="router.currentRoute.value.path"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <!-- 新增：慈善活动菜单 -->
          <el-menu-item index="/charity/activities">
            <el-icon><Promotion /></el-icon>
            <span>慈善活动</span>
          </el-menu-item>

          <!-- 在管理端布局的菜单部分添加 -->
          <el-menu-item index="/charity-manager">
            <el-icon><Flag /></el-icon>
            <span>慈善活动管理</span>
          </el-menu-item>

          <el-menu-item index="/buy" v-if="data.user.role === 'USER'">
            <el-icon><Goods /></el-icon>
            <span>水果购买</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><Tickets /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/cart" v-if="data.user.role === 'USER'">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
          </el-menu-item>
          <el-menu-item index="/category" v-if="data.user.role === 'ADMIN'">
            <el-icon><Menu /></el-icon>
            <span>产品分类管理</span>
          </el-menu-item>
          <el-menu-item index="/goods" v-if="data.user.role === 'ADMIN'">
            <el-icon><Goods /></el-icon>
            <span>水果订单管理</span>
          </el-menu-item>
          <el-menu-item index="/goodsStock" v-if="data.user.role === 'ADMIN'">
            <el-icon><SoldOut /></el-icon>
            <span>水果管理</span>
          </el-menu-item>
          <el-sub-menu index="2" v-if="data.user.role === 'ADMIN'">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin">
              <el-icon><User /></el-icon>
              <span>管理员信息</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>普通用户信息</span>
            </el-menu-item>
            <el-menu-item index="/merchant">
              <el-icon><Shop /></el-icon>
              <span>商家信息</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/notice" v-if="data.user.role === 'ADMIN'">
            <el-icon><Bell /></el-icon>
            <span>品牌信息管理</span>
          </el-menu-item>
          <!-- 在管理端菜单中添加 -->
          <el-menu-item index="/chat-admin" v-if="data.user.role === 'ADMIN'">
            <el-icon><ChatDotRound /></el-icon>
            <span>在线客服管理</span>
          </el-menu-item>
          <el-menu-item index="/feedback" v-if="data.user.role === 'ADMIN'">
            <el-icon><ChatDotRound /></el-icon>
            <span>意见反馈</span>
          </el-menu-item>
          <el-menu-item index="/help" v-if="data.user.role === 'USER'">
            <el-icon><QuestionFilled /></el-icon>
            <span>帮助中心</span>
          </el-menu-item>
          <el-menu-item index="/person">
            <el-icon><UserFilled /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div style="flex: 1; width: 0; background-color: #f8f8ff; padding: 10px">
        <router-view @updateUser="updateUser" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";

// 新增：导入 Promotion 图标
import {
  HomeFilled,
  Goods,
  Tickets,
  ShoppingCart,
  Menu,
  SoldOut,
  Memo,
  User,
  Shop,
  Bell,
  ChatDotRound,
  QuestionFilled,
  UserFilled,
  Promotion  // 新增图标
} from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

const logout = () => {
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
  router.push('/login')
}
</script>

<style scoped>
:deep(.el-menu) {
  background-color: #409EFF !important;
}
:deep(.el-menu-item) {
  color: white !important;
}
:deep(.el-menu-item:hover) {
  background-color: #66b1ff !important;
  color: white !important;
}
:deep(.el-menu-item.is-active) {
  background-color: #66b1ff !important;
  color: white !important;
}
:deep(.el-sub-menu__title) {
  color: white !important;
}
:deep(.el-sub-menu__title:hover) {
  background-color: #66b1ff !important;
  color: white !important;
}
:deep(.el-sub-menu) {
  background-color: #409EFF !important;
}
:deep(.el-menu--inline) {
  background-color: #409EFF !important;
}
:deep(.el-menu--inline .el-menu-item) {
  color: white !important;
  border-left: 3px solid transparent;
}
:deep(.el-menu--inline .el-menu-item:hover) {
  background-color: #66b1ff !important;
  color: white !important;
  border-left-color: #ffd700;
}
:deep(.el-menu--inline .el-menu-item.is-active) {
  background-color: #66b1ff !important;
  color: white !important;
  border-left-color: #ffd700;
}
:deep(.el-sub-menu__title.is-active) {
  background-color: #66b1ff !important;
  color: white !important;
}
th {
  color: #333;
}
</style>