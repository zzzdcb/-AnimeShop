import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

/**
 * 订单项 DTO（对应后端 OrderItemDTO）
 */
export interface OrderItemDTO {
  productId: number
  quantity: number
}

/**
 * 创建订单参数（对应后端 OrderDTO）
 */
export interface PostOrders {
  items: OrderItemDTO[] // 商品 + 数量
  addressId: number // 地址ID
  remark: string // 备注
}

/**
 * 获取订单列表参数
 */
export interface GetOrders {
  page: number
  size?: number
  status?: number
}

// 创建订单
export const addOrderService = (data: PostOrders): Promise<ApiResponse<any>> => {
  return request.post('/orders', data) as Promise<ApiResponse<any>>
}

// 获取订单列表
export const getOrderService = (params: GetOrders): Promise<ApiResponse<any>> => {
  return request.get('/orders', { params }) as Promise<ApiResponse<any>>
}

// 取消订单
export const cancelOrderService = (orderNo: string | number): Promise<ApiResponse<any>> => {
  return request.delete(`/orders/${orderNo}`) as Promise<ApiResponse<any>>
}

// 确认收货
export const confirmOrderService = (orderNo: string | number): Promise<ApiResponse<any>> => {
  return request.post(`/orders/${orderNo}/confirm`) as Promise<ApiResponse<any>>
}
