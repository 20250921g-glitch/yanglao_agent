<template>
  <div class="mall-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- ===== 商品目录 ===== -->
      <el-tab-pane label="商品目录" name="products">
        <el-form :inline="true" :model="search" class="filter-bar">
          <el-form-item label="分类">
            <el-select v-model="search.categoryId" placeholder="全部" clearable style="width:160px" @change="loadProducts">
              <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="search.keyword" placeholder="商品名称" clearable style="width:180px" @keyup.enter="loadProducts" @clear="loadProducts" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadProducts">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <div v-loading="loading" class="card-wrap">
          <el-empty v-if="!loading && products.length === 0" description="暂无在售商品" />
          <el-card v-for="p in products" :key="p.id" class="goods-card" shadow="hover">
            <div class="goods-cover">
              <el-image v-if="p.image" :src="p.image" fit="cover" class="goods-img">
                <template #error><div class="goods-ph">{{ (p.categoryName || p.name || '商').charAt(0) }}</div></template>
              </el-image>
              <div v-else class="goods-ph">{{ (p.categoryName || p.name || '商').charAt(0) }}</div>
              <el-tag v-if="p.recommend === 1" class="goods-badge" type="danger" size="small" effect="dark">推荐</el-tag>
            </div>
            <div class="goods-body">
              <div class="goods-name" :title="p.name">{{ p.name }}</div>
              <div class="goods-meta">
                <el-tag size="small" effect="plain">{{ p.categoryName || '养老商品' }}</el-tag>
                <span class="dot">·</span>已售 {{ p.sales || 0 }}
              </div>
              <div class="goods-foot">
                <div class="goods-price">
                  ¥{{ Number(p.price).toFixed(2) }}
                  <span v-if="p.unit" class="goods-unit">/{{ p.unit }}</span>
                </div>
                <el-button type="primary" size="small" :disabled="p.stock != null && p.stock <= 0" @click="openBuy(p)">
                  {{ p.stock != null && p.stock <= 0 ? '缺货' : '立即购买' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- ===== 我的订单 ===== -->
      <el-tab-pane label="我的订单" name="mine">
        <div class="mine-toolbar">
          <el-select v-model="myStatus" placeholder="全部状态" clearable style="width:150px" @change="reloadMine">
            <el-option v-for="(t, k) in orderStatusText" :key="k" :label="t" :value="Number(k)" />
          </el-select>
        </div>
        <el-table :data="myOrders" stripe v-loading="myLoading" style="margin-top:8px">
          <template #empty><el-empty description="您还没有商城订单" /></template>
          <el-table-column prop="orderNo" label="订单号" min-width="170" />
          <el-table-column prop="productName" label="商品" min-width="160" />
          <el-table-column label="数量" width="80" align="center">
            <template #default="{ row }">x{{ row.productCount || 1 }}</template>
          </el-table-column>
          <el-table-column label="金额" width="110">
            <template #default="{ row }">¥{{ row.totalPrice != null ? Number(row.totalPrice).toFixed(2) : '0.00' }}</template>
          </el-table-column>
          <el-table-column label="下单时间" min-width="160">
            <template #default="{ row }">{{ fmt(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="orderStatusTag[row.status]" size="small">{{ orderStatusText[row.status] || '未知' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 1 || row.status === 2" type="danger" size="small" plain @click="doCancel(row)">取消</el-button>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="myPageNum" v-model:page-size="myPageSize"
          :total="myTotal" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next"
          style="margin-top:16px;justify-content:flex-end" @change="loadMine" />
      </el-tab-pane>
    </el-tabs>

    <!-- 购买对话框 -->
    <el-dialog v-model="buyVisible" title="确认购买" width="520px">
      <div v-if="current" class="buy-preview">
        <div class="bp-title">{{ current.name }}</div>
        <div class="bp-meta">{{ current.categoryName || '养老商品' }}<span v-if="current.stock != null"> · 库存 {{ current.stock }}</span></div>
        <div class="bp-price">¥{{ Number(current.price).toFixed(2) }}<span v-if="current.unit" class="goods-unit">/{{ current.unit }}</span></div>
      </div>
      <el-form label-width="90px" style="margin-top:14px">
        <el-form-item label="购买数量" required>
          <el-input-number v-model="buyForm.quantity" :min="1" :max="current && current.stock ? current.stock : 999" />
        </el-form-item>
        <el-form-item label="收货老人">
          <el-select v-model="buyForm.elderId" placeholder="可选，指定收货老人" clearable style="width:100%">
            <el-option v-for="e in elders" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="buyForm.contactName" placeholder="收货联系人（选填）" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="buyForm.contactPhone" placeholder="收货电话（选填）" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="buyForm.address" placeholder="收货地址（选填）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="buyForm.remark" type="textarea" :rows="2" placeholder="如有特殊需求可在此说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="buy-footer">
          <span class="buy-total">合计：<b>¥{{ buyTotal }}</b></span>
          <span>
            <el-button @click="buyVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitBuy">确认下单</el-button>
          </span>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMallProducts, getMallElders, createMallOrder, myMallOrders, cancelMallOrder
} from '@/api'

// product_order status: 1待付款 2待接单 3待服务 4已完成 5已关闭 6退款售后
const orderStatusText = { 1: '待付款', 2: '待接单', 3: '待服务', 4: '已完成', 5: '已关闭', 6: '退款售后' }
const orderStatusTag = { 1: 'warning', 2: 'primary', 3: '', 4: 'success', 5: 'info', 6: 'danger' }

const activeTab = ref('products')

const loading = ref(false)
const products = ref([])
const search = reactive({ categoryId: null, keyword: '' })

// 分类从商品列表动态提取（categoryId + categoryName）
const categoryOptions = computed(() => {
  const map = new Map()
  products.value.forEach(p => {
    if (p.categoryId != null && !map.has(p.categoryId)) {
      map.set(p.categoryId, { id: p.categoryId, name: p.categoryName || ('分类' + p.categoryId) })
    }
  })
  return Array.from(map.values())
})

const elders = ref([])
const buyVisible = ref(false)
const current = ref(null)
const buyForm = reactive({ quantity: 1, elderId: null, contactName: '', contactPhone: '', address: '', remark: '' })
const submitting = ref(false)

const buyTotal = computed(() => {
  if (!current.value) return '0.00'
  return (Number(current.value.price || 0) * (buyForm.quantity || 1)).toFixed(2)
})

const myLoading = ref(false)
const myOrders = ref([])
const myStatus = ref('')
const myPageNum = ref(1)
const myPageSize = ref(10)
const myTotal = ref(0)

const fmt = (d) => {
  if (!d) return '—'
  return String(d).replace('T', ' ').substring(0, 16)
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getMallProducts({
      categoryId: search.categoryId || undefined,
      keyword: search.keyword || undefined
    })
    products.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  search.categoryId = null
  search.keyword = ''
  loadProducts()
}

const openBuy = async (p) => {
  current.value = p
  buyForm.quantity = 1
  buyForm.elderId = null
  buyForm.contactName = ''
  buyForm.contactPhone = ''
  buyForm.address = ''
  buyForm.remark = ''
  if (elders.value.length === 0) {
    try {
      const res = await getMallElders()
      elders.value = res.data || []
    } catch (e) { /* 忽略 */ }
  }
  buyVisible.value = true
}

const submitBuy = async () => {
  if (!buyForm.quantity || buyForm.quantity < 1) {
    ElMessage.warning('请填写购买数量')
    return
  }
  submitting.value = true
  try {
    await createMallOrder({
      productId: current.value.id,
      quantity: buyForm.quantity,
      elderId: buyForm.elderId || undefined,
      contactName: buyForm.contactName || undefined,
      contactPhone: buyForm.contactPhone || undefined,
      address: buyForm.address || undefined,
      remark: buyForm.remark || undefined
    })
    ElMessage.success('下单成功')
    buyVisible.value = false
    activeTab.value = 'mine'
    reloadMine()
    loadProducts()
  } catch (e) {
    ElMessage.error(e.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

const loadMine = async () => {
  myLoading.value = true
  try {
    const res = await myMallOrders({
      pageNum: myPageNum.value,
      pageSize: myPageSize.value,
      status: myStatus.value === '' ? undefined : myStatus.value
    })
    myOrders.value = res.data.records || []
    myTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载我的订单失败')
  } finally {
    myLoading.value = false
  }
}

const reloadMine = () => {
  myPageNum.value = 1
  loadMine()
}

const doCancel = async (row) => {
  await ElMessageBox.confirm(`确认取消订单「${row.productName}」？`, '提示', { type: 'warning' })
  try {
    await cancelMallOrder(row.id)
    ElMessage.success('已取消')
    loadMine()
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  }
}

const onTabChange = (name) => {
  if (name === 'mine') loadMine()
}

onMounted(loadProducts)
</script>

<style scoped>
.mall-page { padding: 4px; }
.filter-bar { margin-bottom: 8px; background: #fafafa; padding: 12px; border-radius: 8px; }
.card-wrap { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; min-height: 120px; }
.goods-card { border-radius: 10px; overflow: hidden; }
.goods-card :deep(.el-card__body) { padding: 0; }
.goods-cover { position: relative; height: 150px; background: #f2f6f5; }
.goods-img { width: 100%; height: 100%; display: block; }
.goods-ph { width: 100%; height: 150px; display: flex; align-items: center; justify-content: center; font-size: 44px; color: #fff; font-weight: 700; background: linear-gradient(135deg, #00C4A1, #33d4b6); }
.goods-badge { position: absolute; top: 8px; left: 8px; }
.goods-body { padding: 12px; }
.goods-name { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.goods-meta { font-size: 12px; color: #909399; display: flex; align-items: center; gap: 6px; margin-bottom: 12px; }
.goods-meta .dot { color: #dcdfe6; }
.goods-foot { display: flex; align-items: center; justify-content: space-between; }
.goods-price { font-size: 18px; font-weight: 700; color: #F56C6C; }
.goods-unit { font-size: 12px; font-weight: 400; color: #909399; }
.mine-toolbar { display: flex; justify-content: flex-end; margin-bottom: 4px; }
.buy-preview { background: #f7fbfa; padding: 12px; border-radius: 8px; }
.bp-title { font-size: 16px; font-weight: 600; color: #303133; }
.bp-meta { font-size: 13px; color: #909399; margin-top: 4px; }
.bp-price { font-size: 18px; font-weight: 700; color: #F56C6C; margin-top: 6px; }
.buy-footer { display: flex; align-items: center; justify-content: space-between; }
.buy-total { font-size: 14px; color: #606266; }
.buy-total b { font-size: 18px; color: #F56C6C; }
</style>
