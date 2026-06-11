<template>
  <div class="h5-my-feedbacks">
    <H5NavBar title="我的反馈" />

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else class="content-wrap">
      <!-- 顶部过滤和提交 -->
      <div class="top-bar">
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
        <t-button theme="primary" size="small" @click="showSubmitDialog">
          提交反馈
        </t-button>
      </div>

      <!-- 反馈列表 -->
      <div v-if="records.length > 0" class="feedback-list">
        <div
          v-for="item in records"
          :key="item.id"
          class="feedback-item"
          @click="showDetail(item)"
        >
          <div class="item-header">
            <div class="item-title">{{ item.title || '反馈' }}</div>
            <div class="item-time">{{ formatDateTime(item.createTime) }}</div>
          </div>
          <div class="item-activity">{{ item.activityTitle || '-' }}</div>
          <div class="item-content">
            <div v-html="item.content?.substring(0, 100) + (item.content?.length > 100 ? '...' : '')" />
          </div>
          <div v-if="getAttachments(item).length > 0" class="item-attachments">
            <span class="attachment-icon">📎</span>
            <span>{{ getAttachments(item).length }}个附件</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-wrap">
        <div class="empty-text">暂无反馈记录</div>
      </div>
    </div>

    <!-- Submit dialog -->
    <t-popup
      v-model:visible="submitVisible"
      placement="bottom"
      class="submit-popup"
    >
      <div class="submit-content">
        <div class="popup-header">
          <div class="popup-title">提交反馈</div>
          <div class="close-btn" @click="closeSubmitDialog">×</div>
        </div>

        <div class="popup-body">
          <t-form ref="submitFormRef" :data="submitForm" :rules="submitRules" label-width="auto">
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
                />
              </t-select>
            </t-form-item>
            <t-form-item label="反馈标题" name="title">
              <t-input v-model="submitForm.title" placeholder="请输入反馈标题" clearable />
            </t-form-item>
            <t-form-item label="反馈内容" name="content">
              <t-textarea
                v-model="submitForm.content"
                placeholder="请输入反馈内容..."
                :autosize="{ minRows: 5, maxRows: 12 }"
              />
            </t-form-item>
            <t-form-item label="附件上传">
              <FileUploader v-model="submitFileList" :multiple="true" />
            </t-form-item>
          </t-form>
        </div>

        <div class="popup-footer">
          <t-button theme="default" size="large" block @click="closeSubmitDialog">
            取消
          </t-button>
          <t-button theme="primary" size="large" block :loading="submitting" @click="handleSubmitFeedback">
            提交
          </t-button>
        </div>
      </div>
    </t-popup>

    <!-- Detail popup -->
    <t-popup
      v-model:visible="detailVisible"
      placement="bottom"
      class="detail-popup"
    >
      <div v-if="currentFeedback" class="detail-content">
        <div class="detail-header">
          <div class="detail-title">反馈详情</div>
          <div class="close-btn" @click="closeDetailDialog">×</div>
        </div>

        <div class="detail-body">
          <div class="info-section">
            <div class="info-item">
              <span class="info-key">活动名称</span>
              <span class="info-val">{{ currentFeedback.activityTitle || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">提交时间</span>
              <span class="info-val">{{ formatDateTime(currentFeedback.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">反馈标题</span>
              <span class="info-val">{{ currentFeedback.title || '-' }}</span>
            </div>
          </div>

          <div class="feedback-body">
            <div class="section-title">反馈内容</div>
            <div class="content-full" v-html="currentFeedback.content || '暂无内容'" />
          </div>

          <div v-if="getAttachments(currentFeedback).length > 0" class="attachment-section">
            <div class="section-title">附件列表</div>
            <div
              v-for="(url, index) in getAttachments(currentFeedback)"
              :key="index"
              class="attachment-item"
              @click="downloadFile(url)"
            >
              <span class="attachment-icon">📎</span>
              <span class="file-name">{{ getFileName(url) || '附件' + (index + 1) }}</span>
              <t-icon name="download" size="16" class="download-icon" />
            </div>
          </div>
        </div>
      </div>
    </t-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { feedbackApi } from '@/api/feedback'
import { registrationApi } from '@/api/registeration'
import { getFileUrl } from '@/utils/file'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'
import FileUploader from '@/components/business/FileUploader.vue'

const loading = ref(false)
const records = ref([])
const activityFilter = ref('')
const activityOptions = ref([])
const approvedActivities = ref([])
const submitting = ref(false)

const submitVisible = ref(false)
const detailVisible = ref(false)
const currentFeedback = ref(null)
const submitFormRef = ref(null)
const submitFileList = ref([])

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

const getAttachments = (row) => {
  if (!row) return []
  if (Array.isArray(row.attachments) && row.attachments.length > 0) {
    return row.attachments.map(a => getFileUrl(a.url || a.fileUrl || a))
  }
  if (row.attachmentUrls) {
    if (Array.isArray(row.attachmentUrls)) {
      return row.attachmentUrls.filter(Boolean).map(url => getFileUrl(url))
    }
    if (typeof row.attachmentUrls === 'string') {
      return row.attachmentUrls.split(',').filter(Boolean).map(url => getFileUrl(url.trim()))
    }
  }
  return []
}

const getFileName = (url) => {
  if (!url) return ''
  try {
    const parts = url.split('/')
    const filename = parts[parts.length - 1].split('?')[0]
    return filename.length > 40 ? filename.substring(0, 37) + '...' : filename
  } catch {
    return '附件'
  }
}

const downloadFile = (url) => {
  if (url) {
    window.open(url)
  } else {
    MessagePlugin.info('附件地址无效')
  }
}

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getMyFeedbacks(activityFilter.value || undefined)
    // 兼容多种返回格式：直接数组 或 {records: []}
    if (Array.isArray(res.data)) {
      records.value = res.data
    } else if (res.data?.records && Array.isArray(res.data.records)) {
      records.value = res.data.records
    } else {
      records.value = []
    }
  } catch (err) {
    console.error('获取反馈列表失败', err)
    records.value = []
  } finally {
    loading.value = false
  }
}

const fetchActivityOptions = async () => {
  try {
    const regRes = await registrationApi.getMyRegistrations({ page: 1, size: 1000 })
    const approvedRegs = (regRes.data?.records || regRes.data || []).filter(
      r => r.status === 1 || r.status === 2
    )
    activityOptions.value = approvedRegs.map(r => ({
      value: r.activityId,
      label: r.activityTitle || r.activityName || '活动'
    }))
    approvedActivities.value = activityOptions.value
  } catch (err) {
    console.error('获取活动选项失败', err)
  }
}

const handleSearch = () => {
  fetchFeedbacks()
}

const showSubmitDialog = () => {
  submitForm.value = { activityId: '', title: '', content: '' }
  submitFileList.value = []
  submitVisible.value = true
}

const closeSubmitDialog = () => {
  submitVisible.value = false
}

const closeDetailDialog = () => {
  detailVisible.value = false
}

const handleSubmitFeedback = async () => {
  const valid = await submitFormRef.value.validate()
  if (valid !== true) return

  submitting.value = true
  try {
    let attachmentUrls = ''
    if (submitFileList.value) {
      if (Array.isArray(submitFileList.value)) {
        attachmentUrls = submitFileList.value.filter(Boolean).join(',')
      } else if (typeof submitFileList.value === 'string') {
        attachmentUrls = submitFileList.value
      }
    }

    await feedbackApi.submit({
      activityId: submitForm.value.activityId,
      title: submitForm.value.title,
      content: submitForm.value.content,
      attachmentUrls: attachmentUrls
    })
    MessagePlugin.success('反馈提交成功')
    submitVisible.value = false
    fetchFeedbacks()
  } catch (err) {
    console.error('提交反馈失败', err)
    MessagePlugin.error(err.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
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
.h5-my-feedbacks {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.content-wrap {
  padding: 12px;
}

.top-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.filter-select {
  flex: 1;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feedback-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
}

.feedback-item:active {
  background: #f9fafb;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  margin-right: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

.item-activity {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.item-content {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

.item-attachments {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
  font-size: 12px;
  color: #2563eb;
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

.submit-popup,
.detail-popup {
  width: 100%;
  height: 85vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
}

.popup-header,
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.popup-title,
.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  font-size: 28px;
  color: #9ca3af;
  cursor: pointer;
  line-height: 1;
  padding: 0 8px;
}

.popup-body,
.detail-body {
  padding: 16px;
  max-height: 65vh;
  overflow-y: auto;
}

.popup-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
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

.feedback-body {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.content-full {
  font-size: 14px;
  line-height: 1.8;
  color: #6b7280;
}

.attachment-section {
  margin-bottom: 20px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.attachment-item:active {
  background: #f3f4f6;
}

.file-name {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.download-icon {
  color: #2563eb;
}
</style>
