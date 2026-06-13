// stores/cart.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { updateCartService, getCartService, deleteCartService } from '@/api/cart'
import { useUserStore } from './userStore'

// 定义购物车商品类型（匹配前端使用）
export interface CartItem {
  productId: number
  name: string
  animeName: string
  characterName: string
  price: number
  quantity: number
  image: string
  checked: boolean
}

export const useCartStore = defineStore('cart', () => {
  // 获取用户登录状态
  const userStore = useUserStore()
  const isLogin = computed(() => userStore.isLoggedIn)

  // state
  const items = ref<CartItem[]>([])

  // getters
  const totalQuantity = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))

  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  const selectedTotalPrice = computed(() =>
    items.value
      .filter((item) => item.checked)
      .reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  const selectedTotalQuantity = computed(() =>
    items.value.filter((item) => item.checked).reduce((sum, item) => sum + item.quantity, 0)
  )

  // 本地存储操作
  const STORAGE_KEY = 'localCart'

  const saveToLocal = () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value))
  }

  const loadFromLocal = () => {
    const localCart = localStorage.getItem(STORAGE_KEY)
    if (localCart) {
      items.value = JSON.parse(localCart)
    }
  }

  const clearLocal = () => {
    localStorage.removeItem(STORAGE_KEY)
  }

  // ========== actions ==========

  const initFromLocal = () => {
    loadFromLocal()
  }

  /**
   * 添加商品到购物车
   */
  const addToCart = async (item: Omit<CartItem, 'checked'>) => {
    const newItem = { ...item, checked: true }

    // 先更新本地状态，提供即时反馈
    const existIndex = items.value.findIndex((i) => i.productId === newItem.productId)
    if (existIndex >= 0) {
      if (items.value[existIndex]) {
        items.value[existIndex].quantity += newItem.quantity
      }
    } else {
      items.value.push(newItem)
    }

    if (isLogin.value) {
      try {
        await updateCartService({
          id: newItem.productId,
          quantity: newItem.quantity
        })
        // 添加成功后不需要立即调用 fetchCart，因为我们已经乐观更新了本地状态
        // fetchCart 会在下次进入购物车页面时调用
        console.log('添加成功，购物车已更新')
      } catch (error) {
        console.error('添加失败:', error)
        // 失败时回滚本地状态
        if (existIndex >= 0 && items.value[existIndex]) {
          items.value[existIndex].quantity -= newItem.quantity
        } else {
          items.value.pop()
        }
      }
    } else {
      saveToLocal()
    }
  }

  /**
   * 更新购物车商品数量
   */
  const updateQuantity = async (productId: number, quantity: number) => {
    // 先保存原值
    const item = items.value.find((i) => i.productId === productId)
    const originalQuantity = item?.quantity || 0

    // 先更新本地状态
    if (item) {
      if (quantity <= 0) {
        items.value = items.value.filter((i) => i.productId !== productId)
      } else {
        item.quantity = quantity
      }
    }

    if (isLogin.value) {
      try {
        await updateCartService({
          id: productId,
          quantity: quantity
        })
        await fetchCart()
      } catch (error) {
        console.error('更新失败:', error)
        // 失败时回滚
        if (item) {
          item.quantity = originalQuantity
        }
      }
    } else {
      saveToLocal()
    }
  }

  /**
   * 切换商品选中状态
   */
  const toggleChecked = (productId: number) => {
    const item = items.value.find((i) => i.productId === productId)
    if (item) {
      item.checked = !item.checked
      if (!isLogin.value) {
        saveToLocal()
      }
    }
  }

  /**
   * 全选/取消全选
   */
  const toggleAllChecked = (checked: boolean) => {
    items.value.forEach((item) => {
      item.checked = checked
    })
    if (!isLogin.value) {
      saveToLocal()
    }
  }

  /**
   * 删除购物车商品
   */
  const removeItem = async (productId: number) => {
    // 先从本地删除，提供即时反馈
    const originalItems = [...items.value]
    items.value = items.value.filter((i) => i.productId !== productId)

    if (isLogin.value) {
      try {
        await deleteCartService([productId])
        // 删除成功后，调用 fetchCart 确保数据同步
        await fetchCart()
      } catch (error) {
        console.error('删除失败:', error)
        // 删除失败时恢复原数据
        items.value = originalItems
      }
    } else {
      saveToLocal()
    }
  }

  /**
   * 批量删除
   */
  const removeItems = async (productIds: number[]) => {
    // 先从本地删除，提供即时反馈
    const originalItems = [...items.value]
    items.value = items.value.filter((i) => !productIds.includes(i.productId))

    if (isLogin.value) {
      try {
        await deleteCartService(productIds)
        // 删除成功后，调用 fetchCart 确保数据同步
        await fetchCart()
      } catch (error) {
        console.error('批量删除失败:', error)
        // 删除失败时恢复原数据
        items.value = originalItems
      }
    } else {
      saveToLocal()
    }
  }

  /**
   * 清空购物车
   */
  const clearCart = async () => {
    // 先从本地清空，提供即时反馈
    const originalItems = [...items.value]
    items.value = []

    if (isLogin.value) {
      try {
        const ids = originalItems.map((item) => item.productId)
        if (ids.length > 0) {
          await deleteCartService(ids)
        }
      } catch (error) {
        console.error('清空购物车失败:', error)
        // 清空失败时恢复原数据
        items.value = originalItems
      }
    } else {
      clearLocal()
    }
  }

  /**
   * 用户登录后：合并购物车
   */
  const mergeCartAfterLogin = async () => {
    const localCart = [...items.value]

    if (localCart.length === 0) {
      await fetchCart()
      return
    }

    for (const item of localCart) {
      try {
        await updateCartService({
          id: item.productId,
          quantity: item.quantity
        })
      } catch (error) {
        console.error('同步购物车失败:', error)
      }
    }

    items.value = []
    clearLocal()
    await fetchCart()
  }

  /**
   * 获取后端购物车数据
   */
  const fetchCart = async () => {
    try {
      const res = (await getCartService()) as any
      console.log('购物车响应原始数据:', res)

      // 兼容多种响应格式：
      // 1. { items: [...] } - 直接返回数组
      // 2. { data: { items: [...] } } - 标准响应格式
      // 3. axios 包装: { data: { items: [...] } }
      let rawItems: any[] = []

      if (Array.isArray(res)) {
        // 格式1: 直接是数组
        rawItems = res
      } else if (res?.data?.items) {
        // 格式2或3: 有 data.items
        rawItems = res.data.items
      } else if (res?.items) {
        // 格式2: 直接有 items
        rawItems = res.items
      }

      console.log('购物车数据加载成功:', rawItems)

      // 只有当后端返回了数据才更新，否则保留本地数据
      if (rawItems.length > 0) {
        items.value = rawItems.map((item: any) => ({
          productId: item.id,
          name: item.name || '',
          animeName: item.animeName || '',
          characterName: item.characterName || '',
          price: item.price || 0,
          quantity: item.quantity || 1,
          image: item.mainImage || '',
          checked: true
        }))
      }
    } catch (error) {
      console.error('获取购物车失败:', error)
      // 不要清空本地数据，保持当前状态
    }
  }

  /**
   * 用户登出时调用
   */
  const logout = () => {
    items.value = []
    clearLocal()
  }

  /**
   * 初始化购物车
   */
  const initCart = async () => {
    if (isLogin.value) {
      await fetchCart()
    } else {
      loadFromLocal()
    }
  }

  return {
    // state
    items,
    isLogin,
    // getters
    totalQuantity,
    totalPrice,
    selectedTotalPrice,
    selectedTotalQuantity,
    // actions
    initFromLocal,
    initCart,
    addToCart,
    updateQuantity,
    toggleChecked,
    toggleAllChecked,
    removeItem,
    removeItems,
    clearCart,
    mergeCartAfterLogin,
    fetchCart,
    logout
  }
})
