<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <el-button @click="$router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <span class="page-title">新增服务人员</span>
    </div>

    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <!-- 基础信息 -->
        <div class="form-section-title">基础信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务类型" prop="serviceType">
              <el-select v-model="form.serviceType" placeholder="请选择服务类型" style="width:100%">
                <el-option label="家政护工" value="家政护工" />
                <el-option label="康复理疗" value="康复理疗" />
                <el-option label="上门体检" value="上门体检" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="头像URL">
              <el-input v-model="form.avatar" placeholder="请输入头像图片URL" />
              <div v-if="form.avatar" style="margin-top:8px">
                <el-avatar :size="60" :src="form.avatar" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="个人简介">
              <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入个人简介" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责区域">
              <el-input v-model="form.region" placeholder="请输入负责区域" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标签">
              <el-select v-model="form.tags" multiple placeholder="请选择标签" style="width:100%" allow-create filterable>
                <el-option label="金牌" value="金牌" />
                <el-option label="银牌" value="银牌" />
                <el-option label="资深" value="资深" />
                <el-option label="新人" value="新人" />
                <el-option label="好评优先" value="好评优先" />
                <el-option label="快速响应" value="快速响应" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 实名信息 -->
        <div class="form-section-title">实名信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行卡号">
              <el-input v-model="form.bankCard" placeholder="请输入银行卡号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户行">
              <el-input v-model="form.bankName" placeholder="请输入开户行" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="职业证书">
              <el-input v-model="form.certificate" placeholder="请输入职业证书编号或URL" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 服务设置 -->
        <div class="form-section-title">服务设置</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许打赏">
              <el-switch v-model="form.allowTip" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="初始密码">
              <el-input v-model="form.password" type="password" placeholder="请输入初始密码（可选）" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" @click="submit" :loading="submitting">提交</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createServiceWorker } from '@/api'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  name: '',
  phone: '',
  gender: 1,
  avatar: '',
  intro: '',
  serviceType: '',
  tags: [],
  region: '',
  idCard: '',
  bankCard: '',
  bankName: '',
  certificate: '',
  allowTip: 1,
  status: 1,
  password: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }]
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = { ...form }
    if (!payload.password) delete payload.password
    await createServiceWorker(payload)
    ElMessage.success('新增成功')
    router.push('/service/worker')
  } catch (e) {} finally {
    submitting.value = false
  }
}
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
