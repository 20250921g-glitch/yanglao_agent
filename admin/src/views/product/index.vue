<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>商品列表</span>
        <el-button type="primary" v-has="'product:add'" @click="openDialog()">新增商品</el-button>
      </div>
    </template>

    <el-alert v-if="lockedServiceType" :title="`当前服务类型：${lockedServiceType}（仅显示该类商品）`" type="info" :closable="false" show-icon style="margin-bottom:16px" />

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom:16px">
      <el-form-item label="商品名称">
        <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="选择日期">
        <el-date-picker v-model="searchForm.dateRange" type="daterange" unlink-panels
          range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width:240px" />
      </el-form-item>
      <el-form-item label="价格">
        <el-input-number v-model="searchForm.priceMin" :min="0" :precision="2" controls-position="right" placeholder="最低" style="width:120px" />
        <span style="margin:0 6px">-</span>
        <el-input-number v-model="searchForm.priceMax" :min="0" :precision="2" controls-position="right" placeholder="最高" style="width:120px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width:140px">
          <el-option label="待审核" :value="0" />
          <el-option label="已上架" :value="1" />
          <el-option label="审核未通过" :value="2" />
          <el-option label="已下架" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品分类">
        <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width:200px">
          <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tableRef" :data="pagedData" stripe v-loading="loading" @selection-change="onSelect">
      <template #empty><el-empty description="暂无数据" /></template>
      <el-table-column type="selection" width="48" />
      <el-table-column prop="code" label="商品编码" width="130" />
      <el-table-column label="商品信息" min-width="260">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:10px">
            <el-image :src="row.image" :preview-src-list="row.image ? [row.image] : []" fit="cover"
              style="width:48px;height:48px;border-radius:6px;flex:none" />
            <div style="min-width:0">
              <div style="font-weight:600;color:#303133">{{ row.name }}</div>
              <div class="muted ellipsis">{{ row.description || '—' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ row.categoryName || getCategoryName(row.categoryId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120">
        <template #default="{ row }">
          <span style="color:#FF4D4F">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag size="small" :style="STATUS_MAP[row.status]?.style">{{ STATUS_MAP[row.status]?.text || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后更新时间" width="170" />
      <el-table-column label="最后更新人" width="120">
        <template #default="{ row }">
          <span>{{ row.updateBy || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" v-has="'product:edit'" @click="openDialog(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="copyRow(row)">复制</el-button>
          <el-button link type="danger" size="small" v-has="'product:delete'" @click="del(row)">删除</el-button>
          <el-button link type="warning" size="small" @click="toggleShelf(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="displayTotal"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />

    <!-- 批量操作栏 -->
    <div v-if="selection.length" class="batch-bar">
      <span class="batch-tip">已选 {{ selection.length }} 项</span>
      <el-button type="danger" size="small" v-has="'product:delete'" @click="batchDelete">批量删除</el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>
  </el-card>

  <!-- 新增/编辑对话框 -->
  <el-dialog v-model="dialogVisible" :title="editId ? '编辑商品' : '新增商品'" width="700px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入商品名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="商品编码" prop="code">
            <el-input v-model="form.code" placeholder="请输入商品编码" />
          </el-form-item>
        </el-col>
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
              <el-option label="家政护理" value="家政护理" />
              <el-option label="康复理疗" value="康复理疗" />
              <el-option label="上门体检" value="上门体检" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="售价" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="原价">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="单位">
            <el-input v-model="form.unit" placeholder="次/小时/套" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="推荐">
            <el-switch v-model="form.recommend" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="商品描述">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入商品描述" />
      </el-form-item>
      <el-form-item label="商品详情">
        <el-input v-model="form.detail" type="textarea" :rows="3" placeholder="请输入商品详情" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductPage, getProduct, addProduct, updateProduct, deleteProduct,
         getProductCategoryList } from '@/api'

const route = useRoute()
const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const dialogVisible = ref(false), editId = ref(null), formRef = ref(null), tableRef = ref(null)
const selection = ref([])
const categoryList = ref([])
const lockedServiceType = ref(route.query.serviceType || '')

const searchForm = reactive({ name: '', dateRange: null, priceMin: null, priceMax: null, status: '', categoryId: '' })
const form = reactive({
  name: '', code: '', categoryId: '', serviceType: '', price: 0, originalPrice: 0,
  unit: '', description: '', detail: '', sort: 0, recommend: 0, status: 1
})
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

// 商品状态枚举（0待审核 / 1已上架 / 2审核未通过 / 3已下架）
const STATUS_MAP = {
  0: { text: '待审核', style: { color: '#FAAD14', backgroundColor: 'rgba(250,173,20,.12)', borderColor: 'transparent' } },
  1: { text: '已上架', style: { color: '#00C4A1', backgroundColor: 'rgba(0,196,161,.12)', borderColor: 'transparent' } },
  2: { text: '审核未通过', style: { color: '#FF4D4F', backgroundColor: 'rgba(255,77,79,.12)', borderColor: 'transparent' } },
  3: { text: '已下架', style: { color: '#909399', backgroundColor: 'rgba(144,147,153,.12)', borderColor: 'transparent' } }
}

// 分类树形结构（编辑对话框使用）
const categoryTree = computed(() => {
  const types = ['家政护理', '康复理疗', '上门体检']
  return types.map(type => ({
    label: type,
    children: categoryList.value.filter(c => c.serviceType === type)
  })).filter(g => g.children.length > 0)
})

// 筛选用分类下拉（按当前服务类型过滤）
const categoryOptions = computed(() => {
  const list = lockedServiceType.value ? categoryList.value.filter(c => c.serviceType === lockedServiceType.value) : categoryList.value
  return list
})

// 分类名称映射
const getCategoryName = (categoryId) => {
  const cat = categoryList.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

// 选择分类时自动填充服务类型
const onCategoryChange = (categoryId) => {
  const cat = categoryList.value.find(c => c.id === categoryId)
  if (cat) {
    form.serviceType = cat.serviceType
  }
}

const load = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, name: searchForm.name || null, categoryId: searchForm.categoryId || null }
    if (lockedServiceType.value) params.serviceType = lockedServiceType.value
    const res = await getProductPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {}
  finally { loading.value = false }
}

// 日期/价格/状态为前端筛选（后端 /product/page 仅支持 name/categoryId/serviceType）
const filteredData = computed(() => {
  let list = tableData.value
  const range = searchForm.dateRange
  if (range && range.length === 2) {
    const start = new Date(range[0]).setHours(0, 0, 0, 0)
    const end = new Date(range[1]).setHours(23, 59, 59, 999)
    list = list.filter(r => { const t = new Date(r.updateTime).getTime(); return t >= start && t <= end })
  }
  if (searchForm.priceMin != null && searchForm.priceMin !== '') {
    list = list.filter(r => Number(r.price) >= Number(searchForm.priceMin))
  }
  if (searchForm.priceMax != null && searchForm.priceMax !== '') {
    list = list.filter(r => Number(r.price) <= Number(searchForm.priceMax))
  }
  if (searchForm.status !== '' && searchForm.status != null) {
    list = list.filter(r => r.status === searchForm.status)
  }
  return list
})
const useClientFilter = computed(() =>
  !!(searchForm.dateRange ||
    (searchForm.priceMin != null && searchForm.priceMin !== '') ||
    (searchForm.priceMax != null && searchForm.priceMax !== '') ||
    (searchForm.status !== '' && searchForm.status != null))
)
const pagedData = computed(() => {
  if (useClientFilter.value) {
    const s = (pageNum.value - 1) * pageSize.value
    return filteredData.value.slice(s, s + pageSize.value)
  }
  return tableData.value
})
const displayTotal = computed(() => useClientFilter.value ? filteredData.value.length : total.value)

const loadCategoryList = async () => {
  try {
    const res = await getProductCategoryList()
    categoryList.value = res.data || []
  } catch (e) {}
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.dateRange = null
  searchForm.priceMin = null
  searchForm.priceMax = null
  searchForm.status = ''
  searchForm.categoryId = ''
  pageNum.value = 1
  load()
}

const search = () => { pageNum.value = 1; load() }

const openDialog = async (row) => {
  editId.value = row?.id || null
  if (editId.value) {
    const res = await getProduct(editId.value)
    Object.assign(form, res.data)
  } else {
    Object.assign(form, {
      name: '', code: '', categoryId: '', serviceType: '', price: 0, originalPrice: 0,
      unit: '', description: '', detail: '', sort: 0, recommend: 0, status: 1
    })
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editId.value) {
      await updateProduct({ ...form, id: editId.value })
      ElMessage.success('修改成功')
    } else {
      await addProduct(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {}
}

const del = async (row) => {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  load()
}

const onSelect = (rows) => { selection.value = rows }
const clearSelection = () => { tableRef.value?.clearSelection(); selection.value = [] }

// 上架/下架：调用真实 updateProduct（PUT /product）更新 status（1上架 / 3下架）
const toggleShelf = async (row) => {
  const next = row.status === 1 ? 3 : 1
  try {
    await updateProduct({ id: row.id, status: next })
    ElMessage.success(next === 1 ? '上架成功' : '下架成功')
    load()
  } catch (e) {}
}

// 复制商品：后端未提供复制接口
// TODO 后端未提供
const copyRow = (row) => {
  // TODO 后端未提供 复制商品接口
}

const batchDelete = async () => {
  if (!selection.value.length) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selection.value.length} 个商品？`, '提示', { type: 'warning' })
  } catch { return }
  let ok = 0
  for (const row of selection.value) {
    try { await deleteProduct(row.id); ok++ } catch {}
  }
  ElMessage.success(`已删除 ${ok} 个商品`)
  clearSelection(); load()
}

onMounted(() => { load(); loadCategoryList() })
watch(() => route.query.serviceType, (nv) => { lockedServiceType.value = nv || ''; load() })
</script>

<style scoped>
.batch-bar { display:flex; align-items:center; gap:12px; margin-top:14px; padding:10px 14px; background:#f4f6f8; border-radius:8px; }
.batch-tip { font-size:13px; color:#606266; }
</style>
