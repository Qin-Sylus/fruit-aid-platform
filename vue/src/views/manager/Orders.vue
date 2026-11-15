<!--<template>-->
<!--  <div>-->

<!--    <div class="card" style="margin-bottom: 5px;">-->
<!--      <el-input v-model="data.orderNo" style="width: 300px; margin-right: 10px" placeholder="请输入订单编号查询"></el-input>-->
<!--      <el-button type="primary" @click="load">查询</el-button>-->
<!--      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>-->
<!--    </div>-->

<!--    <div class="card" style="margin-bottom: 5px">-->
<!--      <el-table :data="data.tableData" stripe>-->
<!--        <el-table-column label="订单编号" prop="orderNo"></el-table-column>-->
<!--        <el-table-column label="产品名称" prop="goodsName"></el-table-column>-->
<!--        <el-table-column label="产品图片" prop="goodsImg">-->
<!--          <template #default="scope">-->
<!--            <el-image style="width: 50px; height: 50px; border-radius: 5px" :src="scope.row.goodsImg"-->
<!--                      :preview-src-list="[scope.row.goodsImg]" preview-teleported></el-image>-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column label="购买数量" prop="num"></el-table-column>-->
<!--        <el-table-column label="下单人" prop="userName"></el-table-column>-->
<!--        <el-table-column label="订单状态" prop="status">-->
<!--          <template #default="scope">-->
<!--            <el-tag v-if="scope.row.status === '已取消'" type="danger">已取消</el-tag>-->
<!--            <el-tag v-if="scope.row.status === '待支付'" type="warning">待支付</el-tag>-->
<!--            <el-tag v-if="scope.row.status === '待发货'" type="primary">待发货</el-tag>-->
<!--            <el-tag v-if="scope.row.status === '待收货'" type="primary">待收货</el-tag>-->
<!--            <el-tag v-if="scope.row.status === '已完成'" type="success">已完成</el-tag>-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column label="下单时间" prop="time"></el-table-column>-->
<!--        <el-table-column label="操作" width="160">-->
<!--          <template #default="scope">-->
<!--            <el-button  v-if="data.user.role === 'USER' && scope.row.status === '待支付'" type="primary" @click="changeStatus(scope.row, '待发货')">支付</el-button>-->
<!--            <el-button  v-if="data.user.role === 'ADMIN' && scope.row.status === '待发货'" type="primary" @click="changeStatus(scope.row, '待收货')">发货</el-button>-->
<!--            <el-button  v-if="data.user.role === 'USER' && scope.row.status === '待收货'" type="success" @click="changeStatus(scope.row, '已完成')">收货</el-button>-->
<!--            <el-button v-if="data.user.role === 'USER' && scope.row.status === '待支付'" type="danger" @click="changeStatus(scope.row, '已取消')">取消</el-button>-->
<!--            <el-button v-if="data.user.role === 'ADMIN'" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--      </el-table>-->
<!--    </div>-->

<!--    <div class="card" v-if="data.total">-->
<!--      <el-pagination @current-change="load" background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>-->
<!--    </div>-->

<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import request from "@/utils/request";-->
<!--import {reactive} from "vue";-->
<!--import {ElMessageBox, ElMessage} from "element-plus";-->

<!--const data = reactive({-->
<!--  user: JSON.parse(localStorage.getItem('system-user') || '{}'),-->
<!--  pageNum: 1,-->
<!--  pageSize: 10,-->
<!--  total: 0,-->
<!--  formVisible: false,-->
<!--  form: {},-->
<!--  tableData: [],-->
<!--  orderNo: null,-->
<!--})-->

<!--// 分页查询-->
<!--const load = () => {-->
<!--  request.get('/orders/selectPage', {-->
<!--    params: {-->
<!--      pageNum: data.pageNum,-->
<!--      pageSize: data.pageSize,-->
<!--      orderNo: data.orderNo,-->
<!--      userId: data.user.role === 'ADMIN' ? null : data.user.id-->
<!--    }-->
<!--  }).then(res => {-->
<!--    data.tableData = res.data?.list-->
<!--    data.total = res.data?.total-->
<!--  })-->
<!--}-->

<!--// 操作按钮-->
<!--const changeStatus = (row, status) => {-->
<!--  row.status = status-->
<!--  request.put('/orders/update', row).then(res => {-->
<!--    if (res.code === '200') {-->
<!--      load()-->
<!--      ElMessage.success('操作成功')-->
<!--      data.formVisible = false-->
<!--    } else {-->
<!--      ElMessage.error(res.msg)-->
<!--    }-->
<!--  })-->
<!--}-->

<!--// 删除-->
<!--const handleDelete = (id) => {-->
<!--  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {-->
<!--    request.delete('/orders/delete/' + id).then(res => {-->
<!--      if (res.code === '200') {-->
<!--        load()-->
<!--        ElMessage.success('操作成功')-->
<!--      } else {-->
<!--        ElMessage.error(res.msg)-->
<!--      }-->
<!--    })-->
<!--  }).catch(err => {})-->
<!--}-->

<!--// 重置-->
<!--const reset = () => {-->
<!--  data.orderNo = null-->
<!--  load()-->
<!--}-->

<!--load()-->
<!--</script>-->

<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.orderNo" style="width: 300px; margin-right: 10px" placeholder="请输入订单编号查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="订单编号" prop="orderNo"></el-table-column>
        <el-table-column label="产品名称" prop="goodsName"></el-table-column>
        <el-table-column label="产品图片" prop="goodsImg">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px; border-radius: 5px" :src="scope.row.goodsImg"
                      :preview-src-list="[scope.row.goodsImg]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column label="购买数量" prop="num"></el-table-column>
        <el-table-column label="下单人" prop="userName"></el-table-column>
        <el-table-column label="订单状态" prop="status">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '已取消'" type="danger">已取消</el-tag>
            <el-tag v-if="scope.row.status === '待支付'" type="warning">待支付</el-tag>
            <el-tag v-if="scope.row.status === '待发货'" type="primary">待发货</el-tag>
            <el-tag v-if="scope.row.status === '待收货'" type="primary">待收货</el-tag>
            <el-tag v-if="scope.row.status === '已完成'" type="success">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" prop="time"></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button  v-if="data.user.role === 'USER' && scope.row.status === '待支付'" type="primary" @click="changeStatus(scope.row, '待发货')">支付</el-button>
            <el-button  v-if="(data.user.role === 'ADMIN' || data.user.role === 'MERCHANT') && scope.row.status === '待发货'" type="primary" @click="changeStatus(scope.row, '待收货')">发货</el-button>
            <el-button  v-if="data.user.role === 'USER' && scope.row.status === '待收货'" type="success" @click="changeStatus(scope.row, '已完成')">收货</el-button>
            <el-button v-if="data.user.role === 'USER' && scope.row.status === '待支付'" type="danger" @click="changeStatus(scope.row, '已取消')">取消</el-button>
            <el-button v-if="data.user.role === 'ADMIN'" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, onMounted} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  orderNo: null,
  storeId: 0 // 新增店铺ID
})

onMounted(() => {
  // 先获取商家店铺ID
  if (data.user.role === 'MERCHANT') {
    request.get('/store/myStore', { params: { userId: data.user.id } }).then(res => {
      if (res.data) {
        data.storeId = res.data.id;
        load(); // 获取店铺ID后再加载订单
      }
    });
  } else {
    load(); // 非商家直接加载
  }
});

// 分页查询
const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    orderNo: data.orderNo
  };

  // 商家角色添加店铺ID筛选
  if (data.user.role === 'MERCHANT') {
    params.storeId = data.storeId;
  } else if (data.user.role === 'USER') {
    params.userId = data.user.id;
  }

  request.get('/orders/selectPage', { params }).then(res => {
    data.tableData = res.data?.list;
    data.total = res.data?.total;
  });
}

// 操作按钮
const changeStatus = (row, status) => {
  // 商家只能将状态从待支付改为待发货
  if (data.user.role === 'MERCHANT' && !(row.status === '待支付' && status === '待发货')) {
    ElMessage.warning('无权执行此操作');
    return;
  }

  row.status = status
  request.put('/orders/update', row).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/orders/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.orderNo = null
  load()
}
</script>