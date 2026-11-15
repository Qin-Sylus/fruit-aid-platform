<template>
  <div class="sidebar-container">
    <el-menu
        router
        :default-active="activePath"
        class="merchant-sidebar"
        background-color="#2c3e50"
        text-color="#fff"
        active-text-color="#ffd700"
        :collapse-transition="false"
    >

      <!-- 商家首页：index = 路由完整路径 /merchant/home -->
      <el-menu-item index="/merchant/home">
        <el-icon><HomeFilled /></el-icon>
        <span>商家首页</span>
      </el-menu-item>

      <!-- 店铺管理（子菜单） -->
      <el-sub-menu index="store-group"> <!-- 子菜单父index可自定义，不影响路由 -->
        <template #title>
          <el-icon><Shop /></el-icon>
          <span>店铺管理</span>
        </template>
        <!-- 创建店铺：index = /merchant/store-create -->
        <el-menu-item index="/merchant/store-create">
          <el-icon><CirclePlus /></el-icon>
          <span>创建店铺</span>
        </el-menu-item>
        <!-- 店铺设置：index = /merchant/store-manage -->
        <el-menu-item index="/merchant/store-manage">
          <el-icon><Tools /></el-icon>
          <span>店铺设置</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 商品管理：index = /merchant/store-goods -->
      <el-menu-item index="/merchant/store-goods">
        <el-icon><Goods /></el-icon>
        <span>商品管理</span>
      </el-menu-item>

      <!-- 订单管理：index = /merchant/store-orders -->
      <el-menu-item index="/merchant/store-orders">
        <el-icon><Tickets /></el-icon>
        <span>订单管理</span>
        <!-- 待处理订单徽章（可选） -->
        <el-badge v-if="pendingOrderCount > 0" :value="pendingOrderCount" class="order-badge" />
      </el-menu-item>

      <!-- 销售统计：index = /merchant/statistics -->
      <el-menu-item index="/merchant/statistics">
        <el-icon><Histogram /></el-icon>
        <span>销售统计</span>
      </el-menu-item>

      <el-menu-item index="/charity/activities">
        <el-icon><Promotion /></el-icon>
        <span>慈善活动</span>
      </el-menu-item>

      <el-menu-item index="/merchant/store-apply">
        <el-icon><CircleCheck /></el-icon>
        <span>活动报名</span>
      </el-menu-item>

      <!-- 个人中心：index = /merchant/profile -->
      <el-menu-item index="/merchant/profile">
        <el-icon><User /></el-icon>
        <span>个人中心</span>
      </el-menu-item>

      <!-- 退出登录：index = /login（跳登录页） -->
      <el-menu-item index="/login" @click="handleLogout">
        <el-icon><Close /></el-icon>
        <span>退出登录</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

// 手动导入需要的图标组件
import {
  HomeFilled,
  Shop,
  CirclePlus,
  Tools,
  Goods,
  Tickets,
  Histogram,
  User,
  Close
} from '@element-plus/icons-vue';

const props = defineProps({
  activePath: {
    type: String,
    required: true,
    default: '/merchant/home'
  }
});

// 待处理订单数量
const pendingOrderCount = ref(0);

// 初始化：完全避免接口调用
onMounted(() => {
  // 直接设置订单数量为0，避免接口调用
  pendingOrderCount.value = 0;

  // 或者为了演示效果，设置一个随机数（可选）
  // pendingOrderCount.value = Math.floor(Math.random() * 5);
});

// 退出登录逻辑
const handleLogout = () => {
  localStorage.removeItem('system-user');
  ElMessage.success('已退出登录');
  // 可选：跳转到登录页
  // window.location.href = '/login';
};
</script>

<style scoped>
.sidebar-container {
  width: 220px;
  height: calc(100vh - 60px); /* 减去顶部导航高度 */
  background-color: #308ce7;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

/* Logo样式 */
.sidebar-logo {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #308ce7;
  margin-bottom: 10px;
}

.logo-img {
  width: 32px;
  height: 32px;
  border-radius: 4px;
}

.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  margin-left: 12px;
}

/* 菜单样式优化 */
.merchant-sidebar {
  border-right: none;
  height: 100%;
}

/* 菜单hover和激活样式 */
:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  background-color: #308ce7 !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #308ce7 !important;
}

/* 订单徽章位置 */
.order-badge {
  position: absolute;
  top: 5px;
  right: 10px;
}

/* 适配小屏幕（可选） */
@media (max-width: 1200px) {
  .sidebar-container {
    width: 64px;
  }

  .logo-text {
    display: none;
  }

  :deep(.el-menu-item__content) {
    display: none;
  }

  :deep(.el-sub-menu__title span) {
    display: none;
  }
}
</style>