import { createRouter, createWebHistory } from 'vue-router'

/**
 * 管理员路由
 */
const adminRouter = {
  path: '/admin',
  name: 'admin',
  component: () => import('@/layouts/EmptyLayout.vue'), // 空布局，防止发生重叠效果
  meta: {
    layout: 'admin'
  }
}

/**
 * 用户路由
 */
const userRouter = {
  path: '/user',
  name: 'user',
  component: () => import('@/layouts/EmptyLayout.vue'), // 空布局，防止发生重叠效果
  meta: {
    layout: 'user'
  },
  children: [
    {
      path: '',
      name: 'userHome',
      component: () => import('@/views/user/UserHomePage.vue')
    },
    {
      path: 'category/:id',
      name: 'userCategory',
      component: () => import('@/views/user/product/ProductByCategoryPage.vue')
    },
    {
      path: 'product/:id',
      name: 'userProduct',
      component: () => import('@/views/user/product/ProductDetatilPage.vue')
    },
    {
      path: 'cart',
      name: 'userCart',
      component: () => import('@/views/user/CartPage.vue')
    },
    {
      path: 'order',
      name: 'userOrder',
      component: () => import('@/views/user/OrderPage.vue')
    },
    {
      path: 'address',
      name: 'userAddress',
      component: () => import('@/views/user/AddressPage.vue')
    },
    {
      path: 'rePassword',
      name: 'userRepassword',
      component: () => import('@/views/user/RepasswordPage.vue')
    }
  ]
}

/**
 * 总路由
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/components/LoginPage.vue'),
      meta: {
        layout: 'none'
      }
    },
    adminRouter,
    userRouter
  ]
})

export default router
