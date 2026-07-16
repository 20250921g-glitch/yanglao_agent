<template>
  <div class="detail-container">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
      <span class="page-title">用户详情</span>
      <div class="action-buttons">
        <el-button type="primary" :icon="Edit" @click="handleEdit">编辑</el-button>
        <el-button :type="userInfo.status === 1 ? 'warning' : 'success'" :icon="Switch" @click="handleToggleStatus">
          {{ userInfo.status === 1 ? '禁用' : '启用' }}
        </el-button>
        <el-button type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <!-- 左侧：基本信息卡片 -->
      <el-col :span="8">
        <el-card shadow="never" class="left-card">
          <div class="user-header">
            <el-avatar :size="80" :src="userInfo.avatar" style="flex-shrink:0">
              {{ userInfo.realName?.charAt(0) || '?' }}
            </el-avatar>
            <div class="user-header-info">
              <div class="user-name">{{ userInfo.realName || '-' }}</div>
              <div class="user-phone">{{ userInfo.phone || '-' }}</div>
              <div class="user-level">
                <el-tag type="warning" size="small">{{ userInfo.levelName || '普通用户' }}</el-tag>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 标签 -->
          <div class="section-title">用户标签</div>
          <div class="tags-wrap">
            <el-tag
              v-for="tag in userTags" :key="tag"
              closable size="small" style="margin:4px 4px 0 0"
              @close="handleRemoveTag(tag)"
            >{{ tag }}</el-tag>
            <el-button size="small" text type="primary" @click="showTagDialog = true" style="margin-top:4px">+ 添加标签</el-button>
          </div>

          <el-divider />

          <!-- 状态 -->
          <div class="section-title">账户状态</div>
          <el-switch
            v-model="userInfo.status"
            :active-value="1" :inactive-value="0"
            active-text="正常" inactive-text="禁用"
            style="margin-bottom:8px"
            @change="handleToggleStatus"
          />
          <div class="status-info">
            <span class="label">注册时间：</span><span>{{ userInfo.createTime || '-' }}</span>
          </div>
          <div class="status-info">
            <span class="label">登录方式：</span><span>{{ loginTypeText }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：Tab区域 -->
      <el-col :span="16">
        <el-card shadow="never">
          <el-tabs v-model="activeTab">
            <!-- Tab1: 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="真实姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="昵称">{{ userInfo.username || '-' }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ userInfo.gender === 1 ? '男' : userInfo.gender === 0 ? '女' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="出生日期">{{ userInfo.birthDate || '-' }}</el-descriptions-item>
                <el-descriptions-item label="年龄">{{ age != null ? age : '-' }}</el-descriptions-item>
                <el-descriptions-item label="身份证号" :span="2">{{ userInfo.idCard || '-' }}</el-descriptions-item>
                <el-descriptions-item label="民族">{{ userInfo.nation || '-' }}</el-descriptions-item>
                <el-descriptions-item label="籍贯">{{ userInfo.nativePlace || '-' }}</el-descriptions-item>
                <el-descriptions-item label="婚姻状况">{{ userInfo.marriage || '-' }}</el-descriptions-item>
                <el-descriptions-item label="文化程度">{{ userInfo.education || '-' }}</el-descriptions-item>
                <el-descriptions-item label="职业">{{ userInfo.occupation || '-' }}</el-descriptions-item>
                <el-descriptions-item label="工作单位" :span="2">{{ userInfo.workUnit || '-' }}</el-descriptions-item>
                <el-descriptions-item label="身高(cm)">{{ userInfo.height || '-' }}</el-descriptions-item>
                <el-descriptions-item label="体重(kg)">{{ userInfo.weight || '-' }}</el-descriptions-item>
                <el-descriptions-item label="家庭住址" :span="2">{{ userInfo.address || '-' }}</el-descriptions-item>
                <el-descriptions-item label="紧急联系人">{{ userInfo.emergencyContact || '-' }}</el-descriptions-item>
                <el-descriptions-item label="紧急联系电话">{{ userInfo.emergencyPhone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="简介" :span="2">{{ userInfo.bio || userInfo.remark || '-' }}</el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ userInfo.createTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="用户类型">{{ roleText }}</el-descriptions-item>
                <el-descriptions-item label="登录方式">{{ loginTypeText }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>

            <!-- Tab2: 健康数据 -->
            <el-tab-pane label="健康数据" name="health">
              <el-form :inline="true" style="margin-bottom:12px">
                <el-form-item label="记录类型">
                  <el-select v-model="healthSearch.recordType" placeholder="全部" clearable style="width:140px">
                    <el-option value="血压" label="血压" />
                    <el-option value="血糖" label="血糖" />
                    <el-option value="心率" label="心率" />
                    <el-option value="体重" label="体重" />
                    <el-option value="睡眠" label="睡眠" />
                    <el-option value="步数" label="步数" />
                    <el-option value="血氧" label="血氧" />
                    <el-option value="体温" label="体温" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadHealthRecords">查询</el-button>
                  <el-button @click="healthSearch.recordType = ''; loadHealthRecords()">重置</el-button>
                </el-form-item>
              </el-form>
              <el-table :data="healthRecords" stripe v-loading="healthLoading" size="small">
                <el-table-column prop="recordType" label="记录类型" width="100" />
                <el-table-column prop="recordValue" label="记录值" min-width="120" />
                <el-table-column prop="recordTime" label="记录时间" width="160" />
                <el-table-column prop="doctor" label="医生/来源" width="120" />
                <el-table-column prop="remark" label="备注" show-overflow-tooltip />
              </el-table>
              <el-pagination v-model:current-page="healthPageNum" v-model:page-size="healthPageSize" :total="healthTotal"
                layout="total,sizes,prev,pager,next" style="margin-top:12px;justify-content:flex-end" @change="loadHealthRecords" />
            </el-tab-pane>

            <!-- Tab3: 资产信息 -->
            <el-tab-pane label="资产信息" name="asset">
              <el-row :gutter="16" style="margin-bottom:20px">
                <el-col :span="6">
                  <el-statistic title="优惠券" :value="assetInfo.couponCount">
                    <template #prefix><el-icon><Ticket /></el-icon></template>
                  </el-statistic>
                </el-col>
                <el-col :span="6">
                  <el-statistic title="积分" :value="assetInfo.integral">
                    <template #prefix><el-icon><Coin /></el-icon></template>
                  </el-statistic>
                </el-col>
                <el-col :span="6">
                  <el-statistic title="成长值" :value="assetInfo.growthValue">
                    <template #prefix><el-icon><TrendCharts /></el-icon></template>
                  </el-statistic>
                </el-col>
                <el-col :span="6">
                  <el-statistic title="会员等级" :value="userInfo.levelName || '普通用户'" />
                </el-col>
              </el-row>

              <div class="section-title" style="margin-bottom:12px">最近优惠券</div>
              <el-table :data="coupons" stripe v-loading="couponLoading" size="small">
                <el-table-column prop="name" label="券名称" />
                <el-table-column prop="type" label="类型" width="80">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.type === '满减' ? 'danger' : 'success'">{{ row.type || '-' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额/折扣" width="100" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">{{ row.status === 1 ? '可用' : '已用/过期' }}</template>
                </el-table-column>
                <el-table-column prop="expireTime" label="有效期至" width="120" />
              </el-table>
            </el-tab-pane>

            <!-- Tab4: 订单记录 -->
            <el-tab-pane label="订单记录" name="productOrder">
              <el-table :data="productOrders" stripe v-loading="productOrderLoading" size="small">
                <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
                <el-table-column prop="productName" label="商品名称" min-width="120" />
                <el-table-column prop="quantity" label="数量" width="70" />
                <el-table-column prop="totalAmount" label="实付金额" width="100">
                  <template #default="{ row }">¥{{ row.totalAmount }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="orderStatusMap[row.status] || 'info'">{{ orderStatusText[row.status] || '未知' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="下单时间" width="160" />
              </el-table>
              <el-pagination v-model:current-page="productPageNum" v-model:page-size="productPageSize" :total="productTotal"
                layout="total,sizes,prev,pager,next" style="margin-top:12px;justify-content:flex-end" @change="loadProductOrders" />
            </el-tab-pane>

            <!-- Tab5: 服务记录 -->
            <el-tab-pane label="服务记录" name="serviceOrder">
              <el-table :data="serviceOrders" stripe v-loading="serviceOrderLoading" size="small">
                <el-table-column prop="orderNo" label="服务单号" width="180" show-overflow-tooltip />
                <el-table-column prop="serviceName" label="服务名称" min-width="120" />
                <el-table-column prop="workerName" label="服务人员" width="100" />
                <el-table-column prop="serviceTime" label="预约时间" width="160" />
                <el-table-column prop="status" label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="serviceStatusMap[row.status] || 'info'">{{ serviceStatusText[row.status] || '未知' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="下单时间" width="160" />
              </el-table>
              <el-pagination v-model:current-page="servicePageNum" v-model:page-size="servicePageSize" :total="serviceTotal"
                layout="total,sizes,prev,pager,next" style="margin-top:12px;justify-content:flex-end" @change="loadServiceOrders" />
            </el-tab-pane>

            <!-- Tab6: 社交信息 -->
            <el-tab-pane label="社交信息" name="social">
              <el-empty description="暂无社交数据" v-if="!socialInfo.dynamicCount" />
              <el-descriptions v-else :column="2" border size="small">
                <el-descriptions-item label="动态数">{{ socialInfo.dynamicCount }}</el-descriptions-item>
                <el-descriptions-item label="关注数">{{ socialInfo.followCount }}</el-descriptions-item>
                <el-descriptions-item label="粉丝数">{{ socialInfo.fansCount }}</el-descriptions-item>
                <el-descriptions-item label="点赞数">{{ socialInfo.likeCount }}</el-descriptions-item>
                <el-descriptions-item label="评论数">{{ socialInfo.commentCount }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>

            <!-- Tab7: 操作记录 -->
            <el-tab-pane label="操作记录" name="log">
              <el-table :data="operationLogs" stripe size="small" v-loading="logLoading">
                <el-table-column prop="createTime" label="操作时间" width="170" />
                <el-table-column prop="userName" label="操作人" width="100" />
                <el-table-column prop="module" label="模块" width="150" />
                <el-table-column prop="operation" label="操作类型" width="120" />
                <el-table-column prop="requestUri" label="请求" min-width="180" show-overflow-tooltip />
                <el-table-column label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                      {{ row.status === 1 ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加标签对话框 -->
    <el-dialog v-model="showTagDialog" title="添加标签" width="400px">
      <el-form>
        <el-form-item label="选择标签">
          <el-select v-model="newTag" placeholder="请选择标签" filterable allow-create clearable style="width:100%">
            <el-option v-for="tag in allTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTagDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Delete, Switch, Ticket, Coin, TrendCharts } from '@element-plus/icons-vue'
import {
  getAppUser, updateAppUserStatus, deleteAppUser,
  getAppUserTags, updateAppUserTags, getUserTagList,
  getRecordPage, getProductOrderPage, getServiceOrderPage,
  getCouponPage, getOperationLogPage
} from '@/api'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const loading = ref(false)
const activeTab = ref('basic')
// 切换到操作记录 Tab 时加载真实日志
watch(activeTab, (nv) => {
  if (nv === 'log') loadOperationLogs()
})
const userInfo = ref({})
const userTags = ref([])
const allTags = ref([])
const showTagDialog = ref(false)
const newTag = ref('')

// 由出生日期推算年龄
const age = computed(() => {
  const b = userInfo.value?.birthDate
  if (!b) return null
  const birth = new Date(b)
  if (isNaN(birth.getTime())) return null
  const now = new Date()
  let a = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) a--
  return a >= 0 && a < 150 ? a : null
})

// 登录方式：后端 source 字段当前未填充，按手机号注册合理推断
const loginTypeText = computed(() => {
  const s = userInfo.value?.source
  if (s) return s
  return userInfo.value?.phone ? '手机号注册' : '-'
})

// 用户类型中文映射
const roleText = computed(() => {
  const map = { ELDER: '老人', FAMILY: '家属', VOLUNTEER: '志愿者', STAFF: '工作人员' }
  return map[userInfo.value?.role] || '—'
})

// 健康记录
const healthLoading = ref(false)
const healthRecords = ref([])
const healthPageNum = ref(1)
const healthPageSize = ref(10)
const healthTotal = ref(0)
const healthSearch = reactive({ recordType: '' })

// 资产
const assetInfo = ref({ couponCount: 0, integral: 0, growthValue: 0 })
const couponLoading = ref(false)
const coupons = ref([])

// 订单
const productOrderLoading = ref(false)
const productOrders = ref([])
const productPageNum = ref(1)
const productPageSize = ref(10)
const productTotal = ref(0)
const orderStatusMap = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
const orderStatusText = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '已完成', 4: '退款中' }

// 服务订单
const serviceOrderLoading = ref(false)
const serviceOrders = ref([])
const servicePageNum = ref(1)
const servicePageSize = ref(10)
const serviceTotal = ref(0)
const serviceStatusMap = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
const serviceStatusText = { 0: '已取消', 1: '待服务', 2: '服务中', 3: '已完成', 4: '异常' }

// 社交 & 操作日志
const socialInfo = ref({})
const logLoading = ref(false)
const operationLogs = ref([])

// 操作记录：真实落库数据（操作员维度的操作日志）
const loadOperationLogs = async () => {
  logLoading.value = true
  try {
    const res = await getOperationLogPage({ pageNum: 1, pageSize: 20, userName: 'admin' })
    operationLogs.value = res.data?.records || res.data?.list || []
  } catch (e) {
    operationLogs.value = []
  } finally {
    logLoading.value = false
  }
}

// 加载用户基本信息
const loadUserInfo = async () => {
  loading.value = true
  try {
    const res = await getAppUser(id)
    userInfo.value = res.data || {}
    // 加载资产信息
    assetInfo.value.couponCount = res.data.couponCount || 0
    assetInfo.value.integral = res.data.integral || res.data.points || 0
    assetInfo.value.growthValue = res.data.growthValue || res.data.growth || 0
  } catch (e) {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

// 加载用户标签
const loadUserTags = async () => {
  try {
    const res = await getAppUserTags(id)
    userTags.value = res.data || []
  } catch (e) {}
}

// 加载全部可选标签
const loadAllTags = async () => {
  try {
    const res = await getUserTagList()
    allTags.value = (res.data || []).map(t => t.name || t.tagName || t)
  } catch (e) {}
}

// 加载健康记录
const loadHealthRecords = async () => {
  healthLoading.value = true
  try {
    const res = await getRecordPage({
      pageNum: healthPageNum.value,
      pageSize: healthPageSize.value,
      userId: id,
      recordType: healthSearch.recordType || undefined
    })
    healthRecords.value = res.data.records || []
    healthTotal.value = res.data.total || 0
  } catch (e) {
    healthRecords.value = []
  } finally {
    healthLoading.value = false
  }
}

// 加载优惠券
const loadCoupons = async () => {
  couponLoading.value = true
  try {
    const res = await getCouponPage({ pageNum: 1, pageSize: 10, userId: id })
    coupons.value = res.data.records || []
    assetInfo.value.couponCount = res.data.total || 0
  } catch (e) {
    coupons.value = []
  } finally {
    couponLoading.value = false
  }
}

// 加载商品订单
const loadProductOrders = async () => {
  productOrderLoading.value = true
  try {
    const res = await getProductOrderPage({
      pageNum: productPageNum.value,
      pageSize: productPageSize.value,
      userId: id
    })
    productOrders.value = res.data.records || []
    productTotal.value = res.data.total || 0
  } catch (e) {
    productOrders.value = []
  } finally {
    productOrderLoading.value = false
  }
}

// 加载服务订单
const loadServiceOrders = async () => {
  serviceOrderLoading.value = true
  try {
    const res = await getServiceOrderPage({
      pageNum: servicePageNum.value,
      pageSize: servicePageSize.value,
      userId: id
    })
    serviceOrders.value = res.data.records || []
    serviceTotal.value = res.data.total || 0
  } catch (e) {
    serviceOrders.value = []
  } finally {
    serviceOrderLoading.value = false
  }
}

// 编辑
const handleEdit = () => {
  router.push({ name: 'UserAdd', query: { id } })
}

// 切换状态
const handleToggleStatus = async () => {
  try {
    await updateAppUserStatus(id, userInfo.value.status)
    ElMessage.success(userInfo.value.status === 1 ? '已启用' : '已禁用')
  } catch (e) {}
}

// 删除
const handleDelete = async () => {
  await ElMessageBox.confirm('确认删除该用户？删除后不可恢复。', '危险操作', { type: 'warning' })
  await deleteAppUser(id)
  ElMessage.success('删除成功')
  router.push({ name: 'AppUser' })
}

// 添加标签
const handleAddTag = async () => {
  if (!newTag.value) return
  const tags = [...userTags.value, newTag.value]
  try {
    await updateAppUserTags(id, tags)
    userTags.value = tags
    showTagDialog.value = false
    newTag.value = ''
    ElMessage.success('标签已添加')
  } catch (e) {}
}

// 移除标签
const handleRemoveTag = async (tag) => {
  const tags = userTags.value.filter(t => t !== tag)
  try {
    await updateAppUserTags(id, tags)
    userTags.value = tags
    ElMessage.success('标签已移除')
  } catch (e) {}
}

onMounted(() => {
  loadUserInfo()
  loadUserTags()
  loadAllTags()
})
</script>

<style scoped>
.detail-container {
  padding: 16px;
}
.top-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}
.action-buttons {
  display: flex;
  gap: 8px;
}
.left-card {
  height: 100%;
}
.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-header-info {
  min-width: 0;
}
.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.user-phone {
  font-size: 13px;
  color: #A0AEC0;
  margin-top: 4px;
}
.user-level {
  margin-top: 6px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}
.tags-wrap {
  min-height: 40px;
}
.status-info {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}
.status-info .label {
  color: #A0AEC0;
}
</style>
