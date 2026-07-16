<template>
  <div>
    <!-- 优惠券信息卡片 -->
    <el-card v-loading="loading" style="margin-bottom:16px">
      <template #header><span style="font-weight:bold;font-size:16px">优惠券信息</span></template>
      <el-descriptions v-if="found" :column="3" border>
        <el-descriptions-item label="优惠券ID">{{ info.id }}</el-descriptions-item>
        <el-descriptions-item label="优惠券名称">{{ info.name }}</el-descriptions-item>
        <el-descriptions-item label="优惠券类型">
          <el-tag :type="info.type === 1 ? 'warning' : 'primary'" size="small">
            {{ info.type === 1 ? '满减券' : '折扣券' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优惠金额">
          <span style="color:#52C41A;font-weight:bold">{{ info.type === 1 ? `¥${info.denomination}` : `${(info.denomination * 10).toFixed(1)}折` }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="使用门槛">{{ info.minAmount > 0 ? `满${info.minAmount}元` : '无门槛' }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ info.startTime }} ~ {{ info.endTime }}</el-descriptions-item>
        <el-descriptions-item label="发放总量">{{ info.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="剩余数量">{{ info.remainCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="info.status === 1 ? 'success' : 'info'" size="small">
            {{ info.status === 1 ? '生效' : '失效' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ info.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="未找到该优惠券" />
      <div v-if="found" style="margin-top:16px;text-align:right">
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button :type="info.status === 1 ? 'warning' : 'success'" @click="toggleStatus">
          {{ info.status === 1 ? '停用' : '启用' }}
        </el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </el-card>

    <!-- 领取记录 -->
    <el-card>
      <template #header><span style="font-weight:bold;font-size:16px">领取记录</span></template>
      <!-- TODO: 后端暂无优惠券领取记录接口，暂以空列表展示 -->
      <el-table :data="recordList" stripe size="small">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="nickname" label="用户昵称" width="140" />
        <el-table-column prop="receiveTime" label="领取时间" width="160" />
        <el-table-column label="使用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.usedStatus === 1 ? 'success' : row.usedStatus === 2 ? 'info' : 'warning'" size="small">
              {{ ['未使用', '已使用', '已过期'][row.usedStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usedTime" label="使用时间" width="160" />
        <el-table-column prop="orderNo" label="订单编号" width="160" />
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑优惠券" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">满减券</el-radio>
            <el-radio :label="2">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.type === 1 ? '面额(元)' : '折扣率'" prop="denomination">
          <el-input-number v-if="form.type === 1" v-model="form.denomination" :min="0.01" :precision="2" style="width:100%" />
          <el-input-number v-else v-model="form.denomination" :min="0.01" :max="1" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="使用门槛"><el-input-number v-model="form.minAmount" :min="0" style="width:100%" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponPage, updateCoupon, updateCouponStatus, deleteCoupon } from '@/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const found = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

const info = reactive({
  id: '', name: '', type: 1, denomination: 0, minAmount: 0,
  totalCount: 0, remainCount: 0, startTime: '', endTime: '', status: 1, createTime: ''
})

const form = reactive({ id: '', name: '', type: 1, denomination: 0, minAmount: 0, startTime: '', endTime: '' })
const rules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

// 领取记录（后端暂无接口，保持空列表）
const recordList = ref([])

const load = async () => {
  loading.value = true
  try {
    const routeId = Number(route.params.id)
    const res = await getCouponPage({ pageSize: 1000 })
    const row = (res.data.records || []).find(r => r.id === routeId)
    if (row) {
      Object.assign(info, { ...row })
      found.value = true
    } else {
      found.value = false
    }
  } catch (e) { /* 响应拦截器已提示 */ }
  finally { loading.value = false }
}

const handleEdit = () => {
  Object.assign(form, {
    id: info.id, name: info.name, type: info.type,
    denomination: Number(info.denomination), minAmount: Number(info.minAmount),
    startTime: info.startTime, endTime: info.endTime
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await updateCoupon({
    id: form.id, name: form.name, type: form.type,
    denomination: form.denomination, minAmount: form.minAmount,
    startTime: form.startTime, endTime: form.endTime
  })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const toggleStatus = async () => {
  const next = info.status === 1 ? 0 : 1
  await updateCouponStatus(info.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已停用')
  load()
}

const handleDelete = async () => {
  await ElMessageBox.confirm('确定删除该优惠券吗？', '提示', { type: 'warning' })
  await deleteCoupon(info.id)
  ElMessage.success('删除成功')
  router.back()
}

onMounted(load)
</script>
