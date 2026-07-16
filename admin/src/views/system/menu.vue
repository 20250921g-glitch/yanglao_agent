<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>菜单管理</span><el-button type="primary" @click="openDialog()">新增菜单</el-button></div>
    </template>
    <el-tree :data="menuTree" :props="{ label: 'name', children: 'children' }" default-expand-all>
      <template #default="{ data }">
        <span class="menu-node">
          <span>{{ data.name }}</span>
          <span class="menu-ops">
            <el-button link type="primary" @click.stop="openDialog(data)">编辑</el-button>
            <el-button link type="danger" @click.stop="handleDelete(data.id)">删除</el-button>
          </span>
        </span>
      </template>
    </el-tree>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="上级菜单" prop="parentId">
        <el-select v-model="form.parentId" style="width:100%">
          <el-option :value="0" label="无" />
          <template v-for="item in menuTree" :key="item.id">
            <el-option :value="item.id" :label="item.name" />
            <el-option v-for="child in item.children" :key="child.id" :value="child.id" :label="'├── ' + child.name" />
          </template>
        </el-select>
      </el-form-item>
      <el-form-item label="菜单名称" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="菜单类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio :label="1">目录</el-radio>
          <el-radio :label="2">菜单</el-radio>
          <el-radio :label="3">按钮</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="路由路径" prop="path"><el-input v-model="form.path" /></el-form-item>
      <el-form-item label="组件路径"><el-input v-model="form.component" placeholder="如: system/user" /></el-form-item>
      <el-form-item label="权限标识"><el-input v-model="form.perms" placeholder="如: system:user:list" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width:100%" /></el-form-item>
      <el-form-item label="图标" v-if="form.type !== 3"><el-input v-model="form.icon" placeholder="如: User" /></el-form-item>
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
import { getSysMenuTree, addSysMenu, updateSysMenu, deleteSysMenu } from '@/api'

const menuTree = ref([])
const dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, parentId: 0, name: '', type: 1, path: '', component: '', perms: '', icon: '', sort: 0 })
const rules = { name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }], type: [{ required: true, message: '请选择类型', trigger: 'change' }] }

const load = async () => {
  try { const res = await getSysMenuTree(); menuTree.value = res.data || [] } catch (e) {}
}

const openDialog = (row) => {
  if (row) { Object.assign(form, { ...row, children: undefined }); isEdit.value = true }
  else { formRef.value?.resetFields(); Object.assign(form, { id: null, parentId: 0, name: '', type: 1, path: '', component: '', perms: '', icon: '', sort: 0 }); isEdit.value = false }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try { isEdit.value ? await updateSysMenu(form) : await addSysMenu(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {}
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该菜单吗？', '提示', { type: 'warning' })
  try { await deleteSysMenu(id); ElMessage.success('删除成功'); load() } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.menu-node { display:flex; justify-content:space-between; width:100%; padding-right:8px; }
.menu-ops { display:flex; gap:4px; }
</style>
