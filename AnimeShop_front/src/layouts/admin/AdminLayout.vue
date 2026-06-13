<template>
  <el-header>
    <AHeaderPage />
  </el-header>
  <el-container>
    <el-aside width="200px">
      <AmenuPage />
    </el-aside>
    <el-main>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import AHeaderPage from './AHeader.vue'
import AmenuPage from './Amenu.vue'
</script>

<style scoped>
.el-header {
  padding: 0;
}
</style>
