<template>
  <div class="cart-page">
    <!-- 装饰元素 -->
    <div class="anime-decor">
      <span class="decor-star">✨</span>
      <span class="decor-star">⭐</span>
      <span class="decor-star">🌟</span>
      <span class="decor-star">💫</span>
    </div>

    <!-- 页面标题 -->
    <div class="cart-header">
      <h1 class="cart-title">
        <span class="title-emoji">🛒</span>
        我的购物车
        <span class="title-emoji">🎀</span>
      </h1>
      <p class="cart-subtitle" v-if="items.length > 0">
        共有
        <span class="count">{{ items.length }}</span>
        件商品
      </p>
    </div>

    <!-- 空购物车 -->
    <div v-if="items.length === 0" class="empty-cart">
      <div class="empty-anime">
        <span class="empty-emoji">😿</span>
        <span class="empty-emoji">🛒</span>
        <span class="empty-emoji">💔</span>
        <h3>购物车还是空的~</h3>
        <p>快去挑选喜欢的二次元商品吧！</p>
        <el-button type="primary" class="go-shop-btn" @click="goToCategory">
          <span class="btn-emoji">🌸</span>
          去逛逛
        </el-button>
      </div>
    </div>

    <!-- 购物车有商品 -->
    <div v-else class="cart-content">
      <!-- 商品列表 -->
      <div class="cart-items">
        <!-- 表头 -->
        <div class="cart-header-row">
          <el-checkbox :model-value="allChecked" @change="handleToggleAll" class="header-checkbox">
            全选
          </el-checkbox>
          <span class="col-product">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-quantity">数量</span>
          <span class="col-total">小计</span>
          <span class="col-action">操作</span>
        </div>

        <!-- 商品行 -->
        <div v-for="item in items" :key="item.productId" class="cart-item">
          <el-checkbox
            :model-value="item.checked"
            @change="() => handleToggleChecked(item.productId)"
            class="item-checkbox"
          />

          <div class="product-info" @click="goToDetail(item.productId)">
            <div class="product-img-wrapper">
              <img :src="item.image" :alt="item.name" class="product-img" />
              <div class="img-hover-effect"></div>
            </div>
            <div class="product-detail">
              <h4 class="product-anime">{{ item.animeName }}</h4>
              <p class="product-character">{{ item.characterName }}</p>
              <p class="product-name">{{ item.name }}</p>
            </div>
          </div>

          <div class="product-price">¥{{ item.price }}</div>

          <div class="product-quantity">
            <el-input-number
              :model-value="item.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="(val: number) => handleUpdateQuantity(item.productId, val)"
              class="quantity-input"
            />
          </div>

          <div class="product-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>

          <div class="product-action">
            <el-button link class="delete-btn" @click="handleRemove(item.productId)">
              <span class="delete-emoji">🗑️</span>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox :model-value="allChecked" @change="handleToggleAll" class="footer-checkbox">
            全选
          </el-checkbox>
          <el-button link class="clear-btn" @click="handleClear">
            <span class="clear-emoji">🧹</span>
            清空购物车
          </el-button>
        </div>

        <div class="footer-right">
          <div class="total-info">
            <span class="total-label">合计：</span>
            <span class="total-price">¥{{ selectedTotalPrice.toFixed(2) }}</span>
          </div>
          <div class="total-count">
            已选
            <span class="count-num">{{ selectedTotalQuantity }}</span>
            件商品
          </div>
          <el-button type="primary" class="checkout-btn" @click="handleCheckout">
            <span class="btn-emoji">⚡</span>
            去结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const { items, selectedTotalPrice, selectedTotalQuantity } = storeToRefs(cartStore)

// 全选状态
const allChecked = computed(
  () => items.value.length > 0 && items.value.every((item) => item.checked)
)

// 切换选中
const handleToggleChecked = (productId: number) => {
  cartStore.toggleChecked(productId)
}

// 全选/取消全选
const handleToggleAll = (checked: boolean) => {
  cartStore.toggleAllChecked(checked)
}

// 更新数量
const handleUpdateQuantity = async (productId: number, quantity: number) => {
  await cartStore.updateQuantity(productId, quantity)
}

// 删除
const handleRemove = async (productId: number) => {
  await cartStore.removeItem(productId)
  ElMessage.success('✨ 已删除 ✨')
}

// 清空购物车
const handleClear = async () => {
  if (items.value.length === 0) return
  await cartStore.clearCart()
  ElMessage.success('🧹 购物车已清空')
}

// 去结算
const handleCheckout = () => {
  const selectedItems = items.value.filter((i) => i.checked)
  if (selectedItems.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }

  // 将选中商品完整信息写入 sessionStorage，供订单页读取
  const checkoutItems = selectedItems.map((item) => ({
    productId: item.productId,
    quantity: item.quantity,
    price: item.price,
    name: item.name,
    image: item.image
  }))
  sessionStorage.setItem('checkoutItems', JSON.stringify(checkoutItems))

  // 将选中的商品ID拼接成字符串，传递给订单页面
  const productIds = selectedItems.map((item) => item.productId).join(',')
  console.log('结算商品:', selectedItems)

  // 跳转到订单页面，并传递商品ID
  router.push({
    path: '/user/order',
    query: { ids: productIds }
  })
}

// 去逛逛
const goToCategory = () => {
  router.push('/user')
}

// 跳转详情
const goToDetail = (id: number) => {
  router.push(`/user/product/${id}`)
}

// 页面加载时刷新购物车
onMounted(async () => {
  if (cartStore.isLogin) {
    await cartStore.fetchCart()
  }
})
</script>

<style scoped>
/* 样式保持不变，和之前一样 */
.cart-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e8f4ff 100%);
  padding: 40px 20px;
  position: relative;
}

/* 装饰元素 */
.anime-decor {
  position: fixed;
  top: 100px;
  left: 20px;
  z-index: 0;
  pointer-events: none;
}

.decor-star {
  display: inline-block;
  font-size: 20px;
  margin: 0 4px;
  animation: floatStar 3s ease-in-out infinite;
  opacity: 0.3;
}

.decor-star:nth-child(1) {
  animation-delay: 0s;
}
.decor-star:nth-child(2) {
  animation-delay: 0.8s;
}
.decor-star:nth-child(3) {
  animation-delay: 1.6s;
}
.decor-star:nth-child(4) {
  animation-delay: 2.4s;
}

@keyframes floatStar {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-10px) rotate(10deg);
    opacity: 0.6;
  }
}

/* 页面标题 */
.cart-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.cart-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.title-emoji {
  font-size: 36px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.cart-subtitle {
  font-size: 14px;
  color: #7f8c8d;
}

.cart-subtitle .count {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 16px;
}

/* 空购物车 */
.empty-cart {
  max-width: 500px;
  margin: 80px auto;
  background: white;
  border-radius: 32px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(108, 180, 238, 0.1);
  border: 1px solid rgba(108, 180, 238, 0.2);
}

.empty-anime {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-emoji {
  font-size: 64px;
  display: inline-block;
  margin: 0 8px;
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
  font-size: 24px;
  color: #2c3e50;
  margin: 16px 0 8px;
}

.empty-anime p {
  color: #7f8c8d;
  margin-bottom: 24px;
}

.go-shop-btn {
  border-radius: 40px;
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  border: none;
  padding: 12px 32px;
  font-weight: 600;
}

.btn-emoji {
  margin-right: 8px;
}

/* 购物车内容 */
.cart-content {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(108, 180, 238, 0.1);
  border: 1px solid rgba(108, 180, 238, 0.2);
}

/* 表头 */
.cart-header-row {
  display: grid;
  grid-template-columns: 80px 1fr 120px 120px 120px 100px;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8fafc, #f0f5fa);
  border-bottom: 2px solid #e8eef2;
  font-weight: 600;
  color: #2c3e50;
  gap: 12px;
}

.header-checkbox {
  justify-self: center;
}

.col-product {
  text-align: left;
}
.col-price {
  text-align: center;
}
.col-quantity {
  text-align: center;
}
.col-total {
  text-align: center;
}
.col-action {
  text-align: center;
}

/* 商品行 */
.cart-item {
  display: grid;
  grid-template-columns: 80px 1fr 120px 120px 120px 100px;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e8eef2;
  transition: all 0.3s ease;
  gap: 12px;
  background: white;
}

.cart-item:hover {
  background: linear-gradient(135deg, #6cb4ee05, #6cb4ee02);
  transform: translateX(4px);
}

.item-checkbox {
  justify-self: center;
}

/* 商品信息 */
.product-info {
  display: flex;
  gap: 16px;
  cursor: pointer;
  align-items: center;
}

.product-img-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  background: #f0f8ff;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-info:hover .product-img {
  transform: scale(1.05);
}

.img-hover-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(108, 180, 238, 0.2), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.product-info:hover .img-hover-effect {
  opacity: 1;
}

.product-detail {
  flex: 1;
}

.product-anime {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.product-character {
  font-size: 13px;
  color: #7f8c8d;
  margin: 0 0 4px 0;
}

.product-name {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* 价格 */
.product-price,
.product-total {
  text-align: center;
  font-weight: 600;
  color: #ff6b6b;
}

.product-price {
  font-size: 16px;
}

.product-total {
  font-size: 16px;
}

/* 数量 */
.product-quantity {
  display: flex;
  justify-content: center;
}

.quantity-input :deep(.el-input__wrapper) {
  border-radius: 40px;
  border-color: #e8eef2;
  box-shadow: none;
}

.quantity-input :deep(.el-input__wrapper:hover) {
  border-color: #6cb4ee;
}

/* 删除按钮 */
.product-action {
  text-align: center;
}

.delete-btn {
  color: #999;
  font-size: 13px;
  transition: all 0.2s;
}

.delete-btn:hover {
  color: #ff6b6b;
  transform: scale(1.05);
}

.delete-emoji {
  margin-right: 4px;
}

/* 底部结算栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-top: 2px solid #e8eef2;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.clear-btn {
  color: #999;
  font-size: 14px;
}

.clear-btn:hover {
  color: #ff6b6b;
}

.clear-emoji {
  margin-right: 4px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 16px;
  color: #2c3e50;
}

.total-price {
  font-size: 28px;
  font-weight: bold;
  color: #ff6b6b;
}

.total-count {
  font-size: 14px;
  color: #7f8c8d;
}

.count-num {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 16px;
}

.checkout-btn {
  border-radius: 40px;
  background: linear-gradient(135deg, #ff9f4a, #ff8a2a);
  border: none;
  padding: 12px 28px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s;
}

.checkout-btn:hover {
  background: linear-gradient(135deg, #ff8a2a, #ff6b2a);
  transform: scale(1.02);
}

.btn-emoji {
  margin-right: 8px;
}

/* 复选框样式 */
:deep(.el-checkbox__inner) {
  border-radius: 6px;
  border-color: #e8eef2;
  transition: all 0.2s;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #6cb4ee;
  border-color: #6cb4ee;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: white;
}

/* 响应式 */
@media (max-width: 900px) {
  .cart-header-row,
  .cart-item {
    grid-template-columns: 60px 1fr 100px 100px 100px 80px;
    gap: 8px;
  }

  .product-name {
    display: none;
  }

  .product-img-wrapper {
    width: 60px;
    height: 60px;
  }
}

@media (max-width: 768px) {
  .cart-page {
    padding: 20px 12px;
  }

  .cart-header-row {
    display: none;
  }

  .cart-item {
    grid-template-columns: 40px 1fr 80px;
    gap: 12px;
    position: relative;
    padding: 16px;
  }

  .product-price {
    grid-row: 2;
    grid-column: 2;
    text-align: left;
  }

  .product-quantity {
    grid-row: 2;
    grid-column: 3;
  }

  .product-total {
    grid-row: 3;
    grid-column: 2;
    text-align: left;
  }

  .product-action {
    grid-row: 3;
    grid-column: 3;
  }

  .cart-footer {
    flex-direction: column;
    gap: 16px;
  }

  .footer-right {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
