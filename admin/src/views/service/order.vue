<template>
  <el-card>
    <template #header><span>服务工单</span></template>
    
    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="工单编号">
        <el-input v-model="searchForm.orderNo" placeholder="请输入工单编号" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="老人姓名">
        <el-input v-model="searchForm.elderName" placeholder="请输入老人姓名" clearable @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="服务类型">
        <el-select v-model="searchForm.serviceType" placeholder="请选择" clearable>
          <el-option label="家政护工" value="家政护工" />
          <el-option label="康复理疗" value="康复理疗" />
          <el-option label="上门体检" value="上门体检" />
        </el-select>
      </el-form-item>
      <el-form-item label="工单状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="待接单" :value="1" />
          <el-option label="已接单" :value="2" />
          <el-option label="服务中" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="orderNo" label="工单编号" width="140" />
      <el-table-column prop="elderName" label="老人姓名" width="100" />
      <el-table-column prop="serviceType" label="服务类型" width="100" />
      <el-table-column prop="serviceName" label="服务名称" width="120" />
      <el-table-column prop="nurseName" label="服务人员" width="100" />
      <el-table-column prop="price" label="服务价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="工单状态" width="100">
        <template #default="{ row }">
          <el-tag :type="['','warning','','primary','success','info'][row.status]" size="small">
            {{ ['','待接单','已接单','服务中','已完成','已取消'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="appointmentTime" label="预约时间" width="160" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/service/order-detail/${row.id}`)">详情</el-button>
          <el-button v-if="row.status===1" type="primary" size="small" @click="openAssignDialog(row)">派单</el-button>
          <el-button v-if="row.status===2" type="success" size="small" @click="changeStatus(row,3)">开始服务</el-button>
          <el-button v-if="row.status===3" type="success" size="small" @click="changeStatus(row,4)">完成</el-button>
          <el-button v-if="row.status<=2" type="danger" size="small" @click="changeStatus(row,5)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <!-- 派单对话框 -->
  <el-dialog v-model="assignDialogVisible" title="派单" width="500px">
    <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
      <el-form-item label="选择服务人员" prop="nurseId">
        <el-select v-model="assignForm.nurseId" placeholder="请选择服务人员" style="width:100%">
          <el-option v-for="item in workerList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="assignDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitAssign">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceOrderPage, assignWorker as assignWorkerApi, startService, completeService, getServiceWorkerList } from '@/api'

const router = useRouter()

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const assignDialogVisible = ref(false), assignFormRef = ref(null), workerList = ref([])
const currentOrderId = ref(null)

const searchForm = reactive({ orderNo: '', elderName: '', serviceType: '', status: '' })
const assignForm = reactive({ nurseId: null })
const assignRules = { nurseId: [{ required: true, message: '请选择服务人员', trigger: 'change' }] }

const load = async () => {
  loading.value = true
  try {
    const res = await getServiceOrderPage({ pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.elderName = ''
  searchForm.serviceType = ''
  searchForm.status = ''
  load()
}

const loadWorkerList = async () => {
  try {
    const res = await getServiceWorkerList()
    workerList.value = res.data
  } catch (e) {}
}

const openAssignDialog = async (row) => {
  currentOrderId.value = row.id
  assignForm.nurseId = null
  await loadWorkerList()
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  const valid = await assignFormRef.value.validate().catch(() => false)
  if (!valid) return
  const worker = workerList.value.find(w => w.id === assignForm.nurseId)
  await assignWorkerApi(currentOrderId.value, assignForm.nurseId, worker.name)
  ElMessage.success('派单成功')
  assignDialogVisible.value = false
  load()
}

const changeStatus = async (row, status) => {
  const statusText = ['', '待接单', '已接单', '服务中', '已完成', '已取消'][status]
  await ElMessageBox.confirm(`确认将工单状态改为"${statusText}"？`, '提示', { type: 'warning' })
  if (status === 3) {
    await startService(row.id)
  } else if (status === 4) {
    await completeService(row.id)
  }
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>
