<template>
  <div class="data-export">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">数据导出</span>
          <el-button type="primary" @click="load">刷新</el-button>
        </div>
      </template>

      <el-table :data="list" border stripe v-loading="loading">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="id" label="导出ID" width="100" />
        <el-table-column prop="type" label="导出类型" width="140" />
        <el-table-column prop="fileName" label="文件名称" min-width="220" />
        <el-table-column prop="exportTime" label="导出时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '成功' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDownload(row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next"
        style="margin-top:16px;justify-content:flex-end" @current-change="load" @size-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getExportPage } from '@/api'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await getExportPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示 */ }
  finally { loading.value = false }
}

// TODO: 后端暂未提供导出文件下载接口（/data/export 仅返回记录列表）
const handleDownload = (row) => {
  ElMessage.info('后端暂未提供文件下载接口')
}

onMounted(load)
</script>

<style scoped>
.data-export { padding: 16px; }
.card-title { font-weight: 600; font-size: 15px; }
</style>
