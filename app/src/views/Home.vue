<template>
  <div class="home" v-loading="loading">
    <!-- 轮播图 -->
    <el-carousel v-if="banners.length" height="160px" indicator-position="outside" class="home-banner">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <img
          class="banner-img"
          :src="b.imageUrl"
          :alt="b.title"
          @click="openBanner(b)"
        />
      </el-carousel-item>
    </el-carousel>

    <!-- 个人信息卡 -->
    <el-card class="info-card" shadow="never">
      <div class="info-head">
        <el-avatar v-if="user && user.avatar" :size="64" :src="user.avatar" />
        <el-avatar v-else :size="64">{{ avatarText }}</el-avatar>
        <div class="info-base">
          <div class="name">
            {{ user ? (user.username || '用户') : '用户' }}
            <el-tag v-if="user && user.status === 1" size="small" type="success">正常</el-tag>
            <el-tag v-else-if="user" size="small" type="danger">禁用</el-tag>
          </div>
          <div class="sub">
            {{ user && user.realName ? '真实姓名：' + user.realName : '尚未实名认证' }}
          </div>
        </div>
      </div>

      <el-descriptions :column="3" border class="info-desc">
        <el-descriptions-item label="手机号">{{ user && user.phone ? user.phone : '-' }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">{{ user && user.levelName ? user.levelName : '普通会员' }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ user && user.points != null ? user.points : 0 }}</el-descriptions-item>
        <el-descriptions-item label="注册来源">{{ user && user.source ? user.source : '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ user && user.createTime ? user.createTime : '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ user && user.id ? user.id : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 功能中心 -->
    <div class="section-title">功能中心</div>
    <div class="feature-grid">
      <div
        v-for="f in features"
        :key="f.path"
        class="feature-card"
        @click="go(f)"
      >
        <div class="f-icon" :style="{ background: f.color }">{{ f.icon }}</div>
        <div class="f-name">{{ f.name }}</div>
        <div class="f-status">{{ f.ready ? '进入' : '功能待开发' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { currentUser, fetchUser } from '@/store/user'
import { getBanners } from '@/api'

const router = useRouter()
const loading = ref(false)
const user = currentUser
const banners = ref([])

const avatarText = computed(() => {
  const name = user.value ? (user.value.username || '用') : '用'
  return name.charAt(0)
})

const openBanner = (b) => {
  if (b.linkUrl) {
    if (b.linkUrl.startsWith('http')) {
      window.open(b.linkUrl, '_blank')
    } else {
      router.push(b.linkUrl)
    }
  }
}

// 养老系统为普通用户规划的功能模块（后续逐个开发）
const features = [
  { name: '健康档案', path: '/health', icon: '健', color: '#67C23A', ready: true },
  { name: '活动报名', path: '/activity', icon: '活', color: '#E6A23C', ready: true },
  { name: '邻里圈', path: '/community', icon: '邻', color: '#409EFF', ready: true },
  { name: '养老服务', path: '/service', icon: '养', color: '#00C4A1', ready: true },
  { name: '商城', path: '/mall', icon: '购', color: '#F56C6C', ready: true },
  { name: '消息通知', path: '/message', icon: '消', color: '#909399', ready: true }
]

const go = (f) => router.push(f.path)

const load = async () => {
  loading.value = true
  try {
    await fetchUser(true)
  } catch (e) {
    ElMessage.error(e.message || '获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const loadBanners = async () => {
  try {
    const res = await getBanners()
    if (res && res.data && res.data.records) {
      banners.value = res.data.records
    }
  } catch (e) {
    // 轮播图加载失败不影响主页其他功能
  }
}

onMounted(() => {
  load()
  loadBanners()
})
</script>

<style scoped>
.home-banner {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}
.banner-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
  cursor: pointer;
}
</style>
