<template>
  <div class="health-data-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康数据详情</span>
          <div class="type-tabs">
            <el-radio-group v-model="recordType" @change="loadData">
              <el-radio-button value="体重">体重</el-radio-button>
              <el-radio-button value="步数">步数</el-radio-button>
              <el-radio-button value="睡眠">睡眠</el-radio-button>
              <el-radio-button value="血糖">血糖</el-radio-button>
              <el-radio-button value="血压">血压</el-radio-button>
              <el-radio-button value="血氧饱和度">血氧饱和度</el-radio-button>
              <el-radio-button value="心率">心率</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="老人姓名">
          <el-select v-model="search.elderId" placeholder="请选择老人" style="width:200px" clearable @change="loadData">
            <el-option v-for="e in elderList" :key="e.id" :value="e.id" :label="e.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="search.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:280px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="stats-row" v-if="stats">
        <div class="stat-card">
          <div class="stat-icon weight"><el-icon :size="32" name="Scale" /></div>
          <div class="stat-info">
            <div class="stat-label">最近记录</div>
            <div class="stat-value">{{ stats.latestValue }}<span class="stat-unit">{{ unit }}</span></div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon avg"><el-icon :size="32" name="TrendCharts" /></div>
          <div class="stat-info">
            <div class="stat-label">平均值</div>
            <div class="stat-value">{{ stats.avgValue }}<span class="stat-unit">{{ unit }}</span></div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon max"><el-icon :size="32" name="ArrowUp" /></div>
          <div class="stat-info">
            <div class="stat-label">最高值</div>
            <div class="stat-value">{{ stats.maxValue }}<span class="stat-unit">{{ unit }}</span></div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon min"><el-icon :size="32" name="ArrowDown" /></div>
          <div class="stat-info">
            <div class="stat-label">最低值</div>
            <div class="stat-value">{{ stats.minValue }}<span class="stat-unit">{{ unit }}</span></div>
          </div>
        </div>
      </div>

      <div class="chart-section">
        <div class="chart-card">
          <div class="chart-title">{{ recordType }}趋势图</div>
          <div ref="chartRef" class="chart-container"></div>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="elderName" label="老人姓名" width="120" />
        <el-table-column prop="recordValue" label="记录值" width="150">
          <template #default="{ row }">{{ row.recordValue }}{{ unit }}</template>
        </el-table-column>
        <el-table-column prop="recordTime" label="记录时间" width="180" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>

      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecordPage, getElderList } from '@/api'
import * as echarts from 'echarts'

const recordType = ref('体重')
const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const elderList = ref([])
const chartRef = ref(null)
let chartInstance = null

const search = reactive({
  elderId: null,
  dateRange: null
})

const unitMap = {
  '体重': 'kg',
  '步数': '步',
  '睡眠': '小时',
  '血糖': 'mmol/L',
  '血压': 'mmHg',
  '血氧饱和度': '%',
  '心率': '次/分'
}

const unit = computed(() => unitMap[recordType.value] || '')

const stats = computed(() => {
  if (!tableData.value.length) return null
  const values = tableData.value.map(item => parseFloat(item.recordValue)).filter(v => !isNaN(v))
  if (!values.length) return null
  return {
    latestValue: tableData.value[0]?.recordValue || '-',
    avgValue: (values.reduce((a, b) => a + b, 0) / values.length).toFixed(1),
    maxValue: Math.max(...values).toFixed(1),
    minValue: Math.min(...values).toFixed(1)
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      recordType: recordType.value
    }
    if (search.elderId) params.elderId = search.elderId
    if (search.dateRange && search.dateRange.length === 2) {
      params.startDate = search.dateRange[0]
      params.endDate = search.dateRange[1]
    }
    const res = await getRecordPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  search.elderId = null
  search.dateRange = null
  pageNum.value = 1
  loadData()
}

const loadElders = async () => {
  try {
    const res = await getElderList()
    elderList.value = res.data || []
  } catch (e) {}
}

const renderChart = () => {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)
  const data = tableData.value.slice(0, 10).reverse()
  const dates = data.map(item => item.recordTime?.substring(5, 16) || '-')
  const values = data.map(item => parseFloat(item.recordValue) || 0)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: 30, color: '#606266', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#606266', formatter: `{value}${unit.value}` }
    },
    series: [{
      name: recordType.value,
      type: 'line',
      smooth: true,
      data: values,
      lineStyle: { color: '#4caf50', width: 3 },
      itemStyle: { color: '#4caf50' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(76, 175, 80, 0.5)' },
          { offset: 1, color: 'rgba(76, 175, 80, 0.1)' }
        ])
      }
    }]
  })
}

watch(recordType, () => {
  pageNum.value = 1
  loadData()
})

onMounted(() => {
  loadElders()
  loadData()
})
</script>

<style scoped>
.health-data-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.type-tabs { display: flex; gap: 0; }
.search-form { margin-bottom: 20px; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8faf9;
  border-radius: 8px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.weight { background: rgba(76, 175, 80, 0.1); color: #4caf50; }
.stat-icon.avg { background: rgba(0,196,161, 0.1); color: #00C4A1; }
.stat-icon.max { background: rgba(255,77,79, 0.1); color: #FF4D4F; }
.stat-icon.min { background: rgba(250,173,20, 0.1); color: #FAAD14; }

.stat-info { flex: 1; }
.stat-label { font-size: 12px; color: #A0AEC0; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; }
.stat-unit { font-size: 14px; color: #A0AEC0; font-weight: normal; margin-left: 4px; }

.chart-section { margin-bottom: 20px; }
.chart-card { padding: 16px; background: #fff; border-radius: 8px; border: 1px solid #ebeef5; }
.chart-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.chart-container { height: 300px; }
</style>