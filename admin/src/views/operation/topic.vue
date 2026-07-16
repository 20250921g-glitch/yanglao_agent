<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>话题管理</span><el-button type="primary" @click="openDialog()">新增话题</el-button></div>
    </template>

    <el-form :inline="true" :model="queryForm" style="margin-bottom:12px">
      <el-form-item label="话题名称">
        <el-input v-model="queryForm.name" placeholder="请输入话题名称" clearable style="width:200px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:140px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="话题ID" width="100" />
      <el-table-column prop="title" label="话题名称" min-width="150" />
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

  <el-dialog v-model="dialogVisible" :title="editId ? '编辑话题' : '新增话题'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="话题名称" prop="title">
        <el-input v-model="form.title" placeholder="请输入话题名称" />
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
import { getTopicPage, addTopic, updateTopic, deleteTopic } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

const queryForm = reactive({ title: '', status: '' })
const form = reactive({ title: '', sort: 0, status: 1 })
const rules = {
  title: [{ required: true, message: '请输入话题名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getTopicPage({ page: pageNum.value, size: pageSize.value, ...queryForm })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    tableData.value = []
    total.value = 0
  }
  loading.value = false
}

const reset = () => {
  queryForm.title = ''
  queryForm.status = ''
  pageNum.value = 1
  load()
}

const openDialog = (row) => {
  editId.value = row ? row.id : null
  if (editId.value) {
    Object.assign(form, { title: row.title, sort: row.sort, status: row.status })
  } else {
    formRef.value?.resetFields()
    Object.assign(form, { title: '', sort: 0, status: 1 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateTopic({ id: editId.value, ...form })
      ElMessage.success('修改成功')
    } else {
      await addTopic(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该话题？', '提示', { type: 'warning' })
  try {
    await deleteTopic(row.id)
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