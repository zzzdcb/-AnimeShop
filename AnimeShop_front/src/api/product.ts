import request from '@/utils/request.ts'

/**
 * 商品列表参数
 */
interface Product {
  pageNo: number
  pageSize: number
  categoryId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
  sortBy?: string
  isLimited?: boolean
}

/**
 * 获取商品列表
 * @param params
 * @returns
 */
export const getProductService = (params: Product) => {
  return request.get(`/products`, { params })
}

/**
 * 获取商品详情
 * @param productId
 * @returns
 */
export const getProductDetailService = (productId: number) => {
  return request.get(`/products/${productId}`)
}
