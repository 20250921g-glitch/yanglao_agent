<template>
  <div class="report-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报告管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">新增报告</el-button>
        </div>
      </template>

      <!-- 筛选区 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户姓名">
          <el-input v-model="search.userName" placeholder="请输入用户姓名" clearable style="width:160px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="search.reportType" placeholder="全部" clearable style="width:150px">
            <el-option label="健康报告" value="健康报告" />
            <el-option label="体检报告" value="体检报告" />
            <el-option label="评估报告" value="评估报告" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="search.dateRange" type="daterange" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期"
            value-format="YYYY-MM-DD" style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="list" stripe v-loading="loading">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="id" label="报告ID" width="90" />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column prop="reportType" label="报告类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="reportTypeColor[row.reportType] || 'info'">{{ row.reportType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportDate" label="报告日期" width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusMap[row.status]?.type || 'info'">{{ statusMap[row.status]?.label || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next"
        style="margin-top:16px;justify-content:flex-end" @current-change="load" @size-change="load"
      />
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog v-model="dialogVisible" title="新增报告" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="用户" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户姓名" />
        </el-form-item>
        <el-form-item label="报告类型" prop="reportType">
          <el-select v-model="form.reportType" placeholder="请选择" style="width:100%">
            <el-option label="健康报告" value="健康报告" />
            <el-option label="体检报告" value="体检报告" />
            <el-option label="评估报告" value="评估报告" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告日期" prop="reportDate">
          <el-date-picker v-model="form.reportDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择" style="width:100%">
            <el-option label="已上传" :value="1" />
            <el-option label="待审核" :value="2" />
            <el-option label="已归档" :value="3" />
            <el-option label="已作废" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewVisible" title="报告详情" width="600px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="报告ID">{{ viewRow?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ viewRow?.userName }}</el-descriptions-item>
        <el-descriptions-item label="报告类型">{{ viewRow?.reportType }}</el-descriptions-item>
        <el-descriptions-item label="报告日期">{{ viewRow?.reportDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusMap[viewRow?.status]?.type || 'info'">{{ statusMap[viewRow?.status]?.label || viewRow?.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserReportPage } from '@/api'

const loading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewRow = ref(null)
const formRef = ref(null)

// 后端 /user/report 返回 status 为整型
const statusMap = {
  1: { label: '已上传', type: 'success' },
  2: { label: '待审核', type: 'warning' },
  3: { label: '已归档', type: 'info' },
  0: { label: '已作废', type: 'danger' }
}
const reportTypeColor = { '健康报告': 'success', '体检报告': 'primary', '评估报告': 'warning' }

const search = reactive({
  userName: '',
  reportType: '',
  dateRange: null
})

const form = reactive({
  userName: '',
  reportType: '',
  reportDate: '',
  status: 1
})

const formRules = {
  userName: [{ required: true, message: '请输入用户姓名', trigger: 'blur' }],
  reportType: [{ required: true, message: '请选择报告类型', trigger: 'change' }],
  reportDate: [{ required: true, message: '请选择报告日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getUserReportPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userName: search.userName || null,
      reportType: search.reportType || null
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示 */ }
  finally { loading.value = false }
}

const resetSearch = () => {
  search.userName = ''
  search.reportType = ''
  search.dateRange = null
  pageNum.value = 1
  load()
}

const openAddDialog = () => {
  Object.assign(form, { userName: '', reportType: '', reportDate: '', status: 1 })
  dialogVisible.value = true
}

const handleView = (row) => {
  viewRow.value = row
  viewVisible.value = true
}

// TODO: 后端暂无 /user/report 删除接口
const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除报告《${row.reportType}》？`, '删除确认', { type: 'warning' })
  ElMessage.info('后端暂不支持删除报告')
}

// TODO: 后端暂无 /user/report 新增接口
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  ElMessage.info('后端暂不支持新增报告')
  dialogVisible.value = false
}

onMounted(load)
</script>

<style scoped>
.report-container {
  padding: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
