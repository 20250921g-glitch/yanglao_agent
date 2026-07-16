<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>报名信息</span>
      </div>
    </template>

    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="活动名称">
        <el-input v-model="searchForm.activityId" placeholder="请选择活动" clearable>
          <template #append>
            <el-select v-model="searchForm.activityId" placeholder="选择活动" clearable @change="load">
              <el-option v-for="item in activityList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="activityName" label="活动名称" min-width="150" />
      <el-table-column prop="userName" label="用户姓名" width="100" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]" size="small">{{ statusTextMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="报名时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="handleApprove(row)" v-if="row.status === 0">通过</el-button>
          <el-button type="danger" size="small" @click="handleReject(row)" v-if="row.status === 0">拒绝</el-button>
          <el-button type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityRegistrationPage, updateActivityRegistrationStatus, deleteActivityRegistration, getActivityList } from '@/api'

const route = useRoute()

const statusTextMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
const statusMap = { 0: 'warning', 1: 'success', 2: 'danger' }

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activityList = ref([])

const searchForm = reactive({ activityId: route.query.activityId || '', status: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await getActivityRegistrationPage({ pageNum: pageNum.value, pageSize: pageSize.value, activityId: searchForm.activityId || undefined, status: searchForm.status || undefined })
    tableData.value = (res.data.records || res.data.list || []).map(item => ({
      ...item,
      createTime: item.createTime ? item.createTime.replace('T', ' ').substring(0, 19) : ''
    }))
    total.value = res.data.total
  } catch {}
  loading.value = false
}

const resetSearch = () => { Object.assign(searchForm, { activityId: '', status: '' }); pageNum.value = 1; load() }

const loadActivityList = async () => {
  try {
    const res = await getActivityList()
    activityList.value = res.data || []
  } catch {}
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该报名？', '提示', { type: 'success' })
    await updateActivityRegistrationStatus(row.id, 1)
    ElMessage.success('已通过')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝该报名？', '提示', { type: 'warning' })
    await updateActivityRegistrationStatus(row.id, 2)
    ElMessage.success('已拒绝')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该报名记录？', '提示', { type: 'warning' })
  try {
    await deleteActivityRegistration(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(() => { loadActivityList(); load() })
</script>
