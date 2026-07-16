<template>
  <div class="page-container">
    <el-card>
      <template #header><span class="page-title-bar">商品通用设置</span></template>

      <el-form ref="formRef" :model="form" label-width="140px" style="max-width:700px">

        <!-- 基础设置 -->
        <div class="form-section-title">基础设置</div>
        <el-form-item label="默认商品状态">
          <el-select v-model="form.defaultStatus" style="width:100%">
            <el-option label="待审核" :value="0" />
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认销售模式">
          <el-radio-group v-model="form.saleMode">
            <el-radio label="线上预约">线上预约</el-radio>
            <el-radio label="即时购买">即时购买</el-radio>
            <el-radio label="混合模式">混合模式</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="自动下架天数">
          <el-input-number v-model="form.autoDelistDays" :min="0" :max="365" style="width:200px" />
          <span style="margin-left:8px;color:#A0AEC0;font-size:13px">天（0表示不自动下架）</span>
        </el-form-item>

        <el-divider />

        <!-- 审核设置 -->
        <div class="form-section-title">审核设置</div>
        <el-form-item label="是否需要审核">
          <el-switch v-model="form.needAudit" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="审核人">
          <el-input v-model="form.auditor" placeholder="请输入审核人姓名" :disabled="!form.needAudit" style="width:200px" />
        </el-form-item>

        <el-divider />

        <!-- 库存设置 -->
        <div class="form-section-title">库存设置</div>
        <el-form-item label="低库存预警阈值">
          <el-input-number v-model="form.lowStockThreshold" :min="0" style="width:200px" />
          <span style="margin-left:8px;color:#A0AEC0;font-size:13px">件</span>
        </el-form-item>
        <el-form-item label="是否允许超卖">
          <el-switch v-model="form.allowOversell" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-divider />

        <!-- 售后设置 -->
        <div class="form-section-title">售后设置</div>
        <el-form-item label="默认售后期限">
          <el-input-number v-model="form.afterSaleDays" :min="0" :max="365" style="width:200px" />
          <span style="margin-left:8px;color:#A0AEC0;font-size:13px">天</span>
        </el-form-item>
        <el-form-item label="七天无理由退货">
          <el-switch v-model="form.allowReturn" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <!-- 保存按钮 -->
        <div class="form-actions">
          <el-button @click="reset">重置</el-button>
          <el-button type="primary" @click="save" :loading="submitting">保存设置</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductSettings, saveProductSettings } from '@/api'

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  defaultStatus: 0,
  saleMode: '线上预约',
  autoDelistDays: 30,
  needAudit: 1,
  auditor: '管理员',
  lowStockThreshold: 10,
  allowOversell: 0,
  afterSaleDays: 7,
  allowReturn: 1
})

const loadSettings = async () => {
  try {
    const res = await getProductSettings()
    const settings = res.data || {}
    if (settings.defaultStatus !== undefined) form.defaultStatus = parseInt(settings.defaultStatus) || 0
    if (settings.saleMode) form.saleMode = settings.saleMode
    if (settings.autoDelistDays !== undefined) form.autoDelistDays = parseInt(settings.autoDelistDays) || 30
    if (settings.needAudit !== undefined) form.needAudit = parseInt(settings.needAudit) || 1
    if (settings.auditor) form.auditor = settings.auditor
    if (settings.lowStockThreshold !== undefined) form.lowStockThreshold = parseInt(settings.lowStockThreshold) || 10
    if (settings.allowOversell !== undefined) form.allowOversell = parseInt(settings.allowOversell) || 0
    if (settings.afterSaleDays !== undefined) form.afterSaleDays = parseInt(settings.afterSaleDays) || 7
    if (settings.allowReturn !== undefined) form.allowReturn = parseInt(settings.allowReturn) || 1
  } catch (e) {
    ElMessage.error('加载设置失败')
  }
}

const save = async () => {
  submitting.value = true
  try {
    const settingsData = [
      { settingKey: 'defaultStatus', settingValue: String(form.defaultStatus) },
      { settingKey: 'saleMode', settingValue: form.saleMode },
      { settingKey: 'autoDelistDays', settingValue: String(form.autoDelistDays) },
      { settingKey: 'needAudit', settingValue: String(form.needAudit) },
      { settingKey: 'auditor', settingValue: form.auditor },
      { settingKey: 'lowStockThreshold', settingValue: String(form.lowStockThreshold) },
      { settingKey: 'allowOversell', settingValue: String(form.allowOversell) },
      { settingKey: 'afterSaleDays', settingValue: String(form.afterSaleDays) },
      { settingKey: 'allowReturn', settingValue: String(form.allowReturn) }
    ]
    await saveProductSettings(settingsData)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const reset = () => {
  form.defaultStatus = 0
  form.saleMode = '线上预约'
  form.autoDelistDays = 30
  form.needAudit = 1
  form.auditor = '管理员'
  form.lowStockThreshold = 10
  form.allowOversell = 0
  form.afterSaleDays = 7
  form.allowReturn = 1
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.page-title-bar {
  font-size: 16px;
  font-weight: 600;
}
.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #00C4A1;
  margin: 8px 0 16px;
  padding-left: 10px;
  border-left: 3px solid #00C4A1;
}
.form-actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
