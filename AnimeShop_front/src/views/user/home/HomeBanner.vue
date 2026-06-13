<template>
  <div class="home-banner">
    <el-carousel height="375px">
      <el-carousel-item v-for="(Banner, index) in banners" :key="index">
        <h3>{{ Banner.title }}</h3>
        <img :src="Banner.imageUrl" :alt="Banner.title" />
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup lang="ts">
import { getBannerService } from '@/api/home.ts'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
const banners = ref<Banner[]>([])

interface Banner {
  imageUrl: string
  title: string
}

const getBanner = async () => {
  try {
    const res = await getBannerService()
    banners.value = res.data
  } catch (error) {
    ElMessage.error('获取轮播图失败')
  }
}

onMounted(() => {
  getBanner()
})
</script>

<style scoped>
/* 覆盖轮播图默认样式 */
.home-banner .el-carousel-item {
  position: relative;
  min-width: 350px;
  max-width: 400px;
}

/* 修改图片样式 */
.home-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 添加标题样式 */
.home-banner h3 {
  position: absolute;
  bottom: 20px; /* 距离底部20px */
  left: 0;
  right: 0;
  text-align: center;
  margin: 0;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.6); /* 半透黑底，保证文字可读 */
  color: #fff; /* 白色文字（后续可换主题色） */
  font-size: 1.2rem;
  font-weight: bold;
  backdrop-filter: blur(4px);
  border-radius: 8px;
  width: 90%;
  margin: 0 auto;
  left: 0;
  right: 0;
  bottom: 20px;
}

/* 调整标题和下方商品的间距 */
.home-banner h3 {
  margin-top: 0 !important;
  margin-bottom: 16px; /* 这里可以改成你想要的标题和下方商品的间距 */
}
</style>
