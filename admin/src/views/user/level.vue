<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>会员等级管理</span><el-button type="primary" @click="openDialog()">新增等级</el-button></div>
    </template>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="levelName" label="等级名称" />
      <el-table-column prop="minScore" label="最低成长值" />
      <el-table-column prop="maxScore" label="最高成长值" />
      <el-table-column prop="discount" label="折扣率">
        <template #default="{ row }">{{ (row.discount * 10).toFixed(1) }}折</template>
      </el-table-column>
      <el-table-column prop="benefits" label="权益说明" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="210">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/user/level-detail/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
  <el-dialog v-model="dialogVisible" :title="isEdit?'编辑等级':'新增等级'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="等级名称" prop="levelName"><el-input v-model="form.levelName" /></el-form-item>
      <el-row :gutter="16">
        <el-col :span="12"><el-form-item label="最低成长值" prop="minScore"><el-input-number v-model="form.minScore" :min="0" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="最高成长值" prop="maxScore"><el-input-number v-model="form.maxScore" :min="0" style="width:100%" /></el-form-item></el-col>
      </el-row>
      <el-form-item label="折扣率" prop="discount"><el-input-number v-model="form.discount" :min="0.1" :max="1" :precision="2" style="width:100%" /></el-form-item>
      <el-form-item label="权益说明"><el-input v-model="form.benefits" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width:100%" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMemberLevelPage, addMemberLevel, updateMemberLevel, deleteMemberLevel } from '@/api'

const router = useRouter()
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, levelName: '', minScore: 0, maxScore: 0, discount: 1.0, benefits: '', sort: 0, status: 1 })
const rules = { levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }] }

const load = async () => { loading.value = true; try { const res = await getMemberLevelPage({ pageNum: pageNum.value, pageSize: pageSize.value }); tableData.value = res.data.records; total.value = res.data.total } catch (e) {} finally { loading.value = false } }
const openDialog = (row) => { row ? Object.assign(form, row) : (formRef.value?.resetFields(), Object.assign(form, { id: null, levelName: '', minScore: 0, maxScore: 0, discount: 1.0, benefits: '', sort: 0, status: 1 })); isEdit.value = !!row; dialogVisible.value = true }
const handleSave = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; try { isEdit.value ? await updateMemberLevel(form) : await addMemberLevel(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {} }
const handleDelete = async (id) => { await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }); try { await deleteMemberLevel(id); ElMessage.success('删除成功'); load() } catch (e) {} }
onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
</style>
