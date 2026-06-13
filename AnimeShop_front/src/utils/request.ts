// request.ts - Axios 网络请求封装
// 作用：统一管理 HTTP 请求，包括请求拦截、响应拦截、错误处理、请求取消等功能

import axios from 'axios'
import type {
  AxiosInstance, //Axios 实例
  AxiosRequestConfig, //请求配置
  AxiosResponse, //响应数据
  InternalAxiosRequestConfig // 内部请求配置
} from 'axios'
import qs from 'qs' // 用于序列化查询参数（处理数组、对象等复杂参数）
import { ElMessage } from 'element-plus' // Element Plus 的消息提示组件
import { useUserStore } from '@/stores/module/userStore' // Pinia 用户状态管理

/* ================================
 * 类型定义
 * ================================ */

// 后端统一响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 请求配置扩展（添加取消标识）
interface CancelableRequestConfig extends AxiosRequestConfig {
  signal?: AbortSignal
}

/* ================================
 * 一、创建 Axios 实例
 * ================================ */

// 创建 Axios 实例（每个实例都有自己的配置和拦截器）
const service: AxiosInstance = axios.create({
  // 基础 URL：从环境变量读取，开发环境和生产环境自动切换
  // 需要在 .env.development 和 .env.production 中配置 VITE_API_BASE_URL
  baseURL: import.meta.env.VITE_API_BASE_URL as string,

  // 请求超时时间：10秒（超过这个时间还未响应，视为超时）
  timeout: 10000,

  // 默认请求头
  headers: {
    'Content-Type': 'application/json;charset=utf-8' // JSON 格式
  },

  // 参数序列化器：将参数对象转换成查询字符串
  // 例如：{ ids: [1, 2, 3] } → "ids[0]=1&ids[1]=2&ids[2]=3"
  paramsSerializer: (params: any) => qs.stringify(params, { arrayFormat: 'indices' }),
  // 跨域请求是否携带 cookie（false 表示不携带）
  withCredentials: false
})

/* ================================
 * 二、请求拦截器（在请求发送前执行）
 * ================================ */

/**
 * 请求拦截器的作用：
 * 1. 添加认证 Token（JWT）
 * 2. 开发环境打印请求日志
 * 3. 处理特殊请求格式
 */
service.interceptors.request.use(
  // 成功回调：修改请求配置
  (config: InternalAxiosRequestConfig) => {
    // 获取 Pinia store 中的 token
    const userStore = useUserStore()
    const token = userStore.token

    // 如果 token 存在，添加到请求头的 Authorization 字段
    // Bearer 是 JWT 的标准前缀，后端会验证这个 token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 为 API 请求添加统一前缀 /api
    // 这样可以避免与前端路由冲突
    if (config.url && !config.url.startsWith('/api')) {
      config.url = `/api${config.url}`
    }

    // 开发环境打印请求日志，方便调试
    if (import.meta.env.DEV) {
      console.log(`🚀 ${config.method?.toUpperCase()} ${config.url}`, {
        params: config.params, // URL 参数（GET 请求）
        data: config.data // 请求体数据（POST/PUT 请求）
      })
    }

    return config // 必须返回修改后的 config
  },
  // 失败回调：请求配置出错时触发
  (error: any) => {
    ElMessage.error('请求配置错误') // ✅ Element Plus 写法
    return Promise.reject(error) // 将错误传递给调用方
  }
)

/* ================================
 * 三、响应拦截器（在响应返回后执行）
 * ================================ */

/**
 * 响应拦截器的作用：
 * 1. 统一处理业务状态码
 * 2. 处理 401 未授权（登录过期）
 * 3. 提取真正的业务数据
 * 4. 开发环境打印响应日志
 */
service.interceptors.response.use(
  // 成功回调：HTTP 状态码为 2xx 时触发
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data // 提取响应体数据

    // 开发环境打印响应日志
    if (import.meta.env.DEV) {
      console.log(`✅ ${response.config.method?.toUpperCase()} ${response.config.url}`, res)
    }

    /**
     * 业务状态码判断（根据后端实际规范调整）
     * 常见规范：
     * - code === 200 或 0：请求成功
     * - code === 401：未授权（token 过期或无效）
     * - code === 403：无权限
     * - code === 404：资源不存在
     * - 其他：业务错误
     */
    if (res.code !== 200 && res.code !== 0) {
      // 401：登录过期，需要重新登录
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout() // 清空本地 token 和用户信息
        ElMessage.warning('登录已过期，请重新登录') // ✅ Element Plus 写法
        // 可选：跳转到登录页
        // router.push('/login')
      } else {
        // 其他业务错误，显示后端返回的错误消息
        ElMessage.error(res.message || '请求失败') // ✅ Element Plus 写法
      }
      // 将错误传递给调用方的 catch 块
      return Promise.reject(new Error(res.message))
    }

    /**
     * 返回业务数据（简化响应结构）
     * 原响应：{ code: 200, message: 'ok', data: { ... } }
     * 实际返回：{ ... }（直接返回 data 内容）
     * 优点：调用方不需要写 response.data.data
     */
    return res as any
  },
  // 失败回调：HTTP 状态码不是 2xx 时触发（如 404、500、网络错误等）
  (error: any) => {
    /**
     * 情况1：主动取消的请求
     * 使用 cancelRequest 或 requestWithCancel 取消的请求会进入这里
     * 这种情况不需要给用户提示，直接静默处理
     */
    if (axios.isCancel(error)) {
      console.log('请求已取消：', error.message)
      return Promise.reject(error)
    }

    /**
     * 情况2：服务器返回了错误状态码（4xx、5xx）
     * error.response 存在，说明服务器有响应
     */
    if (error.response) {
      const { status, data } = error.response as { status: number; data: ApiResponse }

      // 根据 HTTP 状态码显示不同的错误消息
      const errorMsg: string =
        {
          200: '请求成功', // 理论上不会走到这里
          400: '请求参数错误', // 前端传参有问题
          401: '登录已过期', // token 无效或过期
          403: '权限不足', // 没有访问权限
          404: '资源不存在', // 请求的接口或资源不存在
          500: '服务器错误' // 后端服务异常
        }[status] ||
        data?.message || // 后端自定义的错误消息
        `请求失败：${status}` // 兜底消息

      ElMessage.error(errorMsg) // ✅ Element Plus 写法

      // 401 状态码清空本地登录状态
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        // router.push('/login')
      }
    } else if (error.request) {
      /**
       * 情况3：请求已发出，但没有收到响应
       * error.request 存在，说明请求超时或网络断开
       */
      ElMessage.error('网络异常，请检查网络连接') // ✅ Element Plus 写法
    } else {
      /**
       * 情况4：请求配置错误
       * 例如：请求地址格式错误、参数类型错误等
       */
      ElMessage.error(error.message || '请求失败') // ✅ Element Plus 写法
    }

    // 打印错误详情（开发环境便于调试）
    if (import.meta.env.DEV) {
      console.error('请求错误详情：', error)
    }

    return Promise.reject(error)
  }
)

/* ================================
 * 四、请求取消功能（防抖/防重复请求）
 * ================================ */

/**
 * 背景：
 * - 用户快速点击按钮时，会发起多个相同请求
 * - 切换页面时，未完成的请求应该被取消
 * - 搜索框输入时，只需要最后一次请求的结果
 *
 * 实现原理：
 * - 使用 AbortController API（现代浏览器原生支持）
 * - 每个请求分配一个唯一标识（cancelKey）
 * - 新请求发起前，先取消同标识的旧请求
 */

// 存储所有请求的取消控制器（key: cancelKey, value: AbortController 实例）
const abortControllers: Map<string, AbortController> = new Map()

/**
 * 取消指定 key 的请求
 * @param {string} key - 请求的唯一标识
 */
const cancelRequest = (key: string): void => {
  if (abortControllers.has(key)) {
    // 调用 abort() 方法取消请求
    abortControllers.get(key)?.abort()
    // 从 Map 中删除
    abortControllers.delete(key)
  }
}

/**
 * 取消所有未完成的请求
 * 适用场景：用户退出登录、切换页面时调用
 */
const cancelAllRequests = (): void => {
  abortControllers.forEach((controller: AbortController) => controller.abort())
  abortControllers.clear()
}

/**
 * 发送可取消的请求（自动处理重复请求）
 * @param {Object} config - Axios 请求配置
 * @param {string} cancelKey - 请求唯一标识（相同标识的请求会被互斥）
 * @returns {Promise} 请求 Promise
 *
 * 使用示例：
 * requestWithCancel({ url: '/api/search', params: { keyword } }, 'search')
 */
const requestWithCancel = <T = any>(
  config: CancelableRequestConfig,
  cancelKey: string
): Promise<T> => {
  // 1. 取消之前同标识的未完成请求（防止重复请求）
  cancelRequest(cancelKey)

  // 2. 创建新的 AbortController
  const controller: AbortController = new AbortController()

  // 3. 存储起来，以便后续取消
  abortControllers.set(cancelKey, controller)

  // 4. 将取消信号绑定到请求配置
  config.signal = controller.signal

  // 5. 发送请求，请求完成后从 Map 中删除
  return service(config).finally(() => {
    abortControllers.delete(cancelKey)
  }) as Promise<T>
}

/* ================================
 * 五、导出
 * ================================ */

// 默认导出：普通的请求实例（不自动取消重复请求）
export default service

// 命名导出：取消相关的工具函数
export { cancelRequest, cancelAllRequests, requestWithCancel }
