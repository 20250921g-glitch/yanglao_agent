<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>家属管理</span>
        <el-button type="primary" @click="openDialog()">新增家属</el-button>
      </div>
    </template>
    <el-form :inline="true" class="search-form">
      <el-form-item label="姓名"><el-input v-model="search.name" placeholder="姓名" clearable style="width:140px" /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button><el-button @click="search.name=''; load()">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="elderName" label="关联老人" />
      <el-table-column prop="familyName" label="家属姓名" />
      <el-table-column prop="relation" label="关系" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑家属' : '新增家属'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="关联老人" prop="elderId">
        <el-select v-model="form.elderId" style="width:100%">
          <el-option v-for="e in elderList" :key="e.id" :value="e.id" :label="e.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="家属(用户)" prop="appUserId">
        <el-select v-model="form.appUserId" style="width:100%" @change="onUserChange" placeholder="选择真实家属账号">
          <el-option v-for="u in candidateList" :key="u.id" :value="u.id" :label="(u.realName || u.username) + ' (' + u.phone + ')'" />
        </el-select>
      </el-form-item>
      <el-form-item label="家属姓名"><el-input v-model="form.familyName" disabled /></el-form-item>
      <el-form-item label="关系" prop="relation"><el-input v-model="form.relation" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.phone" disabled /></el-form-item>
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
import { getAllFamily, addFamily, updateFamily, deleteFamily, getElderList, getFamilyCandidates } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ name: '' })
const dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, elderId: null, appUserId: null, familyName: '', relation: '', phone: '', remark: '' })
const elderList = ref([])
const candidateList = ref([])
const rules = { elderId: [{ required: true, message: '请选择关联老人', trigger: 'change' }], appUserId: [{ required: true, message: '请选择家属账号', trigger: 'change' }], relation: [{ required: true, message: '请输入关系', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try {
    const res = await getAllFamily()
    const all = res.data || []
    const keyword = (search.name || '').trim()
    tableData.value = keyword ? all.filter(r => r.familyName && r.familyName.includes(keyword)) : all
    total.value = tableData.value.length
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

const loadElders = async () => {
  try { const res = await getElderList(); elderList.value = res.data || [] } catch (e) {}
}

const loadCandidates = async () => {
  try { const res = await getFamilyCandidates(); candidateList.value = res.data || [] } catch (e) {}
}

const onUserChange = (id) => {
  const u = candidateList.value.find(x => x.id === id)
  if (u) {
    form.familyName = u.realName || u.username
    form.phone = u.phone
  }
}

const openDialog = (row) => {
  if (row) { Object.assign(form, row); isEdit.value = true }
  else { formRef.value?.resetFields(); Object.assign(form, { id: null, elderId: null, appUserId: null, familyName: '', relation: '', phone: '', remark: '' }); isEdit.value = false }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try { isEdit.value ? await updateFamily(form) : await addFamily(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
  catch (e) {}
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  try { await deleteFamily(id); ElMessage.success('删除成功'); load() } catch (e) {}
}

onMounted(() => { load(); loadElders(); loadCandidates() })
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.search-form { margin-bottom:16px; }
</style>
