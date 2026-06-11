<template>
  <div class="h5-college-feedback">
    <H5NavBar title="反馈管理" />

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else class="content-wrap">
      <!-- 搜索 & 筛选 -->
      <div class="filter-bar">
        <div class="search-row">
          <t-input
            v-model="keyword"
            placeholder="搜索反馈标题或内容..."
            clearable
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <div class="filter-row">
          <t-select
            v-model="activityFilter"
            placeholder="按活动筛选"
            clearable
            @change="handleSearch"
            @clear="handleSearch"
          >
            <t-option
              v-for="opt in activityOptions"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
            />
          </t-select>
        </div>
      </div>

      <!-- 反馈列表 -->
      <div v-if="viewList.length > 0" class="feedback-list">
        <div
          v-for="item in viewList"
          :key="item.id"
          class="feedback-item"
          @click="showDetail(item)"
        >
          <div class="item-header">
            <t-tag
              :theme="item.userRole === 'STUDENT' ? 'primary' : 'warning'"
              variant="light"
              size="small"
            >
              {{ item.userRole === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
            <span class="item-realname">{{ item.realName || '匿名' }}</span>
            <span class="item-time">{{ formatDateTime(item.createTime) }}</span>
          </div>
          <div class="item-title">{{ item.title || '反馈' }}</div>
          <div class="item-activity">
            <span class="activity-label">活动：</span>{{ item.activityTitle || '-' }}
          </div>
          <div class="item-content">
            {{ (item.content || '').substring(0, 80) }}{{ (item.content || '').length > 80 ? '...' : '' }}
          </div>
          <div v-if="attachmentsOf(item).length > 0" class="item-attachments">
            <span class="attachment-icon">📎</span>
            <span>{{ attachmentsOf(item).length }} 个附件</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-wrap">
        <div class="empty-text">暂无反馈记录</div>
      </div>
    </div>

    <!-- 详情覆盖层（自定义全屏弹层） -->
    <div v-if="detailVisible && currentRecord" class="detail-overlay" @click.self="closeDetailDialog">
      <div class="detail-panel">
        <div class="detail-header">
          <div class="detail-title">反馈详情</div>
          <button class="close-btn" @click.stop="closeDetailDialog">关闭</button>
        </div>

        <div class="detail-body">
          <div class="info-section">
            <div class="info-item">
              <span class="info-key">提交人</span>
              <span class="info-val">{{ currentRecord.realName || '匿名' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">类型</span>
              <t-tag
                :theme="currentRecord.userRole === 'STUDENT' ? 'primary' : 'warning'"
                variant="light"
                size="small"
              >
                {{ currentRecord.userRole === 'STUDENT' ? '学生' : '教师' }}
              </t-tag>
            </div>
            <div class="info-item">
              <span class="info-key">活动名称</span>
              <span class="info-val">{{ currentRecord.activityTitle || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">反馈标题</span>
              <span class="info-val">{{ currentRecord.title || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">提交时间</span>
              <span class="info-val">{{ formatDateTime(currentRecord.createTime) }}</span>
            </div>
          </div>

          <div class="feedback-body">
            <div class="section-title">反馈内容</div>
            <div class="content-full">{{ currentRecord.content || '暂无内容' }}</div>
          </div>

          <div v-if="attachmentsOf(currentRecord).length > 0" class="attachment-section">
            <div class="section-title">附件列表</div>
            <div
              v-for="(url, index) in attachmentsOf(currentRecord)"
              :key="index"
              class="attachment-item"
              @click.stop="downloadFile(url)"
            >
              <span class="attachment-icon-small">📎</span>
              <span class="file-name">{{ fileNameOf(url) || '附件' + (index + 1) }}</span>
              <span class="download-text">下载</span>
            </div>
          </div>

          <div class="detail-footer">
            <button class="footer-close-btn" @click.stop="closeDetailDialog">我知道了</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { feedbackApi } from '@/api/feedback'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const loading = ref(false)
const records = ref([])
const keyword = ref('')
const activityFilter = ref(null)
const activityOptions = ref([])

const detailVisible = ref(false)
const currentRecord = ref(null)

// 计算后的展示列表（支持搜索和筛选）
const viewList = computed(() => {
  let list = records.value
  if (activityFilter.value) {
    list = list.filter(item => String(item.activityId) === String(activityFilter.value))
  }
  if (keyword.value && keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(item =>
      (item.title && String(item.title).toLowerCase().includes(kw)) ||
      (item.content && String(item.content).toLowerCase().includes(kw))
    )
  }
  return list
})

// 获取附件（兼容多种数据格式）
const attachmentsOf = (row) => {
  if (!row) return []
  // 优先处理 attachments 数组（后端可能直接返回对象数组）
  if (Array.isArray(row.attachments) && row.attachments.length > 0) {
    return row.attachments.map(a => {
      const url = a.url || a.fileUrl || a
      return getFileUrl(typeof url === 'string' ? url : String(url))
    })
  }
  // 处理 attachmentUrls 字段（后端存储的逗号分隔字符串）
  if (row.attachmentUrls) {
    if (Array.isArray(row.attachmentUrls)) {
      return row.attachmentUrls.filter(Boolean).map(url => getFileUrl(url))
    }
    if (typeof row.attachmentUrls === 'string') {
      return row.attachmentUrls
        .split(',')
        .map(url => url.trim())
        .filter(Boolean)
        .map(url => getFileUrl(url))
    }
  }
  return []
}

// 从URL中提取文件名（用于显示）
const fileNameOf = (url) => {
  if (!url) return ''
  try {
    const parts = String(url).split('/')
    let filename = parts[parts.length - 1].split('?')[0]
    // 如果是 UUID 或过长的文件名，显示前20位
    if (filename.length > 40) filename = filename.substring(0, 37) + '...'
    return filename
  } catch (e) {
    return '附件'
  }
}

// 下载附件
const downloadFile = (url) => {
  if (url) {
    window.open(url, '_blank')
  } else {
    MessagePlugin.info('附件地址无效')
  }
}

// 获取反馈列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getCollegeFeedbacks(
      activityFilter.value || undefined
    )
    let data = res.data
    let list = []
    if (Array.isArray(data)) {
      list = data
    } else if (data && Array.isArray(data.records)) {
      list = data.records
    } else if (data && Array.isArray(data.data)) {
      list = data.data
    } else {
      list = []
    }
    records.value = list
  } catch (err) {
    console.error('获取反馈列表失败', err)
    records.value = []
    MessagePlugin.error('获取反馈列表失败')
  } finally {
    loading.value = false
  }
}

// 获取活动选项
const fetchActivities = async () => {
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 200 })
    let list = []
    if (Array.isArray(res.data)) {
      list = res.data
    } else if (res.data && Array.isArray(res.data.records)) {
      list = res.data.records
    }
    activityOptions.value = list.map(a => ({
      value: a.id,
      label: a.name || a.title || '活动'
    }))
  } catch (err) {
    console.error('获取活动列表失败', err)
  }
}

// 搜索/筛选
const handleSearch = () => {
  fetchData()
}

// 打开详情
const showDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

// 关闭详情
const closeDetailDialog = () => {
  detailVisible.value = false
  currentRecord.value = null
}

// 格式化时间
const formatDateTime = (str) => {
  if (!str) return '-'
  try {
    const d = new Date(str)
    if (isNaN(d.getTime())) return String(str).substring(0, 19)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch (e) {
    return String(str)
  }
}

onMounted(() => {
  fetchActivities()
  fetchData()
})
</script>

<style scoped>
.h5-college-feedback {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 60px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.content-wrap {
  padding: 12px;
}

.filter-bar {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.search-row :deep(.t-input) {
  flex: 1;
}

.search-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 14px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.filter-row {
  width: 100%;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feedback-item {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.feedback-item:active {
  transform: scale(0.98);
  background: #f3f4f6;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.item-realname {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.item-time {
  font-size: 12px;
  color: #9ca3af;
  margin-left: auto;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.item-activity {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.activity-label {
  color: #9ca3af;
}

.item-content {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 10px;
}

.item-attachments {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: #f3f4f6;
  border-radius: 6px;
  font-size: 12px;
  color: #6b7280;
  width: fit-content;
}

.attachment-icon {
  font-size: 14px;
}

.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.empty-text {
  color: #9ca3af;
  font-size: 14px;
}

/* 详情覆盖层 & 弹窗样式 */
.detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.detail-panel {
  background: #fff;
  border-radius: 16px 16px 0 0;
  width: 100%;
  max-width: 640px;
  max-height: 85vh;
  overflow-y: auto;
  padding: 0;
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 10;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: #f3f4f6;
  color: #4b5563;
  border: none;
  border-radius: 6px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
}

.close-btn:active {
  background: #e5e7eb;
}

.detail-body {
  padding: 16px 20px 30px;
}

.info-section {
  background: #f9fafb;
  border-radius: 10px;
  padding: 12px 14px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e5e7eb;
}

.info-item:last-child {
  border-bottom: none;
}

.info-key {
  font-size: 13px;
  color: #6b7280;
  width: 80px;
  flex-shrink: 0;
}

.info-val {
  font-size: 13px;
  color: #1f2937;
  flex: 1;
  word-break: break-all;
}

.feedback-body {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 10px;
  padding-left: 8px;
  border-left: 3px solid #2563eb;
}

.content-full {
  background: #f9fafb;
  border-radius: 10px;
  padding: 14px;
  font-size: 14px;
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.attachment-section {
  margin-top: 16px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.attachment-item:active {
  background: #e5e7eb;
}

.attachment-icon-small {
  font-size: 16px;
}

.file-name {
  font-size: 13px;
  color: #374151;
  flex: 1;
  word-break: break-all;
}

.download-text {
  font-size: 13px;
  color: #2563eb;
  font-weight: 500;
}

/* 底部关闭按钮 */
.detail-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.footer-close-btn {
  width: 100%;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 0;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.footer-close-btn:active {
  background: #1d4ed8;
}
</style>
