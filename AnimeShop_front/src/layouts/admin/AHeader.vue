<template>
  <div class="aheaderpage">
    <div class="logo">
      <img :src="logoUrl" alt="管理端logo" />
      <span>二次元电商管理端</span>
    </div>
    <div class="userDatiles">
      <el-dropdown @command="handleCommand">
        <span class="el-dropdown-link">
          <el-avatar :size="50" :src="userStore.userInfo?.avatar" />
          {{ userStore.userInfo?.username }}
          <el-icon class="el-icon--right">
            <arrow-down />
          </el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="rePassword">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/index'
import { userLogoutService } from '@/api/user'
import { useRouter } from 'vue-router'
import { showModule } from '@/utils/ElFunction'
import adminLogo from '@/assets/images/adminLogo.png'

const userStore = useUserStore()
const router = useRouter()
const logoUrl = adminLogo

const handleCommand = (command: string) => {
  switch (command) {
    case 'logout':
      logout()
      break
  }
}
/**
 * 退出登录
 */
const logout = () => {
  showModule('确定要退出登录吗？', '退出登录', 'warning').then(async () => {
    await userLogoutService()
    userStore.logout()
    router.push('/login')
  })
}
</script>

<style scoped>
.aheaderpage {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
  margin-bottom: 5px;
  padding: 5px 10px;
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
  width: 100%;
}

.aheaderpage .logo img {
  width: 50px;
  height: 50px;
}

.aheaderpage .logo {
  display: flex;
  align-items: center;
}

.aheaderpage .logo span {
  margin-left: 10px;
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.aheaderpage .userDatiles span {
  display: flex;
  align-items: center;
  margin: 0 15px;
  padding: 5px;
}

.aheaderpage .userDatiles .el-avatar {
  margin-right: 2px;
}

.el-dropdown-link:focus {
  outline: none !important;
}
</style>
