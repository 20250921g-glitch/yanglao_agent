<template>
  <div class="age-analysis">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="background: #00C4A1">
          <div class="stat-value">{{ totalUsers }}</div>
          <div class="stat-title">用户总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #14B8A6">
          <div class="stat-value">{{ avgAge }}</div>
          <div class="stat-title">平均年龄</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0EA5A4">
          <div class="stat-value">{{ mainGroup }}</div>
          <div class="stat-title">主要年龄组</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #00C4A1">
          <div class="stat-value">{{ oldestPercent }}%</div>
          <div class="stat-title">高龄用户占比</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card>
          <template #header><span class="card-title">用户年龄分布柱状图</span></template>
          <div class="chart-container" ref="barChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span class="card-title">用户年龄分布饼图</span></template>
          <div class="chart-container" ref="pieChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header><span class="card-title">年龄分组详情</span></template>
      <el-table :data="ageData" stripe>
        <el-table-column prop="ageGroup" label="年龄组" width="120" />
        <el-table-column prop="count" label="人数" width="100" />
        <el-table-column label="占比" width="100">
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
import { getUserAgeAnalysis } from '@/api'

const ageData = ref([])
const barChartRef = ref(null)
const pieChartRef = ref(null)

const totalUsers = ref(0)
const avgAge = ref(0)
const mainGroup = ref('')
const oldestPercent = ref(0)

const loadData = async () => {
  try {
    const res = await getUserAgeAnalysis()
    ageData.value = res.data || []
    totalUsers.value = ageData.value.reduce((sum, item) => sum + item.count, 0)
    avgAge.value = Math.round(totalUsers.value > 0 ? ageData.value.reduce((sum, item) => {
      const ages = item.ageGroup.replace('岁', '').split('-')
      const avg = (parseInt(ages[0]) + (ages[1] ? parseInt(ages[1]) : parseInt(ages[0]) + 5)) / 2
      return sum + avg * item.count
    }, 0) / totalUsers.value : 0)
    const maxItem = ageData.value.reduce((max, item) => item.count > max.count ? item : max, ageData.value[0])
    mainGroup.value = maxItem?.ageGroup || ''
    const oldest = ageData.value.find(item => item.ageGroup.includes('80'))
    oldestPercent.value = oldest?.percentage || 0
    nextTick(() => { renderBarChart(); renderPieChart() })
  } catch {}
}

const renderBarChart = () => {
  const chart = echarts.init(barChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ageData.value.map(item => item.ageGroup), axisLabel: { interval: 0 } },
    yAxis: { type: 'value' },
    series: [{
      name: '人数',
      type: 'bar',
      data: ageData.value.map(item => item.count),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#00C4A1' }, { offset: 1, color: '#66B1FF' }]) }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const renderPieChart = () => {
  const chart = echarts.init(pieChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '年龄分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['55%', '50%'],
      data: ageData.value.map((item, i) => ({
        value: item.count, name: item.ageGroup,
        itemStyle: { color: ['#00C4A1', '#52C41A', '#FAAD14', '#FF4D4F', '#00C4A1'][i] }
      }))
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
