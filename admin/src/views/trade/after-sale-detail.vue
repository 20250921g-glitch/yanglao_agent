<template>
  <div class="detail-container">
    <!-- 顶部导航 -->
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <span class="page-title">售后详情</span>
      </div>
      <el-tag :type="statusTagType" size="large">{{ statusText }}</el-tag>
    </div>

    <!-- 操作按钮组 -->
    <div class="action-bar" v-if="detail.id">
      <!-- 处理中: 同意退款 / 拒绝退款 -->
      <template v-if="detail.status === 0">
        <el-button type="success" @click="openApproveDialog">同意退款</el-button>
        <el-button type="danger" @click="openRejectDialog">拒绝退款</el-button>
      </template>
      <!-- 售后关闭: 显示关闭原因 -->
      <template v-if="detail.status === 2">
        <el-alert v-if="detail.closeReason" :title="`关闭原因：${detail.closeReason}`" type="warning" :closable="false" />
      </template>
    </div>

    <div class="detail-body" v-loading="loading">
      <!-- 售后信息卡片 -->
      <el-card class="mb-16">
        <template #header><span class="card-title">售后信息</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="售后单号">{{ detail.refundNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请退款金额">
            <span class="price">¥{{ detail.refundAmount || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退款方式">
            {{ refundTypeMap[detail.refundType] || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="3">{{ detail.refundReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退款说明" :span="3">{{ detail.refundDescription || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ detail.auditor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-row :gutter="20">
        <!-- 左侧: 订单信息 + 商品信息 -->
        <el-col :span="12">
          <!-- 订单信息卡片 -->
          <el-card class="mb-16">
            <template #header><span class="card-title">订单信息</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="订单编号">{{ detail.orderNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ detail.orderCreateTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="付款时间">{{ detail.payTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="接单时间">{{ detail.acceptTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="成交时间">{{ detail.completeTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">{{ orderStatusText }}</el-descriptions-item>
              <el-descriptions-item label="核销人员">{{ detail.verifier || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ detail.payMethod || '-' }}</el-descriptions-item>
              <el-descriptions-item label="订单来源">{{ detail.orderSource || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 商品信息卡片 -->
          <el-card class="mb-16">
            <template #header><span class="card-title">商品信息</span></template>
            <div class="product-item" v-if="detail.product">
              <el-image :src="detail.product.picUrl" fit="cover" class="product-img" />
              <div class="product-info">
                <div class="product-name">{{ detail.product.name || '-' }}</div>
                <div class="product-price">
                  <span>单价：¥{{ detail.product.price ?? '-' }}</span>
                  <span>数量：{{ detail.product.quantity ?? '-' }}</span>
                </div>
              </div>
              <div class="product-subtotal">
                实付：<span class="price">¥{{ detail.product.actualAmount ?? '-' }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无商品信息" />
          </el-card>
        </el-col>

        <!-- 右侧: 用户信息 + 处理记录 -->
        <el-col :span="12">
          <!-- 用户信息卡片 -->
          <el-card class="mb-16">
            <template #header><span class="card-title">用户信息</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户ID">{{ detail.userId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="用户昵称">{{ detail.userName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detail.userPhone || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 处理记录时间线 -->
          <el-card v-if="detail.records && detail.records.length">
            <template #header><span class="card-title">处理记录</span></template>
            <el-timeline>
              <el-timeline-item v-for="(record, idx) in detail.records" :key="idx"
                :timestamp="record.time" placement="top" :type="recordType(record.type)">
                <div>{{ record.content }}</div>
                <div v-if="record.operator" class="record-operator">操作人：{{ record.operator }}</div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 同意退款对话框 -->
    <el-dialog v-model="approveDialogVisible" title="同意退款" width="420px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="退款金额">
          <span class="price">¥{{ detail.refundAmount }}</span>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="请输入处理说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove" :loading="btnLoading">确认退款</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝退款对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝退款" width="420px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.remark" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :loading="btnLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getRefundDetail, approveRefund, rejectRefund, processRefund } from '@/api'

const route = useRoute()
const loading = ref(false)
const btnLoading = ref(false)
const detail = ref({})
const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const approveForm = ref({ remark: '' })
const rejectForm = ref({ remark: '' })

const refundTypeMap = { 1: '仅退款', 2: '退货退款' }

const statusMap = [
  { text: '处理中', tag: 'warning' },
  { text: '售后完成', tag: 'success' },
  { text: '售后关闭', tag: 'info' }
]

const statusText = computed(() => statusMap[detail.value.status]?.text || '-')
const statusTagType = computed(() => statusMap[detail.value.status]?.tag || 'info')

const orderStatusMap = ['', '待支付', '待接单', '待服务', '已完成', '已关闭', '退款售后']
const orderStatusText = computed(() => orderStatusMap[detail.value.orderStatus] || '-')

const recordType = (type) => {
  const map = { 1: 'success', 2: 'danger', 3: 'primary', 4: 'info' }
  return map[type] || 'info'
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getRefundDetail(route.params.id)
    detail.value = res.data || res
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const openApproveDialog = () => {
  approveForm.value.remark = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  btnLoading.value = true
  try {
    await approveRefund(route.params.id, 'admin', approveForm.value.remark)
    ElMessage.success('已同意退款')
    approveDialogVisible.value = false
    loadDetail()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    btnLoading.value = false
  }
}

const openRejectDialog = () => {
  rejectForm.value.remark = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.value.remark.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  btnLoading.value = true
  try {
    await rejectRefund(route.params.id, 'admin', rejectForm.value.remark)
    ElMessage.success('已拒绝退款')
    rejectDialogVisible.value = false
    loadDetail()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    btnLoading.value = false
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
.price { color: #FF4D4F; font-weight: 600; }

.product-item {
  display: flex; align-items: center; gap: 16px; padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.product-item:last-child { border-bottom: none; }
.product-img { width: 80px; height: 80px; border-radius: 8px; flex-shrink: 0; }
.product-info { flex: 1; }
.product-name { font-weight: 600; margin-bottom: 8px; color: #303133; }
.product-price { font-size: 13px; color: #A0AEC0; display: flex; gap: 16px; }
.product-subtotal { font-size: 14px; color: #606266; white-space: nowrap; }
.record-operator { font-size: 12px; color: #A0AEC0; margin-top: 4px; }
</style>
