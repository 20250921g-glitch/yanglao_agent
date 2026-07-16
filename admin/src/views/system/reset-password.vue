<template>
  <div class="reset-wrap">
    <el-card shadow="never" class="reset-card">
      <h2 class="page-title">重置密码</h2>
      <p class="page-sub">修改当前登录账号的登录密码</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" @submit.prevent>
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="6-20 位，含字母与数字" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">确认修改</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { resetPassword } from '@/api/index'

const formRef = ref()
const submitting = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, cb) => {
  if (value !== form.newPassword) cb(new Error('两次输入的密码不一致'))
  else cb()
}
const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).{6,20}$/, message: '6-20 位，须含字母与数字', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

const submit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await resetPassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        reset()
      } else {
        ElMessage.error(res.message || '修改失败')
      }
    } catch (e) {
      ElMessage.error('后端接口暂未提供（/sys/user/reset-password），请联系开发接入')
    } finally {
      submitting.value = false
    }
  })
}
const reset = () => { formRef.value?.resetFields() }
</script>

<style scoped>
.reset-wrap { display: flex; justify-content: center; padding-top: 40px; }
.reset-card { width: 480px; }
.page-title { margin: 0 0 4px; font-size: 20px; font-weight: 600; color: #303133; }
.page-sub { margin: 0 0 20px; font-size: 13px; color: #A0AEC0; }
</style>
