import request from '@/utils/request.ts'

/**
 * 获取分类列表
 */
export const getCategoryService = () => {
  return request.get('/categories')
}
