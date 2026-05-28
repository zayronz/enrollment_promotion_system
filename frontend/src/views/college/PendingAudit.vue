<template>
  <div class="pending-audit">
    <div class="page-header">
      <h2 class="page-title">报名审核</h2>
      <t-space>
        <t-button theme="success" variant="outline" :disabled="selectedIds.length === 0" @click="batchAudit('APPROVED')">
          <template #icon><CheckCircleIcon /></template>
          批量通过
        </t-button>
        <t-button theme="danger" variant="outline" :disabled="selectedIds.length === 0" @click="batchAudit('REJECTED')">
          <template #icon><CloseCircleIcon /></template>
          批量拒绝
        </t-button>
      </t-space>
    </div>

    <!-- Search & filter -->
    <div class="filter-bar">
      <t-input
        v-model="keyword"
        placeholder="搜索报名人或学校..."
        clearable
        class="filter-input"
        @change="fetchData"
      >
        <template #prefix-icon><SearchIcon /></template>
      </t-input>
      <t-select
        v-model="activityFilter"
        placeholder="筛选活动"
        clearable
        class="filter-select"
        @change="fetchData"
      >
        <t-option v-for="a in activityOptions" :key="a.value" :value="a.value" :label="a.label" />
      </t-select>
    </div>

    <t-table
      :data="records"
      :columns="columns"
      :loading="loading"
      :selected-row-keys="selectedIds"
      row-key="id"
      hover
      stripe
      :pagination="pagination"
      @page-change="handlePageChange"
      @select-change="handleSelectChange"
    >
      <template #userType="{ row }">
        <t-tag :theme="row.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
          {{ row.userType === 'STUDENT' ? '学生' : '教师' }}
        </t-tag>
      </template>
      <template #action="{ row }">
        <t-space size="small">
          <t-button theme="primary" variant="text" size="small" @click="showDetail(row)">
            详情
          </t-button>
          <t-button theme="success" variant="text" size="small" @click="handleAudit(row.id, 'APPROVED')">
            通过
          </t-button>
          <t-button theme="danger" variant="text" size="small" @click="handleAudit(row.id, 'REJECTED')">
            拒绝
          </t-button>
        </t-space>
      </template>
    </t-table>

    <!-- Detail dialog -->
    <t-dialog
      v-model:visible="dialogVisible"
      header="报名详情"
      width="560px"
      :confirm-btn="{ content: '通过', theme: 'success' }"
      :cancel-btn="{ content: '拒绝', theme: 'danger', variant: 'outline' }"
      @confirm="doAudit('APPROVED')"
      @cancel="doAudit('REJECTED')"
    >
      <div v-if="currentRecord" class="detail-content">
        <t-descriptions :column="1" bordered>
          <t-descriptions-item label="报名人">{{ currentRecord.realName }}</t-descriptions-item>
          <t-descriptions-item label="类型">
            <t-tag :theme="currentRecord.userType === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
              {{ currentRecord.userType === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="目标学校">{{ currentRecord.targetSchool || '-' }}</t-descriptions-item>
          <t-descriptions-item label="成绩/GPA">{{ currentRecord.score || '-' }}</t-descriptions-item>
          <t-descriptions-item label="报名时间">{{ formatDateTime(currentRecord.createTime) }}</t-descriptions-item>
        </t-descriptions>
        <t-form class="audit-form">
          <t-form-item label="审核意见">
            <t-textarea v-model="auditComment" placeholder="可选：填写审核意见" :maxlength="500" />
          </t-form-item>
        </t-form>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { registrationApi } from '@/api/registeration'
import { auditApi } from '@/api/audit'
import { activityApi } from '@/api/activity'
import { SearchIcon, CheckCircleIcon, CloseCircleIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const activityFilter = ref('')
const activityOptions = ref([])
const selectedIds = ref([])
const dialogVisible = ref(false)
const currentRecord = ref(null)
const auditComment = ref('')

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { colKey: 'row-select', type: 'multiple', width: 50 },
  { colKey: 'realName', title: '报名人', width: 100 },
  { colKey: 'userType', title: '类型', width: 80 },
  { colKey: 'activityTitle', title: '活动', ellipsis: true },
  { colKey: 'targetSchool', title: '目标学校', ellipsis: true, width: 160 },
  { colKey: 'createTime', title: '报名时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 160 }
]

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

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchData()
}

const handleSelectChange = (ids) => {
  selectedIds.value = ids
}

const showDetail = (row) => {
  currentRecord.value = row
  auditComment.value = ''
  dialogVisible.value = true
}

const handleAudit = (id, result) => {
  const label = result === 'APPROVED' ? '通过' : '拒绝'
  DialogPlugin.confirm({
    header: '确认操作',
    body: `确定要${label}此报名吗？`,
    confirmBtn: `确定${label}`,
    onConfirm: () => {
      doSingleAudit(id, result)
    }
  })
}

const doSingleAudit = async (id, result) => {
  try {
    await auditApi.audit({ registrationId: id, result, comment: '' })
    MessagePlugin.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    console.error('审核失败', err)
  }
}

const doAudit = async (result) => {
  if (!currentRecord.value) return
  await doSingleAudit(currentRecord.value.id, result)
}

const batchAudit = (result) => {
  const label = result === 'APPROVED' ? '通过' : '拒绝'
  DialogPlugin.confirm({
    header: '批量操作',
    body: `确定要批量${label}选中的 ${selectedIds.value.length} 条报名吗？`,
    confirmBtn: `确定${label}`,
    onConfirm: async () => {
      try {
        await auditApi.batchAudit({ ids: selectedIds.value, result })
        MessagePlugin.success(`已${label} ${selectedIds.value.length} 条报名`)
        selectedIds.value = []
        fetchData()
      } catch (err) {
        console.error('批量审核失败', err)
      }
    }
  })
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
.pending-audit {
  padding: 0;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 0;
}
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.filter-input {
  flex: 1;
}
.filter-select {
  width: 220px;
  flex-shrink: 0;
}
.detail-content {
  max-height: 50vh;
  overflow-y: auto;
}
.audit-form {
  margin-top: 16px;
}

@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .filter-bar {
    flex-direction: column;
  }
  .filter-select {
    width: 100%;
  }
}
</style>
