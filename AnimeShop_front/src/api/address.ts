import request from '@/utils/request.ts'
import type { ApiResponse } from '@/utils/request.ts'

/**
 * 地址
 */
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

/**
 * 获取地址列表
 */
export const getAddressService = (): Promise<ApiResponse<Address[]>> => {
  return request.get('/address') as Promise<ApiResponse<Address[]>>
}

/**
 * 添加地址
 * @param data
 */
export const addAddressService = (data: Address): Promise<ApiResponse<any>> => {
  return request.post('/address', data) as Promise<ApiResponse<any>>
}

/**
 * 删除地址
 * @param addressId
 */
export const deleteAddressService = (addressId: number): Promise<ApiResponse<any>> => {
  return request.delete(`/address/${addressId}`) as Promise<ApiResponse<any>>
}

/**
 * 设置默认地址
 * @param addressId
 */
export const putAddressService = (addressId: number): Promise<ApiResponse<any>> => {
  return request.put(`/address/${addressId}/default`) as Promise<ApiResponse<any>>
}
