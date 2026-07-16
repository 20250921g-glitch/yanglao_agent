<template>
  <el-card>
    <template #header><span>订单评价</span></template>
    
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单编号" width="140" />
      <el-table-column prop="userName" label="用户姓名" width="100" />
      <el-table-column prop="workerName" label="服务人员" width="100" />
      <el-table-column label="评分" width="150">
        <template #default="{ row }">
          <el-rate v-model="row.score" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
      <el-table-column prop="reply" label="回复内容" show-overflow-tooltip />
      <el-table-column prop="replyTime" label="回复时间" width="160" />
      <el-table-column prop="createTime" label="评价时间" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="!row.reply" type="primary" size="small" @click="openReply(row)">回复</el-button>
          <span v-else style="color:#999">已回复</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <!-- 回复对话框 -->
  <el-dialog v-model="dialogVisible" title="回复评价" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="评价内容">
        <el-input v-model="currentRow.content" type="textarea" :rows="3" disabled />
      </el-form-item>
      <el-form-item label="回复内容" prop="reply">
        <el-input v-model="form.reply" type="textarea" :rows="3" placeholder="请输入回复内容" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitReply">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderEvaluationPage, replyOrderEvaluation } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), formRef = ref(null), currentRow = ref({})
const form = reactive({ reply: '' })
const rules = { reply: [{ required: true, message: '请输入回复内容', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try {
    const res = await getOrderEvaluationPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const openReply = (row) => {
  currentRow.value = row
  form.reply = ''
  dialogVisible.value = true
}

const submitReply = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await replyOrderEvaluation(currentRow.value.id, { reply: form.reply })
  ElMessage.success('回复成功')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>
