<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>养老机构</span>
        <el-button type="primary" @click="openDialog(null)">新增机构</el-button>
      </div>
    </template>

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="机构类型">
        <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width:160px">
          <el-option label="综合养老院" value="综合养老院" />
          <el-option label="护理院" value="护理院" />
          <el-option label="日间照料中心" value="日间照料中心" />
          <el-option label="社区养老驿站" value="社区养老驿站" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:120px">
          <el-option label="启用" value="1" />
          <el-option label="停用" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="机构名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="code" label="机构编码" width="120" />
      <el-table-column prop="type" label="机构类型" width="130" />
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="contact" label="联系人" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="capacity" label="床位容量" width="100" align="center" />
      <el-table-column prop="staffCount" label="员工数" width="90" align="center" />
      <el-table-column prop="rating" label="评分" width="90" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.rating ? Number(row.rating) : 0" disabled allow-half />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(v) => changeStatus(row, v)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <!-- 新增/编辑对话框 -->
  <el-dialog v-model="dialogVisible" :title="editId ? '编辑机构' : '新增机构'" width="640px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="机构名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入机构名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="机构编码" prop="code">
            <el-input v-model="form.code" placeholder="如 YL-001" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="机构类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择" style="width:100%">
              <el-option label="综合养老院" value="综合养老院" />
              <el-option label="护理院" value="护理院" />
              <el-option label="日间照料中心" value="日间照料中心" />
              <el-option label="社区养老驿站" value="社区养老驿站" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="机构地址" prop="address">
        <el-input v-model="form.address" placeholder="请输入详细地址" />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="联系人" prop="contact">
            <el-input v-model="form.contact" placeholder="联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="邮箱" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="床位容量">
            <el-input-number v-model="form.capacity" :min="0" controls-position="right" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="员工数">
            <el-input-number v-model="form.staffCount" :min="0" controls-position="right" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="评分">
            <el-rate v-model="form.rating" allow-half />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="执照编号">
        <el-input v-model="form.license" placeholder="营业执照/许可证编号" />
      </el-form-item>
      <el-form-item label="机构简介">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入机构简介" />
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
import {
  getInstitutionPage, getInstitutionById, addInstitution, updateInstitution,
  deleteInstitution, updateInstitutionStatus
} from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

const searchForm = reactive({ type: '', status: '' })
const form = reactive({
  name: '', code: '', type: '', address: '', contact: '', phone: '', email: '',
  capacity: 0, staffCount: 0, rating: 0, license: '', description: '', status: 1
})
const rules = {
  name: [{ required: true, message: '请输入机构名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入机构编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择机构类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入机构地址', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: searchForm.type || undefined,
      status: searchForm.status === '' ? undefined : Number(searchForm.status)
    }
    const res = await getInstitutionPage(params)
    tableData.value = (res.data.records || res.data.list || []).map(item => ({ ...item }))
    total.value = res.data.total
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
  loading.value = false
}

const resetSearch = () => {
  Object.assign(searchForm, { type: '', status: '' })
  pageNum.value = 1
  load()
}

const openDialog = async (row) => {
  editId.value = row?.id || null
  Object.assign(form, {
    name: '', code: '', type: '', address: '', contact: '', phone: '', email: '',
    capacity: 0, staffCount: 0, rating: 0, license: '', description: '', status: 1
  })
  if (editId.value) {
    try {
      const res = await getInstitutionById(editId.value)
      Object.assign(form, res.data)
    } catch (e) {
      ElMessage.error(e.message || '获取详情失败')
      return
    }
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = { ...form }
    if (editId.value) {
      payload.id = editId.value
      await updateInstitution(payload)
      ElMessage.success('修改成功')
    } else {
      await addInstitution(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const changeStatus = async (row, val) => {
  try {
    await updateInstitutionStatus(row.id, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success('状态已更新')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该机构？', '提示', { type: 'warning' })
  try {
    await deleteInstitution(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>
