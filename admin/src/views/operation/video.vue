<template>
  <div class="video-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>视频管理</span>
          <div class="type-tabs">
            <el-radio-group v-model="tabType" @change="loadData">
              <el-radio-button value="all">全部视频</el-radio-button>
              <el-radio-button value="course">健康课程</el-radio-button>
              <el-radio-button value="activity">活动视频</el-radio-button>
              <el-radio-button value="other">其他</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" @click="openDialog()">上传视频</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="视频名称">
          <el-input v-model="search.name" placeholder="请输入" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="视频分类">
          <el-select v-model="search.category" placeholder="请选择" clearable style="width:120px">
            <el-option value="course">健康课程</el-option>
            <el-option value="activity">活动视频</el-option>
            <el-option value="other">其他</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" placeholder="请选择" clearable style="width:120px">
            <el-option value="1">发布</el-option>
            <el-option value="0">草稿</el-option>
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
        <div class="video-grid" v-else>
          <div class="video-card" v-for="item in list" :key="item.id">
            <div class="video-cover">
              <el-image :src="item.cover" fit="cover" class="cover-image" v-if="item.cover" />
              <div class="cover-image cover-image--ph" v-else></div>
              <div class="play-btn" @click="playVideo(item)">
                <el-icon :size="40" name="Play" />
              </div>
              <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small" class="status-tag">{{ item.status === 1 ? '已发布' : '草稿' }}</el-tag>
              <span class="duration-tag" v-if="item.duration">{{ item.duration }}</span>
            </div>
            <div class="video-info">
              <div class="video-title">{{ item.title }}</div>
              <div class="video-meta">
                <span class="meta-item"><el-icon :size="12" name="View" /> {{ item.viewCount || 0 }}</span>
                <span class="meta-item"><el-icon :size="12" name="Star" /> {{ item.likeCount || 0 }}</span>
              </div>
              <div class="video-desc">{{ item.description || '—' }}</div>
              <div class="action-bar">
                <el-button type="primary" size="small" @click="playVideo(item)">播放</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑视频' : '上传视频'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="视频分类" prop="category">
          <el-select v-model="form.category" style="width:100%">
            <el-option value="course">健康课程</el-option>
            <el-option value="activity">活动视频</el-option>
            <el-option value="other">其他</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="视频文件">
          <el-upload action="#" :file-list="form.videoFileList" :auto-upload="false">
            <el-button type="primary">选择视频</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload action="#" :file-list="form.coverFileList" :auto-upload="false" list-type="picture-card">
            <el-icon :size="32" name="Plus" />
          </el-upload>
        </el-form-item>
        <el-form-item label="视频描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="btnLoading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="playVisible" title="视频播放" width="700px" :close-on-click-modal="false">
      <div class="video-player">
        <el-image :src="playData?.cover || ''" fit="cover" class="player-cover" v-if="playData?.cover" />
        <div class="player-overlay">
          <el-icon :size="64" name="Play" style="color:#fff" />
        </div>
        <div class="player-info">
          <div class="player-title">{{ playData?.title }}</div>
          <div class="player-meta">
            <span>{{ playData?.viewCount || 0 }}次观看</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVideoPage, deleteVideo } from '@/api'

const tabType = ref('all')
const loading = ref(false)
const btnLoading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(9)
const total = ref(0)
const dialogVisible = ref(false)
const playVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const playData = ref({})

const search = reactive({
  name: '',
  category: '',
  status: ''
})

const form = reactive({
  id: null,
  title: '',
  category: 'course',
  videoFileList: [],
  coverFileList: [],
  description: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择视频分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入视频描述', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (tabType.value !== 'all') params.category = tabType.value
    if (search.name) params.name = search.name
    if (search.category) params.category = search.category
    if (search.status) params.status = Number(search.status)
    const res = await getVideoPage(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { loading.value = false }
}

const reset = () => {
  search.name = ''
  search.category = ''
  search.status = ''
  pageNum.value = 1
  loadData()
}

const openDialog = () => {
  isEdit.value = false
  formRef.value?.resetFields()
  Object.assign(form, { id: null, title: '', category: 'course', videoFileList: [], coverFileList: [], description: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (item) => {
  isEdit.value = true
  Object.assign(form, { id: item.id, title: item.title, category: item.category, videoFileList: [], coverFileList: [], description: item.description || '', status: item.status === undefined ? 1 : item.status })
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  btnLoading.value = true
  try {
    // TODO: 后端视频保存需配合文件/封面上传，待接入
    ElMessage.info('视频保存功能待接入后端')
    dialogVisible.value = false
  } catch (e) { /* 响应拦截器已提示错误 */ }
  finally { btnLoading.value = false }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该视频？', '提示', { type: 'warning' })
    await deleteVideo(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const playVideo = (item) => {
  playData.value = item
  playVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.video-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.type-tabs { display: flex; gap: 0; }
.search-form { margin-bottom: 20px; }
.empty-wrap { padding: 40px 0; }

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.video-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.video-cover {
  position: relative;
  width: 100%;
  height: 160px;
}

.cover-image {
  width: 100%;
  height: 100%;
}
.cover-image--ph { background: #f4f6f8; }

.play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  padding: 12px;
  cursor: pointer;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-tag {
  position: absolute;
  top: 8px;
  left: 8px;
}

.duration-tag {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.video-info { padding: 12px; }

.video-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #A0AEC0;
}

.video-desc {
  font-size: 12px;
  color: #A0AEC0;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.action-bar {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.video-player {
  position: relative;
  width: 100%;
  height: 380px;
}

.player-cover {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.player-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  padding: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.player-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  border-radius: 0 0 8px 8px;
}

.player-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.player-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}
</style>
