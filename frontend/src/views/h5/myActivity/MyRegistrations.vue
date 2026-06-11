<template>
  <div class="h5-my-registrations">
    <H5NavBar title="我的报名" />
    
    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />
    
    <div v-else-if="records.length > 0" class="registration-list">
      <div
        v-for="item in records"
        :key="item.id"
        class="registration-item"
        @click="showDetail(item)"
      >
        <div class="item-header">
          <div class="item-title">{{ item.activityTitle || '活动报名' }}</div>
          <t-tag :theme="getStatusTheme(item.status)" variant="light" size="small">
            {{ getStatusLabel(item.status) }}
          </t-tag>
        </div>
        <div class="item-info">
          <div class="info-row">
            <span class="info-label">目标学校：</span>
            <span class="info-value">{{ item.targetSchool || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">报名时间：</span>
            <span class="info-value">{{ formatDateTime(item.createTime) }}</span>
          </div>
        </div>
        <div class="item-action" v-if="item.status === 0" @click.stop>
          <t-button theme="light" size="small" @click="handleWithdraw(item.id)">
            撤销报名
          </t-button>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-wrap">
      <div class="empty-text">暂无报名记录</div>
    </div>

    <!-- Detail popup -->
    <t-popup
      v-model:visible="detailVisible"
      placement="bottom"
      class="detail-popup"
      @visible-change="onPopupChange"
    >
      <div v-if="currentRecord" class="detail-content">
        <div class="detail-header">
          <div class="detail-title">报名详情</div>
          <t-icon name="close" size="20" class="close-icon" @click="detailVisible = false" />
        </div>
        
        <div class="detail-body">
          <div class="info-section">
            <div class="info-item">
              <span class="info-key">活动名称</span>
              <span class="info-val">{{ currentRecord.activityTitle || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">审核状态</span>
              <t-tag :theme="getStatusTheme(currentRecord.status)" variant="light" size="small">
                {{ getStatusLabel(currentRecord.status) }}
              </t-tag>
            </div>
            <div class="info-item">
              <span class="info-key">审批进度</span>
              <span class="info-val">{{ auditProgress || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">目标学校</span>
              <span class="info-val">{{ currentRecord.targetSchool || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">报名时间</span>
              <span class="info-val">{{ formatDateTime(currentRecord.createTime) }}</span>
            </div>
            <div class="info-item" v-if="currentRecord.groupName">
              <span class="info-key">分组</span>
              <span class="info-val">{{ currentRecord.groupName || '-' }}</span>
            </div>
            <div class="info-item" v-if="currentRecord.groupRank !== null">
              <span class="info-key">排名</span>
              <span class="info-val">#{{ currentRecord.groupRank }}</span>
            </div>
          </div>

          <div v-if="activityDetail" class="custom-fields">
            <div class="section-title">活动详情</div>
            <div class="field-list">
              <div class="field-item">
                <span class="field-label">活动时间：</span>
                <span class="field-value">{{ formatDateTime(activityDetail.activityStartTime) }} 至 {{ formatDateTime(activityDetail.activityEndTime) }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">报名时间：</span>
                <span class="field-value">{{ formatDateTime(activityDetail.registrationStartTime) }} 至 {{ formatDateTime(activityDetail.registrationEndTime) }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">活动地点：</span>
                <span class="field-value">{{ activityDetail.location || '-' }}</span>
              </div>
              <div class="field-item">
                <span class="field-label">活动介绍：</span>
                <span class="field-value" v-html="activityDetail.description || '-'"></span>
              </div>
            </div>
          </div>

          <!-- Custom fields -->
          <div v-if="customFields.length > 0" class="custom-fields">
            <div class="section-title">填报信息</div>
            <div class="field-list">
              <div v-for="(field, index) in customFields" :key="index" class="field-item">
                <span class="field-label">{{ field.label || field.name }}：</span>
                <span class="field-value">{{ field.value || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Attachments -->
          <div v-if="attachments.length > 0" class="custom-fields">
            <div class="section-title">报名附件</div>
            <div class="field-list">
              <div v-for="(file, index) in attachments" :key="index" class="field-item attachment-item" @click="openAttachment(file)">
                <span class="field-label">附件{{ index + 1 }}：</span>
                <span class="field-value attachment-link">{{ getAttachmentName(file) }}</span>
              </div>
            </div>
          </div>

          <div v-if="teamMembers.length > 0" class="custom-fields">
            <div class="section-title">当前分组成员</div>
            <div class="field-list">
              <div v-for="member in teamMembers" :key="member.registrationId" class="member-card-mini">
                <div class="member-title">{{ member.realName || '-' }} <span>#{{ member.groupRank || '-' }}</span></div>
                <div class="member-desc">{{ member.role || '-' }} · {{ member.phone || '-' }}</div>
              </div>
            </div>
          </div>

          <!-- Audit timeline -->
          <div v-if="auditLogs.length > 0" class="audit-timeline">
            <div class="section-title">审核记录</div>
            <div class="timeline-list">
              <div v-for="(log, index) in auditLogs" :key="index" class="timeline-item">
                <div class="timeline-dot" :class="log.result === 'APPROVED' ? 'success' : 'danger'"></div>
                <div class="timeline-content">
                  <div class="timeline-user">{{ log.auditorName || '审核人' }}</div>
                  <div class="timeline-action">
                    {{ log.result === 'APPROVED' ? '通过' : '拒绝' }}
                    <span v-if="log.comment"> — {{ log.comment }}</span>
                  </div>
                  <div class="timeline-time">{{ formatDateTime(log.createTime) }}</div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="feedbacks.length > 0" class="audit-timeline">
            <div class="section-title">工作反馈</div>
            <div class="timeline-list">
              <div v-for="fb in feedbacks" :key="fb.id" class="timeline-item">
                <div class="timeline-dot success"></div>
                <div class="timeline-content">
                  <div class="timeline-user">{{ fb.title || '反馈' }}</div>
                  <div class="timeline-action">
                    {{ fb.userName || '反馈人' }}：<span v-html="fb.content || '-'"></span>
                  </div>
                  <div class="timeline-time">{{ formatDateTime(fb.createTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </t-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { registrationApi } from '@/api/registeration'
import { MessagePlugin } from 'tdesign-vue-next'
import { getFileUrl } from '@/utils/file'
import H5NavBar from '../components/H5NavBar.vue'

const loading = ref(false)
const records = ref([])
const detailVisible = ref(false)
const currentRecord = ref(null)
const customFields = ref([])
const attachments = ref([])
const auditLogs = ref([])
const activityDetail = ref(null)
const teamMembers = ref([])
const feedbacks = ref([])
const auditProgress = ref('')

const fetchRegistrations = async () => {
  loading.value = true
  try {
    const res = await registrationApi.getMyRegistrations({ page: 1, size: 100 })
    records.value = res.data?.records || []
  } catch (err) {
    console.error('获取报名列表失败', err)
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  currentRecord.value = row
  try {
    const res = await registrationApi.getRegistrationDetail(row.id)
    const detail = res.data
    customFields.value = detail.customFields || []
    attachments.value = detail.attachments || []
    auditLogs.value = detail.auditLogs || []
    activityDetail.value = detail.activityDetail || null
    teamMembers.value = detail.teamMembers || []
    feedbacks.value = detail.feedbacks || []
    auditProgress.value = detail.auditProgress || ''
  } catch (err) {
    console.error('获取详情失败', err)
    customFields.value = []
    attachments.value = []
    auditLogs.value = []
    activityDetail.value = null
    teamMembers.value = []
    feedbacks.value = []
    auditProgress.value = ''
  }
  detailVisible.value = true
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

const onPopupChange = (visible) => {
  if (!visible) {
    currentRecord.value = null
    customFields.value = []
    attachments.value = []
    auditLogs.value = []
    activityDetail.value = null
    teamMembers.value = []
    feedbacks.value = []
    auditProgress.value = ''
  }
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

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

const getStatusTheme = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'default' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '待审核', 1: '学院通过', 2: '已通过', 3: '已拒绝', 4: '已撤回' }
  return map[status] || '未知'
}

onMounted(fetchRegistrations)
</script>

<style scoped>
.h5-my-registrations {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
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

.registration-list {
  padding: 12px;
}

.registration-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  margin-right: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}

.info-label {
  color: #9ca3af;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-action {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.detail-popup {
  width: 100%;
  height: 80vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-icon {
  color: #9ca3af;
  cursor: pointer;
}

.detail-body {
  padding: 16px;
  max-height: 70vh;
  overflow-y: auto;
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

.custom-fields {
  margin-bottom: 20px;
}

.audit-timeline {
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

.field-list {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
}

.field-item {
  display: flex;
  padding: 8px 0;
  font-size: 14px;
  line-height: 1.6;
}

.field-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.field-label {
  color: #9ca3af;
  flex-shrink: 0;
}

.field-value {
  color: #1f2937;
  flex: 1;
  margin-left: 8px;
  word-break: break-word;
}
.attachment-item {
  cursor: pointer;
}
.attachment-link {
  color: #2563eb;
}
.member-card-mini {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.member-card-mini:last-child {
  border-bottom: none;
}
.member-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}
.member-title span {
  color: #2563eb;
  font-size: 12px;
  margin-left: 6px;
}
.member-desc {
  font-size: 12px;
  color: #6b7280;
}

.timeline-list {
  position: relative;
  padding-left: 20px;
}

.timeline-list::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: #e5e7eb;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -18px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #e5e7eb;
}

.timeline-dot.success {
  background: #10b981;
}

.timeline-dot.danger {
  background: #ef4444;
}

.timeline-content {
  padding-left: 8px;
}

.timeline-user {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.timeline-action {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
  line-height: 1.6;
}

.timeline-time {
  font-size: 12px;
  color: #9ca3af;
}
</style>
