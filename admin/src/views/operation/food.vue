<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>食物管理</span><el-button type="primary" @click="openDialog()">新增食物</el-button></div>
    </template>
    <el-form :inline="true" class="search-form">
      <el-form-item label="名称"><el-input v-model="search.name" clearable style="width:160px" /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button><el-button @click="search.name=''; load()">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="食物名称" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="calorie" label="热量(kcal)" />
      <el-table-column prop="protein" label="蛋白质(g)" />
      <el-table-column prop="fat" label="脂肪(g)" />
      <el-table-column prop="carbs" label="碳水(g)" />
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

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑食物' : '新增食物'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="食物名称" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="分类" prop="category"><el-input v-model="form.category" /></el-form-item>
      <el-row :gutter="16">
        <el-col :span="12"><el-form-item label="热量"><el-input-number v-model="form.calorie" :min="0" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="蛋白质"><el-input-number v-model="form.protein" :min="0" :precision="1" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="脂肪"><el-input-number v-model="form.fat" :min="0" :precision="1" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="碳水"><el-input-number v-model="form.carbs" :min="0" :precision="1" style="width:100%" /></el-form-item></el-col>
      </el-row>
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
import { getFoodPage, addFood, updateFood, deleteFood } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ name: '' })
const dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, name: '', category: '', calorie: 0, protein: 0, fat: 0, carbs: 0 })
const rules = { name: [{ required: true, message: '请输入食物名称', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try { const res = await getFoodPage({ pageNum: pageNum.value, pageSize: pageSize.value, name: search.name }); tableData.value = res.data.records; total.value = res.data.total }
  catch (e) {}
  finally { loading.value = false }
}

const openDialog = (row) => {
  if (row) { Object.assign(form, row); isEdit.value = true }
  else { formRef.value?.resetFields(); Object.assign(form, { id: null, name: '', category: '', calorie: 0, protein: 0, fat: 0, carbs: 0 }); isEdit.value = false }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try { isEdit.value ? await updateFood(form) : await addFood(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {}
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  try { await deleteFood(id); ElMessage.success('删除成功'); load() } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.search-form { margin-bottom:16px; }
</style>
