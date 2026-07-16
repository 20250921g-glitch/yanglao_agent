<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>角色管理</span><el-button type="primary" @click="openDialog()">新增角色</el-button></div>
    </template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleKey" label="权限字符" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/system/role-detail/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
      <el-form-item label="权限字符" prop="roleKey"><el-input v-model="form.roleKey" /></el-form-item>
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
import { getSysRolePage, addSysRole, updateSysRole, deleteSysRole } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, roleName: '', roleKey: '', remark: '' })
const rules = { roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }], roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try { const res = await getSysRolePage({ pageNum: pageNum.value, pageSize: pageSize.value }); tableData.value = res.data.records; total.value = res.data.total }
  catch (e) {}
  finally { loading.value = false }
}

const openDialog = (row) => {
  if (row) { Object.assign(form, row); isEdit.value = true }
  else { formRef.value?.resetFields(); Object.assign(form, { id: null, roleName: '', roleKey: '', remark: '' }); isEdit.value = false }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try { isEdit.value ? await updateSysRole(form) : await addSysRole(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {}
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  try { await deleteSysRole(id); ElMessage.success('删除成功'); load() } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
</style>
