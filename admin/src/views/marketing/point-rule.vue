<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">积分规则配置</h2>
          <span class="page-sub">配置用户获取与消费积分的行为、分值与上限</span>
        </div>
        <div>
          <el-button type="primary" v-has="'pointrule:add'" @click="openAdd">新增规则</el-button>
        </div>
      </div>

      <el-form :inline="true" :model="search" class="search-bar">
        <el-form-item label="规则名称">
          <el-input v-model="search.ruleName" placeholder="请输入规则名称" clearable @keyup.enter="load" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="search.actionType" placeholder="全部" clearable style="width:130px">
            <el-option label="获取" value="获取" />
            <el-option label="消费" value="消费" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <template #empty><el-empty description="暂无积分规则" /></template>
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column prop="ruleCode" label="规则编码" width="150" />
        <el-table-column prop="actionType" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.actionType === '获取' ? 'success' : 'warning'" size="small">{{ row.actionType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分值" width="110" align="center">
          <template #default="{ row }">
            <span :class="row.actionType === '获取' ? 'plus' : 'minus'">
              {{ row.actionType === '获取' ? '+' : '-' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="limitCount" label="次数上限" width="110" align="center">
          <template #default="{ row }">{{ row.limitCount > 0 ? row.limitCount : '不限制' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="(v) => changeStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link v-has="'pointrule:edit'" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link v-has="'pointrule:delete'" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" :current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="handlePage" />
    </el-card>

    <el-dialog v-model="dialog" :title="form.id ? '编辑规则' : '新增规则'" width="480px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="规则名称">
          <el-input v-model="form.ruleName" placeholder="如：每日签到" />
        </el-form-item>
        <el-form-item label="规则编码">
          <el-input v-model="form.ruleCode" placeholder="如：daily_sign（选填）" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.actionType">
            <el-radio value="获取">获取</el-radio>
            <el-radio value="消费">消费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分值">
          <el-input-number v-model="form.points" :min="0" :step="5" />
        </el-form-item>
        <el-form-item label="次数上限">
          <el-input-number v-model="form.limitCount" :min="0" :step="1" />
          <span class="hint">0 表示不限制</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="规则说明（选填）" />
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
import { getPointRulePage, addPointRule, updatePointRule, deletePointRule } from '@/api/index'

const loading = ref(false)
const dialog = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const search = ref({ ruleName: '', actionType: '' })
const form = ref({ id: null, ruleName: '', ruleCode: '', actionType: '获取', points: 5, limitCount: 0, status: 1, remark: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await getPointRulePage({ pageNum: pageNum.value, pageSize: pageSize.value, ruleName: search.value.ruleName || undefined, actionType: search.value.actionType || undefined })
    if (res.code === 200) { list.value = res.data.records || []; total.value = res.data.total || 0 }
  } finally { loading.value = false }
}
const handlePage = (p) => { pageNum.value = p; load() }
const resetSearch = () => { search.value = { ruleName: '', actionType: '' }; pageNum.value = 1; load() }

const openAdd = () => { form.value = { id: null, ruleName: '', ruleCode: '', actionType: '获取', points: 5, limitCount: 0, status: 1, remark: '' }; dialog.value = true }
const openEdit = (row) => { form.value = { ...row }; dialog.value = true }
const save = async () => {
  if (!form.value.ruleName) return ElMessage.warning('请填写规则名称')
  const res = form.value.id ? await updatePointRule(form.value) : await addPointRule(form.value)
  if (res.code === 200) { ElMessage.success('已保存'); dialog.value = false; load() }
}
const changeStatus = async (row, v) => {
  const res = await updatePointRule({ id: row.id, status: v })
  if (res.code !== 200) { row.status = row.status === 1 ? 0 : 1; ElMessage.error('操作失败') }
}
const remove = (row) => {
  ElMessageBox.confirm(`确定删除「${row.ruleName}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await deletePointRule(row.id)
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
.search-bar { margin-bottom: 8px; }
.plus { color: #00C4A1; font-weight: 600; }
.minus { color: #FAAD14; font-weight: 600; }
.hint { margin-left: 8px; font-size: 12px; color: #A0AEC0; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
