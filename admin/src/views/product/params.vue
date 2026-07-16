<template>
  <div class="page-container">
    <!-- 顶部工具栏 -->
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>商品参数管理</span>
          <el-button type="primary" @click="openDialog()">新增参数</el-button>
        </div>
      </template>

      <!-- 服务类型筛选 -->
      <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
        <el-form-item label="服务类型">
          <el-select v-model="searchForm.serviceType" placeholder="全部" clearable style="width:180px">
            <el-option label="全部" value="" />
            <el-option label="家政护理" value="家政护理" />
            <el-option label="康复理疗" value="康复理疗" />
            <el-option label="上门体检" value="上门体检" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数名">
          <el-input v-model="searchForm.name" placeholder="请输入参数名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="参数ID" width="100" />
        <el-table-column prop="name" label="参数名" width="160" />
        <el-table-column prop="value" label="参数值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="serviceType" label="所属服务类型" width="120">
          <template #default="{ row }">
            <el-tag :type="serviceTypeTag(row.serviceType)" size="small">{{ row.serviceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="所属分类" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top:16px;text-align:right">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑参数' : '新增参数'" width="500px">
      <el-form ref="formRef" :model="dialogForm" :rules="dialogRules" label-width="110px">
        <el-form-item label="参数名" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入参数名，如：服务时长" />
        </el-form-item>
        <el-form-item label="参数值" prop="value">
          <el-input v-model="dialogForm.value" placeholder="请输入参数值，如：2小时" />
        </el-form-item>
        <el-form-item label="所属服务类型" prop="serviceType">
          <el-select v-model="dialogForm.serviceType" placeholder="请选择服务类型" style="width:100%">
            <el-option label="家政护理" value="家政护理" />
            <el-option label="康复理疗" value="康复理疗" />
            <el-option label="上门体检" value="上门体检" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属分类">
          <el-input v-model="dialogForm.category" placeholder="请输入所属分类" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dialogForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDialog">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { getProductParamPage, addProductParam, updateProductParam, deleteProductParam } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref(null)
const tableRef = ref(null)

const tableData = ref([])
const total = ref(0)

const searchForm = reactive({ serviceType: '', name: '', pageNum: 1, pageSize: 10 })

const serviceTypeTag = (type) => {
  const map = { '家政护理': 'success', '康复理疗': 'warning', '上门体检': 'primary' }
  return map[type] || 'info'
}

const dialogForm = reactive({
  name: '',
  value: '',
  serviceType: '',
  category: '',
  status: 1
})

const dialogRules = {
  name: [{ required: true, message: '请输入参数名', trigger: 'blur' }],
  value: [{ required: true, message: '请输入参数值', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductParamPage(searchForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    tableData.value = []
  }
  loading.value = false
}

const resetSearch = () => {
  searchForm.serviceType = ''
  searchForm.name = ''
  searchForm.pageNum = 1
  loadData()
}

const openDialog = (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    Object.assign(dialogForm, {
      name: row.name,
      value: row.value,
      serviceType: row.serviceType,
      category: row.category,
      status: row.status
    })
  } else {
    Object.assign(dialogForm, { name: '', value: '', serviceType: '', category: '', status: 1 })
  }
  dialogVisible.value = true
}

const submitDialog = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateProductParam(editId.value, dialogForm)
      ElMessage.success('修改成功')
    } else {
      await addProductParam(dialogForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const del = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该参数？', '提示', { type: 'warning' })
    await deleteProductParam(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleSizeChange = (size) => {
  searchForm.pageSize = size
  searchForm.pageNum = 1
  loadData()
}

const handleCurrentChange = (page) => {
  searchForm.pageNum = page
  loadData()
}

onMounted(loadData)
</script>
