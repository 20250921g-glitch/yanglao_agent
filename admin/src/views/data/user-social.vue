<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">用户社交统计</h2>
          <span class="page-sub">用户社交行为趋势分析</span>
        </div>
        <el-tag type="success">实时数据 · /data/user-social-analysis</el-tag>
      </div>

      <div class="stat-row">
        <div class="stat-card" v-for="s in stats" :key="s.label">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>

      <el-card shadow="never" class="chart-card">
        <div ref="chartRef" class="chart"></div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getUserSocialAnalysis } from '@/api/index'

const chartRef = ref(null)
const stats = ref([
  { label: '总发帖数', value: 0 },
  { label: '总评论数', value: 0 },
  { label: '总点赞数', value: 0 }
])

const renderChart = (data) => {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['发帖', '评论', '点赞'] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: data.map(d => d.date), boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      { name: '发帖', type: 'line', smooth: true, data: data.map(d => d.posts), itemStyle: { color: '#00C4A1' }, areaStyle: { color: 'rgba(0,196,161,0.12)' } },
      { name: '评论', type: 'line', smooth: true, data: data.map(d => d.comments), itemStyle: { color: '#52C41A' } },
      { name: '点赞', type: 'line', smooth: true, data: data.map(d => d.likes), itemStyle: { color: '#FAAD14' } }
    ]
  })
}

onMounted(async () => {
  try {
    const res = await getUserSocialAnalysis()
    if (res.code === 200) {
      const data = res.data || []
      const totals = data.reduce((a, d) => ({ posts: a.posts + (d.posts || 0), comments: a.comments + (d.comments || 0), likes: a.likes + (d.likes || 0) }), { posts: 0, comments: 0, likes: 0 })
      stats.value = [
        { label: '总发帖数', value: totals.posts },
        { label: '总评论数', value: totals.comments },
        { label: '总点赞数', value: totals.likes }
      ]
      await nextTick()
      renderChart(data)
    }
  } catch (e) { /* 静默 */ }
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; font-weight: 600; color: #303133; }
.page-sub { font-size: 13px; color: #A0AEC0; margin-left: 8px; }
.stat-row { display: flex; gap: 16px; margin-bottom: 16px; }
.stat-card { flex: 1; background: linear-gradient(135deg, #00C4A1, #04a98a); color: #fff; border-radius: 10px; padding: 18px 20px; }
.stat-value { font-size: 26px; font-weight: 700; }
.stat-label { font-size: 13px; opacity: 0.9; margin-top: 4px; }
.chart-card { margin-top: 4px; }
.chart { width: 100%; height: 360px; }
</style>
