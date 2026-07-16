<template>
  <el-card>
    <template #header>
      <div class="card-header"><span>优惠券管理</span><el-button type="primary" @click="openDialog()">新增优惠券</el-button></div>
    </template>
    <el-form :inline="true" class="search-form">
      <el-form-item label="优惠券名"><el-input v-model="search.name" clearable style="width:160px" /></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="search.status" clearable style="width:100px">
          <el-option :value="1" label="生效中" /><el-option :value="0" label="已失效" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button><el-button @click="search.name=''; search.status=null; load()">重置</el-button></el-form-item>
    </el-form>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="优惠券名称" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }"><el-tag :type="row.type===1?'warning':'primary'" size="small">{{ row.type===1?'满减券':'折扣券' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="面额/折扣" width="110">
        <template #default="{ row }">{{ row.type===1 ? `¥${row.denomination}` : `${(row.denomination*10).toFixed(1)}折` }}</template>
      </el-table-column>
      <el-table-column prop="minAmount" label="使用门槛" width="110">
        <template #default="{ row }">{{ row.minAmount > 0 ? `满${row.minAmount}` : '无门槛' }}</template>
      </el-table-column>
      <el-table-column label="发放/剩余" width="110">
        <template #default="{ row }">{{ row.remainCount }}/{{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column label="有效期" width="220">
        <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'生效':'失效' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="210">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/user/coupon-detail/${row.id}`)">详情</el-button>
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link :type="row.status===1?'warning':'success'" @click="toggleStatus(row)">{{ row.status===1?'下架':'上架' }}</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </el-card>
  <el-dialog v-model="dialogVisible" :title="isEdit?'编辑优惠券':'新增优惠券'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="类型" prop="type">
        <el-radio-group v-model="form.type" @change="form.denomination = form.type===1 ? 20 : 0.9">
          <el-radio :label="1">满减券</el-radio>
          <el-radio :label="2">折扣券</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="form.type===1?'面额(元)':'折扣率'" prop="denomination">
        <el-input-number v-if="form.type===1" v-model="form.denomination" :min="0.01" :precision="2" style="width:100%" />
        <el-input-number v-else v-model="form.denomination" :min="0.01" :max="1" :precision="2" style="width:100%" />
      </el-form-item>
      <el-form-item label="使用门槛"><el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" placeholder="满XX可用，填0表示无门槛" /></el-form-item>
      <el-form-item label="发放总量"><el-input-number v-model="form.totalCount" :min="1" style="width:100%" /></el-form-item>
      <el-row :gutter="16">
        <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponPage, addCoupon, updateCoupon, deleteCoupon, updateCouponStatus } from '@/api'

const router = useRouter()
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const search = reactive({ name: '', status: null })
const dialogVisible = ref(false), isEdit = ref(false), formRef = ref(null)
const form = reactive({ id: null, name: '', type: 1, denomination: 20, minAmount: 0, totalCount: 100, remainCount: 100, startTime: '', endTime: '', status: 1 })
const rules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }], type: [{ required: true }], denomination: [{ required: true }], startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }], endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }] }

const load = async () => { loading.value = true; try { const res = await getCouponPage({ pageNum: pageNum.value, pageSize: pageSize.value, ...search }); tableData.value = res.data.records; total.value = res.data.total } catch (e) {} finally { loading.value = false } }
const openDialog = (row) => { row ? Object.assign(form, { ...row, remainCount: row.totalCount }) : (formRef.value?.resetFields(), Object.assign(form, { id: null, name: '', type: 1, denomination: 20, minAmount: 0, totalCount: 100, remainCount: 100, startTime: '', endTime: '', status: 1 })); isEdit.value = !!row; dialogVisible.value = true }
const handleSave = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; try { isEdit.value ? await updateCoupon(form) : await addCoupon(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() } catch (e) {} }
const toggleStatus = async (row) => { try { await updateCouponStatus(row.id, row.status === 1 ? 0 : 1); ElMessage.success(row.status === 1 ? '下架成功' : '上架成功'); load() } catch (e) {} }
const handleDelete = async (id) => { await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }); try { await deleteCoupon(id); ElMessage.success('删除成功'); load() } catch (e) {} }
onMounted(load)
</script>

<style scoped>
.card-header { display:flex; justify-content:space-between; align-items:center; }
.search-form { margin-bottom:16px; }
</style>
