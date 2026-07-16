<template>
  <div class="user-analysis">
    <!-- 顶部工具栏：日期筛选 -->
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="时间筛选">
          <el-radio-group v-model="timeRange" size="default" @change="loadTrend">
            <el-radio-button label="week">近7天</el-radio-button>
            <el-radio-button label="month">近30天</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="timeRange === 'custom'" label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadTrend"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户趋势统计：新增用户数量折线图（按日期） -->
    <el-card class="chart-card">
      <template #header><span class="card-title">用户趋势统计（新增用户数量）</span></template>
      <div class="chart-container" ref="trendChartRef"></div>
    </el-card>

    <el-row :gutter="16" class="chart-row">
      <!-- 用户年龄构成 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header><span class="card-title">用户年龄构成</span></template>
          <div class="chart-container" ref="ageChartRef"></div>
        </el-card>
      </el-col>
      <!-- 用户性别构成 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header><span class="card-title">用户性别构成</span></template>
          <div class="chart-container" ref="genderChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getUserAnalysisPage, getUserAgeAnalysis, getUserGenderAnalysis } from '@/api'

const timeRange = ref('week')
const dateRange = ref([])
const trendChartRef = ref(null)
const ageChartRef = ref(null)
const genderChartRef = ref(null)

const trendData = ref([])
const ageData = ref([])
const genderData = ref([])

const themeColors = ['#00C4A1', '#2DD4BF', '#14B8A6', '#0EA5A4', '#0891B2', '#5EEAD4']

const loadTrend = async () => {
  try {
    const res = await getUserAnalysisPage()
    trendData.value = res.data.records || []
  } catch (e) {}
  renderTrend()
}
const loadAge = async () => {
  try { const res = await getUserAgeAnalysis(); ageData.value = res.data || [] } catch (e) {}
  renderAge()
}
const loadGender = async () => {
  try { const res = await getUserGenderAnalysis(); genderData.value = res.data || [] } catch (e) {}
  renderGender()
}

const renderTrend = () => {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>新增用户: <strong>${p[0].value}</strong>` },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 30, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: trendData.value.map(i => i.date), axisLabel: { color: '#606266' } },
    yAxis: { type: 'value', axisLabel: { color: '#606266' } },
    series: [{
      name: '新增用户', type: 'line', smooth: true, symbol: 'circle', symbolSize: 8,
      data: trendData.value.map(i => i.newUsers || 0),
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(0,196,161,0.4)' }, { offset: 1, color: 'rgba(0,196,161,0.05)' }]) },
      lineStyle: { color: '#00C4A1', width: 3 },
      itemStyle: { color: '#00C4A1', borderWidth: 2, borderColor: '#fff' }
    }]
  })
}
const renderAge = () => {
  if (!ageChartRef.value || !ageData.value.length) return
  const chart = echarts.init(ageChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center', textStyle: { color: '#606266' } },
    series: [{
      type: 'pie', radius: ['42%', '70%'], center: ['50%', '45%'],
      avoidLabelOverlap: false, itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4A5568' },
      data: ageData.value.map((i, idx) => ({ value: i.count, name: i.ageGroup, itemStyle: { color: themeColors[idx % themeColors.length] } }))
    }]
  })
}
const renderGender = () => {
  if (!genderChartRef.value || !genderData.value.length) return
  const chart = echarts.init(genderChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center', textStyle: { color: '#606266' } },
    series: [{
      type: 'pie', radius: ['42%', '70%'], center: ['50%', '45%'],
      avoidLabelOverlap: false, itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4A5568' },
      data: genderData.value.map((i, idx) => ({ value: i.count, name: i.gender, itemStyle: { color: themeColors[idx % themeColors.length] } }))
    }]
  })
}

onMounted(async () => {
  await Promise.all([loadTrend(), loadAge(), loadGender()])
  nextTick(() => { renderTrend(); renderAge(); renderGender() })
})
</script>

<style scoped>
.user-analysis { padding: 16px; }
.filter-card { margin-bottom: 16px; }
.chart-card { margin-bottom: 16px; }
.card-title { font-weight: 600; font-size: 15px; }
.chart-container { height: 300px; }
</style>
