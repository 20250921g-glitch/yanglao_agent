<template>
  <div class="service-analysis">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="background: #00C4A1">
          <div class="stat-value">{{ stats.totalOrders }}</div>
          <div class="stat-title">服务订单总数</div>
          <div class="stat-change up">+{{ stats.orderGrowth }} 较上月</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #14B8A6">
          <div class="stat-value">{{ stats.completionRate }}%</div>
          <div class="stat-title">完成率</div>
          <div class="stat-change up">+{{ stats.completionGrowth }}%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0EA5A4">
          <div class="stat-value">{{ stats.avgDuration }}分钟</div>
          <div class="stat-title">平均服务时长</div>
          <div class="stat-change down">+{{ stats.durationGrowth }}分钟</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0891B2">
          <div class="stat-value">{{ stats.satisfaction }}分</div>
          <div class="stat-title">客户满意度</div>
          <div class="stat-change up">{{ stats.satisfactionChange }}分 较上月</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">服务类型分布</span></template>
          <div class="chart-container" ref="typeChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">服务完成趋势（近30天）</span></template>
          <div class="chart-container" ref="trendChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">服务人员业绩排行 TOP10</span></template>
          <div class="chart-container" ref="workerChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">服务区域分布</span></template>
          <div class="chart-container" ref="areaChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getServiceAnalysisPage, getWorkerRank, getAreaRank } from '@/api/index'

const typeChartRef = ref(null)
const trendChartRef = ref(null)
const workerChartRef = ref(null)
const areaChartRef = ref(null)
const loading = ref(false)

const stats = reactive({
  totalOrders: 0,
  orderGrowth: 0,
  completionRate: 0,
  completionGrowth: 0,
  avgDuration: 0,
  durationGrowth: 0,
  satisfaction: 0,
  satisfactionChange: 0
})

const analysisData = ref([])
const workerRankData = ref([])
const areaRankData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getServiceAnalysisPage()
    analysisData.value = res.data.records || []
    try {
      const wrRes = await getWorkerRank()
      workerRankData.value = wrRes.data || []
    } catch (e) { workerRankData.value = [] }
    try {
      const arRes = await getAreaRank()
      areaRankData.value = arRes.data || []
    } catch (e) { areaRankData.value = [] }
    
    if (analysisData.value.length > 0) {
      stats.totalOrders = analysisData.value.reduce((sum, item) => sum + (item.orders || 0), 0)
      const totalAmount = analysisData.value.reduce((sum, item) => sum + (item.amount || 0), 0)
      stats.completionRate = analysisData.value.length > 0 ? Math.round(analysisData.value.reduce((sum, item) => sum + (item.completionRate || 0), 0) / analysisData.value.length * 10) / 10 : 0
      stats.satisfaction = analysisData.value.length > 0 ? Math.round(analysisData.value.reduce((sum, item) => sum + (item.avgScore || 0), 0) / analysisData.value.length * 10) / 10 : 0
      stats.avgDuration = Math.round(totalAmount / stats.totalOrders) || 0
      stats.orderGrowth = 0
      stats.completionGrowth = 0
      stats.durationGrowth = 0
      stats.satisfactionChange = 0
    }
  } catch (e) {
    analysisData.value = []
  }
  loading.value = false
  
  nextTick(() => {
    renderTypeChart()
    renderTrendChart()
    renderWorkerChart()
    renderAreaChart()
  })
}

const renderTypeChart = () => {
  if (!typeChartRef.value) return
  const chart = echarts.init(typeChartRef.value)
  
  const data = analysisData.value.map(item => ({
    name: item.serviceType,
    value: item.orders || 0,
    color: ['#00C4A1', '#52C41A', '#FAAD14', '#FF4D4F', '#A0AEC0'][analysisData.value.indexOf(item) % 5]
  }))
  
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } },
      data: data
    }]
  })
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  
  const xAxis = analysisData.value.map(item => item.serviceType)
  const completed = analysisData.value.map(item => Math.round((item.orders || 0) * (item.completionRate || 90) / 100))
  const pending = analysisData.value.map(item => (item.orders || 0) - Math.round((item.orders || 0) * (item.completionRate || 90) / 100))
  
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['已完成', '进行中'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 40, containLabel: true },
    xAxis: { type: 'category', data: xAxis, axisLabel: { color: '#606266', rotate: 30 } },
    yAxis: { type: 'value', axisLabel: { color: '#606266' } },
    series: [
      { name: '已完成', type: 'bar', data: completed, itemStyle: { color: '#52C41A' } },
      { name: '进行中', type: 'bar', data: pending, itemStyle: { color: '#FAAD14' } }
    ]
  })
}

const renderWorkerChart = () => {
  if (!workerChartRef.value) return
  const chart = echarts.init(workerChartRef.value)
  const palette = ['#00C4A1', '#52C41A', '#FAAD14', '#FF4D4F', '#A0AEC0', '#9254DE', '#13C2C2', '#EB2F96', '#FA8C16', '#2F54EB']
  const rows = (workerRankData.value || []).slice().sort((a, b) => (Number(b.value) || 0) - (Number(a.value) || 0))
  const workers = rows.map(r => r.name)
  const counts = rows.map((r, i) => ({ value: Number(r.value) || 0, itemStyle: { color: palette[i % palette.length] } }))
  if (workers.length === 0) {
    chart.clear()
    chart.setOption({ title: { text: '暂无人员业绩数据', left: 'center', top: 'center', textStyle: { color: '#A0AEC0', fontSize: 13 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: 10, containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: '#606266' } },
    yAxis: { type: 'category', data: workers, axisLabel: { color: '#606266', fontSize: 12 } },
    series: [{
      type: 'bar', barWidth: 14,
      data: counts,
      label: { show: true, position: 'right', color: '#606266', fontSize: 11 }
    }]
  })
}

const renderAreaChart = () => {
  if (!areaChartRef.value) return
  const chart = echarts.init(areaChartRef.value)
  const palette = ['#FF4D4F', '#00C4A1', '#52C41A', '#FAAD14', '#9254DE', '#13C2C2', '#EB2F96', '#FA8C16']
  const rows = (areaRankData.value || []).slice().sort((a, b) => (Number(b.value) || 0) - (Number(a.value) || 0))
  const areas = rows.map(r => r.name)
  const counts = rows.map((r, i) => ({ value: Number(r.value) || 0, itemStyle: { color: palette[i % palette.length] } }))
  if (areas.length === 0) {
    chart.clear()
    chart.setOption({ title: { text: '暂无区域分布数据', left: 'center', top: 'center', textStyle: { color: '#A0AEC0', fontSize: 13 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 10, containLabel: true },
    xAxis: { type: 'category', data: areas, axisLabel: { color: '#606266', rotate: 20 } },
    yAxis: { type: 'value', axisLabel: { color: '#606266' } },
    series: [{
      type: 'bar', name: '订单量', barWidth: 18,
      data: counts,
      label: { show: true, position: 'top', color: '#606266', fontSize: 11 }
    }]
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.service-analysis { padding: 16px; }
.stat-row, .chart-row { margin-bottom: 16px; }
.stat-card { padding: 20px; border-radius: 8px; color: #fff; display: flex; flex-direction: column; gap: 6px; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-title { font-size: 14px; opacity: 0.9; }
.stat-change { font-size: 12px; }
.up { color: #d4fc79; }
.down { color: #ff9a9e; }
.card-title { font-weight: 600; font-size: 15px; }
.chart-container { height: 280px; }
</style>
