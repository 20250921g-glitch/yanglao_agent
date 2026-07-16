<template>
  <el-card>
    <template #header><span>佣金记录</span></template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="workerName" label="服务人员" width="100" />
      <el-table-column prop="serviceType" label="服务类型" width="100" />
      <el-table-column prop="orderNo" label="工单编号" width="140" />
      <el-table-column prop="orderAmount" label="订单金额" width="100">
        <template #default="{ row }">¥{{ row.orderAmount }}</template>
      </el-table-column>
      <el-table-column prop="commissionRate" label="佣金比例" width="90">
        <template #default="{ row }">{{ (row.commissionRate * 100).toFixed(1) }}%</template>
      </el-table-column>
      <el-table-column prop="commissionAmount" label="佣金金额" width="100">
        <template #default="{ row }">
          <span style="color:#52C41A;font-weight:600">¥{{ row.commissionAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结算状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status===1?'success':'warning'" size="small">
            {{ row.status===1?'已结算':'待结算' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="settleTime" label="结算时间" width="160" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCommissionRecordPage } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await getCommissionRecordPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

onMounted(load)
</script>
