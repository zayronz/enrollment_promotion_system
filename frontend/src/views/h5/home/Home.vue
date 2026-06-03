<template>
  <div class="h5-home">
    <!-- 头部 -->
    <div class="home-header">
      <div class="header-left">
        <div class="role-badge" :class="roleClass">{{ roleLabel }}</div>
        <div class="header-title">{{ headerTitle }}</div>
      </div>
      <div class="header-right">
        <div class="user-avatar" @click="router.push('/h5/profile')">
          <img v-if="userStore.avatarUrl" :src="userStore.avatarUrl" alt="avatar" />
          <span v-else>{{ userStore.realName?.charAt(0) || 'U' }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索 (学生角色) -->
    <div v-if="isStudent" class="search-box">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
      </svg>
      <input v-model="keyword" placeholder="请输入活动名称模糊查询" @keyup.enter="handleSearch" />
    </div>

    <!-- Tab (学生角色) -->
    <div v-if="isStudent" class="tab-bar">
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

    <!-- 学生：活动列表 -->
    <div v-else-if="isStudent" class="activity-list">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="activity-card"
        @click="goDetail(item)"
      >
        <div v-if="item.bannerUrl" class="card-cover">
          <img :src="getFileUrl(item.bannerUrl)" :alt="item.name" />
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

    <!-- 教师/学院/学校：快捷入口 -->
    <div v-else class="quick-entry">
      <!-- 教师端快捷入口 -->
      <template v-if="isTeacher">
        <div class="entry-card" @click="router.push('/h5/teacher/activities')">
          <div class="entry-icon" style="background: #dbeafe;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">活动报名</div>
            <div class="entry-desc">查看所有可参与的活动</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="entry-card" @click="router.push('/h5/teacher/my-registrations')">
          <div class="entry-icon" style="background: #fce7f3;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ec4899" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">我的活动</div>
            <div class="entry-desc">查看已报名的活动</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </template>

      <!-- 学院端快捷入口 -->
      <template v-if="isCollege">
        <div class="entry-card" @click="router.push('/h5/college/pending')">
          <div class="entry-icon" style="background: #d1fae5;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">报名审核</div>
            <div class="entry-desc">审核学生提交的报名</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="entry-card" @click="router.push('/h5/college/history')">
          <div class="entry-icon" style="background: #fef3c7;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">审核历史</div>
            <div class="entry-desc">查看历史审核记录</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="entry-card" @click="router.push('/h5/college/feedback')">
          <div class="entry-icon" style="background: #e0e7ff;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6366f1" stroke-width="2">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">反馈管理</div>
            <div class="entry-desc">查看学生提交的反馈</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </template>

      <!-- 学校端快捷入口 -->
      <template v-if="isSchool">
        <div class="entry-card" @click="router.push('/h5/school/dashboard')">
          <div class="entry-icon" style="background: #dbeafe;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">数据仪表盘</div>
            <div class="entry-desc">查看活动统计数据</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="entry-card" @click="router.push('/h5/school/activity-list')">
          <div class="entry-icon" style="background: #fce7f3;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ec4899" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">活动管理</div>
            <div class="entry-desc">管理学校发布的所有活动</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="entry-card" @click="router.push('/h5/school/audit-pending')">
          <div class="entry-icon" style="background: #d1fae5;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">审核管理</div>
            <div class="entry-desc">审核学生报名申请</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="entry-card" @click="router.push('/h5/school/user-list')">
          <div class="entry-icon" style="background: #fef3c7;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">用户管理</div>
            <div class="entry-desc">管理学校用户账号</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="entry-card" @click="router.push('/h5/school/feedback-list')">
          <div class="entry-icon" style="background: #e0e7ff;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6366f1" stroke-width="2">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            </svg>
          </div>
          <div class="entry-info">
            <div class="entry-title">反馈管理</div>
            <div class="entry-desc">查看所有学生反馈</div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </template>
    </div>

    <H5BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import H5BottomNav from '../components/H5BottomNav.vue'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('activity')
const keyword = ref('')
const loading = ref(false)
const activities = ref([])

// 角色判断
const role = computed(() => userStore.role)
const isStudent = computed(() => role.value === 'STUDENT')
const isTeacher = computed(() => role.value === 'TEACHER')
const isCollege = computed(() => role.value === 'COLLEGE')
const isSchool = computed(() => role.value === 'SCHOOL')

const roleLabel = computed(() => {
  const map = { 'STUDENT': '学生', 'TEACHER': '教师', 'COLLEGE': '学院', 'SCHOOL': '学校' }
  return map[role.value] || '用户'
})

const roleClass = computed(() => {
  const map = { 'STUDENT': 'student', 'TEACHER': 'teacher', 'COLLEGE': 'college', 'SCHOOL': 'school' }
  return map[role.value] || ''
})

const headerTitle = computed(() => {
  const map = {
    'STUDENT': '招生宣传',
    'TEACHER': '教师端',
    'COLLEGE': '院校端',
    'SCHOOL': '管理端'
  }
  return map[role.value] || '招生系统'
})

const filteredList = computed(() => {
  let list = activities.value
  if (activeTab.value === 'activity') {
    list = list.filter(a => a.type === 0)
  } else {
    list = list.filter(a => a.type === 1)
  }
  if (keyword.value) {
    list = list.filter(a => a.name && a.name.includes(keyword.value))
  }
  return list
})

const fetchActivities = async () => {
  if (!isStudent.value) return
  loading.value = true
  try {
    const res = await activityApi.getOpenActivities()
    activities.value = res.data?.records || res.data || []
  } catch (err) {
    console.error('获取活动列表失败', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {}

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
.h5-home {
  padding-bottom: 70px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.loading-text, .empty-text {
  color: #9ca3af;
  font-size: 14px;
}

.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.home-header {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  width: fit-content;
  font-weight: 500;
}

.role-badge.student { background: rgba(255,255,255,0.3); color: #fff; }
.role-badge.teacher { background: #f59e0b; color: #fff; }
.role-badge.college { background: #10b981; color: #fff; }
.role-badge.school { background: #6366f1; color: #fff; }

.header-title {
  color: #fff;
  font-size: 18px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar span {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.search-box {
  background-color: #fff;
  border-radius: 0 0 20px 20px;
  margin: 0 12px;
  position: relative;
  top: -8px;
  padding: 10px 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
}

.search-box svg { flex-shrink: 0; }

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  margin-left: 8px;
  font-size: 14px;
  color: #374151;
  background: transparent;
}

.search-box input::placeholder { color: #9ca3af; }

.tab-bar {
  display: flex;
  padding: 12px 16px 0;
  gap: 24px;
  background: #f5f7fa;
}

.tab-item {
  font-size: 15px;
  color: #6b7280;
  padding-bottom: 8px;
  position: relative;
  cursor: pointer;
  font-weight: 500;
}

.tab-item.active { color: #1f2937; }

.tab-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #2563eb;
  border-radius: 2px;
}

.activity-list { padding: 12px; }

.activity-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
}

.card-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
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
  padding: 12px 14px 8px;
}

.card-title-row { flex: 1; margin-right: 8px; }

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.card-info { padding: 0 14px 14px; }

.info-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}

.quick-entry { padding: 12px; }

.entry-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.entry-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
}

.entry-info { flex: 1; }

.entry-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.entry-desc {
  font-size: 13px;
  color: #9ca3af;
}
</style>
