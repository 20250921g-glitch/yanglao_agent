<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>商品订单</span>
        <el-button type="primary" v-has="'order:export'" @click="exportCsv">导出</el-button>
      </div>
    </template>

    <!-- 状态页签 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane v-for="t in tabs" :key="t.value" :label="t.label" :name="t.value" />
    </el-tabs>

    <!-- 搜索筛选 -->
    <el-form :inline="true" :model="searchForm" style="margin:16px 0">
      <el-form-item label="订单编号">
        <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable style="width:180px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item label="下单日期">
        <el-date-picker v-model="searchForm.dateRange" type="daterange" unlink-panels
          range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:240px" @change="load" />
      </el-form-item>
      <el-form-item label="支付方式">
        <el-select v-model="searchForm.payType" placeholder="全部" clearable style="width:130px" @change="load">
          <el-option label="支付宝" value="支付宝" />
          <el-option label="微信" value="微信" />
          <el-option label="银行卡" value="银行卡" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务类型">
        <!-- TODO 数据无服务类型字段，后端未提供该筛选 -->
        <el-select disabled placeholder="暂无" style="width:130px">
          <el-option label="—" value="" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="tableRef" :data="pagedData" stripe v-loading="loading" @selection-change="onSelect">
      <template #empty><el-empty description="暂无订单数据" /></template>
      <el-table-column type="selection" width="48" />
      <el-table-column label="商品信息" min-width="200">
        <template #default="{ row }">
          <div class="goods-cell">
            <el-image :src="row.productImage || defaultImg" fit="cover" class="goods-img">
              <template #error><div class="goods-img goods-img--ph"><el-icon><Goods /></el-icon></div></template>
            </el-image>
            <div class="goods-meta">
              <div class="goods-name">{{ row.productName || '养老服务订单' }}</div>
              <div class="goods-sub">{{ row.serviceType || '居家养老服务' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="150">
        <template #default="{ row }">
          <div class="price">
            <div>应付：<span class="pay">¥{{ row.payablePrice ?? row.totalPrice }}</span></div>
            <div>实付：<span class="actual">¥{{ row.totalPrice }}</span></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="买家" min-width="150">
        <template #default="{ row }">
          <div>{{ row.elderName || row.contactName || '—' }}</div>
          <div class="sub">{{ row.contactPhone || '—' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.label || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="100">
        <template #default="{ row }">{{ row.payType || '—' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column prop="orderNo" label="订单编号" width="190" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="contact(row)">联系用户</el-button>
          <el-button link type="primary" size="small" @click="toDetail(row)">订单详情</el-button>
          <el-button link type="primary" size="small" v-has="'order:remark'" @click="openRemark(row)">备注</el-button>
          <el-button link type="warning" size="small" v-if="row.status === 2 || row.status === 3" @click="refundOrder(row)">退款</el-button>
          <el-button link type="primary" size="small" v-if="row.status === 2" @click="manualDispatch(row)">手动派单</el-button>
          <el-button link type="warning" size="small" v-if="row.status === 4" @click="afterSale(row)">发起售后</el-button>
          <el-button link type="danger" size="small" v-if="row.status === 1" v-has="'order:close'" @click="closeOrder(row)">关闭订单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批量操作栏 -->
    <div v-if="selection.length" class="batch-bar">
      <span class="batch-tip">已选 {{ selection.length }} 项</span>
      <el-button type="warning" size="small" v-has="'order:close'" @click="batchClose">批量关闭</el-button>
      <el-button type="success" size="small" v-has="'order:export'" @click="exportSelected">导出选中</el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>

    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="filteredTotal"
      :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />

    <!-- 备注对话框（演示：后端暂无备注字段，仅前端草稿） -->
    <el-dialog v-model="remarkVisible" title="订单备注" width="460px">
      <el-input v-model="remarkText" type="textarea" :rows="4" placeholder="请输入备注（保存需后端扩展）" />
      <template #footer>
        <el-button @click="remarkVisible=false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductOrderPage, updateProductOrderStatus, updateProductOrderPrice } from '@/api'

const router = useRouter()
const defaultImg = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

// 状态：1待付款 2待接单 3待服务 4已完成 5已关闭 6退款售后
const statusMap = {
  1: { label: '待付款', type: 'warning' },
  2: { label: '待接单', type: 'primary' },
  3: { label: '待服务', type: 'info' },
  4: { label: '已完成', type: 'success' },
  5: { label: '已关闭', type: 'info' },
  6: { label: '退款售后', type: 'danger' }
}
const tabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: '1' },
  { label: '待接单', value: '2' },
  { label: '待服务', value: '3' },
  { label: '退款售后', value: '6' },
  { label: '已关闭', value: '5' },
  { label: '已完成', value: '4' }
]

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const rawData = ref([]), selection = ref([]), tableRef = ref(null)
const activeTab = ref('all')
const searchForm = reactive({ orderNo: '', dateRange: null, payType: null })
const remarkVisible = ref(false), remarkText = ref(''), remarkRow = ref(null)

const fmtDate = (d) => {
  const dt = new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())}`
}

const load = async () => {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? null : Number(activeTab.value)
    const range = searchForm.dateRange
    const res = await getProductOrderPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderNo: searchForm.orderNo || null,
      status,
      payType: searchForm.payType || null,
      startDate: range && range.length === 2 ? fmtDate(range[0]) : null,
      endDate: range && range.length === 2 ? fmtDate(range[1]) : null
    })
    rawData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {}
  finally { loading.value = false }
}

// 服务端已完成状态/日期/订单号筛选；支付方式后端未提供筛选参数，前端按真实返回数据二次过滤
const filteredTotal = computed(() => total.value)
const pagedData = computed(() => {
  const p = searchForm.payType
  if (!p) return rawData.value
  return rawData.value.filter(r => r.payType === p)
})

const handleTabChange = () => { pageNum.value = 1; load() }
const resetSearch = () => { searchForm.orderNo = ''; searchForm.dateRange = null; searchForm.payType = null; pageNum.value = 1; load() }
const onSelect = (rows) => { selection.value = rows }
const toDetail = (row) => router.push(`/trade/order-detail/${row.id}`)
const contact = (row) => {
  const phone = row.contactPhone || row.phone
  if (phone) { navigator.clipboard?.writeText(phone); ElMessage.success(`已复制手机号：${phone}`) }
  else ElMessage.info('该订单暂无联系方式')
}
const closeOrder = async (row) => {
  await ElMessageBox.confirm('确认关闭该订单？', '提示', { type: 'warning' })
  await updateProductOrderStatus(row.id, 5)
  ElMessage.success('订单已关闭'); load()
}
const refundOrder = async (row) => {
  await ElMessageBox.confirm('确认对该订单发起退款？', '提示', { type: 'warning' })
  await updateProductOrderStatus(row.id, 6) // 退款售后
  ElMessage.success('已发起退款'); load()
}
const manualDispatch = (row) => {
  // TODO 后端未提供派单接口
  ElMessage.info('手动派单功能待后端提供接口')
}
const afterSale = async (row) => {
  await ElMessageBox.confirm('确认对该订单发起售后？', '提示', { type: 'warning' })
  await updateProductOrderStatus(row.id, 6) // 退款售后
  ElMessage.success('已发起售后'); load()
}
const openRemark = (row) => { remarkRow.value = row; remarkText.value = row.remark || ''; remarkVisible.value = true }
const saveRemark = async () => {
  if (!remarkRow.value) return
  try {
    await updateProductOrderPrice(remarkRow.value.id, { price: remarkRow.value.totalPrice, remark: remarkText.value })
    ElMessage.success('备注已保存')
    remarkVisible.value = false
    load()
  } catch (e) { ElMessage.error('备注保存失败') }
}
const exportCsv = (rows) => {
  const data = rows || pagedData.value
  if (!data.length) return ElMessage.warning('当前无数据可导出')
  const headers = ['订单编号', '商品', '件数', '应付', '实付', '买家', '手机号', '支付方式', '状态', '下单时间']
  const lines = data.map(r => [
    r.orderNo, r.productName || '', r.productCount ?? '', r.payablePrice ?? r.totalPrice ?? '',
    r.totalPrice ?? '', r.elderName || '', r.contactPhone || '', r.payType || '',
    statusMap[r.status]?.label || '', r.createTime || ''
  ].map(v => `"${String(v).replace(/"/g, '""')}"`).join(','))
  const csv = '\uFEFF' + [headers.join(','), ...lines].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `商品订单_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success(`已导出 ${data.length} 条订单`)
}

const clearSelection = () => { tableRef.value?.clearSelection(); selection.value = [] }

const batchClose = async () => {
  if (!selection.value.length) return
  try {
    await ElMessageBox.confirm(`确认关闭选中的 ${selection.value.length} 个订单？`, '提示', { type: 'warning' })
  } catch { return }
  let ok = 0
  for (const row of selection.value) {
    if (row.status === 5) continue
    try { await updateProductOrderStatus(row.id, 5); ok++ } catch {}
  }
  ElMessage.success(`已关闭 ${ok} 个订单`)
  clearSelection(); load()
}

const exportSelected = () => {
  if (!selection.value.length) return ElMessage.warning('请先勾选订单')
  exportCsv(selection.value)
}

onMounted(load)
</script>

<style scoped>
.goods-cell { display:flex; align-items:center; gap:10px; }
.goods-img { width:46px; height:46px; border-radius:6px; flex:0 0 auto; }
.goods-img--ph { display:flex; align-items:center; justify-content:center; background:#f4f6f8; color:#bbb; font-size:20px; }
.goods-name { font-weight:500; color:#303133; }
.goods-sub { color:#A0AEC0; font-size:12px; margin-top:2px; }
.sub { color:#A0AEC0; font-size:12px; }
.price { line-height:1.5; }
.price .pay { color:#A0AEC0; }
.price .actual { color:#FF4D4F; font-weight:600; }
.batch-bar { display:flex; align-items:center; gap:12px; margin-top:14px; padding:10px 14px; background:#f4f6f8; border-radius:8px; }
.batch-tip { font-size:13px; color:#606266; }
</style>
