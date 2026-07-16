<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>商品分类管理</span>
        <el-button type="primary" @click="openDialog()">新增分类</el-button>
      </div>
    </template>
    
    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="分类名称">
        <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="服务类型">
        <el-select v-model="searchForm.serviceType" placeholder="请选择" clearable :disabled="!!lockedServiceType">
          <el-option label="家政护理" value="家政护理" />
          <el-option label="康复理疗" value="康复理疗" />
          <el-option label="上门体检" value="上门体检" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" width="120" />
      <el-table-column prop="serviceType" label="服务类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getServiceTypeTag(row.serviceType)" size="small">{{ row.serviceType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="updateStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <!-- 新增/编辑对话框 -->
  <el-dialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新增分类'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="服务类型" prop="serviceType">
        <el-select v-model="form.serviceType" placeholder="请选择服务类型" style="width:100%">
          <el-option label="家政护理" value="家政护理" />
          <el-option label="康复理疗" value="康复理疗" />
          <el-option label="上门体检" value="上门体检" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="1" :max="100" style="width:100%" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductCategoryPage, getProductCategory, createProductCategory, updateProductCategory, deleteProductCategory } from '@/api'

const route = useRoute()
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), editId = ref(null), formRef = ref(null)
const lockedServiceType = ref(route.query.serviceType || '')

const searchForm = reactive({ name: '', serviceType: '' })
const form = reactive({ name: '', serviceType: '', sort: 1, description: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }]
}

const getServiceTypeTag = (type) => {
  const map = { '家政护理': 'success', '康复理疗': 'warning', '上门体检': 'primary' }
  return map[type] || 'info'
}

const load = async () => {
  loading.value = true
  if (lockedServiceType.value) searchForm.serviceType = lockedServiceType.value
  try {
    const res = await getProductCategoryPage({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.name = ''
  if (!lockedServiceType.value) searchForm.serviceType = ''
  load()
}

const openDialog = async (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    const res = await getProductCategory(editId.value)
    Object.assign(form, res.data)
  } else {
    Object.assign(form, { name: '', serviceType: '', sort: 1, description: '', status: 1 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateProductCategory(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createProductCategory(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {}
}

const updateStatus = async (row) => {
  try {
    const res = await fetch(`/api/product/category/${row.id}/status?status=${row.status}`, { method: 'PUT' })
    ElMessage.success('状态更新成功')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
  await deleteProductCategory(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
watch(() => route.query.serviceType, (nv) => {
  lockedServiceType.value = nv || ''
  if (lockedServiceType.value) searchForm.serviceType = lockedServiceType.value
  else searchForm.serviceType = ''
  load()
})
</script>
