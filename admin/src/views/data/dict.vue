<template>
  <div class="data-dict">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">数据字典</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增字典项
          </el-button>
        </div>
      </template>

      <!-- 筛选区 -->
      <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
        <el-form-item label="字典类型">
          <el-input v-model="searchForm.dictType" placeholder="请输入字典类型" clearable style="width:160px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="字典标签">
          <el-input v-model="searchForm.dictLabel" placeholder="请输入字典标签" clearable style="width:160px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border stripe v-loading="loading">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dictType" label="字典类型" width="140" />
        <el-table-column prop="dictCode" label="字典编码" width="140" />
        <el-table-column prop="dictLabel" label="字典标签" min-width="160" />
        <el-table-column prop="dictValue" label="字典值" min-width="160" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getDictPage } from '@/api'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref([])
const searchForm = reactive({ dictType: '', dictLabel: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await getDictPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      dictType: searchForm.dictType || null,
      dictLabel: searchForm.dictLabel || null
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示 */ }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.dictType = ''
  searchForm.dictLabel = ''
  pageNum.value = 1
  load()
}

// TODO: 后端暂无字典项的增删改接口（仅有 /data/dict/page 查询）
const handleAdd = () => { ElMessage.info('后端暂无字典项维护接口') }
const handleEdit = (row) => { ElMessage.info('后端暂无字典项维护接口') }
const handleDelete = (row) => { ElMessage.info('后端暂无字典项维护接口') }

onMounted(load)
</script>

<style scoped>
.data-dict { padding: 16px; }
.card-title { font-weight: 600; font-size: 15px; }
</style>
