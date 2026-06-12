<template>
  <div class="h5-teacher-activities">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrap">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input
          v-model="keyword"
          placeholder="请输入活动名称模糊查询"
          @keyup.enter="handleSearch"
          @input="debouncedSearch"
        />
        <span v-if="keyword" class="clear-btn" @click="clearSearch">×</span>
      </div>
      <button class="search-btn" @click="handleSearch">搜索</button>
    </div>

    <!-- Tab 栏 -->
    <div class="tab-bar">
      <div
        class="tab-item"
        :class="{ active: currentType === null }"
        @click="switchTab(null)"
      >
        全部活动
        <span class="tab-count" v-if="allCount > 0">({{ allCount }})</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: currentType === 0 }"
        @click="switchTab(0)"
      >
        线上活动
        <span class="tab-count" v-if="onlineCount > 0">({{ onlineCount }})</span>
      </div>
      <div
        class="tab-item"
        :class="{ active: currentType === 1 }"
        @click="switchTab(1)"
      >
        线下活动
        <span class="tab-count" v-if="offlineCount > 0">({{ offlineCount }})</span>
      </div>
    </div>

    <!-- Loading 状态 -->
    <div v-if="loading" class="state-wrap">
      <svg class="spinner" width="32" height="32" viewBox="0 0 24 24">
        <circle cx="12" cy="12" r="10" stroke="#2563eb" stroke-width="3" fill="none" stroke-dasharray="31.4 31.4" stroke-linecap="round"></circle>
      </svg>
      <div class="state-text">加载中...</div>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="errorMsg" class="state-wrap">
      <div class="state-icon">⚠️</div>
      <div class="state-text">{{ errorMsg }}</div>
      <button class="retry-btn" @click="fetchActivities">重新加载</button>
    </div>

    <!-- 活动列表 -->
    <div v-else class="activity-list">
      <div
        v-for="item in activities"
        :key="item.id"
        class="activity-card"
        @click="goDetail(item)"
      >
        <!-- 封面图 -->
        <div v-if="getCoverUrl(item)" class="card-cover">
          <img :src="getCoverUrl(item)" :alt="item.name" />
        </div>
        <div v-else class="card-cover card-cover-placeholder">
          <span class="placeholder-icon">{{ getTypeIcon(item.type) }}</span>
        </div>

        <!-- 标题行 + 状态 -->
        <div class="card-header">
          <span class="card-title">{{ item.name || '未命名活动' }}</span>
          <span
            class="status-badge"
            :class="{
              'status-draft': item.status === 0,
              'status-published': item.status === 1,
              'status-ended': item.status === 2
            }"
          >{{ getStatusLabel(item.status) }}</span>
        </div>

        <!-- 简介 -->
        <div v-if="item.description" class="card-desc">
          {{ truncate(item.description, 80) }}
        </div>

        <!-- 信息行 -->
        <div class="card-info">
          <div class="info-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6b7280" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>活动时间：{{ formatDateRange(item.activityStartTime, item.activityEndTime) }}</span>
          </div>
          <div class="info-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6b7280" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span>活动地点：{{ item.location || '线上宣讲' }}</span>
          </div>
        </div>

        <!-- 报名时间提示 -->
        <div class="card-footer">
          <span class="reg-period">
            报名时间：{{ formatDateRange(item.registrationStartTime, item.registrationEndTime) }}
          </span>
          <span class="go-arrow">查看详情 →</span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="activities.length === 0" class="empty-wrap">
        <div class="empty-icon">📭</div>
        <div class="empty-text">{{ keyword ? '未找到匹配的活动' : '暂无活动' }}</div>
        <div v-if="keyword" class="empty-sub">试试其他关键词，或清空搜索条件</div>
        <button v-if="keyword" class="retry-btn" @click="clearSearch">清空搜索</button>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item active">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
          <polyline points="9 22 9 12 15 12 15 22"></polyline>
        </svg>
        <span>活动广场</span>
      </div>
      <div class="nav-item" @click="navMore">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
          <circle cx="5" cy="12" r="1.5"></circle>
          <circle cx="12" cy="12" r="1.5"></circle>
          <circle cx="19" cy="12" r="1.5"></circle>
        </svg>
        <span>更多</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'

const router = useRouter()

// 状态
const activities = ref([])
const allCount = ref(0)
const onlineCount = ref(0)
const offlineCount = ref(0)
const loading = ref(false)
const errorMsg = ref('')
const keyword = ref('')
const currentType = ref(null) // null = 全部, 0 = 线上, 1 = 线下

let searchTimer = null

// 防抖搜索
const debouncedSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchActivities()
  }, 300)
}

// 手动搜索
const handleSearch = () => {
  fetchActivities()
}

// 清空搜索
const clearSearch = () => {
  keyword.value = ''
  fetchActivities()
}

// 切换 Tab
const switchTab = (type) => {
  currentType.value = type
  fetchActivities()
}

// 获取活动列表（后端分页 + 关键词 + 类型筛选）
const fetchActivities = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const params = { page: 1, size: 1000 }
    if (currentType.value !== null && currentType.value !== undefined) {
      params.type = currentType.value
    }
    if (keyword.value && keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }

    const res = await activityApi.getActivityList(params)

    let data = []
    if (Array.isArray(res.data)) {
      data = res.data
    } else if (res.data && Array.isArray(res.data.records)) {
      data = res.data.records
    } else if (res.data && Array.isArray(res.data.data)) {
      data = res.data.data
    } else if (res.data && res.data.total !== undefined) {
      data = []
    }

    // 额外过滤：只保留已发布（status=1）或已结束（status=2）的活动 — 教师端只看学校发布的
    const visible = (data || []).filter(item => item.status === 1 || item.status === 2)

    activities.value = visible
    allCount.value = visible.length
    onlineCount.value = visible.filter(a => a.type === 0 || a.type === undefined || a.type === null).length
    offlineCount.value = visible.filter(a => a.type === 1).length
  } catch (err) {
    console.error('获取活动列表失败', err)
    errorMsg.value = '获取活动列表失败，请稍后重试'
    activities.value = []
  } finally {
    loading.value = false
  }
}

// 获取封面图 URL（优先 coverImage，再 bannerUrl）
const getCoverUrl = (item) => {
  if (!item) return ''
  if (item.coverImage) {
    return getFileUrl(item.coverImage)
  }
  if (item.bannerUrl) {
    return getFileUrl(item.bannerUrl)
  }
  if (item.bannerUrls && Array.isArray(item.bannerUrls) && item.bannerUrls.length > 0) {
    return getFileUrl(item.bannerUrls[0])
  }
  return ''
}

// 获取类型图标
const getTypeIcon = (type) => {
  if (type === 1) return '🏫'
  return '💻'
}

// 格式化日期
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

// 状态标签
const getStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[status] || '已发布'
}

// 截断长文本
const truncate = (text, len) => {
  if (!text) return ''
  const str = String(text).replace(/\s+/g, ' ')
  if (str.length <= len) return str
  return str.substring(0, len) + '...'
}

// 跳转详情
const goDetail = (item) => {
  if (!item || !item.id) return
  router.push(`/h5/teacher/activity/${item.id}`)
}

// 跳更多
const navMore = () => {
  router.push('/h5/teacher/my-registrations')
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.h5-teacher-activities {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #1f2937;
}

/* 搜索栏 */
.search-bar {
  padding: 12px 14px 14px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f3f4f6;
  border-radius: 100px;
  flex: 1;
}

.search-input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #374151;
}

.clear-btn {
  width: 18px;
  height: 18px;
  line-height: 1;
  border-radius: 50%;
  background: #d1d5db;
  color: #fff;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.search-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 100px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
}

.search-btn:active {
  background: #1d4ed8;
}

/* Tab 栏 */
.tab-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  margin: 0 8px 10px;
  padding: 0 8px;
  border-radius: 10px;
  overflow-x: auto;
}

.tab-item {
  position: relative;
  text-align: center;
  padding: 12px 14px;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.tab-count {
  color: #9ca3af;
  font-size: 12px;
  margin-left: 2px;
}

.tab-item.active {
  color: #2563eb;
  font-weight: 600;
}

.tab-item.active .tab-count {
  color: #2563eb;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 2px;
  background: #2563eb;
  border-radius: 2px;
}

/* Loading / Error / Empty */
.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 12px;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.state-icon {
  font-size: 48px;
}

.state-text {
  color: #6b7280;
  font-size: 14px;
}

.retry-btn {
  background: #fff;
  color: #2563eb;
  border: 1px solid #2563eb;
  border-radius: 100px;
  padding: 8px 20px;
  font-size: 13px;
  cursor: pointer;
  margin-top: 8px;
}

.retry-btn:active {
  background: #eff6ff;
}

/* 活动列表 */
.activity-list {
  padding: 0 14px;
}

.activity-card {
  background: #fff;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.activity-card:active {
  transform: scale(0.98);
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.08);
}

/* 封面图 */
.card-cover {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #e5e7eb;
  position: relative;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-cover-placeholder {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 60%, #ffffff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-icon {
  font-size: 48px;
  opacity: 0.65;
}

/* 标题行 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px 8px;
  gap: 10px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  flex-shrink: 0;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 100px;
  background: #ecfdf5;
  color: #10b981;
  font-weight: 500;
}

.status-badge.status-draft {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.status-published {
  background: #ecfdf5;
  color: #10b981;
}

.status-badge.status-ended {
  background: #fef3c7;
  color: #d97706;
}

/* 简介 */
.card-desc {
  padding: 0 14px 10px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
}

/* 信息行 */
.card-info {
  padding: 0 14px 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
}

/* 底部报名时间 */
.card-footer {
  padding: 10px 14px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f3f4f6;
}

.reg-period {
  font-size: 12px;
  color: #9ca3af;
}

.go-arrow {
  font-size: 13px;
  color: #2563eb;
  font-weight: 500;
}

/* 空状态 */
.empty-wrap {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 80px 20px;
  gap: 10px;
}

.empty-icon {
  font-size: 52px;
}

.empty-text {
  color: #6b7280;
  font-size: 14px;
}

.empty-sub {
  color: #9ca3af;
  font-size: 12px;
}

/* 底部导航栏 */
.bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 60px;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-around;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
  z-index: 20;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  font-size: 12px;
  color: #6b7280;
}

.nav-item.active {
  color: #2563eb;
}

.nav-item.active span {
  font-weight: 600;
}
</style>
