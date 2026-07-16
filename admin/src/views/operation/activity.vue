<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>活动管理</span>
        <el-button type="primary" v-has="'activity:add'" @click="openDialog(null)">新增活动</el-button>
      </div>
    </template>

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="更新日期">
        <el-date-picker v-model="searchForm.updateRange" type="daterange" unlink-panels
          range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:240px" @change="load" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:140px">
          <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="searchForm.category" placeholder="请选择分类" clearable style="width:160px">
          <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="tableData" stripe v-loading="loading">
      <template #empty><el-empty description="暂无活动数据" /></template>
      <el-table-column label="活动信息" min-width="240">
        <template #default="{ row }">
          <div class="activity-info">
            <el-image v-if="row.imageUrl" :src="row.imageUrl" fit="cover" class="activity-thumb">
              <template #error><div class="image-fallback">无图</div></template>
            </el-image>
            <span class="activity-name">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          <el-tag :type="typeMap[row.type] || 'info'" size="small">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="活动时间" min-width="200">
        <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column prop="location" label="活动地点" min-width="120">
        <template #default="{ row }">{{ row.location || '—' }}</template>
      </el-table-column>
      <el-table-column label="活动状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagMap[row.status] || 'info'" size="small">{{ statusTextMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后更新时间" width="160" />
      <el-table-column label="最后更新人" width="120">
        <template #default="{ row }">{{ row.updateBy || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" v-has="'activity:edit'" @click="openDialog(row)">编辑</el-button>
          <el-button type="success" size="small" v-has="'activity:registration'" @click="goRegistration(row)">报名信息</el-button>
          <el-button type="danger" size="small" v-has="'activity:delete'" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <!-- 新增/编辑对话框 -->
  <el-dialog v-model="dialogVisible" :title="editId ? '编辑活动' : '新增活动'" width="600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="活动名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入活动名称" />
      </el-form-item>
      <el-form-item label="活动分类" prop="type">
        <el-select v-model="form.type" placeholder="请选择">
          <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item label="活动地点">
        <el-input v-model="form.location" placeholder="请输入活动地点" />
      </el-form-item>
      <el-form-item label="活动图片">
        <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="活动描述">
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入活动描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityPage, addActivity, updateActivity, deleteActivity } from '@/api'

const router = useRouter()
const categoryOptions = ['节日活动', '健康服务', '技能培训', '兴趣社团', '公益讲座']

const statusTextMap = { 0: '未开始', 1: '进行中', 2: '已结束' }
const statusOptions = [{ label: '未开始', value: 0 }, { label: '进行中', value: 1 }, { label: '已结束', value: 2 }]

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

// 真实分类取值（与后端枚举保持一致，未强转蓝图旧值）
const typeMap = { '节日活动': 'danger', '健康服务': 'success', '技能培训': 'warning', '兴趣社团': '', '公益讲座': 'info' }
// 真实状态枚举：0=未开始，1=进行中，2=已结束
const statusTagMap = { 0: 'info', 1: 'success', 2: 'warning' }

const searchForm = reactive({ category: '', status: '', updateRange: null })
const form = reactive({ name: '', type: '', location: '', imageUrl: '', startTime: '', endTime: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动分类', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const fmtDate = (d) => {
  if (!d) return null
  const dt = new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())}`
}

const load = async () => {
  loading.value = true
  try {
    const range = searchForm.updateRange
    const res = await getActivityPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      category: searchForm.category || undefined,
      status: searchForm.status ?? undefined,
      startUpdateTime: range && range.length === 2 ? fmtDate(range[0]) : undefined,
      endUpdateTime: range && range.length === 2 ? fmtDate(range[1]) : undefined
    })
    tableData.value = (res.data.records || res.data.list || []).map(item => ({
      ...item,
      status: statusTextMap[item.status] || item.status,
      startTime: item.startTime ? item.startTime.replace('T', ' ').substring(0, 19) : '',
      endTime: item.endTime ? item.endTime.replace('T', ' ').substring(0, 19) : '',
      updateTime: item.updateTime ? item.updateTime.replace('T', ' ').substring(0, 19) : '',
      participants: item.participantCount
    }))
    total.value = res.data.total
  } catch {}
  loading.value = false
}

const resetSearch = () => { Object.assign(searchForm, { category: '', status: '', updateRange: null }); pageNum.value = 1; load() }

const goRegistration = (row) => {
  router.push({ path: '/operation/activity-registration', query: { activityId: row.id } })
}

const openDialog = (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    Object.assign(form, {
      name: row.name,
      type: row.type,
      location: row.location || '',
      imageUrl: row.imageUrl,
      startTime: row.startTime ? row.startTime.replace(' ', 'T').substring(0, 16) : '',
      endTime: row.endTime ? row.endTime.replace(' ', 'T').substring(0, 16) : '',
      description: row.description,
      status: statusOptions.find(o => o.label === row.status)?.value ?? row.status
    })
  } else {
    Object.assign(form, { name: '', type: '', location: '', imageUrl: '', startTime: '', endTime: '', description: '', status: 0 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = { ...form, startTime: form.startTime ? form.startTime.replace(' ', 'T') : null, endTime: form.endTime ? form.endTime.replace(' ', 'T') : null }
    if (editId.value) {
      payload.id = editId.value
      await updateActivity(payload)
      ElMessage.success('修改成功')
    } else {
      await addActivity(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该活动？', '提示', { type: 'warning' })
  try {
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.activity-info { display: flex; align-items: center; gap: 10px; }
.activity-thumb { width: 56px; height: 56px; border-radius: 6px; flex-shrink: 0; }
.image-fallback { width: 56px; height: 56px; border-radius: 6px; background: #f4f4f5; color: #c0c4cc; font-size: 12px; display: flex; align-items: center; justify-content: center; }
.activity-name { font-weight: 500; color: #303133; }
</style>
