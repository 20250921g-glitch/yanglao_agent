<template>
  <div class="service-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- ===== 服务项目 ===== -->
      <el-tab-pane label="服务项目" name="projects">
        <el-form :inline="true" :model="search" class="filter-bar">
          <el-form-item label="分类">
            <el-select v-model="search.category" placeholder="全部" clearable style="width:160px" @change="loadProjects">
              <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="search.keyword" placeholder="服务名称" clearable style="width:180px" @keyup.enter="loadProjects" @clear="loadProjects" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadProjects">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <div v-loading="loading" class="card-wrap">
          <el-empty v-if="!loading && projects.length === 0" description="暂无可预约的服务项目" />
          <el-card v-for="p in projects" :key="p.id" class="svc-card" shadow="hover">
            <div class="svc-cover" :style="{ background: catColor[p.category] || '#00C4A1' }">
              {{ p.category ? p.category.charAt(0) : '服' }}
            </div>
            <div class="svc-body">
              <div class="svc-name">{{ p.name }}</div>
              <div class="svc-meta">
                <el-tag size="small" effect="plain">{{ p.category }}</el-tag>
                <span class="dot">·</span>{{ p.method || '上门服务' }}
                <span class="dot">·</span>{{ p.duration || 0 }} 分钟
              </div>
              <div class="svc-foot">
                <span class="svc-price">¥{{ Number(p.price).toFixed(2) }}</span>
                <el-button type="primary" size="small" @click="openBook(p)">立即预约</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- ===== 我的服务订单 ===== -->
      <el-tab-pane label="我的服务" name="mine">
        <div class="mine-toolbar">
          <el-select v-model="myStatus" placeholder="全部状态" clearable style="width:150px" @change="reloadMine">
            <el-option v-for="(t, k) in orderStatusText" :key="k" :label="t" :value="Number(k)" />
          </el-select>
        </div>
        <el-table :data="myOrders" stripe v-loading="myLoading" style="margin-top:8px">
          <template #empty><el-empty description="您还没有预约任何服务" /></template>
          <el-table-column prop="orderNo" label="订单号" min-width="170" />
          <el-table-column prop="serviceName" label="服务项目" min-width="140" />
          <el-table-column prop="elderName" label="服务对象" width="110" />
          <el-table-column label="预约时间" min-width="160">
            <template #default="{ row }">{{ fmt(row.appointmentTime) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">¥{{ row.price != null ? Number(row.price).toFixed(2) : '0.00' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="orderStatusTag[row.status]" size="small">{{ orderStatusText[row.status] }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 1 || row.status === 2" type="danger" size="small" plain @click="doCancel(row)">取消</el-button>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="myPageNum" v-model:page-size="myPageSize"
          :total="myTotal" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
          style="margin-top:16px;justify-content:flex-end" @change="loadMine" />
      </el-tab-pane>
    </el-tabs>

    <!-- 预约对话框 -->
    <el-dialog v-model="bookVisible" title="预约服务" width="520px">
      <div v-if="current" class="book-preview">
        <div class="bp-title">{{ current.name }}</div>
        <div class="bp-meta">{{ current.category }} · {{ current.method || '上门服务' }} · {{ current.duration || 0 }} 分钟</div>
        <div class="bp-price">¥{{ Number(current.price).toFixed(2) }}</div>
      </div>
      <el-form label-width="90px" style="margin-top:14px">
        <el-form-item label="服务对象" required>
          <el-select v-model="bookForm.elderId" placeholder="请选择老人" style="width:100%">
            <el-option v-for="e in elders" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间" required>
          <el-date-picker
            v-model="bookForm.appointmentTime" type="datetime"
            placeholder="选择预约时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm"
            style="width:100%" :disabled-date="disabledDate"
            :disabled-hours="disabledHours" :disabled-minutes="disabledMinutes" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bookForm.remark" type="textarea" :rows="3" placeholder="如有特殊需求可在此说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBook">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getServiceProjects, getServiceElders, createServiceOrder, myServiceOrders, cancelServiceOrder
} from '@/api'

const categoryOptions = ['康复理疗', '中医推拿', '运动康复', '居家护理', '健康体检']
const catColor = { '康复理疗': '#00C4A1', '中医推拿': '#E6A23C', '运动康复': '#409EFF', '居家护理': '#F56C6C', '健康体检': '#909399' }
// service_order status: 1待接单 2已接单 3服务中 4已完成 5已取消
const orderStatusText = { 1: '待接单', 2: '已接单', 3: '服务中', 4: '已完成', 5: '已取消' }
const orderStatusTag = { 1: 'warning', 2: 'primary', 3: '', 4: 'success', 5: 'info' }

const activeTab = ref('projects')

const loading = ref(false)
const projects = ref([])
const search = reactive({ category: '', keyword: '' })

const elders = ref([])
const bookVisible = ref(false)
const current = ref(null)
const bookForm = reactive({ elderId: null, appointmentTime: '', remark: '' })
const submitting = ref(false)

const myLoading = ref(false)
const myOrders = ref([])
const myStatus = ref('')
const myPageNum = ref(1)
const myPageSize = ref(10)
const myTotal = ref(0)

const fmt = (d) => {
  if (!d) return '—'
  return String(d).replace('T', ' ').substring(0, 16)
}

const loadProjects = async () => {
  loading.value = true
  try {
    const res = await getServiceProjects({
      category: search.category || undefined,
      keyword: search.keyword || undefined
    })
    projects.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载服务项目失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  search.category = ''
  search.keyword = ''
  loadProjects()
}

const openBook = async (p) => {
  current.value = p
  bookForm.elderId = null
  bookForm.appointmentTime = ''
  bookForm.remark = ''
  if (elders.value.length === 0) {
    try {
      const res = await getServiceElders()
      elders.value = res.data || []
    } catch (e) { /* 忽略 */ }
  }
  if (elders.value.length === 1) {
    bookForm.elderId = elders.value[0].id
  }
  bookVisible.value = true
}

const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

// 仅当面板日期为今天时，禁用已过去的小时/分钟（过去日期已被 disabledDate 屏蔽）
const disabledHours = (role, comparingDate) => {
  const arr = []
  const now = new Date()
  const d = comparingDate && comparingDate.toDate ? comparingDate.toDate() : now
  if (d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()) {
    for (let h = 0; h < now.getHours(); h++) arr.push(h)
  }
  return arr
}
const disabledMinutes = (hour, role, comparingDate) => {
  const arr = []
  const now = new Date()
  const d = comparingDate && comparingDate.toDate ? comparingDate.toDate() : now
  if (d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate() && hour === now.getHours()) {
    for (let m = 0; m < now.getMinutes(); m++) arr.push(m)
  }
  return arr
}

const submitBook = async () => {
  if (!bookForm.elderId) {
    ElMessage.warning('请选择服务对象')
    return
  }
  if (!bookForm.appointmentTime) {
    ElMessage.warning('请选择预约时间')
    return
  }
  submitting.value = true
  try {
    await createServiceOrder({
      projectId: current.value.id,
      elderId: bookForm.elderId,
      appointmentTime: bookForm.appointmentTime,
      remark: bookForm.remark
    })
    ElMessage.success('预约成功，等待服务人员接单')
    bookVisible.value = false
    activeTab.value = 'mine'
    reloadMine()
  } catch (e) {
    ElMessage.error(e.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

const loadMine = async () => {
  myLoading.value = true
  try {
    const res = await myServiceOrders({
      pageNum: myPageNum.value,
      pageSize: myPageSize.value,
      status: myStatus.value === '' ? undefined : myStatus.value
    })
    myOrders.value = res.data.records || []
    myTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载我的服务失败')
  } finally {
    myLoading.value = false
  }
}

const reloadMine = () => {
  myPageNum.value = 1
  loadMine()
}

const doCancel = async (row) => {
  await ElMessageBox.confirm(`确认取消服务「${row.serviceName}」？`, '提示', { type: 'warning' })
  try {
    await cancelServiceOrder(row.id)
    ElMessage.success('已取消')
    loadMine()
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  }
}

const onTabChange = (name) => {
  if (name === 'mine') loadMine()
}

onMounted(loadProjects)
</script>

<style scoped>
.service-page { padding: 4px; }
.filter-bar { margin-bottom: 8px; background: #fafafa; padding: 12px; border-radius: 8px; }
.card-wrap { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; min-height: 120px; }
.svc-card { border-radius: 10px; overflow: hidden; }
.svc-cover { height: 96px; display: flex; align-items: center; justify-content: center; font-size: 36px; color: #fff; font-weight: 600; }
.svc-body { padding: 12px; }
.svc-name { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.svc-meta { font-size: 12px; color: #909399; display: flex; align-items: center; gap: 6px; margin-bottom: 12px; }
.svc-meta .dot { color: #dcdfe6; }
.svc-foot { display: flex; align-items: center; justify-content: space-between; }
.svc-price { font-size: 18px; font-weight: 700; color: #F56C6C; }
.mine-toolbar { display: flex; justify-content: flex-end; margin-bottom: 4px; }
.book-preview { background: #f7fbfa; padding: 12px; border-radius: 8px; }
.bp-title { font-size: 16px; font-weight: 600; color: #303133; }
.bp-meta { font-size: 13px; color: #909399; margin-top: 4px; }
.bp-price { font-size: 18px; font-weight: 700; color: #F56C6C; margin-top: 6px; }
</style>
