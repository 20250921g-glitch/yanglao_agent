<template>
  <div class="login-container">
    <!-- 左侧品牌区 -->
    <div class="login-left">
      <div class="left-content">
        <h1 class="brand-title">智慧养老后台管理系统</h1>
        <img class="brand-illustration" src="/images/login-illustration.png" alt="智慧养老" />
        <p class="brand-subtitle">智能健康信息及服务管理；</p>
        <p class="brand-subtitle">实现资源的优化配置和管理，降低运营成本。</p>
      </div>
    </div>

    <!-- 右侧登录卡片 -->
    <div class="login-right">
      <div class="login-card">
        <!-- 右上角设置/更多图标（原型 u878/u880） -->
        <div class="card-extra-btn" title="设置">
          <el-icon :size="18"><Setting /></el-icon>
        </div>

        <div class="login-header">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-tip">忘记密码请联系管理员</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入手机号"
              :prefix-icon="Iphone"
              size="large"
              class="login-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              class="login-input"
            />
          </el-form-item>

          <el-form-item class="agreement-item">
            <el-checkbox v-model="agreement" size="large">
              <span class="agreement-text">我已阅读并同意</span>
              <a class="agreement-link" @click.stop.prevent="showPrivacy = true">《用户隐私政策》</a>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 隐私政策弹窗 -->
    <el-dialog v-model="showPrivacy" title="用户隐私政策" width="560px" :close-on-click-modal="true">
      <div class="privacy-content">
        <p>欢迎使用智慧养老后台管理系统。</p>
        <p>我们重视您的个人信息和隐私保护。本政策说明我们如何收集、使用、存储和保护您的信息：</p>
        <ul>
          <li>为提供后台管理服务，我们会收集您的手机号、密码及操作日志。</li>
          <li>您的信息将严格用于系统身份认证、权限管理及安全审计。</li>
          <li>我们采取加密存储、访问控制等技术措施保护数据安全。</li>
          <li>未经您授权，我们不会向第三方披露您的个人信息。</li>
        </ul>
        <p>继续使用本系统即表示您已阅读并同意本隐私政策。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="showPrivacy = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, Setting } from '@element-plus/icons-vue'
import { login, getUserPerms } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const agreement = ref(false)
const showPrivacy = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!agreement.value) {
    ElMessage.warning('请阅读并同意用户隐私政策')
    return
  }

  loading.value = true
  try {
    const res = await login(form)
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.user)
    userStore.setRoles(res.data.roles || [])
    // 拉取按钮级权限码
    try {
      const permsRes = await getUserPerms(res.data.user.id)
      userStore.setPerms(permsRes.data || [])
    } catch (e) {
      userStore.setPerms([])
    }
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  background: #f5f7fa;
}

/* 左侧品牌区 */
.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f9f5 0%, #d4f0ea 100%);
  position: relative;
  overflow: hidden;
  min-width: 420px;
}

.login-left::before,
.login-left::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  opacity: 0.35;
}

.login-left::before {
  top: -80px;
  right: -60px;
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, rgba(0, 196, 161, 0.25) 0%, transparent 70%);
}

.login-left::after {
  bottom: -60px;
  left: -40px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(0, 196, 161, 0.22) 0%, transparent 70%);
}

.left-content {
  text-align: center;
  position: relative;
  z-index: 1;
  padding: 0 60px;
  max-width: 560px;
}

.brand-title {
  font-size: 32px;
  color: #1f3d36;
  margin: 0 0 28px;
  font-weight: 600;
  letter-spacing: 1px;
}

.brand-illustration {
  width: 100%;
  max-width: 420px;
  height: auto;
  margin-bottom: 24px;
}

.brand-subtitle {
  font-size: 15px;
  color: #5a8d82;
  margin: 6px 0;
  line-height: 1.6;
}

/* 右侧登录卡片 */
.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
  position: relative;
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 44px 36px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 196, 161, 0.08);
  position: relative;
}

.card-extra-btn {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  background: #f0f9f7;
  cursor: pointer;
  transition: all 0.2s;
}

.card-extra-btn:hover {
  background: var(--primary-light);
  transform: rotate(30deg);
}

.login-header {
  margin-bottom: 30px;
}

.login-title {
  font-size: 24px;
  color: #1f3d36;
  margin: 0 0 10px;
  font-weight: 600;
}

.login-tip {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.login-form {
  margin-top: 8px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e0e6e4 inset;
  padding: 0 12px;
}

.login-input :deep(.el-input__inner) {
  height: 44px;
}

.login-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.agreement-item {
  margin-bottom: 18px;
}

.agreement-text {
  color: #999;
  font-size: 13px;
}

.agreement-link {
  color: var(--primary-color);
  font-size: 13px;
  cursor: pointer;
  text-decoration: none;
}

.agreement-link:hover {
  color: var(--primary-dark);
  text-decoration: underline;
}

.login-button {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 15px;
  letter-spacing: 2px;
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.login-button:hover,
.login-button:focus {
  background-color: var(--primary-dark);
  border-color: var(--primary-dark);
}

.privacy-content {
  color: #4a5568;
  line-height: 1.8;
  font-size: 14px;
}

.privacy-content p {
  margin: 0 0 12px;
}

.privacy-content ul {
  padding-left: 20px;
  margin: 0 0 12px;
}

.privacy-content li {
  margin-bottom: 8px;
}

/* 响应式：小屏时上下布局 */
@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
  }

  .login-left {
    min-height: auto;
    padding: 40px 24px;
    min-width: auto;
  }

  .left-content {
    padding: 0;
  }

  .brand-title {
    font-size: 24px;
    margin-bottom: 20px;
  }

  .brand-illustration {
    max-width: 260px;
  }

  .login-right {
    width: 100%;
    padding: 32px 20px;
  }

  .login-card {
    box-shadow: none;
    padding: 0;
  }
}
</style>
