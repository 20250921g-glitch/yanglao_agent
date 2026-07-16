<template>
  <el-card>
    <template #header><span>打赏记录</span></template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="workerName" label="服务人员" width="120" />
      <el-table-column prop="amount" label="打赏金额" width="120">
        <template #default="{ row }">
          <span style="color:#FAAD14;font-weight:600">¥{{ row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="打赏备注" show-overflow-tooltip />
      <el-table-column prop="createTime" label="打赏时间" width="160" />
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTipRecordPage } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await getTipRecordPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

onMounted(load)
</script>
