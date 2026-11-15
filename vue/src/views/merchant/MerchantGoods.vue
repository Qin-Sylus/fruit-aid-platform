<template>
  <div class="merchant-container content-wrapper">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>商品管理</h2>
      <p>管理您的店铺商品，支持新增、编辑和下架操作</p>
    </div>
    <!-- 搜索与操作区 -->
    <el-card class="search-bar" :body-style="{ padding: '20px' }">
      <div class="search-form">
        <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            class="search-input"
            clearable
        ></el-input>
        <el-select
            v-model="searchForm.categoryId"
            placeholder="选择分类"
            class="search-select"
            clearable
        >
          <el-option label="全部分类" value=""></el-option>
          <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          ></el-option>
        </el-select>
        <el-button type="primary" @click="loadGoods" class="search-btn">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="resetSearch" class="reset-btn">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
        <el-button type="success" @click="handleAdd" class="add-btn">
          <el-icon><CirclePlusFilled /></el-icon>新增商品
        </el-button>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="goods-table" :body-style="{ padding: '0' }">
      <div class="table-container">
        <el-table
            :data="goodsList"
            stripe
            border
            :loading="loading"
            style="width: 100%"
            :max-height="tableMaxHeight"
        >
          <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
          <el-table-column label="商品图片" width="100" align="center">
            <template #default="scope">
              <el-image
                  :src="scope.row.img || defaultImage"
                  style="width: 60px; height: 60px; border-radius: 4px"
                  :preview-src-list="[scope.row.img || defaultImage]"
                  fit="cover"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100" align="center"></el-table-column>
          <el-table-column prop="price" label="价格" width="120" align="center" :formatter="formatPrice"></el-table-column>
          <el-table-column prop="store" label="库存" width="100" align="center"></el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === '1' ? 'success' : 'info'" size="small">
                {{ scope.row.status === '1' ? '上架' : '下架' }}
              </el-tag>
              <el-switch
                  v-model="scope.row.status"
                  active-value="1"
                  inactive-value="0"
                  @change="handleStatusChange(scope.row)"
                  style="margin-left: 8px"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="scope">
              <el-button
                  size="small"
                  type="primary"
                  @click="handleEdit(scope.row)"
                  class="action-btn"
              >
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  @click="handleDelete(scope.row.id)"
                  class="action-btn"
              >
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
        ></el-pagination>
      </div>
      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && goodsList.length === 0">
        <el-empty description="暂无商品数据" :image-size="200">
          <el-button type="primary" @click="handleAdd">新增商品</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
  <!-- 商品表单弹窗 -->
  <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑商品' : '新增商品'"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
  >
    <el-form
        :model="form"
        ref="goodsForm"
        :rules="formRules"
        label-width="100px"
        label-position="right"
    >
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%" clearable>
          <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="商品价格" prop="price">
        <el-input-number
            v-model="form.price"
            :min="0"
            :step="0.01"
            :precision="2"
            controls-position="right"
            style="width: 100%"
        >
          <template #append>元</template>
        </el-input-number>
      </el-form-item>
      <el-form-item label="商品图片" prop="img">
        <el-upload
            :action="uploadUrl"
            list-type="picture-card"
            :on-success="handleImgUpload"
            :file-list="imgFileList"
            :limit="1"
            :on-remove="handleImgRemove"
            accept="image/*"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">建议上传 1:1 比例图片，大小不超过 2MB</div>
      </el-form-item>
      <el-form-item label="商品描述" prop="descr">
        <el-input
            type="textarea"
            v-model="form.descr"
            :rows="3"
            placeholder="请输入商品描述"
            maxlength="200"
            show-word-limit
        ></el-input>
      </el-form-item>
      <el-form-item label="商品特色" prop="specials">
        <el-input v-model="form.specials" placeholder="请输入商品特色"></el-input>
      </el-form-item>
      <el-form-item label="单位" prop="unit">
        <el-input v-model="form.unit" placeholder="如：个、斤、箱"></el-input>
      </el-form-item>
      <el-form-item label="库存数量" prop="store">
        <el-input-number
            v-model="form.store"
            :min="0"
            controls-position="right"
            style="width: 100%"
        ></el-input-number>
      </el-form-item>
      <el-input type="hidden" v-model="form.storeId"></el-input>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" @click="saveGoods" :loading="saveLoading">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  CirclePlusFilled,
  Search,
  Refresh,
  Edit,
  Delete,
  Picture,
  Plus
} from '@element-plus/icons-vue';
import request from '@/utils/request';

// 状态管理
const user = JSON.parse(localStorage.getItem('system-user') || '{}');
const storeId = ref(0);
const categoryList = ref([]);
const goodsList = ref([]);
const total = ref(0);
const loading = ref(false);
const saveLoading = ref(false);
const tableMaxHeight = ref(500);
const defaultImage = ref('/src/assets/imgs/default-goods.png');

// 分页参数
const pageNum = ref(1);
const pageSize = ref(10);

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: ''
});

// 表单弹窗
const formVisible = ref(false);
const goodsForm = ref(null);
const imgFileList = ref([]);
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload';

// 表单数据与验证规则
const form = reactive({
  id: null,
  name: '',
  categoryId: '',
  img: '',
  descr: '',
  specials: '',
  price: '',
  unit: '',
  store: '',
  storeId: '',
  status: '1'
});

const formRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度在 2-50 个字符之间', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  img: [{ required: true, message: '请上传商品图片', trigger: 'change' }],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ],
  store: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须大于等于0', trigger: 'blur' }
  ],
  unit: [{ required: true, message: '请输入商品单位', trigger: 'blur' }]
};

// 价格格式化函数
const formatPrice = (row, column, cellValue) => {
  if (cellValue == null || cellValue === '') return '-';
  return `¥${parseFloat(cellValue).toFixed(2)}`;
};

// 计算表格高度
const calculateTableHeight = () => {
  nextTick(() => {
    const windowHeight = window.innerHeight;
    const headerHeight = 60; // 顶部导航高度
    const pageHeaderHeight = 100; // 页面头部高度
    const searchBarHeight = 100; // 搜索栏高度
    const paginationHeight = 60; // 分页高度
    const padding = 40; // 内边距

    tableMaxHeight.value = windowHeight - headerHeight - pageHeaderHeight - searchBarHeight - paginationHeight - padding;
  });
};

// 初始化
onMounted(() => {
  if (!user.id) {
    ElMessage.error('请先登录');
    return;
  }

  // 监听窗口大小变化
  window.addEventListener('resize', calculateTableHeight);
  calculateTableHeight();

  // 获取店铺ID
  request.get('/store/myStore', { params: { userId: user.id } })
      .then(res => {
        if (res.data) {
          storeId.value = res.data.id;
          form.storeId = res.data.id;
          // 加载分类和商品
          loadCategories();
          loadGoods();
        } else {
          ElMessage.warning('请先创建店铺');
          setTimeout(() => window.location.href = '/merchant/store-create', 1500);
        }
      })
      .catch(() => {
        ElMessage.error('获取店铺信息失败，请检查网络连接');
      });
});

onUnmounted(() => {
  window.removeEventListener('resize', calculateTableHeight);
});

// 加载分类
const loadCategories = () => {
  request.get('/category/selectAll').then(res => {
    categoryList.value = res.data || [];
  }).catch(() => {
    ElMessage.error('加载分类数据失败');
  });
};

// 加载商品列表
const loadGoods = () => {
  loading.value = true;
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    storeId: storeId.value,
    name: searchForm.name,
    categoryId: searchForm.categoryId
  };

  request.get('/goods/selectPage', { params })
      .then(res => {
        goodsList.value = res.data?.list || [];
        total.value = res.data?.total || 0;
      })
      .catch(() => {
        ElMessage.error('加载商品数据失败');
        goodsList.value = [];
        total.value = 0;
      })
      .finally(() => loading.value = false);
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  pageNum.value = 1;
  loadGoods();
};

const handleCurrentChange = (val) => {
  pageNum.value = val;
  loadGoods();
};

// 搜索与重置
const resetSearch = () => {
  searchForm.name = '';
  searchForm.categoryId = '';
  pageNum.value = 1;
  loadGoods();
};

// 新增商品
const handleAdd = () => {
  formVisible.value = true;
  // 重置表单
  Object.keys(form).forEach(key => {
    form[key] = key === 'storeId' ? storeId.value : (key === 'status' ? '1' : '');
  });
  imgFileList.value = [];
};

// 编辑商品
const handleEdit = (row) => {
  formVisible.value = true;
  // 复制数据
  Object.assign(form, { ...row, storeId: storeId.value });
  imgFileList.value = row.img ? [{ name: '商品图片', url: row.img }] : [];
};

// 图片上传处理
const handleImgUpload = (res) => {
  if (res.code === '200' && res.data) {
    form.img = res.data;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error('图片上传失败');
  }
};

// 图片移除处理
const handleImgRemove = () => {
  form.img = '';
  imgFileList.value = [];
};

// 保存商品
const saveGoods = () => {
  goodsForm.value.validate((valid) => {
    if (!valid) return;

    saveLoading.value = true;
    const url = form.id ? '/goods/update' : '/goods/add';
    const method = form.id ? 'put' : 'post';

    request({ url, method, data: form })
        .then(res => {
          if (res.code === '200') {
            ElMessage.success('操作成功');
            formVisible.value = false;
            loadGoods();
          } else {
            ElMessage.error(res.msg || '操作失败');
          }
        })
        .catch(() => {
          ElMessage.error('操作失败，请检查网络连接');
        })
        .finally(() => {
          saveLoading.value = false;
        });
  });
};

// 状态变更
const handleStatusChange = (row) => {
  const originalStatus = row.status;
  const updateData = {
    id: row.id,
    status: row.status,
    name: row.name,
    categoryId: row.categoryId,
    price: row.price,
    store: row.store,
    img: row.img,
    descr: row.descr || '',
    specials: row.specials || '',
    unit: row.unit || '',
    storeId: storeId.value
  };

  request.put('/goods/update', updateData)
      .then(res => {
        if (res.code === '200') {
          ElMessage.success('状态更新成功');
        } else {
          row.status = originalStatus;
          ElMessage.error(res.msg || '状态更新失败');
        }
      })
      .catch(() => {
        row.status = originalStatus;
        ElMessage.error('状态更新失败，请重试');
      });
};

// 删除商品
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复', '确认删除', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(() => {
    request.delete(`/goods/delete/${id}`)
        .then(res => {
          if (res.code === '200') {
            ElMessage.success('删除成功');
            loadGoods();
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(() => {
          ElMessage.error('删除失败，请检查网络连接');
        });
  }).catch(() => {});
};
</script>

<style scoped>
/* 假设侧边栏使用固定宽度，内容区域自适应剩余空间 */
.merchant-container {
  /* 确保容器使用 flex 布局适应父容器 */
  width: 100%;
  min-width: 0; /* 解决 flex 子元素溢出问题 */
}

/* 如果存在布局容器，添加 flex 布局控制 */
.layout-container {
  display: flex;
  width: 100%;
  min-height: 100vh;
}

/* 假设侧边栏类名为 sidebar */
.sidebar {
  width: 200px; /* 侧边栏固定宽度 */
  flex-shrink: 0; /* 防止侧边栏被压缩 */
}

/* 内容区域自适应剩余宽度 */
.content-wrapper {
  flex: 1; /* 占据剩余所有宽度 */
  padding: 0; /* 移除可能导致空白的内边距 */
  margin: 0; /* 移除可能导致空白的外边距 */
  overflow: hidden; /* 防止内容溢出产生横向滚动 */
}

/* 同时检查父元素是否有约束，如果有需要同步调整 */
:deep(.content-wrapper) {
  padding: 0; /* 清除内容容器的内边距 */
  margin: 0;
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-bar {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 160px;
}

.add-btn {
  margin-left: auto;
}

.goods-table {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.table-container {
  flex: 1;
  overflow: hidden;
}

.pagination-container {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.empty-state {
  padding: 60px 0;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-error {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #c0c4cc;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.action-btn {
  margin: 0 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .merchant-container {
    padding: 16px;
  }

  .search-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .search-select {
    width: 100%;
  }

  .add-btn {
    margin-left: 0;
  }
}

/* 滚动条样式优化 */
:deep(.el-table__body-wrapper) {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f5f5f5;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

:deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 卡片阴影优化 */
:deep(.el-card) {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #e4e7ed;
  padding: 16px 20px;
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table .cell) {
  padding: 8px 12px;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 4px;
}

:deep(.el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
}

:deep(.el-button--success) {
  background-color: #67c23a;
  border-color: #67c23a;
}

:deep(.el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
}
</style>