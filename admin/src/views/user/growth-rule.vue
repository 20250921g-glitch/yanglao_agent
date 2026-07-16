<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">成长值规则</h2>
          <span class="page-sub">配置用户获取成长值的行为与上限</span>
        </div>
        <div>
          <el-button type="primary" @click="openAdd">新增规则</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="ruleName" label="行为" min-width="160" />
        <el-table-column prop="growthValue" label="获取成长值" width="120" align="center">
          <template #default="{ row }"><span class="point">+{{ row.growthValue }}</span></template>
        </el-table-column>
        <el-table-column prop="limitCount" label="每日上限" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="(v) => changeStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" :current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="handlePage" />
    </el-card>

    <el-dialog v-model="dialog" :title="form.id ? '编辑规则' : '新增规则'" width="460px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="行为名称">
          <el-input v-model="form.ruleName" placeholder="如：每日签到" />
        </el-form-item>
        <el-form-item label="获取成长值">
          <el-input-number v-model="form.growthValue" :min="0" :step="5" />
        </el-form-item>
        <el-form-item label="每日上限">
          <el-input-number v-model="form.limitCount" :min="0" :step="5" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGrowthRulePage, saveGrowthRule, updateGrowthRule, updateGrowthRuleStatus, deleteGrowthRule } from '@/api/index'

const loading = ref(false)
const dialog = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const form = ref({ id: null, ruleName: '', growthValue: 5, limitCount: 20, status: 1 })

const load = async () => {
  loading.value = true
  try {
    const res = await getGrowthRulePage({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) { list.value = res.data.records || []; total.value = res.data.total || 0 }
  } finally { loading.value = false }
}
const handlePage = (p) => { pageNum.value = p; load() }

const openAdd = () => { form.value = { id: null, ruleName: '', growthValue: 5, limitCount: 20, status: 1 }; dialog.value = true }
const openEdit = (row) => { form.value = { ...row }; dialog.value = true }
const save = async () => {
  if (!form.value.ruleName) return ElMessage.warning('请填写行为名称')
  const res = form.value.id ? await updateGrowthRule(form.value) : await saveGrowthRule(form.value)
  if (res.code === 200) { ElMessage.success('已保存'); dialog.value = false; load() }
}
const changeStatus = async (row, v) => {
  const res = await updateGrowthRuleStatus(row.id, v)
  if (res.code !== 200) { row.status = row.status === 1 ? 0 : 1; ElMessage.error('操作失败') }
}
const remove = (row) => {
  ElMessageBox.confirm(`确定删除「${row.ruleName}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await deleteGrowthRule(row.id)
    if (res.code === 200) { ElMessage.success('已删除'); load() }
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; font-weight: 600; color: #303133; }
.page-sub { font-size: 13px; color: #A0AEC0; margin-left: 8px; }
.point { color: #00C4A1; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
