<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <el-button @click="$router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <span class="page-title">新增商品</span>
    </div>

    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <!-- 基本信息 -->
        <div class="form-section-title">基本信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编码" prop="code">
              <el-input v-model="form.code" placeholder="请输入商品编码" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品图片">
              <el-input v-model="form.image" placeholder="请输入商品图片URL" />
              <div v-if="form.image" style="margin-top:8px">
                <el-image :src="form.image" style="width:100px;height:100px;border-radius:6px;border:1px solid #ebeef5" fit="cover" :preview-src-list="[form.image]" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品描述">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分类与类型 -->
        <div class="form-section-title">分类与类型</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务类型" prop="serviceType">
              <el-select v-model="form.serviceType" placeholder="请选择服务类型" style="width:100%" @change="onServiceTypeChange">
                <el-option label="家政护理" value="家政护理" />
                <el-option label="康复理疗" value="康复理疗" />
                <el-option label="上门体检" value="上门体检" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请先选择服务类型" style="width:100%" :disabled="!form.serviceType">
                <el-option v-for="cat in filteredCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 价格与库存 -->
        <div class="form-section-title">价格与库存</div>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价（元）" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市场价（元）">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 商品详情 -->
        <div class="form-section-title">商品详情</div>
        <el-form-item label="商品详情">
          <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="请输入商品详情" />
        </el-form-item>

        <!-- 销售设置 -->
        <div class="form-section-title">销售设置</div>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="如：次/小时/套" />
            </el-form-item>
          </el-col>
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
              <el-select v-model="form.status" style="width:100%">
                <el-option label="待审核" :value="0" />
                <el-option label="已上架" :value="1" />
                <el-option label="已下架" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标签">
              <el-select v-model="form.tags" multiple placeholder="请选择商品标签" style="width:100%" allow-create filterable>
                <el-option label="热门" value="热门" />
                <el-option label="推荐" value="推荐" />
                <el-option label="特价" value="特价" />
                <el-option label="新品" value="新品" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="销售说明">
              <el-input v-model="form.saleNote" type="textarea" :rows="2" placeholder="如：预约后24小时内上门服务" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" @click="submit" :loading="submitting">提交</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addProduct, getProductCategoryList } from '@/api'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categoryList = ref([])

const form = reactive({
  name: '',
  code: '',
  image: '',
  description: '',
  serviceType: '',
  categoryId: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  detail: '',
  unit: '',
  sort: 0,
  recommend: 0,
  status: 0,
  tags: [],
  saleNote: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const filteredCategories = computed(() => {
  if (!form.serviceType) return categoryList.value
  return categoryList.value.filter(c => c.serviceType === form.serviceType)
})

const onServiceTypeChange = () => {
  form.categoryId = ''
}

const loadCategories = async () => {
  try {
    const res = await getProductCategoryList()
    categoryList.value = res.data || []
  } catch (e) {}
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await addProduct({ ...form })
    ElMessage.success('新增成功')
    router.push('/product/list')
  } catch (e) {} finally {
    submitting.value = false
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.page-container {
  padding: 16px;
  max-width: 960px;
  margin: 0 auto;
}
.top-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  flex: 1;
}
.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #00C4A1;
  margin: 8px 0 16px;
  padding-left: 10px;
  border-left: 3px solid #00C4A1;
}
.form-actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
