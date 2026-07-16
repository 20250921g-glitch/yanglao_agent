<template>
  <div class="profile-page">
    <el-card class="profile-card" shadow="never">
      <div class="header">
        <div class="title">个人中心</div>
        <el-button type="primary" :icon="Edit" @click="openEdit" :loading="saving">编辑资料</el-button>
      </div>

      <el-descriptions :column="1" border v-loading="loading">
        <el-descriptions-item label="用户ID">{{ user ? user.id : '' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user ? user.username : '' }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ user && user.realName ? user.realName : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ user ? user.phone : '' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ user && user.birthDate ? user.birthDate : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ user && user.idCard ? user.idCard : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ user && user.nation ? user.nation : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="籍贯">{{ user && user.nativePlace ? user.nativePlace : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="婚姻状况">{{ user && user.marriage ? user.marriage : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ user && user.education ? user.education : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="职业">{{ user && user.occupation ? user.occupation : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="工作单位">{{ user && user.workUnit ? user.workUnit : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="身高(cm)">{{ user && user.height != null ? user.height : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="体重(kg)">{{ user && user.weight != null ? user.weight : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系地址">{{ user && user.address ? user.address : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ user && user.emergencyContact ? user.emergencyContact : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系电话">{{ user && user.emergencyPhone ? user.emergencyPhone : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="个人简介">{{ user && user.bio ? user.bio : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="注册来源">{{ user && user.source ? user.source : '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ user && user.status === 1 ? '正常' : '禁用' }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ user && user.points != null ? user.points : 0 }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">{{ user && user.levelName ? user.levelName : '普通会员' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ user ? user.createTime : '' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editVisible" title="编辑个人资料" width="560px" :close-on-click-modal="false">
      <el-form :model="form" label-width="110px" v-loading="saving">
        <el-form-item label="昵称">
          <el-input v-model="form.username" placeholder="请输入昵称" maxlength="30" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="form.avatar" placeholder="请输入头像图片地址" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="30" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
            <el-option label="女" :value="0" />
            <el-option label="男" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthDate" type="date" placeholder="选择生日"
            value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>
        <el-form-item label="民族">
          <el-input v-model="form.nation" placeholder="如：汉" maxlength="20" />
        </el-form-item>
        <el-form-item label="籍贯">
          <el-input v-model="form.nativePlace" placeholder="如：山东济南" maxlength="50" />
        </el-form-item>
        <el-form-item label="婚姻状况">
          <el-input v-model="form.marriage" placeholder="如：已婚/未婚" maxlength="20" />
        </el-form-item>
        <el-form-item label="学历">
          <el-input v-model="form.education" placeholder="如：本科" maxlength="30" />
        </el-form-item>
        <el-form-item label="职业">
          <el-input v-model="form.occupation" placeholder="如：退休" maxlength="30" />
        </el-form-item>
        <el-form-item label="工作单位">
          <el-input v-model="form.workUnit" placeholder="请输入工作单位" maxlength="60" />
        </el-form-item>
        <el-form-item label="身高(cm)">
          <el-input-number v-model="form.height" :min="0" :max="300" :precision="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :min="0" :max="500" :precision="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系地址">
          <el-input v-model="form.address" placeholder="请输入联系地址" maxlength="120" />
        </el-form-item>
        <el-form-item label="紧急联系人">
          <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人姓名" maxlength="30" />
        </el-form-item>
        <el-form-item label="紧急电话">
          <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="请输入个人简介" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { currentUser, fetchUser } from '@/store/user'
import { appUpdateUser } from '@/api'

const loading = ref(false)
const saving = ref(false)
const editVisible = ref(false)
const user = currentUser

const genderText = computed(() => {
  if (!user.value || user.value.gender == null) return '未填写'
  return user.value.gender === 1 ? '男' : '女'
})

const emptyForm = () => ({
  username: '', avatar: '', realName: '', gender: null, birthDate: '',
  idCard: '', nation: '', nativePlace: '', marriage: '', education: '',
  occupation: '', workUnit: '', height: null, weight: null, address: '',
  emergencyContact: '', emergencyPhone: '', bio: ''
})
const form = reactive(emptyForm())

const openEdit = () => {
  const u = user.value || {}
  Object.assign(form, emptyForm(), {
    username: u.username || '', avatar: u.avatar || '', realName: u.realName || '',
    gender: u.gender ?? null, birthDate: u.birthDate || '', idCard: u.idCard || '',
    nation: u.nation || '', nativePlace: u.nativePlace || '', marriage: u.marriage || '',
    education: u.education || '', occupation: u.occupation || '', workUnit: u.workUnit || '',
    height: u.height != null ? Number(u.height) : null,
    weight: u.weight != null ? Number(u.weight) : null,
    address: u.address || '', emergencyContact: u.emergencyContact || '',
    emergencyPhone: u.emergencyPhone || '', bio: u.bio || ''
  })
  editVisible.value = true
}

const submit = async () => {
  saving.value = true
  try {
    await appUpdateUser({ ...form })
    await fetchUser(true) // 刷新全局当前用户，保证两端数据一致
    ElMessage.success('资料已更新')
    editVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const load = async () => {
  loading.value = true
  try {
    await fetchUser(true)
  } catch (e) {
    ElMessage.error(e.message || '获取信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.profile-page { padding: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 18px; font-weight: 600; color: #00C4A1; }
</style>
