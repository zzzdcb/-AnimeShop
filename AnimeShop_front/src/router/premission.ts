import router from '.'
import { useUserStore } from '@/stores/index.ts'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

//全局前置守卫
router.beforeEach((to, from, next) => {
  NProgress.start()

  // 👇 直接读 localStorage 看看
  const rawToken = localStorage.getItem('user-token')
  console.log('直接从 localStorage 读:', rawToken ? '有' : '无')

  const userStore = useUserStore() // 获取用户信息
  const token = userStore.token // 获取用户的 token

  //没有token，说明用户未登录，跳转到登录页
  if (!token && to.path !== '/login') {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  //防止重复登录
  if (to.path === '/login' && token) {
    ElMessage.warning('请勿重复登录')
    next(from.path ? from.path : '/') // 跳转到来源页,如果没有来源页则跳转到首页
    return
  }

  next()
})

//全局后置守卫
router.afterEach(() => {
  NProgress.done()
})
