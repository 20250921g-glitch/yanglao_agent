<template>
  <div class="dashboard">
    <!-- Row 1: 四个统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <div class="stat-card" :style="{ background: card.color }">
          <div class="stat-left">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
            <div class="stat-change" :class="card.trend">
              {{ card.change >= 0 ? '+' : '' }}{{ card.change }}% {{ card.label }}
            </div>
          </div>
          <div class="stat-icon">
            <el-icon :size="48"><component :is="card.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Row 2: 快捷入口 + 用户标签分布 + 服务类型占比 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <!-- 快捷入口 -->
      <el-col :span="6">
        <el-card class="quick-card">
          <template #header>
            <span class="card-title">快捷入口</span>
          </template>
          <div class="quick-grid">
            <div class="quick-item" v-for="item in quickLinks" :key="item.name" @click="navigateTo(item.path)">
              <div class="quick-icon" :style="{ background: item.color }">
                <el-icon :size="24"><component :is="item.icon" /></el-icon>
              </div>
              <div class="quick-name">{{ item.name }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 用户标签分布 -->
      <el-col :span="9">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">用户标签分布</span>
          </template>
          <div class="chart-container" ref="tagChartRef"></div>
        </el-card>
      </el-col>

      <!-- 各服务类型商品订单量占比 -->
      <el-col :span="9">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">各服务类型商品订单量占比</span>
          </template>
          <div class="chart-container" ref="pieChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 3: 用户趋势 + 商品TOP5 + 服务人员TOP5 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <!-- 用户趋势统计 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">用户趋势统计</span>
          </template>
          <div class="trend-chart" ref="trendChartRef"></div>
        </el-card>
      </el-col>

      <!-- 支付榜TOP5商品排行 -->
      <el-col :span="6">
        <el-card class="top-card">
          <template #header>
            <span class="card-title">支付榜TOP5商品排行</span>
          </template>
          <div class="top-list">
            <div class="top-item" v-for="(item, idx) in productTop" :key="idx">
              <span class="rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
              <span class="name">{{ item.product_name }}</span>
              <span class="count">{{ item.order_count }}单</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 服务人员业绩TOP5排行 -->
      <el-col :span="6">
        <el-card class="top-card">
          <template #header>
            <span class="card-title">服务人员业绩TOP5排行</span>
          </template>
          <div class="top-list">
            <div class="top-item" v-for="(item, idx) in workerTop" :key="idx">
              <span class="rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
              <div class="worker-info">
                <span class="name">{{ item.nurse_name }}</span>
                <span class="type">{{ item.service_type }}</span>
              </div>
              <span class="count">{{ item.order_count }}单</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats, getStatCards, getTagDistribution, getServiceTypeDistribution, 
         getUserTrend, getProductTop, getWorkerTop } from '@/api'
import * as echarts from 'echarts'

const router = useRouter()

const statCards = ref([
  { title: '新增用户数量', value: 0, change: 0, icon: 'User', color: 'linear-gradient(135deg, #00C4A1 0%, #00A889 100%)', label: '较上周', trend: 'up' },
  { title: '新增工单数量', value: 0, change: 0, icon: 'Document', color: 'linear-gradient(135deg, #2DD4BF 0%, #14B8A6 100%)', label: '较上周', trend: 'up' },
  { title: '新增订单数量', value: 0, change: 0, icon: 'ShoppingCart', color: 'linear-gradient(135deg, #0EA5A4 0%, #0D9488 100%)', label: '较上周', trend: 'up' },
  { title: '新增动态数量', value: 0, change: 0, icon: 'Bell', color: 'linear-gradient(135deg, #0891B2 0%, #0E7490 100%)', label: '较昨日', trend: 'up' }
])

const quickLinks = [
  { name: '全部用户', icon: 'User', color: 'linear-gradient(135deg, #00C4A1, #00A889)', path: '/user/list' },
  { name: '报告管理', icon: 'Document', color: 'linear-gradient(135deg, #2DD4BF, #14B8A6)', path: '/user/report' },
  { name: '会话', icon: 'ChatDotRound', color: 'linear-gradient(135deg, #0EA5A4, #0D9488)', path: '/user/conversation' },
  { name: '全部订单', icon: 'Tickets', color: 'linear-gradient(135deg, #0891B2, #0E7490)', path: '/trade/product-order' },
  { name: '工单管理', icon: 'Suitcase', color: 'linear-gradient(135deg, #00C4A1, #00A889)', path: '/service/order' },
  { name: '审核管理', icon: 'Stamp', color: 'linear-gradient(135deg, #2DD4BF, #14B8A6)', path: '/service/audit' },
  { name: '售后管理', icon: 'Box', color: 'linear-gradient(135deg, #0EA5A4, #0D9488)', path: '/trade/refund' },
  { name: '动态管理', icon: 'ChatLineSquare', color: 'linear-gradient(135deg, #0891B2, #0E7490)', path: '/operation/dynamic' }
]

const tagData = ref([])
const pieData = ref([])
const userTrend = ref([])
const productTop = ref([])
const workerTop = ref([])

const tagChartRef = ref(null)
const pieChartRef = ref(null)
const trendChartRef = ref(null)

const navigateTo = (path) => {
  router.push(path)
}

onMounted(async () => {
  try {
    // 获取统计卡片数据
    const cardsRes = await getStatCards()
    if (cardsRes.data) {
      statCards.value = cardsRes.data
    }

    // 用户标签分布
    const tagRes = await getTagDistribution()
    tagData.value = tagRes.data || []
    
    // 服务类型分布
    const pieRes = await getServiceTypeDistribution()
    pieData.value = pieRes.data || []

    // 用户趋势
    const trendRes = await getUserTrend()
    userTrend.value = trendRes.data || []

    // 商品TOP5
    const productRes = await getProductTop()
    productTop.value = productRes.data || []

    // 服务人员TOP5
    const workerRes = await getWorkerTop()
    workerTop.value = workerRes.data || []

    await nextTick()
    renderCharts()
  } catch (e) {
    console.error('Dashboard load error:', e)
  }
})

const adjustColor = (color, amount) => {
  const hex = color.replace('#', '')
  const num = parseInt(hex, 16)
  const r = Math.max(0, Math.min(255, (num >> 16) + amount))
  const g = Math.max(0, Math.min(255, ((num >> 8) & 0x00FF) + amount))
  const b = Math.max(0, Math.min(255, (num & 0x0000FF) + amount))
  return `#${((1 << 24) | (r << 16) | (g << 8) | b).toString(16).slice(1)}`
}

const renderCharts = () => {
  // 标签分布横向柱状图
  if (tagChartRef.value && tagData.value.length > 0) {
    const tagChart = echarts.init(tagChartRef.value)
    const colors = ['#00C4A1', '#2DD4BF', '#14B8A6', '#0EA5A4', '#0891B2', '#5EEAD4', '#0D9488', '#99E0D8']
    tagChart.setOption({
      tooltip: { 
        trigger: 'axis', 
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#E2E8F0',
        borderWidth: 1,
        textStyle: { color: '#4A5568' },
        extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); border-radius: 8px;'
      },
      grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
      xAxis: { 
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#EDF2F7', type: 'dashed' } },
        axisLabel: { color: '#718096' }
      },
      yAxis: { 
        type: 'category', 
        data: tagData.value.map(i => i.tagName),
        axisLabel: { color: '#4A5568' },
        axisLine: { lineStyle: { color: '#E2E8F0' } }
      },
      series: [{
        type: 'bar',
        data: tagData.value.map((i, idx) => ({
          value: i.count,
          itemStyle: { 
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: colors[idx % colors.length] },
              { offset: 1, color: adjustColor(colors[idx % colors.length], -20) }
            ]),
            borderRadius: [0, 6, 6, 0]
          }
        })),
        barWidth: 18,
        label: { show: true, position: 'right', color: '#718096', fontWeight: '500' }
      }]
    })
  }

  // 服务类型饼图
  if (pieChartRef.value && pieData.value.length > 0) {
    const pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { 
        trigger: 'item', 
        formatter: '{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#E2E8F0',
        borderWidth: 1,
        textStyle: { color: '#4A5568' },
        extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); border-radius: 8px;'
      },
      legend: { 
        bottom: '5%', 
        left: 'center',
        textStyle: { color: '#4A5568' },
        itemWidth: 12,
        itemHeight: 12,
        itemGap: 16
      },
      series: [{
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: { 
          label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#4A5568' },
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.1)' }
        },
        labelLine: { show: false },
        data: pieData.value.map(i => ({
          value: i.value,
          name: i.name,
          itemStyle: { color: i.color || '#00C4A1' }
        }))
      }]
    })
  }

  // 用户趋势面积图
  if (trendChartRef.value && userTrend.value.length > 0) {
    const trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { 
        trigger: 'axis',
        formatter: (params) => {
          const d = params[0]
          return `${d.name}<br/>新增用户: <strong>${d.value}</strong>`
        },
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#E2E8F0',
        borderWidth: 1,
        textStyle: { color: '#4A5568' },
        extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); border-radius: 8px;'
      },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
      xAxis: { 
        type: 'category', 
        boundaryGap: false,
        data: userTrend.value.map(i => i.date),
        axisLine: { lineStyle: { color: '#E2E8F0' } },
        axisLabel: { color: '#718096' }
      },
      yAxis: { 
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#EDF2F7', type: 'dashed' } },
        axisLabel: { color: '#718096' }
      },
      series: [{
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        data: userTrend.value.map(i => i.count),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 196, 161, 0.4)' },
            { offset: 1, color: 'rgba(0, 196, 161, 0.05)' }
          ])
        },
        lineStyle: { color: '#00C4A1', width: 3 },
        itemStyle: { color: '#00C4A1', borderWidth: 2, borderColor: '#fff' }
      }]
    })
  }
}
</script>

<style scoped>
.dashboard { padding: 0; }

/* 统计卡片 */
.stat-row { margin-bottom: 0; }
.stat-card {
  border-radius: 16px;
  padding: 24px 28px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  position: relative;
  overflow: hidden;
}
.stat-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
}
.stat-left { flex: 1; position: relative; z-index: 1; }
.stat-value { font-size: 40px; font-weight: 700; letter-spacing: -1px; }
.stat-title { font-size: 15px; opacity: 0.9; margin-top: 6px; font-weight: 500; }
.stat-change { font-size: 13px; margin-top: 10px; opacity: 0.85; font-weight: 500; }
.stat-change.up::before { content: '↑ '; }
.stat-change.down { color: #fff; opacity: 0.7; }
.stat-change.down::before { content: '↓ '; }
.stat-icon { opacity: 0.25; position: relative; z-index: 1; transform: scale(1.1); }

/* 快捷入口 */
.quick-card { height: 100%; }
.card-title { font-weight: 500; font-size: 14px; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.quick-item { cursor: pointer; text-align: center; }
.quick-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.quick-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}
.quick-name { font-size: 12px; color: #606266; }

/* 图表卡片 */
.chart-card { height: 280px; }
.chart-container { height: 200px; }
.trend-chart { height: 240px; }

/* TOP5列表 */
.top-card { height: 280px; }
.top-list { padding: 0 8px; }
.top-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}
.top-item:last-child { border-bottom: none; }
.rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  background: #A0AEC0;
  margin-right: 12px;
  flex-shrink: 0;
}
.rank-1 { background: #FF6B6B; }
.rank-2 { background: #FFA94D; }
.rank-3 { background: #FFD93D; color: #333; }
.top-item .name { flex: 1; font-size: 14px; color: #303133; }
.top-item .count { font-size: 14px; color: #00C4A1; font-weight: 500; }
.worker-info { flex: 1; display: flex; flex-direction: column; }
.worker-info .name { font-size: 14px; color: #303133; }
.worker-info .type { font-size: 12px; color: #A0AEC0; margin-top: 2px; }
</style>
