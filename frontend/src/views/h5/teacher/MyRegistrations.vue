<template>
  <div class="teacher-registrations">
    <H5NavBar title="我的活动" />

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else-if="records.length > 0" class="registration-list">
      <div
        v-for="item in records"
        :key="item.id"
        class="registration-item"
      >
        <div class="item-header">
          <div class="item-title">{{ item.activityTitle || '活动' }}</div>
          <t-tag :theme="getStatusTheme(item.status)" variant="light" size="small">
            {{ getStatusLabel(item.status) }}
          </t-tag>
        </div>
        <div class="item-info">
          <div class="info-row"><span class="info-label">目标学校：</span>{{ item.targetSchool || '-' }}</div>
          <div class="info-row"><span class="info-label">报名时间：</span>{{ formatDateTime(item.createTime) }}</div>
        </div>
      </div>
    </div>

    <div v-else class="empty-wrap">
      <div class="empty-text">暂无报名记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { registrationApi } from '@/api/registeration'
import H5NavBar from '../components/H5NavBar.vue'

const records = ref([])
const loading = ref(true)

const fetchRegistrations = async () => {
  loading.value = true
  try {
    const res = await registrationApi.getMyRegistrations({ page: 1, size: 100 })
    records.value = res.data?.records || []
  } catch (err) {
    console.error('获取报名列表失败', err)
  } finally {
    loading.value = false
  }
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

const getStatusTheme = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'default' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '待审核', 1: '学院通过', 2: '已通过', 3: '已拒绝', 4: '已撤回' }
  return map[status] || '未知'
}

onMounted(fetchRegistrations)
</script>

<style scoped>
.teacher-registrations { min-height: 100vh; background: #f5f7fa; padding-bottom: 80px; }
.loading-wrap { display: flex; justify-content: center; padding: 60px 0; }
.registration-list { padding: 12px; }
.registration-item { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.item-title { font-size: 16px; font-weight: 600; color: #1f2937; flex: 1; margin-right: 12px; }
.item-info { font-size: 13px; color: #6b7280; }
.info-row { display: flex; line-height: 1.8; }
.info-label { color: #9ca3af; flex-shrink: 0; }
.empty-wrap { display: flex; justify-content: center; align-items: center; padding: 60px 0; }
.empty-text { color: #9ca3af; font-size: 14px; }
</style>
