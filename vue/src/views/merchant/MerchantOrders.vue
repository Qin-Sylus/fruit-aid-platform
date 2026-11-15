<template>
  <div class="merchant-container">
    <div class="page-header">
      <h2>订单管理</h2>
      <p>查看本店铺的订单信息，并按状态筛选与处理</p>
    </div>

    <el-card :body-style="{ padding: '20px' }" class="search-bar">
      <div class="search-form">
        <el-input v-model="keyword" placeholder="订单号 / 收货人" class="search-input" clearable />
        <el-select v-model="status" placeholder="订单状态" class="search-select" clearable>
          <el-option label="全部状态" value="" />
          <el-option label="待付款" value="0" />
          <el-option label="待发货" value="1" />
          <el-option label="已发货" value="2" />
          <el-option label="已完成" value="3" />
          <el-option label="已取消" value="4" />
        </el-select>
        <el-button type="primary" @click="loadOrders">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <el-card :body-style="{ padding: '0' }">
      <el-table v-loading="loading" :data="orderList" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="订单号" width="160" show-overflow-tooltip />
        <el-table-column prop="username" label="收货人" width="120" />
        <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="total" label="金额" width="120" align="center">
          <template #default="scope">
            {{ scope.row.total != null ? '¥' + Number(scope.row.total).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
      </el-table>

      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
      <div v-if="!loading && orderList.length === 0" class="empty-state">
        <el-empty description="暂无订单数据（需启动后端服务后加载）" :image-size="160" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

const user = JSON.parse(localStorage.getItem('system-user') || '{}');
const orderList = ref([]);
const total = ref(0);
const loading = ref(false);
const keyword = ref('');
const status = ref('');
const pageNum = ref(1);
const pageSize = ref(10);
const storeId = ref(0);

const statusMap = { '0': '待付款', '1': '待发货', '2': '已发货', '3': '已完成', '4': '已取消' };
const statusText = (s) => statusMap[String(s)] || '未知';
const statusType = (s) => (String(s) === '3' ? 'success' : String(s) === '4' ? 'info' : 'warning');

const loadOrders = () => {
  loading.value = true;
  request.get('/orders/selectPage', {
    params: { pageNum: pageNum.value, pageSize: pageSize.value, storeId: storeId.value, keyword: keyword.value, status: status.value }
  }).then((res) => {
    orderList.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  }).catch(() => {
    orderList.value = [];
    total.value = 0;
  }).finally(() => { loading.value = false; });
};

const resetSearch = () => {
  keyword.value = '';
  status.value = '';
  pageNum.value = 1;
  loadOrders();
};

onMounted(() => {
  if (!user.id) {
    ElMessage.warning('请先登录后查看订单');
    return;
  }
  request.get('/store/myStore', { params: { userId: user.id } })
    .then((res) => { storeId.value = res.data?.id || 0; loadOrders(); })
    .catch(() => loadOrders());
});
</script>

<style scoped>
.merchant-container { width: 100%; min-width: 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; font-size: 24px; font-weight: 600; }
.page-header p { margin: 0; color: #606266; font-size: 14px; }
.search-bar { margin-bottom: 16px; }
.search-form { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.search-input { width: 220px; }
.search-select { width: 160px; }
.pagination-container { padding: 16px; border-top: 1px solid #f0f0f0; }
.empty-state { padding: 60px 0; display: flex; align-items: center; justify-content: center; }
</style>