<template>
  <div class="home-panel">
    <div class="head">
      <!-- 主标题和副标题 -->
      <h3>
        {{ title }}
        <small>{{ subTitle }}</small>
      </h3>
    </div>
    <!-- 插槽占位，等待父组件<Card />传递主体内容 -->
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
//定义props
defineProps({
  title: {
    type: String,
    default: '主标题'
  },
  subTitle: {
    type: String,
    default: '副标题'
  }
})
</script>

<style scoped>
.home-panel {
  background-color: #fff;
  border-radius: 16px; /* 圆润的圆角，二次元常见 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 柔和阴影 */
}

.home-panel .head {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 2px solid #e8eef2; /* 浅灰蓝分割线 */
}

.home-panel .head h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50; /* 深紫色，更二次元 */
  letter-spacing: 0.5px; /* 字间距 */
  height: 0;
}

.home-panel .head small {
  font-size: 12px; /* 从默认14px改小 */
  font-weight: 400;
  color: #7f8c8d; /* 淡紫色，透明感 */
  opacity: 0.7; /* 透明度70% */
  margin-left: 12px;
  letter-spacing: 0.3px; /* 字间距 */
}
</style>
