<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>老人档案管理</span>
        <el-button type="primary" @click="openDialog()">新增老人</el-button>
      </div>
    </template>

    <!-- Search -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="姓名">
        <el-input v-model="search.realName" placeholder="请输入姓名" clearable style="width:160px" />
      </el-form-item>
      <el-form-item label="身份证">
        <el-input v-model="search.idCard" placeholder="请输入身份证" clearable style="width:180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- Table -->
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="gender" label="性别" :formatter="row => row.gender === 1 ? '男' : '女'" />
      <el-table-column prop="age" label="年龄" />
      <el-table-column prop="idCard" label="身份证" width="180" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="address" label="地址" show-overflow-tooltip />
      <el-table-column prop="healthLevel" label="健康状态" :formatter="r => ['未知','良好','一般','较差'][r.healthLevel] || '未知'" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/health/elder-detail/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="primary" @click="openRecord(row)">健康记录</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10,20,50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top:16px;justify-content:flex-end"
      @change="loadData"
    />
  </el-card>

  <!-- Dialog -->
  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑老人' : '新增老人'" width="600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年龄" prop="age">
            <el-input-number v-model="form.age" :min="1" :max="150" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="健康状态" prop="healthLevel">
            <el-select v-model="form.healthLevel" style="width:100%">
              <el-option :value="1" label="良好" />
              <el-option :value="2" label="一般" />
              <el-option :value="3" label="较差" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="地址" prop="address">
            <el-input v-model="form.address" type="textarea" placeholder="请输入地址" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <!-- Health Record Dialog -->
  <el-dialog v-model="recordDialogVisible" title="健康记录" width="800px">
    <el-button type="primary" mb-4 @click="openRecordForm">新增记录</el-button>
    <el-table :data="recordList" stripe>
      <el-table-column prop="recordType" label="记录类型" />
      <el-table-column prop="recordValue" label="记录值" />
      <el-table-column prop="recordTime" label="记录时间" />
      <el-table-column prop="doctor" label="医生" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button link type="danger" @click="deleteRecord(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getElderPage, addElder, updateElder, deleteElder, getRecordPage, deleteRecord as delRecord } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const search = reactive({ name: '', idCard: '' })

const dialogVisible = ref(false)
const recordDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentElderId = ref(null)
const recordList = ref([])

const form = reactive({
  name: '', gender: 1, age: 65, phone: '', idCard: '', healthLevel: 1, address: ''
})

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getElderPage({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

const reset = () => { search.name = ''; search.idCard = ''; pageNum.value = 1; loadData() }

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
    isEdit.value = true
  } else {
    formRef.value?.resetFields()
    Object.assign(form, { name: '', gender: 1, age: 65, phone: '', idCard: '', healthLevel: 1, address: '' })
    isEdit.value = false
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) await updateElder(form)
    else await addElder(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该老人档案吗？', '提示', { type: 'warning' })
  try {
    await deleteElder(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* handled */ }
}

const openRecord = async (row) => {
  currentElderId.value = row.id
  try {
    const res = await getRecordPage({ pageNum: 1, pageSize: 100, elderId: row.id })
    recordList.value = res.data.records
  } catch (e) { /* handled */ }
  recordDialogVisible.value = true
}

const openRecordForm = () => ElMessage.info('可在健康记录菜单中新建')
const deleteRecord = async (id) => {
  await ElMessageBox.confirm('确定删除该记录吗？', '提示', { type: 'warning' })
  try {
    await delRecord(id)
    ElMessage.success('删除成功')
    const res = await getRecordPage({ pageNum: 1, pageSize: 100, elderId: currentElderId.value })
    recordList.value = res.data.records
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
