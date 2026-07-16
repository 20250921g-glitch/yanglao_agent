<template>
  <div class="evaluation-detail" style="padding:16px">
    <div v-loading="loading" element-loading-text="加载中">
      <!-- 未找到 -->
      <el-empty v-if="!loading && !evaluationInfo" description="未找到该测评记录" style="margin-top:40px" />

      <!-- 测评信息 -->
      <el-card shadow="hover" mb-4 v-if="evaluationInfo">
        <template #header><span style="font-weight:600">测评信息</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="测评ID">{{ evaluationInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="测评标题">{{ evaluationInfo.title }}</el-descriptions-item>
          <el-descriptions-item label="测评类型">{{ evaluationInfo.type || '—' }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ evaluationInfo.score != null ? evaluationInfo.score + ' 分' : '—' }}</el-descriptions-item>
          <el-descriptions-item label="老人ID">{{ evaluationInfo.elderId != null ? evaluationInfo.elderId : '—' }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ evaluationInfo.elderName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ evaluationInfo.createTime || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:16px;text-align:right">
          <!-- TODO: 后端未提供测评更新/编辑接口，编辑功能待接入 -->
          <el-button type="primary" @click="ElMessage.info('编辑功能待后端接口支持')">编辑</el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </el-card>

      <!-- 测评内容 -->
      <el-card shadow="hover" v-if="evaluationInfo">
        <template #header><span style="font-weight:600">测评内容</span></template>
        <div style="white-space:pre-wrap;line-height:1.8;color:#606266">{{ evaluationInfo.content || '—' }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvaluationPage, deleteEvaluation } from '@/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const evaluationInfo = ref(null)

const load = async () => {
  loading.value = true
  try {
    // 后端无单条测评接口，分页拉取后在结果中按 id 匹配
    const res = await getEvaluationPage({ pageSize: 1000 })
    const records = res.data.records || []
    const targetId = String(route.params.id)
    evaluationInfo.value = records.find(r => String(r.id) === targetId) || null
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const handleDelete = async () => {
  await ElMessageBox.confirm('确定删除该测评吗？删除后不可恢复！', '警告', { type: 'warning' })
  try {
    await deleteEvaluation(evaluationInfo.value.id)
    ElMessage.success('删除成功')
    router.back()
  } catch (e) { /* 响应拦截器已提示错误 */ }
}

onMounted(load)
</script>

<style scoped>
[mb-4] { margin-bottom: 16px; }
</style>
