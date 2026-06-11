<template>
  <div class="audit-pending">
    <div class="page-header">
      <h2 class="page-title">待审核报名</h2>
      <div class="header-actions">
        <t-button
          v-if="selectedIds.length > 0"
          theme="success"
          size="small"
          @click="handleBatchApprove"
        >
          批量通过
        </t-button>
        <t-button
          v-if="selectedIds.length > 0"
          theme="danger"
          size="small"
          variant="outline"
          @click="handleBatchReject"
        >
          批量拒绝
        </t-button>
      </div>
    </div>

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
        v-model="collegeFilter"
        placeholder="筛选学院"
        clearable
        class="filter-select"
        @change="fetchData"
      >
        <t-option v-for="c in collegeOptions" :key="c.value" :value="c.value" :label="c.label" />
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
      @select-change="handleSelectChange"
    >
      <template #userType="{ row }">
        <t-tag
          :theme="row.userType === 'STUDENT' ? 'primary' : 'warning'"
          variant="light"
          size="small"
        >
          {{ row.userType === 'STUDENT' ? '学生' : '教师' }}
        </t-tag>
      </template>
      <template #collegeName="{ row }">
        <span>{{ row.collegeName || '-' }}</span>
      </template>
      <template #action="{ row }">
        <t-space>
          <t-link theme="primary" hover="color" size="small" @click="showDetail(row)">
            详情
          </t-link>
          <t-link theme="success" hover="color" size="small" @click="handleApprove(row.id)">
            通过
          </t-link>
          <t-link theme="danger" hover="color" size="small" @click="handleReject(row.id)">
            拒绝
          </t-link>
        </t-space>
      </template>
    </t-table>

    <t-dialog
      v-model:visible="detailVisible"
      header="报名详情"
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
          <t-descriptions-item label="所属学院">{{ currentRecord.collegeName || '-' }}</t-descriptions-item>
          <t-descriptions-item label="活动名称">{{ currentRecord.activityTitle || '-' }}</t-descriptions-item>
          <t-descriptions-item label="目标学校">{{ currentRecord.targetSchool || '-' }}</t-descriptions-item>
          <t-descriptions-item label="成绩/绩点">{{ currentRecord.score || '-' }}</t-descriptions-item>
          <t-descriptions-item label="报名时间">{{ formatDateTime(currentRecord.createTime) }}</t-descriptions-item>
        </t-descriptions>

        <div class="detail-actions">
          <t-button theme="success" @click="doApprove">通过</t-button>
          <t-button theme="danger" variant="outline" @click="showRejectDialog = true">拒绝</t-button>
        </div>
      </div>
    </t-dialog>

    <t-dialog
      v-model:visible="showRejectDialog"
      header="拒绝报名"
      width="400px"
    >
      <t-textarea
        v-model="rejectReason"
        placeholder="请输入拒绝原因"
        :rows="3"
        class="reject-reason"
      />
      <template #footer>
        <t-button theme="primary" @click="doReject">确认拒绝</t-button>
        <t-button variant="outline" @click="showRejectDialog = false">取消</t-button>
      </template>
    </t-dialog>

    <t-dialog
      v-model:visible="batchRejectDialog"
      header="批量拒绝"
      width="400px"
    >
      <t-textarea
        v-model="batchRejectReason"
        placeholder="请输入拒绝原因（将应用到所有选中记录）"
        :rows="3"
        class="reject-reason"
      />
      <template #footer>
        <t-button theme="danger" @click="confirmBatchReject">确认批量拒绝</t-button>
        <t-button variant="outline" @click="batchRejectDialog = false">取消</t-button>
      </template>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { registrationApi } from '@/api/registeration'
import { auditApi } from '@/api/audit'
import { activityApi } from '@/api/activity'
import { userApi } from '@/api/user'
import { MessagePlugin } from 'tdesign-vue-next'
import { SearchIcon } from 'tdesign-icons-vue-next'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const activityFilter = ref('')
const collegeFilter = ref('')
const activityOptions = ref([])
const collegeOptions = ref([])
const selectedIds = ref([])
const detailVisible = ref(false)
const currentRecord = ref(null)
const showRejectDialog = ref(false)
const rejectReason = ref('')
const batchRejectDialog = ref(false)
const batchRejectReason = ref('')

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { type: 'selection', width: 60 },
  { colKey: 'realName', title: '报名人', width: 100 },
  { colKey: 'userType', title: '类型', width: 80 },
  { colKey: 'collegeName', title: '所属学院', width: 140 },
  { colKey: 'activityTitle', title: '活动', ellipsis: true },
  { colKey: 'targetSchool', title: '目标学校', ellipsis: true, width: 160 },
  { colKey: 'score', title: '成绩/绩点', width: 100 },
  { colKey: 'createTime', title: '报名时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 180 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await registrationApi.getPendingAudit({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      node: 'school_audit',
      keyword: keyword.value || undefined,
      activityId: activityFilter.value || undefined,
      collegeId: collegeFilter.value || undefined
    })
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
    selectedIds.value = []
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
      label: a.name
    }))
  } catch (err) {
    console.error(err)
  }
}

const fetchColleges = async () => {
  try {
    const res = await userApi.getCollegeList()
    collegeOptions.value = (res.data || []).map(c => ({
      value: c.id,
      label: c.realName
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

const handleSelectChange = (selectedRowKeys) => {
  selectedIds.value = selectedRowKeys
}

const showDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

const handleApprove = async (id) => {
  try {
    await auditApi.audit({ registrationId: id, passed: true, comment: '' })
    MessagePlugin.success('已通过')
    fetchData()
  } catch (err) {
    console.error('审核失败', err)
    MessagePlugin.error(err.response?.data?.message || '审核失败')
  }
}

const handleReject = async (id) => {
  currentRecord.value = { id }
  showRejectDialog.value = true
}

const doApprove = async () => {
  if (!currentRecord.value) return
  try {
    await auditApi.audit({ registrationId: currentRecord.value.id, passed: true, comment: '' })
    MessagePlugin.success('已通过')
    detailVisible.value = false
    fetchData()
  } catch (err) {
    console.error('审核失败', err)
    MessagePlugin.error(err.response?.data?.message || '审核失败')
  }
}

const doReject = async () => {
  if (!currentRecord.value) return
  try {
    await auditApi.audit({ registrationId: currentRecord.value.id, passed: false, comment: rejectReason.value })
    MessagePlugin.success('已拒绝')
    showRejectDialog.value = false
    detailVisible.value = false
    rejectReason.value = ''
    fetchData()
  } catch (err) {
    console.error('审核失败', err)
    MessagePlugin.error(err.response?.data?.message || '审核失败')
  }
}

const handleBatchApprove = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await auditApi.batchAudit({ registrationIds: selectedIds.value, passed: true, comment: '' })
    MessagePlugin.success(`已批量通过 ${selectedIds.value.length} 条记录`)
    fetchData()
  } catch (err) {
    console.error('批量审核失败', err)
    MessagePlugin.error(err.response?.data?.message || '批量审核失败')
  }
}

const handleBatchReject = () => {
  if (selectedIds.value.length === 0) return
  batchRejectDialog.value = true
}

const confirmBatchReject = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await auditApi.batchAudit({ registrationIds: selectedIds.value, passed: false, comment: batchRejectReason.value })
    MessagePlugin.success(`已批量拒绝 ${selectedIds.value.length} 条记录`)
    batchRejectDialog.value = false
    batchRejectReason.value = ''
    fetchData()
  } catch (err) {
    console.error('批量审核失败', err)
    MessagePlugin.error(err.response?.data?.message || '批量审核失败')
  }
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
  fetchActivities()
  fetchColleges()
})
</script>

<style scoped>
.audit-pending { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.header-actions { display: flex; gap: 8px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-input { flex: 1; }
.filter-select { width: 180px; flex-shrink: 0; }
.detail-content { padding-bottom: 20px; }
.detail-actions { display: flex; gap: 12px; margin-top: 20px; justify-content: flex-end; }
.reject-reason { width: 100%; }
</style>
