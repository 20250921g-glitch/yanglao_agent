<template>
  <div>
    <el-card v-loading="loading">
      <template #header><span style="font-weight:bold;font-size:16px">会员等级信息</span></template>
      <el-descriptions v-if="found" :column="2" border>
        <el-descriptions-item label="等级ID">{{ info.id }}</el-descriptions-item>
        <el-descriptions-item label="等级名称">{{ info.levelName }}</el-descriptions-item>
        <el-descriptions-item label="等级图标">{{ info.levelIcon || '—' }}</el-descriptions-item>
        <el-descriptions-item label="最低成长值">{{ info.minScore }}</el-descriptions-item>
        <el-descriptions-item label="最高成长值">{{ info.maxScore }}</el-descriptions-item>
        <el-descriptions-item label="折扣率">{{ (info.discount * 10).toFixed(1) }}折</el-descriptions-item>
        <el-descriptions-item label="权益说明" :span="2">{{ info.benefits || '—' }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ info.sort }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="info.status === 1 ? 'success' : 'info'" size="small">
            {{ info.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ info.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="未找到该会员等级" />
      <div v-if="found" style="margin-top:16px;text-align:right">
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑会员等级" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="levelName"><el-input v-model="form.levelName" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="最低成长值" prop="minScore"><el-input-number v-model="form.minScore" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最高成长值" prop="maxScore"><el-input-number v-model="form.maxScore" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="折扣率" prop="discount"><el-input-number v-model="form.discount" :min="0.1" :max="1" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="权益说明"><el-input v-model="form.benefits" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="等级图标"><el-input v-model="form.levelIcon" placeholder="如图标字符或URL" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMemberLevelPage, updateMemberLevel, deleteMemberLevel } from '@/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const found = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const info = reactive({
  id: '', levelName: '', levelIcon: '', minScore: 0, maxScore: 0,
  discount: 1, benefits: '', sort: 0, status: 1, createTime: ''
})
const form = reactive({ id: '', levelName: '', levelIcon: '', minScore: 0, maxScore: 0, discount: 1, benefits: '', sort: 0 })
const rules = { levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }] }

const load = async () => {
  loading.value = true
  try {
    const routeId = Number(route.params.id)
    const res = await getMemberLevelPage({ pageSize: 1000 })
    const row = (res.data.records || []).find(r => r.id === routeId)
    if (row) {
      Object.assign(info, { ...row })
      found.value = true
    } else {
      found.value = false
    }
  } catch (e) { /* 响应拦截器已提示 */ }
  finally { loading.value = false }
}

const handleEdit = () => {
  Object.assign(form, {
    id: info.id, levelName: info.levelName, levelIcon: info.levelIcon,
    minScore: info.minScore, maxScore: info.maxScore,
    discount: Number(info.discount), benefits: info.benefits, sort: info.sort
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await updateMemberLevel({
    id: form.id, levelName: form.levelName, levelIcon: form.levelIcon,
    minScore: form.minScore, maxScore: form.maxScore,
    discount: form.discount, benefits: form.benefits, sort: form.sort
  })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const handleDelete = async () => {
  await ElMessageBox.confirm('确定删除该会员等级吗？', '提示', { type: 'warning' })
  await deleteMemberLevel(info.id)
  ElMessage.success('删除成功')
  router.back()
}

onMounted(load)
</script>
