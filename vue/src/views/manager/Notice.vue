<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.title" style="width: 300px; margin-right: 10px" placeholder="请输入标题查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="标题" prop="title"></el-table-column>
        <el-table-column label="内容" prop="content"></el-table-column>
        <el-table-column label="发布时间" prop="time"></el-table-column>
        <!-- 优化后的图片列 -->
        <el-table-column label="图片" header-align="center" width="200">
          <template #default="scope">
            <div v-if="scope.row.tupian && scope.row.tupian.split(',').length > 0">
              <el-carousel height="100px" indicator-position="outside" :autoplay="false">
                <el-carousel-item v-for="(img, index) in scope.row.tupian.split(',')" :key="index">
                  <el-image
                      :src="getFullImageUrl(img)"
                      style="width: 100%; height: 100%; object-fit: contain"
                      :preview-src-list="getPreviewImages(scope.row.tupian)"
                      :initial-index="index"
                      fit="contain"
                      hide-on-click-modal
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>加载失败</span>
                      </div>
                    </template>
                  </el-image>
                </el-carousel-item>
              </el-carousel>
            </div>
            <div v-else class="no-image">无图片</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="系统公告信息" width="50%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="data.form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" v-model="data.form.content" autocomplete="off" :rows="4" />
        </el-form-item>

        <!-- 优化后的轮播图片上传 -->
        <el-form-item label="轮播图片">
          <div style="margin-bottom: 10px;">
            <el-upload
                action="http://localhost:9090/files/upload"
                list-type="picture-card"
                :on-success="handleImageSuccess"
                :on-remove="handleImageRemove"
                :limit="5"
                :file-list="getImageFileList()"
                accept=".jpg,.jpeg,.png,.gif"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG、GIF 格式，最多上传 5 张图片</div>
          </div>

          <div v-if="data.form.carouselImages && data.form.carouselImages.split(',').filter(img => img).length > 0" style="margin-top: 20px">
            <div style="margin-bottom: 10px; font-size: 14px; color: #666;">预览：</div>
            <el-carousel height="200px" indicator-position="outside" :autoplay="false">
              <el-carousel-item v-for="(img, index) in data.form.carouselImages.split(',').filter(img => img)" :key="index">
                <el-image
                    :src="getFullImageUrl(img)"
                    style="width: 100%; height: 100%; object-fit: contain"
                    fit="contain"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                </el-image>
              </el-carousel-item>
            </el-carousel>
          </div>
        </el-form-item>

        <!-- 优化后的视频上传 -->
        <el-form-item label="视频">
          <el-upload
              action="http://localhost:9090/files/upload"
              accept="video/*"
              :on-success="handleVideoSuccess"
              :on-remove="handleVideoRemove"
              :limit="1"
              :file-list="getVideoFileList()"
          >
            <el-button type="primary">上传视频</el-button>
            <template #tip>
              <div class="upload-tip">支持 MP4、AVI 等视频格式，最多上传 1 个视频</div>
            </template>
          </el-upload>
          <video v-if="data.form.videoUrl" :src="getFullImageUrl(data.form.videoUrl)" controls style="margin-top: 10px; width: 100%; max-height: 300px"></video>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { Plus, Picture } from "@element-plus/icons-vue";

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {
    carouselImages: '', // 轮播图片字段
    videoUrl: '', // 视频字段
  },
  tableData: [],
  title: null
})

// 获取完整的图片URL
const getFullImageUrl = (url) => {
  if (!url) return '';
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http')) return url;
  // 否则添加基础路径
  return `http://localhost:9090/files/${url}`;
}

// 获取预览图片列表
const getPreviewImages = (images) => {
  if (!images) return [];
  return images.split(',').map(img => getFullImageUrl(img));
}

// 获取图片文件列表用于显示
const getImageFileList = () => {
  if (!data.form.carouselImages) return [];
  return data.form.carouselImages.split(',').filter(img => img).map(url => ({
    name: url.split('/').pop(),
    url: getFullImageUrl(url)
  }));
}

// 获取视频文件列表用于显示
const getVideoFileList = () => {
  if (!data.form.videoUrl) return [];
  return [{
    name: data.form.videoUrl.split('/').pop(),
    url: getFullImageUrl(data.form.videoUrl)
  }];
}

// 图片上传成功处理
const handleImageSuccess = (response) => {
  if (response.code === '200') {
    const imgUrl = response.data;
    const currentImages = data.form.carouselImages ? data.form.carouselImages.split(',').filter(img => img) : [];
    currentImages.push(imgUrl);
    data.form.carouselImages = currentImages.join(',');
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error('图片上传失败');
  }
}

// 图片删除处理
const handleImageRemove = (file) => {
  const url = file.url || file.response?.data;
  const fileName = url.split('/').pop();
  const currentImages = data.form.carouselImages ? data.form.carouselImages.split(',').filter(img => img) : [];
  const updatedImages = currentImages.filter(img => !img.includes(fileName));
  data.form.carouselImages = updatedImages.join(',');
}

// 视频上传成功处理
const handleVideoSuccess = (response) => {
  if (response.code === '200') {
    data.form.videoUrl = response.data;
    ElMessage.success('视频上传成功');
  } else {
    ElMessage.error('视频上传失败');
  }
}

// 视频删除处理
const handleVideoRemove = () => {
  data.form.videoUrl = '';
}

// 分页查询
const load = () => {
  request.get('/notice/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title
    }
  }).then(res => {
    data.tableData = res.data?.list || [];
    data.total = res.data?.total || 0;
  })
}

// 新增
const handleAdd = () => {
  data.form = {
    carouselImages: '',
    videoUrl: ''
  }
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/notice/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const update = () => {
  request.put('/notice/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  if (!data.form.title) {
    ElMessage.error('请输入标题');
    return;
  }
  data.form.id ? update() : add();
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/notice/delete/' + id).then(res => {
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
  data.title = null
  load()
}

// 初始化加载数据
load()
</script>

<style scoped>
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.no-image {
  color: #999;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}
</style>