<template>
  <div class="user-list">
    <H5NavBar title="用户管理" />

    <!-- 搜索框 -->
    <div class="search-box">
      <input v-model="keyword" placeholder="搜索用户名或姓名..." @keyup.enter="fetchData" />
    </div>

    <!-- 用户列表 -->
    <div class="list-content">
      <div v-for="item in users" :key="item.id" class="user-card">
        <div class="user-header">
          <div class="user-avatar">
            <img v-if="item.avatar" :src="item.avatar" alt="avatar" />
            <span v-else>{{ item.realName?.charAt(0) || 'U' }}</span>
          </div>
          <div class="user-info">
            <div class="user-name">{{ item.realName }}</div>
            <div class="user-role">
              <t-tag :theme="getRoleTheme(item.role)" variant="light" size="small">
                {{ getRoleLabel(item.role) }}
              </t-tag>
              <t-tag :theme="getStatusTheme(item.status)" variant="light" size="small" style="margin-left: 6px;">
                {{ getStatusLabel(item.status) }}
              </t-tag>
            </div>
          </div>
        </div>
        <div class="user-meta">
          <div class="meta-item">
            <span class="label">用户名:</span>
            <span class="value">{{ item.username }}</span>
          </div>
          <div v-if="item.collegeName" class="meta-item">
            <span class="label">学院:</span>
            <span class="value">{{ item.collegeName }}</span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="!loading && users.length === 0" class="empty-text">暂无用户</div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
        加载更多
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import H5NavBar from '../components/H5NavBar.vue'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const current = ref(1)
const hasMore = ref(true)

const getRoleTheme = (role) => {
  const map = { 'STUDENT': 'primary', 'TEACHER': 'warning', 'COLLEGE': 'success', 'SCHOOL': 'danger' }
  return map[role] || 'default'
}

const getRoleLabel = (role) => {
  const map = { 'STUDENT': '学生', 'TEACHER': '教师', 'COLLEGE': '学院', 'SCHOOL': '学校' }
  return map[role] || '未知'
}

const getStatusTheme = (status) => {
  return (status === 1 || status === 'ACTIVE') ? 'success' : 'danger'
}

const getStatusLabel = (status) => {
  return (status === 1 || status === 'ACTIVE') ? '正常' : '禁用'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/list', {
      params: { page: 1, size: 10, keyword: keyword.value || undefined }
    })
    users.value = res.data?.records || res.data || []
    current.value = 1
    hasMore.value = users.value.length >= 10
  } catch (err) {
    console.error('获取用户列表失败', err)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res = await request.get('/user/list', {
      params: { page: current.value + 1, size: 10, keyword: keyword.value || undefined }
    })
    const newItems = res.data?.records || res.data || []
    users.value = [...users.value, ...newItems]
    current.value++
    hasMore.value = newItems.length >= 10
  } catch (err) {
    console.error('加载更多失败', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-list {
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

.list-content {
  padding: 0 12px;
}

.user-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #dbeafe;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar span {
  font-size: 20px;
  font-weight: 600;
  color: #2563eb;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.user-meta {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  font-size: 13px;
  margin-bottom: 6px;
}

.meta-item .label {
  color: #6b7280;
  margin-right: 8px;
  flex-shrink: 0;
}

.meta-item .value {
  color: #1f2937;
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
