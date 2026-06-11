<template>
  <div class="h5-school-feedback">
    <H5NavBar title="反馈管理" />

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else class="content-wrap">
      <!-- Search & filter -->
      <div class="filter-bar">
        <t-input
          v-model="keyword"
          placeholder="搜索反馈标题或内容..."
          clearable
          @change="handleSearch"
        />
        <t-select
          v-model="activityFilter"
          placeholder="按活动筛选"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <t-option
            v-for="opt in activityOptions"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          />
        </t-select>
      </div>

      <!-- Feedback list -->
      <div v-if="records.length > 0" class="feedback-list">
        <div
          v-for="item in records"
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
          <div class="item-content" v-html="item.content?.substring(0, 80) + (item.content?.length > 80 ? '...' : '')" />
          <div v-if="getAttachments(item).length > 0" class="item-attachments">
            <span class="attachment-icon">📎</span>
            <span>{{ getAttachments(item).length }}个附件</span>
          </div>
        </div>

        <!-- Load more -->
        <div v-if="hasMore && !loadingMore" class="load-more" @click="loadMore">
          加载更多
        </div>
      </div>

      <div v-else class="empty-wrap">
        <div class="empty-text">暂无反馈记录</div>
      </div>
    </div>

    <!-- Detail popup -->
    <t-popup
      v-model:visible="detailVisible"
      placement="bottom"
      class="detail-popup"
    >
      <div v-if="currentRecord" class="detail-content">
        <div class="detail-header">
          <div class="detail-title">反馈详情</div>
          <div class="close-btn" @click="closeDetailDialog">×</div>
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
            <div class="content-full" v-html="currentRecord.content || '暂无内容'" />
          </div>

          <div v-if="getAttachments(currentRecord).length > 0" class="attachment-section">
            <div class="section-title">附件列表</div>
            <div
              v-for="(url, index) in getAttachments(currentRecord)"
              :key="index"
              class="attachment-item"
              @click="downloadFile(url)"
            >
              <span class="attachment-icon">📎</span>
              <span class="file-name">{{ getFileName(url) || '附件' + (index + 1) }}</span>
              <t-icon name="download" size="16" class="download-icon" />
            </div>
          </div>
        </div>
      </div>
    </t-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { feedbackApi } from '@/api/feedback'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const loading = ref(false)
const loadingMore = ref(false)
const records = ref([])
const keyword = ref('')
const activityFilter = ref('')
const activityOptions = ref([])

const detailVisible = ref(false)
const currentRecord = ref(null)

const hasMore = computed(() => records.value.length >= 10)

const getAttachments = (row) => {
  if (!row) return []
  if (Array.isArray(row.attachments) && row.attachments.length > 0) {
    return row.attachments.map(a => getFileUrl(a.url || a.fileUrl || a))
  }
  if (row.attachmentUrls) {
    if (Array.isArray(row.attachmentUrls)) {
      return row.attachmentUrls.filter(Boolean).map(url => getFileUrl(url))
    }
    if (typeof row.attachmentUrls === 'string') {
      return row.attachmentUrls.split(',').filter(Boolean).map(url => getFileUrl(url.trim()))
    }
  }
  return []
}

const getFileName = (url) => {
  if (!url) return ''
  try {
    const parts = url.split('/')
    const filename = parts[parts.length - 1].split('?')[0]
    return filename.length > 40 ? filename.substring(0, 37) + '...' : filename
  } catch {
    return '附件'
  }
}

const downloadFile = (url) => {
  if (url) {
    window.open(url)
  } else {
    MessagePlugin.info('附件地址无效')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getAllFeedbacks(activityFilter.value || undefined)
    let data = res.data
    let list = []
    if (Array.isArray(data)) {
      list = data
    } else if (data && Array.isArray(data.records)) {
      list = data.records
    } else {
      list = []
    }

    if (keyword.value) {
      const kw = keyword.value.toLowerCase()
      list = list.filter(item =>
        (item.title && item.title.toLowerCase().includes(kw)) ||
        (item.content && item.content.toLowerCase().includes(kw))
      )
    }
    records.value = list
  } catch (err) {
    console.error('获取反馈列表失败', err)
    records.value = []
  } finally {
    loading.value = false
  }
}

const fetchActivities = async () => {
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 100 })
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
    console.error(err)
  }
}

const handleSearch = () => fetchData()

const loadMore = async () => {
  if (loadingMore.value) return
  loadingMore.value = true
  try {
    await fetchData()
  } finally {
    loadingMore.value = false
  }
}

const showDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

const closeDetailDialog = () => {
  detailVisible.value = false
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
  fetchActivities()
})
</script>

<style scoped>
.h5-school-feedback {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
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
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-select {
  width: 100%;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feedback-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
}

.feedback-item:active {
  background: #f9fafb;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.item-realname {
  font-size: 13px;
  color: #1f2937;
  font-weight: 500;
  flex: 1;
}

.item-time {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  color: #6b7280;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-attachments {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
  font-size: 12px;
  color: #2563eb;
}

.load-more {
  text-align: center;
  padding: 16px;
  color: #2563eb;
  font-size: 14px;
  cursor: pointer;
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

.detail-popup {
  width: 100%;
  height: 85vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  font-size: 28px;
  color: #9ca3af;
  cursor: pointer;
  line-height: 1;
  padding: 0 8px;
}

.detail-body {
  padding: 16px;
  max-height: 72vh;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-key {
  font-size: 14px;
  color: #9ca3af;
  flex-shrink: 0;
}

.info-val {
  font-size: 14px;
  color: #1f2937;
  text-align: right;
  flex: 1;
  margin-left: 16px;
  word-break: break-word;
}

.feedback-body {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.content-full {
  font-size: 14px;
  line-height: 1.8;
  color: #6b7280;
}

.attachment-section {
  margin-bottom: 20px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.attachment-item:active {
  background: #f3f4f6;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.download-icon {
  color: #2563eb;
}
</style>
