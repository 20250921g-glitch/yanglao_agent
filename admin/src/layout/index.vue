<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <el-icon v-if="isCollapse"><UserFilled /></el-icon>
        <span v-else>🏠 智慧养老系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#00C4A1"
      >
        <MenuTree :menus="menus" />
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { UserFilled, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { getUserMenus } from '@/api/index'
import MenuTree from '@/components/MenuTree.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const menus = ref([])

const activeMenu = computed(() => {
  const q = route.query.serviceType
  if (q) return route.path + '?serviceType=' + q
  return route.path
})

const loadMenus = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      return
    }
    const res = await getUserMenus(userId)
    if (res.code === 200) {
      menus.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载菜单失败')
  }
}

onMounted(() => {
  loadMenus()
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
      .then(() => { userStore.logout(); router.push('/login') })
      .catch(() => {})
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.sidebar { background-color: #304156; transition: width 0.3s; overflow: hidden; display: flex; flex-direction: column; height: 100%; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 16px; font-weight: bold; background: #2b3a4b; display: flex; align-items: center; justify-content: center; gap: 8px; white-space: nowrap; flex-shrink: 0; }
.sidebar-menu { border-right: none; flex: 1; overflow-y: auto; overflow-x: hidden; }
.sidebar-menu::-webkit-scrollbar { width: 6px; }
.sidebar-menu::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.2); border-radius: 3px; }
.header { background: #fff; box-shadow: 0 1px 4px rgba(0,21,41,.08); display: flex; align-items: center; justify-content: space-between; padding: 0 16px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.collapse-btn { font-size: 20px; cursor: pointer; color: #666; }
.header-right { display: flex; align-items: center; }
.user-dropdown { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 0 8px; }
.username { font-size: 14px; color: #333; }
.main-content { background: #f0f2f5; padding: 16px; overflow-y: auto; }
</style>
