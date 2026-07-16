<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>商品列表</span>
        <el-button type="primary" @click="router.push('/product/add')">新增商品</el-button>
      </div>
    </template>

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="商品名称">
        <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable style="width:150px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width:180px">
          <el-option-group v-for="group in categoryTree" :key="group.label" :label="group.label">
            <el-option v-for="item in group.children" :key="item.id" :label="item.name" :value="item.id" />
          </el-option-group>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width:130px">
          <el-option label="待审核" :value="0" /><el-option label="已上架" :value="1" />
          <el-option label="审核未通过" :value="2" /><el-option label="已下架" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="价格">
        <el-input v-model="searchForm.priceMin" placeholder="最低" style="width:90px" /><span style="margin:0 6px">-</span>
        <el-input v-model="searchForm.priceMax" placeholder="最高" style="width:90px" />
      </el-form-item>
      <el-form-item label="更新日期">
        <el-date-picker v-model="searchForm.dateRange" type="daterange" unlink-panels
          range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width:240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 批量操作 -->
    <div class="batch-bar" v-if="selection.length">
      <span class="sel-tip">已选 {{ selection.length }} 项</span>
      <el-button type="danger" plain size="small" @click="batchDelete">批量删除</el-button>
    </div>

    <el-table :data="pagedData" stripe v-loading="loading" @selection-change="onSelect">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="code" label="商品编码" width="120" />
      <el-table-column label="商品信息" min-width="260">
        <template #default="{ row }">
          <div class="goods-cell">
            <el-image :src="row.image" fit="cover" class="goods-img" v-if="row.image">
              <template #error><div class="goods-img goods-img--ph"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div v-else class="goods-img goods-img--ph"><el-icon><Picture /></el-icon></div>
            <div class="goods-meta">
              <div class="goods-name">{{ row.name }}</div>
              <div class="goods-desc">{{ row.description || '暂无描述' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="120">
        <template #default="{ row }"><el-tag size="small">{{ row.categoryName || getCategoryName(row.categoryId) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="价格" width="100">
        <template #default="{ row }"><span style="color:#FF4D4F">¥{{ row.price }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后更新时间" width="170" />
      <el-table-column label="最后更新人" width="120">
        <template #default="{ row }">{{ row.updateBy || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="copyProduct(row)">复制</el-button>
          <el-button link type="warning" size="small" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button link type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="filteredTotal"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑商品' : '新增商品'" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="商品名称" prop="name"><el-input v-model="form.name" placeholder="请输入商品名称" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="商品编码" prop="code"><el-input v-model="form.code" placeholder="请输入商品编码" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:100%" @change="onCategoryChange">
                <el-option-group v-for="group in categoryTree" :key="group.label" :label="group.label">
                  <el-option v-for="item in group.children" :key="item.id" :label="item.name" :value="item.id" />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务类型" prop="serviceType">
              <el-select v-model="form.serviceType" placeholder="请选择服务类型" style="width:100%">
                <el-option label="家政护理" value="家政护理" /><el-option label="康复理疗" value="康复理疗" /><el-option label="上门体检" value="上门体检" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="售价" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单位"><el-input v-model="form.unit" placeholder="次/小时/套" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="推荐"><el-switch v-model="form.recommend" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="商品描述"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入商品描述" /></el-form-item>
        <el-form-item label="商品详情"><el-input v-model="form.detail" type="textarea" :rows="3" placeholder="请输入商品详情" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductPage, getProduct, addProduct, updateProduct, deleteProduct, getProductCategoryList } from '@/api'

const router = useRouter()

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const rawData = ref([]), selection = ref([]), categoryList = ref([])
const dialogVisible = ref(false), editId = ref(null), formRef = ref(null)
const searchForm = reactive({ name: '', categoryId: '', status: '', priceMin: '', priceMax: '', dateRange: null })
const form = reactive({ name: '', code: '', categoryId: '', serviceType: '', price: 0, originalPrice: 0, unit: '', description: '', detail: '', sort: 0, recommend: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await getProductPage({ pageNum: pageNum.value, pageSize: pageSize.value, name: searchForm.name || null, categoryId: searchForm.categoryId || null })
    rawData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {}
  finally { loading.value = false }
}
const loadCategoryList = async () => {
  try { const res = await getProductCategoryList(); categoryList.value = res.data || [] } catch (e) {}
}
const categoryTree = computed(() => {
  const types = ['家政护理', '康复理疗', '上门体检']
  return types.map(type => ({ label: type, children: categoryList.value.filter(c => c.serviceType === type) })).filter(g => g.children.length > 0)
})
const getCategoryName = (categoryId) => { const c = categoryList.value.find(x => x.id === categoryId); return c ? c.name : '-' }
const onCategoryChange = (categoryId) => { const c = categoryList.value.find(x => x.id === categoryId); if (c) form.serviceType = c.serviceType }

const filteredData = computed(() => {
  let list = rawData.value
  if (searchForm.status !== '') list = list.filter(r => r.status === searchForm.status)
  if (searchForm.priceMin !== '') list = list.filter(r => Number(r.price) >= Number(searchForm.priceMin))
  if (searchForm.priceMax !== '') list = list.filter(r => Number(r.price) <= Number(searchForm.priceMax))
  const range = searchForm.dateRange
  if (range && range.length === 2) {
    const start = new Date(range[0]).setHours(0, 0, 0, 0)
    const end = new Date(range[1]).setHours(23, 59, 59, 999)
    list = list.filter(r => { const t = new Date(r.updateTime).getTime(); return t >= start && t <= end })
  }
  return list
})
const filteredTotal = computed(() => (searchForm.status !== '' || searchForm.priceMin !== '' || searchForm.priceMax !== '' || searchForm.dateRange) ? filteredData.value.length : total.value)
const pagedData = computed(() => {
  if (searchForm.status !== '' || searchForm.priceMin !== '' || searchForm.priceMax !== '' || searchForm.dateRange) {
    const s = (pageNum.value - 1) * pageSize.value
    return filteredData.value.slice(s, s + pageSize.value)
  }
  return filteredData.value
})

const statusLabel = (s) => ({ 0: '待审核', 1: '已上架', 2: '审核未通过', 3: '已下架' }[s] ?? (s === 1 ? '已上架' : '已下架'))
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[s] ?? 'info')

const resetSearch = () => { Object.assign(searchForm, { name: '', categoryId: '', status: '', priceMin: '', priceMax: '', dateRange: null }); pageNum.value = 1; load() }
const onSelect = (rows) => { selection.value = rows }
const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await updateProduct({ id: row.id, status: next }); ElMessage.success(next === 1 ? '已上架' : '已下架'); load()
}
const del = async (row) => {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning' })
  await deleteProduct(row.id); ElMessage.success('删除成功'); load()
}
const batchDelete = async () => {
  await ElMessageBox.confirm(`确认删除选中的 ${selection.value.length} 个商品？`, '提示', { type: 'warning' })
  try { for (const row of selection.value) await deleteProduct(row.id); ElMessage.success('批量删除成功'); load() } catch (e) {}
}
const copyProduct = async (row) => {
  const data = {
    name: row.name + ' 副本', code: 'P' + Date.now(), categoryId: row.categoryId, serviceType: row.serviceType,
    price: row.price, originalPrice: row.originalPrice, unit: row.unit, description: row.description,
    detail: row.detail, sort: row.sort || 0, recommend: 0, status: 0
  }
  try { await addProduct(data); ElMessage.success('已复制为新商品'); load() } catch (e) {}
}
const openDialog = async (row) => {
  editId.value = row?.id || null
  if (editId.value) { const res = await getProduct(editId.value); Object.assign(form, res.data) }
  else Object.assign(form, { name: '', code: '', categoryId: '', serviceType: '', price: 0, originalPrice: 0, unit: '', description: '', detail: '', sort: 0, recommend: 0, status: 1 })
  dialogVisible.value = true
}
const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  try {
    if (editId.value) { await updateProduct({ ...form, id: editId.value }); ElMessage.success('修改成功') }
    else { await addProduct(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; load()
  } catch (e) {}
}

onMounted(() => { load(); loadCategoryList() })
</script>

<style scoped>
.goods-cell { display:flex; align-items:center; gap:10px; }
.goods-img { width:50px; height:50px; border-radius:6px; flex:0 0 auto; }
.goods-img--ph { display:flex; align-items:center; justify-content:center; background:#f4f6f8; color:#bbb; font-size:20px; }
.goods-name { font-weight:500; color:#303133; }
.goods-desc { color:#A0AEC0; font-size:12px; margin-top:2px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:200px; }
.batch-bar { margin-bottom:12px; display:flex; align-items:center; gap:10px; }
.sel-tip { font-size:13px; color:#606266; }
</style>
