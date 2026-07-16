<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">活动字段管理</h2>
          <span class="page-sub">自定义报名表单字段</span>
        </div>
        <div>
          <el-input v-model="keyword" placeholder="搜索字段名称" clearable style="width: 180px; margin-right: 10px" @clear="load" @keyup.enter="load" />
          <el-button @click="load">查询</el-button>
          <el-button type="primary" @click="openAdd">新增字段</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="label" label="字段名称" min-width="140" />
        <el-table-column prop="type" label="字段类型" width="120" align="center">
          <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="options" label="选项" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.options || '—' }}</template>
        </el-table-column>
        <el-table-column prop="required" label="是否必填" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.required" :active-value="1" :inactive-value="0" @change="(v) => changeRequired(row, v)" />
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑字段' : '新增字段'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="字段名称"><el-input v-model="form.label" /></el-form-item>
        <el-form-item label="字段类型">
          <el-select v-model="form.type" placeholder="选择类型">
            <el-option label="单行文本" value="单行文本" />
            <el-option label="多行文本" value="多行文本" />
            <el-option label="单选" value="单选" />
            <el-option label="多选" value="多选" />
            <el-option label="日期" value="日期" />
          </el-select>
        </el-form-item>
        <el-form-item label="选项" v-if="['单选','多选'].includes(form.type)">
          <el-input v-model="form.options" placeholder="用逗号分隔，如：男,女" />
        </el-form-item>
        <el-form-item label="是否必填">
          <el-radio-group v-model="form.required">
            <el-radio :value="1">必填</el-radio>
            <el-radio :value="0">选填</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { getActivityFieldPage, saveActivityField, updateActivityField, updateActivityFieldStatus, deleteActivityField } from '@/api/index'

const loading = ref(false)
const dialog = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const form = ref({ id: null, label: '', type: '单行文本', options: '', required: 0, status: 1 })

const load = async () => {
  loading.value = true
  try {
    const res = await getActivityFieldPage({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    if (res.code === 200) { list.value = res.data.records || []; total.value = res.data.total || 0 }
  } finally { loading.value = false }
}
const handlePage = (p) => { pageNum.value = p; load() }

const openAdd = () => { form.value = { id: null, label: '', type: '单行文本', options: '', required: 0, status: 1 }; dialog.value = true }
const openEdit = (row) => { form.value = { ...row }; dialog.value = true }
const save = async () => {
  if (!form.value.label) return ElMessage.warning('请填写字段名称')
  const res = form.value.id ? await updateActivityField(form.value) : await saveActivityField(form.value)
  if (res.code === 200) { ElMessage.success('已保存'); dialog.value = false; load() }
}
const changeStatus = async (row, v) => {
  const res = await updateActivityFieldStatus(row.id, v)
  if (res.code !== 200) { row.status = row.status === 1 ? 0 : 1; ElMessage.error('操作失败') }
}
const changeRequired = (row, v) => { row.required = v }
const remove = (row) => {
  ElMessageBox.confirm(`确定删除「${row.label}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await deleteActivityField(row.id)
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
