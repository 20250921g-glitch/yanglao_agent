<template>
  <div class="points-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分/成长值管理</span>
          <div class="type-tabs">
            <el-radio-group v-model="tabType" @change="loadData">
              <el-radio-button value="points">积分管理</el-radio-button>
              <el-radio-button value="growth">成长值管理</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="用户昵称">
          <el-input v-model="search.userName" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="search.phone" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="search.type" placeholder="请选择" clearable style="width:140px">
            <el-option value="earn">获取</el-option>
            <el-option value="spend">消耗</el-option>
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

      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon total"><el-icon :size="32" name="Coin" /></div>
          <div class="stat-info">
            <div class="stat-label">总{{ tabType === 'points' ? '积分' : '成长值' }}</div>
            <div class="stat-value">{{ stats.total }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon earn"><el-icon :size="32" name="Plus" /></div>
          <div class="stat-info">
            <div class="stat-label">本期获取</div>
            <div class="stat-value">{{ stats.earned }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon spend"><el-icon :size="32" name="Minus" /></div>
          <div class="stat-info">
            <div class="stat-label">本期消耗</div>
            <div class="stat-value">{{ stats.spent }}</div>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card">
          <div class="chart-title">{{ tabType === 'points' ? '积分' : '成长值' }}趋势</div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-title">获取来源分布</div>
          <div ref="sourceChartRef" class="chart-container"></div>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userName" label="用户昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="type" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'earn' ? 'success' : 'danger'">
              {{ row.type === 'earn' ? '获取' : '消耗' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="amount" label="数量" width="100">
          <template #default="{ row }">
            <span :class="row.type === 'earn' ? 'earn-amount' : 'spend-amount'">
              {{ row.type === 'earn' ? '+' : '-' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="100" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="手动调整{{ tabType === 'points' ? '积分' : '成长值' }}" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户" prop="userId">
          <el-select v-model="form.userId" placeholder="请选择用户" style="width:100%" filterable>
            <el-option v-for="u in userList" :key="u.id" :label="`${u.realName || u.username} - ${u.phone}`" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="earn">增加</el-radio>
            <el-radio value="spend">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量" prop="amount">
          <el-input-number v-model="form.amount" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-input v-model="form.source" placeholder="如：管理员手动调整" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="btnLoading">确认调整</el-button>
      </template>
    </el-dialog>

    <div class="btn-bar">
      <el-button type="primary" @click="openDialog()">手动调整{{ tabType === 'points' ? '积分' : '成长值' }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAppUserPage } from '@/api'
import * as echarts from 'echarts'

const tabType = ref('points')
const loading = ref(false)
const btnLoading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const trendChartRef = ref(null)
const sourceChartRef = ref(null)
let trendChartInstance = null
let sourceChartInstance = null

const search = reactive({
  userName: '',
  phone: '',
  type: '',
  dateRange: null
})

const form = reactive({
  userId: null,
  type: 'earn',
  amount: 0,
  source: '',
  remark: ''
})

const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  type: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  source: [{ required: true, message: '请输入来源', trigger: 'blur' }]
}

const stats = computed(() => {
  const data = tableData.value
  const earned = data.filter(i => i.type === 'earn').reduce((sum, i) => sum + (i.amount || 0), 0)
  const spent = data.filter(i => i.type === 'spend').reduce((sum, i) => sum + (i.amount || 0), 0)
  return {
    total: data.length > 0 ? data[0]?.balance || 0 : 0,
    earned,
    spent
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: tabType.value
    }
    if (search.userName) params.userName = search.userName
    if (search.phone) params.phone = search.phone
    if (search.type) params.operationType = search.type
    if (search.dateRange && search.dateRange.length === 2) {
      params.startDate = search.dateRange[0]
      params.endDate = search.dateRange[1]
    }
    const res = await getAppUserPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  search.userName = ''
  search.phone = ''
  search.type = ''
  search.dateRange = null
  pageNum.value = 1
  loadData()
}

const loadUsers = async () => {
  try {
    const res = await getAppUserPage({ pageNum: 1, pageSize: 50 })
    userList.value = res.data.records || []
  } catch (e) {}
}

const renderCharts = () => {
  renderTrendChart()
  renderSourceChart()
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChartInstance) trendChartInstance.dispose()
  trendChartInstance = echarts.init(trendChartRef.value)
  const dates = ['1月', '2月', '3月', '4月', '5月', '6月']
  const earnData = [1200, 1500, 1800, 1300, 2000, 1600]
  const spendData = [500, 800, 600, 900, 700, 850]
  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['获取', '消耗'] },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '获取', type: 'line', smooth: true, data: earnData, lineStyle: { color: '#4caf50' }, itemStyle: { color: '#4caf50' } },
      { name: '消耗', type: 'line', smooth: true, data: spendData, lineStyle: { color: '#FF4D4F' }, itemStyle: { color: '#FF4D4F' } }
    ]
  })
}

const renderSourceChart = () => {
  if (!sourceChartRef.value) return
  if (sourceChartInstance) sourceChartInstance.dispose()
  sourceChartInstance = echarts.init(sourceChartRef.value)
  sourceChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      data: [
        { value: 35, name: '签到' },
        { value: 25, name: '完成订单' },
        { value: 20, name: '邀请好友' },
        { value: 15, name: '活动奖励' },
        { value: 5, name: '其他' }
      ],
      itemStyle: { borderRadius: 4 }
    }]
  })
}

const openDialog = async () => {
  formRef.value?.resetFields()
  Object.assign(form, { userId: null, type: 'earn', amount: 0, source: '', remark: '' })
  await loadUsers()
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  btnLoading.value = true
  try {
    ElMessage.success(`${tabType.value === 'points' ? '积分' : '成长值'}调整成功`)
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    btnLoading.value = false
  }
}

watch(tabType, () => {
  pageNum.value = 1
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.points-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.type-tabs { display: flex; gap: 0; }
.search-form { margin-bottom: 20px; }
.btn-bar { margin-bottom: 16px; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.stat-icon.total { background: rgba(0,196,161, 0.1); color: #00C4A1; }
.stat-icon.earn { background: rgba(76, 175, 80, 0.1); color: #4caf50; }
.stat-icon.spend { background: rgba(255,77,79, 0.1); color: #FF4D4F; }

.stat-info { flex: 1; }
.stat-label { font-size: 12px; color: #A0AEC0; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; }

.chart-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card { padding: 16px; background: #fff; border-radius: 8px; border: 1px solid #ebeef5; }
.chart-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.chart-container { height: 250px; }

.earn-amount { color: #4caf50; font-weight: 600; }
.spend-amount { color: #FF4D4F; font-weight: 600; }
</style>