<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <div class="brand">
        <div class="logo">智慧养老</div>
        <div class="sub">用户端登录</div>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="submit" />
        </el-form-item>
        <el-form-item label="图形验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="请输入右侧字符" maxlength="4" @keyup.enter="submit" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha-img" alt="captcha" @click="refreshCaptcha" title="点击刷新" />
          </div>
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="submit">登录</el-button>
        <div class="foot">
          <span>还没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { appCaptcha, appLogin } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const captchaKey = ref('')
const captchaImage = ref('')
const form = reactive({ phone: '', password: '', captchaCode: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  const res = await appCaptcha()
  captchaKey.value = res.data.captchaKey
  captchaImage.value = res.data.captchaImage
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await appLogin({
      phone: form.phone,
      password: form.password,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    })
    localStorage.setItem('app_token', res.data.token)
    localStorage.setItem('app_user', JSON.stringify(res.data.user))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
</script>
