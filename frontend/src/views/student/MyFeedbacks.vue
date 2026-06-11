<template>
  <div class="my-feedbacks">
    <div class="page-header">
      <h2 class="page-title">我的反馈</h2>
    </div>

    <div class="filter-bar">
      <t-select
        v-model="activityFilter"
        placeholder="按活动筛选"
        clearable
        class="filter-select"
        @change="handleSearch"
      >
        <t-option
          v-for="opt in activityOptions"
          :key="opt.value"
          :value="opt.value"
          :label="opt.label"
        />
      </t-select>
      <t-button theme="primary" @click="showSubmitDialog()">
        <template #icon><AddIcon /></template>
        提交反馈
      </t-button>
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
      <template #content="{ row }">
        <div class="content-cell" v-html="row.content?.substring(0, 80) + (row.content?.length > 80 ? '...' : '')" />
      </template>
      <template #action="{ row }">
        <t-space>
          <t-link theme="primary" hover="color" size="small" @click="showDetail(row)">
            查看详情
          </t-link>
        </t-space>
      </template>
    </t-table>

    <t-dialog
      v-model:visible="submitVisible"
      header="提交反馈"
      width="720px"
      :confirm-btn="{ content: '提交', theme: 'primary' }"
      @confirm="handleSubmitFeedback"
    >
      <t-form ref="submitFormRef" :data="submitForm" :rules="submitRules" label-width="100px">
        <t-form-item label="所属活动" name="activityId">
          <t-select
            v-model="submitForm.activityId"
            placeholder="请选择活动"
            filterable
          >
            <t-option
              v-for="opt in approvedActivities"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
              :disabled="opt.expired"
            />
          </t-select>
          <div class="deadline-tip" v-if="selectedActivityDeadline">
            反馈截止：{{ formatDateTime(selectedActivityDeadline) }}
          </div>
        </t-form-item>
        <t-form-item label="反馈标题" name="title">
          <t-input v-model="submitForm.title" placeholder="请输入反馈标题" clearable />
        </t-form-item>
        <t-form-item label="反馈内容" name="content">
          <RichTextEditor v-model="submitForm.content" />
        </t-form-item>
        <t-form-item label="附件上传">
          <FileUploader v-model="submitFileList" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="detailVisible"
      header="反馈详情"
      width="680px"
      :footer="false"
    >
      <div v-if="currentFeedback" class="detail-content">
        <t-descriptions :column="2" bordered>
          <t-descriptions-item label="活动名称">
            {{ currentFeedback.activityTitle || '-' }}
          </t-descriptions-item>
          <t-descriptions-item label="提交时间">
            {{ formatDateTime(currentFeedback.createTime) }}
          </t-descriptions-item>
          <t-descriptions-item label="反馈标题" :span="2">
            {{ currentFeedback.title || '-' }}
          </t-descriptions-item>
        </t-descriptions>
        <div class="feedback-body">
          <h4 class="detail-subtitle">反馈内容</h4>
          <div class="content-full" v-html="currentFeedback.content || '暂无内容'" />
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { feedbackApi } from '@/api/feedback'
import { registrationApi } from '@/api/registeration'
import { AddIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'
import RichTextEditor from '@/components/business/RichTextEditor.vue'
import FileUploader from '@/components/business/FileUploader.vue'

const records = ref([])
const loading = ref(false)
const activityFilter = ref('')
const activityOptions = ref([])
const approvedActivities = ref([])

const submitVisible = ref(false)
const detailVisible = ref(false)
const currentFeedback = ref(null)
const submitFormRef = ref(null)
const submitFileList = ref([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const submitForm = ref({
  activityId: '',
  title: '',
  content: ''
})

const submitRules = {
  activityId: [{ required: true, message: '请选择活动' }],
  title: [{ required: true, message: '请输入反馈标题' }],
  content: [{ required: true, message: '请输入反馈内容' }]
}

const columns = [
  { colKey: 'rowIndex', title: '编号', width: 80, cell: (_, { rowIndex }) => rowIndex + 1 },
  { colKey: 'activityTitle', title: '所属活动', ellipsis: true, width: 180 },
  { colKey: 'title', title: '标题', ellipsis: true },
  { colKey: 'content', title: '内容摘要', width: 200 },
  { colKey: 'createTime', title: '提交时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 100 }
]

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getMyFeedbacks(activityFilter.value || undefined)
    records.value = res.data || []
    pagination.value.total = (res.data || []).length
  } catch (err) {
    console.error('获取反馈列表失败', err)
  } finally {
    loading.value = false
  }
}

const fetchActivityOptions = async () => {
  try {
    const regRes = await registrationApi.getMyRegistrations({ page: 1, size: 1000 })
    const approvedRegs = (regRes.data?.records || []).filter(
      r => r.status === 1 || r.status === 2
    )

    // 只显示已通过审核的报名对应的活动，超过反馈截止时间的活动禁用提交
    activityOptions.value = approvedRegs.map(r => ({
      value: r.activityId,
      label: r.activityTitle,
      feedbackDeadline: r.feedbackDeadline,
      expired: isFeedbackExpired(r.feedbackDeadline)
    }))
    approvedActivities.value = activityOptions.value
  } catch (err) {
    console.error('获取活动选项失败', err)
  }
}

const handleSearch = () => {
  pagination.value.current = 1
  fetchFeedbacks()
}

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchFeedbacks()
}

const showSubmitDialog = () => {
  submitForm.value = { activityId: '', title: '', content: '' }
  submitFileList.value = []
  submitVisible.value = true
}

const selectedActivityDeadline = computed(() => {
  const item = approvedActivities.value.find(opt => opt.value === submitForm.value.activityId)
  return item?.feedbackDeadline || ''
})

const isFeedbackExpired = (deadline) => {
  return deadline ? new Date(deadline).getTime() < Date.now() : false
}

const handleSubmitFeedback = async () => {
  const valid = await submitFormRef.value.validate()
  if (valid !== true) return

  const selected = approvedActivities.value.find(opt => opt.value === submitForm.value.activityId)
  if (selected?.expired) {
    MessagePlugin.warning('已过反馈时间，无法提交反馈')
    return
  }

  try {
    let attachmentUrls = ''
    if (submitFileList.value) {
      if (Array.isArray(submitFileList.value)) {
        attachmentUrls = submitFileList.value.join(',')
      } else if (typeof submitFileList.value === 'string') {
        attachmentUrls = submitFileList.value
      }
    }
    
    await feedbackApi.submit({
      activityId: submitForm.value.activityId,
      title: submitForm.value.title,
      content: submitForm.value.content,
      attachmentUrls
    })
    MessagePlugin.success('反馈提交成功')
    submitVisible.value = false
    fetchFeedbacks()
  } catch (err) {
    console.error('提交反馈失败', err)
    MessagePlugin.error(err.response?.data?.message || '提交失败')
  }
}

const showDetail = (row) => {
  currentFeedback.value = row
  detailVisible.value = true
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchFeedbacks()
  fetchActivityOptions()
})
</script>

<style scoped>
.my-feedbacks { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 20px; align-items: center; }
.filter-select { width: 240px; flex-shrink: 0; }
.deadline-tip { margin-top: 6px; color: var(--td-text-color-placeholder); font-size: 12px; }
.content-cell { font-size: 13px; color: var(--td-text-color-secondary); line-height: 1.5; }
.detail-content { max-height: 60vh; overflow-y: auto; }
.feedback-body { margin-top: 16px; }
.detail-subtitle { font-size: 14px; font-weight: 600; color: var(--td-text-color-primary); margin-bottom: 12px; }
.content-full { font-size: 14px; line-height: 1.8; color: var(--td-text-color-secondary); }
@media (max-width: 640px) {
  .filter-bar { flex-direction: column; align-items: stretch; }
  .filter-select { width: 100%; }
}
</style>
