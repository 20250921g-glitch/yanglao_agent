<template>
  <div class="role-detail" style="padding:16px">
    <el-row :gutter="20">
      <!-- 左侧角色信息 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span style="font-weight:600">角色信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="角色名称">
              <el-tag type="primary">{{ roleInfo.roleName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="权限字符">{{ roleInfo.roleKey }}</el-descriptions-item>
            <el-descriptions-item label="角色描述">{{ roleInfo.description }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ roleInfo.createTime }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="roleInfo.status === 1 ? 'success' : 'danger'" size="small">
                {{ roleInfo.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="排序">{{ roleInfo.sort }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:20px;text-align:center">
            <el-button type="primary" @click="handleSavePerm">保存权限</el-button>
            <el-button type="danger" @click="handleDelete">删除角色</el-button>
            <el-button @click="$router.back()">返回</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧权限配置 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:600">权限配置</span>
              <div>
                <el-button size="small" @click="expandAll">全部展开</el-button>
                <el-button size="small" @click="collapseAll">全部折叠</el-button>
              </div>
            </div>
          </template>
          <div style="padding:8px 0">
            <el-tree
              ref="treeRef"
              :data="permissionTree"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              :default-expand-all="false"
              show-checkbox
              :default-checked-keys="defaultChecked"
              highlight-current
            >
              <template #default="{ data }">
                <span style="display:flex;align-items:center;gap:6px">
                  <span>{{ data.name }}</span>
                  <el-tag v-if="data.type==='btn'" size="small" type="info">{{ data.action }}</el-tag>
                </span>
              </template>
            </el-tree>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSysRoleById, getSysMenuTree, getRoleMenuIds, saveRoleMenus, deleteSysRole } from '@/api/index'

const route = useRoute()
const router = useRouter()
const treeRef = ref(null)

const roleInfo = ref({})
const defaultChecked = ref([])
const permissionTree = ref([])

const expandAll = () => {
  const nodes = treeRef.value?.store?.nodesMap || {}
  Object.values(nodes).forEach(n => n.expand())
}

const collapseAll = () => {
  const nodes = treeRef.value?.store?.nodesMap || {}
  Object.values(nodes).forEach(n => n.collapse())
}

const loadRoleInfo = async () => {
  try {
    const res = await getSysRoleById(route.params.id)
    if (res.code === 200) {
      roleInfo.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载角色信息失败')
  }
}

const loadPermissionTree = async () => {
  try {
    const res = await getSysMenuTree()
    if (res.code === 200) {
      permissionTree.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载菜单树失败')
  }
}

const loadRoleMenus = async () => {
  try {
    const res = await getRoleMenuIds(route.params.id)
    if (res.code === 200) {
      defaultChecked.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载角色权限失败')
  }
}

const handleSavePerm = async () => {
  try {
    const checked = treeRef.value?.getCheckedKeys() || []
    await saveRoleMenus(route.params.id, checked)
    ElMessage.success('权限保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async () => {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  try {
    await deleteSysRole(route.params.id)
    ElMessage.success('删除成功')
    router.push('/system/role')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await Promise.all([loadRoleInfo(), loadPermissionTree(), loadRoleMenus()])
  nextTick(() => {
    const nodes = treeRef.value?.store?.nodesMap || {}
    Object.values(nodes).forEach(n => n.expand())
  })
})
</script>
