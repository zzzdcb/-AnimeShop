import request from '@/utils/request.ts'

/**
 * 用户信息
 */
interface UserInfo {
  username: string
  password: string
}

/**
 * 用户登录
 * @param data
 * @returns
 */
export const userLoginService = (data: UserInfo) => {
  return request.post('/user/login', data)
}

/**
 * 用户登出
 * @returns
 */
export const userLogoutService = () => {
  return request.post('/user/logout')
}
