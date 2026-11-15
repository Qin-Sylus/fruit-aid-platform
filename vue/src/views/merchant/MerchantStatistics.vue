<template>
  <div class="merchant-container">
    <div class="page-header">
      <h2>销售统计</h2>
      <p>查看本店铺的订单量、销售额与商品热销情况</p>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="stat-item"><span class="label">订单总数</span><span class="value">{{ stats.orderCount }}</span></div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="stat-item"><span class="label">销售总额</span><span class="value">¥{{ stats.totalAmount }}</span></div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="stat-item"><span class="label">待发货订单</span><span class="value">{{ stats.pending }}</span></div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="stat-item"><span class="label">完成订单</span><span class="value">{{ stats.finished }}</span></div></el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" :body-style="{ padding: '20px' }">
      <template #header><span>近半年销售额趋势</span></template>
      <div ref="chartRef" class="chart"></div>
    </el-card>

    <el-card :body-style="{ padding: '20px' }" class="chart-card">
      <template #header><span>数据说明</span></template>
      <p class="tip">统计口径：以本店铺已完成订单（状态 3）为有效销量；后端未启动时页面展示为空数据。</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import request from '@/utils/request';

const user = JSON.parse(localStorage.getItem('system-user') || '{}');
const chartRef = ref(null);
let chart = null;

const stats = reactive({ orderCount: 0, totalAmount: '0.00', pending: 0, finished: 0 });
const months = ref([]);
const amounts = ref([]);

const renderChart = () => {
  if (!chartRef.value) return;
  if (!chart) chart = echarts.init(chartRef.value);
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 24, top: 32, bottom: 32 },
    xAxis: { type: 'category', data: months.value },
    yAxis: { type: 'value', name: '元' },
    series: [{
      name: '销售额',
      type: 'bar',
      data: amounts.value,
      barWidth: '45%',
      itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }
    }]
  });
};

const resize = () => { chart && chart.resize(); };

const loadData = () => {
  request.get('/orders/selectAll')
    .then((res) => {
      const list = res.data || [];
      stats.orderCount = list.length;
      stats.pending = list.filter((o) => String(o.status) === '1').length;
      stats.finished = list.filter((o) => String(o.status) === '3').length;
      const sum = list.reduce((acc, o) => acc + (Number(o.total) || 0), 0);
      stats.totalAmount = sum.toFixed(2);

      const labels = [];
      const values = [];
      for (let offset = 5; offset >= 0; offset--) {
        const d = new Date();
        d.setMonth(d.getMonth() - offset);
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
        labels.push(key.slice(5) + '月');
        values.push(list.filter((o) => (o.createTime || '').startsWith(key)).reduce((acc, o) => acc + (Number(o.total) || 0), 0));
      }
      months.value = labels;
      amounts.value = values;
      nextTick(renderChart);
    })
    .catch(() => {
      const labels = [];
      for (let offset = 5; offset >= 0; offset--) {
        const d = new Date();
        d.setMonth(d.getMonth() - offset);
        labels.push(`${String(d.getMonth() + 1)}月`);
      }
      months.value = labels;
      amounts.value = [0, 0, 0, 0, 0, 0];
      nextTick(renderChart);
    });
};

onMounted(() => {
  window.addEventListener('resize', resize);
  if (!user.id) {
    ElMessage.warning('请先登录后查看统计数据');
  }
  loadData();
});

onUnmounted(() => {
  window.removeEventListener('resize', resize);
  chart && chart.dispose();
});
</script>

<style scoped>
.merchant-container { width: 100%; min-width: 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; font-size: 24px; font-weight: 600; }
.page-header p { margin: 0; color: #606266; font-size: 14px; }
.stat-row { margin-bottom: 8px; }
.stat-item { display: flex; flex-direction: column; gap: 8px; }
.stat-item .label { color: #909399; font-size: 14px; }
.stat-item .value { color: #303133; font-size: 26px; font-weight: 600; }
.chart-card { margin-bottom: 16px; }
.chart { width: 100%; height: 320px; }
.tip { margin: 0; color: #606266; font-size: 14px; line-height: 1.8; }
</style>