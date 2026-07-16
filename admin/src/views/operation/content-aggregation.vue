<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">内容聚合</h2>
          <span class="page-sub">各内容类型的体量、状态分布与最新动态一览</span>
        </div>
        <div>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>

      <el-row :gutter="16" v-loading="loading">
        <el-col v-for="item in list" :key="item.type" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
          <el-card shadow="hover" class="agg-card" @click="goManage(item.path)">
            <div class="agg-head">
              <span class="agg-name">{{ item.typeName }}</span>
              <el-tag size="small" type="info">{{ item.total }} 条</el-tag>
            </div>
            <div class="agg-body">
              <div class="stat">
                <span class="stat-num enabled">{{ item.enabled }}</span>
                <span class="stat-label">启用</span>
              </div>
              <div class="stat">
                <span class="stat-num disabled">{{ item.disabled }}</span>
                <span class="stat-label">停用</span>
              </div>
            </div>
            <div class="agg-foot">
              <div class="latest-title" :title="item.latestTitle">最新：{{ item.latestTitle || '—' }}</div>
              <div class="latest-time">{{ formatTime(item.latestTime) }}</div>
            </div>
            <div class="agg-link">前往管理 →</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getContentAggregation } from '@/api/index'

const router = useRouter()
const loading = ref(false)
const list = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await getContentAggregation()
    if (res.code === 200) list.value = res.data || []
    else ElMessage.error(res.message || '加载失败')
  } finally { loading.value = false }
}

const goManage = (path) => { if (path) router.push('/' + path) }

const formatTime = (t) => {
  if (!t) return ''
  const s = String(t).replace('T', ' ').substring(0, 19)
  return s
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; font-weight: 600; color: #303133; }
.page-sub { font-size: 13px; color: #A0AEC0; margin-left: 8px; }
.card-col { margin-bottom: 16px; }
.agg-card { cursor: pointer; transition: transform .15s; }
.agg-card:hover { transform: translateY(-3px); }
.agg-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.agg-name { font-size: 15px; font-weight: 600; color: #303133; }
.agg-body { display: flex; gap: 24px; margin-bottom: 14px; }
.stat { display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 24px; font-weight: 700; line-height: 1; }
.stat-num.enabled { color: #00C4A1; }
.stat-num.disabled { color: #A0AEC0; }
.stat-label { font-size: 12px; color: #A0AEC0; margin-top: 4px; }
.agg-foot { border-top: 1px dashed #ebeef5; padding-top: 10px; }
.latest-title { font-size: 13px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.latest-time { font-size: 12px; color: #c0c4cc; margin-top: 4px; }
.agg-link { margin-top: 10px; font-size: 13px; color: #00C4A1; font-weight: 500; }
</style>
