<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>服务人员管理</span>
        <el-button type="primary" @click="router.push('/service/worker-add')">新增服务人员</el-button>
      </div>
    </template>

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="姓名">
        <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable style="width:130px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="服务类型">
        <el-select v-model="searchForm.serviceType" placeholder="请选择" clearable style="width:130px">
          <el-option label="家政护工" value="家政护工" />
          <el-option label="康复理疗" value="康复理疗" />
          <el-option label="上门体检" value="上门体检" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="searchForm.tag" placeholder="请选择标签" clearable filterable style="width:150px">
          <el-option v-for="t in tagOptions" :key="t.id" :label="t.tagName" :value="t.tagName" />
        </el-select>
      </el-form-item>
      <el-form-item label="加入日期">
        <el-date-picker v-model="searchForm.dateRange" type="daterange" unlink-panels
          range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width:240px" />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select v-model="searchForm.auditStatus" placeholder="请选择" clearable style="width:120px">
          <el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已拒绝" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 批量操作 -->
    <div class="batch-bar" v-if="selection.length">
      <span class="sel-tip">已选 {{ selection.length }} 项</span>
      <el-button type="danger" plain size="small" @click="batchDelete">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="pagedData" stripe v-loading="loading" @selection-change="onSelect">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="服务人员ID" width="100" />
      <el-table-column label="服务人员信息" min-width="210">
        <template #default="{ row }">
          <div class="worker-cell">
            <el-avatar :size="40" :src="row.avatar" v-if="row.avatar">{{ row.name?.charAt(0) }}</el-avatar>
            <el-avatar :size="40" v-else>{{ row.name?.charAt(0) }}</el-avatar>
            <div>
              <div class="w-name">{{ row.name }}</div>
              <div class="w-sub">{{ row.phone }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="serviceType" label="服务类型" width="110" />
      <el-table-column label="标签" min-width="170">
        <template #default="{ row }">
          <el-tag v-for="tag in (row.tags || [])" :key="tag.id ?? tag" size="small" type="warning" style="margin-right:4px">
            {{ tag.tagName || tag }}
          </el-tag>
          <span v-if="!row.tags || !row.tags.length" class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="region" label="负责区域" width="110" />
      <el-table-column prop="createTime" label="加入时间" width="170" />
      <el-table-column prop="joinType" label="加入方式" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="router.push('/service/order')">工单记录</el-button>
          <el-button link type="primary" size="small" @click="router.push('/service/commission')">佣金记录</el-button>
          <el-button link type="warning" size="small" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button link type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="filteredTotal"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑人员' : '新增人员'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="请输入姓名" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender"><el-radio :label="1">男</el-radio><el-radio :label="0">女</el-radio></el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务类型" prop="serviceType">
              <el-select v-model="form.serviceType" placeholder="请选择">
                <el-option label="家政护工" value="家政护工" /><el-option label="康复理疗" value="康复理疗" /><el-option label="上门体检" value="上门体检" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号"><el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责区域"><el-input v-model="form.region" placeholder="请输入负责区域" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="银行卡号"><el-input v-model="form.bankCard" placeholder="请输入银行卡号" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户行"><el-input v-model="form.bankName" placeholder="请输入开户行" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="个人简介"><el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入个人简介" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许打赏"><el-switch v-model="form.allowTip" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceWorkerPage, getServiceWorker, createServiceWorker, updateServiceWorker, deleteServiceWorker,
         updateServiceWorkerStatus, getServiceWorkerTagPage } from '@/api'

const router = useRouter()

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const rawData = ref([]), selection = ref([]), tagOptions = ref([])
const dialogVisible = ref(false), editId = ref(null), formRef = ref(null)
const searchForm = reactive({ name: '', serviceType: '', tag: '', dateRange: null, auditStatus: '' })
const form = reactive({ name: '', phone: '', gender: 1, idCard: '', bankCard: '', bankName: '', serviceType: '', region: '', intro: '', allowTip: 1, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getServiceWorkerPage({ pageNum: pageNum.value, pageSize: pageSize.value, name: searchForm.name || null, serviceType: searchForm.serviceType || null, auditStatus: searchForm.auditStatus === '' ? null : searchForm.auditStatus })
    rawData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {}
  finally { loading.value = false }
}
const loadTags = async () => {
  try { const res = await getServiceWorkerTagPage({ pageNum: 1, pageSize: 100 }); tagOptions.value = (res.data.records || res.data || []) } catch (e) {}
}

const filteredData = computed(() => {
  let list = rawData.value
  const range = searchForm.dateRange
  if (range && range.length === 2) {
    const start = new Date(range[0]).setHours(0, 0, 0, 0)
    const end = new Date(range[1]).setHours(23, 59, 59, 999)
    list = list.filter(r => { const t = new Date(r.createTime).getTime(); return t >= start && t <= end })
  }
  if (searchForm.tag) {
    list = list.filter(r => (r.tags || []).some(t => (t.tagName || t) === searchForm.tag))
  }
  return list
})
const filteredTotal = computed(() => (searchForm.dateRange || searchForm.tag) ? filteredData.value.length : total.value)
const pagedData = computed(() => {
  if (searchForm.dateRange || searchForm.tag) {
    const s = (pageNum.value - 1) * pageSize.value
    return filteredData.value.slice(s, s + pageSize.value)
  }
  return filteredData.value
})

const resetSearch = () => { Object.assign(searchForm, { name: '', serviceType: '', tag: '', dateRange: null, auditStatus: '' }); pageNum.value = 1; load() }
const onSelect = (rows) => { selection.value = rows }
const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await updateServiceWorkerStatus(row.id, next); ElMessage.success(next === 1 ? '已启用' : '已禁用'); load()
}
const del = async (row) => {
  await ElMessageBox.confirm('确认删除该服务人员？', '提示', { type: 'warning' })
  await deleteServiceWorker(row.id); ElMessage.success('删除成功'); load()
}
const batchDelete = async () => {
  await ElMessageBox.confirm(`确认删除选中的 ${selection.value.length} 个服务人员？`, '提示', { type: 'warning' })
  try { for (const row of selection.value) await deleteServiceWorker(row.id); ElMessage.success('批量删除成功'); load() } catch (e) {}
}
const openDialog = async (row) => {
  editId.value = row?.id || null
  if (editId.value) { const res = await getServiceWorker(editId.value); Object.assign(form, res.data) }
  else Object.assign(form, { name: '', phone: '', gender: 1, idCard: '', bankCard: '', bankName: '', serviceType: '', region: '', intro: '', allowTip: 1, status: 1 })
  dialogVisible.value = true
}
const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  try {
    if (editId.value) { await updateServiceWorker(editId.value, form); ElMessage.success('修改成功') }
    else { await createServiceWorker(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; load()
  } catch (e) {}
}

onMounted(() => { load(); loadTags() })
</script>

<style scoped>
.worker-cell { display:flex; align-items:center; gap:10px; }
.w-name { font-weight:500; color:#303133; }
.w-sub { color:#A0AEC0; font-size:12px; margin-top:2px; }
.batch-bar { margin-bottom:12px; display:flex; align-items:center; gap:10px; }
.sel-tip { font-size:13px; color:#606266; }
.muted { color:#c0c4cc; }
</style>
