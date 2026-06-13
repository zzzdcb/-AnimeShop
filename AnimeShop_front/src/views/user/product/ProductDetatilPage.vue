<template>
  <div class="product-detail-page">
    <!-- 装饰元素 -->
    <div class="anime-decor">
      <span class="decor-star">✨</span>
      <span class="decor-star">⭐</span>
      <span class="decor-star">🌟</span>
    </div>

    <div class="detail-container" v-loading="loading">
      <!-- 商品信息区域 -->
      <div class="product-main" v-if="product">
        <!-- 左侧图片区域 -->
        <div class="product-gallery">
          <div class="main-image">
            <el-image :src="currentImage" fit="cover" class="main-img">
              <template #error>
                <div class="image-placeholder">
                  <span class="placeholder-emoji">🎀</span>
                  <span>商品图片</span>
                </div>
              </template>
            </el-image>
            <span v-if="product.isLimited" class="badge limited">✨ 限定版</span>
          </div>

          <!-- 缩略图列表 -->
          <div class="thumb-list" v-if="product.images && product.images.length > 0">
            <div
              v-for="(img, index) in [product.mainImage, ...product.images]"
              :key="index"
              class="thumb-item"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <el-image :src="img" fit="cover" />
            </div>
          </div>
        </div>

        <!-- 右侧信息区域 -->
        <div class="product-info">
          <!-- 分类标签 -->
          <div class="category-tag">
            <span class="tag">{{ product.categoryName }}</span>
          </div>

          <!-- 商品标题 -->
          <h1 class="product-title">
            {{ product.animeName }}
            <span class="character-name">- {{ product.characterName }}</span>
          </h1>
          <p class="product-name">{{ product.name }}</p>

          <!-- 价格区域 -->
          <div class="price-section">
            <div class="current-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ product.price }}</span>
            </div>
            <div class="original-price" v-if="product.originalPrice">
              <span>原价 ¥{{ product.originalPrice }}</span>
              <span class="discount">省 ¥{{ product.originalPrice - product.price }}</span>
            </div>
            <div class="sales-count">
              <span class="sales-icon">📈</span>
              已售 {{ product.sales }} 件
            </div>
          </div>

          <!-- 商品属性 -->
          <div class="attrs-section">
            <div class="attr-item">
              <span class="attr-label">📦 库存：</span>
              <span class="attr-value" :class="{ 'low-stock': product.stock < 20 }">
                {{ product.stock > 0 ? `${product.stock} 件` : '售罄' }}
              </span>
            </div>
          </div>

          <!-- 购买数量 -->
          <div class="quantity-section">
            <span class="quantity-label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              size="large"
              class="quantity-input"
            />
            <span class="stock-info" v-if="product.stock">库存 {{ product.stock }} 件</span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button class="cart-btn" size="large" @click="addToCart">
              <span class="btn-emoji">🛒</span>
              加入购物车
            </el-button>
            <el-button type="primary" class="buy-btn" size="large" @click="buyNow">
              <span class="btn-emoji">⚡</span>
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品详情描述 -->
      <div class="detail-section" v-if="product">
        <div class="section-tabs">
          <div
            class="tab-item"
            :class="{ active: activeTab === 'detail' }"
            @click="activeTab = 'detail'"
          >
            <span class="tab-emoji">📖</span>
            商品详情
          </div>
          <div
            class="tab-item"
            :class="{ active: activeTab === 'spec' }"
            @click="activeTab = 'spec'"
          >
            <span class="tab-emoji">📐</span>
            规格参数
          </div>
        </div>

        <div class="section-content" v-if="activeTab === 'detail'">
          <div class="description" v-html="product.description"></div>
        </div>

        <div class="section-content" v-if="activeTab === 'spec'">
          <div class="spec-list">
            <div class="spec-item">
              <span class="spec-label">作品名称：</span>
              <span class="spec-value">{{ product.animeName }}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">角色名称：</span>
              <span class="spec-value">{{ product.characterName }}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">商品名称：</span>
              <span class="spec-value">{{ product.name }}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">商品编号：</span>
              <span class="spec-value">{{ product.id }}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">分类：</span>
              <span class="spec-value">{{ product.categoryName }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐商品区域 -->
      <div class="recommend-section" v-if="recommendProducts.length > 0">
        <h3 class="section-title">
          <span class="title-emoji">🎁</span>
          猜你喜欢
          <span class="title-emoji">🎀</span>
        </h3>
        <div class="recommend-grid">
          <div
            v-for="item in recommendProducts"
            :key="item.id"
            class="recommend-card"
            @click="goToDetail(item.id)"
          >
            <div class="recommend-image">
              <el-image :src="item.mainImage" fit="cover" />
            </div>
            <div class="recommend-info">
              <h4 class="recommend-title">{{ item.animeName }} - {{ item.characterName }}</h4>
              <div class="recommend-price">¥{{ item.price }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 回到顶部按钮 -->
    <el-backtop :right="40" :bottom="40" />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { getProductDetailService } from '@/api/product'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updateCart } from '@/utils/updataCart'

const route = useRoute()
const router = useRouter()

// 商品数据类型
interface ProductDetail {
  id: number
  animeName: string
  characterName: string
  name: string
  categoryId: number
  categoryName: string
  price: number
  originalPrice: number
  description: string
  mainImage: string
  images: string[]
  isLimited: boolean
  stock: number
  sales: number
}

const product = ref<ProductDetail | null>(null)
const loading = ref(false)
const quantity = ref(1)
const activeTab = ref('detail')
const currentImage = ref('')
const recommendProducts = ref<any[]>([])

// 获取商品ID
const productId = Number(route.params.id)

/**
 * 获取商品详情
 */
const getProductDetail = async () => {
  if (!productId) return

  loading.value = true
  try {
    const res = await getProductDetailService(productId)
    console.log('商品详情:', res.data)

    product.value = res.data
    currentImage.value = product.value?.mainImage || ''
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加入购物车
 */
const addToCart = async () => {
  if (!product.value) return

  try {
    await updateCart({
      productId: product.value.id,
      quantity: quantity.value,
      name: product.value.name,
      animeName: product.value.animeName,
      characterName: product.value.characterName,
      price: product.value.price,
      image: product.value.mainImage
    })
    ElMessage.success(
      `✨ 已添加 ${product.value.animeName} - ${product.value.characterName} ×${quantity.value} 到购物车 ✨`
    )
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

/**
 * 立即购买
 */
const buyNow = () => {
  if (!product.value) return
  // TODO: 跳转到结算页面
  ElMessage.info('即将开放~')
}

/**
 * 跳转商品详情
 */
const goToDetail = (id: number) => {
  router.push(`/user/product/${id}`)
}

/**
 * 获取推荐商品（可选）
 */
const getRecommendProducts = async () => {
  // TODO: 调用推荐接口
  // 这里可以根据分类ID获取同分类的其他商品
}

onMounted(() => {
  getProductDetail()
  getRecommendProducts()
})
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e8f4ff 100%);
  padding: 40px 20px;
  position: relative;
}

/* 装饰元素 */
.anime-decor {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 0;
  pointer-events: none;
}

.decor-star {
  display: inline-block;
  font-size: 24px;
  margin: 0 4px;
  animation: floatStar 3s ease-in-out infinite;
  opacity: 0.3;
}

.decor-star:nth-child(2) {
  animation-delay: 1s;
}

.decor-star:nth-child(3) {
  animation-delay: 2s;
}

@keyframes floatStar {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(10deg);
  }
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 商品主区域 */
.product-main {
  display: flex;
  gap: 40px;
  background: white;
  border-radius: 24px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 24px rgba(108, 180, 238, 0.1);
  border: 1px solid rgba(108, 180, 238, 0.2);
}

/* 左侧图片区域 */
.product-gallery {
  flex: 1;
}

.main-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #f0f8ff, #e8f4ff);
  margin-bottom: 16px;
}

.main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 14px;
  border-radius: 40px;
  font-size: 13px;
  font-weight: bold;
  color: white;
  z-index: 2;
}

.badge.limited {
  background: linear-gradient(135deg, #ff9f4a, #ff8a2a);
  box-shadow: 0 2px 8px rgba(255, 159, 74, 0.3);
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #7f8c8d;
}

.placeholder-emoji {
  font-size: 48px;
  margin-bottom: 8px;
}

/* 缩略图 */
.thumb-list {
  display: flex;
  gap: 12px;
}

.thumb-item {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.thumb-item.active {
  border-color: #6cb4ee;
  box-shadow: 0 0 0 2px rgba(108, 180, 238, 0.2);
}

.thumb-item:hover {
  transform: translateY(-2px);
}

/* 右侧信息区域 */
.product-info {
  flex: 1;
}

.category-tag {
  margin-bottom: 12px;
}

.tag {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #6cb4ee20, #6cb4ee10);
  color: #6cb4ee;
  border-radius: 40px;
  font-size: 12px;
  font-weight: 500;
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.character-name {
  font-size: 20px;
  color: #7f8c8d;
  font-weight: 400;
}

.product-name {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0 0 16px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8eef2;
}

/* 价格区域 */
.price-section {
  background: #fff8f0;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 24px;
}

.current-price {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
}

.price-symbol {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
}

.price-value {
  font-size: 36px;
  font-weight: bold;
  color: #ff6b6b;
}

.original-price {
  margin-top: 8px;
  font-size: 14px;
  color: #7f8c8d;
  text-decoration: line-through;
}

.discount {
  display: inline-block;
  margin-left: 12px;
  color: #ff6b6b;
  text-decoration: none;
  font-weight: 500;
}

.sales-count {
  margin-top: 8px;
  font-size: 13px;
  color: #7f8c8d;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 属性区域 */
.attrs-section {
  background: #f8fafc;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 24px;
}

.attr-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.attr-item:last-child {
  margin-bottom: 0;
}

.attr-label {
  width: 80px;
  color: #7f8c8d;
  font-size: 14px;
}

.attr-value {
  color: #2c3e50;
  font-size: 14px;
  font-weight: 500;
}

.attr-value.low-stock {
  color: #ff6b6b;
}

/* 数量区域 */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
}

.quantity-label {
  color: #2c3e50;
  font-weight: 500;
}

.quantity-input {
  width: 120px;
}

.stock-info {
  color: #7f8c8d;
  font-size: 13px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.cart-btn {
  flex: 1;
  background: white;
  border: 2px solid #6cb4ee;
  color: #6cb4ee;
  border-radius: 48px;
  font-weight: 600;
  transition: all 0.3s;
}

.cart-btn:hover {
  background: #6cb4ee10;
  transform: translateY(-2px);
}

.buy-btn {
  flex: 1;
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
  border-radius: 48px;
  font-weight: 600;
  transition: all 0.3s;
}

.buy-btn:hover {
  background: linear-gradient(135deg, #5aa0d8, #4a8ab8);
  transform: translateY(-2px);
}

.btn-emoji {
  margin-right: 8px;
}

/* 详情区域 */
.detail-section {
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(108, 180, 238, 0.1);
  border: 1px solid rgba(108, 180, 238, 0.2);
  margin-bottom: 30px;
}

.section-tabs {
  display: flex;
  border-bottom: 2px solid #e8eef2;
  background: #f8fafc;
}

.tab-item {
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 500;
  color: #7f8c8d;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.tab-item:hover {
  color: #6cb4ee;
}

.tab-item.active {
  color: #6cb4ee;
  background: white;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #6cb4ee;
}

.tab-emoji {
  margin-right: 8px;
}

.section-content {
  padding: 30px;
}

.description {
  line-height: 1.8;
  color: #2c3e50;
}

.description :deep(p) {
  margin-bottom: 16px;
}

/* 规格参数 */
.spec-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.spec-item {
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
}

.spec-label {
  color: #7f8c8d;
  font-size: 14px;
  margin-right: 12px;
}

.spec-value {
  color: #2c3e50;
  font-weight: 500;
}

/* 推荐商品 */
.recommend-section {
  background: white;
  border-radius: 24px;
  padding: 30px;
  box-shadow: 0 8px 24px rgba(108, 180, 238, 0.1);
  border: 1px solid rgba(108, 180, 238, 0.2);
}

.section-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-emoji {
  font-size: 28px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.recommend-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e8eef2;
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(108, 180, 238, 0.15);
  border-color: #6cb4ee;
}

.recommend-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
}

.recommend-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.recommend-card:hover .recommend-image img {
  transform: scale(1.05);
}

.recommend-info {
  padding: 12px;
}

.recommend-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-price {
  font-size: 16px;
  font-weight: bold;
  color: #ff6b6b;
}

/* 响应式 */
@media (max-width: 768px) {
  .product-main {
    flex-direction: column;
    padding: 20px;
  }

  .spec-list {
    grid-template-columns: 1fr;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .product-title {
    font-size: 20px;
  }

  .price-value {
    font-size: 28px;
  }
}
</style>
