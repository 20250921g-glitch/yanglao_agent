<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>内容管理</span>
        <el-button type="primary" @click="openDialog()">新增内容</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="queryForm" style="margin-bottom:12px">
      <el-form-item label="内容类型">
        <el-select v-model="queryForm.type" placeholder="全部" clearable style="width:140px">
          <el-option label="文章" value="article" />
          <el-option label="视频" value="video" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:140px">
          <el-option label="草稿" value="0" />
          <el-option label="已发布" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" stripe v-loading="loading">
      <template #empty><el-empty description="暂无数据" /></template>
      <el-table-column prop="id" label="内容ID" width="100" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 'article' ? '' : 'warning'" size="small">{{ row.type === 'article' ? '文章' : '视频' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <el-image v-if="row.image" :src="row.image" style="width:50px;height:50px;border-radius:4px" fit="cover" :preview-src-list="[row.image]" />
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读量" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button link type="success" size="small" @click="publish(row)">发布</el-button>
          <el-button link type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end"
      @current-change="load" @size-change="load" />
  </el-card>

  <!-- 新增/编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="editId ? '编辑内容' : '新增内容'" width="600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入内容标题" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio value="article">文章</el-radio>
          <el-radio value="video">视频</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="封面URL">
        <el-input v-model="form.image" placeholder="请输入封面图片URL" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入内容正文" />
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
import { getContentPage, addContent, updateContent, deleteContent } from '@/api'

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const list = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

const queryForm = reactive({ type: '', status: '' })
const form = reactive({ title: '', type: 'article', image: '', content: '' })
const rules = {
  title: [{ required: true, message: '请输入内容标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择内容类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容正文', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getContentPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: queryForm.type || null,
      status: queryForm.status || null
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const reset = () => { queryForm.type = ''; queryForm.status = ''; pageNum.value = 1; load() }

const openDialog = (row) => {
  editId.value = row ? row.id : null
  if (editId.value) {
    Object.assign(form, { title: row.title, type: row.type, image: row.image || '', content: row.content || '' })
  } else {
    formRef.value?.resetFields()
    Object.assign(form, { title: '', type: 'article', image: '', content: '' })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateContent({ id: editId.value, ...form })
      ElMessage.success('修改成功')
    } else {
      await addContent(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) { /* 响应拦截器已提示错误 */ }
}

// 后端暂无独立的发布接口，发布即新增/更新为已发布状态
const publish = async (row) => {
  // TODO: 待后端提供独立的发布接口；当前以更新 status=1 处理
  try {
    await updateContent({ id: row.id, status: 1 })
    ElMessage.success('发布成功')
    load()
  } catch (e) { /* 响应拦截器已提示错误 */ }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该内容？', '提示', { type: 'warning' })
  await deleteContent(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
