<template>
  <div class="repurchase-analysis">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="background: #00C4A1">
          <div class="stat-value">{{ avgRate }}%</div>
          <div class="stat-title">平均复购率</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #14B8A6">
          <div class="stat-value">{{ totalNewUsers }}</div>
          <div class="stat-title">累计新用户</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0EA5A4">
          <div class="stat-value">{{ totalRepurchase }}</div>
          <div class="stat-title">累计复购用户</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0891B2">
          <div class="stat-value">{{ maxRate }}%</div>
          <div class="stat-title">最高复购率</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header><span class="card-title">复购率趋势（近7天）</span></template>
          <div class="chart-container" ref="lineChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card>
          <template #header><span class="card-title">新用户与复购用户对比</span></template>
          <div class="chart-container" ref="barChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span class="card-title">复购率分布</span></template>
          <div class="chart-container" ref="pieChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header><span class="card-title">每日复购详情</span></template>
      <el-table :data="repurchaseData" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="newUsers" label="新用户" width="100" />
        <el-table-column prop="repurchaseUsers" label="复购用户" width="110" />
        <el-table-column label="复购率" width="100">
          <template #default="{ row }">
            <el-progress :percentage="row.rate" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column label="复购率" width="80">
          <template #default="{ row }">{{ row.rate }}%</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getRepurchaseAnalysis } from '@/api'

const repurchaseData = ref([])
const lineChartRef = ref(null)
const barChartRef = ref(null)
const pieChartRef = ref(null)

const avgRate = ref(0)
const totalNewUsers = ref(0)
const totalRepurchase = ref(0)
const maxRate = ref(0)

const loadData = async () => {
  try {
    const res = await getRepurchaseAnalysis()
    repurchaseData.value = res.data || []
    totalNewUsers.value = repurchaseData.value.reduce((sum, item) => sum + item.newUsers, 0)
    totalRepurchase.value = repurchaseData.value.reduce((sum, item) => sum + item.repurchaseUsers, 0)
    avgRate.value = Math.round(totalNewUsers.value > 0 ? totalRepurchase.value / totalNewUsers.value * 100 : 0)
    maxRate.value = Math.max(...repurchaseData.value.map(item => item.rate), 0)
    nextTick(() => { renderLineChart(); renderBarChart(); renderPieChart() })
  } catch {}
}

const renderLineChart = () => {
  const chart = echarts.init(lineChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['复购率'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: repurchaseData.value.map(item => item.date) },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}%' }, max: 100 },
    series: [{
      name: '复购率',
      type: 'line',
      smooth: true,
      data: repurchaseData.value.map(item => item.rate),
      itemStyle: { color: '#00C4A1' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,196,161,0.3)' }, { offset: 1, color: 'rgba(0,196,161,0.05)' }]) }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const renderBarChart = () => {
  const chart = echarts.init(barChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['新用户', '复购用户'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: repurchaseData.value.map(item => item.date) },
    yAxis: { type: 'value' },
    series: [
      { name: '新用户', type: 'bar', data: repurchaseData.value.map(item => item.newUsers), itemStyle: { color: '#00C4A1' } },
      { name: '复购用户', type: 'bar', data: repurchaseData.value.map(item => item.repurchaseUsers), itemStyle: { color: '#52C41A' } }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

const renderPieChart = () => {
  const chart = echarts.init(pieChartRef.value)
  const highRate = repurchaseData.value.filter(item => item.rate >= 40).length
  const midRate = repurchaseData.value.filter(item => item.rate >= 30 && item.rate < 40).length
  const lowRate = repurchaseData.value.filter(item => item.rate < 30).length
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}天 ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['55%', '50%'],
      data: [
        { value: highRate, name: '高复购(>=40%)', itemStyle: { color: '#52C41A' } },
        { value: midRate, name: '中复购(30-40%)', itemStyle: { color: '#FAAD14' } },
        { value: lowRate, name: '低复购(<30%)', itemStyle: { color: '#FF4D4F' } }
      ]
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(loadData)
</script>

<style scoped>
.stat-row { margin-bottom: 16px; }
.chart-row { margin-bottom: 16px; }
.chart-container { height: 300px; }
.card-title { font-weight: 600; }
.stat-card {
  border-radius: 12px; padding: 20px; color: #fff;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
}
.stat-value { font-size: 32px; font-weight: 700; }
.stat-title { font-size: 14px; margin-top: 8px; }
</style>
