<template>
  <div class="audit-history">
    <div class="page-header">
      <h2 class="page-title">审核历史</h2>
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
      <t-select
        v-model="resultFilter"
        placeholder="审核结果"
        clearable
        class="filter-select-short"
        @change="fetchData"
      >
        <t-option value="APPROVED" label="已通过" />
        <t-option value="REJECTED" label="已拒绝" />
      </t-select>
    </div>

    <t-table
      :data="records"
      :columns="columns"
      :loading="loading"
      row-key="id"
      hover
      stripe
      :pagination="pagination"
      @page-change="handlePageChange"
    >
      <template #result="{ row }">
        <t-tag
          :theme="row.result === 'APPROVED' ? 'success' : 'danger'"
          variant="light"
          size="small"
        >
          {{ row.result === 'APPROVED' ? '已通过' : '已拒绝' }}
        </t-tag>
      </template>
      <template #userType="{ row }">
        <t-tag
          :theme="row.userType === 'STUDENT' ? 'primary' : 'warning'"
          variant="light"
          size="small"
        >
          {{ row.userType === 'STUDENT' ? '学生' : '教师' }}
        </t-tag>
      </template>
      <template #action="{ row }">
        <t-link theme="primary" hover="color" size="small" @click="showDetail(row)">
          查看详情
        </t-link>
      </template>
    </t-table>

    <!-- Detail dialog -->
    <t-dialog
      v-model:visible="dialogVisible"
      header="审核详情"
      width="560px"
      :footer="false"
    >
      <div v-if="currentRecord" class="detail-content">
        <t-descriptions :column="1" bordered>
          <t-descriptions-item label="报名人">{{ currentRecord.realName || '-' }}</t-descriptions-item>
          <t-descriptions-item label="类型">
            <t-tag
              :theme="currentRecord.userType === 'STUDENT' ? 'primary' : 'warning'"
              variant="light"
              size="small"
            >
              {{ currentRecord.userType === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="活动名称">{{ currentRecord.activityTitle || '-' }}</t-descriptions-item>
          <t-descriptions-item label="目标学校">{{ currentRecord.targetSchool || '-' }}</t-descriptions-item>
          <t-descriptions-item label="审核结果">
            <t-tag
              :theme="currentRecord.result === 'APPROVED' ? 'success' : 'danger'"
              variant="light"
              size="small"
            >
              {{ currentRecord.result === 'APPROVED' ? '已通过' : '已拒绝' }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="审核意见">{{ currentRecord.comment || '-' }}</t-descriptions-item>
          <t-descriptions-item label="审核时间">{{ formatDateTime(currentRecord.createTime) }}</t-descriptions-item>
        </t-descriptions>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { auditApi } from '@/api/audit'
import { activityApi } from '@/api/activity'
import { SearchIcon } from 'tdesign-icons-vue-next'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const activityFilter = ref('')
const resultFilter = ref('')
const activityOptions = ref([])
const dialogVisible = ref(false)
const currentRecord = ref(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { colKey: 'realName', title: '报名人', width: 100 },
  { colKey: 'userType', title: '类型', width: 80 },
  { colKey: 'activityTitle', title: '活动', ellipsis: true },
  { colKey: 'targetSchool', title: '目标学校', ellipsis: true, width: 160 },
  { colKey: 'result', title: '审核结果', width: 100 },
  { colKey: 'createTime', title: '审核时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 100 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await auditApi.getHistory({
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

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchData()
}

const showDetail = (row) => {
  currentRecord.value = row
  dialogVisible.value = true
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
.audit-history { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-input { flex: 1; }
.filter-select { width: 200px; flex-shrink: 0; }
.filter-select-short { width: 140px; flex-shrink: 0; }
.detail-content { max-height: 50vh; overflow-y: auto; }
@media (max-width: 640px) {
  .filter-bar { flex-direction: column; }
  .filter-select, .filter-select-short { width: 100%; }
}
</style>
