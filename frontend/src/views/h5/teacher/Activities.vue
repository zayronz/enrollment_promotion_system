<template>
  <div class="teacher-activities">
    <H5NavBar title="活动报名" />

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else class="activity-list">
      <div
        v-for="item in records"
        :key="item.id"
        class="activity-card"
        @click="goDetail(item)"
      >
        <div v-if="item.coverImage" class="card-cover">
          <img :src="getFileUrl(item.coverImage)" :alt="item.name" />
        </div>
        <div class="card-header">
          <div class="card-title">{{ item.name }}</div>
          <t-tag :theme="getStatusTheme(item.status)" variant="light" size="small">
            {{ getStatusLabel(item.status) }}
          </t-tag>
        </div>
        <div class="card-info">
          <div class="info-row">活动时间: {{ formatDate(item.activityStartTime) }} - {{ formatDate(item.activityEndTime) }}</div>
          <div v-if="item.location" class="info-row">活动地点: {{ item.location }}</div>
        </div>
      </div>

      <div v-if="records.length === 0" class="empty-wrap">
        <div class="empty-text">暂无活动</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const records = ref([])
const loading = ref(true)

const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await activityApi.getOpenActivities()
    records.value = res.data?.records || res.data || []
  } catch (err) {
    console.error('获取活动列表失败', err)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const getStatusTheme = (status) => {
  const map = { 0: 'default', 1: 'success', 2: 'warning' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[status] || '未知'
}

const goDetail = (item) => {
  router.push(`/h5/activity/${item.id}`)
}

onMounted(fetchActivities)
</script>

<style scoped>
.teacher-activities { min-height: 100vh; background: #f5f7fa; padding-bottom: 80px; }
.loading-wrap { display: flex; justify-content: center; padding: 60px 0; }
.activity-list { padding: 12px; }
.activity-card { background: #fff; border-radius: 12px; margin-bottom: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04); cursor: pointer; }
.card-cover { width: 100%; height: 140px; overflow: hidden; }
.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.card-header { display: flex; justify-content: space-between; align-items: flex-start; padding: 12px 14px 8px; }
.card-title { font-size: 15px; font-weight: 600; color: #1f2937; flex: 1; margin-right: 8px; }
.card-info { padding: 0 14px 14px; }
.info-row { font-size: 13px; color: #6b7280; line-height: 1.8; }
.empty-wrap { display: flex; justify-content: center; align-items: center; padding: 60px 0; }
.empty-text { color: #9ca3af; font-size: 14px; }
</style>
