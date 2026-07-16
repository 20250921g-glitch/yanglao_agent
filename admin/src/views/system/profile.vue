<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span>个人资料</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:520px">
            <el-form-item label="账号"><el-input v-model="form.username" disabled /></el-form-item>
            <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" maxlength="11" />
            </el-form-item>
            <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
            <el-form-item label="头像URL"><el-input v-model="form.avatar" placeholder="请输入头像URL" /></el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>重置密码</span></template>
          <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-width="100px" style="max-width:520px">
            <el-form-item label="原密码" prop="oldPwd">
              <el-input v-model="pwd.oldPwd" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPwd">
              <el-input v-model="pwd.newPwd" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input v-model="pwd.confirmPwd" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api'

const formRef = ref(null), pwdRef = ref(null)
const form = reactive({ username: '', realName: '', phone: '', email: '', avatar: '' })
const pwd = reactive({ oldPwd: '', newPwd: '', confirmPwd: '' })

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }]
}

const pwdRules = {
  oldPwd: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPwd: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPwd: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, val, cb) => val === pwd.newPwd ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ]
}

const load = async () => {
  try {
    const res = await getUserInfo()
    Object.assign(form, res.data || {})
  } catch (e) {}
}

const saveProfile = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  ElMessage.success('保存成功（演示）')
  // 实际项目调 updateSysUser(form)
}

const changePwd = async () => {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return
  ElMessage.success('密码修改成功（演示）')
  pwd.oldPwd = ''; pwd.newPwd = ''; pwd.confirmPwd = ''
}

onMounted(load)
</script>
