<template>
  <div class="activity-list">
    <H5NavBar title="活动管理" />

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="search-box">
        <input v-model="keyword" placeholder="搜索活动名称..." @keyup.enter="fetchData" />
      </div>
      <div class="filter-row">
        <t-select
          v-model="statusFilter"
          placeholder="发布状态"
          clearable
          size="small"
          @change="fetchData"
        >
          <t-option label="草稿" value="0" />
          <t-option label="已发布" value="1" />
          <t-option label="已结束" value="2" />
        </t-select>
        <t-select
          v-model="typeFilter"
          placeholder="活动类型"
          clearable
          size="small"
          @change="fetchData"
        >
          <t-option label="线上" value="0" />
          <t-option label="线下" value="1" />
        </t-select>
      </div>
    </div>

    <!-- 创建活动按钮 -->
    <div class="create-btn-wrap">
      <t-button theme="primary" size="large" block @click="goCreate">
        <template #icon><t-icon name="add" /></template>
        创建新活动
      </t-button>
    </div>

    <!-- 活动列表 -->
    <div class="activity-list-content">
      <div v-for="item in activities" :key="item.id" class="activity-card">
        <div class="card-header">
          <span class="card-title">{{ item.name }}</span>
          <t-tag :theme="getStatusTheme(item.status)" variant="light" size="small">
            {{ getStatusLabel(item.status) }}
          </t-tag>
        </div>
        <div class="card-info">
          <span class="info-item">
            <t-icon name="user" size="14" />
            {{ item.registrationCount || 0 }} 人报名
          </span>
          <span class="info-item">
            <t-icon name="time" size="14" />
            {{ formatDate(item.activityStartTime) }}
          </span>
        </div>
        <div class="card-type">
          <t-tag :theme="item.type === 0 ? 'primary' : 'warning'" variant="light" size="small">
            {{ item.type === 0 ? '线上' : '线下' }}
          </t-tag>
        </div>
        
        <!-- 操作按钮 -->
        <div class="card-actions">
          <t-button
            v-if="item.status === 0"
            theme="success"
            size="small"
            variant="outline"
            @click.stop="handlePublish(item)"
          >
            发布
          </t-button>
          <t-button
            v-if="item.status !== 2"
            theme="primary"
            size="small"
            variant="outline"
            @click.stop="goEdit(item)"
          >
            编辑
          </t-button>
          <t-button
            theme="default"
            size="small"
            variant="outline"
            @click.stop="goDetail(item)"
          >
            详情
          </t-button>
          <t-button
            theme="warning"
            size="small"
            variant="outline"
            @click.stop="showGroups(item)"
          >
            分组
          </t-button>
          <t-popconfirm
            content="确定要删除此活动吗？"
            @confirm="handleDelete(item.id)"
          >
            <t-button theme="danger" size="small" variant="outline" @click.stop>
              删除
            </t-button>
          </t-popconfirm>
        </div>
      </div>

      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-if="!loading && activities.length === 0" class="empty-text">暂无活动</div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
        加载更多
      </div>
    </div>

    <!-- 分组弹窗 -->
    <t-dialog
      v-model:visible="groupVisible"
      :header="`${currentActivity?.name || '活动'} - 分组与排名`"
      width="95%"
      :footer="false"
      placement="bottom"
    >
      <t-loading v-if="groupLoading" text="加载中..." />
      <div v-else-if="activityGroups.length" class="group-list">
        <div v-for="group in activityGroups" :key="group.targetSchool" class="group-card">
          <div class="group-header">
            <strong>{{ group.groupName }}</strong>
            <t-tag theme="primary" variant="light" size="small">{{ group.memberCount }} 人</t-tag>
          </div>
          <div class="group-members">
            <div v-for="(member, idx) in group.members" :key="member.registrationId" class="member-row">
              <span class="member-rank">#{{ idx + 1 }}</span>
              <span class="member-name">{{ member.realName }}</span>
              <span class="member-role">{{ member.role === 'STUDENT' ? '学生' : '教师' }}</span>
              <span class="member-score">{{ member.score ?? '-' }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-group">暂无已通过报名记录，或活动未开启自动分组</div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registeration'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const activities = ref([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const current = ref(1)
const hasMore = ref(true)

const groupVisible = ref(false)
const groupLoading = ref(false)
const currentActivity = ref(null)
const activityGroups = ref([])

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
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      type: typeFilter.value || undefined
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
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      type: typeFilter.value || undefined
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

const goCreate = () => {
  router.push('/h5/school/activity-create')
}

const goEdit = (item) => {
  router.push(`/h5/school/activity-edit/${item.id}`)
}

const goDetail = (item) => {
  router.push(`/h5/school-activity/${item.id}`)
}

const handlePublish = async (item) => {
  try {
    await activityApi.publishActivity(item.id)
    MessagePlugin.success('活动已发布')
    fetchData()
  } catch (err) {
    console.error('发布失败', err)
    MessagePlugin.error(err.response?.data?.message || '发布失败')
  }
}

const handleDelete = async (id) => {
  try {
    await activityApi.deleteActivity(id)
    MessagePlugin.success('删除成功')
    fetchData()
  } catch (err) {
    console.error('删除失败', err)
    MessagePlugin.error(err.response?.data?.message || '删除失败')
  }
}

const showGroups = async (item) => {
  currentActivity.value = item
  groupVisible.value = true
  groupLoading.value = true
  try {
    const res = await registrationApi.getActivityGroups(item.id)
    activityGroups.value = res.data || []
  } catch (err) {
    console.error('获取活动分组失败', err)
    activityGroups.value = []
  } finally {
    groupLoading.value = false
  }
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

.filter-section {
  padding: 12px;
  background: #fff;
}

.search-box {
  margin-bottom: 8px;
}

.search-box input {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.search-box input:focus {
  border-color: #2563eb;
}

.filter-row {
  display: flex;
  gap: 8px;
}

.filter-row .t-select {
  flex: 1;
}

.create-btn-wrap {
  padding: 12px;
}

.activity-list-content {
  padding: 0 12px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
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
  gap: 12px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-type {
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

.group-list {
  max-height: 60vh;
  overflow-y: auto;
}

.group-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.group-members {
  font-size: 13px;
}

.member-row {
  display: flex;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid #f3f4f6;
}

.member-row:last-child {
  border-bottom: none;
}

.member-rank {
  width: 30px;
  color: #6b7280;
}

.member-name {
  flex: 1;
  font-weight: 500;
}

.member-role {
  width: 50px;
  color: #6b7280;
}

.member-score {
  width: 60px;
  text-align: right;
  color: #6b7280;
}

.empty-group {
  text-align: center;
  color: #9ca3af;
  padding: 20px;
  font-size: 14px;
}
</style>