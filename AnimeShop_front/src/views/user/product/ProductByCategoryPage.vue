<template>
  <div class="category-page">
    <div class="category-container">
      <!-- 筛选侧边栏 -->
      <aside class="filter-sidebar">
        <!-- 二次元装饰头 -->
        <div class="sidebar-header">
          <span class="decor-emoji">🌸</span>
          <span class="decor-text">筛选条件</span>
          <span class="decor-emoji">⭐</span>
        </div>

        <!-- 价格区间 -->
        <div class="filter-group">
          <h3 class="filter-title">
            <span class="title-icon">💰</span>
            价格区间
          </h3>
          <div class="price-range">
            <el-input
              v-model="minPrice"
              placeholder="最低价"
              type="number"
              size="large"
              class="price-input"
            />
            <span class="separator">~</span>
            <el-input
              v-model="maxPrice"
              placeholder="最高价"
              type="number"
              size="large"
              class="price-input"
            />
            <el-button type="primary" size="large" class="price-confirm" @click="handlePriceFilter">
              🔍 确定
            </el-button>
          </div>
        </div>

        <!-- 商品属性 -->
        <div class="filter-group">
          <h3 class="filter-title">
            <span class="title-icon">🎁</span>
            商品属性
          </h3>
          <div class="filter-options">
            <el-checkbox v-model="isLimited" class="custom-checkbox">
              <span class="checkbox-text">✨ 限定版</span>
            </el-checkbox>
            <el-checkbox v-model="isPreorder" class="custom-checkbox">
              <span class="checkbox-text">📦 预售商品</span>
            </el-checkbox>
            <el-checkbox v-model="inStock" class="custom-checkbox">
              <span class="checkbox-text">✅ 仅看有货</span>
            </el-checkbox>
          </div>
        </div>

        <!-- 排序方式 -->
        <div class="filter-group">
          <h3 class="filter-title">
            <span class="title-icon">📊</span>
            排序方式
          </h3>
          <div class="sort-options">
            <el-radio-group v-model="sortType" class="custom-radio-group">
              <div class="sort-option">
                <el-radio value="default">
                  <span class="radio-text">✨ 默认排序</span>
                </el-radio>
              </div>
              <div class="sort-option">
                <el-radio value="sales">
                  <span class="radio-text">🔥 按销量</span>
                </el-radio>
              </div>
              <div class="sort-option">
                <el-radio value="price_asc">
                  <span class="radio-text">⬆️ 价格从低到高</span>
                </el-radio>
              </div>
              <div class="sort-option">
                <el-radio value="price_desc">
                  <span class="radio-text">⬇️ 价格从高到低</span>
                </el-radio>
              </div>
              <div class="sort-option">
                <el-radio value="newest">
                  <span class="radio-text">🆕 最新上架</span>
                </el-radio>
              </div>
            </el-radio-group>
          </div>
        </div>

        <!-- 重置按钮 -->
        <div class="filter-actions">
          <el-button class="reset-btn" @click="resetFilters">🧹 重置筛选</el-button>
          <el-button type="primary" class="apply-btn" @click="applyFilters">✨ 应用筛选</el-button>
        </div>

        <!-- 二次元装饰脚 -->
        <div class="sidebar-footer">
          <span class="footer-text">₊˚✧ ﾟ✧ ٩(ˊωˋ*)و ✧ﾟ✧˚₊</span>
        </div>
      </aside>

      <!-- 商品网格区 -->
      <main class="products-main">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <div class="anime-loading">
            <span class="loading-emoji">🎴</span>
            <span class="loading-emoji">✨</span>
            <span class="loading-emoji">⭐</span>
            <p>加载中...</p>
          </div>
        </div>

        <!-- 商品网格 -->
        <div v-else-if="products.length > 0" class="products-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goToDetail(product.id)"
          >
            <div class="product-image">
              <el-image :src="product.image || '/placeholder-image.jpg'" fit="cover" class="image">
                <template #error>
                  <div class="image-placeholder">
                    <span class="placeholder-emoji">🎀</span>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
              <span v-if="product.isLimited" class="limited-badge">✨ 限定</span>
              <span v-if="product.isPreorder" class="preorder-badge">📦 预售</span>
              <!-- 悬停装饰 -->
              <div class="card-hover-glow"></div>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.animeName || product.name }}</h3>
              <p class="product-character">
                <span class="character-emoji">👤</span>
                {{ product.characterName }}
              </p>
              <div class="product-price">
                <span class="current-price">¥{{ product.price }}</span>
                <span v-if="product.originalPrice" class="original-price">
                  ¥{{ product.originalPrice }}
                </span>
              </div>
              <div class="product-footer">
                <span class="sales-count">
                  <span class="sales-emoji">📈</span>
                  销量 {{ product.salesCount || 0 }}
                </span>
                <el-button class="cart-btn" size="small" @click.stop="addToCart(product)">
                  <span class="cart-emoji">🛒</span>
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-anime">
            <span class="empty-emoji">😿</span>
            <span class="empty-emoji">💔</span>
            <span class="empty-emoji">🎴</span>
            <h3>暂无商品</h3>
            <p>试试调整筛选条件吧~</p>
            <el-button class="reset-empty-btn" @click="resetFilters">🧹 重置筛选</el-button>
          </div>
        </div>

        <!-- 分页组件 -->
        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="pageNo"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 48]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="anime-pagination"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { getProductService } from '@/api/product'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updateCart } from '@/utils/updataCart'

const route = useRoute()
const router = useRouter()

// ========== 类型定义 ==========

// 商品类型
interface ProductItem {
  id: number
  animeName?: string
  name?: string
  characterName: string
  price: number
  originalPrice?: number
  image?: string
  salesCount?: number
  isLimited?: boolean
  isPreorder?: boolean
  stock?: number
}

// ========== 响应式数据 ==========

// 获取分类id - 改为响应式
const categoryId = ref<number>(0)

// 商品列表
const products = ref<ProductItem[]>([])

// 分页
const pageNo = ref<number>(1)
const pageSize = ref<number>(12)
const total = ref<number>(0)
const loading = ref<boolean>(false)

// 筛选参数
const minPrice = ref<number | null>(null)
const maxPrice = ref<number | null>(null)
const isLimited = ref<boolean>(false)
const isPreorder = ref<boolean>(false)
const inStock = ref<boolean>(false)
const sortType = ref<string>('default')

// ========== 方法 ==========

/**
 * 获取商品列表
 */
const getProducts = async (): Promise<void> => {
  if (!categoryId.value) return

  loading.value = true

  const params: any = {
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    categoryId: categoryId.value
  }

  if (minPrice.value !== null) params.minPrice = minPrice.value
  if (maxPrice.value !== null) params.maxPrice = maxPrice.value
  if (isLimited.value) params.isLimited = true
  if (isPreorder.value) params.isPreorder = true
  if (inStock.value) params.inStock = true
  if (sortType.value !== 'default') params.sortBy = sortType.value

  try {
    const response = await getProductService(params)

    products.value = response.data?.list || []
    total.value = response.data?.total || 0
  } catch (error) {
    ElMessage.error('获取商品失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 价格筛选
 */
const handlePriceFilter = (): void => {
  pageNo.value = 1
  getProducts()
}

/**
 * 应用筛选
 */
const applyFilters = (): void => {
  pageNo.value = 1
  getProducts()
}

/**
 * 重置筛选
 */
const resetFilters = (): void => {
  minPrice.value = null
  maxPrice.value = null
  isLimited.value = false
  isPreorder.value = false
  inStock.value = false
  sortType.value = 'default'
  pageNo.value = 1
  getProducts()
}

/**
 * 分页大小改变
 */
const handleSizeChange = (size: number): void => {
  pageSize.value = size
  pageNo.value = 1
  getProducts()
}

/**
 * 当前页改变
 */
const handleCurrentChange = (page: number): void => {
  pageNo.value = page
  getProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 跳转商品详情
 */
const goToDetail = (id: number): void => {
  router.push(`/user/product/${id}`)
}

/**
 * 加入购物车
 */
const addToCart = async (product: ProductItem): Promise<void> => {
  try {
    await updateCart({
      productId: product.id,
      quantity: 1,
      name: product.name || '',
      animeName: product.animeName || '',
      characterName: product.characterName || '',
      price: product.price,
      image: (product as any).mainImage || (product as any).image || ''
    })
    ElMessage.success(`✨ 已添加 ${product.animeName || product.name} 到购物车 ✨`)
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

/**
 * 重置分页并刷新（分类切换时使用）
 */
const refreshProducts = (): void => {
  pageNo.value = 1
  getProducts()
}

// ========== 路由监听 ==========

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      const id = Number(newId)
      if (!isNaN(id) && id !== categoryId.value) {
        categoryId.value = id
        refreshProducts()
      }
    }
  },
  { immediate: true } // 改为 true，确保首次加载也触发
)

// 监听整个路由
watch(
  () => route.fullPath,
  () => {
    const id = Number(route.params.id)
    if (!isNaN(id) && id !== categoryId.value) {
      categoryId.value = id
      refreshProducts()
    }
  }
)
</script>

<style scoped>
/* ========== 全局配色 ========== */
.category-page {
  padding: 20px;
  background-color: #f0f8ff;
  min-height: 100vh;
  position: relative;
}

/* 二次元装饰：漂浮星星 */
.category-page::before {
  content: '✨';
  position: fixed;
  font-size: 180px;
  opacity: 0.04;
  bottom: 20px;
  right: 20px;
  pointer-events: none;
  z-index: 0;
  animation: floatStar 8s ease-in-out infinite;
}

.category-page::after {
  content: '⭐';
  position: fixed;
  font-size: 120px;
  opacity: 0.03;
  top: 100px;
  left: 30px;
  pointer-events: none;
  z-index: 0;
  animation: floatStar 6s ease-in-out infinite reverse;
}

@keyframes floatStar {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}

.category-container {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* ========== 筛选侧边栏 ========== */
.filter-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 20px;
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 20px;
  border: 1px solid #e8eef2;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(108, 180, 238, 0.08);
}

.filter-sidebar:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(108, 180, 238, 0.12);
}

/* 侧边栏头部装饰 */
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px dotted #e8eef2;
}

.decor-emoji {
  font-size: 20px;
  animation: bounce 2s ease-in-out infinite;
}

.decor-emoji:first-child {
  animation-delay: 0s;
}

.decor-emoji:last-child {
  animation-delay: 1s;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

.decor-text {
  font-size: 16px;
  font-weight: 600;
  color: #6cb4ee;
  letter-spacing: 2px;
}

.filter-group {
  margin-bottom: 28px;
}

.filter-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 14px 0;
  padding-bottom: 8px;
  border-bottom: 2px dotted #e8eef2;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  font-size: 18px;
}

/* 价格区间 */
.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.price-range .price-input {
  flex: 1;
  min-width: 80px;
}

.price-range .price-input :deep(.el-input__wrapper) {
  border-radius: 40px;
  border-color: #e8eef2;
  box-shadow: none;
  transition: all 0.3s;
}

.price-range .price-input :deep(.el-input__wrapper:hover) {
  border-color: #6cb4ee;
}

.price-range .price-input :deep(.el-input__wrapper.is-focus) {
  border-color: #6cb4ee;
  box-shadow: 0 0 0 2px rgba(108, 180, 238, 0.2);
}

.price-range .separator {
  color: #7f8c8d;
  font-size: 14px;
}

.price-confirm {
  width: 100%;
  margin-top: 12px;
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
  border-radius: 40px;
  transition: all 0.3s;
}

.price-confirm:hover {
  background: linear-gradient(135deg, #5aa0d8, #4a8ab8);
  transform: scale(1.02);
}

/* 复选框样式 */
.filter-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.custom-checkbox {
  margin: 0;
}

.custom-checkbox :deep(.el-checkbox__inner) {
  border-radius: 6px;
  border-color: #e8eef2;
  transition: all 0.2s;
}

.custom-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #6cb4ee;
  border-color: #6cb4ee;
}

.custom-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: white;
}

.checkbox-text {
  color: #2c3e50;
  font-size: 14px;
  transition: color 0.2s;
}

.custom-checkbox:hover :deep(.checkbox-text) {
  color: #6cb4ee;
}

/* 单选框样式 */
.sort-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sort-option {
  margin: 0;
}

.custom-radio-group :deep(.el-radio__inner) {
  border-color: #e8eef2;
  transition: all 0.2s;
}

.custom-radio-group :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #6cb4ee;
  border-color: #6cb4ee;
}

.radio-text {
  color: #2c3e50;
  font-size: 13px;
  transition: color 0.2s;
}

.custom-radio-group :deep(.el-radio:hover .radio-text) {
  color: #6cb4ee;
}

/* 筛选按钮 */
.filter-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 2px dotted #e8eef2;
}

.filter-actions .el-button {
  flex: 1;
  border-radius: 40px;
  font-weight: 500;
  transition: all 0.3s;
}

.reset-btn {
  background: white;
  border: 1px solid #e8eef2;
  color: #7f8c8d;
}

.reset-btn:hover {
  border-color: #6cb4ee;
  color: #6cb4ee;
  transform: translateY(-1px);
}

.apply-btn {
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
}

.apply-btn:hover {
  background: linear-gradient(135deg, #5aa0d8, #4a8ab8);
  transform: translateY(-1px);
}

/* 侧边栏底部装饰 */
.sidebar-footer {
  margin-top: 20px;
  padding-top: 12px;
  text-align: center;
  border-top: 2px dotted #e8eef2;
}

.footer-text {
  font-size: 11px;
  color: #7f8c8d;
  letter-spacing: 1px;
}

/* ========== 商品主区域 ========== */
.products-main {
  flex: 1;
  min-width: 0;
}

/* 加载动画 */
.loading-container {
  background: #ffffff;
  border-radius: 20px;
  padding: 60px 20px;
  text-align: center;
  border: 1px solid #e8eef2;
}

.anime-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-emoji {
  font-size: 40px;
  display: inline-block;
  animation: loadingBounce 0.6s ease-in-out infinite;
  margin: 0 6px;
}

.loading-emoji:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-emoji:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes loadingBounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-12px);
  }
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

/* 商品卡片 */
.product-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e8eef2;
  position: relative;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(108, 180, 238, 0.15);
  border-color: #6cb4ee;
}

.product-image {
  position: relative;
  height: 240px;
  overflow: hidden;
  background: linear-gradient(135deg, #f0f8ff, #e8f4ff);
}

.product-image .image {
  width: 100%;
  height: 100%;
  display: block;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image .image {
  transform: scale(1.08);
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #7f8c8d;
  background: linear-gradient(135deg, #f0f8ff, #e8f4ff);
}

.placeholder-emoji {
  font-size: 48px;
  margin-bottom: 8px;
  opacity: 0.6;
}

/* 标签样式 */
.limited-badge,
.preorder-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 5px 12px;
  border-radius: 40px;
  font-size: 12px;
  font-weight: bold;
  color: white;
  z-index: 2;
  backdrop-filter: blur(2px);
}

.limited-badge {
  background: linear-gradient(135deg, #ff9f4a, #ff8a2a);
  box-shadow: 0 2px 8px rgba(255, 159, 74, 0.3);
}

.preorder-badge {
  background: linear-gradient(135deg, #ff4757, #ff2e40);
  right: 80px;
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

/* 卡片悬停光效 */
.card-hover-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 50% 0%, rgba(108, 180, 238, 0.15), transparent);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.product-card:hover .card-hover-glow {
  opacity: 1;
}

/* 商品信息 */
.product-info {
  padding: 14px;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-character {
  font-size: 13px;
  color: #7f8c8d;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.character-emoji {
  font-size: 12px;
}

.product-price {
  margin-bottom: 12px;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
}

.current-price::before {
  content: '¥';
  font-size: 14px;
}

.original-price {
  font-size: 13px;
  color: #7f8c8d;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sales-count {
  font-size: 12px;
  color: #7f8c8d;
  display: flex;
  align-items: center;
  gap: 4px;
}

.sales-emoji {
  font-size: 12px;
}

.cart-btn {
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
  border-radius: 40px;
  padding: 6px 14px;
  transition: all 0.3s;
  color: white;
  font-weight: 500;
}

.cart-btn:hover {
  background: linear-gradient(135deg, #5aa0d8, #4a8ab8);
  transform: scale(1.05);
}

.cart-emoji {
  margin-right: 4px;
}

/* 空状态 */
.empty-state {
  background: #ffffff;
  border-radius: 24px;
  padding: 60px 20px;
  text-align: center;
  border: 1px solid #e8eef2;
}

.empty-anime {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-emoji {
  font-size: 56px;
  display: inline-block;
  margin: 0 6px;
  animation: shake 0.5s ease-in-out;
}

.empty-emoji:nth-child(2) {
  animation-delay: 0.1s;
}

.empty-emoji:nth-child(3) {
  animation-delay: 0.2s;
}

@keyframes shake {
  0%,
  100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-15deg);
  }
  75% {
    transform: rotate(15deg);
  }
}

.empty-anime h3 {
  font-size: 22px;
  color: #2c3e50;
  margin: 8px 0 4px;
}

.empty-anime p {
  color: #7f8c8d;
  margin-bottom: 20px;
}

.reset-empty-btn {
  border-radius: 40px;
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
  color: white;
  padding: 10px 28px;
}

.reset-empty-btn:hover {
  background: linear-gradient(135deg, #5aa0d8, #4a8ab8);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
  background: #ffffff;
  border-radius: 40px;
  border: 1px solid #e8eef2;
}

.anime-pagination :deep(.el-pagination__total) {
  color: #6cb4ee;
  font-weight: 500;
}

.anime-pagination :deep(.btn-prev),
.anime-pagination :deep(.btn-next) {
  border-radius: 40px !important;
  margin: 0 4px;
}

.anime-pagination :deep(.el-pager li) {
  border-radius: 40px !important;
  margin: 0 4px;
  background: white;
  transition: all 0.3s;
}

.anime-pagination :deep(.el-pager li:hover) {
  color: #6cb4ee;
}

.anime-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  color: white;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .category-container {
    flex-direction: column;
  }

  .filter-sidebar {
    width: 100%;
    position: static;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }

  .product-image {
    height: 180px;
  }

  .product-title {
    font-size: 14px;
  }

  .current-price {
    font-size: 16px;
  }

  .cart-btn span:not(.cart-emoji) {
    display: none;
  }
}
</style>
