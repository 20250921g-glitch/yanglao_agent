<template>
  <el-card>
    <template #header>
      <span>退款管理</span>
    </template>
    
    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="订单编号">
        <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="退款状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="审核拒绝" :value="2" />
          <el-option label="已退款" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单类型">
        <el-select v-model="searchForm.orderType" placeholder="请选择" clearable>
          <el-option label="服务订单" :value="1" />
          <el-option label="商品订单" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" width="150" />
      <el-table-column label="订单类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.orderType === 1 ? 'primary' : 'success'" size="small">
            {{ row.orderType === 1 ? '服务订单' : '商品订单' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户姓名" width="100" />
      <el-table-column prop="refundAmount" label="退款金额" width="100">
        <template #default="{ row }">
          <span style="color:#FF4D4F;font-weight:600">¥{{ row.refundAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款方式" width="100">
        <template #default="{ row }">
          {{ row.refundType === 1 ? '仅退款' : '退货退款' }}
        </template>
      </el-table-column>
      <el-table-column prop="refundReason" label="退款原因" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]" size="small">
            {{ statusText[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditor" label="审核人" width="100" />
      <el-table-column prop="auditRemark" label="审核备注" width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/trade/after-sale-detail/${row.id}`)">详情</el-button>
          <template v-if="row.status === 0">
            <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
          <template v-else-if="row.status === 1">
            <el-button type="primary" size="small" @click="handleProcess(row)">执行退款</el-button>
          </template>
          <span v-else style="color:#999">-</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRefundPage, approveRefund, rejectRefund, processRefund } from '@/api'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({ orderNo: '', status: '', orderType: '' })

const statusText = ['待审核', '审核通过', '审核拒绝', '已退款', '已取消']
const statusType = ['warning', 'success', 'danger', 'success', 'info']

const load = async () => {
  loading.value = true
  try {
    const res = await getRefundPage({ 
      pageNum: pageNum.value, 
      pageSize: pageSize.value, 
      ...searchForm 
    })
    tableData.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('Load refund list error:', e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.orderType = ''
  load()
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认审核通过该退款申请？', '审核确认', { type: 'success' })
    await approveRefund(row.id, 'admin', '审核通过')
    ElMessage.success('审核通过')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', { 
      confirmButtonText: '确定', 
      cancelButtonText: '取消' 
    })
    if (!value) return
    await rejectRefund(row.id, 'admin', value)
    ElMessage.success('已拒绝')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleProcess = async (row) => {
  try {
    await ElMessageBox.confirm(`确认退款 ¥${row.refundAmount} 到用户账户？`, '执行退款', { type: 'warning' })
    await processRefund(row.id)
    ElMessage.success('退款成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(load)
</script>
