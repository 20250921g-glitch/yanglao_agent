<template>
  <div class="booking-board">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>预约看板</span>
          <div>
            <el-radio-group v-model="viewMode" size="small" style="margin-right:12px">
              <el-radio-button label="today">今日</el-radio-button>
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
            </el-radio-group>
            <el-button type="primary" @click="load">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 顶部 4 统计卡片（真实数据派生） -->
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="kpi-card kpi-blue">
            <div class="kpi-label">待接单</div>
            <div class="kpi-value">{{ stats.waiting }}</div>
            <div class="kpi-sub">等待派单</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="kpi-card kpi-green">
            <div class="kpi-label">已接单</div>
            <div class="kpi-value">{{ stats.accepted }}</div>
            <div class="kpi-sub">服务人员已确认</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="kpi-card kpi-orange">
            <div class="kpi-label">服务中</div>
            <div class="kpi-value">{{ stats.serving }}</div>
            <div class="kpi-sub">正在进行</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="kpi-card kpi-purple">
            <div class="kpi-label">今日已完成</div>
            <div class="kpi-value">{{ stats.completed }}</div>
            <div class="kpi-sub">已结单</div>
          </div>
        </el-col>
      </el-row>

      <!-- 筛选 -->
      <el-form :inline="true" style="margin-top:16px">
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width:140px">
            <el-option label="待接单" :value="1" />
            <el-option label="已接单" :value="2" />
            <el-option label="服务中" :value="3" />
            <el-option label="已完成" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="filter.elderName" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 时间线式调度看板 -->
      <el-row :gutter="16" style="margin-top:8px">
        <el-col :span="8" v-for="column in columns" :key="column.key">
          <div class="kanban-col" :style="{ borderTopColor: column.color }">
            <div class="kanban-col-header">
              <span>{{ column.label }}</span>
              <el-tag size="small" :type="column.tagType">{{ groupedOrders[column.key]?.length || 0 }}</el-tag>
            </div>
            <div class="kanban-col-body">
              <div v-for="order in groupedOrders[column.key]" :key="order.id" class="kanban-card" @click="goDetail(order)">
                <div class="kanban-card-title">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(order.appointmentTime) }}</span>
                  <el-tag size="small" :type="statusTag(order.status)" class="card-status">{{ statusText(order.status) }}</el-tag>
                </div>
                <div class="kanban-card-row"><b>{{ order.elderName }}</b> · {{ order.phone || '—' }}</div>
                <div class="kanban-card-row">服务：{{ order.serviceName || '—' }}</div>
                <div class="kanban-card-row">地址：{{ order.address || '—' }}</div>
                <div class="kanban-card-row" v-if="order.nurseName">
                  人员：<el-tag size="small">{{ order.nurseName }}</el-tag>
                </div>
                <div class="kanban-card-row" v-else>
                  <el-tag type="warning" size="small">未派单</el-tag>
                </div>
                <div class="kanban-card-actions">
                  <el-button v-if="order.status === 1 && !order.nurseName" type="primary" size="small" @click.stop="goAssign(order)">派单</el-button>
                  <el-button v-if="order.status === 2" type="success" size="small" @click.stop="handleStart(order)">开始服务</el-button>
                  <el-button v-if="order.status === 3" type="warning" size="small" @click.stop="handleComplete(order)">完成</el-button>
                </div>
              </div>
              <el-empty v-if="(groupedOrders[column.key] || []).length === 0" :image-size="60" description="暂无订单" />
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import { getServiceOrderPage, startService, completeService } from '@/api'

const router = useRouter()
const viewMode = ref('today')
const loading = ref(false)
const allOrders = ref([])

const filter = reactive({ status: '', elderName: '' })

const stats = reactive({ waiting: 0, accepted: 0, serving: 0, completed: 0 })

const statusTextMap = { 1: '待接单', 2: '已接单', 3: '服务中', 4: '已完成', 5: '已取消' }
const statusText = (s) => statusTextMap[s] || '未知'
const statusTag = (s) => ({ 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }[s] || 'info')

const columns = [
  { key: 'morning', label: '上午 (08:00-12:00)', color: '#00C4A1', tagType: 'primary' },
  { key: 'noon', label: '中午 (12:00-18:00)', color: '#52C41A', tagType: 'success' },
  { key: 'evening', label: '晚上 (18:00-22:00)', color: '#FAAD14', tagType: 'warning' }
]

const boardOrders = computed(() => {
  // 看板只展示进行中的预约（待接单 / 已接单 / 服务中）
  return allOrders.value.filter(o => [1, 2, 3].includes(o.status))
})

const groupedOrders = computed(() => {
  const groups = { morning: [], noon: [], evening: [] }
  boardOrders.value.forEach(o => {
    const t = o.appointmentTime ? new Date(o.appointmentTime) : null
    if (!t) { groups.morning.push(o); return }
    const h = t.getHours()
    if (h < 12) groups.morning.push(o)
    else if (h < 18) groups.noon.push(o)
    else groups.evening.push(o)
  })
  return groups
})

const load = async () => {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 200 }
    if (filter.status !== '') params.status = filter.status
    if (filter.elderName) params.elderName = filter.elderName
    const res = await getServiceOrderPage(params)
    allOrders.value = res.data.records || []
    // KPI 始终基于当前加载结果派生
    stats.waiting = allOrders.value.filter(o => o.status === 1).length
    stats.accepted = allOrders.value.filter(o => o.status === 2).length
    stats.serving = allOrders.value.filter(o => o.status === 3).length
    stats.completed = allOrders.value.filter(o => o.status === 4).length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const reset = () => { filter.status = ''; filter.elderName = ''; load() }

const formatTime = (t) => {
  if (!t) return '—'
  const d = new Date(t)
  return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const goDetail = (order) => router.push(`/service/order-detail/${order.id}`)
const goAssign = (order) => router.push(`/service/order-detail/${order.id}`)

const handleStart = async (order) => {
  try { await startService(order.id); ElMessage.success('已开始服务'); load() } catch (e) {}
}

const handleComplete = async (order) => {
  try {
    await ElMessageBox.confirm('确认完成此服务？', '提示', { type: 'warning' })
    await completeService(order.id); ElMessage.success('已完成'); load()
  } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.kpi-card { padding: 20px; border-radius: 8px; color: #fff; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.kpi-label { font-size: 14px; opacity: 0.9; }
.kpi-value { font-size: 32px; font-weight: bold; margin: 8px 0; }
.kpi-sub { font-size: 12px; opacity: 0.85; }
.kpi-blue { background: linear-gradient(135deg, #00C4A1, #00A889); }
.kpi-green { background: linear-gradient(135deg, #14B8A6, #0D9488); }
.kpi-orange { background: linear-gradient(135deg, #0EA5A4, #0D9488); }
.kpi-purple { background: linear-gradient(135deg, #2F8CFF, #1E63D6); }

.kanban-col { background: #f5f7fa; border-radius: 6px; margin-top: 12px; border-top: 4px solid #00C4A1; min-height: 400px; }
.kanban-col-header { padding: 12px 16px; font-weight: 600; display: flex; justify-content: space-between; align-items: center; background: #fff; border-bottom: 1px solid #ebeef5; }
.kanban-col-body { padding: 12px; max-height: 600px; overflow-y: auto; }
.kanban-card { background: #fff; border-radius: 4px; padding: 12px; margin-bottom: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); cursor: pointer; transition: all 0.2s; }
.kanban-card:hover { box-shadow: 0 4px 12px rgba(0,196,161,0.15); transform: translateY(-1px); }
.kanban-card-title { display: flex; align-items: center; gap: 6px; color: #00C4A1; font-weight: 600; margin-bottom: 8px; }
.card-status { color: inherit; }
.kanban-card-row { font-size: 13px; color: #606266; line-height: 1.8; }
.kanban-card-actions { margin-top: 8px; display: flex; gap: 4px; flex-wrap: wrap; }
</style>
