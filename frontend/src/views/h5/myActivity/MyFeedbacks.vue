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
          <t-icon name="close" size="20" class="close-icon" @click="submitVisible = false" />
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
                placeholder="请输入工作反馈内容..."
                :autosize="{ minRows: 5, maxRows: 12 }"
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
        </div>

        <div class="popup-footer">
          <t-button theme="default" size="large" block @click="submitVisible = false">
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
          <t-icon name="close" size="20" class="close-icon" @click="detailVisible = false" />
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
        </div>
      </div>
    </t-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { feedbackApi } from '@/api/feedback'
import { registrationApi } from '@/api/registeration'
import { activityApi } from '@/api/activity'
import { getToken } from '@/utils/auth'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

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
  title: [{ required: true, message: '请输入反馈标题' }],
  content: [{ required: true, message: '请输入反馈内容' }]
}

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getMyFeedbacks(activityFilter.value || undefined)
    records.value = res.data?.records || []
  } catch (err) {
    console.error('获取反馈列表失败', err)
  } finally {
    loading.value = false
  }
}

const fetchActivityOptions = async () => {
  try {
    const [activityRes, regRes] = await Promise.all([
      activityApi.getActivityList({ page: 1, size: 100 }),
      registrationApi.getMyRegistrations()
    ])
    const allActivities = activityRes.data?.records || []
    const approvedRegs = (regRes.data?.records || []).filter(
      r => r.status === 1 || r.status === 2
    )

    activityOptions.value = allActivities.map(a => ({ value: a.id, label: a.name }))
    approvedActivities.value = allActivities
      .filter(a => approvedRegs.some(r => r.activityId === a.id))
      .map(a => ({ value: a.id, label: a.title }))
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

const handleSubmitFeedback = async () => {
  const valid = await submitFormRef.value.validate()
  if (valid !== true) return

  submitting.value = true
  try {
    await feedbackApi.submit({
      activityId: submitForm.value.activityId,
      title: submitForm.value.title,
      content: submitForm.value.content,
      fileIds: submitFileList.value.map(f => f.response?.data || f.url).filter(Boolean)
    })
    MessagePlugin.success('反馈提交成功')
    submitVisible.value = false
    fetchFeedbacks()
  } catch (err) {
    console.error('提交反馈失败', err)
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
  padding-bottom: 80px;
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

.close-icon {
  color: #9ca3af;
  cursor: pointer;
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
</style>
