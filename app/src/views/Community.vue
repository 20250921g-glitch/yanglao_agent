<template>
  <div class="community">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- ===== 邻里圈信息流 ===== -->
      <el-tab-pane label="邻里圈" name="feed">
        <div class="toolbar">
          <el-input
            v-model="keyword"
            placeholder="搜索话题或内容"
            clearable
            style="width:240px"
            @keyup.enter="loadFeed"
            @clear="loadFeed"
          >
            <template #suffix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="openPublish">发布动态</el-button>
        </div>

        <div v-loading="loading" class="feed">
          <el-empty v-if="!loading && list.length === 0" description="暂无动态，快来发布第一条吧" />
          <div v-for="d in list" :key="d.id" class="post">
            <div class="post-head">
              <el-avatar>{{ (d.userName || '匿').charAt(0) }}</el-avatar>
              <div class="post-meta">
                <div class="post-name">{{ d.userName || '匿名用户' }}</div>
                <div class="post-time">{{ d.createTime }}</div>
              </div>
              <el-tag v-if="d.topic" size="small" type="info" effect="plain">{{ d.topic }}</el-tag>
            </div>
            <div class="post-content">{{ d.content }}</div>
            <div v-if="d.images" class="post-imgs">
              <el-image
                v-for="(img, i) in (d.images || '').split(',').filter(Boolean)"
                :key="i"
                :src="img"
                :preview-src-list="(d.images || '').split(',').filter(Boolean)"
                fit="cover"
                class="post-img"
              />
            </div>
            <div class="post-actions">
              <span class="act" :class="{ on: d.liked, disabled: liking[d.id] }" @click="toggleLike(d)">
                <el-icon><Pointer /></el-icon> {{ d.likeCount }}
              </span>
              <span class="act" :class="{ on: d.favorited, disabled: faving[d.id] }" @click="toggleFav(d)">
                <el-icon><Star /></el-icon> {{ d.collectCount }}
              </span>
              <span class="act" @click="openDetail(d)">
                <el-icon><ChatDotRound /></el-icon> {{ d.commentCount }}
              </span>
              <span class="act" @click="doShare(d)">
                <el-icon><Share /></el-icon> {{ d.shareCount }}
              </span>
            </div>
          </div>
        </div>

        <el-pagination
          v-if="total > pageSize"
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          style="justify-content:flex-end;margin-top:16px"
          @current-change="loadFeed"
        />
      </el-tab-pane>

      <!-- ===== 我的发布 ===== -->
      <el-tab-pane label="我的发布" name="mine">
        <div class="toolbar">
          <el-button type="primary" @click="openPublish">发布动态</el-button>
        </div>
        <div v-loading="myLoading" class="feed">
          <el-empty v-if="!myLoading && myList.length === 0" description="你还没有发布过动态" />
          <div v-for="d in myList" :key="d.id" class="post">
            <div class="post-head">
              <el-avatar>{{ (d.userName || '匿').charAt(0) }}</el-avatar>
              <div class="post-meta">
                <div class="post-name">{{ d.userName || '匿名用户' }}</div>
                <div class="post-time">{{ d.createTime }}</div>
              </div>
              <el-tag v-if="d.topic" size="small" type="info" effect="plain">{{ d.topic }}</el-tag>
              <el-tag :type="statusTag[d.status]" size="small" class="ml">{{ statusText[d.status] }}</el-tag>
            </div>
            <div class="post-content">{{ d.content }}</div>
            <div class="post-actions">
              <span class="act"><el-icon><Pointer /></el-icon> {{ d.likeCount || 0 }}</span>
              <span class="act"><el-icon><Star /></el-icon> {{ d.collectCount || 0 }}</span>
              <span class="act"><el-icon><ChatDotRound /></el-icon> {{ d.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 发布弹窗 -->
    <el-dialog v-model="publishVisible" title="发布动态" width="560px">
      <el-form :model="form" label-width="72px">
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="分享你的养老生活、活动感悟…" />
        </el-form-item>
        <el-form-item label="话题">
          <el-input v-model="form.topic" placeholder="如：健康生活 / 社区活动" />
        </el-form-item>
        <el-form-item label="图片链接">
          <el-input v-model="form.images" placeholder="可填图片URL，多个用逗号分隔（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="submitPublish">发布</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗（含评论） -->
    <el-dialog v-model="detailVisible" title="动态详情" width="600px">
      <div v-if="detail" v-loading="detailLoading">
        <div class="post-head">
          <el-avatar>{{ (detail.userName || '匿').charAt(0) }}</el-avatar>
          <div class="post-meta">
            <div class="post-name">{{ detail.userName || '匿名用户' }}</div>
            <div class="post-time">{{ detail.createTime }}</div>
          </div>
          <el-tag v-if="detail.topic" size="small" type="info" effect="plain">{{ detail.topic }}</el-tag>
        </div>
        <div class="post-content">{{ detail.content }}</div>
        <div v-if="detail.images" class="post-imgs">
          <el-image
            v-for="(img, i) in (detail.images || '').split(',').filter(Boolean)"
            :key="i" :src="img"
            :preview-src-list="(detail.images || '').split(',').filter(Boolean)"
            fit="cover" class="post-img"
          />
        </div>
        <div class="post-actions">
          <span class="act" :class="{ on: detail.liked, disabled: liking[detail.id] }" @click="toggleLike(detail, true)">
            <el-icon><Pointer /></el-icon> {{ detail.likeCount }}
          </span>
          <span class="act" :class="{ on: detail.favorited, disabled: faving[detail.id] }" @click="toggleFav(detail, true)">
            <el-icon><Star /></el-icon> {{ detail.collectCount }}
          </span>
          <span class="act" @click="doShare(detail, true)">
            <el-icon><Share /></el-icon> {{ detail.shareCount }}
          </span>
        </div>

        <el-divider>评论 ({{ detail.comments ? detail.comments.length : 0 }})</el-divider>
        <div v-if="detail.comments && detail.comments.length" class="comment-list">
          <div v-for="c in detail.comments" :key="c.id" class="comment">
            <el-avatar :size="28">{{ (c.userName || '匿').charAt(0) }}</el-avatar>
            <div class="c-body">
              <div class="c-name">{{ c.userName || '匿名用户' }}</div>
              <div class="c-text">{{ c.content }}</div>
              <div class="c-time">{{ c.createTime }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="还没有评论，抢沙发吧" :image-size="60" />
        <div class="comment-input">
          <el-input v-model="commentText" placeholder="说点什么…" @keyup.enter="submitComment">
            <template #append>
              <el-button @click="submitComment">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Pointer, Star, ChatDotRound, Share } from '@element-plus/icons-vue'
import {
  getDynamicList, getDynamicDetail, publishDynamic,
  likeDynamic, favoriteDynamic, commentDynamic, myDynamics, shareDynamic
} from '@/api'

const activeTab = ref('feed')
const keyword = ref('')
const loading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const myLoading = ref(false)
const myList = ref([])

const publishVisible = ref(false)
const publishing = ref(false)
const form = reactive({ content: '', topic: '', images: '' })

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const commentText = ref('')

// 点赞/收藏请求锁，防止快速点击导致并发重复提交（同标签页）
const liking = reactive({})
const faving = reactive({})

// ===== 跨标签页点赞/收藏防重 + 状态同步 =====
// 同一用户可能开了多个标签页，若各标签页独立发 toggle 请求，后端虽由 opLocks
// 保证 DB 正确，但各标签页 UI 可能短暂不一致。这里借助 localStorage 的短时锁
// 避免多个标签页同时发起同一动态的操作，并通过 storage 事件把操作结果广播给
// 其他标签页，使其 UI 收敛到后端真实状态。
const TAB_ID = String(Date.now()) + '-' + Math.random().toString(16).slice(2)
const LOCK_PREFIX = 'dynOp:lock:'
const STATE_PREFIX = 'dynOp:state:'
const LOCK_TTL = 5000

const tryCrossTabLock = (dynId, kind) => {
  const key = LOCK_PREFIX + kind + ':' + dynId
  try {
    const raw = localStorage.getItem(key)
    if (raw) {
      const obj = JSON.parse(raw)
      if (obj.tabId !== TAB_ID && Date.now() - obj.ts < LOCK_TTL) return false
    }
    localStorage.setItem(key, JSON.stringify({ tabId: TAB_ID, ts: Date.now() }))
    return true
  } catch (e) {
    return true
  }
}
const releaseCrossTabLock = (dynId, kind) => {
  const key = LOCK_PREFIX + kind + ':' + dynId
  try {
    const raw = localStorage.getItem(key)
    if (raw) {
      const obj = JSON.parse(raw)
      if (obj.tabId === TAB_ID) localStorage.removeItem(key)
    }
  } catch (e) { /* ignore */ }
}
const broadcastState = (d) => {
  try {
    localStorage.setItem(STATE_PREFIX + d.id, JSON.stringify({
      liked: d.liked, likeCount: d.likeCount,
      favorited: d.favorited, collectCount: d.collectCount,
      ts: Date.now(), tabId: TAB_ID
    }))
  } catch (e) { /* ignore */ }
}
const applyRemoteState = (dynId, st) => {
  const sync = (arr) => {
    const t = (arr || []).find(x => x.id === dynId)
    if (!t) return
    if (typeof st.liked === 'boolean') t.liked = st.liked
    if (typeof st.likeCount === 'number') t.likeCount = st.likeCount
    if (typeof st.favorited === 'boolean') t.favorited = st.favorited
    if (typeof st.collectCount === 'number') t.collectCount = st.collectCount
  }
  sync(list.value)
  sync(myList.value)
  if (detail.value && detail.value.id === dynId) {
    if (typeof st.liked === 'boolean') detail.value.liked = st.liked
    if (typeof st.likeCount === 'number') detail.value.likeCount = st.likeCount
    if (typeof st.favorited === 'boolean') detail.value.favorited = st.favorited
    if (typeof st.collectCount === 'number') detail.value.collectCount = st.collectCount
  }
}
const adoptLocalState = (d) => {
  try {
    const raw = localStorage.getItem(STATE_PREFIX + d.id)
    if (!raw) return
    const st = JSON.parse(raw)
    if (typeof st.liked === 'boolean') d.liked = st.liked
    if (typeof st.likeCount === 'number') d.likeCount = st.likeCount
    if (typeof st.favorited === 'boolean') d.favorited = st.favorited
    if (typeof st.collectCount === 'number') d.collectCount = st.collectCount
  } catch (e) { /* ignore */ }
}
const onStorage = (e) => {
  if (!e.key || !e.key.startsWith(STATE_PREFIX)) return
  try {
    const dynId = Number(e.key.slice(STATE_PREFIX.length))
    if (!e.newValue) return
    const st = JSON.parse(e.newValue)
    if (st.tabId === TAB_ID) return
    applyRemoteState(dynId, st)
  } catch (err) { /* ignore */ }
}

const statusText = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已下架' }
const statusTag = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }

const loadFeed = async () => {
  loading.value = true
  try {
    const res = await getDynamicList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadMine = async () => {
  myLoading.value = true
  try {
    const res = await myDynamics({ pageNum: 1, pageSize: 50 })
    myList.value = res.data.records || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    myLoading.value = false
  }
}

const onTabChange = (name) => {
  if (name === 'mine') loadMine()
}

const openPublish = () => {
  form.content = ''
  form.topic = ''
  form.images = ''
  publishVisible.value = true
}

const submitPublish = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  publishing.value = true
  try {
    await publishDynamic({ ...form })
    ElMessage.success('发布成功，等待管理员审核')
    publishVisible.value = false
    await loadMine()
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const toggleLike = async (d, isDetail = false) => {
  if (liking[d.id]) return
  if (!tryCrossTabLock(d.id, 'like')) {
    // 其他标签页正在操作同一动态，直接同步其最新状态，避免本页 UI 陈旧
    adoptLocalState(d)
    return
  }
  liking[d.id] = true
  try {
    const res = await likeDynamic(d.id)
    d.liked = res.data.liked
    d.likeCount = res.data.likeCount
    if (isDetail && detail.value) detail.value.likeCount = res.data.likeCount
    broadcastState(d)
  } catch (e) {
    ElMessage.error(e.message || '点赞失败')
  } finally {
    liking[d.id] = false
    releaseCrossTabLock(d.id, 'like')
  }
}

const toggleFav = async (d, isDetail = false) => {
  if (faving[d.id]) return
  if (!tryCrossTabLock(d.id, 'fav')) {
    adoptLocalState(d)
    return
  }
  faving[d.id] = true
  try {
    const res = await favoriteDynamic(d.id)
    d.favorited = res.data.favorited
    d.collectCount = res.data.collectCount
    if (isDetail && detail.value) detail.value.collectCount = res.data.collectCount
    broadcastState(d)
  } catch (e) {
    ElMessage.error(e.message || '收藏失败')
  } finally {
    faving[d.id] = false
    releaseCrossTabLock(d.id, 'fav')
  }
}

const doShare = async (d, isDetail = false) => {
  const res = await shareDynamic(d.id)
  d.shareCount = res.data.shareCount
  if (isDetail && detail.value) detail.value.shareCount = res.data.shareCount
  ElMessage.success('已分享')
}

const openDetail = async (d) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await getDynamicDetail(d.id)
    detail.value = res.data
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    detailLoading.value = false
  }
}

const submitComment = async () => {
  if (!commentText.value.trim() || !detail.value) return
  try {
    await commentDynamic(detail.value.id, commentText.value.trim())
    commentText.value = ''
    // 刷新详情
    const res = await getDynamicDetail(detail.value.id)
    detail.value = res.data
    // 同步列表计数
    const t = list.value.find(x => x.id === detail.value.id)
    if (t) t.commentCount = detail.value.commentCount
    ElMessage.success('评论成功')
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  }
}

onMounted(() => {
  loadFeed()
  window.addEventListener('storage', onStorage)
})
onUnmounted(() => {
  window.removeEventListener('storage', onStorage)
})
</script>

<style scoped>
.community { padding: 4px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.feed { display: flex; flex-direction: column; gap: 14px; min-height: 120px; }
.post {
  border: 1px solid #ebeef5; border-radius: 10px; padding: 14px 16px; background: #fff;
}
.post-head { display: flex; align-items: center; gap: 10px; }
.post-meta { flex: 1; }
.post-name { font-weight: 600; color: #303133; }
.post-time { font-size: 12px; color: #909399; margin-top: 2px; }
.post-content { margin: 10px 0; color: #303133; line-height: 1.7; white-space: pre-wrap; }
.post-imgs { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.post-img { width: 110px; height: 110px; border-radius: 8px; }
.post-actions { display: flex; gap: 22px; color: #909399; font-size: 14px; }
.post-actions .act { display: inline-flex; align-items: center; gap: 4px; cursor: pointer; transition: color .2s; }
.post-actions .act:hover { color: #00C4A1; }
.post-actions .act.on { color: #00C4A1; font-weight: 600; }
.post-actions .act.disabled { opacity: 0.5; cursor: not-allowed; pointer-events: none; }
.ml { margin-left: auto; }
.comment-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 12px; }
.comment { display: flex; gap: 8px; }
.c-body { flex: 1; background: #f7f8fa; border-radius: 8px; padding: 8px 10px; }
.c-name { font-size: 13px; font-weight: 600; color: #303133; }
.c-text { font-size: 14px; color: #606266; margin: 2px 0; }
.c-time { font-size: 12px; color: #c0c4cc; }
.comment-input { margin-top: 8px; }
</style>
