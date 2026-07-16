<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>人员标签</span>
        <el-button type="primary" @click="openDialog()">新增标签</el-button>
      </div>
    </template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="tagName" label="标签名称" width="150" />
      <el-table-column prop="serviceType" label="所属服务类型" width="120" />
      <el-table-column label="标签颜色" width="100">
        <template #default="{ row }">
          <span :style="{ display:'inline-block', width:'20px', height:'20px', borderRadius:'4px', backgroundColor:row.color, verticalAlign:'middle' }"></span>
          <span style="margin-left:8px">{{ row.color }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editId ? '编辑标签' : '新增标签'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="标签名称" prop="tagName">
        <el-input v-model="form.tagName" placeholder="请输入标签名称" />
      </el-form-item>
      <el-form-item label="所属服务类型" prop="serviceType">
        <el-select v-model="form.serviceType" placeholder="请选择">
          <el-option label="家政护工" value="家政护工" />
          <el-option label="康复理疗" value="康复理疗" />
          <el-option label="上门体检" value="上门体检" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签颜色" prop="color">
        <el-color-picker v-model="form.color" />
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
import { getServiceWorkerTagPage, getServiceWorkerTag, createServiceWorkerTag, updateServiceWorkerTag, deleteServiceWorkerTag } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), editId = ref(null), formRef = ref(null)

const form = reactive({ tagName: '', serviceType: '', color: '#00C4A1' })
const rules = {
  tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  color: [{ required: true, message: '请选择标签颜色', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getServiceWorkerTagPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

const openDialog = async (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    const res = await getServiceWorkerTag(editId.value)
    Object.assign(form, res.data)
  } else {
    Object.assign(form, { tagName: '', serviceType: '', color: '#00C4A1' })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateServiceWorkerTag(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createServiceWorkerTag(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {}
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该标签？', '提示', { type: 'warning' })
  await deleteServiceWorkerTag(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
