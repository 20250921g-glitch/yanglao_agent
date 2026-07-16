<template>
  <div class="detail-container">
    <!-- 顶部导航 -->
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <span class="page-title">工单详情</span>
      </div>
      <el-tag :type="statusTagType" size="large">{{ statusText }}</el-tag>
    </div>

    <!-- 操作按钮组 -->
    <div class="action-bar" v-if="detail.id">
      <template v-if="detail.status === 1">
        <el-button type="primary" @click="openAssignDialog">派单</el-button>
        <el-button type="danger" @click="handleCancel">取消</el-button>
      </template>
      <template v-if="detail.status === 2">
        <el-button type="success" @click="handleStartService">开始服务</el-button>
        <el-button type="danger" @click="handleCancel">取消</el-button>
      </template>
      <template v-if="detail.status === 3">
        <el-button type="success" @click="handleCompleteService">完成服务</el-button>
      </template>
    </div>

    <div class="detail-body" v-loading="loading">
      <el-card class="mb-16">
        <template #header><span class="card-title">基本信息</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="工单编号">{{ detail.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ detail.serviceType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="服务名称">{{ detail.serviceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ detail.elderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="服务价格">¥{{ detail.price || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ detail.appointmentTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="服务人员">{{ detail.nurseName || '待分配' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="mb-16">
            <template #header><span class="card-title">服务进度</span></template>
            <el-timeline>
              <el-timeline-item timestamp="待接单" placement="top" :type="detail.status >= 1 ? 'success' : ''">
                <template #icon><el-icon v-if="detail.status >= 1"><CircleCheck /></el-icon></template>
                <p>用户提交服务订单</p>
              </el-timeline-item>
              <el-timeline-item timestamp="已接单" placement="top" :type="detail.status >= 2 ? 'success' : ''">
                <template #icon><el-icon v-if="detail.status >= 2"><CircleCheck /></el-icon></template>
                <p>服务人员已接单</p>
              </el-timeline-item>
              <el-timeline-item timestamp="服务中" placement="top" :type="detail.status >= 3 ? 'success' : ''">
                <template #icon><el-icon v-if="detail.status >= 3"><CircleCheck /></el-icon></template>
                <p>服务人员正在提供服务</p>
              </el-timeline-item>
              <el-timeline-item timestamp="已完成" placement="top" :type="detail.status >= 4 ? 'success' : ''">
                <template #icon><el-icon v-if="detail.status >= 4"><CircleCheck /></el-icon></template>
                <p>服务已完成</p>
              </el-timeline-item>
            </el-timeline>
          </el-card>

          <el-card>
            <template #header><span class="card-title">订单评价</span></template>
            <div v-if="evaluation.id">
              <el-rate v-model="evaluation.score" disabled />
              <p style="margin-top:8px">{{ evaluation.content }}</p>
              <p v-if="evaluation.reply" style="margin-top:8px;color:#00C4A1">回复：{{ evaluation.reply }}</p>
            </div>
            <p v-else style="color:#999">暂无评价</p>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="mb-16">
            <template #header><span class="card-title">佣金信息</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="订单金额">¥{{ detail.price || '0.00' }}</el-descriptions-item>
              <el-descriptions-item label="佣金比例">{{ commissionRate || '0' }}%</el-descriptions-item>
              <el-descriptions-item label="佣金金额">¥{{ commissionAmount || '0.00' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card>
            <template #header><span class="card-title">操作记录</span></template>
            <el-table :data="operationLogs" stripe size="small">
              <el-table-column prop="action" label="操作" width="120" />
              <el-table-column prop="operator" label="操作人" width="100" />
              <el-table-column prop="time" label="操作时间" width="160" />
              <el-table-column prop="remark" label="备注" show-overflow-tooltip />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="assignDialogVisible" title="派单" width="500px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="110px">
        <el-form-item label="选择服务人员" prop="nurseId">
          <el-select v-model="assignForm.nurseId" placeholder="请选择服务人员" style="width:100%" filterable>
            <el-option v-for="w in workerList" :key="w.id" :label="`${w.name} - ${w.serviceType} - ${w.region}`" :value="w.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="btnLoading">确认派单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, CircleCheck } from '@element-plus/icons-vue'
import { getServiceOrderDetail, assignWorker, cancelServiceOrder, startService, completeService, getServiceWorkerList, getOrderEvaluationPage } from '@/api'

const route = useRoute()
const loading = ref(false)
const btnLoading = ref(false)
const detail = ref({})
const workerList = ref([])
const assignDialogVisible = ref(false)
const assignFormRef = ref(null)
const assignForm = ref({ nurseId: null })
const assignRules = {
  nurseId: [{ required: true, message: '请选择服务人员', trigger: 'change' }]
}

const evaluation = ref({})
const operationLogs = ref([])
const commissionRate = ref(0)
const commissionAmount = ref(0)

const statusMap = [
  { text: '-', tag: '' },
  { text: '待接单', tag: 'warning' },
  { text: '已接单', tag: 'primary' },
  { text: '服务中', tag: 'primary' },
  { text: '已完成', tag: 'success' },
  { text: '已取消', tag: 'info' }
]

const statusText = computed(() => statusMap[detail.value.status]?.text || '-')
const statusTagType = computed(() => statusMap[detail.value.status]?.tag || 'info')

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getServiceOrderDetail(route.params.id)
    detail.value = res.data || res
    loadEvaluation()
    loadCommissionInfo()
    buildOperationLogs()
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const loadEvaluation = async () => {
  try {
    const res = await getOrderEvaluationPage({ pageNum: 1, pageSize: 1, orderId: route.params.id })
    if (res.data.records && res.data.records.length > 0) {
      evaluation.value = res.data.records[0]
    }
  } catch (e) {}
}

const loadCommissionInfo = async () => {
  commissionRate.value = 0
  commissionAmount.value = 0
  if (detail.value.serviceType) {
    const rates = { '家政护理': 20, '康复理疗': 25, '上门体检': 15 }
    commissionRate.value = rates[detail.value.serviceType] || 20
    commissionAmount.value = ((detail.value.price || 0) * commissionRate.value / 100).toFixed(2)
  }
}

const buildOperationLogs = () => {
  operationLogs.value = []
  if (detail.value.createTime) {
    operationLogs.value.push({ action: '创建工单', operator: '系统', time: detail.value.createTime, remark: '' })
  }
  if (detail.value.nurseName && detail.value.status >= 2) {
    operationLogs.value.push({ action: '派单', operator: '管理员', time: detail.value.updateTime, remark: `分配给${detail.value.nurseName}` })
  }
  if (detail.value.status >= 3) {
    operationLogs.value.push({ action: '开始服务', operator: detail.value.nurseName || '系统', time: detail.value.updateTime, remark: '' })
  }
  if (detail.value.status >= 4) {
    operationLogs.value.push({ action: '完成服务', operator: detail.value.nurseName || '系统', time: detail.value.updateTime, remark: '' })
  }
  if (detail.value.status === 5) {
    operationLogs.value.push({ action: '取消工单', operator: '系统', time: detail.value.updateTime, remark: '' })
  }
}

const loadWorkerList = async () => {
  try {
    const res = await getServiceWorkerList()
    workerList.value = res.data || []
  } catch (e) {}
}

const openAssignDialog = async () => {
  assignForm.value.nurseId = null
  await loadWorkerList()
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  const valid = await assignFormRef.value.validate().catch(() => false)
  if (!valid) return
  btnLoading.value = true
  try {
    const worker = workerList.value.find(w => w.id === assignForm.value.nurseId)
    await assignWorker(route.params.id, assignForm.value.nurseId, worker?.name || '')
    ElMessage.success('派单成功')
    assignDialogVisible.value = false
    loadDetail()
  } catch (e) {
    ElMessage.error('派单失败')
  } finally {
    btnLoading.value = false
  }
}

const handleStartService = async () => {
  try {
    await ElMessageBox.confirm('确认开始服务？', '提示', { type: 'info' })
    await startService(route.params.id)
    ElMessage.success('已开始服务')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleCompleteService = async () => {
  try {
    await ElMessageBox.confirm('确认完成服务？', '提示', { type: 'success' })
    await completeService(route.params.id)
    ElMessage.success('服务已完成')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认取消该工单？', '取消确认', { type: 'warning' })
    await cancelServiceOrder(route.params.id)
    ElMessage.success('工单已取消')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.detail-container { padding: 20px; }
.detail-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px; padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 18px; font-weight: 600; }
.action-bar { margin-bottom: 16px; display: flex; gap: 10px; flex-wrap: wrap; }
.detail-body { min-height: 400px; }
.mb-16 { margin-bottom: 16px; }
.card-title { font-weight: 600; color: #303133; }
.price-highlight { color: #FF4D4F; font-weight: 700; font-size: 16px; }
.ml-8 { margin-left: 8px; color: #A0AEC0; font-size: 13px; }
</style>
