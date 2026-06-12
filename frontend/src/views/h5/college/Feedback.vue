<template>
  <div class="h5-college-feedback">
    <H5NavBar title="反馈管理" />

    <div v-if="loading" class="loading-wrap">
      <svg class="spinner" width="32" height="32" viewBox="0 0 24 24">
        <circle cx="12" cy="12" r="10" stroke="#2563eb" stroke-width="3" fill="none" stroke-dasharray="31.4 31.4" stroke-linecap="round"></circle>
      </svg>
      <div class="loading-text">加载中...</div>
    </div>

    <div v-else class="content-wrap">
      <!-- 搜索 & 筛选 -->
      <div class="filter-bar">
        <div class="search-row">
          <div class="search-input-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              v-model="keyword"
              placeholder="搜索反馈标题或内容"
              @keyup.enter="handleSearch"
              @input="debouncedSearch"
            />
            <span v-if="keyword" class="clear-btn" @click="clearSearch">×</span>
          </div>
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>

        <!-- 活动筛选 -->
        <div class="activity-filter">
          <div
            class="filter-chip"
            :class="{ active: !activityFilter }"
            @click="setActivityFilter(null)"
          >
            全部
          </div>
          <div
            v-for="opt in activityOptions.slice(0, 8)"
            :key="opt.value"
            class="filter-chip"
            :class="{ active: String(activityFilter) === String(opt.value) }"
            @click="setActivityFilter(opt.value)"
          >
            {{ truncate(opt.label, 10) }}
          </div>
          <div v-if="activityOptions.length > 8" class="filter-chip more-chip">
            +{{ activityOptions.length - 8 }}
          </div>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-bar">
        <span>共 {{ viewList.length }} 条反馈</span>
        <span v-if="studentCount > 0" class="stat-item">学生 {{ studentCount }}</span>
        <span v-if="teacherCount > 0" class="stat-item">教师 {{ teacherCount }}</span>
      </div>

      <!-- 反馈列表 -->
      <div v-if="viewList.length > 0" class="feedback-list">
        <div
          v-for="item in viewList"
          :key="item.id"
          class="feedback-item"
          @click="goDetail(item)"
        >
          <div class="item-header">
            <span
              class="role-tag"
              :class="item.userRole === 'STUDENT' ? 'role-student' : 'role-teacher'"
            >
              {{ item.userRole === 'STUDENT' ? '学生' : '教师' }}
            </span>
            <span class="item-realname">{{ item.realName || '匿名' }}</span>
            <span class="item-time">{{ formatDateTime(item.createTime) }}</span>
          </div>

          <div class="item-title">{{ item.title || '反馈' }}</div>

          <div class="item-activity">
            <span class="activity-label">活动：</span>{{ item.activityTitle || '-' }}
          </div>

          <div class="item-content">
            {{ truncate(item.content, 100) }}
          </div>

          <div v-if="attachmentsOf(item).length > 0" class="item-attachments">
            <span class="attachment-icon">📎</span>
            <span>{{ attachmentsOf(item).length }} 个附件</span>
          </div>

          <div class="item-footer">
            <span class="view-detail">查看详情 →</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-wrap">
        <div class="empty-icon">📭</div>
        <div class="empty-text">{{ keyword ? '未找到匹配的反馈' : '暂无反馈记录' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { feedbackApi } from '@/api/feedback'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()

const loading = ref(false)
const records = ref([])
const keyword = ref('')
const activityFilter = ref(null)
const activityOptions = ref([])

let searchTimer = null

// 防抖搜索
const debouncedSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    // 纯前端筛选，无需请求
  }, 200)
}

// 统计
const studentCount = computed(() =>
  records.value.filter(r => r.userRole === 'STUDENT').length
)
const teacherCount = computed(() =>
  records.value.filter(r => r.userRole === 'TEACHER').length
)

// 筛选后的列表（前端实时筛选）
const viewList = computed(() => {
  let list = records.value
  // 活动筛选
  if (activityFilter.value) {
    list = list.filter(item => String(item.activityId) === String(activityFilter.value))
  }
  // 关键词筛选（标题或内容）
  if (keyword.value && keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(item =>
      (item.title && String(item.title).toLowerCase().includes(kw)) ||
      (item.content && String(item.content).toLowerCase().includes(kw))
    )
  }
  return list
})

// 获取附件（兼容多种格式）
const attachmentsOf = (row) => {
  if (!row) return []
  if (Array.isArray(row.attachments) && row.attachments.length > 0) {
    return row.attachments.map(a => {
      const url = a.url || a.fileUrl || a
      return getFileUrl(typeof url === 'string' ? url : String(url))
    })
  }
  if (row.attachmentUrls) {
    if (Array.isArray(row.attachmentUrls)) {
      return row.attachmentUrls.filter(Boolean).map(url => getFileUrl(url))
    }
    if (typeof row.attachmentUrls === 'string') {
      return row.attachmentUrls
        .split(',')
        .map(url => url.trim())
        .filter(Boolean)
        .map(url => getFileUrl(url))
    }
  }
  return []
}

// 获取反馈列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getCollegeFeedbacks()
    let data = []
    if (Array.isArray(res.data)) {
      data = res.data
    } else if (res.data && Array.isArray(res.data.records)) {
      data = res.data.records
    } else if (res.data && Array.isArray(res.data.data)) {
      data = res.data.data
    }
    records.value = data || []
  } catch (err) {
    console.error('获取反馈列表失败', err)
    records.value = []
  } finally {
    loading.value = false
  }
}

// 获取活动选项
const fetchActivities = async () => {
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 200 })
    let list = []
    if (Array.isArray(res.data)) {
      list = res.data
    } else if (res.data && Array.isArray(res.data.records)) {
      list = res.data.records
    }
    activityOptions.value = list.map(a => ({
      value: a.id,
      label: a.name || a.title || '活动'
    }))
  } catch (err) {
    console.error('获取活动列表失败', err)
  }
}

// 搜索
const handleSearch = () => {
  // 前端筛选已在 computed 中实现
}

// 清空搜索
const clearSearch = () => {
  keyword.value = ''
}

// 设置活动筛选
const setActivityFilter = (id) => {
  activityFilter.value = id
}

// 跳转详情页（通过 state 传递数据）
const goDetail = (item) => {
  if (!item || !item.id) return
  router.push({
    path: `/h5/college/feedback/${item.id}`,
    state: { feedbackRecord: item }
  })
}

// 文本截断
const truncate = (text, len) => {
  if (!text) return ''
  const str = String(text).replace(/\s+/g, ' ')
  if (str.length <= len) return str
  return str.substring(0, len) + '...'
}

// 格式化时间
const formatDateTime = (str) => {
  if (!str) return '-'
  try {
    const d = new Date(str)
    if (isNaN(d.getTime())) return String(str).substring(0, 16)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch (e) {
    return String(str).substring(0, 16)
  }
}

onMounted(() => {
  fetchActivities()
  fetchData()
})
</script>

<style scoped>
.h5-college-feedback {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40px;
}

/* Loading */
.loading-wrap {
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

.loading-text {
  color: #6b7280;
  font-size: 14px;
}

/* 内容区 */
.content-wrap {
  padding: 12px;
}

/* 筛选栏 */
.filter-bar {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

/* 搜索行 */
.search-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
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
  padding: 9px 18px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
}

.search-btn:active {
  background: #1d4ed8;
}

/* 活动筛选 chip */
.activity-filter {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-chip {
  background: #f3f4f6;
  color: #6b7280;
  padding: 6px 14px;
  border-radius: 100px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}

.filter-chip.active {
  background: #2563eb;
  color: #fff;
}

.filter-chip.more-chip {
  background: #e5e7eb;
  color: #9ca3af;
}

/* 统计信息 */
.stats-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 10px 14px;
  border-radius: 10px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #6b7280;
}

.stat-item {
  color: #2563eb;
  font-weight: 500;
}

/* 反馈列表 */
.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feedback-item {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.feedback-item:active {
  transform: scale(0.98);
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.08);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.role-tag {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 100px;
  font-weight: 500;
}

.role-student {
  background: #dbeafe;
  color: #1d4ed8;
}

.role-teacher {
  background: #fef3c7;
  color: #d97706;
}

.item-realname {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.item-time {
  font-size: 12px;
  color: #9ca3af;
  margin-left: auto;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.item-activity {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.activity-label {
  color: #9ca3af;
}

.item-content {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 10px;
}

.item-attachments {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f3f4f6;
  border-radius: 8px;
  font-size: 12px;
  color: #6b7280;
  width: fit-content;
  margin-bottom: 10px;
}

.attachment-icon {
  font-size: 14px;
}

.item-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 6px;
  border-top: 1px solid #f3f4f6;
}

.view-detail {
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
  padding: 60px 20px;
  gap: 10px;
}

.empty-icon {
  font-size: 52px;
}

.empty-text {
  color: #6b7280;
  font-size: 14px;
}
</style>
