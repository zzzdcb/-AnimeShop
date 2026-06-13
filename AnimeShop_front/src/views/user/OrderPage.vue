<template>
  <div class="order-page">
    <div class="page-header">
      <h1 class="page-title">
        <span class="title-emoji">📦</span>
        我的订单
        <span class="title-emoji">🎀</span>
      </h1>
    </div>

    <!-- 创建订单区域 -->
    <div v-if="orderItems.length > 0" class="create-order-section">
      <div class="create-order-card">
        <div class="create-order-header">
          <span class="create-icon">🛒</span>
          <span class="create-title">确认订单</span>
        </div>
        <div class="create-order-body">
          <div class="selected-products">
            <p class="product-count">
              已选择
              <span>{{ orderItems.length }}</span>
              件商品
            </p>
            <p class="product-ids">
              商品信息：
              <span v-for="item in orderItems" :key="item.productId">
                ID:{{ item.productId }} ×{{ item.quantity }}
              </span>
            </p>
          </div>
          <div class="address-section">
            <label class="address-label">选择收货地址:</label>
            <el-select v-model="selectedAddressId" class="address-select">
              <el-option
                v-for="addr in addresses"
                :key="addr.id"
                :label="`${addr.receiver} ${addr.phone} ${addr.address}`"
                :value="addr.id"
              />
            </el-select>
          </div>
          <div class="remark-section">
            <label class="remark-label">订单备注:</label>
            <el-input
              v-model="remark"
              placeholder="选填，如有特殊要求请备注"
              class="remark-input"
            />
          </div>
        </div>
        <div class="create-order-footer">
          <el-button
            type="primary"
            size="large"
            :loading="creatingOrder"
            :disabled="!selectedAddressId"
            @click="handleCreateOrder"
            class="create-order-btn"
          >
            <span class="btn-emoji">✨</span>
            {{ creatingOrder ? '创建中...' : '创建订单' }}
          </el-button>
          <el-button size="large" @click="clearProductIds" class="cancel-create-btn">
            取消
          </el-button>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="order-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="待付款" name="0"></el-tab-pane>
        <el-tab-pane label="待发货" name="1"></el-tab-pane>
        <el-tab-pane label="待收货" name="2"></el-tab-pane>
        <el-tab-pane label="已完成" name="3"></el-tab-pane>
        <el-tab-pane label="已取消" name="4"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="anime-loading">
        <span class="loading-emoji">📦</span>
        <span class="loading-emoji">✨</span>
        <span>加载中...</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-else-if="orders.length > 0">
      <div v-for="order in orders" :key="order.orderNo" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <div class="order-status" :class="getStatusClass(order.status)">
            {{ order.statusText || getStatusText(order.status) }}
          </div>
        </div>

        <!-- 订单商品列表 -->
        <div class="order-items">
          <div
            v-for="item in order.orderItemList"
            :key="item.productId"
            class="order-item"
            @click="goToProduct(item.productId)"
          >
            <div class="product-img-wrapper">
              <img :src="item.image" :alt="item.productName" class="product-img" />
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ item.productName }}</h4>
              <p class="product-spec">{{ item.spec || '' }}</p>
            </div>
            <div class="product-price">¥{{ item.price }}</div>
            <div class="product-quantity">x{{ item.quantity }}</div>
            <div class="product-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <div class="order-total">
            共
            <span class="count">{{ order.orderItemList?.length || 0 }}</span>
            件商品 合计：
            <span class="price">¥{{ order.payAmount?.toFixed(2) || 0 }}</span>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 0 || order.status === 1"
              type="danger"
              size="small"
              @click="handleCancel(order.orderNo)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 0"
              type="primary"
              size="small"
              @click="handlePay(order.orderNo)"
            >
              立即付款
            </el-button>
            <el-button
              v-if="order.status === 2"
              type="primary"
              size="small"
              @click="handleConfirm(order.orderNo)"
            >
              确认收货
            </el-button>
            <el-button
              v-if="order.status === 3"
              type="info"
              size="small"
              @click="handleReview(order.orderNo)"
            >
              评价
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-order">
      <div class="empty-anime">
        <span class="empty-emoji">😿</span>
        <span class="empty-emoji">📦</span>
        <span class="empty-emoji">💔</span>
        <h3>还没有订单</h3>
        <p>快去挑选喜欢的商品吧~</p>
        <el-button type="primary" class="go-shop-btn" @click="goToCategory">
          <span class="btn-emoji">🌸</span>
          去逛逛
        </el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderService,
  cancelOrderService,
  confirmOrderService,
  addOrderService
} from '@/api/order'
import { getAddressService } from '@/api/address'
import { useCartStore } from '@/stores'

// 订单商品项
interface OrderItem {
  productId: number
  productName: string
  price: number
  quantity: number
  image?: string
  spec?: string
}

// 订单信息
interface Order {
  orderNo: string
  orderId: string
  status: number
  statusText?: string
  payAmount: number
  createTime: string
  orderItemList: OrderItem[]
}

// 订单商品（创建订单用）
interface OrderItemParam {
  productId: number
  quantity: number
}

// 购物车商品（从 sessionStorage 读取）
interface CheckoutItem {
  productId: number
  quantity: number
  price?: number
  name?: string
  image?: string
}

interface Address {
  id?: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  address: string
  isDefault: number // 0 或 1，表示是否为默认地址
}

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// 请求序列号，用于防止竞态条件（只处理最新的请求响应）
let requestId = 0

// 状态映射（标签名 → 状态码）
const statusMap: Record<string, number | undefined> = {
  all: undefined,
  '0': 0,
  '1': 1,
  '2': 2,
  '3': 3,
  '4': 4
}

const activeTab = ref<string>('all')
const orders = ref<Order[]>([])
const loading = ref<boolean>(false)
const pageNo = ref<number>(1)
const pageSize = ref<number>(10)
const total = ref<number>(0)

// ========== 创建订单相关 ==========
const orderItems = ref<OrderItemParam[]>([])
const addresses = ref<Address[]>([])
const selectedAddressId = ref<number | undefined>()
const remark = ref<string>('')
const creatingOrder = ref<boolean>(false)

// 获取订单状态文本
const getStatusText = (status: number): string => {
  const map: Record<number, string> = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知状态'
}

// 获取订单状态样式
const getStatusClass = (status: number): string => {
  const map: Record<number, string> = {
    0: 'status-pending',
    1: 'status-shipped',
    2: 'status-delivered',
    3: 'status-completed',
    4: 'status-cancelled'
  }
  return map[status] || ''
}

// 获取订单列表
const fetchOrders = async (): Promise<void> => {
  const currentRequestId = ++requestId
  loading.value = true
  orders.value = [] // 清空旧数据，避免闪旧数据
  try {
    const params: { page: number; size: number; status?: number } = {
      page: pageNo.value,
      size: pageSize.value
    }

    const statusValue = statusMap[activeTab.value]
    if (statusValue !== undefined) {
      params.status = statusValue
    }

    const res = await getOrderService(params)
    console.log('订单列表:', res.data)

    // 如果已经不是最新的请求，丢弃这个过期响应
    if (currentRequestId !== requestId) return

    if (res?.code === 200 && res?.data) {
      orders.value = res.data.list || []
      total.value = res.data.total || 0
    } else if (res?.data?.list) {
      orders.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      orders.value = []
      total.value = 0
    }
  } catch (error) {
    // 请求被取消或出错，只有当前请求才处理
    if (currentRequestId === requestId) {
      console.error('获取订单失败:', error)
      ElMessage.error('获取订单失败')
      orders.value = []
    }
  } finally {
    if (currentRequestId === requestId) {
      loading.value = false
    }
  }
}

// 获取地址列表
const fetchAddresses = async (): Promise<void> => {
  try {
    const res = await getAddressService()

    if (Array.isArray(res.data)) {
      addresses.value = res.data
    } else if (res?.code === 200 && res?.data) {
      addresses.value = res.data
    } else {
      addresses.value = []
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

// 获取订单参数中的商品ID
const initOrderItems = (): void => {
  const idsParam = route.query.ids as string
  if (idsParam) {
    const productIds = idsParam.split(',').map(Number)
    const storedData = sessionStorage.getItem('checkoutItems')
    if (storedData) {
      const allItems = JSON.parse(storedData) as CheckoutItem[]
      orderItems.value = allItems
        .filter((item) => productIds.includes(item.productId))
        .map((item) => ({
          productId: item.productId,
          quantity: item.quantity
        }))
    } else {
      orderItems.value = productIds.map((id) => ({
        productId: id,
        quantity: 1
      }))
    }
  }
}

// 创建订单
const handleCreateOrder = async (): Promise<void> => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  creatingOrder.value = true
  try {
    const params = {
      addressId: selectedAddressId.value,
      items: orderItems.value.map((item) => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      remark: remark.value
    }

    const res = await addOrderService(params)
    console.log('创建订单响应:', res)

    if (res?.code === 200) {
      ElMessage.success('订单创建成功')
      sessionStorage.removeItem('checkoutItems')
      // 从购物车中移除已下单的商品
      const orderedIds = orderItems.value.map((item) => item.productId)
      await cartStore.removeItems(orderedIds)
      await fetchOrders()
      orderItems.value = []
      remark.value = ''
      selectedAddressId.value = undefined
    } else {
      ElMessage.error(res?.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  } finally {
    creatingOrder.value = false
  }
}

// 清除商品ID，隐藏创建订单区域
const clearProductIds = (): void => {
  orderItems.value = []
  remark.value = ''
  selectedAddressId.value = undefined
  router.replace({ query: {} })
}

// 监听标签切换 → 重新获取订单
watch(activeTab, () => {
  pageNo.value = 1
  fetchOrders()
})

// 分页
const handleSizeChange = (size: number): void => {
  pageSize.value = size
  pageNo.value = 1
  fetchOrders()
}

const handleCurrentChange = (page: number): void => {
  pageNo.value = page
  fetchOrders()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 取消订单
const handleCancel = (orderNo: string): void => {
  if (!orderNo) {
    ElMessage.error('订单ID无效')
    return
  }

  ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelOrderService(orderNo)
      ElMessage.success('取消成功')
      fetchOrders()
    } catch (error) {
      console.error('取消失败:', error)
      ElMessage.error('取消失败')
    }
  })
}

// 确认收货
const handleConfirm = (orderNo: string): void => {
  if (!orderNo) {
    ElMessage.error('订单号无效')
    return
  }

  ElMessageBox.confirm('确认已收到商品吗？', '提示', {
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await confirmOrderService(orderNo)
      ElMessage.success('确认收货成功')
      fetchOrders()
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  })
}

// 立即付款
const handlePay = (orderNo: string): void => {
  if (!orderNo) {
    ElMessage.error('订单号无效')
    return
  }
  console.log('待支付订单:', orderNo)
  ElMessage.info('支付功能开发中，请耐心等待~')
}

// 评价
const handleReview = (orderId: string): void => {
  router.push(`/order/${orderId}/review`)
}

// 跳转商品详情
const goToProduct = (productId: number): void => {
  router.push(`/user/product/${productId}`)
}

// 去逛逛
const goToCategory = (): void => {
  router.push('/user')
}

onMounted(() => {
  fetchOrders()
  fetchAddresses()
  initOrderItems()
})
</script>

<style scoped>
.order-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e8f4ff 100%);
  padding: 40px 20px;
}

/* 创建订单区域 */
.create-order-section {
  max-width: 800px;
  margin: 0 auto 30px;
}

.create-order-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 16px rgba(108, 180, 238, 0.15);
  overflow: hidden;
}

.create-order-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  color: white;
}

.create-icon {
  font-size: 24px;
}

.create-title {
  font-size: 18px;
  font-weight: 600;
}

.create-order-body {
  padding: 24px;
}

.selected-products {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.product-count {
  font-size: 14px;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.product-count span {
  color: #6cb4ee;
  font-weight: bold;
  font-size: 18px;
}

.product-ids {
  font-size: 13px;
  color: #7f8c8d;
  margin: 0;
}

.address-section,
.remark-section {
  margin-bottom: 20px;
}

.address-label,
.remark-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 8px;
}

.address-select {
  width: 100%;
}

.remark-input {
  width: 100%;
}

.create-order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 20px 24px;
  background: #f8fafc;
  border-top: 1px solid #e8eef2;
}

.create-order-btn {
  border-radius: 40px;
  background: linear-gradient(135deg, #ff9f4a, #ff8a2a);
  border: none;
  padding: 12px 32px;
  font-weight: 600;
}

.cancel-create-btn {
  border-radius: 40px;
  padding: 12px 24px;
}

.page-header {
  max-width: 1000px;
  margin: 0 auto 30px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-emoji {
  font-size: 32px;
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

/* 标签页 */
.order-tabs {
  max-width: 1000px;
  margin: 0 auto 20px;
  background: white;
  border-radius: 16px;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(108, 180, 238, 0.1);
}

:deep(.el-tabs__header) {
  margin: 0;
}

:deep(.el-tabs__active-bar) {
  background-color: #6cb4ee;
}

:deep(.el-tabs__item:hover) {
  color: #6cb4ee;
}

:deep(.el-tabs__item.is-active) {
  color: #6cb4ee;
}

/* 加载状态 */
.loading-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 24px;
  padding: 60px 20px;
  text-align: center;
}

.anime-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-emoji {
  font-size: 40px;
  animation: bounce 0.6s ease-in-out infinite;
}

/* 订单列表 */
.order-list {
  max-width: 1000px;
  margin: 0 auto;
}

.order-card {
  background: white;
  border-radius: 16px;
  margin-bottom: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(108, 180, 238, 0.1);
  transition: all 0.3s;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(108, 180, 238, 0.15);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e8eef2;
}

.order-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #7f8c8d;
}

.order-no {
  color: #2c3e50;
  font-weight: 500;
}

.order-status {
  font-size: 14px;
  font-weight: 600;
}

.status-pending {
  color: #ff9f4a;
}
.status-shipped {
  color: #6cb4ee;
}
.status-delivered {
  color: #ff6b6b;
}
.status-completed {
  color: #2ecc71;
}
.status-cancelled {
  color: #999;
}

/* 订单商品列表 */
.order-items {
  padding: 0 20px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #e8eef2;
  cursor: pointer;
  transition: background 0.2s;
}

.order-item:hover {
  background: #f0f8ff;
  margin: 0 -10px;
  padding: 15px 10px;
}

.product-img-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  background: #f0f8ff;
  margin-right: 15px;
  flex-shrink: 0;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  margin-right: 15px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px 0;
}

.product-spec {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.product-price,
.product-quantity,
.product-total {
  width: 100px;
  text-align: center;
  color: #ff6b6b;
  font-weight: 500;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8fafc;
  border-top: 1px solid #e8eef2;
}

.order-total {
  font-size: 14px;
  color: #2c3e50;
}

.order-total .count {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 16px;
  margin: 0 4px;
}

.order-total .price {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 18px;
  margin-left: 8px;
}

.order-actions {
  display: flex;
  gap: 12px;
}

.order-actions .el-button {
  border-radius: 40px;
  padding: 8px 20px;
}

/* 空状态 */
.empty-order {
  max-width: 500px;
  margin: 80px auto;
  background: white;
  border-radius: 32px;
  padding: 60px 40px;
  text-align: center;
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
}

.btn-emoji {
  margin-right: 8px;
}

/* 分页 */
.pagination-container {
  max-width: 1000px;
  margin: 30px auto 0;
  display: flex;
  justify-content: center;
  background: white;
  border-radius: 40px;
  padding: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .order-page {
    padding: 20px 12px;
  }

  .order-item {
    flex-wrap: wrap;
  }

  .product-info {
    width: 100%;
    margin-bottom: 10px;
  }

  .product-price,
  .product-quantity,
  .product-total {
    width: 33%;
    font-size: 14px;
  }

  .order-footer {
    flex-direction: column;
    gap: 15px;
  }

  .order-actions {
    width: 100%;
    justify-content: center;
  }
}
</style>
