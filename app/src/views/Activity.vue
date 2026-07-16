<template>
  <div class="activity-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- 活动报名 -->
      <el-tab-pane label="活动报名" name="list">
        <!-- 筛选 -->
        <el-form :inline="true" :model="search" class="filter-bar" @submit.prevent>
          <el-form-item label="类型">
            <el-select v-model="search.type" placeholder="全部" clearable style="width:150px" @change="load">
              <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="search.status" placeholder="全部" clearable style="width:130px" @change="load">
              <el-option label="未开始" :value="0" />
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="search.keyword" placeholder="活动名称" clearable style="width:180px" @keyup.enter="load" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="button" @click="load">查询</el-button>
            <el-button native-type="button" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 活动卡片 -->
        <div v-loading="loading" class="card-wrap">
          <el-empty v-if="!loading && list.length === 0" description="暂无活动" />
          <el-card v-for="a in list" :key="a.id" class="act-card" shadow="hover">
            <div class="act-cover">
              <el-image v-if="a.imageUrl" :src="a.imageUrl" fit="cover">
                <template #error><div class="cover-fallback">无图</div></template>
              </el-image>
              <div v-else class="cover-fallback">{{ a.type ? a.type.charAt(0) : '活' }}</div>
              <span class="cover-tag" :style="{ background: typeColor[a.type] || '#909399' }">{{ a.type }}</span>
            </div>
            <div class="act-body">
              <div class="act-name">{{ a.name }}</div>
              <div class="act-meta">
                <div><el-icon><Clock /></el-icon> {{ fmt(a.startTime) }} ~ {{ fmt(a.endTime) }}</div>
                <div><el-icon><Location /></el-icon> {{ a.location || '待定' }}</div>
                <div><el-icon><User /></el-icon> 已报名 {{ a.participantCount || 0 }} 人</div>
              </div>
              <div class="act-foot">
                <el-tag :type="statusTag[a.status]" size="small">{{ statusText[a.status] }}</el-tag>
                <div class="foot-btns">
                  <el-button size="small" link type="info" @click="showDetail(a.id)">详情</el-button>
                  <el-button
                    v-if="registeredStatus(a.id) === 1"
                    size="small" type="success" plain disabled>已报名</el-button>
                  <el-button
                    v-else-if="registeredStatus(a.id) === 0"
                    size="small" type="warning" plain disabled>待审核</el-button>
                  <el-button
                    v-else
                    size="small" type="primary"
                    :disabled="a.status === 2 || isExpired(a)"
                    @click="openRegister(a)">报名</el-button>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <el-pagination
          v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[6,12,24]" layout="total,sizes,prev,pager,next"
          style="margin-top:16px;justify-content:flex-end" @change="load" />
      </el-tab-pane>

      <!-- 我的报名 -->
      <el-tab-pane label="我的报名" name="mine">
        <el-table :data="myList" stripe v-loading="myLoading" style="margin-top:8px">
          <template #empty><el-empty description="您还没有报名任何活动" /></template>
          <el-table-column prop="activityName" label="活动名称" min-width="160" />
          <el-table-column label="类型" width="120">
            <template #default="{ row }"><el-tag size="small" :type="typeTag[row.activityType] || 'info'">{{ row.activityType }}</el-tag></template>
          </el-table-column>
          <el-table-column label="活动时间" min-width="180">
            <template #default="{ row }">{{ fmt(row.activityStartTime) }} ~ {{ fmt(row.activityEndTime) }}</template>
          </el-table-column>
          <el-table-column prop="activityLocation" label="地点" min-width="120">
            <template #default="{ row }">{{ row.activityLocation || '待定' }}</template>
          </el-table-column>
          <el-table-column label="报名状态" width="110">
            <template #default="{ row }"><el-tag :type="regTag[row.registrationStatus]" size="small">{{ regText[row.registrationStatus] }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createTime" label="报名时间" width="170">
            <template #default="{ row }">{{ fmt(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.registrationStatus === 1" type="danger" size="small" plain @click="doCancel(row)">取消</el-button>
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

    <!-- 报名对话框 -->
    <el-dialog v-model="regVisible" title="活动报名" width="520px">
      <div v-if="current" class="reg-preview">
        <div class="rp-title">{{ current.name }}</div>
        <div class="rp-meta">{{ current.type }} · {{ fmt(current.startTime) }} ~ {{ fmt(current.endTime) }}</div>
        <div class="rp-meta">地点：{{ current.location || '待定' }}</div>
      </div>
      <el-form label-width="80px" style="margin-top:12px">
        <el-form-item label="手机号">
          <el-input v-model="regForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="regForm.remark" type="textarea" :rows="3" placeholder="如有特殊需求可在此说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="regVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRegister">确认报名</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="560px">
      <div v-if="detail" v-loading="detailLoading">
        <el-image v-if="detail.imageUrl" :src="detail.imageUrl" fit="cover" class="detail-cover" />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="活动名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="活动类型">{{ detail.type }}</el-descriptions-item>
          <el-descriptions-item label="活动时间">{{ fmt(detail.startTime) }} ~ {{ fmt(detail.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="活动地点">{{ detail.location || '待定' }}</el-descriptions-item>
          <el-descriptions-item label="已报名人数">{{ detail.participantCount || 0 }} 人</el-descriptions-item>
          <el-descriptions-item label="活动状态">
            <el-tag :type="statusTag[detail.status]" size="small">{{ statusText[detail.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="活动描述">{{ detail.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Location, User } from '@element-plus/icons-vue'
import {
  getActivityList, getActivityDetail, registerActivity, myActivities, cancelActivity
} from '@/api'
import { currentUser } from '@/store/user'

const categoryOptions = ['节日活动', '健康服务', '技能培训', '兴趣社团', '公益讲座']
const typeColor = { '节日活动': '#F56C6C', '健康服务': '#67C23A', '技能培训': '#E6A23C', '兴趣社团': '#409EFF', '公益讲座': '#909399' }
const typeTag = { '节日活动': 'danger', '健康服务': 'success', '技能培训': 'warning', '兴趣社团': '', '公益讲座': 'info' }
const statusText = { 0: '未开始', 1: '进行中', 2: '已结束' }
const statusTag = { 0: 'info', 1: 'success', 2: 'warning' }
const regText = { 0: '待审核', 1: '报名成功', 2: '已拒绝', 3: '已取消' }
const regTag = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }

const activeTab = ref('list')
const loading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(6)
const total = ref(0)
const search = reactive({ type: '', status: '', keyword: '' })

// 已报名映射：activityId -> status（1成功/0待审核/3已取消）
const registeredMap = ref({})

const regVisible = ref(false)
const current = ref(null)
const regForm = reactive({ phone: '', remark: '' })
const submitting = ref(false)

const detailVisible = ref(false)
const detail = ref(null)
const detailLoading = ref(false)

const myList = ref([])
const myLoading = ref(false)
const myPageNum = ref(1)
const myPageSize = ref(10)
const myTotal = ref(0)

const fmt = (d) => {
  if (!d) return '—'
  return String(d).replace('T', ' ').substring(0, 16)
}

// 活动是否已过期（结束时间早于当前时间）
const isExpired = (a) => {
  if (!a || !a.endTime) return false
  const t = new Date(String(a.endTime).replace(' ', 'T'))
  return !isNaN(t.getTime()) && t.getTime() < Date.now()
}

const registeredStatus = (activityId) => registeredMap.value[activityId]

const load = async () => {
  loading.value = true
  try {
    const res = await getActivityList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: search.type || undefined,
      status: search.status ?? undefined,
      keyword: search.keyword || undefined
    })
    list.value = (res.data.records || []).map(a => ({
      ...a,
      startTime: fmt(a.startTime),
      endTime: fmt(a.endTime)
    }))
    total.value = res.data.total
    await loadRegistered()
  } catch (e) {
    ElMessage.error(e.message || '加载活动失败')
  } finally {
    loading.value = false
  }
}

// 类型/状态切换时自动筛选并重置到第 1 页
watch(() => [search.type, search.status], () => {
  pageNum.value = 1
  load()
})

const loadRegistered = async () => {
  try {
    const res = await myActivities({ pageNum: 1, pageSize: 100 })
    const map = {}
    ;(res.data.records || []).forEach(r => { map[r.activityId] = r.registrationStatus })
    registeredMap.value = map
  } catch (e) { /* 忽略 */ }
}

const resetSearch = () => {
  Object.assign(search, { type: '', status: '', keyword: '' })
  pageNum.value = 1
  load()
}

const openRegister = (a) => {
  current.value = a
  regForm.phone = currentUser.value?.phone || ''
  regForm.remark = ''
  regVisible.value = true
}

const submitRegister = async () => {
  if (!regForm.phone || !/^1\d{10}$/.test(regForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  submitting.value = true
  try {
    await registerActivity({ activityId: current.value.id, phone: regForm.phone, remark: regForm.remark })
    ElMessage.success('报名成功')
    regVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '报名失败')
  } finally {
    submitting.value = false
  }
}

const showDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await getActivityDetail(id)
    detail.value = res.data
  } catch (e) {
    ElMessage.error(e.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

const loadMine = async () => {
  myLoading.value = true
  try {
    const res = await myActivities({ pageNum: myPageNum.value, pageSize: myPageSize.value })
    myList.value = res.data.records || []
    myTotal.value = res.data.total
  } catch (e) {
    ElMessage.error(e.message || '加载我的报名失败')
  } finally {
    myLoading.value = false
  }
}

const doCancel = async (row) => {
  await ElMessageBox.confirm(`确认取消报名「${row.activityName}」？`, '提示', { type: 'warning' })
  try {
    await cancelActivity(row.id)
    ElMessage.success('已取消报名')
    await loadMine()
    await loadRegistered()
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  }
}

// 切换到"我的报名"时加载
const onTabChange = (name) => {
  if (name === 'mine') loadMine()
}

onMounted(load)
</script>

<style scoped>
.activity-page { padding: 4px; }
.filter-bar { margin-bottom: 8px; background: #fafafa; padding: 12px; border-radius: 8px; }
.card-wrap { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; min-height: 120px; }
.act-card { border-radius: 10px; overflow: hidden; }
.act-cover { position: relative; height: 140px; background: #f4f4f5; }
.act-cover .el-image, .cover-fallback { width: 100%; height: 140px; }
.cover-fallback { display: flex; align-items: center; justify-content: center; font-size: 40px; color: #fff; background: #00C4A1; }
.cover-tag { position: absolute; top: 8px; left: 8px; color: #fff; font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.act-body { padding: 12px; }
.act-name { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.act-meta { font-size: 12px; color: #909399; display: flex; flex-direction: column; gap: 4px; margin-bottom: 10px; }
.act-meta div { display: flex; align-items: center; gap: 4px; }
.act-foot { display: flex; align-items: center; justify-content: space-between; }
.foot-btns { display: flex; align-items: center; gap: 4px; }
.reg-preview { background: #f7fbfa; padding: 12px; border-radius: 8px; }
.rp-title { font-size: 16px; font-weight: 600; color: #303133; }
.rp-meta { font-size: 13px; color: #909399; margin-top: 4px; }
.detail-cover { width: 100%; height: 180px; border-radius: 8px; margin-bottom: 12px; }
</style>
