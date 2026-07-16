<template>
  <div class="page-container">
    <!-- 顶部筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="交易类型">
          <el-select v-model="filterForm.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="收入" value="收入" />
            <el-option label="支出" value="支出" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片（基于真实列表数据计算） -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-label">总收入</div>
        <div class="stat-value income">¥{{ stats.totalIncome.toLocaleString() }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">总支出</div>
        <div class="stat-value expense">¥{{ stats.totalExpense.toLocaleString() }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">净利润</div>
        <div class="stat-value profit">¥{{ stats.profit.toLocaleString() }}</div>
      </div>
    </div>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="id" label="流水ID" width="90" />
        <el-table-column label="交易类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === '收入' ? 'success' : 'danger'">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="交易金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.type === '收入' ? '#52C41A' : '#FF4D4F', fontWeight: 600 }">
              {{ row.type === '收入' ? '+' : '-' }}¥{{ Number(row.amount || 0).toLocaleString() }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="账户余额" width="120">
          <template #default="{ row }">¥{{ Number(row.balance || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单" width="170" />
        <el-table-column prop="createTime" label="交易时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end"
        @current-change="load" @size-change="load" />
    </el-card>

    <!-- 查看弹窗 -->
    <el-dialog v-model="detailVisible" title="流水详情" width="480px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="流水ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="交易类型">
          <el-tag :type="currentRow.type === '收入' ? 'success' : 'danger'">{{ currentRow.type }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易金额">
          <span :style="{ color: currentRow.type === '收入' ? '#52C41A' : '#FF4D4F', fontWeight: 600 }">
            {{ currentRow.type === '收入' ? '+' : '-' }}¥{{ Number(currentRow.amount || 0).toLocaleString() }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="账户余额">¥{{ Number(currentRow.balance || 0).toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="关联订单" :span="2">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="交易时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getFinancePage } from '@/api'

const loading = ref(false), pageNum = ref(1), pageSize = ref(10), total = ref(0)
const list = ref([])
const filterForm = reactive({ type: '', keyword: '' })
const detailVisible = ref(false), currentRow = ref(null)

const stats = computed(() => {
  const inc = list.value.filter(i => i.type === '收入').reduce((s, i) => s + Number(i.amount || 0), 0)
  const exp = list.value.filter(i => i.type === '支出').reduce((s, i) => s + Number(i.amount || 0), 0)
  return { totalIncome: inc, totalExpense: exp, profit: inc - exp }
})

const load = async () => {
  loading.value = true
  try {
    const res = await getFinancePage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: filterForm.type || null,
      keyword: filterForm.keyword || null
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // 响应拦截器已弹出错误提示
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; load() }
const handleReset = () => { filterForm.type = ''; filterForm.keyword = ''; pageNum.value = 1; load() }
const handleView = (row) => { currentRow.value = row; detailVisible.value = true }

onMounted(load)
</script>

<style scoped>
.page-container { padding: 16px; }
.filter-card { margin-bottom: 12px; }

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #ebeef5;
}

.stat-label { font-size: 13px; color: #A0AEC0; margin-bottom: 8px; }
.stat-value { font-size: 22px; font-weight: 700; }
.stat-value.income { color: #52C41A; }
.stat-value.expense { color: #FF4D4F; }
.stat-value.profit { color: #00C4A1; }
</style>
