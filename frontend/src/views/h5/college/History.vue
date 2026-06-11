<template>
  <div class="history-page">
    <H5NavBar title="审核历史" />

    <!-- 搜索筛选区域 -->
    <div class="filter-section">
      <div class="search-bar">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none">
          <circle cx="11" cy="11" r="7" stroke="#9ca3af" stroke-width="2"/>
          <path d="M16 16l4 4" stroke="#9ca3af" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索报名人或学校..."
          @input="debounceFetch"
        />
        <span v-if="keyword" class="clear-btn" @click="clearKeyword">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="#d1d5db"/>
            <path d="M8 8l8 8M16 8l-8 8" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </span>
      </div>

      <div class="filter-row">
        <div class="filter-item" :class="{ active: showActivityFilter }" @click="toggleActivityFilter">
          <span>{{ selectedActivityLabel || '选择活动' }}</span>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
            <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="filter-item" :class="{ active: resultFilter }" @click="toggleResultFilter">
          <span>{{ resultFilterLabel }}</span>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
            <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
      </div>

      <!-- 活动筛选下拉 -->
      <div v-if="showActivityFilter" class="dropdown-panel">
        <div class="dropdown-item" :class="{ selected: !activityFilter }" @click="selectActivity('')">全部活动</div>
        <div
          v-for="a in activityOptions"
          :key="a.value"
          class="dropdown-item"
          :class="{ selected: activityFilter === a.value }"
          @click="selectActivity(a.value, a.label)"
        >
          {{ a.label }}
        </div>
      </div>

      <!-- 审核结果筛选 -->
      <div v-if="showResultFilter" class="dropdown-panel">
        <div class="dropdown-item" :class="{ selected: !resultFilter }" @click="selectResult('')">全部结果</div>
        <div class="dropdown-item" :class="{ selected: resultFilter === 'APPROVED' }" @click="selectResult('APPROVED')">已通过</div>
        <div class="dropdown-item" :class="{ selected: resultFilter === 'REJECTED' }" @click="selectResult('REJECTED')">已拒绝</div>
      </div>
    </div>

    <!-- 遮罩层 -->
    <div v-if="showActivityFilter || showResultFilter" class="mask" @click="closeFilters"></div>

    <!-- 列表内容 -->
    <div class="list-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <EmptyState
        v-else-if="!loading && records.length === 0"
        type="empty"
        title="暂无审核记录"
        description="没有找到符合条件的审核历史"
      />

      <!-- 列表 -->
      <div v-else class="record-list">
        <div
          v-for="item in records"
          :key="item.id"
          class="record-card"
          @click="showDetail(item)"
        >
          <div class="card-main">
            <div class="card-left">
              <div class="user-avatar">
                {{ (item.realName || '未知').charAt(0) }}
              </div>
              <div class="user-info">
                <div class="user-name">{{ item.realName || '-' }}</div>
                <div class="user-meta">
                  <span class="user-type" :class="item.userType === 'STUDENT' ? 'student' : 'teacher'">
                    {{ item.userType === 'STUDENT' ? '学生' : '教师' }}
                  </span>
                  <span class="activity-title">{{ item.activityTitle || '-' }}</span>
                </div>
              </div>
            </div>
            <div class="card-right">
              <span class="result-tag" :class="item.result === 'APPROVED' ? 'approved' : 'rejected'">
                {{ item.result === 'APPROVED' ? '已通过' : '已拒绝' }}
              </span>
            </div>
          </div>
          <div class="card-footer">
            <div class="footer-item">
              <span class="label">目标学校：</span>
              <span class="value">{{ item.targetSchool || '-' }}</span>
            </div>
            <div class="footer-item">
              <span class="label">审核时间：</span>
              <span class="value">{{ formatDateTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="records.length > 0" class="pagination">
        <button
          class="page-btn"
          :disabled="pagination.current <= 1"
          @click="handlePrevPage"
        >
          上一页
        </button>
        <span class="page-info">{{ pagination.current }} / {{ totalPages }}</span>
        <button
          class="page-btn"
          :disabled="pagination.current >= totalPages"
          @click="handleNextPage"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 详情抽屉 -->
    <div v-if="showDrawer" class="drawer-mask" @click="closeDrawer"></div>
    <div class="detail-drawer" :class="{ open: showDrawer }">
      <div class="drawer-header">
        <div class="drawer-title">审核详情</div>
        <div class="drawer-close" @click="closeDrawer">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
      </div>
      <div v-if="currentRecord" class="drawer-content">
        <div class="detail-section">
          <div class="detail-label">报名人</div>
          <div class="detail-value">{{ currentRecord.realName || '-' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">类型</div>
          <div class="detail-value">
            <span class="user-type" :class="currentRecord.userType === 'STUDENT' ? 'student' : 'teacher'">
              {{ currentRecord.userType === 'STUDENT' ? '学生' : '教师' }}
            </span>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-label">活动名称</div>
          <div class="detail-value">{{ currentRecord.activityTitle || '-' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">目标学校</div>
          <div class="detail-value">{{ currentRecord.targetSchool || '-' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">审核结果</div>
          <div class="detail-value">
            <span class="result-tag" :class="currentRecord.result === 'APPROVED' ? 'approved' : 'rejected'">
              {{ currentRecord.result === 'APPROVED' ? '已通过' : '已拒绝' }}
            </span>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-label">审核意见</div>
          <div class="detail-value">{{ currentRecord.comment || '-' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">审核时间</div>
          <div class="detail-value">{{ formatDateTime(currentRecord.createTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { auditApi } from '@/api/audit'
import { activityApi } from '@/api/activity'
import H5NavBar from '../components/H5NavBar.vue'
import EmptyState from '../components/EmptyState.vue'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const activityFilter = ref('')
const resultFilter = ref('')
const activityOptions = ref([])
const showActivityFilter = ref(false)
const showResultFilter = ref(false)
const selectedActivityLabel = ref('')
const showDrawer = ref(false)
const currentRecord = ref(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const totalPages = computed(() => {
  return Math.ceil(pagination.value.total / pagination.value.pageSize) || 1
})

const resultFilterLabel = computed(() => {
  const map = { APPROVED: '已通过', REJECTED: '已拒绝' }
  return resultFilter.value ? map[resultFilter.value] : '审核结果'
})

let debounceTimer = null
const debounceFetch = () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    pagination.value.current = 1
    fetchData()
  }, 300)
}

const fetchData = async () => {
  loading.value = true
  closeFilters()
  try {
    const res = await auditApi.getAuditHistory({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      activityId: activityFilter.value || undefined,
      result: resultFilter.value || undefined
    })
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (err) {
    console.error('获取审核历史失败', err)
  } finally {
    loading.value = false
  }
}

const fetchActivities = async () => {
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 100 })
    activityOptions.value = (res.data?.records || []).map(a => ({
      value: a.id,
      label: a.title
    }))
  } catch (err) {
    console.error(err)
  }
}

const toggleActivityFilter = () => {
  showResultFilter.value = false
  showActivityFilter.value = !showActivityFilter.value
}

const toggleResultFilter = () => {
  showActivityFilter.value = false
  showResultFilter.value = !showResultFilter.value
}

const closeFilters = () => {
  showActivityFilter.value = false
  showResultFilter.value = false
}

const selectActivity = (value, label) => {
  activityFilter.value = value
  selectedActivityLabel.value = label || ''
  pagination.value.current = 1
  fetchData()
}

const selectResult = (value) => {
  resultFilter.value = value
  pagination.value.current = 1
  fetchData()
}

const clearKeyword = () => {
  keyword.value = ''
  pagination.value.current = 1
  fetchData()
}

const handlePrevPage = () => {
  if (pagination.value.current > 1) {
    pagination.value.current--
    fetchData()
  }
}

const handleNextPage = () => {
  if (pagination.value.current < totalPages.value) {
    pagination.value.current++
    fetchData()
  }
}

const showDetail = (item) => {
  currentRecord.value = item
  showDrawer.value = true
}

const closeDrawer = () => {
  showDrawer.value = false
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
  fetchActivities()
})
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.filter-section {
  position: sticky;
  top: 44px;
  z-index: 90;
  background: #fff;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 20px;
  padding: 8px 12px;
  margin-bottom: 10px;
}

.search-icon {
  flex-shrink: 0;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
  color: #1f2937;
}

.search-input::placeholder {
  color: #9ca3af;
}

.clear-btn {
  flex-shrink: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.filter-row {
  display: flex;
  gap: 10px;
}

.filter-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-item.active {
  background: #e0e7ff;
  color: #4f46e5;
}

.dropdown-panel {
  position: absolute;
  left: 12px;
  right: 12px;
  top: calc(44px + 90px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
}

.dropdown-item {
  padding: 12px 16px;
  font-size: 14px;
  color: #374151;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-item.selected {
  color: #4f46e5;
  background: #e0e7ff;
}

.mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  z-index: 80;
}

.list-content {
  padding: 12px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.record-card:active {
  transform: scale(0.98);
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.card-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 10px;
}

.card-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-info {
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.user-type {
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.user-type.student {
  background: #dbeafe;
  color: #2563eb;
}

.user-type.teacher {
  background: #fef3c7;
  color: #d97706;
}

.activity-title {
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

.result-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.result-tag.approved {
  background: #d1fae5;
  color: #059669;
}

.result-tag.rejected {
  background: #fee2e2;
  color: #dc2626;
}

.card-footer {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 10px;
  border-top: 1px solid #f3f4f6;
}

.footer-item {
  display: flex;
  font-size: 12px;
}

.footer-item .label {
  color: #9ca3af;
  flex-shrink: 0;
}

.footer-item .value {
  color: #6b7280;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
  padding: 10px 0;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:not(:disabled):active {
  background: #f5f7fa;
}

.page-info {
  font-size: 13px;
  color: #6b7280;
}

.drawer-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  z-index: 200;
}

.detail-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 85%;
  max-width: 360px;
  background: #fff;
  z-index: 210;
  transform: translateX(100%);
  transition: transform 0.3s ease;
  display: flex;
  flex-direction: column;
}

.detail-drawer.open {
  transform: translateX(0);
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.drawer-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #6b7280;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 6px;
}

.detail-value {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
}
</style>
