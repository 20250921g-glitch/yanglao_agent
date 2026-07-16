<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="page-header">
        <div>
          <h2 class="page-title">会话管理</h2>
          <span class="page-sub">客服与用户的对话记录管理</span>
        </div>
        <el-input v-model="keyword" placeholder="搜索用户/手机号/消息" clearable style="width: 240px" @clear="load" @keyup.enter="load" />
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column label="用户" min-width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.userAvatar">{{ row.userName?.charAt(0) }}</el-avatar>
              <div>
                <div class="strong">{{ row.userName }}</div>
                <div class="muted">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastMessage" label="最近消息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="msgCount" label="消息数" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '进行中' : '已结束' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openPreview(row)">查看</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" :current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="handlePage" />
    </el-card>

    <el-drawer v-model="drawer" :title="`与 ${current.userName || ''} 的会话`" size="440px">
      <div class="chat-wrap">
        <div v-loading="msgLoading" class="chat-box">
          <div v-for="(m, i) in current.messages" :key="i" :class="['bubble', m.from === 'user' ? 'left' : 'right']">
            <div class="bubble-inner">{{ m.text }}</div>
            <div class="bubble-time">{{ m.time }}</div>
          </div>
          <el-empty v-if="!msgLoading && (!current.messages || current.messages.length === 0)" description="暂无消息" />
        </div>
        <div class="chat-input">
          <el-input v-model="draft" type="textarea" :rows="2" resize="none"
            placeholder="输入回复内容，Enter 发送 / Shift+Enter 换行"
            @keydown.enter.exact.prevent="send" />
          <el-button type="primary" :loading="sending" @click="send">发送</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConversationPage, getConversationMessages, sendConversationMessage, deleteConversation } from '@/api/index'

const loading = ref(false)
const drawer = ref(false)
const msgLoading = ref(false)
const sending = ref(false)
const draft = ref('')
const current = ref({})
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const load = async () => {
  loading.value = true
  try {
    const res = await getConversationPage({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    if (res.code === 200) {
      list.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally { loading.value = false }
}
const handlePage = (p) => { pageNum.value = p; load() }

const openPreview = async (row) => {
  current.value = { ...row, messages: [] }
  draft.value = ''
  drawer.value = true
  msgLoading.value = true
  try {
    const res = await getConversationMessages(row.id)
    if (res.code === 200) current.value.messages = res.data || []
  } finally { msgLoading.value = false }
}

const send = async () => {
  const text = draft.value.trim()
  if (!text) return
  sending.value = true
  try {
    const res = await sendConversationMessage(current.value.id, { content: text })
    if (res.code === 200) {
      draft.value = ''
      const r = await getConversationMessages(current.value.id)
      if (r.code === 200) current.value.messages = r.data || []
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } finally { sending.value = false }
}

const remove = (row) => {
  ElMessageBox.confirm(`确定删除与「${row.userName}」的会话？`, '提示', { type: 'warning' }).then(async () => {
    const res = await deleteConversation(row.id)
    if (res.code === 200) { ElMessage.success('已删除'); load() }
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; font-weight: 600; color: #303133; }
.page-sub { font-size: 13px; color: #A0AEC0; margin-left: 8px; }
.user-cell { display: flex; align-items: center; gap: 10px; }
.strong { font-weight: 600; color: #303133; }
.muted { color: #A0AEC0; font-size: 12px; }
.pager { margin-top: 16px; justify-content: flex-end; }
:deep(.el-drawer__body) { display: flex; flex-direction: column; padding: 0; }
.chat-wrap { flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 16px; }
.chat-box { flex: 1; overflow: auto; display: flex; flex-direction: column; gap: 14px; padding: 8px; }
.bubble { display: flex; flex-direction: column; max-width: 75%; }
.bubble.left { align-self: flex-start; }
.bubble.right { align-self: flex-end; align-items: flex-end; }
.bubble-inner { padding: 10px 14px; border-radius: 10px; font-size: 14px; line-height: 1.5; background: #f4f4f5; color: #303133; word-break: break-all; }
.bubble.right .bubble-inner { background: #00C4A1; color: #fff; }
.bubble-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
.chat-input { display: flex; gap: 8px; padding-top: 12px; border-top: 1px solid #f0f0f0; align-items: flex-end; }
</style>
