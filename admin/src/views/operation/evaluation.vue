<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>测评管理</span><el-button type="primary" @click="openDialog()">新增测评</el-button></div>
    </template>
    <el-form :inline="true" class="search-form">
      <el-form-item label="标题"><el-input v-model="search.title" clearable style="width:160px" /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button><el-button @click="search.title=''; load()">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="elderName" label="老人姓名" />
      <el-table-column prop="title" label="测评标题" />
      <el-table-column prop="score" label="评分" />
      <el-table-column prop="evaluator" label="评估人" />
      <el-table-column prop="createTime" label="时间" width="160" />
      <el-table-column label="操作" width="130">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/operation/evaluation-detail/${row.id}`)">详情</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增测评" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="老人" prop="elderId">
        <el-select v-model="form.elderId" style="width:100%">
          <el-option v-for="e in elderList" :key="e.id" :value="e.id" :label="e.realName" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="评分"><el-input-number v-model="form.score" :min="0" :max="100" style="width:100%" /></el-form-item>
      <el-form-item label="评估人"><el-input v-model="form.evaluator" /></el-form-item>
      <el-form-item label="结论"><el-input v-model="form.conclusion" type="textarea" /></el-form-item>
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
import { getEvaluationPage, addEvaluation, deleteEvaluation, getElderList } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ title: '' })
const dialogVisible = ref(false), formRef = ref(null), elderList = ref([])
const form = reactive({ elderId: null, title: '', score: 0, evaluator: '', conclusion: '' })
const rules = { elderId: [{ required: true, message: '请选择老人', trigger: 'change' }], title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try { const res = await getEvaluationPage({ pageNum: pageNum.value, pageSize: pageSize.value, title: search.title }); tableData.value = res.data.records; total.value = res.data.total }
  catch (e) {}
  finally { loading.value = false }
}

const loadElders = async () => { try { const res = await getElderList(); elderList.value = res.data || [] } catch (e) {} }
const openDialog = () => { formRef.value?.resetFields(); Object.assign(form, { elderId: null, title: '', score: 0, evaluator: '', conclusion: '' }); dialogVisible.value = true }

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try { await addEvaluation(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {}
}
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  try { await deleteEvaluation(id); ElMessage.success('删除成功'); load() } catch (e) {}
}

onMounted(() => { load(); loadElders() })
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.search-form { margin-bottom:16px; }
</style>
