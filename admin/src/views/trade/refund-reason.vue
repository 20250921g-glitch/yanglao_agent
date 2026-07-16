<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>退款原因</span><el-button type="primary" @click="openDialog()">新增原因</el-button></div>
    </template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="reason" label="退款原因" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
  <el-dialog v-model="dialogVisible" :title="isEdit?'编辑原因':'新增原因'" width="450px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="退款原因" prop="reason"><el-input v-model="form.reason" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width:100%" /></el-form-item>
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
import { getRefundReasonList, addRefundReason, updateRefundReason, deleteRefundReason } from '@/api'

const loading = ref(false), tableData = ref([]), dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, reason: '', sort: 0, status: 1 })
const rules = { reason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }] }

const load = async () => { loading.value = true; try { const res = await getRefundReasonList(); tableData.value = res.data || [] } catch (e) {} finally { loading.value = false } }
const openDialog = (row) => { row ? Object.assign(form, row) : (formRef.value?.resetFields(), Object.assign(form, { id: null, reason: '', sort: 0, status: 1 })); isEdit.value = !!row; dialogVisible.value = true }
const handleSave = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; try { isEdit.value ? await updateRefundReason(form) : await addRefundReason(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {} }
const handleDelete = async (id) => { await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }); try { await deleteRefundReason(id); ElMessage.success('删除成功'); load() } catch (e) {} }
onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
</style>
