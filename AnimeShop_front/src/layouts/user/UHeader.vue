<template>
  <el-affix :offset="0">
    <div class="header">
      <el-row :gutter="20" class="header-row">
        <!-- Logo区域 -->
        <el-col :span="6">
          <div class="logo">
            <img :src="logoUrl" alt="用户页面logo" />
            <span class="logo-text">二次元网站用户端</span>
            <span class="logo-decor">✨</span>
          </div>
        </el-col>

        <!-- 导航菜单 -->
        <el-col :span="12">
          <u-nav />
        </el-col>

        <!-- 右侧用户区域 -->
        <el-col :span="6">
          <div class="user-actions">
            <!-- 📍 地址管理图标 -->
            <div class="address-icon" @click="goToAddress">
              <el-icon :size="22">
                <Location />
              </el-icon>
            </div>

            <!-- 🛒 购物车图标 -->
            <el-badge :value="cartCount" :hidden="cartCount === 0" :max="99" class="cart-badge">
              <div class="cart-icon" @click="goToCart">
                <el-icon :size="24">
                  <ShoppingCart />
                </el-icon>
              </div>
            </el-badge>

            <!-- 用户信息下拉菜单 -->
            <el-dropdown @command="handleCommand" class="user-dropdown">
              <span class="el-dropdown-link">
                <el-avatar :size="40" :src="userStore.userInfo?.avatar" class="user-avatar" />
                <span class="username">{{ userStore.userInfo?.username || '游客' }}</span>
                <el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="address">
                    <span class="dropdown-emoji">📍</span>
                    地址管理
                  </el-dropdown-item>
                  <el-dropdown-item command="rePassword">
                    <span class="dropdown-emoji">🔐</span>
                    修改密码
                  </el-dropdown-item>
                  <el-dropdown-item command="order">
                    <span class="dropdown-emoji">📦</span>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <span class="dropdown-emoji">🚪</span>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-col>
      </el-row>

      <!-- 装饰元素 -->
      <div class="header-decor">
        <span class="decor-star">⭐</span>
        <span class="decor-star">🌟</span>
        <span class="decor-star">✨</span>
      </div>
    </div>
  </el-affix>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, ArrowDown, Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/index'
import { useCartStore } from '@/stores'
import { userLogoutService } from '@/api/user'
import { showModule } from '@/utils/ElFunction'
import UNav from './UNav.vue'
import userLogo from '@/assets/images/userLogo.png'

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()
const logoUrl = userLogo

// 购物车商品数量
const cartCount = computed(() => cartStore.totalQuantity)

/**
 * 跳转购物车页面
 */
const goToCart = () => {
  router.push('/user/cart')
}

/**
 * 跳转地址管理页面
 */
const goToAddress = () => {
  router.push('/user/address')
}

/**
 * 用户信息菜单项被点击
 */
const handleCommand = async (command: string) => {
  switch (command) {
    case 'logout':
      await logout()
      break
    case 'rePassword':
      router.push('/user/repassword')
      break
    case 'order':
      router.push('/user/order')
      break
    case 'address':
      router.push('/user/address')
      break
  }
}

/**
 * 退出登录
 */
const logout = () => {
  showModule('确定要退出登录吗？', '退出登录', 'warning').then(async () => {
    await userLogoutService()
    userStore.logout()
    cartStore.logout()
    router.push('/login')
  })
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #f0f8ff 0%, #e8f4ff 100%);
  height: 60px;
  position: relative;
  box-shadow: 0 2px 12px rgba(108, 180, 238, 0.15);
  border-bottom: 2px solid rgba(108, 180, 238, 0.2);
}

.header-row {
  height: 60px;
  display: flex;
  align-items: center;
}

/* ========== Logo 区域 ========== */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 60px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: translateY(-2px);
}

.logo img {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(108, 180, 238, 0.2);
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #6cb4ee, #4a8ab8);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: 1px;
}

.logo-decor {
  font-size: 20px;
  animation: bounce 2s ease-in-out infinite;
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

/* ========== 右侧用户区域 ========== */
.user-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 15px;
  height: 60px;
}

/* 📍 地址图标 */
.address-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  color: #6cb4ee;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(108, 180, 238, 0.15);
}

.address-icon:hover {
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(108, 180, 238, 0.3);
}

/* 🛒 购物车图标 */
.cart-badge {
  cursor: pointer;
}

.cart-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  color: #6cb4ee;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(108, 180, 238, 0.15);
}

.cart-icon:hover {
  background: linear-gradient(135deg, #6cb4ee, #5aa0d8);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(108, 180, 238, 0.3);
}

/* 角标样式覆盖 */
.cart-badge :deep(.el-badge__content) {
  background: linear-gradient(135deg, #ff6b6b, #ff4757);
  border: 2px solid white;
  font-size: 12px;
  height: 18px;
  line-height: 14px;
  padding: 0 6px;
}

/* 用户下拉菜单 */
.user-dropdown {
  cursor: pointer;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 40px;
  background: white;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(108, 180, 238, 0.1);
}

.el-dropdown-link:hover {
  background: linear-gradient(135deg, #6cb4ee10, #6cb4ee20);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 180, 238, 0.2);
}

.user-avatar {
  border: 2px solid #6cb4ee;
  transition: transform 0.3s;
}

.el-dropdown-link:hover .user-avatar {
  transform: scale(1.05);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 下拉菜单样式 */
.dropdown-emoji {
  margin-right: 8px;
  font-size: 14px;
}

:deep(.el-dropdown-menu__item) {
  padding: 10px 20px;
  transition: all 0.2s;
}

:deep(.el-dropdown-menu__item:hover) {
  background: linear-gradient(135deg, #6cb4ee10, #6cb4ee20);
  color: #6cb4ee;
}

/* ========== 头部装饰元素 ========== */
.header-decor {
  position: absolute;
  bottom: -5px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 20px;
  pointer-events: none;
}

.decor-star {
  font-size: 12px;
  opacity: 0.6;
  animation: floatStar 3s ease-in-out infinite;
}

.decor-star:nth-child(1) {
  animation-delay: 0s;
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
    opacity: 0.6;
  }
  50% {
    transform: translateY(-5px) rotate(10deg);
    opacity: 1;
  }
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .logo-text {
    display: none;
  }

  .username {
    display: none;
  }

  .address-icon,
  .cart-icon {
    width: 36px;
    height: 36px;
  }

  .el-dropdown-link {
    padding: 4px 8px;
  }
}
</style>
