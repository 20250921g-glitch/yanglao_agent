<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>用户列表</span>
        <el-button type="primary" @click="$router.push({ name: 'UserAdd' })">新增用户</el-button>
      </div>
    </template>

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="真实姓名">
        <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable style="width:140px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable style="width:140px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="注册日期">
        <el-date-picker v-model="searchForm.dateRange" type="daterange" unlink-panels
          range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width:240px" />
      </el-form-item>
      <el-form-item label="用户标签">
        <el-select v-model="searchForm.tag" placeholder="请选择标签" clearable filterable style="width:200px">
          <el-option v-for="t in TAG_FILTERS" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 批量操作 -->
    <div class="batch-bar" v-if="selection.length">
      <span class="sel-tip">已选 {{ selection.length }} 项</span>
      <el-button type="primary" plain size="small" @click="batchAddTag">批量添加标签</el-button>
      <el-button type="danger" plain size="small" @click="batchDelete">批量删除</el-button>
    </div>

    <el-table :data="pagedData" stripe v-loading="loading" @selection-change="onSelect">
      <template #empty><el-empty description="暂无数据" /></template>
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="realName" label="真实姓名" width="110" />
      <el-table-column label="昵称" width="120">
        <template #default="{ row }">{{ row.username || '—' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号码" width="130" />
      <el-table-column label="用户标签" min-width="200">
        <template #default="{ row }">
          <el-tag v-for="tag in (row.tags || [])" :key="tag" size="small" type="success" style="margin-right:4px">{{ tag }}</el-tag>
          <span v-if="!row.tags || !row.tags.length" class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="用户类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ roleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用户状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :style="row.status === 1 ? enabledStyle : disabledStyle">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="$router.push({ name: 'UserDetail', params: { id: row.id } })">用户详情</el-button>
          <el-button link type="primary" size="small" @click="openAddTag(row)">添加标签</el-button>
          <el-button link type="warning" size="small" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button link type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="filteredTotal"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />

    <!-- 添加标签对话框 -->
    <el-dialog v-model="tagVisible" :title="batchMode ? '批量添加标签' : '添加标签'" width="460px">
      <el-select v-model="pickedTags" multiple placeholder="请选择标签" style="width:100%">
        <el-option v-for="t in tagOptions" :key="t.id" :label="t.name" :value="t.name" />
      </el-select>
      <template #footer>
        <el-button @click="tagVisible=false">取消</el-button>
        <el-button type="primary" @click="confirmAddTag">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAppUserPage, getAppUserTags, deleteAppUser, updateAppUserStatus, updateAppUserTags,
         getUserTagList } from '@/api'

const router = useRouter()

// 蓝图用户标签筛选枚举
const TAG_FILTERS = ['高血糖', '高血脂', '高血压', '冠心病', '运动疗法', '心理疗法']

// 用户类型中文映射
const ROLE_MAP = { ELDER: '老人', FAMILY: '家属', VOLUNTEER: '志愿者', STAFF: '工作人员' }
const roleText = (role) => ROLE_MAP[role] || '—'

// 状态标签品牌色（#00C4A1 启用 / #909399 禁用）
const enabledStyle = { color: '#00C4A1', backgroundColor: 'rgba(0,196,161,.12)', borderColor: 'transparent' }
const disabledStyle = { color: '#909399', backgroundColor: 'rgba(144,147,153,.12)', borderColor: 'transparent' }

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const rawData = ref([]), selection = ref([]), tagOptions = ref([])
const searchForm = reactive({ realName: '', phone: '', dateRange: null, tag: '' })
const tagVisible = ref(false), batchMode = ref(false), pickedTags = ref([]), currentRow = ref(null)

const formatDate = (d) => {
  const dt = new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())}`
}

const load = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, realName: searchForm.realName || null, phone: searchForm.phone || null }
    const range = searchForm.dateRange
    if (range && range.length === 2) {
      params.startDate = formatDate(range[0])
      params.endDate = formatDate(range[1])
    }
    const res = await getAppUserPage(params)
    rawData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {}
  finally { loading.value = false }
}
const loadTags = async () => {
  try { const res = await getUserTagList(); tagOptions.value = res.data || [] } catch (e) {}
}

const filteredData = computed(() => {
  let list = rawData.value
  if (searchForm.tag) {
    list = list.filter(r => (r.tags || []).includes(searchForm.tag))
  }
  return list
})
const filteredTotal = computed(() => searchForm.tag ? filteredData.value.length : total.value)
const pagedData = computed(() => {
  if (searchForm.tag) {
    const s = (pageNum.value - 1) * pageSize.value
    return filteredData.value.slice(s, s + pageSize.value)
  }
  return filteredData.value
})

const resetSearch = () => { searchForm.realName = ''; searchForm.phone = ''; searchForm.dateRange = null; searchForm.tag = ''; pageNum.value = 1; load() }
const onSelect = (rows) => { selection.value = rows }
const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await updateAppUserStatus(row.id, next); ElMessage.success(next === 1 ? '已启用' : '已禁用'); load()
}
const del = async (row) => {
  await ElMessageBox.confirm('确认删除该用户？', '提示', { type: 'warning' })
  await deleteAppUser(row.id); ElMessage.success('删除成功'); load()
}
const openAddTag = (row) => { currentRow.value = row; batchMode.value = false; pickedTags.value = []; tagVisible.value = true }
const batchAddTag = () => { currentRow.value = null; batchMode.value = true; pickedTags.value = []; tagVisible.value = true }
const confirmAddTag = async () => {
  if (!pickedTags.value.length) return ElMessage.warning('请选择标签')
  const targets = batchMode.value ? selection.value : [currentRow.value]
  try {
    for (const row of targets) {
      let cur = []
      try { const r = await getAppUserTags(row.id); cur = r.data || [] } catch (e) {}
      const merged = Array.from(new Set([...cur, ...pickedTags.value]))
      await updateAppUserTags(row.id, merged)
    }
    ElMessage.success('标签已更新'); tagVisible.value = false; load()
  } catch (e) {}
}
const batchDelete = async () => {
  await ElMessageBox.confirm(`确认删除选中的 ${selection.value.length} 个用户？`, '提示', { type: 'warning' })
  try { for (const row of selection.value) await deleteAppUser(row.id); ElMessage.success('批量删除成功'); load() } catch (e) {}
}

onMounted(() => { load(); loadTags() })
</script>

<style scoped>
.batch-bar { margin-bottom:12px; display:flex; align-items:center; gap:10px; }
.sel-tip { font-size:13px; color:#606266; }
.muted { color:#c0c4cc; }
</style>
