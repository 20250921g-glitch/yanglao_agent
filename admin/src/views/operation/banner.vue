<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>轮播图管理</span>
        <el-button type="primary" @click="openDialog(null)">新增轮播图</el-button>
      </div>
    </template>

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="位置">
        <el-select v-model="searchForm.position" placeholder="请选择" clearable>
          <el-option label="首页" value="首页" />
          <el-option label="商品页" value="商品页" />
          <el-option label="服务页" value="服务页" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="启用" value="启用" />
          <el-option label="禁用" value="禁用" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="id" label="轮播ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="imageUrl" label="图片" width="120">
        <template #default="{ row }">
          <el-image :src="row.imageUrl" fit="cover" style="width:80px;height:40px;border-radius:4px" />
        </template>
      </el-table-column>
      <el-table-column prop="linkUrl" label="跳转链接" min-width="200" show-overflow-tooltip />
      <el-table-column label="位置" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ row.position }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="120">
        <template #default="{ row, $index }">
          <el-button size="small" :disabled="$index === 0" @click="moveUp($index)" text>
            <el-icon><Top /></el-icon>
          </el-button>
          <el-button size="small" :disabled="$index === tableData.length - 1" @click="moveDown($index)" text>
            <el-icon><Bottom /></el-icon>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <!-- 新增/编辑对话框 -->
  <el-dialog v-model="dialogVisible" :title="editId ? '编辑轮播图' : '新增轮播图'" width="600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="图片URL" prop="imageUrl">
        <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
      </el-form-item>
      <el-form-item label="跳转链接">
        <el-input v-model="form.linkUrl" placeholder="请输入跳转链接" />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="位置" prop="position">
            <el-select v-model="form.position" placeholder="请选择">
              <el-option label="首页" value="首页" />
              <el-option label="商品页" value="商品页" />
              <el-option label="服务页" value="服务页" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" :max="999" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="状态">
        <el-switch v-model="form.status" active-value="启用" inactive-value="禁用" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBannerPage, addBanner, updateBanner, deleteBanner } from '@/api'

const bannerStatusMap = { 1: '启用', 0: '禁用' }
const bannerStatusOptions = [{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)

const searchForm = reactive({ position: '', status: '' })
const form = reactive({ title: '', imageUrl: '', linkUrl: '', position: '', sort: 0, status: '启用' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请输入图片URL', trigger: 'blur' }],
  position: [{ required: true, message: '请选择位置', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getBannerPage({ pageNum: 1, pageSize: 100, position: searchForm.position || undefined, status: bannerStatusOptions.find(o => o.label === searchForm.status)?.value })
    tableData.value = (res.data.records || res.data.list || []).map(item => ({
      ...item,
      status: bannerStatusMap[item.status] || (item.status === 1 ? '启用' : '禁用')
    }))
  } catch {}
  loading.value = false
}

const resetSearch = () => { Object.assign(searchForm, { position: '', status: '' }); load() }

const moveUp = (idx) => {
  const arr = [...tableData.value]
  ;[arr[idx - 1], arr[idx]] = [arr[idx], arr[idx - 1]]
  tableData.value = arr
  ElMessage.success('排序已调整')
}

const moveDown = (idx) => {
  const arr = [...tableData.value]
  ;[arr[idx], arr[idx + 1]] = [arr[idx + 1], arr[idx]]
  tableData.value = arr
  ElMessage.success('排序已调整')
}

const openDialog = (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    Object.assign(form, {
      title: row.title,
      imageUrl: row.imageUrl,
      linkUrl: row.linkUrl,
      position: row.position,
      sort: row.sort,
      status: bannerStatusOptions.find(o => o.label === row.status)?.value ?? 1
    })
  } else {
    Object.assign(form, { title: '', imageUrl: '', linkUrl: '', position: '', sort: 0, status: 1 })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = { ...form, status: form.status }
    if (editId.value) {
      payload.id = editId.value
      await updateBanner(payload)
      ElMessage.success('修改成功')
    } else {
      await addBanner(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该轮播图？', '提示', { type: 'warning' })
  try {
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>
