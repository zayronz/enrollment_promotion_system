<template>
  <div class="activity-detail">
    <H5NavBar title="活动详情" />
    <div class="detail-content">
      <div v-if="loading" class="state-tip">加载中...</div>
      <div v-else-if="!activity" class="state-tip">活动不存在或已下架</div>
      <template v-else>
        <div class="detail-header">
          <h2 class="detail-title">{{ activity.name }}</h2>
          <div class="detail-tags">
            <span class="tag type-tag">{{ activity.type === 1 ? '线下活动' : '线上活动' }}</span>
            <span class="tag status-tag" :class="'status-' + activity.status">{{ statusLabel }}</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-label">活动时间</div>
          <div class="section-value">{{ formatDateRange(activity.activityStartTime, activity.activityEndTime) }}</div>
        </div>
        <div class="detail-section">
          <div class="section-label">活动地点</div>
          <div class="section-value">{{ activity.location || '线上宣讲' }}</div>
        </div>
        <div class="detail-section">
          <div class="section-label">报名时间</div>
          <div class="section-value">{{ formatDateRange(activity.registrationStartTime, activity.registrationEndTime) }}</div>
        </div>
        <div class="detail-section">
          <div class="section-label">活动简介</div>
          <div class="section-value desc" v-html="activity.description"></div>
        </div>
        <div class="detail-actions">
          <t-button theme="primary" block size="large" @click="goRegister">立即报名</t-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activity = ref(null)

const statusLabel = computed(() => {
  const map = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[activity.value?.status] || '已发布'
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(route.params.id)
    activity.value = res.data || null
  } catch (err) {
    MessagePlugin.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

const formatDateRange = (startStr, endStr) => {
  const fmt = (dateStr) => {
    if (!dateStr) return ''
    try {
      const d = new Date(dateStr)
      if (isNaN(d.getTime())) {
        const s = String(dateStr).replace('T', ' ')
        return s.substring(0, 10)
      }
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    } catch (e) {
      return ''
    }
  }
  const s = fmt(startStr)
  const e = fmt(endStr)
  if (!s && !e) return '待定'
  if (s && !e) return s
  if (!s && e) return e
  return `${s} ~ ${e}`
}

const goRegister = () => {
  if (!activity.value?.id) return
  router.push(`/h5/register/${activity.value.id}`)
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.activity-detail {
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
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px;
}
.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tag {
  padding: 4px 10px;
  border-radius: 99px;
  font-size: 12px;
  font-weight: 500;
}
.type-tag {
  background: #dbeafe;
  color: #2563eb;
}
.status-tag.status-0 {
  background: #f3f4f6;
  color: #6b7280;
}
.status-tag.status-1 {
  background: #d1fae5;
  color: #059669;
}
.status-tag.status-2 {
  background: #fee2e2;
  color: #dc2626;
}
.detail-section {
  background: #fff;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 12px;
}
.section-label {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 6px;
  font-weight: 500;
}
.section-value {
  font-size: 15px;
  color: #1f2937;
  font-weight: 500;
  line-height: 1.6;
}
.section-value.desc {
  font-weight: 400;
  color: #4b5563;
}
.detail-actions {
  padding: 8px 0 24px;
}
</style>
