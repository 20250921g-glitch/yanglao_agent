<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
        <el-button type="primary" @click="openDialog()">新增用户</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="query" class="search-bar">
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="用户名" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.realName" placeholder="姓名" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="roleName" label="角色" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" :disabled="isEdit" placeholder="登录用户名" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.realName" placeholder="真实姓名（选填）" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password
          :placeholder="isEdit ? '留空则不修改密码' : '请输入登录密码'" />
      </el-form-item>
      <el-form-item label="分配角色" prop="roleId">
        <el-select v-model="form.roleId" placeholder="请选择角色" style="width:100%">
          <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width:100%">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
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
import { getSysUserPage, addSysUser, updateSysUser, deleteSysUser, getSysRoleList } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const roleOptions = ref([])

const query = reactive({ username: '', realName: '' })
const form = reactive({ id: null, username: '', realName: '', password: '', roleId: null, status: 1 })
const rules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  roleId: [{ required: true, message: '请分配角色', trigger: 'change' }]
})

const load = async () => {
  loading.value = true
  try {
    const res = await getSysUserPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: query.username,
      realName: query.realName
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {} finally { loading.value = false }
}

const handleSearch = () => { pageNum.value = 1; load() }
const resetSearch = () => { query.username = ''; query.realName = ''; pageNum.value = 1; load() }

const openDialog = (row) => {
  rules.password[0].required = !row
  if (row) {
    Object.assign(form, {
      id: row.id,
      username: row.username,
      realName: row.realName,
      password: '',
      roleId: row.roleId,
      status: row.status
    })
    isEdit.value = true
  } else {
    formRef.value?.resetFields()
    Object.assign(form, { id: null, username: '', realName: '', password: '', roleId: null, status: 1 })
    isEdit.value = false
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const payload = { ...form }
  if (isEdit.value && !payload.password) delete payload.password
  try {
    if (isEdit.value) await updateSysUser(payload)
    else await addSysUser(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {}
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  try { await deleteSysUser(id); ElMessage.success('删除成功'); load() } catch (e) {}
}

onMounted(async () => {
  try { const r = await getSysRoleList(); roleOptions.value = r.data || [] } catch (e) {}
  load()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; }
</style>
