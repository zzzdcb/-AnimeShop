// 更新购物车
import { useCartStore } from '@/stores'
import { ElMessage } from 'element-plus'

export interface CartProductInfo {
  productId: number
  quantity: number
  name: string
  animeName: string
  characterName: string
  price: number
  image: string
}

export const updateCart = async (data: CartProductInfo) => {
  try {
    const cartStore = useCartStore()
    await cartStore.addToCart({
      productId: data.productId,
      quantity: data.quantity,
      name: data.name,
      animeName: data.animeName,
      characterName: data.characterName,
      price: data.price,
      image: data.image
    })
    return true
  } catch (error) {
    ElMessage.error('更新购物车失败，请稍后再试')
    throw error
  }
}
