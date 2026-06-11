<template>
  <div class="activity-detail">
    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else-if="activity" class="detail-body">
      <!-- Cover image / banner carousel -->
      <div v-if="activity.bannerUrls && activity.bannerUrls.length" class="cover-swiper">
        <t-swiper :autoplay="true">
          <t-swiper-item v-for="(url, index) in activity.bannerUrls" :key="index">
            <div class="cover-slide" :style="{ backgroundImage: `url(${getFileUrl(url)})` }" />
          </t-swiper-item>
        </t-swiper>
      </div>
      <div v-else-if="activity.bannerUrl" class="cover-image" :style="{ backgroundImage: `url(${getFileUrl(activity.bannerUrl)})` }" />

      <!-- Video -->
      <div v-if="activity.videoUrl" class="video-section">
        <video :src="getFileUrl(activity.videoUrl)" controls class="detail-video" />
      </div>

      <!-- Basic info -->
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
            <TimeIcon class="meta-icon" />
            <span>{{ formatFullDate(activity.activityStartTime) }} — {{ formatFullDate(activity.activityEndTime) }}</span>
          </div>
          <div class="meta-row" v-if="activity.location">
            <LocationIcon class="meta-icon" />
            <span>{{ activity.location }}</span>
          </div>
          <div class="meta-row" v-if="activity.maxParticipants">
            <UsergroupIcon class="meta-icon" />
            <span>限 {{ activity.maxParticipants }} 人</span>
          </div>
        </div>

        <!-- Register button -->
        <div v-if="canRegister" class="register-area">
          <t-button
            v-if="!alreadyRegistered"
            theme="primary"
            size="large"
            @click="goRegister"
          >
            立即报名
          </t-button>
          <t-button
            v-else-if="canReApply"
            theme="warning"
            size="large"
            @click="goRegister"
          >
            重新报名
          </t-button>
          <t-button
            v-else-if="activity.status === 2"
            theme="default"
            size="large"
            disabled
          >
            已结束
          </t-button>
          <t-button
            v-else
            theme="default"
            size="large"
            disabled
          >
            {{ registrationStatus === 0 ? '审核中' : registrationStatus === 1 ? '学院审核通过' : registrationStatus === 2 ? '报名成功' : '已报名' }}
          </t-button>
        </div>
      </div>

      <!-- Description -->
      <div v-if="activity.description || activity.content" class="description-card">
        <h3 class="section-title">活动介绍</h3>
        <div class="description-body" v-html="activity.description || activity.content" />
      </div>

      <!-- Attachments -->
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
            <template #prefix-icon><FileIcon /></template>
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
import { TimeIcon, LocationIcon, UsergroupIcon, FileIcon } from 'tdesign-icons-vue-next'
import { getFileUrl } from '@/utils/file'
import { MessagePlugin } from 'tdesign-vue-next'

const route = useRoute()
const router = useRouter()

const activity = ref(null)
const loading = ref(true)
const alreadyRegistered = ref(false)
const registrationStatus = ref(null) // 保存报名状态

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

// 判断是否允许重新报名（被拒绝或已撤回后可以重新报名）
const canReApply = computed(() => {
  return registrationStatus.value === 3 || registrationStatus.value === 4 // 3=已拒绝, 4=已撤回
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(route.params.id)
    activity.value = res.data

    // Check if already registered and get status
    try {
      const regRes = await registrationApi.getMyRegistrations({ page: 1, size: 100 })
      const myRecords = regRes.data?.records || []
      const existingRegistration = myRecords.find(r => r.activityId === activity.value.id)
      if (existingRegistration) {
        alreadyRegistered.value = true
        registrationStatus.value = existingRegistration.status
        console.log('用户报名状态:', existingRegistration.status)
      } else {
        alreadyRegistered.value = false
        registrationStatus.value = null
      }
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
  router.push(`/student/register/${route.params.id}`)
}

const downloadAttachment = (file) => {
  const url = file.url || file.fileUrl || file.path || file.filePath
  if (url) {
    window.open(getFileUrl(url), '_blank')
  } else {
    MessagePlugin.warning('附件无法下载')
  }
}

const formatFullDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleString('zh-CN')
}

const getStatusTheme = (status) => {
  // activity status: 0=草稿, 1=已发布(报名中), 2=已结束
  const map = { 0: 'warning', 1: 'success', 2: 'default' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '未发布', 1: '报名中', 2: '已结束' }
  return map[status] || '未知'
}

onMounted(fetchDetail)
</script>

<style scoped>
.activity-detail {
  padding: 0;
}
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.detail-body {
  max-width: 860px;
}
.cover-swiper {
  height: 280px;
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: hidden;
}
.cover-slide {
  height: 100%;
  background-size: cover;
  background-position: center;
  background-color: #f0f2f5;
}
.cover-image {
  height: 280px;
  background-size: cover;
  background-position: center;
  border-radius: 12px;
  margin-bottom: 24px;
  background-color: #f0f2f5;
}
.video-section {
  margin-bottom: 24px;
}
.detail-video {
  width: 100%;
  border-radius: 12px;
  background: #000;
}
.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 28px;
  margin-bottom: 20px;
  border: 1px solid var(--td-border-level-1-color);
}
.info-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--td-text-color-primary);
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
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--td-text-color-secondary);
  margin-bottom: 8px;
}
.meta-icon {
  font-size: 16px;
  color: var(--td-text-color-placeholder);
  flex-shrink: 0;
}
.register-area {
  padding-top: 8px;
}
.description-card,
.attachments-card {
  background: #fff;
  border-radius: 12px;
  padding: 28px;
  margin-bottom: 20px;
  border: 1px solid var(--td-border-level-1-color);
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin-bottom: 16px;
}
.description-body {
  font-size: 14px;
  line-height: 1.8;
  color: var(--td-text-color-secondary);
}
.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.attachment-link {
  font-size: 14px;
}

@media (max-width: 640px) {
  .cover-swiper,
  .cover-image {
    height: 180px;
  }
  .info-card,
  .description-card,
  .attachments-card {
    padding: 20px;
  }
}
</style>
