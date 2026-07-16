<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">药品单位管理</h2>
          <span class="page-sub">药品计量单位维护</span>
        </div>
        <div>
          <el-input v-model="keyword" placeholder="搜索单位名称" clearable style="width: 180px; margin-right: 10px" @clear="load" @keyup.enter="load" />
          <el-button @click="load">查询</el-button>
          <el-button type="primary" @click="openAdd">新增单位</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="name" label="单位名称" min-width="140" />
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑单位' : '新增单位'" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="单位名称"><el-input v-model="form.name" placeholder="如：盒、瓶、片" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
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
import { getMedicineUnitPage, saveMedicineUnit, updateMedicineUnit, updateMedicineUnitStatus, deleteMedicineUnit } from '@/api/index'

const loading = ref(false)
const dialog = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const form = ref({ id: null, name: '', description: '', status: 1 })

const load = async () => {
  loading.value = true
  try {
    const res = await getMedicineUnitPage({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    if (res.code === 200) { list.value = res.data.records || []; total.value = res.data.total || 0 }
  } finally { loading.value = false }
}
const handlePage = (p) => { pageNum.value = p; load() }

const openAdd = () => { form.value = { id: null, name: '', description: '', status: 1 }; dialog.value = true }
const openEdit = (row) => { form.value = { ...row }; dialog.value = true }
const save = async () => {
  if (!form.value.name) return ElMessage.warning('请填写单位名称')
  const res = form.value.id ? await updateMedicineUnit(form.value) : await saveMedicineUnit(form.value)
  if (res.code === 200) { ElMessage.success('已保存'); dialog.value = false; load() }
}
const changeStatus = async (row, v) => {
  const res = await updateMedicineUnitStatus(row.id, v)
  if (res.code !== 200) { row.status = row.status === 1 ? 0 : 1; ElMessage.error('操作失败') }
}
const remove = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await deleteMedicineUnit(row.id)
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
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
