<template>
  <div class="activity-list">
    <H5NavBar title="活动管理" />

    <!-- 搜索框 -->
    <div class="search-box">
      <input v-model="keyword" placeholder="搜索活动名称..." @keyup.enter="fetchData" />
    </div>

    <!-- 活动列表 -->
    <div class="activity-list-content">
      <div v-for="item in activities" :key="item.id" class="activity-card" @click="goDetail(item)">
        <div class="card-header">
          <span class="card-title">{{ item.name }}</span>
          <t-tag :theme="getStatusTheme(item.status)" variant="light" size="small">
            {{ getStatusLabel(item.status) }}
          </t-tag>
        </div>
        <div class="card-info">
          <span>报名人数: {{ item.registrationCount || 0 }}</span>
          <span>{{ formatDate(item.activityStartTime) }}</span>
        </div>
      </div>

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="!loading && activities.length === 0" class="empty-text">暂无活动</div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
        加载更多
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const activities = ref([])
const loading = ref(false)
const keyword = ref('')
const current = ref(1)
const hasMore = ref(true)

const getStatusTheme = (status) => {
  const map = { 0: 'default', 1: 'success', 2: 'warning' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[status] || '未知'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityList({
      page: 1,
      size: 10,
      keyword: keyword.value || undefined
    })
    activities.value = res.data?.records || []
    current.value = 1
    hasMore.value = activities.value.length >= 10
  } catch (err) {
    console.error('获取活动列表失败', err)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res = await activityApi.getActivityList({
      page: current.value + 1,
      size: 10,
      keyword: keyword.value || undefined
    })
    const newItems = res.data?.records || []
    activities.value = [...activities.value, ...newItems]
    current.value++
    hasMore.value = newItems.length >= 10
  } catch (err) {
    console.error('加载更多失败', err)
  } finally {
    loading.value = false
  }
}

const goDetail = (item) => {
  router.push(`/school/activity/detail/${item.id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.activity-list {
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

.activity-list-content {
  padding: 0 12px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  margin-right: 8px;
}

.card-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #6b7280;
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
</style>
