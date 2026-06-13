import request from '@/utils/request'

// 获取首页轮播图数据
export const getBannerService = () => {
  return request.get('/home/banner')
}

// 获取首页热销商品数据
export const getHotShopService = () => {
  return request.get('/home/hotProduct')
}

// 获取首页新品数据
export const getNewShopService = () => {
  return request.get('/home/newProduct')
}

// 获取首页秒杀商品数据
export const getSeckillShopService = () => {
  return request.get('/home/seckillProduct')
}
