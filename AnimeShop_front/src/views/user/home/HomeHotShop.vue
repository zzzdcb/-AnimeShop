<template>
  <HomePanal title="热门商品" subTitle="热门推荐">
    <ul class="hot-list">
      <li v-for="hot in hotList" :key="hot.id">
        <img :src="hot.mainImage" alt="" />
        <div class="info">
          <p class="animeName">{{ hot.animeName }}</p>
          <p class="name">{{ hot.name }}</p>
          <p class="price">{{ hot.price }}</p>
        </div>
      </li>
    </ul>
  </HomePanal>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHotShopService } from '@/api/home.ts'
import HomePanal from './HomePanal.vue'

interface HotShop {
  id: number
  name: string
  mainImage: string
  animeName: string
  title: string
  price: number
}
const hotList = ref<HotShop[]>([])

const fetchHotShop = async () => {
  try {
    const res = await getHotShopService()
    hotList.value = res.data
  } catch (error) {
    console.error('获取热销商品数据失败:', error)
  }
}

onMounted(() => {
  fetchHotShop()
})
</script>

<style scoped lang="scss">
.hot-list {
  display: flex;
  justify-content: space-between;
  padding: 16px;
  gap: 12px;
}

.hot-list li {
  flex: 1;
  display: flex;
  flex-direction: column;
  text-align: center;
}

.hot-list li img {
  width: 100%;
  height: 350px;
  object-fit: cover;
  border-radius: 8px;
}

.hot-list li .info {
  width: 100%;
  margin-top: 8px;
  align-items: center;
}

.hot-list li .info .animeName {
  font-size: 12px;
  color: #999; /* 稍微淡一点，区分层级 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.hot-list li .info .name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-list li .info .price {
  font-size: 16px; /* 价格稍微大一点 */
  font-weight: 600;
  color: #e74c3c;
}
</style>
