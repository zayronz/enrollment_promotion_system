<template>
  <div class="my-registrations">
    <div class="page-header">
      <h2 class="page-title">我的报名</h2>
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
      <template #status="{ row }">
        <t-tag :theme="getStatusTheme(row.status)" variant="light" size="small">
          {{ getStatusLabel(row.status) }}
        </t-tag>
      </template>
      <template #action="{ row }">
        <t-space>
          <t-link theme="primary" hover="color" size="small" @click="showDetail(row)">
            详情
          </t-link>
          <t-popconfirm
            v-if="row.status === 0"
            content="确定要撤销此报名吗？"
            @confirm="handleWithdraw(row.id)"
          >
            <t-link theme="danger" hover="color" size="small">撤销</t-link>
          </t-popconfirm>
        </t-space>
      </template>
    </t-table>

    <!-- Detail dialog -->
    <t-dialog
      v-model:visible="dialogVisible"
      header="报名详情"
      width="620px"
      :footer="false"
    >
      <div v-if="currentRecord" class="detail-content">
        <t-descriptions :column="2" bordered>
          <t-descriptions-item label="活动名称">
            {{ currentRecord.activityTitle }}
          </t-descriptions-item>
          <t-descriptions-item label="审核状态">
            <t-tag :theme="getStatusTheme(currentRecord.status)" variant="light" size="small">
              {{ getStatusLabel(currentRecord.status) }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="目标学校" :span="2">
            {{ currentRecord.targetSchool || '-' }}
          </t-descriptions-item>
          <t-descriptions-item label="报名时间">
            {{ formatDateTime(currentRecord.createTime) }}
          </t-descriptions-item>
          <t-descriptions-item label="分组">
            {{ currentRecord.groupName || '-' }}
          </t-descriptions-item>
          <t-descriptions-item label="排名" v-if="currentRecord.groupRank !== null">
            #{{ currentRecord.groupRank }}
          </t-descriptions-item>
        </t-descriptions>

        <!-- Custom fields -->
        <div v-if="customFields.length > 0" class="custom-fields">
          <h4 class="detail-subtitle">填报信息</h4>
          <t-descriptions :column="1" bordered>
            <t-descriptions-item
              v-for="field in customFields"
              :key="field.label"
              :label="field.label"
            >
              {{ field.value || '-' }}
            </t-descriptions-item>
          </t-descriptions>
        </div>

        <!-- Audit timeline -->
        <div v-if="auditLogs.length > 0" class="audit-timeline">
          <h4 class="detail-subtitle">审核记录</h4>
          <t-timeline>
            <t-timeline-item
              v-for="(log, index) in auditLogs"
              :key="index"
              :dot-color="log.result === 'APPROVED' ? 'success' : 'danger'"
            >
              <div class="timeline-label">{{ log.auditorName || '审核人' }}</div>
              <div class="timeline-desc">
                {{ log.result === 'APPROVED' ? '通过' : '拒绝' }}
                <span v-if="log.comment"> — {{ log.comment }}</span>
              </div>
              <div class="timeline-time">{{ formatDateTime(log.createTime) }}</div>
            </t-timeline-item>
          </t-timeline>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { registrationApi } from '@/api/registeration'
import { MessagePlugin } from 'tdesign-vue-next'

const records = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentRecord = ref(null)
const customFields = ref([])
const auditLogs = ref([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { colKey: 'id', title: '编号', width: 80 },
  { colKey: 'activityTitle', title: '活动名称', ellipsis: true },
  { colKey: 'targetSchool', title: '目标学校', ellipsis: true, width: 160 },
  { colKey: 'createTime', title: '报名时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'status', title: '审核状态', width: 100 },
  { colKey: 'action', title: '操作', width: 120 }
]

const fetchRegistrations = async () => {
  loading.value = true
  try {
    const res = await registrationApi.getMyRegistrations({
      page: pagination.value.current,
      size: pagination.value.pageSize
    })
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (err) {
    console.error('获取报名列表失败', err)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchRegistrations()
}

const showDetail = async (row) => {
  currentRecord.value = row
  try {
    const res = await registrationApi.getRegistrationDetail(row.id)
    const detail = res.data
    customFields.value = detail.customFields || []
    auditLogs.value = detail.auditLogs || []
  } catch (err) {
    console.error('获取详情失败', err)
    customFields.value = []
    auditLogs.value = []
  }
  dialogVisible.value = true
}

const handleWithdraw = async (id) => {
  try {
    await registrationApi.withdraw(id)
    MessagePlugin.success('已撤销报名')
    fetchRegistrations()
  } catch (err) {
    console.error('撤销失败', err)
  }
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

const getStatusTheme = (status) => {
  // status: 0=待审核, 1=学院通过, 2=全部通过, 3=已拒绝, 4=已撤回
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'default'
  }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = {
    0: '待审核',
    1: '学院通过',
    2: '已通过',
    3: '已拒绝',
    4: '已撤回'
  }
  return map[status] || '未知'
}

onMounted(fetchRegistrations)
</script>

<style scoped>
.my-registrations {
  padding: 0;
}
.page-header {
  margin-bottom: 20px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 0;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}
.detail-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 20px 0 12px;
}
.custom-fields {
  margin-top: 8px;
}
.audit-timeline {
  margin-top: 8px;
}
.timeline-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--td-text-color-primary);
}
.timeline-desc {
  font-size: 13px;
  color: var(--td-text-color-secondary);
  margin-top: 2px;
}
.timeline-time {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
  margin-top: 2px;
}
</style>
