<template>
  <div class="msg-page">
    <div class="msg-toolbar">
      <el-radio-group v-model="typeFilter" @change="reload">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="t in typeOptions" :key="t" :label="t">{{ t }}</el-radio-button>
      </el-radio-group>
      <div class="toolbar-right">
        <span class="unread-tip" v-if="unread > 0">{{ unread }} 条未读</span>
        <el-button size="small" :disabled="unread === 0" @click="markAll">全部已读</el-button>
      </div>
    </div>

    <div v-loading="loading" class="msg-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无消息" />
      <div
        v-for="m in list" :key="m.id"
        class="msg-item" :class="{ unread: !m.read }"
        @click="openMsg(m)">
        <div class="msg-dot" :class="{ show: !m.read }"></div>
        <div class="msg-icon" :style="{ background: typeColor[m.type] || '#00C4A1' }">
          {{ (m.type || '通').charAt(0) }}
        </div>
        <div class="msg-main">
          <div class="msg-head">
            <span class="msg-title">{{ m.title }}</span>
            <el-tag size="small" effect="plain">{{ m.type || '通知' }}</el-tag>
          </div>
          <div class="msg-content">{{ m.content }}</div>
          <div class="msg-time">{{ fmt(m.sendTime) }}</div>
        </div>
        <el-button class="msg-del" link type="danger" @click.stop="del(m)">删除</el-button>
      </div>
    </div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="pageNum" v-model:page-size="pageSize"
      :total="total" layout="prev,pager,next"
      style="margin-top:16px;justify-content:flex-end" @change="load" />

    <!-- 消息详情 -->
    <el-dialog v-model="detailVisible" :title="detail ? detail.title : '消息详情'" width="480px">
      <div v-if="detail">
        <div class="detail-meta">
          <el-tag size="small" effect="plain">{{ detail.type || '通知' }}</el-tag>
          <span class="detail-time">{{ fmt(detail.sendTime) }}</span>
        </div>
        <div class="detail-content">{{ detail.content }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMessageList, getUnreadCount, readMessage, readAllMessages, deleteMessage
} from '@/api'

const typeOptions = ['系统通知', '活动推送', '服务提醒']
const typeColor = { '系统通知': '#00C4A1', '活动推送': '#E6A23C', '服务提醒': '#409EFF' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const typeFilter = ref('')
const unread = ref(0)

const detailVisible = ref(false)
const detail = ref(null)

const fmt = (d) => {
  if (!d) return ''
  return String(d).replace('T', ' ').substring(0, 16)
}

const load = async () => {
  loading.value = true
  try {
    const res = await getMessageList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: typeFilter.value || undefined
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载消息失败')
  } finally {
    loading.value = false
  }
}

const loadUnread = async () => {
  try {
    const res = await getUnreadCount()
    unread.value = res.data || 0
  } catch (e) { /* 忽略 */ }
}

const reload = () => {
  pageNum.value = 1
  load()
}

const openMsg = async (m) => {
  detail.value = m
  detailVisible.value = true
  if (!m.read) {
    try {
      await readMessage(m.id)
      m.read = true
      loadUnread()
    } catch (e) { /* 忽略 */ }
  }
}

const markAll = async () => {
  try {
    await readAllMessages()
    list.value.forEach(m => { m.read = true })
    unread.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const del = async (m) => {
  await ElMessageBox.confirm('确认删除这条消息？', '提示', { type: 'warning' })
  try {
    await deleteMessage(m.id)
    ElMessage.success('已删除')
    load()
    loadUnread()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  load()
  loadUnread()
})
</script>

<style scoped>
.msg-page { padding: 4px; }
.msg-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; flex-wrap: wrap; gap: 8px; }
.toolbar-right { display: flex; align-items: center; gap: 12px; }
.unread-tip { font-size: 13px; color: #F56C6C; }
.msg-list { min-height: 120px; }
.msg-item { position: relative; display: flex; align-items: flex-start; gap: 12px; padding: 14px 16px; background: #fff; border: 1px solid #ebeef5; border-radius: 10px; margin-bottom: 10px; cursor: pointer; transition: box-shadow .2s; }
.msg-item:hover { box-shadow: 0 2px 12px rgba(0,0,0,.08); }
.msg-item.unread { background: #f6fffd; border-color: #b8ece2; }
.msg-dot { width: 8px; height: 8px; border-radius: 50%; margin-top: 18px; background: transparent; flex-shrink: 0; }
.msg-dot.show { background: #F56C6C; }
.msg-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 18px; flex-shrink: 0; }
.msg-main { flex: 1; min-width: 0; }
.msg-head { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.msg-title { font-size: 15px; font-weight: 600; color: #303133; }
.msg-content { font-size: 13px; color: #606266; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.msg-time { font-size: 12px; color: #c0c4cc; margin-top: 6px; }
.msg-del { flex-shrink: 0; }
.detail-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.detail-time { font-size: 12px; color: #c0c4cc; }
.detail-content { font-size: 14px; color: #303133; line-height: 1.7; }
</style>
