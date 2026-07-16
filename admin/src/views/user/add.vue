<template>
  <div class="add-container">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
      <span class="page-title">{{ isEdit ? '编辑用户' : '新增用户' }}</span>
    </div>

    <el-card v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" size="default">
        <el-divider content-position="left">基础信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="昵称">
              <el-input v-model="form.nickName" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="头像URL">
              <el-input v-model="form.avatar" placeholder="请输入头像URL">
                <template #append>
                  <el-button v-if="form.avatar" @click="previewAvatar = true">预览</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker v-model="form.birthDate" type="date" placeholder="请选择" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="form.nation" placeholder="请输入民族" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="form.nativePlace" placeholder="请输入籍贯" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="婚姻状况">
              <el-select v-model="form.marriage" placeholder="请选择" style="width:100%">
                <el-option label="未婚" value="未婚" />
                <el-option label="已婚" value="已婚" />
                <el-option label="离异" value="离异" />
                <el-option label="丧偶" value="丧偶" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文化程度">
              <el-select v-model="form.education" placeholder="请选择" style="width:100%">
                <el-option label="小学" value="小学" />
                <el-option label="初中" value="初中" />
                <el-option label="高中/中专" value="高中/中专" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职业">
              <el-input v-model="form.occupation" placeholder="请输入职业" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="工作单位">
              <el-input v-model="form.workUnit" placeholder="请输入工作单位" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="家庭住址">
              <el-input v-model="form.address" placeholder="请输入家庭住址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">健康信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身高(cm)">
              <el-input-number v-model="form.height" :min="50" :max="250" placeholder="cm" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="form.weight" :min="20" :max="300" placeholder="kg" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">紧急联系人</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急联系人">
              <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系电话">
              <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">其他信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户类型">
              <el-select v-model="form.role" placeholder="请选择用户类型" style="width:100%">
                <el-option label="老人" value="ELDER" />
                <el-option label="家属" value="FAMILY" />
                <el-option label="志愿者" value="VOLUNTEER" />
                <el-option label="工作人员" value="STAFF" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="简介">
              <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="请输入个人简介" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item v-if="!isEdit" label="初始密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请设置初始密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0"
                active-text="正常" inactive-text="禁用" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">提交</el-button>
          <el-button @click="$router.back()" size="large">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 头像预览 -->
    <el-dialog v-model="previewAvatar" title="头像预览" width="300px">
      <div style="text-align:center">
        <el-avatar :size="200" :src="form.avatar">{{ form.realName?.charAt(0) }}</el-avatar>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createAppUser, updateAppUser, getAppUser } from '@/api'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.query.id)
const editId = computed(() => route.query.id)

const loading = ref(false)
const submitting = ref(false)
const previewAvatar = ref(false)
const formRef = ref(null)

const form = reactive({
  realName: '',
  phone: '',
  nickName: '',
  avatar: '',
  gender: 1,
  birthDate: '',
  idCard: '',
  nation: '',
  nativePlace: '',
  marriage: '',
  education: '',
  occupation: '',
  workUnit: '',
  address: '',
  height: null,
  weight: null,
  emergencyContact: '',
  emergencyPhone: '',
  bio: '',
  remark: '',
  password: '',
  status: 1,
  role: 'FAMILY'
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 编辑时加载数据
onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const res = await getAppUser(editId.value)
      const data = res.data
      Object.assign(form, {
        realName: data.realName || '',
        phone: data.phone || '',
        nickName: data.nickName || '',
        avatar: data.avatar || '',
        gender: data.gender ?? 1,
        birthDate: data.birthDate || '',
        idCard: data.idCard || '',
        nation: data.nation || '',
        nativePlace: data.nativePlace || '',
        marriage: data.marriage || '',
        education: data.education || '',
        occupation: data.occupation || '',
        workUnit: data.workUnit || '',
        address: data.address || '',
        height: data.height || null,
        weight: data.weight || null,
        emergencyContact: data.emergencyContact || '',
        emergencyPhone: data.emergencyPhone || '',
        bio: data.bio || '',
        remark: data.remark || '',
        status: data.status ?? 1,
        role: data.role || 'FAMILY'
      })
    } catch (e) {
      ElMessage.error('加载用户信息失败')
    } finally {
      loading.value = false
    }
  }
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAppUser(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createAppUser(form)
      ElMessage.success('新增成功')
    }
    router.push({ name: 'AppUser' })
  } catch (e) {
    // error already handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.add-container {
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
}
</style>
