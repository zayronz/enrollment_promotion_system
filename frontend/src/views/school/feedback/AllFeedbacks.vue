<template>
  <div class="all-feedbacks">
    <div class="page-header">
      <h2 class="page-title">全部反馈</h2>
      <t-button theme="primary" @click="showSubmitDialog()">
        <template #icon><EditIcon /></template>
        提交工作总结
      </t-button>
    </div>

    <div class="filter-bar">
      <t-input
        v-model="keyword"
        placeholder="搜索反馈标题、内容或提交人..."
        clearable
        class="filter-input"
        @blur="fetchData"
        @clear="fetchData"
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
        v-model="typeFilter"
        placeholder="人员类型"
        clearable
        class="filter-select"
        @change="fetchData"
      >
        <t-option label="学生" value="STUDENT" />
        <t-option label="教师" value="TEACHER" />
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
      <template #userType="{ row }">
        <t-tag :theme="row.userRole === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
          {{ row.userRole === 'STUDENT' ? '学生' : '教师' }}
        </t-tag>
      </template>
      <template #contentSummary="{ row }">
        {{ (row.content || '').substring(0, 100) }}{{ (row.content || '').length > 100 ? '...' : '' }}
      </template>
      <template #action="{ row }">
        <t-space size="small">
          <t-link theme="primary" hover="color" size="small" @click="showDetail(row)">
            查看详情
          </t-link>
          <t-link theme="default" hover="color" size="small" @click="downloadAttachments(row)">
            下载附件
          </t-link>
        </t-space>
      </template>
    </t-table>

    <t-dialog
      v-model:visible="submitVisible"
      header="提交工作总结"
      width="720px"
      :confirm-btn="{ content: '提交', theme: 'primary' }"
      @confirm="handleSubmit"
    >
      <t-form ref="submitFormRef" :data="submitForm" :rules="submitRules" label-width="100px">
        <t-form-item label="所属活动" name="activityId">
          <t-select v-model="submitForm.activityId" placeholder="请选择活动" filterable>
            <t-option
              v-for="opt in activityOptions"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
            />
          </t-select>
        </t-form-item>
        <t-form-item label="总结标题" name="title">
          <t-input v-model="submitForm.title" placeholder="请输入总结标题" clearable />
        </t-form-item>
        <t-form-item label="总结内容" name="content">
          <RichTextEditor v-model="submitForm.content" />
        </t-form-item>
        <t-form-item label="附件上传">
          <FileUploader v-model="submitFileList" :multiple="true" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- Detail dialog -->
    <t-dialog
      v-model:visible="detailVisible"
      header="反馈详情"
      width="680px"
      :footer="false"
    >
      <div v-if="currentRecord" class="detail-content">
        <t-descriptions :column="2" bordered>
          <t-descriptions-item label="提交人">{{ currentRecord.realName || '-' }}</t-descriptions-item>
          <t-descriptions-item label="类型">
            <t-tag :theme="currentRecord.userRole === 'STUDENT' ? 'primary' : 'warning'" variant="light" size="small">
              {{ currentRecord.userRole === 'STUDENT' ? '学生' : '教师' }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="活动名称" :span="2">
            {{ currentRecord.activityTitle || '-' }}
          </t-descriptions-item>
          <t-descriptions-item label="反馈标题" :span="2">
            {{ currentRecord.title || '-' }}
          </t-descriptions-item>
          <t-descriptions-item label="提交时间">
            {{ formatDateTime(currentRecord.createTime) }}
          </t-descriptions-item>
        </t-descriptions>
        <div class="feedback-body">
          <h4 class="detail-subtitle">反馈内容</h4>
          <div class="content-full" v-html="currentRecord.content || '暂无内容'" />
        </div>
        <div v-if="getAttachments(currentRecord).length > 0" class="attachment-section">
          <h4 class="detail-subtitle">附件列表</h4>
          <t-link
            v-for="(url, index) in getAttachments(currentRecord)"
            :key="index"
            theme="primary"
            hover="color"
            class="attachment-link"
            @click="window.open(url)"
          >
            <template #prefix-icon><FileIcon /></template>
            {{ url.split('/').pop() || '附件' + (index + 1) }}
          </t-link>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { feedbackApi } from '@/api/feedback'
import { activityApi } from '@/api/activity'
import { getFileUrl } from '@/utils/file'
import { getToken } from '@/utils/auth'
import { SearchIcon, EditIcon, FileIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'
import RichTextEditor from '@/components/business/RichTextEditor.vue'
import FileUploader from '@/components/business/FileUploader.vue'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const activityFilter = ref('')
const typeFilter = ref('')
const activityOptions = ref([])

const submitVisible = ref(false)
const detailVisible = ref(false)
const currentRecord = ref(null)
const submitFormRef = ref(null)
const submitting = ref(false)
const submitFileList = ref([])

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

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
  title: [{ required: true, message: '请输入总结标题' }],
  content: [{ required: true, message: '请输入总结内容' }]
}

const columns = [
  { colKey: 'realName', title: '提交人', width: 100 },
  { colKey: 'userRole', title: '类型', width: 80 },
  { colKey: 'activityTitle', title: '所属活动', width: 180, ellipsis: true },
  { colKey: 'title', title: '标题', minWidth: 180, ellipsis: true },
  { colKey: 'contentSummary', title: '内容摘要', minWidth: 180, ellipsis: true },
  { colKey: 'createTime', title: '提交时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 160 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = activityFilter.value
      ? await feedbackApi.getActivityFeedbacks(activityFilter.value)
      : await feedbackApi.getAllFeedbacks()
    records.value = res.data || []
    pagination.value.total = records.value.length
    if (keyword.value) {
      records.value = records.value.filter(r => 
        r.title?.toLowerCase().includes(keyword.value.toLowerCase()) ||
        r.content?.toLowerCase().includes(keyword.value.toLowerCase()) ||
        r.realName?.toLowerCase().includes(keyword.value.toLowerCase())
      )
    }
    if (typeFilter.value) {
      records.value = records.value.filter(r => r.userRole === typeFilter.value)
    }
    pagination.value.total = records.value.length
  } catch (err) {
    console.error('获取反馈列表失败', err)
  } finally {
    loading.value = false
  }
}

const fetchActivities = async () => {
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 100 })
    activityOptions.value = (res.data?.records || []).map(a => ({
      value: a.id,
      label: a.name || a.title
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

const showSubmitDialog = () => {
  submitForm.value = { activityId: '', title: '', content: '' }
  submitFileList.value = []
  submitVisible.value = true
}

const handleSubmit = async () => {
  const valid = await submitFormRef.value.validate()
  if (valid !== true) return

  submitting.value = true
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
      attachmentUrls,
      type: 1
    })
    MessagePlugin.success('提交成功')
    submitVisible.value = false
    fetchData()
  } catch (err) {
    console.error('提交反馈失败', err)
    MessagePlugin.error(err.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const showDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

const getAttachments = (row) => {
  if (!row.attachmentUrls) return []
  return row.attachmentUrls.split(',').filter(url => url.trim()).map(url => getFileUrl(url.trim()))
}

const downloadAttachments = (row) => {
  const attachments = getAttachments(row)
  if (attachments.length > 0) {
    attachments.forEach(url => {
      window.open(url)
    })
  } else {
    MessagePlugin.info('暂无附件可下载')
  }
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
.all-feedbacks { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-input { flex: 1; }
.filter-select { width: 180px; flex-shrink: 0; }
.detail-content { max-height: 60vh; overflow-y: auto; }
.feedback-body { margin-top: 16px; }
.attachment-section { margin-top: 16px; }
.detail-subtitle { font-size: 14px; font-weight: 600; color: var(--td-text-color-primary); margin-bottom: 12px; }
.content-full { font-size: 14px; line-height: 1.8; color: var(--td-text-color-secondary); }
.attachment-link { display: block; margin-bottom: 6px; font-size: 14px; }

@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .filter-bar { flex-direction: column; }
  .filter-select { width: 100%; }
}
</style>
