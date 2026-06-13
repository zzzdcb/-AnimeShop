// stores/index.js
import { createPinia } from 'pinia'
import persist from 'pinia-plugin-persistedstate'

// 创建 Pinia 实例并注册持久化插件
const pinia = createPinia()
pinia.use(persist)

// 导出 Pinia 实例，供 main.js 安装
export default pinia

// 导出所有 store
export * from './module/userStore'
export * from './module/cartStores'
