<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>用户标签管理</span><el-button type="primary" @click="openDialog()">新增标签</el-button></div>
    </template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="tagName" label="标签名称" />
      <el-table-column prop="tagType" label="标签类型" />
      <el-table-column prop="color" label="颜色" width="120">
        <template #default="{ row }">
          <span :style="{ background: row.color, color: '#fff', padding: '2px 12px', borderRadius: 4 }">{{ row.color }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新增标签'" width="450px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="标签名称" prop="tagName"><el-input v-model="form.tagName" /></el-form-item>
      <el-form-item label="标签类型" prop="tagType">
        <el-select v-model="form.tagType" style="width:100%">
          <el-option value="客户类型" label="客户类型" />
          <el-option value="健康标签" label="健康标签" />
        </el-select>
      </el-form-item>
      <el-form-item label="颜色">
        <el-color-picker v-model="form.color" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserTagPage, addUserTag, updateUserTag, deleteUserTag } from '@/api'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, tagName: '', tagType: '客户类型', color: '#00C4A1' })
const rules = { tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }] }

const load = async () => { loading.value = true; try { const res = await getUserTagPage({ pageNum: pageNum.value, pageSize: pageSize.value }); tableData.value = res.data.records; total.value = res.data.total } catch (e) {} finally { loading.value = false } }
const openDialog = (row) => { row ? Object.assign(form, row) : (formRef.value?.resetFields(), Object.assign(form, { id: null, tagName: '', tagType: '客户类型', color: '#00C4A1' })); isEdit.value = !!row; dialogVisible.value = true }
const handleSave = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; try { isEdit.value ? await updateUserTag(form) : await addUserTag(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {} }
const handleDelete = async (id) => { await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }); try { await deleteUserTag(id); ElMessage.success('删除成功'); load() } catch (e) {} }
onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
</style>
