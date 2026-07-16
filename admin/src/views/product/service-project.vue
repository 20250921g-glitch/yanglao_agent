<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">服务项目管理</h2>
          <span class="page-sub">康复理疗服务项目维护</span>
        </div>
        <div class="filters">
          <el-input v-model="keyword" placeholder="搜索项目名称" clearable style="width: 180px" @clear="load" @keyup.enter="load" />
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="load">
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
          <el-button @click="load">查询</el-button>
          <el-button type="primary" @click="openAdd">新增项目</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="name" label="项目名称" min-width="160" />
        <el-table-column prop="category" label="服务分类" width="130" align="center">
          <template #default="{ row }"><el-tag size="small" type="success">{{ row.category }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="110" align="center" />
        <el-table-column prop="price" label="价格(元)" width="110" align="center">
          <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column prop="method" label="服务方式" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="(v) => changeStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" :current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="handlePage" />
    </el-card>

    <el-dialog v-model="dialog" :title="form.id ? '编辑项目' : '新增项目'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="项目名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="服务分类">
          <el-select v-model="form.category" placeholder="选择分类">
            <el-option label="康复理疗" value="康复理疗" />
            <el-option label="中医推拿" value="中医推拿" />
            <el-option label="运动康复" value="运动康复" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="form.duration" :min="0" :step="15" /></el-form-item>
        <el-form-item label="价格(元)"><el-input-number v-model="form.price" :min="0" :step="10" :precision="2" /></el-form-item>
        <el-form-item label="服务方式">
          <el-radio-group v-model="form.method">
            <el-radio value="上门服务">上门服务</el-radio>
            <el-radio value="到店服务">到店服务</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">已上架</el-radio>
            <el-radio :value="0">已下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceProjectPage, saveServiceProject, updateServiceProject, updateServiceProjectStatus, deleteServiceProject } from '@/api/index'

const loading = ref(false)
const dialog = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const form = ref({ id: null, name: '', category: '康复理疗', duration: 60, price: 200, method: '上门服务', status: 1 })

const load = async () => {
  loading.value = true
  try {
    const res = await getServiceProjectPage({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined, status: statusFilter.value === '' ? undefined : statusFilter.value })
    if (res.code === 200) { list.value = res.data.records || []; total.value = res.data.total || 0 }
  } finally { loading.value = false }
}
const handlePage = (p) => { pageNum.value = p; load() }

const openAdd = () => { form.value = { id: null, name: '', category: '康复理疗', duration: 60, price: 200, method: '上门服务', status: 1 }; dialog.value = true }
const openEdit = (row) => { form.value = { ...row }; dialog.value = true }
const save = async () => {
  if (!form.value.name) return ElMessage.warning('请填写项目名称')
  const res = form.value.id ? await updateServiceProject(form.value) : await saveServiceProject(form.value)
  if (res.code === 200) { ElMessage.success('已保存'); dialog.value = false; load() }
}
const changeStatus = async (row, v) => {
  const res = await updateServiceProjectStatus(row.id, v)
  if (res.code !== 200) { row.status = row.status === 1 ? 0 : 1; ElMessage.error('操作失败') }
}
const remove = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await deleteServiceProject(row.id)
    if (res.code === 200) { ElMessage.success('已删除'); load() }
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; font-weight: 600; color: #303133; }
.page-sub { font-size: 13px; color: #A0AEC0; margin-left: 8px; }
.filters { display: flex; gap: 10px; }
.price { color: #FF4D4F; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
