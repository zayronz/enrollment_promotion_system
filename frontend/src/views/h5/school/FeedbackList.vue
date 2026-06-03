<template>
  <div class="feedback-list">
    <H5NavBar title="反馈管理" />

    <!-- 搜索框 -->
    <div class="search-box">
      <input v-model="keyword" placeholder="搜索反馈标题或内容..." @keyup.enter="fetchData" />
    </div>

    <!-- 反馈列表 -->
    <div class="list-content">
      <div v-for="item in records" :key="item.id" class="feedback-card" @click="showDetail(item)">
        <div class="card-header">
          <div class="user-info">
            <span class="user-name">{{ item.realName }}</span>
            <t-tag :theme="item.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
              {{ item.userType === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
          </div>
          <span class="apply-time">{{ formatDateTime(item.createTime) }}</span>
        </div>

        <div class="card-body">
          <div class="feedback-title">{{ item.title }}</div>
          <div class="feedback-content">{{ item.content }}</div>
          <div class="feedback-meta">
            <span class="activity-name">{{ item.activityTitle }}</span>
            <span v-if="item.attachments && item.attachments.length > 0" class="attachment-count">
              {{ item.attachments.length }}个附件
            </span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="!loading && records.length === 0" class="empty-text">暂无反馈</div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
        加载更多
      </div>
    </div>

    <!-- 详情弹窗 -->
    <t-popup v-model:visible="detailVisible" placement="bottom">
      <div class="detail-panel">
        <div class="panel-header">
          <div class="panel-title">反馈详情</div>
          <div class="panel-close" @click="detailVisible = false">×</div>
        </div>
        <div class="panel-body">
          <div class="detail-section">
            <div class="detail-row">
              <span class="detail-label">提交人:</span>
              <span class="detail-value">{{ currentRecord?.realName }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">类型:</span>
              <t-tag :theme="currentRecord?.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
                {{ currentRecord?.userType === 'STUDENT' ? '学生' : '教师' }}
              </t-tag>
            </div>
            <div class="detail-row">
              <span class="detail-label">活动:</span>
              <span class="detail-value">{{ currentRecord?.activityTitle }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">标题:</span>
              <span class="detail-value">{{ currentRecord?.title }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">时间:</span>
              <span class="detail-value">{{ formatDateTime(currentRecord?.createTime) }}</span>
            </div>
          </div>

          <div class="content-section">
            <div class="section-title">反馈内容</div>
            <div class="content-text" v-html="currentRecord?.content || '暂无内容'"></div>
          </div>

          <div v-if="currentRecord?.attachments && currentRecord.attachments.length > 0" class="attachment-section">
            <div class="section-title">附件列表</div>
            <div
              v-for="(file, index) in currentRecord.attachments"
              :key="index"
              class="attachment-item"
              @click="downloadFile(file)"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <span>{{ file.name || file.fileName || '附件' + (index + 1) }}</span>
            </div>
          </div>
        </div>
      </div>
    </t-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { feedbackApi } from '@/api/feedback'
import H5NavBar from '../components/H5NavBar.vue'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const current = ref(1)
const hasMore = ref(true)
const detailVisible = ref(false)
const currentRecord = ref(null)

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getAllFeedbacks()
    let data = res.data?.records || res.data || []
    // 过滤关键词
    if (keyword.value) {
      data = data.filter(item =>
        item.title?.includes(keyword.value) || item.content?.includes(keyword.value)
      )
    }
    records.value = data.slice(0, 10)
    current.value = 1
    hasMore.value = data.length > 10
  } catch (err) {
    console.error('获取反馈列表失败', err)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res = await feedbackApi.getAllFeedbacks()
    let data = res.data?.records || res.data || []
    if (keyword.value) {
      data = data.filter(item =>
        item.title?.includes(keyword.value) || item.content?.includes(keyword.value)
      )
    }
    const newItems = data.slice(current.value * 10, (current.value + 1) * 10)
    records.value = [...records.value, ...newItems]
    current.value++
    hasMore.value = newItems.length >= 10
  } catch (err) {
    console.error('加载更多失败', err)
  } finally {
    loading.value = false
  }
}

const showDetail = (item) => {
  currentRecord.value = item
  detailVisible.value = true
}

const downloadFile = (file) => {
  if (file.url) {
    window.open(file.url)
  } else {
    console.log('附件地址无效')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.feedback-list {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.search-box {
  margin: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
}

.search-box input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
}

.list-content {
  padding: 0 12px;
}

.feedback-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.apply-time {
  font-size: 12px;
  color: #9ca3af;
}

.feedback-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.feedback-content {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.feedback-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9ca3af;
}

.activity-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-count {
  flex-shrink: 0;
}

.loading-text, .empty-text {
  text-align: center;
  color: #9ca3af;
  padding: 20px;
  font-size: 14px;
}

.load-more {
  text-align: center;
  padding: 12px;
  color: #2563eb;
  font-size: 14px;
  cursor: pointer;
}

/* 详情弹窗样式 */
.detail-panel {
  background: #fff;
  border-radius: 16px 16px 0 0;
  max-height: 85vh;
  overflow-y: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  background: #fff;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.panel-close {
  font-size: 28px;
  color: #9ca3af;
  cursor: pointer;
  line-height: 1;
}

.panel-body {
  padding: 16px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-label {
  color: #6b7280;
  margin-right: 12px;
  flex-shrink: 0;
  width: 60px;
}

.detail-value {
  color: #1f2937;
  flex: 1;
}

.content-section, .attachment-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.content-text {
  font-size: 14px;
  line-height: 1.8;
  color: #6b7280;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #2563eb;
}

.attachment-item:hover {
  background: #e5e7eb;
}
</style>
