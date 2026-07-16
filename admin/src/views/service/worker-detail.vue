<template>
  <div class="worker-detail" v-loading="loading">
    <!-- 左侧基本信息 -->
    <el-card class="left-card">
      <div class="info-panel">
        <el-avatar :size="80" :src="info.avatar" v-if="info.avatar" />
        <el-avatar :size="80" style="background:#00C4A1;font-size:32px" v-else>{{ info.name?.charAt(0) }}</el-avatar>
        <div class="info-name">{{ info.name }}</div>
        <div class="info-tags">
          <el-tag type="primary" size="small">{{ info.serviceType }}</el-tag>
          <el-tag :type="info.status === 1 ? 'success' : 'info'" size="small">
            {{ info.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </div>
        <el-divider />
        <div class="info-item"><span class="label">手机号：</span>{{ info.phone }}</div>
        <div class="info-item"><span class="label">服务区域：</span>{{ info.region }}</div>
        <div class="info-item"><span class="label">审核状态：</span>
          <el-tag :type="['warning','success','danger'][info.auditStatus]" size="small">
            {{ ['待审核','已通过','已拒绝'][info.auditStatus] }}
          </el-tag>
        </div>
        <div class="info-item"><span class="label">创建时间：</span>{{ info.createTime }}</div>
      </div>
    </el-card>

    <!-- 右侧多 Tab -->
    <el-card class="right-card">
      <el-tabs v-model="activeTab">
        <!-- Tab1: 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ info.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ info.gender === 1 ? '男' : '女' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ info.phone }}</el-descriptions-item>
            <el-descriptions-item label="身份证">{{ info.idCard ? maskIdCard(info.idCard) : '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="服务区域">{{ info.region }}</el-descriptions-item>
            <el-descriptions-item label="服务类型">{{ info.serviceType }}</el-descriptions-item>
            <el-descriptions-item label="标签" :span="2">
              <el-tag v-for="(tag, idx) in workerTags" :key="idx" :style="{ backgroundColor: tag.color || '#00C4A1' }" size="small" style="margin-right:4px;color:#fff">{{ tag.tagName }}</el-tag>
              <span v-if="workerTags.length === 0" style="color:#999">暂无标签</span>
            </el-descriptions-item>
            <el-descriptions-item label="个人简介" :span="2">{{ info.intro || '暂无简介' }}</el-descriptions-item>
            <el-descriptions-item label="银行卡">{{ info.bankCard ? maskBankCard(info.bankCard) : '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="开户行">{{ info.bankName || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="职业证书">{{ info.certificate || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="允许打赏">{{ info.allowTip === 1 ? '是' : '否' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- Tab2: 服务统计 -->
        <el-tab-pane label="服务统计" name="stats">
          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="6">
              <el-card shadow="hover" body-style="text-align:center">
                <div style="font-size:24px;color:#00C4A1;font-weight:bold">{{ serviceStats.totalOrders }}</div>
                <div style="color:#666">总订单数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" body-style="text-align:center">
                <div style="font-size:24px;color:#52C41A;font-weight:bold">{{ serviceStats.completedOrders }}</div>
                <div style="color:#666">完成订单</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" body-style="text-align:center">
                <div style="font-size:24px;color:#FAAD14;font-weight:bold">¥{{ serviceStats.monthIncome }}</div>
                <div style="color:#666">本月收入</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" body-style="text-align:center">
                <div style="font-size:24px;color:#FF4D4F;font-weight:bold">{{ serviceStats.goodRate }}%</div>
                <div style="color:#666">好评率</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- Tab3: 服务记录 -->
        <el-tab-pane label="服务记录" name="records">
          <el-table :data="serviceRecords" stripe size="small">
            <el-table-column prop="orderNo" label="工单编号" width="160" />
            <el-table-column prop="serviceName" label="服务名称" width="140" />
            <el-table-column prop="elderName" label="客户" width="100" />
            <el-table-column prop="appointmentTime" label="预约时间" width="160" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="['','warning','primary','success','info'][row.status]" size="small">
                  {{ ['','待接单','已接单','服务中','已完成','已取消'][row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="服务费" width="100">
              <template #default="{ row }"><span style="color:#52C41A">¥{{ row.price }}</span></template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="recordPageNum" v-model:page-size="recordPageSize" :total="recordTotal"
            layout="total,prev,pager,next" style="margin-top:12px;justify-content:flex-end" @change="loadServiceRecords" />
        </el-tab-pane>

        <!-- Tab4: 收入统计 -->
        <el-tab-pane label="收入统计" name="income">
          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="8">
              <el-card shadow="hover" body-style="text-align:center">
                <div style="font-size:24px;color:#52C41A;font-weight:bold">¥{{ incomeStats.totalIncome }}</div>
                <div style="color:#666">总收入</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" body-style="text-align:center">
                <div style="font-size:24px;color:#00C4A1;font-weight:bold">¥{{ incomeStats.monthIncome }}</div>
                <div style="color:#666">本月收入</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" body-style="text-align:center">
                <div style="font-size:24px;color:#FAAD14;font-weight:bold">¥{{ incomeStats.pendingIncome }}</div>
                <div style="color:#666">待结算</div>
              </el-card>
            </el-col>
          </el-row>
          <el-table :data="commissionList" stripe size="small">
            <el-table-column prop="orderNo" label="工单编号" width="160" />
            <el-table-column prop="orderAmount" label="订单金额" width="120">
              <template #default="{ row }"><span style="color:#52C41A">¥{{ row.orderAmount }}</span></template>
            </el-table-column>
            <el-table-column prop="commissionRate" label="佣金比例" width="100">
              <template #default="{ row }">{{ (row.commissionRate * 100).toFixed(1) }}%</template>
            </el-table-column>
            <el-table-column prop="commissionAmount" label="佣金" width="100">
              <template #default="{ row }"><span style="color:#00C4A1">¥{{ row.commissionAmount }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                  {{ row.status === 1 ? '已结算' : '待结算' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
          </el-table>
          <el-pagination v-model:current-page="commissionPageNum" v-model:page-size="commissionPageSize" :total="commissionTotal"
            layout="total,prev,pager,next" style="margin-top:12px;justify-content:flex-end" @change="loadCommissionRecords" />
        </el-tab-pane>

        <!-- Tab5: 评价记录 -->
        <el-tab-pane label="评价记录" name="reviews">
          <el-table :data="reviewList" stripe size="small">
            <el-table-column prop="elderName" label="客户" width="100" />
            <el-table-column label="评分" width="140">
              <template #default="{ row }">
                <el-rate v-model="row.score" disabled size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
            <el-table-column prop="createTime" label="评价时间" width="160" />
          </el-table>
          <el-pagination v-model:current-page="reviewPageNum" v-model:page-size="reviewPageSize" :total="reviewTotal"
            layout="total,prev,pager,next" style="margin-top:12px;justify-content:flex-end" @change="loadReviews" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getServiceWorker, getServiceWorkerTags, getCommissionRecordPage } from '@/api'

const route = useRoute()
const loading = ref(false)
const activeTab = ref('basic')

const recordPageNum = ref(1)
const recordPageSize = ref(5)
const recordTotal = ref(0)
const serviceRecords = ref([])

const commissionPageNum = ref(1)
const commissionPageSize = ref(5)
const commissionTotal = ref(0)
const commissionList = ref([])

const reviewPageNum = ref(1)
const reviewPageSize = ref(5)
const reviewTotal = ref(0)
const reviewList = ref([])

const workerTags = ref([])

const info = reactive({
  name: '', phone: '', serviceType: '', region: '', status: 1, auditStatus: 0,
  gender: 1, idCard: '', intro: '', bankCard: '', bankName: '', certificate: '',
  allowTip: 1, createTime: '', avatar: ''
})

const serviceStats = reactive({ totalOrders: 0, completedOrders: 0, monthIncome: 0, goodRate: 0 })
const incomeStats = reactive({ totalIncome: 0, monthIncome: 0, pendingIncome: 0 })

const maskIdCard = (idCard) => {
  if (!idCard || idCard.length < 18) return idCard
  return idCard.slice(0, 4) + '**********' + idCard.slice(-4)
}

const maskBankCard = (bankCard) => {
  if (!bankCard || bankCard.length < 16) return bankCard
  return bankCard.slice(0, 4) + ' **** **** ' + bankCard.slice(-4)
}

const loadWorkerDetail = async () => {
  loading.value = true
  try {
    const res = await getServiceWorker(route.params.id)
    Object.assign(info, res.data)
  } catch (e) {
    ElMessage.error('加载服务人员信息失败')
  } finally {
    loading.value = false
  }
}

const loadWorkerTags = async () => {
  try {
    const res = await getServiceWorkerTags(route.params.id)
    workerTags.value = res.data || []
  } catch (e) {
    workerTags.value = []
  }
}

const loadServiceRecords = async () => {
  try {
    const res = await getServiceOrderPage({
      pageNum: recordPageNum.value,
      pageSize: recordPageSize.value,
      nurseId: route.params.id
    })
    serviceRecords.value = res.data.records || []
    recordTotal.value = res.data.total || 0
  } catch (e) {
    serviceRecords.value = []
  }
}

const loadCommissionRecords = async () => {
  try {
    const res = await getCommissionRecordPage({
      pageNum: commissionPageNum.value,
      pageSize: commissionPageSize.value,
      workerId: route.params.id
    })
    commissionList.value = res.data.records || []
    commissionTotal.value = res.data.total || 0
  } catch (e) {
    commissionList.value = []
  }
}

const loadReviews = async () => {
  try {
    const res = await getOrderEvaluationPage({
      pageNum: reviewPageNum.value,
      pageSize: reviewPageSize.value,
      workerId: route.params.id
    })
    reviewList.value = res.data.records || []
    reviewTotal.value = res.data.total || 0
  } catch (e) {
    reviewList.value = []
  }
}

const loadStats = async () => {
  serviceStats.totalOrders = 0
  serviceStats.completedOrders = 0
  serviceStats.monthIncome = 0
  serviceStats.goodRate = 0
  incomeStats.totalIncome = 0
  incomeStats.monthIncome = 0
  incomeStats.pendingIncome = 0

  const now = new Date()
  const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

  const [orderRes, commissionRes] = await Promise.all([
    getServiceOrderPage({ pageNum: 1, pageSize: 100, nurseId: route.params.id }),
    getCommissionRecordPage({ pageNum: 1, pageSize: 100, workerId: route.params.id })
  ])

  const orders = orderRes.data.records || []
  serviceStats.totalOrders = orders.length
  serviceStats.completedOrders = orders.filter(o => o.status === 4).length

  const commissions = commissionRes.data.records || []
  commissions.forEach(c => {
    if (c.status === 1) {
      incomeStats.totalIncome = (Number(incomeStats.totalIncome) + Number(c.commissionAmount || 0)).toFixed(2)
    } else {
      incomeStats.pendingIncome = (Number(incomeStats.pendingIncome) + Number(c.commissionAmount || 0)).toFixed(2)
    }
    if (c.createTime?.startsWith(currentMonth)) {
      incomeStats.monthIncome = (Number(incomeStats.monthIncome) + Number(c.commissionAmount || 0)).toFixed(2)
    }
  })

  serviceStats.monthIncome = incomeStats.monthIncome
}

onMounted(() => {
  loadWorkerDetail()
  loadWorkerTags()
  loadServiceRecords()
  loadCommissionRecords()
  loadReviews()
  loadStats()
})
</script>

<style scoped>
.worker-detail { display: flex; gap: 16px; }
.left-card { width: 280px; flex-shrink: 0; }
.right-card { flex: 1; min-width: 0; }
.info-panel { display: flex; flex-direction: column; align-items: center; text-align: center; }
.info-name { font-size: 20px; font-weight: bold; margin-top: 12px; color: #303133; }
.info-tags { margin-top: 8px; display: flex; gap: 4px; }
.info-item { width: 100%; text-align: left; padding: 8px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #606266; }
.info-item .label { color: #A0AEC0; margin-right: 4px; }
</style>
