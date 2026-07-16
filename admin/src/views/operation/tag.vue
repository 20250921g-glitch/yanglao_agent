<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>标签管理</span><el-button type="primary" @click="openDialog()">新增标签</el-button></div>
    </template>

    <el-form :inline="true" :model="queryForm" style="margin-bottom:12px">
      <el-form-item label="标签名称">
        <el-input v-model="queryForm.name" placeholder="请输入标签名称" clearable style="width:200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="标签名称" min-width="150" />
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editId ? '编辑标签' : '新增标签'" width="450px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入标签名称" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTagPage, addTag, updateTag, deleteTag } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

const queryForm = reactive({ name: '' })
const form = reactive({ name: '', sort: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getTagPage({ page: pageNum.value, size: pageSize.value, ...queryForm })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    tableData.value = []
    total.value = 0
  }
  loading.value = false
}

const reset = () => {
  queryForm.name = ''
  pageNum.value = 1
  load()
}

const openDialog = (row) => {
  editId.value = row ? row.id : null
  if (editId.value) {
    Object.assign(form, { name: row.name, sort: row.sort, status: row.status })
  } else {
    formRef.value?.resetFields()
    Object.assign(form, { name: '', sort: 0, status: 1 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateTag({ id: editId.value, ...form })
      ElMessage.success('修改成功')
    } else {
      await addTag(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该标签？', '提示', { type: 'warning' })
  try {
    await deleteTag(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>