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
          <t-descriptions-item label="审批进度" :span="2">
            {{ auditProgress || '-' }}
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

        <div v-if="activityDetail" class="custom-fields">
          <h4 class="detail-subtitle">活动详情</h4>
          <t-descriptions :column="1" bordered>
            <t-descriptions-item label="活动时间">
              {{ formatDateTime(activityDetail.activityStartTime) }} 至 {{ formatDateTime(activityDetail.activityEndTime) }}
            </t-descriptions-item>
            <t-descriptions-item label="报名时间">
              {{ formatDateTime(activityDetail.registrationStartTime) }} 至 {{ formatDateTime(activityDetail.registrationEndTime) }}
            </t-descriptions-item>
            <t-descriptions-item label="活动地点">
              {{ activityDetail.location || '-' }}
            </t-descriptions-item>
            <t-descriptions-item label="活动介绍">
              <span v-html="activityDetail.description || '-'"></span>
            </t-descriptions-item>
          </t-descriptions>
        </div>

        <!-- Custom fields -->
        <div v-if="customFields.length > 0" class="custom-fields">
          <h4 class="detail-subtitle">填报信息</h4>
          <t-descriptions :column="1" bordered>
            <t-descriptions-item
              v-for="field in customFields"
              :key="field.label || field.name"
              :label="field.label || field.name"
            >
              {{ field.value || '-' }}
            </t-descriptions-item>
          </t-descriptions>
        </div>

        <div v-if="attachments.length > 0" class="custom-fields">
          <h4 class="detail-subtitle">报名附件</h4>
          <t-space direction="vertical">
            <t-link
              v-for="(file, index) in attachments"
              :key="index"
              theme="primary"
              @click="openAttachment(file)"
            >
              {{ getAttachmentName(file) }}
            </t-link>
          </t-space>
        </div>

        <div v-if="teamMembers.length > 0" class="custom-fields">
          <h4 class="detail-subtitle">当前分组成员</h4>
          <t-table :data="teamMembers" :columns="teamColumns" row-key="registrationId" size="small" />
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

        <!-- Feedbacks -->
        <div v-if="feedbacks.length > 0" class="feedback-section">
          <h4 class="detail-subtitle">工作反馈</h4>
          <t-timeline>
            <t-timeline-item
              v-for="(fb, index) in feedbacks"
              :key="index"
              dot-color="primary"
            >
              <div class="timeline-label">{{ fb.title || '反馈' }}</div>
              <div class="timeline-desc">
                {{ fb.userName || '反馈人' }}：<span v-html="(fb.content || '').substring(0, 150) + ((fb.content || '').length > 150 ? '...' : '')" />
              </div>
              <div class="timeline-time">{{ formatDateTime(fb.createTime) }}</div>
            </t-timeline-item>
          </t-timeline>
        </div>

        <!-- Feedback submit button -->
        <div v-if="canSubmitFeedback" class="feedback-submit">
          <t-button theme="primary" @click="openFeedbackModal">提交反馈</t-button>
        </div>
      </div>
    </t-dialog>

    <!-- Feedback modal -->
    <t-dialog
      v-model:visible="feedbackModalVisible"
      header="提交工作反馈"
      width="500px"
      @confirm="submitFeedback"
      @cancel="feedbackModalVisible = false"
    >
      <t-form :model="feedbackForm" ref="feedbackFormRef">
        <t-form-item label="反馈标题" name="title" :rules="[{ required: true, message: '请输入反馈标题' }]">
          <t-input v-model="feedbackForm.title" placeholder="请输入反馈标题" />
        </t-form-item>
        <t-form-item label="反馈类型" name="type">
          <t-select v-model="feedbackForm.type" placeholder="请选择反馈类型">
            <t-option value="WORK" label="工作反馈" />
            <t-option value="SUGGESTION" label="建议" />
            <t-option value="COMPLAINT" label="投诉" />
          </t-select>
        </t-form-item>
        <t-form-item label="反馈内容" name="content" :rules="[{ required: true, message: '请输入反馈内容' }]">
          <t-textarea v-model="feedbackForm.content" placeholder="请输入反馈内容" :rows="4" />
        </t-form-item>
        <t-form-item label="附件上传">
          <FileUploader v-model="feedbackFileList" :multiple="true" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { registrationApi } from '@/api/registeration'
import { feedbackApi } from '@/api/feedback'
import { MessagePlugin } from 'tdesign-vue-next'
import { getFileUrl } from '@/utils/file'
import FileUploader from '@/components/business/FileUploader.vue'

const records = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentRecord = ref(null)
const customFields = ref([])
const attachments = ref([])
const auditLogs = ref([])
const feedbacks = ref([])
const activityDetail = ref(null)
const teamMembers = ref([])
const auditProgress = ref('')
const feedbackModalVisible = ref(false)
const feedbackFormRef = ref(null)
const feedbackFileList = ref([])
const feedbackForm = ref({
  title: '',
  type: 'WORK',
  content: ''
})

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

const teamColumns = [
  { colKey: 'realName', title: '姓名', width: 100 },
  { colKey: 'role', title: '角色', width: 100 },
  { colKey: 'groupRank', title: '组内排名', width: 100 },
  { colKey: 'phone', title: '联系电话', ellipsis: true }
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
    attachments.value = detail.attachments || []
    auditLogs.value = detail.auditLogs || []
    feedbacks.value = detail.feedbacks || []
    activityDetail.value = detail.activityDetail || null
    teamMembers.value = detail.teamMembers || []
    auditProgress.value = detail.auditProgress || ''
  } catch (err) {
    console.error('获取详情失败', err)
    customFields.value = []
    attachments.value = []
    auditLogs.value = []
    feedbacks.value = []
    activityDetail.value = null
    teamMembers.value = []
    auditProgress.value = ''
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

const canSubmitFeedback = computed(() => {
  return currentRecord.value && (currentRecord.value.status === 1 || currentRecord.value.status === 2)
})

const openFeedbackModal = () => {
  feedbackForm.value = {
    title: '',
    type: 'WORK',
    content: ''
  }
  feedbackFileList.value = []
  feedbackModalVisible.value = true
}

const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  
  const valid = await feedbackFormRef.value.validate()
  if (valid !== true) return
  
  try {
    let attachmentUrls = ''
    if (feedbackFileList.value) {
      if (Array.isArray(feedbackFileList.value)) {
        attachmentUrls = feedbackFileList.value.join(',')
      } else if (typeof feedbackFileList.value === 'string') {
        attachmentUrls = feedbackFileList.value
      }
    }
    
    await feedbackApi.submit({
      activityId: currentRecord.value.activityId,
      title: feedbackForm.value.title,
      type: feedbackForm.value.type,
      content: feedbackForm.value.content,
      attachmentUrls
    })
    MessagePlugin.success('反馈提交成功')
    feedbackModalVisible.value = false
    showDetail(currentRecord.value)
  } catch (err) {
    console.error('提交反馈失败', err)
    MessagePlugin.error(err.response?.data?.message || '提交反馈失败')
  }
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

const getAttachmentName = (file) => {
  const path = typeof file === 'string' ? file : (file?.filePath || file?.url || '')
  return path ? path.split('/').pop() : '附件文件'
}

const openAttachment = (file) => {
  const path = typeof file === 'string' ? file : (file?.filePath || file?.url)
  if (!path) {
    MessagePlugin.warning('附件地址不存在')
    return
  }
  window.open(getFileUrl(path), '_blank')
}

const getStatusTheme = (status) => {
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
.my-registrations { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.detail-content { max-height: 60vh; overflow-y: auto; }
.detail-subtitle { font-size: 14px; font-weight: 600; color: var(--td-text-color-primary); margin: 20px 0 12px; }
.custom-fields { margin-top: 8px; }
.audit-timeline { margin-top: 8px; }
.feedback-section { margin-top: 8px; }
.timeline-label { font-size: 14px; font-weight: 500; color: var(--td-text-color-primary); }
.timeline-desc { font-size: 13px; color: var(--td-text-color-secondary); margin-top: 2px; }
.timeline-time { font-size: 12px; color: var(--td-text-color-placeholder); margin-top: 2px; }
</style>
