<template>
  <el-card>
    <template #header><span>提现管理</span></template>
    
    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="提现单号">
        <el-input v-model="searchForm.withdrawNo" placeholder="请输入提现单号" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="服务人员">
        <el-input v-model="searchForm.workerName" placeholder="请输入姓名" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="提现状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已完成" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="withdrawNo" label="提现单号" width="140" />
      <el-table-column prop="workerName" label="服务人员" width="100" />
      <el-table-column prop="amount" label="提现金额" width="100">
        <template #default="{ row }">
          <span style="color:#52C41A;font-weight:600">¥{{ row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="withdrawType" label="提现方式" width="100" />
      <el-table-column prop="bankCard" label="银行卡号" width="160" />
      <el-table-column prop="bankName" label="开户行" width="120" />
      <el-table-column label="提现状态" width="100">
        <template #default="{ row }">
          <el-tag :type="['warning','success','danger','info'][row.status]" size="small">
            {{ ['待审核','已通过','已拒绝','已完成'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="160" />
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="router.push(`/trade/withdrawal-detail/${row.id}`)">详情</el-button>
          <el-button v-if="row.status===0" type="success" size="small" @click="handleWithdrawal(row,1)">通过</el-button>
          <el-button v-if="row.status===0" type="danger" size="small" @click="handleWithdrawal(row,2)">拒绝</el-button>
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
import { getWithdrawalPage, handleWithdrawal as handleWithdrawalApi } from '@/api'

const router = useRouter()
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const searchForm = reactive({ withdrawNo: '', workerName: '', status: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await getWithdrawalPage({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.withdrawNo = ''
  searchForm.workerName = ''
  searchForm.status = ''
  load()
}

const handleWithdrawal = async (row, status) => {
  if (status === 2) {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝提现', { confirmButtonText: '确定', cancelButtonText: '取消' })
    if (!value) return
    await handleWithdrawalApi(row.id, { status, rejectReason: value })
    ElMessage.success('已拒绝提现')
  } else {
    await ElMessageBox.confirm('确认通过提现？', '提示', { type: 'warning' })
    await handleWithdrawalApi(row.id, { status, rejectReason: '' })
    ElMessage.success('提现已通过')
  }
  load()
}

onMounted(load)
</script>
