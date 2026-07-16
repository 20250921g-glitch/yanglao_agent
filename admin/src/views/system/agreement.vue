<template>
  <el-card>
    <template #header><span>协议管理</span></template>

    <el-table :data="list" stripe v-loading="loading" border>
      <template #empty><el-empty description="暂无数据" /></template>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="协议标题" min-width="180" />
      <el-table-column prop="content" label="协议内容" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end"
      @current-change="load" @size-change="load" />
  </el-card>

  <el-dialog v-model="editVisible" title="编辑协议" width="640px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入协议标题" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" type="textarea" :rows="12" placeholder="请输入协议内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible=false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAgreementPage, updateAgreement } from '@/api'

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const list = ref([])
const editVisible = ref(false), saving = ref(false)
const form = reactive({ id: null, title: '', content: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await getAgreementPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const openEdit = (row) => {
  form.id = row.id
  form.title = row.title || ''
  form.content = row.content || ''
  editVisible.value = true
}

const save = async () => {
  saving.value = true
  try {
    await updateAgreement({ id: form.id, title: form.title, content: form.content })
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { saving.value = false }
}

onMounted(load)
</script>
