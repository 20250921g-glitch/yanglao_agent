<template>
  <el-card>
    <template #header><span>审核管理</span></template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="workerName" label="服务人员" width="120" />
      <el-table-column prop="auditType" label="审核类型" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="['warning','success','danger'][row.status]" size="small">
            {{ ['待审核','已通过','已拒绝'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="拒绝原因" show-overflow-tooltip />
      <el-table-column prop="auditRemark" label="审核备注" show-overflow-tooltip />
      <el-table-column prop="auditorName" label="审核人" width="100" />
      <el-table-column prop="auditTime" label="审核时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="router.push('/service/audit-detail/' + row.id)">详情</el-button>
          <el-button v-if="row.status===0" type="primary" size="small" @click="audit(row,1)">通过</el-button>
          <el-button v-if="row.status===0" type="danger" size="small" @click="audit(row,2)">拒绝</el-button>
          <span v-else style="color:#999"></span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditRecordPage, auditRecord } from '@/api'

const router = useRouter()

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await getAuditRecordPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const audit = async (row, status) => {
  if (status === 2) {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', { confirmButtonText: '确定', cancelButtonText: '取消' })
    if (!value) return
    try {
      await auditRecord(row.id, { status, rejectReason: value })
      ElMessage.success('已拒绝')
      load()
    } catch (e) {}
  } else {
    await ElMessageBox.confirm('确认审核通过？', '提示', { type: 'warning' })
    await auditRecord(row.id, { status, rejectReason: '' })
    ElMessage.success('审核通过')
    load()
  }
}

onMounted(load)
</script>
