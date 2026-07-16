<template>
  <div class="dynamic-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>动态管理</span>
          <!-- 发布功能蓝图未列出；保留入口但后端发布接口（含文件上传）待接入 -->
          <el-button type="primary" v-has="'dynamic:add'" @click="openPublish">发布动态</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="发布日期">
          <el-date-picker v-model="search.dateRange" type="daterange" unlink-panels
            range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:240px" @change="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="batch-bar">
        <el-button type="danger" :disabled="!selectedIds.length" @click="batchDelete">
          批量删除 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table ref="tableRef" :data="list" v-loading="loading" @selection-change="handleSelectionChange" style="width:100%">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column type="selection" width="48" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="topic" label="话题" min-width="100">
          <template #default="{ row }">{{ row.topic || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="发布人" min-width="160">
          <template #default="{ row }">
            <div>昵称：{{ row.userName || '—' }}</div>
            <div>手机号：{{ row.userPhone || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞" width="80" align="center">
          <template #default="{ row }">{{ row.likeCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="collectCount" label="收藏" width="80" align="center">
          <template #default="{ row }">{{ row.collectCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="shareCount" label="分享" width="80" align="center">
          <template #default="{ row }">{{ row.shareCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="commentCount" label="评论" width="80" align="center">
          <template #default="{ row }">{{ row.commentCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="warning" size="small" @click="toggleVisible(row)">隐藏</el-button>
            <el-button v-else type="success" size="small" @click="toggleVisible(row)">显示</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:20px;justify-content:flex-end"
        @current-change="loadData" @size-change="loadData" />
    </el-card>

    <el-dialog v-model="publishVisible" title="发布动态" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入动态内容" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload action="#" :file-list="form.fileList" :auto-upload="false" list-type="picture-card">
            <el-icon :size="32" name="Plus" />
          </el-upload>
        </el-form-item>
        <el-form-item label="可见范围">
          <el-radio-group v-model="form.visibility">
            <el-radio value="public">公开</el-radio>
            <el-radio value="private">仅自己可见</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <!-- TODO 后端发布接口需配合文件上传，目前仅占位 -->
        <el-button type="primary" @click="handleSave" :loading="btnLoading">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDynamicPage, approveDynamic, rejectDynamic, deleteDynamic } from '@/api'

const loading = ref(false)
const btnLoading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableRef = ref(null)
const publishVisible = ref(false)
const formRef = ref(null)
const selectedRows = ref([])
const selectedIds = ref([])

const search = reactive({ dateRange: null })

const form = reactive({ content: '', fileList: [], visibility: 'public' })
const rules = { content: [{ required: true, message: '请输入动态内容', trigger: 'blur' }] }

// 真实状态枚举：0=隐藏，1=显示
const statusMap = {
  0: { text: '隐藏', tag: 'info' },
  1: { text: '显示', tag: 'success' }
}
const statusText = (status) => statusMap[status]?.text || '—'
const statusTagType = (status) => statusMap[status]?.tag || 'info'

const fmtDate = (d) => {
  if (!d) return null
  const dt = new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())}`
}

const loadData = async () => {
  loading.value = true
  try {
    const range = search.dateRange
    const res = await getDynamicPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      startCreateTime: range && range.length === 2 ? fmtDate(range[0]) : undefined,
      endCreateTime: range && range.length === 2 ? fmtDate(range[1]) : undefined
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const reset = () => {
  search.dateRange = null
  pageNum.value = 1
  loadData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
  selectedIds.value = rows.map(r => r.id)
}

// TODO 后端未提供批量删除接口（仅有单条删除），按钮保留待接口接入
const batchDelete = () => {
  ElMessage.warning('批量删除功能待后端提供接口')
}

const toggleVisible = async (row) => {
  try {
    if (row.status === 1) {
      await ElMessageBox.confirm('确认隐藏该动态？', '提示', { type: 'warning' })
      await rejectDynamic(row.id, '隐藏')
      ElMessage.success('已隐藏')
    } else {
      await ElMessageBox.confirm('确认显示该动态？', '提示', { type: 'warning' })
      await approveDynamic(row.id, '显示')
      ElMessage.success('已显示')
    }
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该动态？', '提示', { type: 'warning' })
    await deleteDynamic(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const openPublish = () => {
  formRef.value?.resetFields()
  Object.assign(form, { content: '', fileList: [], visibility: 'public' })
  publishVisible.value = true
}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  btnLoading.value = true
  try {
    ElMessage.info('发布功能待接入后端文件上传')
    publishVisible.value = false
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { btnLoading.value = false }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.dynamic-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.batch-bar { margin-bottom: 12px; }
</style>
