<!-- views/user/UserHome.vue -->
<template>
  <div class="userhome">
    <HomeBanner />
    <HomeHotShop />
    <HomeNewShop />
    <HomeSeckillShop />
  </div>
</template>

<script setup lang="ts">
import HomeBanner from './home/HomeBanner.vue'
import HomeHotShop from './home/HomeHotShop.vue'
import HomeNewShop from './home/HomeNewShop.vue'
import HomeSeckillShop from './home/HomeSeckillShop.vue'
</script>

<style scoped>
:deep(.el-main) {
  padding: 0 !important;
}
</style>
