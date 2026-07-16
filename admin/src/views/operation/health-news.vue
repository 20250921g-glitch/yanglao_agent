<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>健康资讯管理</span>
        <el-button type="primary" @click="openDialog(null)">新增资讯</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="资讯标题">
        <el-input v-model="searchForm.title" placeholder="请输入标题" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="searchForm.category" placeholder="请选择" clearable>
          <el-option label="健康科普" value="健康科普" />
          <el-option label="疾病护理" value="疾病护理" />
          <el-option label="饮食健康" value="饮食健康" />
          <el-option label="生活保健" value="生活保健" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="资讯标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100">
        <template #default="{ row }">
          <el-tag :type="categoryMap[row.category] || 'info'" size="small">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editId ? '编辑资讯' : '新增资讯'" width="700px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="资讯标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入资讯标题" />
      </el-form-item>
      <el-form-item label="资讯摘要" prop="summary">
        <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入资讯摘要" />
      </el-form-item>
      <el-form-item label="资讯内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入资讯内容（支持HTML）" />
      </el-form-item>
      <el-form-item label="封面图片">
        <el-input v-model="form.coverImage" placeholder="请输入图片URL" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" placeholder="请选择分类">
          <el-option label="健康科普" value="健康科普" />
          <el-option label="疾病护理" value="疾病护理" />
          <el-option label="饮食健康" value="饮食健康" />
          <el-option label="生活保健" value="生活保健" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" :max="999" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getHealthNewsPage, addHealthNews, updateHealthNews, deleteHealthNews } from '@/api'

const categoryMap = { '健康科普': 'success', '疾病护理': 'danger', '饮食健康': 'warning', '生活保健': 'info' }

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

const searchForm = reactive({ title: '', category: '', status: '' })
const form = reactive({ title: '', summary: '', content: '', coverImage: '', category: '', sort: 0, status: 1 })
const rules = {
  title: [{ required: true, message: '请输入资讯标题', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入资讯摘要', trigger: 'blur' }],
  content: [{ required: true, message: '请输入资讯内容', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getHealthNewsPage({ pageNum: pageNum.value, pageSize: pageSize.value, title: searchForm.title || undefined, category: searchForm.category || undefined, status: searchForm.status || undefined })
    tableData.value = (res.data.records || res.data.list || []).map(item => ({
      ...item,
      createTime: item.createTime ? item.createTime.replace('T', ' ').substring(0, 19) : ''
    }))
    total.value = res.data.total
  } catch {}
  loading.value = false
}

const resetSearch = () => { Object.assign(searchForm, { title: '', category: '', status: '' }); pageNum.value = 1; load() }

const openDialog = (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    Object.assign(form, {
      title: row.title,
      summary: row.summary,
      content: row.content,
      coverImage: row.coverImage,
      category: row.category,
      sort: row.sort || 0,
      status: row.status
    })
  } else {
    Object.assign(form, { title: '', summary: '', content: '', coverImage: '', category: '', sort: 0, status: 1 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateHealthNews({ ...form, id: editId.value })
      ElMessage.success('修改成功')
    } else {
      await addHealthNews(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该资讯？', '提示', { type: 'warning' })
  try {
    await deleteHealthNews(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>
