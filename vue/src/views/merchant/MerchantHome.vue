<template>
  <div class="merchant-container">
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 内容区域 -->
      <div class="content-wrapper">
        <div class="page-header">
          <h2>商家中心</h2>
          <p>欢迎回来，{{ merchantInfo.name }}！最近30天销售额：¥{{ salesData.total }}</p>
        </div>
        <!-- 数据概览卡片 -->
        <div class="stats-cards">
          <el-card class="stat-card">
            <div class="stat-item">
              <span class="stat-label">店铺状态</span>
              <span class="stat-value" :class="storeInfo.status === 1 ? 'online' : 'offline'">
                {{ storeInfo.status === 1 ? '营业中' : '已关闭' }}
              </span>
            </div>
          </el-card>

          <el-card class="stat-card">
            <div class="stat-item">
              <span class="stat-label">已上架商品</span>
              <span class="stat-value">{{ goodsCount }}</span>
            </div>
          </el-card>

          <el-card class="stat-card">
            <div class="stat-item">
              <span class="stat-label">待处理订单</span>
              <span class="stat-value">{{ pendingOrders }}</span>
            </div>
          </el-card>

          <el-card class="stat-card">
            <div class="stat-item">
              <span class="stat-label">本月订单量</span>
              <span class="stat-value">{{ monthlyOrders }}</span>
            </div>
          </el-card>
        </div>

        <!-- 店铺信息与快捷操作 -->
        <div class="store-actions">
          <el-card>
            <div class="store-info">
              <el-image
                  :src="storeInfo.logo || defaultStoreLogo"
                  class="store-logo"
              ></el-image>
              <div class="store-detail">
                <h3>{{ storeInfo.name || '未设置店铺名称' }}</h3>
                <p>{{ storeInfo.description || '未添加店铺描述' }}</p>
              </div>
              <div class="action-buttons">
                <el-button
                    type="primary"
                    @click="$router.push('/merchant/store-manage')"
                >
                  管理店铺
                </el-button>
                <el-button
                    @click="$router.push('/merchant/store-goods')"
                >
                  管理商品
                </el-button>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 最近订单列表 -->
        <div class="recent-orders">
          <el-card>
            <div slot="header">最近订单</div>
            <el-table :data="recentOrders" stripe>
              <el-table-column prop="orderNo" label="订单编号"></el-table-column>
              <el-table-column prop="createTime" label="创建时间"></el-table-column>
              <el-table-column prop="totalAmount" label="金额"></el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getStatusTagType(scope.row.status)">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button
                      size="small"
                      @click="$router.push(`/merchant/orders/detail/${scope.row.id}`)"
                  >
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import defaultStoreLogo from '@/assets/imgs/1.png';

// 路由信息
const route = useRoute();

// 状态管理
const user = JSON.parse(localStorage.getItem('system-user') || '{}');
const merchantInfo = ref({ name: user.name || '商家用户' });
const storeInfo = ref({});
const goodsCount = ref(0);
const pendingOrders = ref(0);
const monthlyOrders = ref(0);
const recentOrders = ref([]);
const salesData = ref({ total: '0.00' });

// 状态标签样式映射
const getStatusTagType = (status) => {
  const statusMap = {
    '待支付': 'warning',
    '待发货': 'primary',
    '已发货': 'success',
    '已完成': 'info',
    '已取消': 'danger'
  };
  return statusMap[status] || 'default';
};

// 初始化数据
onMounted(async () => {
  if (!user.id) {
    ElMessage.error('请先登录');
    return;
  }

  try {
    // 先获取店铺信息
    await getStoreInfo();

    // 然后获取其他数据
    if (storeInfo.value.id) {
      await Promise.all([
        getGoodsCount(),
        getOrderStats(),
        getRecentOrders(),
        getSalesData()
      ]);
    }
  } catch (err) {
    console.error('数据加载失败', err);
  }
});

// 获取店铺信息
const getStoreInfo = () => {
  return request.get('/store/myStore', { params: { userId: user.id } })
      .then(res => {
        if (res.data) storeInfo.value = res.data;
      })
      .catch(() => ElMessage.error('获取店铺信息失败'));
};

// 获取商品数量
const getGoodsCount = () => {
  return request.get('/goods/countByStore', { params: { storeId: storeInfo.value.id } })
      .then(res => { goodsCount.value = res.data; })
      .catch(() => ElMessage.error('获取商品数量失败'));
};

// 获取订单统计
const getOrderStats = () => {
  return request.get('/orders/merchant/stats', { params: { storeId: storeInfo.value.id } })
      .then(res => {
        pendingOrders.value = res.data.pending || 0;
        monthlyOrders.value = res.data.monthly || 0;
      })
      .catch(() => {
        pendingOrders.value = 0;
        monthlyOrders.value = 0;
      });
};

// 获取最近订单
const getRecentOrders = () => {
  return request.get('/orders/merchant/recent', {
    params: { storeId: storeInfo.value.id, limit: 5 }
  }).then(res => {
    recentOrders.value = res.data || [];
  }).catch(() => {
    recentOrders.value = [];
  });
};

// 获取销售数据
const getSalesData = () => {
  return request.get('/orders/salesData', {
    params: { storeId: storeInfo.value.id, days: 30 }
  }).then(res => {
    salesData.value = res.data || { total: '0.00' };
  }).catch(() => {
    salesData.value = { total: '0.00' };
  });
};
</script>
<style scoped>
.merchant-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  display: flex;
  min-height: calc(100vh - 60px);
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.page-header {
  margin-bottom: 20px;
}

.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
}

.stat-item {
  padding: 15px;
  text-align: center;
}

.stat-label {
  display: block;
  color: #308ce7;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-value.online {
  color: #10b981;
}

.stat-value.offline {
  color: #ef4444;
}

.store-actions {
  margin-bottom: 20px;
}

.store-info {
  display: flex;
  align-items: center;
  padding: 15px;
  flex-wrap: wrap;
  gap: 20px;
}

.store-logo {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}

.store-detail {
  flex: 1;
  min-width: 200px;
}

.store-detail h3 {
  margin-bottom: 8px;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.recent-orders {
  margin-top: 20px;
}
</style>