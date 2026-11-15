<template>
  <div class="header-nav">
    <div class="nav-left">
      <img src="@/assets/imgs/logo.png" alt="logo" class="logo" />
      <span class="title" v-if="isMerchant">商家管理中心</span>
      <span class="title" v-else>系统管理中心</span>
    </div>
    <div class="nav-right">
      <span class="username">{{ userName }}</span>
      <el-dropdown trigger="click" @command="handleDropdown">
        <span class="user-dropdown">
          <!-- 显示用户头像 -->
          <el-avatar
              :size="36"
              :src="userAvatar"
              class="user-avatar"
          >
            <!-- 如果没有头像，显示用户名的首字母 -->
            <span v-if="!userAvatar" class="avatar-fallback">
              {{ userName.charAt(0).toUpperCase() }}
            </span>
          </el-avatar>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :command="isMerchant ? '/merchant/profile' : '/admin/profile'">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><Close /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Close } from '@element-plus/icons-vue';

const router = useRouter();

// 默认头像
const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 用户信息
const userInfo = ref({});
const userAvatar = ref('');
const isMerchant = ref(false);
const userName = ref('');

// 监听本地存储变化，实时更新用户信息
const updateUserInfo = () => {
  try {
    const userStr = localStorage.getItem('system-user');
    if (userStr) {
      userInfo.value = JSON.parse(userStr);
      userAvatar.value = userInfo.value.avatar || DEFAULT_AVATAR;
      isMerchant.value = userInfo.value.role === 'MERCHANT';
      userName.value = userInfo.value.name ||
          userInfo.value.username ||
          (isMerchant.value ? '商家用户' : '管理员');
    } else {
      userInfo.value = {};
      userAvatar.value = DEFAULT_AVATAR;
      isMerchant.value = false;
      userName.value = '未登录';
    }
  } catch (error) {
    console.error('解析用户信息失败:', error);
    userAvatar.value = DEFAULT_AVATAR;
    userName.value = '用户';
  }
};

// 监听storage变化（用于跨页面同步）
const setupStorageListener = () => {
  window.addEventListener('storage', (e) => {
    if (e.key === 'system-user') {
      updateUserInfo();
    }
  });

  // 自定义事件监听（同页面内更新）
  window.addEventListener('user-info-updated', updateUserInfo);
};

// 页面加载时初始化
onMounted(() => {
  updateUserInfo();
  setupStorageListener();

  // 定时检查用户信息（可选，用于实时同步）
  // setInterval(updateUserInfo, 5000);
});

// 下拉菜单操作
const handleDropdown = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('system-user');
    ElMessage.success('已退出登录');
    router.push('/login');
  } else if (command) {
    router.push(command);
  }
};
</script>

<style scoped>
.header-nav {
  height: 60px;
  background: linear-gradient(135deg, #308ce7 0%, #1a73e8 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  object-fit: contain;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 4px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.95;
}

.user-dropdown {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.user-dropdown:hover {
  background-color: rgba(255, 255, 255, 0.15);
  transform: scale(1.05);
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

.avatar-fallback {
  font-size: 16px;
  font-weight: bold;
  color: #308ce7;
}

/* 下拉菜单样式优化 */
:deep(.el-dropdown-menu) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  border: 1px solid #e0e0e0;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: 14px;
  transition: all 0.2s;
  color: #333;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #f5f7fa;
  color: #308ce7;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
  color: #666;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: #308ce7;
}

:deep(.el-dropdown-menu__item--divided) {
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
  padding-top: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-nav {
    padding: 0 15px;
  }

  .title {
    font-size: 16px;
  }

  .username {
    display: none;
  }

  .nav-right {
    gap: 10px;
  }

  .user-avatar {
    width: 32px;
    height: 32px;
  }

  .avatar-fallback {
    font-size: 14px;
  }
}
</style>