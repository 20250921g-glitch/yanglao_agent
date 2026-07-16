<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>服务人员管理</span>
        <el-button type="primary" @click="openDialog()">新增服务人员</el-button>
      </div>
    </template>

    <!-- 搜索筛选 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="姓名">
        <el-input v-model="search.name" placeholder="请输入姓名" clearable style="width:140px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="加入日期">
        <el-date-picker v-model="search.dateRange" type="daterange" unlink-panels
          range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:240px" @change="load" />
      </el-form-item>
      <el-form-item label="标签">
        <!-- TODO 后端staff无tags字段，未提供该筛选 -->
        <el-select disabled placeholder="暂无" style="width:140px">
          <el-option label="—" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务类型">
        <!-- TODO 后端staff无serviceType字段，未提供该筛选 -->
        <el-select disabled placeholder="暂无" style="width:140px">
          <el-option label="—" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="search.status" placeholder="请选择" clearable style="width:120px" @change="load">
          <el-option :value="1" label="在职" />
          <el-option :value="0" label="离职" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tableRef" :data="displayData" stripe v-loading="loading" @selection-change="onSelect">
      <template #empty><el-empty description="暂无服务人员数据" /></template>
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="服务人员ID" width="110" />
      <el-table-column label="服务人员信息" min-width="200">
        <template #default="{ row }">
          <div class="staff-cell">
            <el-avatar :size="38" class="staff-avatar">{{ (row.name || '?').charAt(0) }}</el-avatar>
            <div class="staff-meta">
              <div class="staff-name">{{ row.name || '—' }}</div>
              <div class="sub">{{ row.phone || '—' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <!-- TODO 后端staff无serviceType字段，暂以占位展示，待后端扩展 -->
      <el-table-column label="服务类型" width="120">
        <template #default><span class="ph">—</span></template>
      </el-table-column>
      <!-- TODO 后端staff无tags字段，暂以占位展示，待后端扩展 -->
      <el-table-column label="标签" width="130">
        <template #default><span class="ph">—</span></template>
      </el-table-column>
      <!-- TODO 后端staff无region/area字段，暂以占位展示，待后端扩展 -->
      <el-table-column label="负责区域" width="120">
        <template #default><span class="ph">—</span></template>
      </el-table-column>
      <el-table-column prop="joinDate" label="加入时间" width="130" />
      <!-- TODO 后端staff无joinType/source字段，暂以占位展示，待后端扩展 -->
      <el-table-column label="加入方式" width="130">
        <template #default>
          <el-tag type="info" size="small">—</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '在职' : '离职' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="workOrders(row)">工单记录</el-button>
          <el-button link type="primary" size="small" @click="commissions(row)">佣金记录</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批量操作栏 -->
    <div v-if="selection.length" class="batch-bar">
      <span class="batch-tip">已选 {{ selection.length }} 项</span>
      <el-button type="danger" size="small" @click="batchDelete">批量删除</el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑服务人员' : '新增服务人员'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工号">
              <el-input v-model="form.employeeNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option :value="1" label="在职" />
                <el-option :value="0" label="离职" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="加入时间">
          <el-date-picker v-model="form.joinDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStaffPage, addStaff, updateStaff, deleteStaff } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const search = reactive({ name: '', dateRange: null, status: null })
const selection = ref([])
const tableRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null, name: '', employeeNo: '', phone: '', joinDate: '', status: 1, remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

// 服务端已完成姓名/状态筛选；加入日期后端未提供筛选参数，按真实返回数据(entryDate)二次过滤
const displayData = computed(() => {
  const range = search.dateRange
  if (!range || range.length !== 2) return tableData.value
  const s = range[0] ? new Date(range[0]).toISOString().slice(0, 10) : ''
  const e = range[1] ? new Date(range[1]).toISOString().slice(0, 10) : ''
  return tableData.value.filter(r => {
    const d = r.joinDate || ''
    return (!s || d >= s) && (!e || d <= e)
  })
})

const load = async () => {
  loading.value = true
  try {
    const res = await getStaffPage({ pageNum: pageNum.value, pageSize: pageSize.value, name: search.name || undefined, status: search.status ?? undefined })
    tableData.value = (res.data.records || res.data.list || []).map(item => ({
      id: item.id,
      name: item.name,
      employeeNo: item.staffNo,
      phone: item.phone,
      joinDate: item.entryDate ? item.entryDate.substring(0, 10) : '',
      status: item.status,
      remark: item.remark
    }))
    total.value = res.data.total
  } catch {}
  loading.value = false
}

const reset = () => {
  search.name = ''
  search.dateRange = null
  search.status = null
  pageNum.value = 1
  load()
}

const onSelect = (rows) => { selection.value = rows }
const clearSelection = () => { tableRef.value?.clearSelection(); selection.value = [] }

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, employeeNo: row.employeeNo, phone: row.phone, joinDate: row.joinDate, status: row.status, remark: row.remark })
    isEdit.value = true
  } else {
    Object.assign(form, { id: null, name: '', employeeNo: '', phone: '', joinDate: '', status: 1, remark: '' })
    isEdit.value = false
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = {
      name: form.name,
      staffNo: form.employeeNo,
      phone: form.phone,
      entryDate: form.joinDate || null,
      status: form.status,
      remark: form.remark
    }
    if (isEdit.value) {
      payload.id = form.id
      await updateStaff(payload)
      ElMessage.success('编辑成功')
    } else {
      await addStaff(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const toggleStatus = async (row) => {
  try {
    await updateStaff({ id: row.id, status: row.status === 1 ? 0 : 1 })
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该服务人员吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteStaff(row.id)
      ElMessage.success('删除成功')
      load()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    }
  }).catch(() => {})
}

const batchDelete = () => {
  if (!selection.value.length) return
  ElMessageBox.confirm(`确定删除选中的 ${selection.value.length} 个服务人员吗？`, '提示', { type: 'warning' }).then(async () => {
    let ok = 0
    for (const row of selection.value) {
      try { await deleteStaff(row.id); ok++ } catch {}
    }
    ElMessage.success(`已删除 ${ok} 个服务人员`)
    clearSelection(); load()
  }).catch(() => {})
}

// TODO 后端未提供工单记录接口
const workOrders = (row) => { ElMessage.info(`工单记录功能待后端提供接口（服务人员ID：${row.id}）`) }
// TODO 后端未提供佣金记录接口
const commissions = (row) => { ElMessage.info(`佣金记录功能待后端提供接口（服务人员ID：${row.id}）`) }

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.staff-cell { display: flex; align-items: center; gap: 10px; }
.staff-avatar { background: #409eff; color: #fff; flex: 0 0 auto; }
.staff-name { font-weight: 500; color: #303133; }
.sub { color: #A0AEC0; font-size: 12px; }
.ph { color: #C0C4CC; }
.batch-bar { display: flex; align-items: center; gap: 12px; margin-top: 14px; padding: 10px 14px; background: #f4f6f8; border-radius: 8px; }
.batch-tip { font-size: 13px; color: #606266; }
</style>
