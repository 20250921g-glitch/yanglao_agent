<template>
  <el-card>
    <template #header><span>系统配置</span></template>

    <el-table :data="list" stripe v-loading="loading" border>
      <template #empty><el-empty description="暂无数据" /></template>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="configName" label="配置名称" min-width="160" />
      <el-table-column prop="configKey" label="配置键" min-width="160" />
      <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
      <el-table-column prop="configType" label="配置类型" width="140" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end"
      @current-change="load" @size-change="load" />
  </el-card>

  <el-dialog v-model="editVisible" title="编辑配置" width="540px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="配置名称">
        <el-input v-model="form.configName" placeholder="请输入配置名称" />
      </el-form-item>
      <el-form-item label="配置键">
        <el-input v-model="form.configKey" placeholder="请输入配置键" />
      </el-form-item>
      <el-form-item label="配置值">
        <el-input v-model="form.configValue" type="textarea" :rows="3" placeholder="请输入配置值" />
      </el-form-item>
      <el-form-item label="配置类型">
        <el-input v-model="form.configType" placeholder="请输入配置类型" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible=false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfigPage, updateSystemConfig } from '@/api'

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const list = ref([])
const editVisible = ref(false), saving = ref(false)
const form = reactive({ id: null, configName: '', configKey: '', configValue: '', configType: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await getSystemConfigPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const openEdit = (row) => {
  form.id = row.id
  form.configName = row.configName || ''
  form.configKey = row.configKey || ''
  form.configValue = row.configValue || ''
  form.configType = row.configType || ''
  editVisible.value = true
}

const save = async () => {
  saving.value = true
  try {
    await updateSystemConfig({
      id: form.id,
      configName: form.configName,
      configKey: form.configKey,
      configValue: form.configValue,
      configType: form.configType
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { saving.value = false }
}

onMounted(load)
</script>
