<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>消息群发</span><el-button type="primary" @click="openDialog()">新建消息</el-button></div>
    </template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="消息标题" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="110" />
      <el-table-column prop="target" label="发送对象" width="100">
        <template #default="{ row }">{{ row.target === 'all' ? '全部用户' : '指定用户' }}</template>
      </el-table-column>
      <el-table-column prop="sendTime" label="发送时间" width="160" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'已发送':'草稿' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
  <el-dialog v-model="dialogVisible" title="新建消息" width="550px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="消息标题" prop="title"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="消息类型" prop="type">
        <el-select v-model="form.type" style="width:100%">
          <el-option value="系统通知" label="系统通知" />
          <el-option value="活动推送" label="活动推送" />
        </el-select>
      </el-form-item>
      <el-form-item label="发送对象">
        <el-radio-group v-model="form.target">
          <el-radio label="all">全部用户</el-radio>
          <el-radio label="specific">指定用户</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="消息内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="handleSend">发送</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAppMessagePage, addAppMessage, deleteAppMessage } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), formRef = ref(null)
const form = reactive({ title: '', content: '', type: '系统通知', target: 'all', status: 1 })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }], content: [{ required: true, message: '请输入内容', trigger: 'blur' }] }

const load = async () => { loading.value = true; try { const res = await getAppMessagePage({ pageNum: pageNum.value, pageSize: pageSize.value }); tableData.value = res.data.records; total.value = res.data.total } catch (e) {} finally { loading.value = false } }
const openDialog = () => { formRef.value?.resetFields(); Object.assign(form, { title: '', content: '', type: '系统通知', target: 'all', status: 1 }); dialogVisible.value = true }
const handleSend = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; try { await addAppMessage(form); ElMessage.success('发送成功'); dialogVisible.value = false; load() } catch (e) {} }
const handleDelete = async (id) => { await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }); try { await deleteAppMessage(id); ElMessage.success('删除成功'); load() } catch (e) {} }
onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
</style>
