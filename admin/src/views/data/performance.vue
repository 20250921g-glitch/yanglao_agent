<template>
  <div class="page-container">
    <!-- 顶部筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="服务人员">
          <el-input v-model="filterForm.workerName" placeholder="请输入姓名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="filterForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background:#ecf5ff; color:#00C4A1">📋</div>
        <div class="stat-info">
          <div class="stat-label">总服务订单</div>
          <div class="stat-value">{{ stats.totalOrders }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#f0f9eb; color:#52C41A">⏱</div>
        <div class="stat-info">
          <div class="stat-label">总服务时长</div>
          <div class="stat-value">{{ stats.totalHours }}h</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#fef0f0; color:#FF4D4F">💰</div>
        <div class="stat-info">
          <div class="stat-label">总佣金收入</div>
          <div class="stat-value">¥{{ stats.totalCommission.toLocaleString() }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#fef9f3; color:#FAAD14">⭐</div>
        <div class="stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ stats.avgRating }}</div>
        </div>
      </div>
    </div>

    <!-- 业绩趋势图 -->
    <el-card class="trend-card" shadow="never">
      <div class="trend-header">
        <span class="trend-title">近7天业绩趋势（柱状图）</span>
      </div>
      <div class="bar-chart">
        <div v-for="(day, idx) in chartData" :key="idx" class="bar-col">
          <div class="bar-value">¥{{ day.amount }}</div>
          <div class="bar" :style="{ height: (day.amount / maxAmount * 180) + 'px', background: barColor(idx) }"></div>
          <div class="bar-label">{{ day.label }}</div>
        </div>
      </div>
    </el-card>

    <!-- 服务人员排行表 -->
    <el-card shadow="never">
      <el-table :data="tableData" stripe>
        <el-table-column label="排名" width="70">
          <template #default="{ $index }">
            <span :class="getRankClass($index)">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="服务人员" width="120" />
        <el-table-column prop="orders" label="服务订单数" width="120" />
        <el-table-column label="服务时长" width="100">
          <template #default="{ row }">{{ row.hours }}h</template>
        </el-table-column>
        <el-table-column label="佣金收入" width="130">
          <template #default="{ row }">
            <span style="color:#FF4D4F; font-weight:600">¥{{ row.commission.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户评分" width="130">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="服务人员详情" width="560px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.phone }}</el-descriptions-item>
        <el-descriptions-item label="服务订单数">{{ currentRow.orders }}</el-descriptions-item>
        <el-descriptions-item label="服务时长">{{ currentRow.hours }}h</el-descriptions-item>
        <el-descriptions-item label="佣金收入">
          <span style="color:#FF4D4F; font-weight:600">¥{{ currentRow.commission.toLocaleString() }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="客户评分">
          <el-rate v-model="currentRow.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="擅长领域" :span="2">{{ currentRow.skills }}</el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">{{ currentRow.bio }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPerformancePage } from '@/api/index'

const filterForm = ref({ workerName: '', dateRange: [] })
const detailVisible = ref(false)
const currentRow = ref(null)
const loading = ref(false)

const stats = ref({ totalOrders: 0, totalHours: 0, totalCommission: 0, avgRating: 0 })

const chartData = ref([
  { label: '07-02', amount: 0 },
  { label: '07-03', amount: 0 },
  { label: '07-04', amount: 0 },
  { label: '07-05', amount: 0 },
  { label: '07-06', amount: 0 },
  { label: '07-07', amount: 0 },
  { label: '07-08', amount: 0 }
])

const allData = ref([])
const tableData = ref([])

const maxAmount = computed(() => {
  const max = Math.max(...chartData.value.map(i => i.amount))
  return max || 1
})

const barColor = (idx) => idx === 4 ? '#00C4A1' : '#b3d8fd'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPerformancePage()
    const records = res.data.records || []
    
    stats.value.totalOrders = records.reduce((sum, item) => sum + (item.orders || 0), 0)
    stats.value.totalCommission = records.reduce((sum, item) => sum + (item.commission || 0), 0)
    stats.value.totalHours = Math.round(stats.value.totalCommission / 200)
    stats.value.avgRating = records.length > 0 ? Math.round(records.reduce((sum, item) => sum + (item.score || 0), 0) / records.length * 10) / 10 : 0
    
    chartData.value = records.slice(0, 7).map((item, idx) => ({
      label: `07-0${idx + 2}`,
      amount: item.commission || 0
    }))
    
    allData.value = records.map((item, idx) => ({
      id: idx + 1,
      name: item.staffName || '-',
      phone: '-',
      orders: item.orders || 0,
      hours: Math.round((item.amount || 0) / 100),
      commission: item.commission || 0,
      rating: item.score || 0,
      skills: item.department || '-',
      bio: '-'
    }))
    
    tableData.value = [...allData.value]
  } catch (e) {
    allData.value = []
    tableData.value = []
  }
  loading.value = false
}

onMounted(() => {
  loadData()
})

const getRankClass = (idx) => {
  if (idx === 0) return 'rank-gold'
  if (idx === 1) return 'rank-silver'
  if (idx === 2) return 'rank-bronze'
  return ''
}

const handleSearch = () => {
  tableData.value = allData.value.filter(i => !filterForm.value.workerName || i.name.includes(filterForm.value.workerName))
}

const handleReset = () => {
  filterForm.value = { workerName: '', dateRange: [] }
  tableData.value = [...allData.value]
}

const handleDetail = (row) => {
  currentRow.value = row
  detailVisible.value = true
}
</script>

<style scoped>
.page-container { padding: 16px; }
.filter-card { margin-bottom: 12px; }

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #ebeef5;
}

.stat-icon { font-size: 32px; border-radius: 10px; width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; }
.stat-label { font-size: 13px; color: #A0AEC0; margin-bottom: 4px; }
.stat-value { font-size: 22px; font-weight: 700; color: #303133; }

.trend-card { margin-bottom: 12px; }
.trend-header { margin-bottom: 16px; }
.trend-title { font-weight: 600; font-size: 15px; color: #303133; }

.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 240px;
  padding: 0 20px;
}

.bar-col { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.bar { width: 48px; border-radius: 4px 4px 0 0; transition: height 0.3s; }
.bar-value { font-size: 12px; color: #606266; font-weight: 500; }
.bar-label { font-size: 12px; color: #A0AEC0; }

.rank-gold { background: linear-gradient(135deg, #ffd700, #ffb300); color: #fff; border-radius: 50%; width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
.rank-silver { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); color: #fff; border-radius: 50%; width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
.rank-bronze { background: linear-gradient(135deg, #cd7f32, #b05c1e); color: #fff; border-radius: 50%; width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }
</style>
