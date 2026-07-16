<template>
  <div class="trade-analysis">
    <!-- 顶部时间筛选 -->
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="时间范围">
          <el-radio-group v-model="timeRange" size="default" @change="loadData">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="timeRange === 'custom'" label="日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" @change="loadData" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="background: #00C4A1">
          <div class="stat-value">¥{{ stats.totalAmount.toLocaleString() }}</div>
          <div class="stat-title">交易总额</div>
          <div class="stat-change up">+{{ stats.amountGrowth }}% 较上周</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #14B8A6">
          <div class="stat-value">{{ stats.orderCount }}</div>
          <div class="stat-title">订单总数</div>
          <div class="stat-change up">+{{ stats.orderGrowth }} 较上周</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0EA5A4">
          <div class="stat-value">¥{{ stats.avgPrice }}</div>
          <div class="stat-title">客单价</div>
          <div class="stat-change up">+{{ stats.priceGrowth }}%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0891B2">
          <div class="stat-value">{{ stats.refundRate }}%</div>
          <div class="stat-title">退款率</div>
          <div class="stat-change down">+{{ stats.refundGrowth }}% 较上周</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <el-card>
          <template #header><span class="card-title">交易额趋势（近30天）</span></template>
          <div class="chart-container" ref="amountChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span class="card-title">支付方式占比</span></template>
          <div class="chart-container" ref="payMethodRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">订单量分布（按日）</span></template>
          <div class="chart-container" ref="orderChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">交易时段分布</span></template>
          <div class="chart-container" ref="timeChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getTradeAnalysisPage, getTradePayMethod, getTradeHourly } from '@/api/index'

const timeRange = ref('month')
const dateRange = ref([])
const amountChartRef = ref(null)
const payMethodRef = ref(null)
const orderChartRef = ref(null)
const timeChartRef = ref(null)
const loading = ref(false)

const stats = reactive({
  totalAmount: 0,
  amountGrowth: 0,
  orderCount: 0,
  orderGrowth: 0,
  avgPrice: 0,
  priceGrowth: 0,
  refundRate: 0,
  refundGrowth: 0
})

const analysisData = ref([])
const payMethodData = ref([])
const hourlyData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTradeAnalysisPage()
    analysisData.value = res.data.records || []
    try {
      const pmRes = await getTradePayMethod()
      payMethodData.value = pmRes.data || []
    } catch (e) { payMethodData.value = [] }
    try {
      const hRes = await getTradeHourly()
      hourlyData.value = hRes.data || []
    } catch (e) { hourlyData.value = [] }
    
    if (analysisData.value.length > 0) {
      const latest = analysisData.value[analysisData.value.length - 1]
      const prev = analysisData.value.length > 1 ? analysisData.value[analysisData.value.length - 2] : latest
      
      stats.totalAmount = analysisData.value.reduce((sum, item) => sum + (item.amount || 0), 0)
      stats.orderCount = analysisData.value.reduce((sum, item) => sum + (item.orders || 0), 0)
      stats.avgPrice = stats.orderCount > 0 ? Math.round((stats.totalAmount / stats.orderCount) * 10) / 10 : 0
      stats.refundRate = stats.orderCount > 0 ? Math.round((analysisData.value.reduce((sum, item) => sum + (item.refunds || 0), 0) / stats.orderCount) * 100 * 10) / 10 : 0
      
      stats.amountGrowth = Math.round((((latest.amount || 0) - (prev.amount || 0)) / (prev.amount || 1)) * 100 * 10) / 10
      stats.orderGrowth = (latest.orders || 0) - (prev.orders || 0)
      stats.priceGrowth = 0
      stats.refundGrowth = 0
    }
  } catch (e) {
    analysisData.value = []
  }
  loading.value = false
  
  nextTick(() => {
    renderAmountChart()
    renderPayMethod()
    renderOrderChart()
    renderTimeChart()
  })
}

const renderAmountChart = () => {
  if (!amountChartRef.value) return
  const chart = echarts.init(amountChartRef.value)
  const xAxis = analysisData.value.map(item => item.date)
  const amounts = analysisData.value.map(item => item.amount || 0)
  
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 20, containLabel: true },
    xAxis: { type: 'category', data: xAxis, axisLabel: { color: '#606266', rotate: 30 } },
    yAxis: { type: 'value', axisLabel: { color: '#606266', formatter: v => `¥${(v/1000).toFixed(0)}k` } },
    series: [{
      type: 'line', smooth: true, name: '交易额',
      data: amounts, itemStyle: { color: '#00C4A1' },
      areaStyle: { color: 'rgba(0,196,161,0.15)' }
    }]
  })
}

const renderPayMethod = () => {
  if (!payMethodRef.value) return
  const chart = echarts.init(payMethodRef.value)
  const palette = ['#07C160', '#1677FF', '#FAAD14', '#A0AEC0', '#FF4D4F']
  const data = (payMethodData.value || []).map((d, i) => ({
    name: d.name,
    value: Number(d.value) || 0,
    itemStyle: { color: palette[i % palette.length] }
  }))
  if (data.length === 0) {
    chart.clear()
    chart.setOption({ title: { text: '暂无支付数据', left: 'center', top: 'center', textStyle: { color: '#A0AEC0', fontSize: 13 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie', radius: ['45%', '75%'], center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } },
      data
    }]
  })
}

const renderOrderChart = () => {
  if (!orderChartRef.value) return
  const chart = echarts.init(orderChartRef.value)
  const xAxis = analysisData.value.map(item => item.date)
  const orders = analysisData.value.map(item => item.orders || 0)
  
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 20, containLabel: true },
    xAxis: { type: 'category', data: xAxis, axisLabel: { color: '#606266', rotate: 30 } },
    yAxis: { type: 'value', axisLabel: { color: '#606266' } },
    series: [{
      type: 'bar', name: '订单量', barWidth: 12,
      data: orders, itemStyle: { color: '#52C41A', borderRadius: [4, 4, 0, 0] }
    }]
  })
}

const renderTimeChart = () => {
  if (!timeChartRef.value) return
  const chart = echarts.init(timeChartRef.value)
  const rows = (hourlyData.value || []).slice().sort((a, b) => (a.hour || '').localeCompare(b.hour || ''))
  const hours = rows.map(r => r.hour)
  const totalAmount = rows.reduce((s, r) => s + (Number(r.amount) || 0), 0) || 1
  const amounts = rows.map(r => Number(r.amount) || 0)
  if (hours.length === 0) {
    chart.clear()
    chart.setOption({ title: { text: '暂无时段数据', left: 'center', top: 'center', textStyle: { color: '#A0AEC0', fontSize: 13 } } })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 20, containLabel: true },
    xAxis: { type: 'category', data: hours, axisLabel: { color: '#606266' } },
    yAxis: { type: 'value', axisLabel: { color: '#606266', formatter: v => `¥${v}` } },
    series: [{
      type: 'bar', name: '交易额', barWidth: 20,
      data: amounts.map(v => ({
        value: v,
        itemStyle: {
          color: v > totalAmount * 0.15 ? '#00C4A1' : v > totalAmount * 0.08 ? '#52C41A' : '#FAAD14'
        }
      }))
    }]
  })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.trade-analysis { padding: 16px; }
.filter-card { margin-bottom: 16px; }
.stat-row, .chart-row { margin-bottom: 16px; }
.stat-card { padding: 20px; border-radius: 8px; color: #fff; display: flex; flex-direction: column; gap: 6px; }
.stat-value { font-size: 26px; font-weight: bold; }
.stat-title { font-size: 14px; opacity: 0.9; }
.stat-change { font-size: 12px; }
.up { color: #d4fc79; }
.down { color: #ff9a9e; }
.card-title { font-weight: 600; font-size: 15px; }
.chart-container { height: 280px; }
</style>
