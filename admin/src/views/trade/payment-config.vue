<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>支付设置</span>
        <el-button type="primary" @click="openDialog()">新增配置</el-button>
      </div>
    </template>
    <el-form :inline="true" class="search-bar">
      <el-form-item label="支付类型">
        <el-input v-model="query.paymentType" placeholder="如 微信支付" clearable style="width:180px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="paymentType" label="支付类型" />
      <el-table-column prop="appId" label="AppID" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total>0" class="pager" background layout="total, prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="handlePage" />
  </el-card>
  <el-dialog v-model="dialogVisible" :title="isEdit?'编辑配置':'新增配置'" width="460px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="支付类型" prop="paymentType"><el-input v-model="form.paymentType" placeholder="如 微信支付" /></el-form-item>
      <el-form-item label="AppID" prop="appId"><el-input v-model="form.appId" placeholder="微信/支付宝 AppID" /></el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
      </el-form-item>
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
import { getPaymentConfigPage, addPaymentConfig, updatePaymentConfig, deletePaymentConfig } from '@/api'

const loading = ref(false), tableData = ref([]), total = ref(0), dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const query = reactive({ pageNum: 1, pageSize: 10, paymentType: '' })
const form = reactive({ id: null, paymentType: '', appId: '', status: 1 })
const rules = {
  paymentType: [{ required: true, message: '请输入支付类型', trigger: 'blur' }],
  appId: [{ required: true, message: '请输入 AppID', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getPaymentConfigPage({ ...query, page: query.pageNum, size: query.pageSize })
    const d = res.data || {}
    tableData.value = d.records || d.list || []
    total.value = d.total || 0
  } catch (e) {} finally { loading.value = false }
}
const resetQuery = () => { query.paymentType = ''; query.pageNum = 1; load() }
const handlePage = (p) => { query.pageNum = p; load() }
const openDialog = (row) => {
  if (row) { Object.assign(form, row) } else { formRef.value?.resetFields(); Object.assign(form, { id: null, paymentType: '', appId: '', status: 1 }) }
  isEdit.value = !!row; dialogVisible.value = true
}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    isEdit.value ? await updatePaymentConfig(form) : await addPaymentConfig(form)
    ElMessage.success('保存成功'); dialogVisible.value = false; load()
  } catch (e) {}
}
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该支付配置吗？', '提示', { type: 'warning' })
  try { await deletePaymentConfig(id); ElMessage.success('删除成功'); load() } catch (e) {}
}
onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.search-bar { margin-bottom:12px; }
.pager { margin-top:14px; justify-content:flex-end; display:flex; }
</style>
