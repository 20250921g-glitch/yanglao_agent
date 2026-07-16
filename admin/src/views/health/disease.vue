<template>
  <div class="disease-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>疾病宝典</span>
          <el-button type="primary" @click="openDialog()">新增疾病信息</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="疾病名称">
          <el-input v-model="search.name" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="search.category" placeholder="请选择" clearable style="width:160px">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" placeholder="请选择" clearable style="width:120px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="pageNum = 1; loadData()">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="疾病名称" width="150" />
        <el-table-column prop="categoryText" label="分类" width="150">
          <template #default="{ row }">{{ row.categoryText || categoryLabel(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="symptoms" label="症状" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(val) => changeStatus(row, val)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:20px;justify-content:flex-end"
        @size-change="loadData" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑疾病信息' : '新增疾病信息'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="疾病名称" prop="name">
          <el-input v-model="form.name" placeholder="如：高血压" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width:100%" placeholder="请选择分类">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="症状" prop="symptoms">
          <el-input v-model="form.symptoms" type="textarea" :rows="3" placeholder="请输入常见症状，逗号分隔" />
        </el-form-item>
        <el-form-item label="治疗方案">
          <el-input v-model="form.treatment" type="textarea" :rows="3" placeholder="请输入治疗方案" />
        </el-form-item>
        <el-form-item label="注意事项">
          <el-input v-model="form.precautions" type="textarea" :rows="3" placeholder="请输入注意事项" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusOn" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="btnLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDiseasePage, addDisease, updateDisease, deleteDisease, changeDiseaseStatus
} from '@/api'

const loading = ref(false)
const btnLoading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const categoryOptions = [
  { value: 'cardiovascular', label: '心血管疾病' },
  { value: 'diabetes', label: '糖尿病' },
  { value: 'respiratory', label: '呼吸系统疾病' },
  { value: 'neurological', label: '神经系统疾病' },
  { value: 'musculoskeletal', label: '骨关节疾病' }
]
const categoryLabel = (val) => categoryOptions.find(c => c.value === val)?.label || val || '-'

const search = reactive({ name: '', category: '', status: '' })

const emptyForm = () => ({
  id: null, name: '', category: 'cardiovascular', symptoms: '', treatment: '', precautions: '', statusOn: true
})
const form = reactive(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入疾病名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  symptoms: [{ required: true, message: '请输入常见症状', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (search.name) params.name = search.name
    if (search.category) params.category = search.category
    if (search.status !== '') params.status = search.status
    const res = await getDiseasePage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  search.name = ''
  search.category = ''
  search.status = ''
  pageNum.value = 1
  loadData()
}

const openDialog = () => {
  isEdit.value = false
  formRef.value?.resetFields()
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    category: row.category,
    symptoms: row.symptoms,
    treatment: row.treatment || '',
    precautions: row.precautions || '',
    statusOn: row.status === 1
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  btnLoading.value = true
  const payload = {
    name: form.name,
    category: form.category,
    symptoms: form.symptoms,
    treatment: form.treatment,
    precautions: form.precautions,
    status: form.statusOn ? 1 : 0
  }
  try {
    if (isEdit.value) {
      payload.id = form.id
      await updateDisease(payload)
      ElMessage.success('修改成功')
    } else {
      await addDisease(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    btnLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该疾病信息？', '提示', { type: 'warning' })
    await deleteDisease(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const changeStatus = async (row, val) => {
  const next = val ? 1 : 0
  try {
    await changeDiseaseStatus(row.id, next)
    row.status = next
    ElMessage.success(next === 1 ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error('状态修改失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.disease-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 20px; }
</style>
