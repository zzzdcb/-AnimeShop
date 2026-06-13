// stores/user.ts
import { userLoginService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 定义用户信息类型
interface UserInfo {
  id: number
  username: string
  email: string
  avatar?: string
  nikename?: string
}

export const useUserStore = defineStore(
  'user',
  () => {
    // state
    const token = ref<string>('') // 存储用户登录状态的token，初始值为空字符串
    const userInfo = ref<UserInfo | null>(null) // 存储用户信息，初始值为null，表示未登录状态

    // getters
    const isLoggedIn = computed(() => !!token.value) // 判断是否有token来确定登录状态
    const userName = computed(() => userInfo.value?.username || '游客') // 如果没有用户信息，显示“游客”

    //获取用户信息
    const getUserInfo = async (username: string, password: string) => {
      try {
        const res = await userLoginService({ username, password })
        setToken(res.data.token)
        setUserInfo({
          id: res.data.id,
          username: res.data.username,
          email: res.data.email,
          avatar: res.data.avatar,
          nikename: res.data.nikename
        })
      } catch (error) {
        console.log(error)
      }
    }

    // actions
    const setToken = (newToken: string) => {
      token.value = newToken //更新token值
    }

    const setUserInfo = (info: UserInfo) => {
      userInfo.value = info //更新用户信息值
    }

    const logout = () => {
      // 登出时清空token和用户信息
      token.value = ''
      userInfo.value = null
    }

    return {
      token,
      userInfo,
      getUserInfo,
      isLoggedIn,
      userName,
      setToken,
      setUserInfo,
      logout
    }
  },
  {
    persist: {
      key: 'user-store', // 存储在localStorage中的键名
      storage: localStorage, // 使用localStorage进行持久化存储
      pick: ['token', 'userInfo'] // ✅ 正确
    }
  }
)
