<template>
  <div id="app">
    <component v-if="layoutComponent !== 'div'" :is="layoutComponent" />
    <router-view v-else />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AdminLayout from '@/layouts/admin/AdminLayout.vue'
import UserLayout from '@/layouts/user/UserLayout.vue'

const route = useRoute()

const layoutComponent = computed(() => {
  const layout = route.meta.layout
  if (layout === 'admin') {
    return AdminLayout
  } else if (layout === 'user') {
    return UserLayout
  } else {
    return 'div'
  }
})
</script>

<style scoped></style>
