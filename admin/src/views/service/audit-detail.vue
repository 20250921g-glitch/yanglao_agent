<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <el-button @click="$router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <span class="page-title">审核详情</span>
      <el-tag :type="tagType" size="large">{{ statusText }}</el-tag>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" style="text-align:center;padding:60px">
      <el-icon class="is-loading" style="font-size:32px;color:#00C4A1"><Loading /></el-icon>
    </div>

    <div v-else-if="detail">
      <!-- 基础信息卡片 -->
      <el-card class="info-card">
        <template #header><span class="card-title">基础信息</span></template>
        <el-row :gutter="24">
          <el-col :span="12"><div class="info-item"><span class="label">服务人员ID：</span>{{ detail.workerId || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">姓名：</span>{{ detail.workerName || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">手机号：</span>{{ detail.phone || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">服务类型：</span>{{ detail.serviceType || '-' }}</div></el-col>
          <el-col :span="24"><div class="info-item"><span class="label">简介：</span>{{ detail.intro || '-' }}</div></el-col>
          <el-col :span="24">
            <div class="info-item"><span class="label">头像：</span>
              <el-avatar v-if="detail.avatar" :size="60" :src="detail.avatar" />
              <el-avatar v-else :size="60">{{ (detail.workerName || '?').charAt(0) }}</el-avatar>
            </div>
          </el-col>
          <el-col :span="24">
            <div class="info-item"><span class="label">标签：</span>
              <el-tag v-for="tag in (detail.tags || [])" :key="tag" size="small" style="margin-right:6px">{{ tag }}</el-tag>
              <span v-if="!detail.tags?.length" style="color:#999">无</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 实名信息卡片 -->
      <el-card class="info-card">
        <template #header><span class="card-title">实名信息</span></template>
        <el-row :gutter="24">
          <el-col :span="12"><div class="info-item"><span class="label">身份证号：</span>{{ detail.idCard || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">银行卡号：</span>{{ detail.bankCard || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">开户行：</span>{{ detail.bankName || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">职业证书：</span>{{ detail.certificate || '-' }}</div></el-col>
        </el-row>
      </el-card>

      <!-- 其他信息卡片 -->
      <el-card class="info-card">
        <template #header><span class="card-title">其他信息</span></template>
        <el-row :gutter="24">
          <el-col :span="12"><div class="info-item"><span class="label">注册时间：</span>{{ detail.createTime || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">申请加入时间：</span>{{ detail.applyTime || '-' }}</div></el-col>
          <el-col :span="12"><div class="info-item"><span class="label">最近登录时间：</span>{{ detail.lastLoginTime || '-' }}</div></el-col>
          <el-col :span="12">
            <div class="info-item"><span class="label">允许打赏：</span>
              <el-tag :type="detail.allowTip === 1 ? 'success' : 'info'" size="small">{{ detail.allowTip === 1 ? '允许' : '禁止' }}</el-tag>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 审核操作区 -->
      <div class="action-bar">
        <!-- 待审核 -->
        <template v-if="detail.status === 0">
          <el-button type="success" size="large" @click="doAudit(1)" :loading="submitting">审核通过</el-button>
          <el-button type="danger" size="large" @click="showRejectDialog = true">驳回</el-button>
        </template>
        <!-- 已通过 -->
        <template v-else-if="detail.status === 1">
          <el-result icon="success" title="已审核通过">
            <template #sub-title>
              <p>审核时间：{{ detail.auditTime || '-' }}</p>
              <p>审核人：{{ detail.auditorName || '-' }}</p>
            </template>
          </el-result>
        </template>
        <!-- 已驳回 -->
        <template v-else-if="detail.status === 2">
          <el-result icon="error" title="已驳回">
            <template #sub-title>
              <p>驳回原因：{{ detail.rejectReason || '-' }}</p>
              <p>审核时间：{{ detail.auditTime || '-' }}</p>
            </template>
          </el-result>
        </template>
      </div>
    </div>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showRejectDialog" title="驳回审核" width="440px">
      <el-form>
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" @click="doReject" :loading="submitting">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceAudit, auditRecord } from '@/api'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref(null)
const submitting = ref(false)
const showRejectDialog = ref(false)
const rejectReason = ref('')

const id = computed(() => route.params.id)

const statusText = computed(() => ['待审核', '已通过', '已驳回'][detail.value?.status] || '-')
const tagType = computed(() => ['', 'success', 'danger'][detail.value?.status] || 'info')

const load = async () => {
  loading.value = true
  try {
    const res = await getServiceAudit(id.value)
    detail.value = res.data
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const doAudit = async (status) => {
  await ElMessageBox.confirm('确认审核通过？', '提示', { type: 'warning' })
  submitting.value = true
  try {
    await auditRecord(id.value, { status, rejectReason: '' })
    ElMessage.success('审核通过')
    showRejectDialog.value = false
    load()
  } catch (e) {} finally {
    submitting.value = false
  }
}

const doReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  submitting.value = true
  try {
    await auditRecord(id.value, { status: 2, rejectReason: rejectReason.value })
    ElMessage.success('已驳回')
    showRejectDialog.value = false
    load()
  } catch (e) {} finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page-container {
  padding: 16px;
  max-width: 900px;
  margin: 0 auto;
}
.top-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  flex: 1;
}
.info-card {
  margin-bottom: 16px;
}
.card-title {
  font-weight: 600;
  font-size: 15px;
}
.info-item {
  padding: 8px 0;
  font-size: 14px;
  color: #333;
}
.info-item .label {
  color: #666;
  font-weight: 500;
  display: inline-block;
  min-width: 110px;
}
.action-bar {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
}
</style>
