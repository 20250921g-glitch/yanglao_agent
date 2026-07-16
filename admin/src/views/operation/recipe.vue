<template>
  <div class="recipe-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>食谱管理</span>
          <div class="type-tabs">
            <el-radio-group v-model="tabType" @change="loadData">
              <el-radio-button value="all">全部食谱</el-radio-button>
              <el-radio-button value="breakfast">早餐</el-radio-button>
              <el-radio-button value="lunch">午餐</el-radio-button>
              <el-radio-button value="dinner">晚餐</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" @click="openDialog()">新增食谱</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="食谱名称">
          <el-input v-model="search.name" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="search.category" placeholder="请选择" clearable style="width:120px">
            <el-option value="breakfast">早餐</el-option>
            <el-option value="lunch">午餐</el-option>
            <el-option value="dinner">晚餐</el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-loading="loading" element-loading-text="加载中">
        <div v-if="!list.length" class="empty-wrap">
          <el-empty description="暂无数据" />
        </div>
        <div class="recipe-grid" v-else>
          <div class="recipe-card" v-for="item in list" :key="item.id">
            <el-image :src="item.image" fit="cover" class="recipe-image" v-if="item.image" />
            <div class="recipe-image recipe-image--ph" v-else></div>
            <div class="recipe-info">
              <div class="recipe-header">
                <span class="recipe-name">{{ item.name }}</span>
                <el-tag :type="mealTypeTag(item.category)" size="small">{{ item.categoryText || item.category || '—' }}</el-tag>
              </div>
              <div class="recipe-desc">{{ item.remark || item.nutrition || '—' }}</div>
              <div class="nutrition-info">
                <span class="nutrition-item">🍽️ {{ item.calories ?? 0 }}千卡</span>
                <span class="nutrition-item">🥗 {{ Number(item.protein) || 0 }}g蛋白质</span>
              </div>
              <div class="action-bar">
                <el-button type="primary" size="small" @click="previewDetail(item)">查看详情</el-button>
                <el-button type="success" size="small" @click="handleEdit(item)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(item.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[9,18,36,72]" layout="total,sizes,prev,pager,next" style="margin-top:20px;justify-content:flex-end"
        @current-change="loadData" @size-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑食谱' : '新增食谱'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="食谱名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="餐次" prop="category">
          <el-select v-model="form.category" style="width:100%">
            <el-option value="breakfast">早餐</el-option>
            <el-option value="lunch">午餐</el-option>
            <el-option value="dinner">晚餐</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="食谱图片">
          <el-upload action="#" :file-list="form.fileList" :auto-upload="false" list-type="picture-card">
            <el-icon :size="32" name="Plus" />
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="热量(千卡)" prop="calories">
          <el-input-number v-model="form.calories" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="蛋白质(g)" prop="protein">
          <el-input-number v-model="form.protein" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="食材清单">
          <el-input v-model="form.ingredients" type="textarea" :rows="3" placeholder="请输入食材清单" />
        </el-form-item>
        <el-form-item label="烹饪步骤">
          <el-input v-model="form.steps" type="textarea" :rows="4" placeholder="请输入烹饪步骤" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="btnLoading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="食谱详情" width="500px">
      <div v-if="previewData" class="preview-content">
        <el-image :src="previewData.image" fit="cover" class="preview-image" v-if="previewData.image" />
        <div class="preview-header">
          <span class="preview-name">{{ previewData.name }}</span>
          <el-tag :type="mealTypeTag(previewData.category)" size="small">{{ previewData.categoryText || previewData.category || '—' }}</el-tag>
        </div>
        <div class="preview-desc">{{ previewData.remark || previewData.nutrition || '—' }}</div>
        <div class="preview-nutrition">
          <div class="nutrition-title">营养信息</div>
          <div class="nutrition-grid">
            <div class="nutrition-item">
              <div class="nutrition-icon">🍽️</div>
              <div class="nutrition-text">{{ previewData.calories ?? 0 }}千卡</div>
            </div>
            <div class="nutrition-item">
              <div class="nutrition-icon">🥗</div>
              <div class="nutrition-text">{{ Number(previewData.protein) || 0 }}g蛋白质</div>
            </div>
          </div>
        </div>
        <div class="preview-section" v-if="previewData.ingredients">
          <div class="section-title">食材清单</div>
          <div class="section-content">{{ previewData.ingredients }}</div>
        </div>
        <div class="preview-section" v-if="previewData.steps">
          <div class="section-title">烹饪步骤</div>
          <div class="section-content">{{ previewData.steps }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecipePage, deleteRecipe } from '@/api'

const tabType = ref('all')
const loading = ref(false)
const btnLoading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(9)
const total = ref(0)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const previewData = ref({})

const search = reactive({
  name: '',
  category: ''
})

const form = reactive({
  id: null,
  name: '',
  category: 'lunch',
  fileList: [],
  remark: '',
  calories: 0,
  protein: 0,
  ingredients: '',
  steps: ''
})

const rules = {
  name: [{ required: true, message: '请输入食谱名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择餐次', trigger: 'change' }],
  remark: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  calories: [{ required: true, message: '请输入热量', trigger: 'blur' }],
  protein: [{ required: true, message: '请输入蛋白质', trigger: 'blur' }]
}

const mealTypeMap = {
  breakfast: { tag: 'success' },
  lunch: { tag: 'primary' },
  dinner: { tag: 'warning' }
}
const mealTypeTag = (type) => mealTypeMap[type]?.tag || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (tabType.value !== 'all') params.category = tabType.value
    if (search.name) params.name = search.name
    if (search.category) params.category = search.category
    const res = await getRecipePage(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const reset = () => {
  search.name = ''
  search.category = ''
  pageNum.value = 1
  loadData()
}

const openDialog = () => {
  isEdit.value = false
  formRef.value?.resetFields()
  Object.assign(form, { id: null, name: '', category: 'lunch', fileList: [], remark: '', calories: 0, protein: 0, ingredients: '', steps: '' })
  dialogVisible.value = true
}

const handleEdit = (item) => {
  isEdit.value = true
  Object.assign(form, { id: item.id, name: item.name, category: item.category, fileList: [], remark: item.remark || '', calories: item.calories || 0, protein: Number(item.protein) || 0, ingredients: item.ingredients || '', steps: item.steps || '' })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  btnLoading.value = true
  try {
    // TODO: 后端食谱保存需配合图片上传，且字段含 nutrition/fat/carbs，待接入
    ElMessage.info('食谱保存功能待接入后端')
    dialogVisible.value = false
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { btnLoading.value = false }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该食谱？', '提示', { type: 'warning' })
    await deleteRecipe(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const previewDetail = (item) => {
  previewData.value = item
  previewVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.recipe-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.type-tabs { display: flex; gap: 0; }
.search-form { margin-bottom: 20px; }
.empty-wrap { padding: 40px 0; }

.recipe-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.recipe-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.recipe-image {
  width: 100%;
  height: 150px;
}
.recipe-image--ph { background: #f4f6f8; }

.recipe-info { padding: 16px; }

.recipe-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.recipe-name { font-size: 16px; font-weight: 600; color: #303133; }

.recipe-desc {
  font-size: 13px;
  color: #A0AEC0;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.nutrition-info {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.nutrition-item { font-size: 12px; color: #606266; }

.action-bar {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.preview-content { padding: 0; }
.preview-image { width: 100%; height: 200px; }
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}
.preview-name { font-size: 18px; font-weight: 600; color: #303133; }
.preview-desc { font-size: 14px; color: #606266; line-height: 1.6; margin-bottom: 16px; }

.preview-nutrition {
  background: #f8faf9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.nutrition-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px; }

.nutrition-grid {
  display: flex;
  gap: 20px;
}

.preview-section { margin-bottom: 16px; }
.section-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.section-content { font-size: 14px; color: #606266; line-height: 1.6; white-space: pre-wrap; }
</style>
