<template>
  <div class="materials">
    <H5NavBar title="招宣资料" />

    <div class="search-box">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
      </svg>
      <input v-model="searchKey" placeholder="请输入活动名称模糊查询" />
    </div>

    <div v-if="canUpload" class="upload-card">
      <div class="upload-title">上传招宣资料</div>
      <input v-model="uploadForm.name" class="form-control" placeholder="资料名称，如：招生简章" />
      <select v-model="uploadForm.category" class="form-control">
        <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
      </select>
      <textarea v-model="uploadForm.description" class="form-control textarea" placeholder="资料说明，可填写适用对象、使用场景等"></textarea>
      <input class="file-control" type="file" @change="handleFileChange" />
      <button class="upload-button" :disabled="uploading" @click="submitMaterialUpload">
        {{ uploading ? '上传中...' : '确认上传' }}
      </button>
    </div>

    <div v-if="loading" class="state-tip">资料加载中...</div>

    <div v-else-if="filteredFiles.length === 0" class="state-tip">
      {{ searchKey ? '没有找到匹配的资料' : '暂无可查看的招宣资料' }}
    </div>

    <div v-else class="file-list">
      <div v-for="file in filteredFiles" :key="file.key" class="file-item" @click="openFile(file)">
        <div class="file-icon" :style="{ background: file.bg }">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="white" v-html="file.icon"></svg>
        </div>
        <div class="file-info">
          <div class="file-name">
            {{ file.name }}
            <span v-if="file.isSample" class="sample-tag">样例</span>
          </div>
          <div class="file-time">{{ file.activityTitle }} · {{ file.time }}</div>
        </div>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="download-icon" @click.stop="downloadFile(file)">
          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { activityApi } from '@/api/activity'
import { materialApi } from '@/api/material'
import { getFileUrl } from '@/utils/file'
import { useUserStore } from '@/store/modules/user'
import H5NavBar from '../components/H5NavBar.vue'

const userStore = useUserStore()
const searchKey = ref('')
const loading = ref(false)
const uploading = ref(false)
const fileList = ref([])
const selectedFile = ref(null)
const categories = ['招生政策', '学校介绍', '专业介绍', '宣传海报', '报考指南', '其他资料']
const uploadForm = ref({
  name: '',
  category: categories[0],
  description: ''
})

const canUpload = computed(() => ['SCHOOL', 'TEACHER'].includes(userStore.role))

const iconMap = {
  doc: { bg: '#3b82f6', icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>' },
  ppt: { bg: '#f97316', icon: '<rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>' },
  xls: { bg: '#10b981', icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/>' },
  image: { bg: '#8b5cf6', icon: '<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>' },
  video: { bg: '#ec4899', icon: '<rect x="2" y="2" width="20" height="20" rx="2.18"/><polygon points="10 8 16 12 10 16 10 8"/>' },
  pdf: { bg: '#ef4444', icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>' },
  zip: { bg: '#6366f1', icon: '<path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/>' },
  other: { bg: '#64748b', icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>' }
}

const sampleFiles = [
  { key: 'sample-doc', name: '招宣政策说明.docx', activityTitle: '样例资料', time: 'Word文档样例', type: 'doc', mime: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', content: '这是招宣政策说明 Word 样例，用于展示文档类资料。' },
  { key: 'sample-ppt', name: '学校宣讲课件.pptx', activityTitle: '样例资料', time: 'PPT课件样例', type: 'ppt', mime: 'application/vnd.openxmlformats-officedocument.presentationml.presentation', content: '这是学校宣讲课件 PPT 样例，用于展示演示文稿类资料。' },
  { key: 'sample-xls', name: '招生计划统计.xlsx', activityTitle: '样例资料', time: 'Excel表格样例', type: 'xls', mime: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', content: '学校,计划人数\n第一中学,120\n第二中学,80' },
  { key: 'sample-image', name: '校园风光图片.png', activityTitle: '样例资料', time: '图片样例', type: 'image', mime: 'image/svg+xml', content: '<svg xmlns="http://www.w3.org/2000/svg" width="640" height="360"><rect width="640" height="360" fill="#e0f2fe"/><rect x="90" y="150" width="460" height="150" rx="16" fill="#2563eb"/><rect x="130" y="190" width="80" height="80" fill="#fff"/><rect x="280" y="190" width="80" height="80" fill="#fff"/><rect x="430" y="190" width="80" height="80" fill="#fff"/><text x="320" y="95" font-size="38" text-anchor="middle" fill="#1e3a8a">校园风光样例</text></svg>' },
  { key: 'sample-video', name: '招生宣传视频.mp4', activityTitle: '样例资料', time: '视频样例', type: 'video', mime: 'text/plain', content: '这是招生宣传视频样例占位文件，用于展示视频类资料。' },
  { key: 'sample-pdf', name: '报考指南.pdf', activityTitle: '样例资料', time: 'PDF样例', type: 'pdf', mime: 'application/pdf', content: '这是报考指南 PDF 样例，用于展示 PDF 类资料。' },
  { key: 'sample-zip', name: '招宣素材包.zip', activityTitle: '样例资料', time: '压缩包样例', type: 'zip', mime: 'application/octet-stream', content: '这是招宣素材包 ZIP 样例，用于展示压缩包类资料。' }
].map(file => ({
  ...file,
  ...iconMap[file.type],
  isSample: true
}))

const getFileMeta = (fileName = '') => {
  const ext = fileName.split('.').pop()?.toLowerCase() || ''
  if (['doc', 'docx'].includes(ext)) return iconMap.doc
  if (['ppt', 'pptx'].includes(ext)) return iconMap.ppt
  if (['xls', 'xlsx', 'csv'].includes(ext)) return iconMap.xls
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(ext)) return iconMap.image
  if (['mp4', 'webm', 'ogg', 'avi', 'mov', 'mkv'].includes(ext)) return iconMap.video
  if (ext === 'pdf') return iconMap.pdf
  if (['zip', 'rar', '7z'].includes(ext)) return iconMap.zip
  return iconMap.other
}

const formatTime = (value) => {
  if (!value) return '最近更新'
  return new Date(value).toLocaleString('zh-CN')
}

const filteredFiles = computed(() => {
  const keyword = searchKey.value.trim().toLowerCase()
  if (!keyword) return fileList.value
  return fileList.value.filter(file =>
    file.name.toLowerCase().includes(keyword) ||
    file.activityTitle.toLowerCase().includes(keyword) ||
    (file.description || '').toLowerCase().includes(keyword)
  )
})

const mapMaterialFile = (material) => {
  const name = material.name || material.fileName || '招宣资料'
  const meta = getFileMeta(material.fileName || name)
  return {
    key: `material-${material.id}`,
    id: material.id,
    materialId: material.id,
    name,
    description: material.description,
    activityTitle: material.category || '招宣资料',
    filePath: material.filePath,
    fullUrl: getFileUrl(material.filePath),
    time: formatTime(material.createTime || material.updateTime),
    source: 'material',
    ...meta
  }
}

const loadMaterials = async () => {
  loading.value = true
  try {
    const materialRes = await materialApi.list()
    const materialFiles = (materialRes.data || []).map(mapMaterialFile)

    const res = await activityApi.getActivityList({ page: 1, size: 100 })
    const activities = res.data?.records || []
    const detailResults = await Promise.allSettled(
      activities.map(activity => activityApi.getActivityDetail(activity.id))
    )
    const files = []
    detailResults.forEach((result, activityIndex) => {
      if (result.status !== 'fulfilled') return
      const activity = result.value.data || {}
      const attachments = activity.attachments || []
      attachments.forEach((attachment, index) => {
        const name = attachment.fileName || attachment.name || `附件${index + 1}`
        const meta = getFileMeta(name)
        files.push({
          key: `${activity.id || activities[activityIndex]?.id}-${attachment.id || index}`,
          id: attachment.id,
          name,
          activityTitle: activity.title || activity.name || activities[activityIndex]?.title || '活动资料',
          filePath: attachment.filePath,
          fullUrl: attachment.fullUrl || getFileUrl(attachment.filePath),
          time: formatTime(activity.updateTime || activity.createTime),
          source: 'activity',
          ...meta
        })
      })
    })
    fileList.value = [...materialFiles, ...files, ...sampleFiles]
  } catch (error) {
    MessagePlugin.error('获取招宣资料失败')
    fileList.value = sampleFiles
  } finally {
    loading.value = false
  }
}

const handleFileChange = (event) => {
  selectedFile.value = event.target.files?.[0] || null
}

const resetUploadForm = () => {
  uploadForm.value = {
    name: '',
    category: categories[0],
    description: ''
  }
  selectedFile.value = null
  const input = document.querySelector('.file-control')
  if (input) input.value = ''
}

const submitMaterialUpload = async () => {
  if (!uploadForm.value.name.trim()) {
    MessagePlugin.warning('请填写资料名称')
    return
  }
  if (!selectedFile.value) {
    MessagePlugin.warning('请选择要上传的资料文件')
    return
  }
  uploading.value = true
  try {
    await materialApi.upload({
      ...uploadForm.value,
      file: selectedFile.value
    })
    MessagePlugin.success('招宣资料上传成功')
    resetUploadForm()
    await loadMaterials()
  } finally {
    uploading.value = false
  }
}

const openSampleFile = (file) => {
  const blob = new Blob([file.content], { type: file.mime || 'text/plain' })
  const url = URL.createObjectURL(blob)
  window.open(url, '_blank')
  setTimeout(() => URL.revokeObjectURL(url), 30000)
}

const openFile = (file) => {
  if (file.isSample) {
    openSampleFile(file)
    return
  }
  const url = file.fullUrl || getFileUrl(file.filePath)
  if (!url) {
    MessagePlugin.warning('资料文件地址不存在')
    return
  }
  window.open(url, '_blank')
}

const downloadFile = (file) => {
  if (file.isSample) {
    const blob = new Blob([file.content], { type: file.mime || 'text/plain' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.name
    link.click()
    URL.revokeObjectURL(url)
    return
  }
  const url = file.source === 'activity' && file.id
    ? `/api/file/download/${file.id}`
    : (file.fullUrl || getFileUrl(file.filePath))
  if (!url) {
    MessagePlugin.warning('资料文件地址不存在')
    return
  }
  window.open(url, '_blank')
}

onMounted(loadMaterials)
</script>

<style scoped>
.materials {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}
.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20px;
  margin: 12px;
  padding: 10px 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.search-box input {
  flex: 1;
  border: none;
  outline: none;
  margin-left: 8px;
  font-size: 14px;
  color: #374151;
  background: transparent;
}
.search-box input::placeholder {
  color: #9ca3af;
}
.upload-card {
  background: #fff;
  border-radius: 12px;
  margin: 0 12px 12px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.upload-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 10px;
}
.form-control {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #374151;
  background: #fff;
  outline: none;
}
.form-control:focus {
  border-color: #2563eb;
}
.textarea {
  min-height: 72px;
  resize: vertical;
}
.file-control {
  display: block;
  width: 100%;
  box-sizing: border-box;
  margin: 4px 0 10px;
  font-size: 13px;
  color: #4b5563;
}
.upload-button {
  width: 100%;
  border: none;
  border-radius: 8px;
  padding: 11px 12px;
  color: #fff;
  background: #2563eb;
  font-size: 14px;
  cursor: pointer;
}
.upload-button:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}
.file-list {
  padding: 0 12px;
}
.file-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
}
.file-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}
.file-info {
  flex: 1;
}
.file-name {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}
.sample-tag {
  display: inline-block;
  font-size: 11px;
  color: #2563eb;
  background: #dbeafe;
  border-radius: 999px;
  padding: 1px 6px;
  margin-left: 6px;
  vertical-align: 1px;
}
.file-time {
  font-size: 12px;
  color: #9ca3af;
}
.download-icon {
  flex-shrink: 0;
  cursor: pointer;
}
.state-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
  padding: 44px 20px;
}
</style>
