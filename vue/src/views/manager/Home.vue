<template>
  <div>
    <!-- 轮播图部分 -->
    <div style="margin-bottom: 30px">
      <el-carousel height="300px" indicator-position="outside" arrow="hover">
        <el-carousel-item v-for="item in carouselData" :key="item.id">
          <div class="carousel-content" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="carousel-overlay">
              <h2>{{ item.title }}</h2>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 系统公告部分 -->
    <div>
      <div style="padding-left: 20px; font-size: 20px; font-weight: bold; margin-bottom: 20px; color: #409EFF">
        📢 品牌最新公告
      </div>
      <el-timeline style="width: 100%">
        <el-timeline-item :timestamp="item.time" placement="top" v-for="item in data.noticeList" :key="item.id">
          <div class="card" style="padding: 15px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 10px;">
            <h3 style="margin-top: 0; color: #333;">{{ item.title }}</h3>
            <p style="color: #666; line-height: 1.6;">{{ item.content }}</p>
            <!-- 图片展示区域 -->
            <div v-if="item.images && item.images.length" style="margin-top: 15px;">
              <div style="display: flex; flex-wrap: wrap; gap: 10px;">
                <div v-for="(img, idx) in item.images" :key="idx" style="flex: 1; min-width: 200px; max-width: 300px;">
                  <img
                      :src="img.url"
                      :alt="img.alt || '公告图片'"
                      style="width: 100%; height: auto; border-radius: 4px; cursor: pointer; transition: transform 0.3s;"
                      @click="previewImage(img.url)"
                      onmouseover="this.style.transform='scale(1.02)'"
                      onmouseout="this.style.transform='scale(1)'">
                </div>
              </div>
            </div>
            <!-- 视频展示区域 -->
            <div v-if="item.videos && item.videos.length" style="margin-top: 15px;">
              <div style="display: flex; flex-wrap: wrap; gap: 15px;">
                <div v-for="(video, idx) in item.videos" :key="idx" style="flex: 1; min-width: 300px; max-width: 600px;">
                  <video
                      :src="video.url"
                      :poster="video.poster || ''"
                      controls
                      style="width: 100%; border-radius: 4px; background: #000;"
                  >
                    您的浏览器不支持视频播放
                  </video>
                  <p v-if="video.caption" style="margin: 5px 0 0; color: #666; font-size: 14px;">{{ video.caption }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>

</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  noticeList: []
})

// 轮播图数据
const carouselData = [
  {
    id: 1,
    title: "新鲜农产品直供",
    description: "从田间到餐桌，为您提供最新鲜的农产品",
    image: "/src/assets/imgs/bg.png"
  },
  {
    id: 2,
    title: "品质保证",
    description: "严格的质量控制，确保每一份产品都安全可靠",
    image: "/src/assets/imgs/2.png"
  },
  {
    id: 3,
    title: "便捷购物体验",
    description: "简单易用的购物系统，让您轻松选购心仪产品",
    image: "/src/assets/imgs/1.png"
  },
  {
    id:4,
    title: "柑橘种植过程",
    description: "从幼苗到成熟的过程，让人在等待后享受到美味的果实",
    image: "/src/assets/imgs/柑橘.jpg"
  }
]

// 请求后台的公告数据
const loadNotices = () => {
  request.get('/notice/selectAll').then(res => {
    data.noticeList = res.data
  })
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.carousel-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
  padding: 40px;
}

.carousel-overlay h2 {
  font-size: 32px;
  margin-bottom: 15px;
  font-weight: bold;
}

.carousel-overlay p {
  font-size: 18px;
  opacity: 0.9;
}

.card {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.card h4 {
  margin: 0 0 10px 0;
  color: #409EFF;
  font-size: 16px;
}

.card p {
  margin: 0;
  color: #666;
  line-height: 1.5;
}
</style>