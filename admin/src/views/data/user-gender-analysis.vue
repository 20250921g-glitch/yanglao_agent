<template>
  <div class="gender-analysis">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="background: #00C4A1">
          <div class="stat-value">{{ totalUsers }}</div>
          <div class="stat-title">用户总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #14B8A6">
          <div class="stat-value">{{ maleCount }}</div>
          <div class="stat-title">男性用户</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0EA5A4">
          <div class="stat-value">{{ femaleCount }}</div>
          <div class="stat-title">女性用户</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="background: #0891B2">
          <div class="stat-value">{{ gap }}</div>
          <div class="stat-title">性别差距</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card>
          <template #header><span class="card-title">用户性别分布饼图</span></template>
          <div class="chart-container" ref="pieChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span class="card-title">性别比例对比</span></template>
          <div class="chart-container" ref="barChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header><span class="card-title">性别统计详情</span></template>
      <el-table :data="genderData" stripe>
        <el-table-column prop="gender" label="性别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? 'primary' : 'danger'" size="small">{{ row.gender }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="人数" width="100" />
        <el-table-column label="占比" width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.percentage" :stroke-width="12" :color="row.gender === '男' ? '#00C4A1' : '#FF4D4F'" />
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
import { getUserGenderAnalysis } from '@/api'

const genderData = ref([])
const pieChartRef = ref(null)
const barChartRef = ref(null)

const totalUsers = ref(0)
const maleCount = ref(0)
const femaleCount = ref(0)
const gap = ref('')

const loadData = async () => {
  try {
    const res = await getUserGenderAnalysis()
    genderData.value = res.data || []
    maleCount.value = genderData.value.find(item => item.gender === '男')?.count || 0
    femaleCount.value = genderData.value.find(item => item.gender === '女')?.count || 0
    totalUsers.value = maleCount.value + femaleCount.value
    const diff = Math.abs(maleCount.value - femaleCount.value)
    const percent = totalUsers.value > 0 ? Math.round(diff / totalUsers.value * 100) : 0
    gap.value = `${maleCount.value > femaleCount.value ? '男' : '女'}性多${diff}人(${percent}%)`
    nextTick(() => { renderPieChart(); renderBarChart() })
  } catch {}
}

const renderPieChart = () => {
  const chart = echarts.init(pieChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [{
      name: '性别分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['55%', '50%'],
      data: [
        { value: maleCount.value, name: '男', itemStyle: { color: '#00C4A1' } },
        { value: femaleCount.value, name: '女', itemStyle: { color: '#FF4D4F' } }
      ],
      label: { fontSize: 14 }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const renderBarChart = () => {
  const chart = echarts.init(barChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: ['男', '女'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [
        { value: maleCount.value, itemStyle: { color: '#00C4A1' } },
        { value: femaleCount.value, itemStyle: { color: '#FF4D4F' } }
      ],
      barWidth: '50%'
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
