<template>
  <div class="cache-page">
    <el-card shadow="never" class="toolbar">
      <div class="toolbar-left">
        <span class="title">缓存监控</span>
        <span class="sub">Redis 全局命中率与各业务分组缓存实时状态</span>
      </div>
      <div class="toolbar-right">
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          :active-value="true"
          :inactive-value="false"
          @change="onAutoChange"
        />
        <span class="updated" v-if="generatedAt">更新于 {{ generatedAt }}</span>
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
    </el-card>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">全局命中率</div>
          <div class="stat-value" :class="hitClass">{{ hitRateText }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">Redis 已用内存</div>
          <div class="stat-value">{{ memoryText }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">Key 总数</div>
          <div class="stat-value">{{ totalKeys }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header><span>业务缓存分组</span></template>
      <el-table :data="groups" stripe v-loading="loading" border>
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="name" label="分组" min-width="140" />
        <el-table-column prop="pattern" label="Key 模式" min-width="200" show-overflow-tooltip />
        <el-table-column prop="designTtlMinutes" label="设计TTL(分)" width="120" align="center" />
        <el-table-column prop="keyCount" label="实际Key数" width="110" align="center" />
        <el-table-column label="剩余TTL(秒)" width="130" align="center">
          <template #default="{ row }">
            <span v-if="row.keyCount > 0">
              <span v-if="row.sampleTtlSeconds >= 0">{{ row.sampleTtlSeconds }}</span>
              <span v-else class="muted">—</span>
            </span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="示例内存" min-width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.keyCount > 0">
              <span v-if="row.sampleMemoryBytes >= 0">{{ formatBytes(row.sampleMemoryBytes) }}</span>
              <span v-else class="muted">不支持</span>
            </span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="sampleKey" label="示例Key" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.keyCount > 0">{{ row.sampleKey || '—' }}</span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getCacheStats } from '@/api'

const loading = ref(false)
const generatedAt = ref('')
const globalHitRate = ref(null)
const redisMemoryUsedMb = ref(null)
const totalKeys = ref(0)
const groups = ref([])

const hitRateText = computed(() => {
  if (globalHitRate.value == null) return '—'
  return globalHitRate.value.toFixed(2) + '%'
})
const hitClass = computed(() => {
  const v = globalHitRate.value
  if (v == null) return ''
  if (v >= 80) return 'good'
  if (v >= 50) return 'mid'
  return 'bad'
})
const memoryText = computed(() => {
  if (redisMemoryUsedMb.value == null) return '—'
  return redisMemoryUsedMb.value + ' MB'
})
const formatBytes = (b) => {
  if (b < 1024) return b + ' B'
  if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB'
  return (b / 1024 / 1024).toFixed(2) + ' MB'
}

const load = async () => {
  loading.value = true
  try {
    const res = await getCacheStats()
    const d = res.data || {}
    globalHitRate.value = d.globalHitRate
    redisMemoryUsedMb.value = d.redisMemoryUsedMb
    totalKeys.value = d.totalKeys || 0
    groups.value = d.groups || []
    generatedAt.value = d.generatedAt || ''
  } catch (e) {
    // 响应拦截器已统一提示错误
  } finally {
    loading.value = false
  }
}

const autoRefresh = ref(false)
let timer = null
const AUTO_INTERVAL = 10000
const onAutoChange = (val) => {
  if (val) {
    if (timer == null) {
      timer = setInterval(() => {
        if (!loading.value) load()
      }, AUTO_INTERVAL)
    }
  } else if (timer != null) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(load)
onUnmounted(() => {
  if (timer != null) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.cache-page { padding: 4px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.toolbar-left { display: flex; align-items: baseline; gap: 12px; }
.toolbar-left .title { font-size: 18px; font-weight: 600; color: #303133; }
.toolbar-left .sub { font-size: 13px; color: #909399; }
.toolbar-right { display: flex; align-items: center; gap: 12px; }
.toolbar-right .updated { font-size: 12px; color: #c0c4cc; }
.stat-row { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 10px; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; }
.stat-value.good { color: #00C4A1; }
.stat-value.mid { color: #e6a23c; }
.stat-value.bad { color: #f56c6c; }
.muted { color: #c0c4cc; }
</style>
