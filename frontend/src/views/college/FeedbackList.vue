<template>
  <div class="feedback-list">
    <div class="page-header">
      <h2 class="page-title">反馈管理</h2>
      <t-button theme="primary" @click="showSubmitDialog()">
        <template #icon><AddIcon /></template>
        提交总结
      </t-button>
    </div>

    <!-- Search & filter -->
    <div class="filter-bar">
      <t-input
        v-model="keyword"
        placeholder="搜索反馈标题或内容..."
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
      row-key="id"
      hover
      stripe
      :pagination="pagination"
      @page-change="handlePageChange"
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
      <template #action="{ row }">
        <t-space>
          <t-link theme="primary" hover="color" size="small" @click="showDetail(row)">
            查看详情
          </t-link>
          <t-link theme="default" hover="color" size="small" @click="downloadAttachments(row)">
            下载附件
          </t-link>
        </t-space>
      </template>
    </t-table>

    <!-- Submit feedback dialog -->
    <t-dialog
      v-model:visible="submitVisible"
      header="提交工作总结"
      width="680px"
      :confirm-btn="{ content: '提交', theme: 'primary' }"
      @confirm="handleSubmit"
    >
      <t-form ref="submitFormRef" :data="submitForm" :rules="submitRules" label-width="100px">
        <t-form-item label="所属活动" name="activityId">
          <t-select
            v-model="submitForm.activityId"
            placeholder="请选择活动"
            filterable
          >
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
          <t-textarea
            v-model="submitForm.content"
            placeholder="请输入工作总结内容..."
            :autosize="{ minRows: 6, maxRows: 14 }"
          />
        </t-form-item>
        <t-form-item label="附件上传">
          <t-upload
            v-model="submitFileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :max="5"
            :size-limit="{ size: 20, unit: 'MB' }"
            theme="file-flow"
            :abridge-name="[8, 6]"
          />
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
            <t-tag
              :theme="currentRecord.userType === 'STUDENT' ? 'primary' : 'warning'"
              variant="light"
              size="small"
            >
              {{ currentRecord.userType === 'STUDENT' ? '学生' : '教师' }}
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
        <div v-if="currentRecord.attachments && currentRecord.attachments.length > 0" class="attachment-section">
          <h4 class="detail-subtitle">附件列表</h4>
          <t-link
            v-for="(file, index) in currentRecord.attachments"
            :key="index"
            theme="primary"
            hover="color"
            class="attachment-link"
          >
            <template #prefix-icon><FileIcon /></template>
            {{ file.name || file.fileName || '附件' + (index + 1) }}
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
import { getToken } from '@/utils/auth'
import { SearchIcon, AddIcon, FileIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const activityFilter = ref('')
const activityOptions = ref([])

const submitVisible = ref(false)
const detailVisible = ref(false)
const currentRecord = ref(null)
const submitFormRef = ref(null)
const submitFileList = ref([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

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
  { colKey: 'userType', title: '类型', width: 80 },
  { colKey: 'activityTitle', title: '所属活动', ellipsis: true, width: 180 },
  { colKey: 'title', title: '标题', ellipsis: true },
  { colKey: 'createTime', title: '提交时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 140 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = activityFilter.value
      ? await feedbackApi.getActivityFeedbacks(activityFilter.value)
      : await feedbackApi.getAllFeedbacks()
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
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

const showSubmitDialog = () => {
  submitForm.value = { activityId: '', title: '', content: '' }
  submitFileList.value = []
  submitVisible.value = true
}

const handleSubmit = async () => {
  const valid = await submitFormRef.value.validate()
  if (valid !== true) return

  try {
    await feedbackApi.submit({
      activityId: submitForm.value.activityId,
      title: submitForm.value.title,
      content: submitForm.value.content,
      fileIds: submitFileList.value.map(f => f.response?.data || f.url).filter(Boolean)
    })
    MessagePlugin.success('提交成功')
    submitVisible.value = false
    fetchData()
  } catch (err) {
    console.error('提交反馈失败', err)
  }
}

const showDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

const downloadAttachments = (row) => {
  if (row.attachments && row.attachments.length > 0) {
    row.attachments.forEach(file => {
      if (file.url) window.open(file.url)
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
.feedback-list { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-input { flex: 1; }
.filter-select { width: 220px; flex-shrink: 0; }
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
