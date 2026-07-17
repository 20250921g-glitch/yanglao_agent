<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <div class="brand">
        <div class="logo">智慧养老</div>
        <div class="sub">用户注册</div>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="短信验证码" prop="smsCode">
          <div class="captcha-row">
            <el-input v-model="form.smsCode" placeholder="请输入短信验证码" maxlength="6" />
            <el-button :disabled="countdown > 0" @click="sendSms">
              {{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="设置登录密码（6位以上）" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="选填，默认手机号用户" />
        </el-form-item>
        <el-form-item label="身份类型" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="ELDER">老人</el-radio>
            <el-radio value="FAMILY">家属</el-radio>
            <el-radio value="VOLUNTEER">志愿者</el-radio>
            <el-radio value="STAFF">工作人员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图形验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="请输入右侧字符" maxlength="4" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha-img" alt="captcha" @click="refreshCaptcha" title="点击刷新" />
          </div>
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="submit">注册</el-button>
        <div class="foot">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { appCaptcha, appSendSms, appRegister } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const captchaKey = ref('')
const captchaImage = ref('')
const countdown = ref(0)
let timer = null
const form = reactive({ phone: '', smsCode: '', password: '', confirm: '', nickname: '', role: 'FAMILY', captchaCode: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: (r, v, cb) => (v !== form.password ? cb(new Error('两次密码不一致')) : cb()), trigger: 'blur' }
  ],
  captchaCode: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  const res = await appCaptcha()
  captchaKey.value = res.data.captchaKey
  captchaImage.value = res.data.captchaImage
}

const sendSms = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请先填写正确的手机号')
    return
  }
  try {
    const res = await appSendSms(form.phone)
    const code = res.data.devCode
    if (code) {
      ElMessage.success('验证码已发送（开发模式）: ' + code)
    } else {
      ElMessage.success('验证码已发送，请注意查收短信')
    }
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) { clearInterval(timer); timer = null }
    }, 1000)
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  }
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await appRegister({
      phone: form.phone,
      password: form.password,
      nickname: form.nickname,
      role: form.role,
      smsCode: form.smsCode,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    })
    // 注册成功自动登录：写入 token 与用户信息，直接进入首页
    localStorage.setItem('app_token', res.data.token)
    localStorage.setItem('app_user', JSON.stringify(res.data.user))
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
