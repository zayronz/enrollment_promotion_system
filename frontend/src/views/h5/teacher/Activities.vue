<template>
  <div class="h5-teacher-activities">
    <!-- 头部 -->
    <div class="home-header">
      <div class="header-left">
        <div class="role-badge teacher">教师</div>
        <div class="header-title">活动广场</div>
      </div>
    </div>

    <!-- 轮播图 Banner -->
    <div v-if="banners.length > 0" class="banner-wrap">
      <div class="banner-carousel">
        <div
          class="banner-slide"
          :style="{ transform: `translateX(-${currentBanner * 100}%)` }"
        >
          <div
            v-for="(item, idx) in banners"
            :key="item.id || idx"
            class="banner-item"
            @click="goDetail(item)"
          >
            <img
              v-if="getBannerUrl(item)"
              :src="getBannerUrl(item)"
              :alt="item.name"
              class="banner-img"
            />
            <div v-else class="banner-fallback">
              <span>{{ item.name }}</span>
            </div>
            <div class="banner-overlay">
              <div class="banner-title">{{ item.name }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 指示器 -->
      <div class="banner-dots" v-if="banners.length > 1">
        <span
          v-for="(_, idx) in banners"
          :key="idx"
          class="dot"
          :class="{ active: idx === currentBanner }"
          @click="currentBanner = idx"
        ></span>
      </div>

      <!-- 左右切换按钮 -->
      <div v-if="banners.length > 1" class="banner-nav prev" @click="prevBanner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </div>
      <div v-if="banners.length > 1" class="banner-nav next" @click="nextBanner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="search-box">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
      </svg>
      <input v-model="keyword" placeholder="请输入活动名称模糊查询" @keyup.enter="handleSearch" />
    </div>

    <!-- Tab 栏 -->
    <div class="tab-bar">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'activity' }"
        @click="activeTab = 'activity'"
      >
        线上活动
        <div class="tab-line" v-if="activeTab === 'activity'"></div>
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'school' }"
        @click="activeTab = 'school'"
      >
        线下活动
        <div class="tab-line" v-if="activeTab === 'school'"></div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 活动列表 -->
    <div v-else class="activity-list">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="activity-card"
        @click="goDetail(item)"
      >
        <div v-if="getCoverUrl(item)" class="card-cover">
          <img :src="getCoverUrl(item)" :alt="item.name" />
        </div>
        <div class="card-header">
          <div class="card-title-row">
            <span class="card-title">{{ item.name }}</span>
          </div>
          <t-tag :theme="getStatusTheme(item.status)" variant="light" size="small">
            {{ getStatusLabel(item.status) }}
          </t-tag>
        </div>
        <div class="card-info">
          <div class="info-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>活动时间: {{ formatDate(item.activityStartTime) }} - {{ formatDate(item.activityEndTime) }}</span>
          </div>
          <div v-if="item.location" class="info-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span>活动地点: {{ item.location }}</span>
          </div>
        </div>
      </div>

      <div v-if="filteredList.length === 0" class="empty-wrap">
        <div class="empty-text">暂无活动</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import H5BottomNav from '../components/H5BottomNav.vue'

const router = useRouter()

// 活动数据
const activities = ref([])
const banners = ref([])
const loading = ref(false)
const keyword = ref('')
const activeTab = ref('activity')

// 轮播图控制
const currentBanner = ref(0)
let bannerTimer = null

const startAutoPlay = () => {
  stopAutoPlay()
  if (banners.value.length > 1) {
    bannerTimer = setInterval(() => {
      currentBanner.value = (currentBanner.value + 1) % banners.value.length
    }, 3500)
  }
}

const stopAutoPlay = () => {
  if (bannerTimer) {
    clearInterval(bannerTimer)
    bannerTimer = null
  }
}

const prevBanner = () => {
  if (banners.value.length <= 0) return
  currentBanner.value = (currentBanner.value - 1 + banners.value.length) % banners.value.length
  startAutoPlay()
}

const nextBanner = () => {
  if (banners.value.length <= 0) return
  currentBanner.value = (currentBanner.value + 1) % banners.value.length
  startAutoPlay()
}

// 按类型（线上0/线下1）和关键词筛选
const filteredList = computed(() => {
  let list = activities.value
  if (activeTab.value === 'activity') {
    list = list.filter(a => a.type === 0 || a.type === undefined || a.type === null)
  } else {
    list = list.filter(a => a.type === 1)
  }
  if (keyword.value && keyword.value.trim()) {
    const kw = keyword.value.trim()
    list = list.filter(a => a.name && a.name.includes(kw))
  }
  return list
})

// 获取活动列表
const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 1000 })
    activities.value = res.data?.records || res.data || []
  } catch (err) {
    console.error('获取活动列表失败', err)
    activities.value = []
  } finally {
    loading.value = false
  }
}

// 获取首页轮播活动
const fetchBanners = async () => {
  try {
    const res = await activityApi.getBannerActivities()
    let data = []
    if (Array.isArray(res.data)) {
      data = res.data
    } else if (res.data && Array.isArray(res.data.records)) {
      data = res.data.records
    } else if (res.data && Array.isArray(res.data.data)) {
      data = res.data.data
    }
    banners.value = data || []
  } catch (err) {
    console.error('获取轮播活动失败', err)
    banners.value = []
  }
}

// 获取卡片封面
const getCoverUrl = (item) => {
  if (!item) return ''
  if (item.bannerUrls && Array.isArray(item.bannerUrls) && item.bannerUrls.length > 0) {
    return getFileUrl(item.bannerUrls[0])
  }
  if (item.bannerUrl) {
    return getFileUrl(item.bannerUrl)
  }
  return ''
}

// 获取轮播图 banner URL
const getBannerUrl = (item) => {
  return getCoverUrl(item)
}

const handleSearch = () => {
  // 关键词筛选在 computed 中已实现，此处留空占位
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return String(dateStr).substring(0, 10)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  } catch (e) {
    return String(dateStr)
  }
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
  if (!item || !item.id) return
  router.push(`/h5/activity/${item.id}`)
}

onMounted(() => {
  fetchBanners().then(() => {
    startAutoPlay()
  })
  fetchActivities()
})

onUnmounted(() => {
  stopAutoPlay()
})
</script>

<style scoped>
.h5-teacher-activities {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

/* 头部 */
.home-header {
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  padding: 20px 16px 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.7);
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
}

.role-badge.teacher {
  color: #4f46e5;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

/* 轮播图 */
.banner-wrap {
  margin: 12px 16px 0;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #e5e7eb;
}

.banner-carousel {
  width: 100%;
  overflow: hidden;
  position: relative;
  height: 180px;
}

.banner-slide {
  display: flex;
  height: 100%;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.banner-item {
  flex: 0 0 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-fallback {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  padding: 0 20px;
  text-align: center;
}

.banner-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 14px 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
}

.banner-title {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 指示器 */
.banner-dots {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
  z-index: 2;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dot.active {
  width: 20px;
  border-radius: 3px;
  background: #fff;
}

/* 左右切换 */
.banner-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 3;
  transition: background 0.2s;
}

.banner-nav:hover {
  background: rgba(0, 0, 0, 0.5);
}

.banner-nav.prev {
  left: 10px;
}

.banner-nav.next {
  right: 10px;
}

/* 搜索框 */
.search-box {
  display: flex;
  align-items: center;
  margin: 12px 16px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 100px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  gap: 8px;
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
  color: #374151;
}

/* Tab 栏 */
.tab-bar {
  display: flex;
  align-items: center;
  background: #fff;
  margin: 0 16px 12px;
  padding: 0 4px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.tab-item {
  position: relative;
  flex: 1;
  text-align: center;
  padding: 12px 8px;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
}

.tab-item.active {
  color: #2563eb;
  font-weight: 600;
}

.tab-line {
  position: absolute;
  bottom: 4px;
  left: 50%;
  width: 32px;
  height: 3px;
  background: #2563eb;
  border-radius: 2px;
  transform: translateX(-50%);
}

/* Loading */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.loading-text, .empty-text {
  color: #9ca3af;
  font-size: 14px;
}

/* 活动列表 */
.activity-list {
  padding: 0 16px;
}

.activity-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 14px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.15s ease;
}

.activity-card:active {
  transform: scale(0.98);
}

/* 封面图 */
.card-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #e5e7eb;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 14px 10px;
  gap: 12px;
}

.card-title-row {
  flex: 1;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

/* 信息行 */
.card-info {
  padding: 0 14px 14px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
  gap: 6px;
}

.info-row:last-child {
  margin-bottom: 0;
}

/* 空状态 */
.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}
</style>
