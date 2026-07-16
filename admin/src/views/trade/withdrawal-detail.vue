<template>
  <div v-loading="loading">
    <el-empty v-if="notFound" description="未找到该提现记录" />

    <template v-else>
      <!-- 提现信息卡片 -->
      <el-card style="margin-bottom:16px">
        <template #header><span style="font-weight:bold;font-size:16px">提现信息</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="提现ID">{{ info.id }}</el-descriptions-item>
          <el-descriptions-item label="提现单号">{{ info.withdrawNo }}</el-descriptions-item>
          <el-descriptions-item label="提现用户">
            <el-button link type="primary" @click="goWorkerDetail">{{ info.workerName || '—' }}</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="提现金额">
            <span style="color:#52C41A;font-size:18px;font-weight:bold">¥{{ Number(info.amount || 0).toLocaleString() }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="提现方式">
            <el-tag size="small">{{ info.withdrawType || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提现账户">{{ info.bankCard || info.bankName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ info.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="statusMeta.type" size="large">{{ statusMeta.name }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 处理信息 -->
      <el-card v-if="info.status && info.status !== 0" style="margin-bottom:16px">
        <template #header><span style="font-weight:bold;font-size:16px">处理信息</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="处理人">{{ info.auditorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ info.auditTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">{{ info.auditRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="info.status === 2" label="拒绝原因" :span="3">
            <span style="color:#FF4D4F">{{ info.rejectReason }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 关联服务订单（暂无独立 API，展示空态） -->
      <el-card style="margin-bottom:16px">
        <template #header><span style="font-weight:bold;font-size:16px">关联服务订单</span></template>
        <el-empty description="暂无关联订单数据" />
      </el-card>

      <!-- 操作按钮 -->
      <el-card>
        <template #header><span style="font-weight:bold;font-size:16px">操作</span></template>
        <div style="display:flex;gap:12px">
          <el-button type="success" size="large" @click="handleApprove" v-if="info.status === 0">
            <el-icon><Check /></el-icon> 审核通过
          </el-button>
          <el-button type="danger" size="large" @click="handleReject" v-if="info.status === 0">
            <el-icon><Close /></el-icon> 审核拒绝
          </el-button>
          <el-button v-if="info.status !== 0" disabled type="info" size="large">该提现已处理</el-button>
        </div>
      </el-card>

      <!-- 拒绝弹窗 -->
      <el-dialog v-model="rejectDialogVisible" title="拒绝提现" width="400px">
        <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="80px">
          <el-form-item label="拒绝原因" prop="reason">
            <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitReject">确认拒绝</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import { getWithdrawalPage, handleWithdrawal } from '@/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false), notFound = ref(false)
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({ reason: '' })
const rejectRules = { reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }] }

const info = ref({})

const statusMeta = computed(() => {
  if (info.value.statusText) {
    const type = info.value.status === 1 ? 'success' : info.value.status === 2 ? 'danger' : 'warning'
    return { name: info.value.statusText, type }
  }
  if (info.value.status === 1) return { name: '已通过', type: 'success' }
  if (info.value.status === 2) return { name: '已拒绝', type: 'danger' }
  return { name: '待审核', type: 'warning' }
})

const load = async () => {
  loading.value = true
  try {
    const routeId = Number(route.params.id)
    const res = await getWithdrawalPage({ pageSize: 1000 })
    const records = res.data.records || []
    const found = records.find(r => r.id === routeId)
    if (found) { info.value = found; notFound.value = false }
    else { notFound.value = true }
  } catch (e) {
    // 响应拦截器已弹出错误提示
    notFound.value = true
  } finally {
    loading.value = false
  }
}

const goWorkerDetail = () => {
  if (info.value.workerId) router.push(`/service/worker-detail/${info.value.workerId}`)
}

const handleApprove = async () => {
  await ElMessageBox.confirm('确认审核通过该提现申请？', '提示', { type: 'warning' })
  try {
    await handleWithdrawal(info.value.id, { status: 1, auditorName: 'admin' })
    ElMessage.success('审核通过')
    load()
  } catch (e) {}
}

const handleReject = () => {
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await handleWithdrawal(info.value.id, { status: 2, rejectReason: rejectForm.reason, auditorName: 'admin' })
    ElMessage.success('已拒绝提现')
    rejectDialogVisible.value = false
    load()
  } catch (e) {}
}

onMounted(load)
</script>
