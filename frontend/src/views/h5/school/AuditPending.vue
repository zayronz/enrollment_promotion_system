<template>
  <div class="audit-pending">
    <H5NavBar title="审核管理" />

    <!-- 筛选 -->
    <div class="filter-section">
      <div class="search-box">
        <input v-model="keyword" placeholder="搜索报名人或学校..." @keyup.enter="fetchData" />
      </div>
      <div class="filter-tabs">
        <div
          class="filter-tab"
          :class="{ active: !activityFilter }"
          @click="activityFilter = ''; fetchData()"
        >
          全部
        </div>
        <div
          v-for="act in activityOptions"
          :key="act.value"
          class="filter-tab"
          :class="{ active: activityFilter === act.value }"
          @click="activityFilter = act.value; fetchData()"
        >
          {{ act.label }}
        </div>
      </div>
    </div>

    <!-- 待审核列表 -->
    <div class="list-content">
      <div v-for="item in records" :key="item.id" class="audit-card">
        <div class="card-header">
          <div class="user-info">
            <span class="user-name">{{ item.realName }}</span>
            <t-tag :theme="item.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
              {{ item.userType === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
          </div>
          <span class="apply-time">{{ formatDateTime(item.createTime) }}</span>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="label">学院:</span>
            <span class="value">{{ item.collegeName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">目标学校:</span>
            <span class="value">{{ item.targetSchool || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">活动:</span>
            <span class="value">{{ item.activityTitle }}</span>
          </div>
        </div>

        <div class="card-actions">
          <t-button theme="primary" variant="outline" size="small" @click="showDetail(item)">
            详情
          </t-button>
          <t-button theme="success" variant="outline" size="small" @click="handleAudit(item.id, 'APPROVED')">
            通过
          </t-button>
          <t-button theme="danger" variant="outline" size="small" @click="handleAudit(item.id, 'REJECTED')">
            拒绝
          </t-button>
        </div>
      </div>

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="!loading && records.length === 0" class="empty-text">暂无待审核数据</div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
        加载更多
      </div>
    </div>

    <!-- 详情弹窗 -->
    <t-popup v-model:visible="detailVisible" placement="bottom">
      <div class="detail-panel">
        <div class="panel-header">
          <div class="panel-title">报名详情</div>
          <div class="panel-close" @click="detailVisible = false">×</div>
        </div>
        <div class="panel-body">
          <div class="detail-row">
            <span class="detail-label">报名人:</span>
            <span class="detail-value">{{ currentRecord?.realName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">类型:</span>
            <t-tag :theme="currentRecord?.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
              {{ currentRecord?.userType === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
          </div>
          <div class="detail-row">
            <span class="detail-label">学院:</span>
            <span class="detail-value">{{ currentRecord?.collegeName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">目标学校:</span>
            <span class="detail-value">{{ currentRecord?.targetSchool || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">成绩/绩点:</span>
            <span class="detail-value">{{ currentRecord?.score || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">报名时间:</span>
            <span class="detail-value">{{ formatDateTime(currentRecord?.createTime) }}</span>
          </div>

          <div class="action-buttons">
            <t-button theme="success" block @click="doAudit('APPROVED')">通过</t-button>
            <t-button theme="danger" variant="outline" block @click="doAudit('REJECTED')">拒绝</t-button>
          </div>
        </div>
      </div>
    </t-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { registrationApi } from '@/api/registeration'
import { auditApi } from '@/api/audit'
import { activityApi } from '@/api/activity'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const activityFilter = ref('')
const activityOptions = ref([])
const current = ref(1)
const hasMore = ref(true)
const detailVisible = ref(false)
const currentRecord = ref(null)

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await registrationApi.getPendingAudit({
      page: 1,
      size: 10,
      node: 'school_audit',
      keyword: keyword.value || undefined,
      activityId: activityFilter.value || undefined
    })
    records.value = res.data?.records || []
    current.value = 1
    hasMore.value = records.value.length >= 10
  } catch (err) {
    console.error('获取待审核列表失败', err)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  loading.value = true
  try {
    const res = await registrationApi.getPendingAudit({
      page: current.value + 1,
      size: 10,
      node: 'school_audit',
      keyword: keyword.value || undefined,
      activityId: activityFilter.value || undefined
    })
    const newItems = res.data?.records || []
    records.value = [...records.value, ...newItems]
    current.value++
    hasMore.value = newItems.length >= 10
  } catch (err) {
    console.error('加载更多失败', err)
  } finally {
    loading.value = false
  }
}

const fetchActivities = async () => {
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 100 })
    activityOptions.value = (res.data?.records || []).map(a => ({
      value: a.id,
      label: a.name
    }))
  } catch (err) {
    console.error(err)
  }
}

const showDetail = (item) => {
  currentRecord.value = item
  detailVisible.value = true
}

const handleAudit = async (id, result) => {
  const label = result === 'APPROVED' ? '通过' : '拒绝'
  try {
    await auditApi.audit({ registrationId: id, passed: result === 'APPROVED', comment: '' })
    MessagePlugin.success(`已${label}`)
    fetchData()
  } catch (err) {
    console.error('审核失败', err)
  }
}

const doAudit = async (result) => {
  if (!currentRecord.value) return
  const label = result === 'APPROVED' ? '通过' : '拒绝'
  try {
    await auditApi.audit({ registrationId: currentRecord.value.id, passed: result === 'APPROVED', comment: '' })
    MessagePlugin.success(`已${label}`)
    detailVisible.value = false
    fetchData()
  } catch (err) {
    console.error('审核失败', err)
  }
}

onMounted(() => {
  fetchData()
  fetchActivities()
})
</script>

<style scoped>
.audit-pending {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.filter-section {
  padding: 12px;
  background: #fff;
  margin-bottom: 12px;
}

.search-box {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
}

.search-box input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.filter-tab {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  white-space: nowrap;
  background: #f5f7fa;
  color: #6b7280;
  cursor: pointer;
}

.filter-tab.active {
  background: #2563eb;
  color: #fff;
}

.list-content {
  padding: 0 12px;
}

.audit-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.apply-time {
  font-size: 12px;
  color: #9ca3af;
}

.card-body {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  font-size: 13px;
  margin-bottom: 6px;
}

.info-row .label {
  color: #6b7280;
  margin-right: 8px;
  flex-shrink: 0;
}

.info-row .value {
  color: #1f2937;
}

.card-actions {
  display: flex;
  gap: 8px;
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

/* 详情弹窗样式 */
.detail-panel {
  background: #fff;
  border-radius: 16px 16px 0 0;
  max-height: 80vh;
  overflow-y: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  background: #fff;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.panel-close {
  font-size: 28px;
  color: #9ca3af;
  cursor: pointer;
  line-height: 1;
}

.panel-body {
  padding: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-label {
  color: #6b7280;
  margin-right: 12px;
  flex-shrink: 0;
  width: 80px;
}

.detail-value {
  color: #1f2937;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-bottom: 20px;
}
</style>
