<template>
  <el-card v-loading="loading">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>员工详情</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </template>

    <el-empty v-if="!loading && notFound" description="未找到该员工" />

    <div v-else-if="info" class="staff-detail">
      <el-card class="left-card" :body-style="{ padding: '20px' }">
        <div class="info-panel">
          <el-avatar :size="80" style="background:#00C4A1;font-size:32px">{{ info.name?.charAt(0) }}</el-avatar>
          <div class="info-name">{{ info.name }}</div>
          <div class="info-tag">
            <el-tag :type="info.status === 1 ? 'success' : 'info'" size="large">
              {{ info.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-item"><span class="label">工号：</span>{{ info.staffNo || '—' }}</div>
          <div class="info-item"><span class="label">部门：</span>{{ info.dept || '—' }}</div>
          <div class="info-item"><span class="label">职位：</span>{{ info.position || '—' }}</div>
          <div class="info-item"><span class="label">手机号：</span>{{ info.phone || '—' }}</div>
          <div class="info-item"><span class="label">邮箱：</span>{{ info.email || '—' }}</div>
          <div class="info-item"><span class="label">入职时间：</span>{{ info.entryDate || '—' }}</div>
        </div>
      </el-card>

      <el-card class="right-card" :body-style="{ padding: '20px' }">
        <el-descriptions title="详细信息" :column="2" border>
          <el-descriptions-item label="员工ID">{{ info.id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ info.name || '—' }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ info.staffNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ info.dept || '—' }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ info.position || '—' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ info.gender || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ info.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ info.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ info.idCard || '—' }}</el-descriptions-item>
          <el-descriptions-item label="入职日期">{{ info.entryDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="info.status === 1 ? 'success' : 'info'" size="small">
              {{ info.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ info.remark || '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ info.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ info.updateTime || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStaffById } from '@/api'

const route = useRoute()
const loading = ref(false)
const info = ref(null)
const notFound = ref(false)

onMounted(async () => {
  const id = route.params.id
  if (!id) { notFound.value = true; return }
  loading.value = true
  try {
    const res = await getStaffById(id)
    info.value = res.data || null
    notFound.value = !info.value
  } catch (e) {
    // 响应拦截器已提示错误
    notFound.value = true
  } finally { loading.value = false }
})
</script>

<style scoped>
.staff-detail { display: flex; gap: 16px; }
.left-card { width: 280px; flex-shrink: 0; }
.right-card { flex: 1; min-width: 0; }
.info-panel { display: flex; flex-direction: column; align-items: center; text-align: center; }
.info-name { font-size: 20px; font-weight: bold; margin-top: 12px; color: #303133; }
.info-tag { margin-top: 8px; }
.info-item { width: 100%; text-align: left; padding: 8px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #606266; }
.info-item .label { color: #A0AEC0; margin-right: 4px; }
</style>
