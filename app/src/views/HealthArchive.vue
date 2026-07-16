<template>
  <div class="health-archive" v-loading="loading">
    <!-- 无关联档案：空态 -->
    <el-empty v-if="!loading && elders.length === 0" description="您还没有添加任何健康档案">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openElderAdd">添加第一位老人档案</el-button>
      </template>
      <template #description>
        <p>您还没有添加任何健康档案</p>
        <p class="empty-tip">您可以自己添加家人档案，并随时录入血压、血糖等健康数据。</p>
      </template>
    </el-empty>

    <template v-else>
      <!-- 老人切换 -->
      <el-card class="elder-switch" shadow="never" v-if="elders.length > 1">
        <span class="switch-label">我的档案：</span>
        <el-select v-model="selectedElderId" @change="onElderChange" style="width:200px">
          <el-option v-for="e in elders" :key="e.id" :value="e.id" :label="e.name + '（' + (e.gender===1?'男':'女') + '·' + (e.age||'?') + '岁）'" />
        </el-select>
      </el-card>

      <!-- 操作工具栏 -->
      <el-card class="archive-toolbar" shadow="never">
        <el-button type="primary" :icon="Plus" @click="openElderAdd">添加老人</el-button>
        <el-button :icon="Edit" :disabled="!selectedElderId" @click="openElderEdit">编辑档案</el-button>
        <el-button type="danger" plain :icon="Delete" :disabled="!selectedElderId" @click="deleteElder">删除档案</el-button>
        <span class="toolbar-tip">档案由您本人维护，可随时修改或删除</span>
      </el-card>

      <el-row :gutter="20">
        <!-- 左：基本信息卡 -->
        <el-col :span="6">
          <el-card shadow="hover" class="profile-card">
            <div class="profile">
              <el-avatar :size="80" style="background:#00C4A1;font-size:32px;margin-bottom:12px">
                {{ elderInfo.name ? elderInfo.name.charAt(0) : '老' }}
              </el-avatar>
              <h3>{{ elderInfo.name || '—' }}</h3>
              <p class="sub">{{ elderInfo.gender === 1 ? '男' : '女' }} · {{ elderInfo.age || '?' }}岁</p>
              <el-tag :type="healthMeta.type" style="margin:8px 0">{{ healthMeta.text }}</el-tag>
              <el-divider />
              <div class="info-item"><span class="label">联系电话</span><span>{{ elderInfo.phone || '—' }}</span></div>
              <div class="info-item"><span class="label">身份证号</span><span>{{ elderInfo.idCard || '—' }}</span></div>
              <div class="info-item"><span class="label">紧急联系人</span><span>{{ elderInfo.emergencyContact || '—' }}</span></div>
              <div class="info-item"><span class="label">紧急电话</span><span>{{ elderInfo.emergencyPhone || '—' }}</span></div>
              <div class="info-item"><span class="label">家庭住址</span><span>{{ elderInfo.address || '—' }}</span></div>
            </div>
          </el-card>
        </el-col>

        <!-- 右：详情 tabs -->
        <el-col :span="18">
          <el-card shadow="hover">
            <el-tabs v-model="activeTab">
              <!-- 基本信息 -->
              <el-tab-pane label="基本信息" name="basic">
                <el-descriptions :column="2" border style="margin-top:8px">
                  <el-descriptions-item label="姓名">{{ elderInfo.name || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="性别">{{ elderInfo.gender === 1 ? '男' : '女' }}</el-descriptions-item>
                  <el-descriptions-item label="年龄">{{ elderInfo.age != null ? elderInfo.age + '岁' : '—' }}</el-descriptions-item>
                  <el-descriptions-item label="身份证号">{{ elderInfo.idCard || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="联系电话">{{ elderInfo.phone || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="健康等级">
                    <el-tag :type="healthMeta.type" size="small">{{ healthMeta.text }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="紧急联系人">{{ elderInfo.emergencyContact || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="紧急电话">{{ elderInfo.emergencyPhone || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="账号状态">
                    <el-tag :type="elderInfo.status === 1 ? 'success' : 'info'" size="small">
                      {{ elderInfo.status === 1 ? '正常' : '离世/退出' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="家庭住址" :span="2">{{ elderInfo.address || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="创建时间">{{ elderInfo.createTime || '—' }}</el-descriptions-item>
                  <el-descriptions-item label="更新时间">{{ elderInfo.updateTime || '—' }}</el-descriptions-item>
                </el-descriptions>
                <el-alert type="info" :closable="false" show-icon title="温馨提示"
                  description="以上信息由您本人维护，点击上方「编辑档案」即可修改。" style="margin-top:16px" />
              </el-tab-pane>

              <!-- 健康数据 -->
              <el-tab-pane label="健康数据" name="data">
                <div class="type-tabs">
                  <el-radio-group v-model="recordType">
                    <el-radio-button v-for="t in recordTypes" :key="t" :value="t">{{ t }}</el-radio-button>
                  </el-radio-group>
                </div>

                <div class="stats-row" v-if="stats">
                  <div class="stat-card">
                    <div class="stat-icon latest"><el-icon :size="30"><DataLine /></el-icon></div>
                    <div class="stat-info"><div class="stat-label">最近一次</div>
                      <div class="stat-value">{{ stats.latest }}<span class="stat-unit">{{ unit }}</span></div></div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-icon avg"><el-icon :size="30"><TrendCharts /></el-icon></div>
                    <div class="stat-info"><div class="stat-label">平均值</div>
                      <div class="stat-value">{{ stats.avg }}<span class="stat-unit">{{ unit }}</span></div></div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-icon max"><el-icon :size="30"><Top /></el-icon></div>
                    <div class="stat-info"><div class="stat-label">最高值</div>
                      <div class="stat-value">{{ stats.max }}<span class="stat-unit">{{ unit }}</span></div></div>
                  </div>
                  <div class="stat-card">
                    <div class="stat-icon min"><el-icon :size="30"><Bottom /></el-icon></div>
                    <div class="stat-info"><div class="stat-label">最低值</div>
                      <div class="stat-value">{{ stats.min }}<span class="stat-unit">{{ unit }}</span></div></div>
                  </div>
                </div>

                <div class="chart-card" v-if="recordsByType.length">
                  <div class="chart-title">{{ recordType }}趋势图</div>
                  <div ref="chartRef" class="chart-container"></div>
                </div>
                <el-empty v-else description="暂无该类型的健康数据" :image-size="80" />

                <el-table :data="recordsByType" stripe style="margin-top:16px" v-if="recordsByType.length">
                  <el-table-column prop="recordValue" :label="'记录值（' + unit + '）'" width="160" />
                  <el-table-column prop="recordTime" label="记录时间" width="180" />
                  <el-table-column prop="remark" label="备注" show-overflow-tooltip />
                  <el-table-column label="操作" width="80" fixed="right">
                    <template #default="{ row }">
                      <el-button type="danger" link size="small" :icon="Delete" @click="deleteRecord(row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <!-- 健康记录 -->
              <el-tab-pane label="健康记录" name="record">
                <div class="tab-toolbar">
                  <el-button type="primary" size="small" :icon="Plus" :disabled="!selectedElderId" @click="openRecordAdd">添加记录</el-button>
                  <span class="toolbar-tip">您可随时录入血压、血糖等健康数据</span>
                </div>
                <el-table :data="recPaged" stripe v-loading="loading">
                  <el-table-column prop="recordType" label="记录类型" width="120" />
                  <el-table-column prop="recordValue" label="记录值" width="160" />
                  <el-table-column prop="recordTime" label="记录时间" width="180" />
                  <el-table-column prop="remark" label="备注" show-overflow-tooltip />
                  <el-table-column label="操作" width="80" fixed="right">
                    <template #default="{ row }">
                      <el-button type="danger" link size="small" :icon="Delete" @click="deleteRecord(row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination v-model:current-page="recPageNum" :page-size="recPageSize"
                  :total="allRecords.length" layout="total,prev,pager,next"
                  style="margin-top:16px;justify-content:flex-end" />
              </el-tab-pane>

              <!-- 疾病宝典 -->
              <el-tab-pane label="疾病宝典" name="disease">
                <el-alert type="success" :closable="false" show-icon
                  description="以下为养老健康科普知识库（由平台维护），供您日常参考。" style="margin-bottom:16px" />
                <el-table :data="diseases" border stripe>
                  <el-table-column prop="name" label="疾病名称" width="160" />
                  <el-table-column label="分类" width="160">
                    <template #default="{ row }">{{ disCategoryMap[row.category] || row.category || '—' }}</template>
                  </el-table-column>
                  <el-table-column prop="symptoms" label="常见症状" min-width="240" show-overflow-tooltip />
                  <el-table-column prop="treatment" label="治疗方案" min-width="200" show-overflow-tooltip />
                  <el-table-column prop="precautions" label="注意事项" min-width="200" show-overflow-tooltip />
                </el-table>
              </el-tab-pane>

              <!-- 家属 -->
              <el-tab-pane label="家属信息" name="family">
                <el-table :data="family" stripe>
                  <el-table-column prop="familyName" label="家属姓名" width="160" />
                  <el-table-column prop="relation" label="关系" width="160" />
                  <el-table-column prop="phone" label="联系电话" />
                </el-table>
                <el-empty v-if="!family.length" description="暂无家属信息" />
              </el-tab-pane>

              <!-- AI 健康建议 -->
              <el-tab-pane label="AI 健康建议" name="ai">
                <div class="ai-advice">
                  <div class="ai-header">
                    <el-button type="primary" :loading="aiLoading" :icon="MagicStick" @click="getAdvice(false)">
                      生成健康建议
                    </el-button>
                    <el-button :loading="aiLoading" :disabled="!aiAdvice" @click="getAdvice(true)">
                      重新生成
                    </el-button>
                    <span class="ai-tip">
                      <el-icon class="ai-tip-icon"><Info-Filled /></el-icon>
                      基于当前老人的真实健康档案，由 DeepSeek 大模型生成
                    </span>
                  </div>
                  <el-alert
                    v-if="aiAdvice && aiCached"
                    type="success"
                    :closable="false"
                    show-icon
                    class="ai-cache-hint"
                    title="已命中缓存（30 分钟内重复生成不消耗 AI 额度）"
                  />
                  <el-card v-if="aiAdvice" shadow="hover" class="ai-result">
                    <div class="ai-result-title">
                      <el-icon><MagicStick /></el-icon>
                      AI 健康建议（覆盖 {{ aiElderCount || 1 }} 位老人）
                      <el-tag v-if="aiCached" size="small" type="info" effect="plain">缓存</el-tag>
                    </div>
                    <div class="ai-result-body" v-html="aiAdviceHtml"></div>
                  </el-card>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 老人档案 新增/编辑 -->
    <el-dialog :title="elderDialog.isEdit ? '编辑老人档案' : '添加老人档案'" v-model="elderDialog.visible" width="560px" @closed="resetElderForm">
      <el-form :model="elderDialog.form" label-width="92px">
        <el-form-item label="姓名" required><el-input v-model="elderDialog.form.name" placeholder="请输入老人姓名" /></el-form-item>
        <el-form-item label="性别" required>
          <el-radio-group v-model="elderDialog.form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="elderDialog.form.age" :min="0" :max="120" /></el-form-item>
        <el-form-item label="健康等级">
          <el-select v-model="elderDialog.form.healthLevel" placeholder="请选择" style="width:160px">
            <el-option :value="1" label="自理" />
            <el-option :value="2" label="半失能" />
            <el-option :value="3" label="失能" />
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号"><el-input v-model="elderDialog.form.idCard" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="elderDialog.form.phone" /></el-form-item>
        <el-form-item label="紧急联系人"><el-input v-model="elderDialog.form.emergencyContact" /></el-form-item>
        <el-form-item label="紧急电话"><el-input v-model="elderDialog.form.emergencyPhone" /></el-form-item>
        <el-form-item label="家庭住址"><el-input v-model="elderDialog.form.address" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="elderDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="elderDialog.loading" @click="submitElder">保存</el-button>
      </template>
    </el-dialog>

    <!-- 健康记录 新增 -->
    <el-dialog title="添加健康记录" v-model="recordDialog.visible" width="480px" @closed="resetRecordForm">
      <el-form :model="recordDialog.form" label-width="92px">
        <el-form-item label="记录类型" required>
          <el-select v-model="recordDialog.form.recordType" style="width:100%">
            <el-option v-for="t in recordTypes" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录值" required>
          <el-input v-model="recordDialog.form.recordValue" :placeholder="'如 ' + (recordTypeExample[recordDialog.form.recordType] || '数值')" />
        </el-form-item>
        <el-form-item label="记录时间">
          <el-date-picker v-model="recordDialog.form.recordTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="默认当前时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="recordDialog.form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="recordDialog.loading" @click="submitRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { DataLine, TrendCharts, Top, Bottom, MagicStick, InfoFilled, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { getMyElders, getElderArchive, getHealthRecords, getDiseases, getFamily, getHealthAdvice,
  addElder, updateElderApi, deleteElderApi, addHealthRecordApi, deleteHealthRecordApi } from '@/api'

const loading = ref(false)
const aiLoading = ref(false)
const aiAdvice = ref('')
const aiCached = ref(false)
const aiElderCount = ref(1)
const aiAdviceHtml = computed(() => {
  if (!aiAdvice.value) return ''
  return DOMPurify.sanitize(marked.parse(aiAdvice.value))
})
const elders = ref([])
const selectedElderId = ref(null)
const elderInfo = ref({})
const allRecords = ref([])
const family = ref([])
const diseases = ref([])
const activeTab = ref('basic')

const recordTypes = ['体重', '步数', '睡眠', '血糖', '血压', '血氧饱和度', '心率']
const recordType = ref('血压')
const unitMap = {
  '体重': 'kg', '步数': '步', '睡眠': '小时', '血糖': 'mmol/L',
  '血压': 'mmHg', '血氧饱和度': '%', '心率': '次/分'
}
const unit = computed(() => unitMap[recordType.value] || '')

const recordsByType = computed(() => allRecords.value.filter(r => r.recordType === recordType.value))

const stats = computed(() => {
  const list = recordsByType.value
  if (!list.length) return null
  const vals = list.map(i => parseFloat(i.recordValue)).filter(v => !isNaN(v))
  if (!vals.length) return null
  return {
    latest: list[0]?.recordValue ?? '-',
    avg: (vals.reduce((a, b) => a + b, 0) / vals.length).toFixed(1),
    max: Math.max(...vals).toFixed(1),
    min: Math.min(...vals).toFixed(1)
  }
})

const healthMeta = computed(() => {
  const map = { 1: { text: '自理', type: 'success' }, 2: { text: '半失能', type: 'warning' }, 3: { text: '失能', type: 'danger' } }
  return map[elderInfo.value.healthLevel] || { text: '未知', type: 'info' }
})

const disCategoryMap = {
  cardiovascular: '心血管疾病', diabetes: '糖尿病', respiratory: '呼吸系统疾病',
  neurological: '神经系统疾病', musculoskeletal: '骨关节疾病'
}

// 健康记录分页
const recPageNum = ref(1)
const recPageSize = ref(8)
const recPaged = computed(() => {
  const start = (recPageNum.value - 1) * recPageSize.value
  return allRecords.value.slice(start, start + recPageSize.value)
})

// 图表
const chartRef = ref(null)
let chartInstance = null
const renderChart = () => {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  const data = [...recordsByType.value].reverse()
  const dates = data.map(d => (d.recordTime || '').substring(5, 16) || '-')
  const values = data.map(d => parseFloat(d.recordValue) || 0)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30, color: '#606266', fontSize: 12 } },
    yAxis: { type: 'value', axisLabel: { color: '#606266', formatter: `{value}${unit.value}` } },
    series: [{
      name: recordType.value, type: 'line', smooth: true, data: values,
      lineStyle: { color: '#00C4A1', width: 3 }, itemStyle: { color: '#00C4A1' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(0,196,161,0.45)' }, { offset: 1, color: 'rgba(0,196,161,0.08)' }]) }
    }]
  })
}

watch(recordType, () => nextTick(renderChart))
watch(activeTab, t => { if (t === 'data') nextTick(renderChart) })

const loadElders = async () => {
  const res = await getMyElders()
  elders.value = res.data || []
  if (elders.value.length) {
    selectedElderId.value = elders.value[0].id
    await loadElderData(selectedElderId.value)
  }
}

const loadElderData = async (id) => {
  loading.value = true
  recPageNum.value = 1
  try {
    const [e, rec, fam] = await Promise.all([
      getElderArchive(id),
      getHealthRecords({ elderId: id, pageNum: 1, pageSize: 200 }),
      getFamily(id)
    ])
    elderInfo.value = e.data || {}
    allRecords.value = rec.data.records || []
    family.value = fam.data || []
  } catch (err) {
    ElMessage.error(err.message || '加载档案失败')
  } finally {
    loading.value = false
  }
}

const onElderChange = () => {
  aiAdvice.value = ''
  aiCached.value = false
  if (selectedElderId.value) loadElderData(selectedElderId.value)
}

const getAdvice = async (force = false) => {
  if (!selectedElderId.value) {
    ElMessage.warning('请先选择一位老人')
    return
  }
  aiLoading.value = true
  try {
    const res = await getHealthAdvice(selectedElderId.value, force)
    aiAdvice.value = res.data.advice
    aiElderCount.value = res.data.elderCount || 1
    aiCached.value = !!res.data.cached
  } catch (e) {
    ElMessage.error(e.message || '生成建议失败')
  } finally {
    aiLoading.value = false
  }
}

const loadDiseases = async () => {
  try { const res = await getDiseases(1); diseases.value = res.data || [] } catch (e) {}
}

// ===================== 自助维护：老人档案 =====================
const elderDialog = reactive({
  visible: false, loading: false, isEdit: false, id: null,
  form: { name: '', gender: null, age: null, idCard: '', phone: '', healthLevel: null, emergencyContact: '', emergencyPhone: '', address: '' }
})
const resetElderForm = () => {
  Object.assign(elderDialog.form, { name: '', gender: null, age: null, idCard: '', phone: '', healthLevel: null, emergencyContact: '', emergencyPhone: '', address: '' })
  elderDialog.isEdit = false
  elderDialog.id = null
}
const openElderAdd = () => { resetElderForm(); elderDialog.visible = true }
const openElderEdit = () => {
  if (!selectedElderId.value) return
  const e = elderInfo.value
  Object.assign(elderDialog.form, {
    name: e.name, gender: e.gender, age: e.age, idCard: e.idCard, phone: e.phone,
    healthLevel: e.healthLevel, emergencyContact: e.emergencyContact, emergencyPhone: e.emergencyPhone, address: e.address
  })
  elderDialog.isEdit = true
  elderDialog.id = selectedElderId.value
  elderDialog.visible = true
}
const submitElder = async () => {
  if (!elderDialog.form.name || elderDialog.form.gender == null) {
    ElMessage.warning('请填写老人姓名和性别')
    return
  }
  elderDialog.loading = true
  try {
    const payload = { ...elderDialog.form }
    if (elderDialog.isEdit) {
      await updateElderApi(elderDialog.id, payload)
      ElMessage.success('档案已更新')
    } else {
      await addElder(payload)
      ElMessage.success('档案已添加')
    }
    elderDialog.visible = false
    await loadElders()
    if (elderDialog.isEdit && selectedElderId.value) await loadElderData(selectedElderId.value)
    aiAdvice.value = ''
    aiCached.value = false
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    elderDialog.loading = false
  }
}
const deleteElder = async () => {
  if (!selectedElderId.value) return
  try {
    await ElMessageBox.confirm('删除后该老人的档案与健康记录将一并清除，确定删除？', '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await deleteElderApi(selectedElderId.value)
    ElMessage.success('已删除')
    elders.value = []
    selectedElderId.value = null
    elderInfo.value = {}
    allRecords.value = []
    aiAdvice.value = ''
    aiCached.value = false
    await loadElders()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

// ===================== 自助维护：健康记录 =====================
const recordDialog = reactive({ visible: false, loading: false, form: { recordType: '血压', recordValue: '', recordTime: '', remark: '' } })
const recordTypeExample = { '体重': '65.5', '步数': '3000', '睡眠': '7.5', '血糖': '6.8', '血压': '140/88', '血氧饱和度': '98', '心率': '75' }
const resetRecordForm = () => {
  Object.assign(recordDialog.form, { recordType: recordType.value || '血压', recordValue: '', recordTime: '', remark: '' })
}
const openRecordAdd = () => { resetRecordForm(); recordDialog.visible = true }
const submitRecord = async () => {
  if (!selectedElderId.value) {
    ElMessage.warning('请先选择一位老人')
    return
  }
  if (!recordDialog.form.recordType || !recordDialog.form.recordValue) {
    ElMessage.warning('请填写记录类型和记录值')
    return
  }
  recordDialog.loading = true
  try {
    const payload = {
      elderId: selectedElderId.value,
      recordType: recordDialog.form.recordType,
      recordValue: recordDialog.form.recordValue,
      remark: recordDialog.form.remark || ''
    }
    if (recordDialog.form.recordTime) payload.recordTime = recordDialog.form.recordTime
    await addHealthRecordApi(payload)
    ElMessage.success('记录已添加')
    recordDialog.visible = false
    recPageNum.value = 1
    await loadElderData(selectedElderId.value)
    aiAdvice.value = ''
    aiCached.value = false
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  } finally {
    recordDialog.loading = false
  }
}
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这条健康记录？', '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await deleteHealthRecordApi(row.id)
    ElMessage.success('已删除')
    await loadElderData(selectedElderId.value)
    aiAdvice.value = ''
    aiCached.value = false
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(async () => {
  await loadDiseases()
  await loadElders()
  await nextTick()
  renderChart()
})
</script>

<style scoped>
.health-archive { padding: 4px; }
.elder-switch { margin-bottom: 16px; }
.archive-toolbar { margin-bottom: 16px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.archive-toolbar .toolbar-tip { color: #909399; font-size: 13px; }
.tab-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; flex-wrap: wrap; }
.tab-toolbar .toolbar-tip { color: #909399; font-size: 13px; }
.switch-label { color: #606266; font-size: 14px; margin-right: 8px; }
.profile { text-align: center; }
.profile h3 { margin: 4px 0; font-size: 18px; }
.profile .sub { color: #A0AEC0; font-size: 14px; margin: 4px 0; }
.info-item { display: flex; flex-direction: column; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.info-item:last-child { border-bottom: none; }
.info-item .label { font-size: 12px; color: #A0AEC0; margin-bottom: 2px; }

.type-tabs { margin-bottom: 16px; }
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 14px; padding: 16px; background: #f8faf9; border-radius: 8px; }
.stat-icon { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-icon.latest { background: rgba(0,196,161,0.1); color: #00C4A1; }
.stat-icon.avg { background: rgba(64,158,255,0.1); color: #409EFF; }
.stat-icon.max { background: rgba(255,77,79,0.1); color: #FF4D4F; }
.stat-icon.min { background: rgba(250,173,20,0.1); color: #FAAD14; }
.stat-info { flex: 1; }
.stat-label { font-size: 12px; color: #A0AEC0; margin-bottom: 4px; }
.stat-value { font-size: 22px; font-weight: bold; color: #303133; }
.stat-unit { font-size: 13px; color: #A0AEC0; font-weight: normal; margin-left: 3px; }
.chart-card { padding: 16px; background: #fff; border-radius: 8px; border: 1px solid #ebeef5; }
.chart-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.chart-container { height: 300px; }
.empty-tip { color: #909399; font-size: 13px; }

.ai-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 4px; }
.ai-tip { color: #909399; font-size: 13px; display: inline-flex; align-items: center; gap: 4px; }
.ai-tip-icon { font-size: 14px; }
.ai-cache-hint { margin-top: 12px; }
.ai-result { margin-top: 16px; background: #f9fffd; border: 1px solid #e0f5f0; }
.ai-result-title { font-size: 16px; font-weight: 600; color: #00C4A1; margin-bottom: 16px; display: flex; align-items: center; gap: 6px; }
</style>

<style>
/* Markdown 渲染样式（作用于 v-html 内容，不能 scoped） */
.ai-result-body {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
}
.ai-result-body p {
  margin: 0 0 12px 0;
}
.ai-result-body h1, .ai-result-body h2, .ai-result-body h3, .ai-result-body h4 {
  color: #00C4A1;
  margin: 16px 0 10px 0;
  font-weight: 600;
}
.ai-result-body h1 { font-size: 18px; }
.ai-result-body h2 { font-size: 17px; }
.ai-result-body h3 { font-size: 16px; }
.ai-result-body strong {
  color: #00C4A1;
  font-weight: 600;
}
.ai-result-body ul, .ai-result-body ol {
  margin: 8px 0 14px 0;
  padding-left: 22px;
}
.ai-result-body li {
  margin-bottom: 8px;
}
.ai-result-body blockquote {
  border-left: 4px solid #00C4A1;
  background: #f0faf8;
  margin: 12px 0;
  padding: 10px 14px;
  color: #606266;
}
.ai-result-body code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
}
.ai-result-body pre {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}
.ai-result-body hr {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 16px 0;
}
</style>
