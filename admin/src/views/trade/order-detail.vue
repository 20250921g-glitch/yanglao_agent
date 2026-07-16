<template>
  <div class="detail-container">
    <!-- 顶部导航 -->
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <span class="page-title">商品订单详情</span>
      </div>
      <el-tag :type="statusTagType" size="large">{{ statusText }}</el-tag>
    </div>

    <!-- 操作按钮组 -->
    <div class="action-bar" v-if="detail.id">
      <!-- 待付款: 关闭订单 / 修改价格 -->
      <template v-if="detail.status === 1">
        <el-button type="danger" @click="handleClose">关闭订单</el-button>
        <el-button type="warning" @click="priceDialogVisible = true">修改价格</el-button>
      </template>
      <!-- 待接单: 手动派单 / 退款 -->
      <template v-if="detail.status === 2">
        <el-button type="primary" @click="openAssignDialog">手动派单</el-button>
        <el-button type="danger" @click="handleRefund">退款</el-button>
      </template>
      <!-- 待服务: 完成订单 / 退款 -->
      <template v-if="detail.status === 3">
        <el-button type="success" @click="handleComplete">完成订单</el-button>
        <el-button type="danger" @click="handleRefund">退款</el-button>
      </template>
      <!-- 已完成: 发起售后 -->
      <template v-if="detail.status === 4">
        <el-button type="primary" @click="$router.push(`/trade/after-sale-detail/${detail.id}`)">发起售后</el-button>
      </template>
    </div>

    <div class="detail-body" v-loading="loading">
      <el-row :gutter="20">
        <!-- 左侧主区 60% -->
        <el-col :span="15">
          <!-- 订单信息卡片 -->
          <el-card class="mb-16">
            <template #header><span class="card-title">订单信息</span></template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="订单编号">{{ detail.orderNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ detail.createTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="订单来源">{{ detail.orderSource || '-' }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">{{ statusText }}</el-descriptions-item>
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
                  <span>数量：{{ detail.quantity ?? '-' }}</span>
                </div>
              </div>
              <div class="product-subtotal">
                小计：<span class="price">¥{{ detail.subtotal ?? '-' }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无商品信息" />
          </el-card>

          <!-- 优惠/价格明细卡片 -->
          <el-card class="mb-16">
            <template #header><span class="card-title">费用明细</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="商品总价">¥{{ detail.totalAmount ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="优惠金额">-¥{{ detail.discountAmount ?? '0.00' }}</el-descriptions-item>
              <el-descriptions-item label="应付款">¥{{ detail.payableAmount ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="实付款">
                <span class="price-highlight">¥{{ detail.actualAmount ?? '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ detail.payMethod || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 备注卡片 -->
          <el-card class="mb-16" v-if="detail.remark">
            <template #header><span class="card-title">备注</span></template>
            <div>{{ detail.remark }}</div>
          </el-card>
        </el-col>

        <!-- 右侧信息区 40% -->
        <el-col :span="9">
          <!-- 用户信息卡片 -->
          <el-card class="mb-16">
            <template #header><span class="card-title">用户信息</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户ID">{{ detail.userId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="用户昵称">{{ detail.userName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detail.userPhone || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 预约信息卡片 -->
          <el-card class="mb-16" v-if="detail.appointmentAddress || detail.appointmentTime">
            <template #header><span class="card-title">预约信息</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="上门地址">{{ detail.appointmentAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="预约时间">{{ detail.appointmentTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="预计时长">{{ detail.estimatedDuration || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 工单/派单信息卡片 -->
          <el-card class="mb-16" v-if="detail.workerName || detail.serviceOrderNo">
            <template #header><span class="card-title">工单信息</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="工单编号">{{ detail.serviceOrderNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="服务人员">{{ detail.workerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="预约上门时间">{{ detail.appointmentTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="派单人">{{ detail.dispatchUser || '-' }}</el-descriptions-item>
              <el-descriptions-item label="派单时间">{{ detail.dispatchTime || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 操作日志 -->
          <el-card v-if="detail.logs && detail.logs.length">
            <template #header><span class="card-title">操作日志</span></template>
            <el-timeline>
              <el-timeline-item v-for="(log, idx) in detail.logs" :key="idx"
                :timestamp="log.time" placement="top">
                {{ log.content }}
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 修改价格对话框 -->
    <el-dialog v-model="priceDialogVisible" title="修改价格" width="400px">
      <el-form :model="priceForm" label-width="100px">
        <el-form-item label="订单编号">{{ detail.orderNo }}</el-form-item>
        <el-form-item label="原价">¥{{ detail.totalAmount }}</el-form-item>
        <el-form-item label="新价格" required>
          <el-input-number v-model="priceForm.newPrice" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="priceForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPrice" :loading="btnLoading">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 手动派单对话框 -->
    <el-dialog v-model="assignDialogVisible" title="手动派单" width="500px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="选择服务人员" prop="nurseId">
          <el-select v-model="assignForm.nurseId" placeholder="请选择服务人员" style="width:100%" filterable>
            <el-option v-for="w in workerList" :key="w.id" :label="`${w.name} - ${w.phone}`" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约上门时间">
          <el-date-picker v-model="assignForm.appointmentTime" type="datetime" placeholder="选择预约时间"
            format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="3" placeholder="可选填派单备注" />
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getProductOrderDetail, updateProductOrderStatus, closeProductOrder, updateProductOrderPrice, dispatchProductOrder, applyRefund, getServiceWorkerList } from '@/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const btnLoading = ref(false)
const detail = ref({})
const priceDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const assignFormRef = ref(null)
const workerList = ref([])

const priceForm = ref({ newPrice: 0, remark: '' })
const assignForm = ref({ nurseId: null, appointmentTime: '', remark: '' })
const assignRules = { nurseId: [{ required: true, message: '请选择服务人员', trigger: 'change' }] }

const statusMap = [
  { text: '-', tag: '' },
  { text: '待付款', tag: 'warning' },
  { text: '待接单', tag: 'primary' },
  { text: '待服务', tag: 'info' },
  { text: '已完成', tag: 'success' },
  { text: '已关闭', tag: 'info' },
  { text: '退款售后', tag: 'danger' }
]

const statusText = computed(() => statusMap[detail.value.status]?.text || '-')
const statusTagType = computed(() => statusMap[detail.value.status]?.tag || 'info')

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getProductOrderDetail(route.params.id)
    detail.value = res.data || res
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const handleClose = async () => {
  try {
    await ElMessageBox.confirm('确认关闭该订单？', '提示', { type: 'warning' })
    await updateProductOrderStatus(route.params.id, 5)
    ElMessage.success('订单已关闭')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const submitAssign = async () => {
  const valid = await assignFormRef.value.validate().catch(() => false)
  if (!valid) return
  btnLoading.value = true
  try {
    const worker = workerList.value.find(w => w.id === assignForm.value.nurseId)
    await dispatchProductOrder(route.params.id, {
      workerId: assignForm.value.nurseId,
      workerName: worker?.name || '',
      appointmentTime: assignForm.value.appointmentTime || '',
      operator: 'admin'
    })
    ElMessage.success('派单成功，订单已进入待服务')
    assignDialogVisible.value = false
    loadDetail()
  } catch (e) {
    ElMessage.error('派单失败')
  } finally {
    btnLoading.value = false
  }
}

const submitPrice = async () => {
  if (!priceForm.value.newPrice && priceForm.value.newPrice !== 0) {
    ElMessage.warning('请输入新价格')
    return
  }
  btnLoading.value = true
  try {
    await updateProductOrderPrice(route.params.id, {
      price: priceForm.value.newPrice,
      remark: priceForm.value.remark
    })
    ElMessage.success('价格已修改')
    priceDialogVisible.value = false
    loadDetail()
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    btnLoading.value = false
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
  assignForm.value.remark = ''
  await loadWorkerList()
  assignDialogVisible.value = true
}

const handleRefund = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入退款原因', '发起退款', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputType: 'textarea',
      type: 'warning'
    })
    const d = detail.value
    // 同步创建售后单，便于售后管理跟进
    await applyRefund({
      orderId: d.id,
      orderNo: d.orderNo,
      userId: d.userId,
      userName: d.userName,
      userPhone: d.userPhone,
      refundAmount: d.actualAmount,
      refundReason: reason || '用户申请退款',
      refundType: '订单退款'
    })
    // 订单状态置为 退款售后(6)，进入退款售后 Tab
    await updateProductOrderStatus(route.params.id, 6)
    ElMessage.success('已提交退款申请，进入退款售后')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('操作失败')
  }
}

const handleComplete = async () => {
  try {
    await ElMessageBox.confirm('确认订单完成？', '提示', { type: 'success' })
    await updateProductOrderStatus(route.params.id, 4)
    ElMessage.success('订单已完成')
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
.price { color: #FF4D4F; font-weight: 600; }
.price-highlight { color: #FF4D4F; font-weight: 700; font-size: 16px; }
</style>
