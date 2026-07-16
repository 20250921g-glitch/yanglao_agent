<template>
  <div class="evaluation-analysis">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="background: #00C4A1">
          <div class="stat-value">{{ totalCount }}</div>
          <div class="stat-title">评价总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #14B8A6">
          <div class="stat-value">{{ avgScore }}</div>
          <div class="stat-title">平均评分</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0EA5A4">
          <div class="stat-value">{{ positiveRate }}%</div>
          <div class="stat-title">好评率</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0891B2">
          <div class="stat-value">{{ replyRate }}%</div>
          <div class="stat-title">回复率</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card>
          <template #header><span class="card-title">评分分布柱状图</span></template>
          <div class="chart-container" ref="barChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span class="card-title">评价分类饼图</span></template>
          <div class="chart-container" ref="pieChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header><span class="card-title">评价数量趋势（近7天）</span></template>
          <div class="chart-container" ref="lineChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header><span class="card-title">评分统计详情</span></template>
      <el-table :data="evaluationData" stripe>
        <el-table-column prop="score" label="评分" width="80">
          <template #default="{ row }">
            <div class="star-rating">
              <i v-for="n in 5" :key="n" class="el-icon-star" :class="{ 'is-active': n <= row.score }"></i>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="100" />
        <el-table-column label="占比" width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.percentage" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column label="占比" width="80">
          <template #default="{ row }">{{ row.percentage }}%</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getEvaluationAnalysis } from '@/api'

const evaluationData = ref([])
const barChartRef = ref(null)
const pieChartRef = ref(null)
const lineChartRef = ref(null)

const totalCount = ref(0)
const avgScore = ref(0)
const positiveRate = ref(0)
const replyRate = ref(0)

const loadData = async () => {
  try {
    const res = await getEvaluationAnalysis()
    evaluationData.value = res.data || []
    totalCount.value = evaluationData.value.reduce((sum, item) => sum + item.count, 0)
    avgScore.value = evaluationData.value.reduce((sum, item) => sum + item.score * item.count, 0) / totalCount.value
    avgScore.value = Math.round(avgScore.value * 10) / 10
    const positive = evaluationData.value.filter(item => item.score >= 4).reduce((sum, item) => sum + item.count, 0)
    positiveRate.value = Math.round(totalCount.value > 0 ? positive / totalCount.value * 100 : 0)
    replyRate.value = 85
    nextTick(() => { renderBarChart(); renderPieChart(); renderLineChart() })
  } catch {}
}

const renderBarChart = () => {
  const chart = echarts.init(barChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: evaluationData.value.map(item => `${item.score}星`) },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: evaluationData.value.map(item => ({
        value: item.count,
        itemStyle: { color: ['#FF4D4F', '#FF4D4F', '#FAAD14', '#52C41A', '#52C41A'][item.score - 1] }
      })),
      barWidth: '50%'
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const renderPieChart = () => {
  const chart = echarts.init(pieChartRef.value)
  const excellent = evaluationData.value.find(item => item.score === 5)?.count || 0
  const good = evaluationData.value.find(item => item.score === 4)?.count || 0
  const medium = evaluationData.value.find(item => item.score === 3)?.count || 0
  const poor = evaluationData.value.filter(item => item.score <= 2).reduce((sum, item) => sum + item.count, 0)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}条 ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['55%', '50%'],
      data: [
        { value: excellent, name: '优秀(5星)', itemStyle: { color: '#52C41A' } },
        { value: good, name: '良好(4星)', itemStyle: { color: '#00C4A1' } },
        { value: medium, name: '中等(3星)', itemStyle: { color: '#FAAD14' } },
        { value: poor, name: '较差(1-2星)', itemStyle: { color: '#FF4D4F' } }
      ]
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const renderLineChart = () => {
  const chart = echarts.init(lineChartRef.value)
  const dates = ['03-01', '03-02', '03-03', '03-04', '03-05', '03-06', '03-07']
  const counts = [45, 62, 38, 55, 78, 52, 68]
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      smooth: true,
      data: counts,
      itemStyle: { color: '#00C4A1' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,196,161,0.3)' }, { offset: 1, color: 'rgba(0,196,161,0.05)' }]) }
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
.star-rating { color: #FAAD14; }
.star-rating .is-active { color: #FAAD14; }
.star-rating i { margin: 0 2px; }
</style>
