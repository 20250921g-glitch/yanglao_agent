<template>
  <div class="user-layout">
    <header class="top-bar">
      <div class="brand" @click="goHome">
        <span class="logo">智慧养老</span>
        <span class="tag">用户端</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        router
        class="top-menu"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/health">健康档案</el-menu-item>
        <el-menu-item index="/activity">活动报名</el-menu-item>
        <el-menu-item index="/community">邻里圈</el-menu-item>
        <el-menu-item index="/service">养老服务</el-menu-item>
        <el-menu-item index="/mall">商城</el-menu-item>
        <el-menu-item index="/message">消息通知</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
      </el-menu>

      <div class="user-area">
        <el-avatar v-if="user && user.avatar" :size="32" :src="user.avatar" />
        <el-avatar v-else :size="32">{{ avatarText }}</el-avatar>
        <span class="uname">{{ user ? user.username || '用户' : '用户' }}</span>
        <el-button text type="primary" @click="logout">退出</el-button>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { currentUser, fetchUser, clearUser } from '@/store/user'

const route = useRoute()
const router = useRouter()
const user = currentUser

const avatarText = computed(() => {
  const name = user.value ? (user.value.username || '用') : '用'
  return name.charAt(0)
})

// 横向菜单高亮：取一级路径
const activeMenu = computed(() => {
  if (route.path === '/') return '/'
  const seg = route.path.split('/')[1]
  return seg ? '/' + seg : '/'
})

const goHome = () => router.push('/')

const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  clearUser()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  fetchUser().catch(() => {
    // token 失效或未登录，回到登录页
    router.push('/login')
  })
})
</script>
