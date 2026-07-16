<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>工单设置</span>
        <el-button type="primary" @click="openDialog()">新增设置</el-button>
      </div>
    </template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="serviceType" label="服务类型" width="120" />
      <el-table-column prop="commissionRate" label="默认佣金比例" width="120">
        <template #default="{ row }">{{ (row.commissionRate * 100).toFixed(1) }}%</template>
      </el-table-column>
      <el-table-column prop="minWithdraw" label="最低提现金额" width="120">
        <template #default="{ row }">¥{{ row.minWithdraw }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
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

  <el-dialog v-model="dialogVisible" :title="editId ? '编辑设置' : '新增设置'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="服务类型" prop="serviceType">
        <el-select v-model="form.serviceType" placeholder="请选择">
          <el-option label="家政护工" value="家政护工" />
          <el-option label="康复理疗" value="康复理疗" />
          <el-option label="上门体检" value="上门体检" />
        </el-select>
      </el-form-item>
      <el-form-item label="默认佣金比例" prop="commissionRate">
        <el-input-number v-model="form.commissionRate" :min="0" :max="1" :step="0.01" :precision="2" />
      </el-form-item>
      <el-form-item label="最低提现金额" prop="minWithdraw">
        <el-input-number v-model="form.minWithdraw" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { getServiceOrderSettingPage, getServiceOrderSetting, createServiceOrderSetting, updateServiceOrderSetting, deleteServiceOrderSetting } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), editId = ref(null), formRef = ref(null)

const form = reactive({ serviceType: '', commissionRate: 0.1, minWithdraw: 100, remark: '' })
const rules = {
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  commissionRate: [{ required: true, message: '请输入佣金比例', trigger: 'blur' }],
  minWithdraw: [{ required: true, message: '请输入最低提现金额', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getServiceOrderSettingPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const openDialog = async (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    const res = await getServiceOrderSetting(editId.value)
    Object.assign(form, res.data)
  } else {
    Object.assign(form, { serviceType: '', commissionRate: 0.1, minWithdraw: 100, remark: '' })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateServiceOrderSetting(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createServiceOrderSetting(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {}
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该设置？', '提示', { type: 'warning' })
  await deleteServiceOrderSetting(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
