<template>
  <div class="merchant-layout">
    <!-- 顶部导航（复用公共组件） -->
    <HeaderNav />

    <!-- 主内容区：侧边栏 + 子路由出口（关键：子页面在这里渲染） -->
    <div class="main-content">
      <MerchantSidebar :active-path="$route.path" />
      <!-- 使用具名插槽方式渲染子路由，避免属性传递问题 -->
      <router-view v-slot="{ Component }">
        <component :is="Component" class="content-wrapper" />
      </router-view>
    </div>
  </div>
</template>

<script setup>
// 导入必要组件（确保路径正确）
import HeaderNav from '@/components/HeaderNav.vue';
import MerchantSidebar from './MerchantSidebar.vue';
</script>

<style scoped>
/* 全屏布局 */
.merchant-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 主内容区：侧边栏 + 内容 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  background-color: #ffffff;
}

/* 子页面容器样式 - 移到全局或由子组件自己控制 */
.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>