<template>
  <div class="card">
    <div style="margin-bottom: 10px; font-size: 18px; font-weight: bold;">我的购物车</div>
    <el-table :data="data.cartList" stripe v-if="data.cartList.length > 0">
      <el-table-column label="商品图片" prop="goodsImg" width="100">
        <template #default="scope">
          <el-image
              style="width: 50px; height: 50px"
              :src="scope.row.goodsImg"
              :preview-src-list="[scope.row.goodsImg]"
              preview-teleported
              hide-on-click-modal>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="商品名称" prop="goodsName">
        <template #default="scope">
          <span style="cursor: pointer; color: #409EFF;" @click="viewGoods(scope.row.goodsId)">
            {{ scope.row.goodsName }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: bold;">
            ¥{{ formatPrice(scope.row.price) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="150">
        <template #default="scope">
          <el-input-number
              v-model="scope.row.num"
              @change="updateNum(scope.row)"
              :min="1"
              :max="999"
              size="small"
              controls-position="right">
          </el-input-number>
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: bold;">
            ¥{{ formatSubtotal(scope.row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button
              type="danger"
              size="small"
              @click="deleteCartItem(scope.row.id)"
              :loading="data.deletingId === scope.row.id">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-else style="text-align: center; padding: 50px 0; color: #888;">
      <el-empty description="购物车是空的，快去添加商品吧~">
        <template #image>
          <el-icon :size="80"><ShoppingCart /></el-icon>
        </template>
        <el-button type="primary" @click="$router.push('/front/home')">
          去逛逛
        </el-button>
      </el-empty>
    </div>

    <!-- 结算区域 -->
    <div v-if="data.cartList.length > 0" class="checkout-area">
      <div class="total-info">
        <span>共 {{ totalCount }} 件商品</span>
        <span style="margin-left: 20px;">
          合计: <strong style="color: #f56c6c; font-size: 24px; margin-left: 5px;">¥{{ totalPrice }}</strong>
        </span>
      </div>
      <div class="checkout-buttons">
        <el-button @click="clearCart" :loading="data.clearing">
          清空购物车
        </el-button>
        <el-button
            type="primary"
            size="large"
            @click="checkout"
            :loading="data.checkingOut"
            style="margin-left: 20px; padding: 0 40px;">
          去结算
        </el-button>
        <!-- 测试按钮（调试用） -->
        <el-button
            type="info"
            size="small"
            @click="testOrder"
            style="margin-left: 10px;"
            v-if="data.cartList.length > 0">
          测试
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import { ShoppingCart } from "@element-plus/icons-vue";

const router = useRouter();
const data = reactive({
  user: (() => {
    try {
      return JSON.parse(localStorage.getItem('system-user') || '{}');
    } catch (err) {
      console.error('解析用户信息失败', err);
      return {};
    }
  })(),
  cartList: [],
  deletingId: null,
  clearing: false,
  checkingOut: false
});

// 计算商品总数
const totalCount = computed(() => {
  return data.cartList.reduce((sum, item) => sum + (Number(item.num) || 0), 0);
});

// 计算总价 - 使用正确的价格字段
const totalPrice = computed(() => {
  const total = data.cartList.reduce((sum, item) => {
    const price = Number(item.price) || 0;
    const num = Number(item.num) || 0;
    return sum + (price * num);
  }, 0);

  return total.toFixed(2);
});

// 格式化价格
const formatPrice = (price) => {
  const numPrice = Number(price) || 0;
  return numPrice.toFixed(2);
};

// 计算小计
const formatSubtotal = (row) => {
  const price = Number(row.price) || 0;
  const num = Number(row.num) || 0;
  return (price * num).toFixed(2);
};

// 查看商品详情
const viewGoods = (goodsId) => {
  router.push(`/front/goods/${goodsId}`);
};

// 加载购物车数据
const loadCart = () => {
  if (!data.user.id) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  request.get(`/cart/getByUserId/${data.user.id}`)
      .then(res => {
        if (res.code === '200') {
          data.cartList = res.data || [];

          // 调试信息
          console.log('=== 购物车数据加载成功 ===');
          if (data.cartList.length > 0) {
            const item = data.cartList[0];
            console.log('商品结构:', Object.keys(item));
            console.log('价格字段值:', item.price);
            console.log('价格类型:', typeof item.price);
            console.log('完整数据示例:', JSON.stringify(item, null, 2));
          }
        } else {
          ElMessage.error(res.msg || '加载购物车失败');
        }
      })
      .catch(err => {
        console.error('加载购物车失败：', err);
        ElMessage.error('加载购物车失败，请刷新页面重试');
      });
};

// 更新数量
const updateNum = (row) => {
  if (!row.num || row.num < 1) {
    row.num = 1;
    return;
  }

  request.put('/cart/updateNum', null, {
    params: {
      id: row.id,
      num: row.num
    }
  })
      .then(res => {
        if (res.code === '200') {
          ElMessage.success('数量已更新');
        } else {
          ElMessage.error(res.msg || '更新数量失败');
          loadCart();
        }
      })
      .catch(err => {
        console.error('更新数量失败：', err);
        ElMessage.error('更新数量失败，请重试');
        loadCart();
      });
};

// 删除购物车项
const deleteCartItem = (id) => {
  data.deletingId = id;

  ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/cart/delete/${id}`)
        .then(res => {
          if (res.code === '200') {
            ElMessage.success('删除成功');
            loadCart();
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(err => {
          console.error('删除失败：', err);
          ElMessage.error('删除失败，请重试');
        })
        .finally(() => {
          data.deletingId = null;
        });
  }).catch(() => {
    data.deletingId = null;
  });
};

// 清空购物车
const clearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    data.clearing = true;

    request.delete(`/cart/clear/${data.user.id}`)
        .then(res => {
          if (res.code === '200') {
            ElMessage.success('购物车已清空');
            data.cartList = [];
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(err => {
          console.error('清空失败：', err);
          ElMessage.error('清空失败，请重试');
        })
        .finally(() => {
          data.clearing = false;
        });
  });
};

// 生成订单号
const generateOrderNo = () => {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 10000);
  return `ORDER${timestamp}${random}`;
};

// 结算
const checkout = () => {
  if (data.cartList.length === 0) {
    ElMessage.warning('购物车为空');
    return;
  }

  data.checkingOut = true;

  // 创建订单数据列表 - 简化版
  const orderDataList = data.cartList.map(item => {
    const price = Number(item.price) || 0;
    const num = Number(item.num) || 0;

    return {
      goodsId: item.goodsId,
      num: num,
      userId: data.user.id
      // 不再发送totalAmount，让后端计算
      // 前端可以发送价格信息，但让后端计算更安全
    };
  });

  console.log('结算数据（简化）:', orderDataList);

  // 发送请求 - 使用简化数据
  request.post('/orders/batchAdd', orderDataList)
      .then(res => {
        if (res.code === '200') {
          ElMessage.success('下单成功');

          // 清空购物车
          request.delete(`/cart/clear/${data.user.id}`)
              .then(() => {
                data.cartList = [];
                router.push('/orders');
              })
              .catch(err => {
                console.error('清空购物车失败：', err);
                router.push('/orders');
              });
        } else {
          ElMessage.error(res.msg || '下单失败');
        }
      })
      .catch(err => {
        console.error('结算请求失败：', err);
        console.error('错误详情：', err.response?.data || err.message);

        // 更详细的错误信息
        let errorMsg = '下单失败，请重试';
        if (err.response?.data?.msg) {
          errorMsg = err.response.data.msg;
        } else if (err.message.includes('totalAmount')) {
          errorMsg = '订单数据格式错误，请检查后端Orders实体类是否有totalAmount字段的getter/setter';
        }

        ElMessage.error(errorMsg);
      })
      .finally(() => {
        data.checkingOut = false;
      });
};
// 测试订单接口（调试用）
const testOrder = () => {
  if (data.cartList.length === 0) {
    ElMessage.warning('购物车为空，无法测试');
    return;
  }

  const testItem = data.cartList[0];
  const price = Number(testItem.price) || 0;
  const num = Number(testItem.num) || 0;
  const totalAmount = price * num;

  const testData = {
    orderNo: 'TEST' + Date.now(),
    goodsId: testItem.goodsId,
    num: num,
    totalAmount: totalAmount,
    userId: data.user.id,
    status: "待付款",
    time: new Date().toISOString().slice(0, 19).replace('T', ' '),
    storeId: 1,
    goodsName: testItem.goodsName,
    goodsImg: testItem.goodsImg
  };

  console.log('测试数据:', testData);
  console.log('测试数据JSON:', JSON.stringify(testData, null, 2));

  ElMessageBox.confirm('将发送测试订单数据到/orders/add接口，确定吗？', '测试确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    request.post('/orders/add', testData)
        .then(res => {
          console.log('测试结果:', res);
          if (res.code === '200') {
            ElMessage.success('测试成功！订单ID: ' + res.data);
          } else {
            ElMessage.error('测试失败: ' + (res.msg || '未知错误'));
          }
        })
        .catch(err => {
          console.error('测试失败:', err);
          console.error('错误响应:', err.response?.data);

          let errorMsg = '测试失败: ';
          if (err.response?.data?.msg) {
            errorMsg += err.response.data.msg;
          } else if (err.message) {
            errorMsg += err.message;
          } else {
            errorMsg += '未知错误';
          }

          ElMessage.error(errorMsg);
        });
  });
};

onMounted(() => {
  loadCart();
});
</script>

<style scoped>
.card {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.checkout-area {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-info {
  font-size: 16px;
  color: #606266;
}

.checkout-buttons {
  display: flex;
  align-items: center;
}

.el-table {
  margin-top: 20px;
}

.el-image {
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

:deep(.el-input-number) {
  width: 120px;
}
</style>