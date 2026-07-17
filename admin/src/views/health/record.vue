<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>健康记录</span>
        <el-button type="primary" @click="openDialog()">新增记录</el-button>
      </div>
    </template>
    <el-form :inline="true" class="search-form">
      <el-form-item label="记录类型"><el-input v-model="search.recordType" placeholder="请输入" clearable style="width:140px" /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="elderName" label="老人姓名" />
      <el-table-column prop="recordType" label="记录类型" />
      <el-table-column prop="recordValue" label="记录值" />
      <el-table-column prop="recordTime" label="记录时间" width="160" />
      <el-table-column prop="doctor" label="医生" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增健康记录" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="老人" prop="elderId">
        <el-select v-model="form.elderId" style="width:100%">
          <el-option v-for="e in elderList" :key="e.id" :value="e.id" :label="e.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="记录类型" prop="recordType">
        <el-select v-model="form.recordType" style="width:100%">
          <el-option value="血压" /><el-option value="血糖" /><el-option value="心率" />
          <el-option value="体温" /><el-option value="体重" /><el-option value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="记录值" prop="recordValue"><el-input v-model="form.recordValue" /></el-form-item>
      <el-form-item label="记录时间" prop="recordTime"><el-date-picker v-model="form.recordTime" type="datetime" style="width:100%" /></el-form-item>
      <el-form-item label="医生"><el-input v-model="form.doctor" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecordPage, addRecord, deleteRecord as delRecord, getElderList } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ recordType: '' })
const dialogVisible = ref(false), formRef = ref(null), elderList = ref([])
const form = reactive({ elderId: null, recordType: '', recordValue: '', recordTime: '', doctor: '', remark: '' })
const rules = { elderId: [{ required: true, message: '请选择老人', trigger: 'change' }], recordType: [{ required: true, message: '请输入类型', trigger: 'blur' }], recordValue: [{ required: true, message: '请输入值', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try {
    const res = await getRecordPage({ pageNum: pageNum.value, pageSize: pageSize.value, ...search })
    tableData.value = res.data.records; total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const reset = () => { search.recordType = ''; pageNum.value = 1; load() }

const loadElders = async () => { try { const res = await getElderList(); elderList.value = res.data || [] } catch (e) {} }

const openDialog = () => { formRef.value?.resetFields(); Object.assign(form, { elderId: null, recordType: '', recordValue: '', recordTime: '', doctor: '', remark: '' }); dialogVisible.value = true }

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try { await addRecord(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {}
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  try { await delRecord(id); ElMessage.success('删除成功'); load() } catch (e) {}
}

onMounted(() => { load(); loadElders() })
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.search-form { margin-bottom:16px; }
</style>
