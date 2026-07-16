<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>社区管理</span></div>
    </template>

    <el-table :data="list" stripe v-loading="loading">
      <template #empty><el-empty description="暂无数据" /></template>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-html="row.content && row.content.includes('<') ? '' : (row.content || '—')"></span>
        </template>
      </el-table-column>
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <el-image v-if="row.image" :src="row.image" style="width:50px;height:50px;border-radius:4px" fit="cover" :preview-src-list="[row.image]" />
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="view(row)">查看</el-button>
          <el-button link type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end"
      @current-change="load" @size-change="load" />

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="社区内容详情" width="600px">
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 1 ? 'success' : 'info'" size="small">{{ currentRow.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentRow.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="封面" v-if="currentRow.image">
          <el-image :src="currentRow.image" style="width:120px;height:120px;border-radius:4px" fit="cover" />
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space:pre-wrap">{{ currentRow.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommunityPage, deleteCommunity } from '@/api'

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const list = ref([])
const viewVisible = ref(false)
const currentRow = ref(null)

const load = async () => {
  loading.value = true
  try {
    const res = await getCommunityPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const view = (row) => { currentRow.value = row; viewVisible.value = true }

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该社区内容？', '提示', { type: 'warning' })
  await deleteCommunity(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
