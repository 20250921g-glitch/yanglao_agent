<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">动态预览</h2>
          <span class="page-sub">生活圈动态内容预览</span>
        </div>
        <div class="filters">
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="fetch">
            <el-option label="显示" :value="1" />
            <el-option label="隐藏" :value="0" />
          </el-select>
          <el-button @click="fetch">查询</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userName" label="发布人" width="130" />
        <el-table-column prop="likeCount" label="点赞" width="90" align="center" />
        <el-table-column prop="viewCount" label="浏览" width="90" align="center" />
        <el-table-column prop="statusText" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.statusText || (row.status === 1 ? '显示' : '隐藏') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openPreview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        :current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="handlePage" />
    </el-card>

    <el-dialog v-model="dialog" title="动态预览" width="400px">
      <div class="phone">
        <div class="phone-bar"><span>动态详情</span></div>
        <div class="phone-body">
          <h3 class="dy-title">{{ current.title }}</h3>
          <div class="dy-author">{{ current.userName }} · {{ current.createTime }}</div>
          <div class="dy-content">{{ current.content }}</div>
          <div class="dy-images" v-if="images.length">
            <el-image v-for="(img, i) in images" :key="i" :src="img" fit="cover" class="dy-img" :preview-src-list="images" />
          </div>
          <div class="dy-meta">
            <span>👁 {{ current.viewCount || 0 }}</span>
            <span>👍 {{ current.likeCount || 0 }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getDynamicPage } from '@/api/index'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const dialog = ref(false)
const current = ref({})
const images = computed(() => (current.value.images ? String(current.value.images).split(',') : []))

const fetch = async () => {
  loading.value = true
  try {
    const res = await getDynamicPage({ pageNum: pageNum.value, pageSize: pageSize.value, status: statusFilter.value === '' ? undefined : statusFilter.value })
    if (res.code === 200) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally { loading.value = false }
}
const handlePage = (p) => { pageNum.value = p; fetch() }
const openPreview = (row) => { current.value = row; dialog.value = true }

onMounted(fetch)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; font-weight: 600; color: #303133; }
.page-sub { font-size: 13px; color: #A0AEC0; margin-left: 8px; }
.filters { display: flex; gap: 10px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.phone { border: 1px solid #ebeef5; border-radius: 16px; overflow: hidden; background: #f7f8fa; }
.phone-bar { background: #00C4A1; color: #fff; text-align: center; padding: 10px; font-size: 14px; }
.phone-body { padding: 16px; min-height: 240px; }
.dy-title { margin: 0 0 8px; font-size: 16px; color: #303133; }
.dy-author { font-size: 12px; color: #A0AEC0; margin-bottom: 12px; }
.dy-content { font-size: 14px; line-height: 1.7; color: #606266; white-space: pre-wrap; }
.dy-images { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; margin: 12px 0; }
.dy-img { width: 100%; height: 90px; border-radius: 6px; }
.dy-meta { display: flex; gap: 18px; color: #A0AEC0; font-size: 13px; }
</style>
