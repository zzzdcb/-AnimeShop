import request from '@/utils/request.ts'

// 购物车商品类型
export interface CartProduct {
  productId: number
  name: string
  animeName: string
  characterName: string
  price: number
  quantity: number
  image: string
}

// API 响应类型
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 购物车列表响应
interface CartListData {
  items: CartProduct[]
}

// 添加/更新购物车
export const updateCartService = (data: { productId?: number; id?: number; quantity: number }) => {
  // 优先使用 id，如果没有则使用 productId
  const payload = {
    id: data.id ?? data.productId,
    quantity: data.quantity
  }
  return request.post('/cart/update', payload)
}

// 获取购物车列表
export const getCartService = () => {
  return request.get<ApiResponse<CartListData>>('/cart/list')
}

// 删除购物车商品（使用 GET 请求，参数放在 URL 中）
export const deleteCartService = (productIds: number[]) => {
  return request.delete(`/cart/remove?productIds=${productIds.join(',')}`)
}
