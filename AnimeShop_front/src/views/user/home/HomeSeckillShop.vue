<template>
  <HomePanal title="秒杀商品" subTitle="限时抢购">
    <!-- 倒计时（有数据时才显示） -->
    <div class="seckill-header" v-if="seckillData.products.length > 0">
      <div class="countdown">
        <span class="countdown-label">距结束还剩</span>
        <div class="countdown-time">
          <span class="time-box">{{ countdown.hours }}</span>
          <span class="time-colon">:</span>
          <span class="time-box">{{ countdown.minutes }}</span>
          <span class="time-colon">:</span>
          <span class="time-box">{{ countdown.seconds }}</span>
        </div>
      </div>
    </div>

    <!-- 有数据时显示商品列表 -->
    <ul class="seckill-list" v-if="seckillData.products.length > 0">
      <li v-for="item in seckillData.products" :key="item.id">
        <img :src="item.mainImage || '/placeholder.jpg'" :alt="item.name" />
        <div class="info">
          <p class="name">{{ item.name }}</p>
          <div class="price-wrapper">
            <span class="seckill-price">¥{{ item.seckillPrice }}</span>
            <span class="original-price">¥{{ item.originalPrice }}</span>
          </div>
          <div class="stock">剩余库存：{{ item.stock }}件</div>
          <button class="buy-btn">立即抢购</button>
        </div>
      </li>
    </ul>

    <!-- 无数据时显示空状态 -->
    <div v-else class="empty-state">
      <div class="empty-content">
        <div class="empty-icon">🎁</div>
        <p class="empty-text">暂无秒杀活动</p>
        <p class="empty-hint">稍后再来看看吧～</p>
      </div>
    </div>
  </HomePanal>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getSeckillShopService } from '@/api/home.ts'
import HomePanal from './HomePanal.vue'

// 秒杀商品接口
interface SeckillProduct {
  id: number
  name: string
  seckillPrice: number
  originalPrice: number
  stock: number
  mainImage?: string
}

// 秒杀数据接口
interface SeckillData {
  remainingSeconds: number
  products: SeckillProduct[]
}

const seckillData = ref<SeckillData>({
  remainingSeconds: 0,
  products: []
})

// 倒计时显示（响应式）
const remainingSeconds = ref(0)
let timer: number | null = null

// 格式化倒计时
const countdown = computed(() => {
  const hours = Math.floor(remainingSeconds.value / 3600)
  const minutes = Math.floor((remainingSeconds.value % 3600) / 60)
  const seconds = remainingSeconds.value % 60

  return {
    hours: String(hours).padStart(2, '0'),
    minutes: String(minutes).padStart(2, '0'),
    seconds: String(seconds).padStart(2, '0')
  }
})

// 启动倒计时
const startCountdown = (seconds: number) => {
  remainingSeconds.value = seconds

  if (timer) clearInterval(timer)

  timer = window.setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      if (timer) clearInterval(timer)
      fetchSeckill()
    }
  }, 1000)
}

// 获取秒杀数据
const fetchSeckill = async () => {
  try {
    const res = await getSeckillShopService()
    seckillData.value = res.data
    if (res.data.products.length > 0) {
      startCountdown(res.data.remainingSeconds)
    }
  } catch (error) {
    console.error('获取秒杀数据失败:', error)
  }
}

onMounted(() => {
  fetchSeckill()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
/**
 * 秒杀商品样式
 */
.seckill-header {
  padding: 16px 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

/**
 * 倒计时样式
 */
.countdown {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
}

/**
 * 倒计时数字样式
 */
.countdown-label {
  font-size: 14px;
  color: #666;
}

/**
 * 倒计时数字样式
 */
.countdown-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

/**
 * 倒计时数字样式
 */
.time-box {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  font-size: 20px;
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 6px;
  min-width: 40px;
  text-align: center;
}

/**
 * 倒计时冒号样式
 */
.time-colon {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
}

/**
 * 商品列表样式
 */
.seckill-list {
  display: flex;
  gap: 20px;
  padding: 20px 24px;
  list-style: none;
  margin: 0;
}

/**
 * 商品列表项样式
 */
.seckill-list li {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  cursor: pointer;

  /**
   * 鼠标悬停样式
   */
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  }
}

/**
 * 商品图片样式
 */
.seckill-list li img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

/**
 * 商品信息样式
 */
.seckill-list li .info {
  padding: 12px;
  text-align: center;
}

/**
 * 商品名称样式
 */
.seckill-list li .info .name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/**
 * 商品价格样式
 */
.price-wrapper {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

/**
 * 商品秒杀价格样式
 */
.seckill-price {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

/**
 * 商品原价样式
 */
.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

/**
 * 商品库存样式
 */
.stock {
  font-size: 12px;
  color: #ff9800;
  margin-bottom: 12px;
}

/**
 * 购买按钮样式
 */
.buy-btn {
  width: 100%;
  padding: 8px 0;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: #6cb4ee;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;

  /**
   * 鼠标按下样式
   */
  &:hover {
    opacity: 0.9;
  }

  /**
   * 鼠标按下样式
   */
  &:active {
    transform: scale(0.98);
  }
}

// 空状态样式
.empty-state {
  padding: 60px 24px;
  text-align: center;
}

/**
 * 空状态内容样式
 */
.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

/**
 * 空状态图标样式
 */
.empty-icon {
  font-size: 64px;
  animation: bounce 2s ease-in-out infinite;
}

/**
 * 空状态文本样式
 */
.empty-text {
  font-size: 18px;
  font-weight: 500;
  color: #666;
  margin: 0;
}

/**
 * 空状态提示样式
 */
.empty-hint {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/**
 * 弹跳动画样式
 */
@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// 响应式
@media (max-width: 768px) {
  .seckill-list {
    flex-wrap: wrap;
  }

  .seckill-list li {
    min-width: calc(50% - 10px);
  }
}
</style>
