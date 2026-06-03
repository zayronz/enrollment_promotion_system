<template>
  <div class="h5-pending-audit">
    <H5NavBar title="报名审核" />

    <!-- Search & filter -->
    <div class="filter-section">
      <div class="search-bar">
        <t-input
          v-model="keyword"
          placeholder="搜索报名人或学校..."
          clearable
          @change="handleSearch"
        >
          <template #prefix-icon>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
          </template>
        </t-input>
      </div>
      <div class="filter-row">
        <t-select
          v-model="activityFilter"
          placeholder="筛选活动"
          clearable
          :style="{ flex: 1 }"
          @change="handleFilterChange"
        >
          <t-option v-for="a in activityOptions" :key="a.value" :value="a.value" :label="a.label" />
        </t-select>
      </div>
    </div>

    <!-- Batch action bar -->
    <div v-if="selectedIds.length > 0" class="batch-bar">
      <span class="selected-count">已选择 {{ selectedIds.length }} 项</span>
      <div class="batch-actions">
        <t-button theme="success" size="small" variant="outline" @click="batchAudit('APPROVED')">
          批量通过
        </t-button>
        <t-button theme="danger" size="small" variant="outline" @click="batchAudit('REJECTED')">
          批量拒绝
        </t-button>
        <t-button size="small" variant="text" @click="clearSelection">取消</t-button>
      </div>
    </div>

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else-if="records.length > 0" class="record-list">
      <div
        v-for="item in records"
        :key="item.id"
        class="record-item"
        :class="{ selected: selectedIds.includes(item.id) }"
        @click="toggleSelect(item.id)"
      >
        <div class="item-checkbox">
          <div class="checkbox" :class="{ checked: selectedIds.includes(item.id) }">
            <svg v-if="selectedIds.includes(item.id)" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
        </div>
        <div class="item-content" @click.stop="showDetail(item)">
          <div class="item-header">
            <div class="item-title">{{ item.realName }}</div>
            <t-tag :theme="item.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
              {{ item.userType === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
          </div>
          <div class="item-info">
            <div class="info-row">
              <span class="info-label">活动：</span>
              <span class="info-value">{{ item.activityTitle || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">目标学校：</span>
              <span class="info-value">{{ item.targetSchool || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">报名时间：</span>
              <span class="info-value">{{ formatDateTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="item-actions" @click.stop>
            <t-button theme="primary" size="small" variant="outline" @click="showDetail(item)">详情</t-button>
            <t-button theme="success" size="small" @click="handleAudit(item.id, 'APPROVED')">通过</t-button>
            <t-button theme="danger" size="small" variant="outline" @click="handleAudit(item.id, 'REJECTED')">拒绝</t-button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-wrap">
      <div class="empty-text">暂无待审核报名</div>
    </div>

    <!-- Pagination -->
    <div v-if="records.length > 0" class="pagination-wrap">
      <t-pagination
        v-model="pagination.current"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-size-options="[10, 20, 50]"
        @change="handlePageChange"
      />
    </div>

    <!-- Detail popup -->
    <t-popup
      v-model:visible="detailVisible"
      placement="bottom"
      class="detail-popup"
      @visible-change="onPopupChange"
    >
      <div v-if="currentRecord" class="detail-content">
        <div class="detail-header">
          <div class="detail-title">报名详情</div>
          <t-icon name="close" size="20" class="close-icon" @click="detailVisible = false" />
        </div>

        <div class="detail-body">
          <div class="info-section">
            <div class="info-item">
              <span class="info-key">报名人</span>
              <span class="info-val">{{ currentRecord.realName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">类型</span>
              <t-tag :theme="currentRecord.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
                {{ currentRecord.userType === 'STUDENT' ? '学生' : '教师' }}
              </t-tag>
            </div>
            <div class="info-item">
              <span class="info-key">活动</span>
              <span class="info-val">{{ currentRecord.activityTitle || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">目标学校</span>
              <span class="info-val">{{ currentRecord.targetSchool || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">成绩/GPA</span>
              <span class="info-val">{{ currentRecord.score || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">报名时间</span>
              <span class="info-val">{{ formatDateTime(currentRecord.createTime) }}</span>
            </div>
          </div>

          <div class="audit-section">
            <div class="section-title">审核操作</div>
            <t-form class="audit-form">
              <t-form-item label="审核意见">
                <t-textarea v-model="auditComment" placeholder="可选：填写审核意见" :maxlength="500" />
              </t-form-item>
            </t-form>
            <div class="audit-buttons">
              <t-button theme="success" size="large" block @click="doAudit('APPROVED')">通过</t-button>
              <t-button theme="danger" size="large" variant="outline" block @click="doAudit('REJECTED')">拒绝</t-button>
            </div>
          </div>
        </div>
      </div>
    </t-popup>

    <!-- Confirm dialog -->
    <t-dialog
      v-model:visible="confirmVisible"
      :header="confirmTitle"
      :footer="false"
      width="300px"
      :close-on-overlay-click="true"
    >
      <div class="confirm-content">
        <p>{{ confirmMessage }}</p>
        <div class="confirm-actions">
          <t-button variant="outline" size="large" @click="confirmVisible = false">取消</t-button>
          <t-button :theme="confirmType === 'APPROVED' ? 'success' : 'danger'" size="large" @click="confirmAction">{{ confirmType === 'APPROVED' ? '通过' : '拒绝' }}</t-button>
        </div>
      </div>
    </t-dialog>
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
const selectedIds = ref([])
const detailVisible = ref(false)
const currentRecord = ref(null)
const auditComment = ref('')
const confirmVisible = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmType = ref('')
const pendingAuditId = ref(null)
const isBatch = ref(false)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await registrationApi.getPendingAudit({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      activityId: activityFilter.value || undefined
    })
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (err) {
    console.error('获取待审核列表失败', err)
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

const handleSearch = () => {
  pagination.value.current = 1
  fetchData()
}

const handleFilterChange = () => {
  pagination.value.current = 1
  fetchData()
}

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchData()
}

const toggleSelect = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const clearSelection = () => {
  selectedIds.value = []
}

const showDetail = (row) => {
  currentRecord.value = row
  auditComment.value = ''
  detailVisible.value = true
}

const onPopupChange = (visible) => {
  if (!visible) {
    currentRecord.value = null
    auditComment.value = ''
  }
}

const handleAudit = (id, result) => {
  pendingAuditId.value = id
  isBatch.value = false
  confirmType.value = result
  const label = result === 'APPROVED' ? '通过' : '拒绝'
  confirmTitle.value = '确认操作'
  confirmMessage.value = `确定要${label}此报名吗？`
  confirmVisible.value = true
}

const batchAudit = (result) => {
  isBatch.value = true
  confirmType.value = result
  const label = result === 'APPROVED' ? '通过' : '拒绝'
  confirmTitle.value = '批量操作'
  confirmMessage.value = `确定要批量${label}选中的 ${selectedIds.value.length} 条报名吗？`
  confirmVisible.value = true
}

const confirmAction = async () => {
  confirmVisible.value = false
  try {
    if (isBatch.value) {
      await auditApi.batchAudit({
        registrationIds: selectedIds.value,
        passed: confirmType.value === 'APPROVED',
        comment: ''
      })
      MessagePlugin.success(`已${confirmType.value === 'APPROVED' ? '通过' : '拒绝'} ${selectedIds.value.length} 条报名`)
      selectedIds.value = []
    } else {
      await auditApi.audit({
        registrationId: pendingAuditId.value,
        passed: confirmType.value === 'APPROVED',
        comment: auditComment.value
      })
      MessagePlugin.success('操作成功')
      detailVisible.value = false
    }
    fetchData()
  } catch (err) {
    console.error('审核失败', err)
  }
}

const doAudit = async (result) => {
  if (!currentRecord.value) return
  pendingAuditId.value = currentRecord.value.id
  isBatch.value = false
  confirmType.value = result
  await confirmAction()
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
.h5-pending-audit {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.empty-text {
  color: #9ca3af;
  font-size: 14px;
}

.filter-section {
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.search-bar {
  margin-bottom: 10px;
}

.filter-row {
  display: flex;
  gap: 10px;
}

.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #e6f4ff;
  border-bottom: 1px solid #91caff;
}

.selected-count {
  font-size: 13px;
  color: #1890ff;
}

.batch-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.record-list {
  padding: 12px;
}

.record-item {
  display: flex;
  align-items: flex-start;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.record-item.selected {
  border: 2px solid #1890ff;
  background: #f0f7ff;
}

.item-checkbox {
  padding-top: 4px;
  margin-right: 10px;
}

.checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #d9d9d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.checkbox.checked {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}

.info-label {
  color: #9ca3af;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-actions {
  display: flex;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.pagination-wrap {
  padding: 16px 12px;
  background: #fff;
  display: flex;
  justify-content: center;
}

.detail-popup {
  width: 100%;
  height: 85vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-icon {
  color: #9ca3af;
  cursor: pointer;
}

.detail-body {
  padding: 16px;
  max-height: calc(85vh - 60px);
  overflow-y: auto;
}

.info-section {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-key {
  font-size: 14px;
  color: #9ca3af;
}

.info-val {
  font-size: 14px;
  color: #1f2937;
  text-align: right;
  flex: 1;
  margin-left: 16px;
  word-break: break-word;
}

.audit-section {
  margin-top: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.audit-form {
  margin-bottom: 16px;
}

.audit-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.confirm-content {
  text-align: center;
}

.confirm-content p {
  margin: 0 0 20px;
  font-size: 14px;
  color: #1f2937;
}

.confirm-actions {
  display: flex;
  gap: 12px;
}

.confirm-actions .t-button {
  flex: 1;
}
</style>
