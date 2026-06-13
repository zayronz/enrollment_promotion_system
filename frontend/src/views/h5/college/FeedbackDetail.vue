<template>
  <div class="feedback-detail">
    <H5NavBar title="反馈详情" />
    <div class="detail-content">
      <div v-if="loading" class="state-tip">加载中...</div>
      <div v-else-if="!detail" class="state-tip">暂无反馈内容</div>
      <template v-else>
        <div class="detail-header">
          <h3 class="detail-title">{{ detail.title }}</h3>
          <div class="detail-meta">
            <span class="meta-tag">{{ detail.activityTitle }}</span>
            <span class="meta-time">{{ formatTime(detail.createTime) }}</span>
          </div>
        </div>
        <div class="detail-body">
          <div class="detail-label">反馈内容</div>
          <div class="detail-text" v-html="detail.content"></div>
        </div>
        <div v-if="detail.attachmentUrls" class="detail-attachments">
          <div class="detail-label">附件</div>
          <div class="attachment-list">
            <div
              v-for="(url, idx) in parseAttachments(detail.attachmentUrls)"
              :key="idx"
              class="attachment-item"
              @click="previewAttachment(url)"
            >
              <t-icon name="file" size="18px" />
              <span class="attachment-name">附件 {{ idx + 1 }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { feedbackApi } from '@/api/feedback'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getFeedbackDetail(route.params.id)
    detail.value = res.data || null
  } catch (err) {
    MessagePlugin.error('获取反馈详情失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (value) => {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN')
}

const parseAttachments = (urls) => {
  if (!urls) return []
  try {
    const parsed = JSON.parse(urls)
    return Array.isArray(parsed) ? parsed : [urls]
  } catch {
    return [urls]
  }
}

const previewAttachment = (url) => {
  window.open(url, '_blank')
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.feedback-detail {
  min-height: 100vh;
  background: #f5f7fa;
}
.detail-content {
  padding: 16px;
}
.state-tip {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 14px;
}
.detail-header {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
}
.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.meta-tag {
  padding: 4px 10px;
  border-radius: 99px;
  background: #dbeafe;
  color: #2563eb;
  font-size: 12px;
  font-weight: 500;
}
.meta-time {
  font-size: 12px;
  color: #9ca3af;
}
.detail-body {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
}
.detail-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 10px;
}
.detail-text {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
}
.detail-attachments {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
}
.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  background: #f9fafb;
  cursor: pointer;
}
.attachment-name {
  font-size: 13px;
  color: #374151;
}
</style>
