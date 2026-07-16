<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>操作日志</span>
      </div>
    </template>

    <el-form :inline="true" :model="filter" style="margin-bottom:16px">
      <el-form-item label="操作人">
        <el-input v-model="filter.userName" placeholder="请输入操作人" clearable style="width:160px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="模块">
        <el-select v-model="filter.module" placeholder="全部" clearable filterable style="width:160px">
          <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker v-model="filter.dateRange" type="datetimerange" unlink-panels
          range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width:360px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" stripe v-loading="loading" border>
      <template #empty><el-empty description="暂无数据" /></template>
      <el-table-column prop="id" label="日志ID" width="90" />
      <el-table-column prop="userName" label="操作人" width="140" />
      <el-table-column prop="module" label="模块" width="140" show-overflow-tooltip />
      <el-table-column prop="operation" label="操作" min-width="140" show-overflow-tooltip />
      <el-table-column prop="method" label="请求方法" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="methodTag(row.method)">{{ row.method || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUri" label="请求路径" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="180" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end"
      @current-change="load" @size-change="load" />
  </el-card>

  <el-dialog v-model="detailVisible" title="日志详情" width="600px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
      <el-descriptions-item label="操作人">{{ currentLog.userName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="模块">{{ currentLog.module || '—' }}</el-descriptions-item>
      <el-descriptions-item label="操作">{{ currentLog.operation || '—' }}</el-descriptions-item>
      <el-descriptions-item label="请求方法">{{ currentLog.method || '—' }}</el-descriptions-item>
      <el-descriptions-item label="请求路径">{{ currentLog.requestUri || '—' }}</el-descriptions-item>
      <el-descriptions-item label="请求参数">{{ currentLog.params || '—' }}</el-descriptions-item>
      <el-descriptions-item label="IP地址">{{ currentLog.ip || '—' }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag size="small" :type="currentLog.status === 1 ? 'success' : 'danger'">{{ currentLog.status === 1 ? '成功' : '失败' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="失败原因">{{ currentLog.errorMsg || '—' }}</el-descriptions-item>
      <el-descriptions-item label="操作时间">{{ currentLog.createTime || '—' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getOperationLogPage } from '@/api'

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const list = ref([])
const moduleOptions = ref([])
const filter = reactive({ userName: '', module: '', dateRange: null })
const detailVisible = ref(false), currentLog = ref({})

const methodTag = (m) => {
  if (m === 'GET') return 'info'
  if (m === 'POST') return 'success'
  if (m === 'PUT') return 'warning'
  if (m === 'DELETE') return 'danger'
  return ''
}

const fmtDateTime = (d) => {
  const dt = new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())} ${p(dt.getHours())}:${p(dt.getMinutes())}:${p(dt.getSeconds())}`
}

const load = async () => {
  loading.value = true
  try {
    const range = filter.dateRange
    const res = await getOperationLogPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userName: filter.userName || null,
      module: filter.module || null,
      startTime: range && range.length === 2 ? fmtDateTime(range[0]) : null,
      endTime: range && range.length === 2 ? fmtDateTime(range[1]) : null
    })
    const records = res.data.records || []
    list.value = records
    total.value = res.data.total || 0
    const mods = []
    records.forEach(r => { if (r.module && !mods.includes(r.module)) mods.push(r.module) })
    moduleOptions.value = mods
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const reset = () => { filter.userName = ''; filter.module = ''; filter.dateRange = null; pageNum.value = 1; load() }
const viewDetail = (row) => { currentLog.value = row; detailVisible.value = true }

onMounted(load)
</script>
