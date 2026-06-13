<template>
  <div class="nav">
    <el-menu
      :default-active="activeIndex"
      mode="horizontal"
      background-color="#f0f8ff"
      text-color="#6b7280"
      active-text-color="#3b82f6"
      class="custom-menu"
      @select="handleSelect"
    >
      <template v-for="item in toTopMenu" :key="item.id">
        <!-- 有子菜单 --->
        <el-sub-menu v-if="toSubMenu(item.id).length > 0" :index="item.id.toString()">
          <template #title>{{ item.name }}</template>
          <el-menu-item
            v-for="subItem in toSubMenu(item.id)"
            :key="subItem.id"
            :index="subItem.id.toString()"
          >
            {{ subItem.name }}
          </el-menu-item>
        </el-sub-menu>
        <!-- 无子菜单 --->
        <el-menu-item v-else :index="item.id.toString()">{{ item.name }}</el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
interface Category {
  id: number
  name: string
  parentId: number
  sort: number
}

import { onMounted, ref, computed } from 'vue'
import { getCategoryService } from '@/api/category'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const activeIndex = ref('1') //菜单项的 index
const categoryList = ref<Category[]>([]) //初始化分类列表,响应式可随时更新
const router = useRouter()

/**
 * 获取分类列表,数据装进categoryList响应式数组
 */
const getCategoryList = async () => {
  try {
    const res = await getCategoryService()
    categoryList.value = res.data
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  }
}

/**
 * 获取顶级分类
 */
const toTopMenu = computed(() => {
  return categoryList.value
    .filter((item) => item.parentId === 0) //筛选
    .sort((a, b) => a.sort - b.sort) //排序，<0的话a在前面，>0的话降序
})

/**
 * 获取子分类，参数表示父id
 */
const toSubMenu = (parentId: number) => {
  return categoryList.value
    .filter((item) => item.parentId === parentId) //筛选子元素对应的父id与参数id是否相同
    .sort((a, b) => a.sort - b.sort) //排序
}

/**
 * 菜单项点击事件
 */
const handleSelect = (key: string) => {
  const id = parseInt(key)
  const item = categoryList.value.find((c) => c.id === id) //根据id查找分类

  // 如果有子菜单，进行路由跳转
  if (item) {
    router.push({
      name: 'userCategory', //路由名称
      params: { id: item.id }, //传递分类ID
      query: { name: item.name } //传递分类名称
    })
  }
}

onMounted(() => {
  getCategoryList()
})
</script>

<style scoped>
.nav {
  height: 50px;
}

.nav .el-menu {
  border: none; /* 确保没有边框 */
  height: 50px;
  background-color: #f0f8ff !important; /* 确保背景色一致 */
}

/* 深度传透防止菜单项折叠 */
.nav :deep(.el-menu-item) {
  padding: 0 16px;
  font-size: 16px;
}
</style>
