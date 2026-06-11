<template>
  <div class="activity-list">
    <!-- Search bar -->
    <div class="search-bar">
      <t-input
        v-model="keyword"
        placeholder="搜索活动名称或关键词..."
        clearable
        size="large"
        class="search-input"
        @change="handleSearch"
      >
        <template #prefix-icon><SearchIcon /></template>
      </t-input>
      <t-select
        v-model="typeFilter"
        placeholder="活动类型"
        clearable
        size="large"
        class="type-select"
        @change="handleSearch"
      >
        <t-option value="" label="全部类型" />
        <t-option :value="0" label="线上活动" />
        <t-option :value="1" label="线下活动" />
      </t-select>
    </div>

    <!-- Banner carousel -->
    <div v-if="banners.length > 0" class="banner-section">
      <t-swiper
        :height="320"
        :interval="4000"
        :navigation="{ showSlideBtn: 'always' }"
      >
        <t-swiper-item v-for="(banner, index) in banners" :key="index">
          <div
            class="banner-slide"
            :style="{ backgroundImage: `url(${banner.imageUrl})` }"
            @click="goToActivity(banner.activityId)"
          >
            <div class="banner-overlay">
              <h3>{{ banner.title }}</h3>
              <p>{{ banner.description }}</p>
            </div>
          </div>
        </t-swiper-item>
      </t-swiper>
    </div>

    <!-- Online activities -->
    <div v-if="onlineActivities.length > 0" class="section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="dot online" />
          线上活动
        </h2>
        <span class="section-count">{{ onlineActivities.length }} 个活动</span>
      </div>
      <div class="card-grid">
        <t-card
          v-for="activity in onlineActivities"
          :key="activity.id"
          class="activity-card"
          hover-shadow
          @click="goToDetail(activity.id)"
        >
          <template #cover>
            <div
              class="card-cover"
              :style="{
                backgroundImage: activity.coverImage
                  ? `url(${getFileUrl(activity.coverImage)})`
                  : undefined
              }"
            >
              <div v-if="!activity.coverImage" class="cover-placeholder">
                <LayersIcon class="placeholder-icon" />
              </div>
              <t-tag
                :theme="getStatusTheme(activity.status)"
                variant="light"
                size="small"
                class="status-tag"
              >
                {{ getStatusLabel(activity.status) }}
              </t-tag>
            </div>
          </template>
          <template #header>
            <div class="card-title">{{ activity.name }}</div>
          </template>
          <div class="card-meta">
            <div class="meta-item">
              <TimeIcon class="meta-icon" />
              <span>{{ formatDate(activity.activityStartTime) }} — {{ formatDate(activity.activityEndTime) }}</span>
            </div>
            <div class="meta-item" v-if="activity.location">
              <LocationIcon class="meta-icon" />
              <span>{{ activity.location }}</span>
            </div>
          </div>
          <template #footer>
            <div class="card-footer-row">
              <t-tag variant="outline" size="small">{{ activity.type === 0 ? '线上' : '线下' }}</t-tag>
              <t-button variant="text" theme="primary" size="small">
                查看详情 <ChevronRightIcon />
              </t-button>
            </div>
          </template>
        </t-card>
      </div>
    </div>

    <!-- Offline activities -->
    <div v-if="offlineActivities.length > 0" class="section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="dot offline" />
          线下活动
        </h2>
        <span class="section-count">{{ offlineActivities.length }} 个活动</span>
      </div>
      <div class="card-grid">
        <t-card
          v-for="activity in offlineActivities"
          :key="activity.id"
          class="activity-card"
          hover-shadow
          @click="goToDetail(activity.id)"
        >
          <template #cover>
            <div
              class="card-cover"
              :style="{
                backgroundImage: activity.coverImage
                  ? `url(${getFileUrl(activity.coverImage)})`
                  : undefined
              }"
            >
              <div v-if="!activity.coverImage" class="cover-placeholder">
                <LayersIcon class="placeholder-icon" />
              </div>
              <t-tag
                :theme="getStatusTheme(activity.status)"
                variant="light"
                size="small"
                class="status-tag"
              >
                {{ getStatusLabel(activity.status) }}
              </t-tag>
            </div>
          </template>
          <template #header>
            <div class="card-title">{{ activity.name }}</div>
          </template>
          <div class="card-meta">
            <div class="meta-item">
              <TimeIcon class="meta-icon" />
              <span>{{ formatDate(activity.activityStartTime) }} — {{ formatDate(activity.activityEndTime) }}</span>
            </div>
            <div class="meta-item" v-if="activity.location">
              <LocationIcon class="meta-icon" />
              <span>{{ activity.location }}</span>
            </div>
          </div>
          <template #footer>
            <div class="card-footer-row">
              <t-tag variant="outline" size="small">{{ activity.type === 0 ? '线上' : '线下' }}</t-tag>
              <t-button variant="text" theme="primary" size="small">
                查看详情 <ChevronRightIcon />
              </t-button>
            </div>
          </template>
        </t-card>
      </div>
    </div>

    <!-- Empty state -->
    <t-empty
      v-if="!loading && onlineActivities.length === 0 && offlineActivities.length === 0"
      description="暂无活动数据"
    />

    <!-- Loading -->
    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <!-- Pagination -->
    <div v-if="total > pageSize" class="pagination-wrap">
      <t-pagination
        v-model="currentPage"
        :total="total"
        :page-size="pageSize"
        :show-page-size="false"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import {
  SearchIcon, LayersIcon, TimeIcon, LocationIcon, ChevronRightIcon
} from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()

const keyword = ref('')
const typeFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)

const activities = ref([])
const banners = ref([])

const onlineActivities = computed(() =>
  activities.value.filter((a) => a.type === 0)
)
const offlineActivities = computed(() =>
  activities.value.filter((a) => a.type === 1)
)

const fetchActivities = async () => {
  loading.value = true
  try {
    // 学生端必须使用登录态接口，后端会按当前学生绩点/成绩自动过滤不可报名活动
    const res = await activityApi.getActivityList({ page: 1, size: 1000 })
    let allActivities = res.data?.records || res.data || []
    
    // 前端筛选
    if (keyword.value) {
      allActivities = allActivities.filter(a => 
        a.name?.toLowerCase().includes(keyword.value.toLowerCase()) ||
        a.description?.toLowerCase().includes(keyword.value.toLowerCase())
      )
    }
    if (typeFilter.value !== '') {
      allActivities = allActivities.filter(a => a.type === Number(typeFilter.value))
    }
    
    // 分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    activities.value = allActivities.slice(start, end)
    total.value = allActivities.length

    // 首页轮播：只从当前学生可见活动中挑选管理员勾选了"首页展示"的活动
    const bannerSource = allActivities.filter(a => Number(a.showOnHome) === 1)
    const bannerList = []
    bannerSource.forEach(a => {
      if (a.bannerUrls && a.bannerUrls.length) {
        a.bannerUrls.forEach(url => {
          bannerList.push({
            activityId: a.id,
            title: a.name,
            description: a.description || '',
            imageUrl: getFileUrl(url)
          })
        })
      } else if (a.bannerUrl) {
        bannerList.push({
          activityId: a.id,
          title: a.name,
          description: a.description || '',
          imageUrl: getFileUrl(a.bannerUrl)
        })
      }
    })
    banners.value = bannerList
  } catch (err) {
    console.error('获取活动列表失败', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchActivities()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchActivities()
}

const goToDetail = (id) => {
  router.push(`/student/activity/${id}`)
}

const goToActivity = (id) => {
  if (id) router.push(`/student/activity/${id}`)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

const getStatusTheme = (status) => {
  const map = { 0: 'default', 1: 'success', 2: 'warning' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[status] || '未知'
}

onMounted(fetchActivities)
</script>

<style scoped>
.activity-list {
  padding: 0;
}

/* Search bar */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.search-input {
  flex: 1;
}
.type-select {
  width: 160px;
  flex-shrink: 0;
}

/* Banner */
.banner-section {
  width: 100%;
  margin-bottom: 32px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}
.banner-slide {
  width: 100%;
  height: 320px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
  position: relative;
  background-color: #e8ecf1;
}
.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px 32px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.55));
  color: #fff;
}
.banner-overlay h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}
.banner-overlay p {
  font-size: 13px;
  opacity: 0.85;
}

/* Section header */
.section {
  margin-bottom: 36px;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.dot.online {
  background: var(--td-brand-color);
}
.dot.offline {
  background: var(--td-warning-color);
}
.section-count {
  font-size: 13px;
  color: var(--td-text-color-placeholder);
}

/* Card grid */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.activity-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 10px;
  overflow: hidden;
}
.activity-card:hover {
  transform: translateY(-2px);
}

/* Card cover */
.card-cover {
  height: 160px;
  background-size: cover;
  background-position: center;
  background-color: #f0f2f5;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-icon {
  font-size: 40px;
  color: #d0d5dd;
}
.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  line-height: 1.5;
}

/* Card meta */
.card-meta {
  padding: 0 0 8px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--td-text-color-secondary);
  margin-bottom: 4px;
}
.meta-icon {
  font-size: 14px;
  color: var(--td-text-color-placeholder);
  flex-shrink: 0;
}

/* Card footer */
.card-footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 16px;
}

/* Loading */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

@media (max-width: 640px) {
  .search-bar {
    flex-direction: column;
  }
  .type-select {
    width: 100%;
  }
  .card-grid {
    grid-template-columns: 1fr;
  }
  .banner-slide {
    height: 190px;
  }
}
</style>
