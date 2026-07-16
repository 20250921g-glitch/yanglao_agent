<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>评论管理</span></div>
    </template>

    <el-form :inline="true" :model="queryForm" style="margin-bottom:12px">
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:140px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="targetId" label="目标ID" width="100" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentPage, updateCommentStatus, deleteComment } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryForm = reactive({ status: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await getCommentPage({ page: pageNum.value, size: pageSize.value, ...queryForm })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    tableData.value = []
    total.value = 0
  }
  loading.value = false
}

const reset = () => {
  queryForm.status = ''
  pageNum.value = 1
  load()
}

const toggleStatus = async (row) => {
  try {
    await updateCommentStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该评论？', '提示', { type: 'warning' })
  try {
    await deleteComment(row.id)
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