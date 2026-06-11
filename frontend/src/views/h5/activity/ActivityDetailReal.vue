<template>
  <div class="activity-detail">
    <H5NavBar title="活动详情" />

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else-if="activity" class="detail-body">
      <!-- 轮播图 -->
      <div v-if="activity.bannerUrls && activity.bannerUrls.length" class="cover-image">
        <t-swiper :autoplay="true" class="banner-swiper">
          <t-swiper-item v-for="(url, index) in activity.bannerUrls" :key="index">
            <img :src="getFileUrl(url)" :alt="`轮播图${index + 1}`" />
          </t-swiper-item>
        </t-swiper>
      </div>
      <div v-else-if="activity.bannerUrl" class="cover-image">
        <img :src="getFileUrl(activity.bannerUrl)" :alt="activity.name" />
      </div>

      <!-- 视频介绍 -->
      <div v-if="activity.videoUrl" class="video-section">
        <h3 class="section-title">视频介绍</h3>
        <video :src="getFileUrl(activity.videoUrl)" controls class="detail-video"></video>
      </div>

      <!-- 基本信息 -->
      <div class="info-card">
        <h1 class="info-title">{{ activity.name }}</h1>
        <div class="info-tags">
          <t-tag :theme="activity.type === 0 ? 'primary' : 'warning'" variant="light">
            {{ activity.type === 0 ? '线上' : '线下' }}
          </t-tag>
          <t-tag :theme="getStatusTheme(activity.status)" variant="light">
            {{ getStatusLabel(activity.status) }}
          </t-tag>
        </div>
        <div class="info-meta">
          <div class="meta-row">
            <span class="meta-label">活动时间</span>
            <span>{{ formatFullDate(activity.activityStartTime) }} — {{ formatFullDate(activity.activityEndTime) }}</span>
          </div>
          <div v-if="activity.location" class="meta-row">
            <span class="meta-label">活动地点</span>
            <span>{{ activity.location }}</span>
          </div>
          <div v-if="activity.maxParticipants" class="meta-row">
            <span class="meta-label">人数限制</span>
            <span>限 {{ activity.maxParticipants }} 人</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">报名时间</span>
            <span>{{ formatDate(activity.registrationStartTime) }} - {{ formatDate(activity.registrationEndTime) }}</span>
          </div>
        </div>

        <!-- 报名按钮 -->
        <div v-if="canRegister" class="register-area">
          <t-button
            theme="primary"
            size="large"
            :disabled="alreadyRegistered || activity.status === 2"
            @click="goRegister"
            block
          >
            {{ alreadyRegistered ? '已报名' : activity.status === 2 ? '已结束' : '立即报名' }}
          </t-button>
        </div>
      </div>

      <!-- 活动介绍 -->
      <div v-if="activity.description || activity.content" class="description-card">
        <h3 class="section-title">活动介绍</h3>
        <div class="description-body" v-html="activity.description || activity.content"></div>
      </div>

      <!-- 附件下载 -->
      <div v-if="activity.attachments && activity.attachments.length > 0" class="attachments-card">
        <h3 class="section-title">附件下载</h3>
        <div class="attachment-list">
          <t-link
            v-for="(file, index) in activity.attachments"
            :key="index"
            theme="primary"
            hover="color"
            class="attachment-link"
            @click="downloadAttachment(file)"
          >
            {{ file.name || file.fileName || '附件' + (index + 1) }}
          </t-link>
        </div>
      </div>
    </div>

    <t-empty v-else description="活动不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registeration'
import { getFileUrl } from '@/utils/file'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const route = useRoute()
const router = useRouter()

const activity = ref(null)
const loading = ref(true)
const alreadyRegistered = ref(false)

const canRegister = computed(() => {
  if (!activity.value) return false
  const now = Date.now()
  if (activity.value.registrationStartTime) {
    const start = new Date(activity.value.registrationStartTime).getTime()
    if (now < start) return false
  }
  if (activity.value.registrationEndTime) {
    const end = new Date(activity.value.registrationEndTime).getTime()
    if (now > end) return false
  }
  return true
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')}`
}

const formatFullDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleString('zh-CN')
}

const getStatusTheme = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'default' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '未发布', 1: '报名中', 2: '已结束' }
  return map[status] || '未知'
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(route.params.id)
    activity.value = res.data

    // 检查当前用户是否已报名
    try {
      const statusRes = await registrationApi.getRegistrationStatus(activity.value.id)
      alreadyRegistered.value = !!statusRes.data?.registered
    } catch (e) {
      console.error(e)
    }
  } catch (err) {
    console.error('获取活动详情失败', err)
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.push(`/h5/register/${route.params.id}`)
}

const downloadAttachment = (file) => {
  if (file.url || file.fileUrl) {
    window.open(getFileUrl(file.url || file.fileUrl), '_blank')
  } else if (file.path || file.filePath) {
    window.open(getFileUrl(file.path || file.filePath), '_blank')
  } else {
    MessagePlugin.info('附件无法下载')
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.activity-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.detail-body {
  padding: 12px;
}

.cover-image {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.cover-image .banner-swiper {
  border-radius: 12px;
  overflow: hidden;
}

.cover-image img {
  width: 100%;
  height: auto;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  display: block;
}

.video-section {
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 12px;
}

.detail-video {
  width: 100%;
  border-radius: 8px;
  background: #000;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 12px;
}

.info-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.info-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.info-meta {
  margin-bottom: 20px;
}

.meta-row {
  display: flex;
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.meta-label {
  min-width: 80px;
  color: #9ca3af;
}

.register-area {
  margin-top: 16px;
}

.description-card,
.attachments-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 12px;
}

.description-body {
  font-size: 14px;
  line-height: 1.8;
  color: #374151;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-link {
  font-size: 14px;
}
</style>
