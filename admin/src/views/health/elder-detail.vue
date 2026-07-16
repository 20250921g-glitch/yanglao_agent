<template>
  <div class="elder-detail" v-loading="loading">
    <el-empty v-if="notFound" description="未找到该老人档案" />

    <el-row v-else :gutter="20">
      <!-- 左侧基本信息 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="profile">
            <el-avatar :size="80" style="background:#00C4A1;font-size:32px;margin-bottom:12px">
              {{ elderInfo.name?.charAt(0) || '老' }}
            </el-avatar>
            <h3>{{ elderInfo.name }}</h3>
            <p class="sub">{{ elderInfo.gender === 1 ? '男' : '女' }} · {{ elderInfo.age }}岁</p>
            <el-tag :type="healthMeta.type" style="margin:8px 0">{{ healthMeta.text }}</el-tag>
            <el-divider />
            <div class="info-item"><span class="label">联系电话</span><span>{{ elderInfo.phone }}</span></div>
            <div class="info-item"><span class="label">身份证</span><span>{{ elderInfo.idCard }}</span></div>
            <div class="info-item"><span class="label">紧急联系人</span><span>{{ elderInfo.emergencyContact }}</span></div>
            <div class="info-item"><span class="label">紧急电话</span><span>{{ elderInfo.emergencyPhone }}</span></div>
            <div class="info-item"><span class="label">家庭住址</span><span>{{ elderInfo.address }}</span></div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧详情 -->
      <el-col :span="18">
        <el-card shadow="hover">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-descriptions :column="2" border style="margin-top:12px">
                <el-descriptions-item label="姓名">{{ elderInfo.name }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ elderInfo.gender === 1 ? '男' : '女' }}</el-descriptions-item>
                <el-descriptions-item label="年龄">{{ elderInfo.age }}岁</el-descriptions-item>
                <el-descriptions-item label="身份证号">{{ elderInfo.idCard }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ elderInfo.phone }}</el-descriptions-item>
                <el-descriptions-item label="健康等级">
                  <el-tag :type="healthMeta.type" size="small">{{ healthMeta.text }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="紧急联系人">{{ elderInfo.emergencyContact }}</el-descriptions-item>
                <el-descriptions-item label="紧急电话">{{ elderInfo.emergencyPhone }}</el-descriptions-item>
                <el-descriptions-item label="账号状态">
                  <el-tag :type="elderInfo.status === 1 ? 'success' : 'info'" size="small">
                    {{ elderInfo.status === 1 ? '正常' : '离世/退出' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="家庭住址" :span="2">{{ elderInfo.address }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ elderInfo.createTime || '—' }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ elderInfo.updateTime || '—' }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getElderById } from '@/api'

const route = useRoute()
const activeTab = ref('basic')
const loading = ref(false), notFound = ref(false)
const elderInfo = ref({})

const healthMeta = computed(() => {
  const map = { 1: { text: '自理', type: 'success' }, 2: { text: '半失能', type: 'warning' }, 3: { text: '失能', type: 'danger' } }
  return map[elderInfo.value.healthLevel] || { text: '未知', type: 'info' }
})

const load = async () => {
  loading.value = true
  try {
    const res = await getElderById(route.params.id)
    if (res.data) { elderInfo.value = res.data; notFound.value = false }
    else { notFound.value = true }
  } catch (e) {
    // 响应拦截器已弹出错误提示
    notFound.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.elder-detail { padding: 16px; }
.profile { text-align: center; }
.profile h3 { margin: 4px 0; font-size: 18px; }
.profile .sub { color: #A0AEC0; font-size: 14px; margin: 4px 0; }
.info-item { display: flex; flex-direction: column; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.info-item:last-child { border-bottom: none; }
.info-item .label { font-size: 12px; color: #A0AEC0; margin-bottom: 2px; }
</style>
