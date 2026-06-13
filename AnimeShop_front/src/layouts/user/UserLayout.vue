<template>
  <el-container>
    <el-header>
      <u-header />
    </el-header>
    <el-main>
      <router-view></router-view>
    </el-main>
    <el-footer>二次元电商用户端 by:DCB 2026.5.21</el-footer>
  </el-container>
</template>

<script setup lang="ts">
import UHeader from './UHeader.vue'
</script>

<style scoped>
.el-footer {
  height: 50px;
  line-height: 50px;
  text-align: center;
  background-color: #e6f3ff;
}
</style>
