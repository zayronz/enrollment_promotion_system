<template>
  <div class="h5-activity-edit">
    <H5NavBar title="编辑活动" />
    <div class="edit-content">
      <div v-if="loading" class="state-tip">加载中...</div>
      <template v-else>
        <div class="form-section">
          <div class="section-title">基本信息</div>
          <div class="form-item">
            <label class="form-label">活动名称 <span class="required">*</span></label>
            <t-input v-model="form.name" placeholder="请输入活动名称" clearable />
          </div>
          <div class="form-item">
            <label class="form-label">活动类型</label>
            <t-radio-group v-model="form.type" variant="default-filled">
              <t-radio-button :value="0">线上活动</t-radio-button>
              <t-radio-button :value="1">线下活动</t-radio-button>
            </t-radio-group>
          </div>
          <div class="form-item">
            <label class="form-label">活动地点</label>
            <t-input v-model="form.location" placeholder="请输入活动地点" clearable />
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">时间安排</div>
          <div class="form-item">
            <label class="form-label">活动时间</label>
            <div class="date-range">
              <t-date-picker v-model="form.activityStartTime" placeholder="开始日期" />
              <span class="range-sep">至</span>
              <t-date-picker v-model="form.activityEndTime" placeholder="结束日期" />
            </div>
          </div>
          <div class="form-item">
            <label class="form-label">报名时间</label>
            <div class="date-range">
              <t-date-picker v-model="form.registrationStartTime" placeholder="开始日期" />
              <span class="range-sep">至</span>
              <t-date-picker v-model="form.registrationEndTime" placeholder="结束日期" />
            </div>
          </div>
          <div class="form-item">
            <label class="form-label">反馈截止时间</label>
            <t-date-picker v-model="form.feedbackDeadline" placeholder="设置反馈截止时间" />
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">活动详情</div>
          <div class="form-item">
            <label class="form-label">活动简介</label>
            <t-textarea v-model="form.description" placeholder="请输入活动简介" :autosize="{ minRows: 4, maxRows: 8 }" />
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">报名设置</div>
          <div class="form-item">
            <label class="form-label">最低绩点要求</label>
            <t-input-number v-model="form.minGpa" :min="0" :max="4" :decimal-places="2" placeholder="不限制请留空" />
          </div>
          <div class="form-item">
            <label class="form-label">每校学生上限</label>
            <t-input-number v-model="form.maxStudentsPerSchool" :min="0" :max="999" placeholder="不限制请留空" />
          </div>
          <div class="form-item">
            <label class="form-label">每校教师上限</label>
            <t-input-number v-model="form.maxTeachersPerSchool" :min="0" :max="999" placeholder="不限制请留空" />
          </div>
        </div>

        <div class="form-actions">
          <t-button theme="primary" block size="large" :loading="submitting" @click="handleSubmit">保存修改</t-button>
          <t-button theme="danger" block size="large" style="margin-top: 12px;" variant="outline" @click="handleDelete">删除活动</t-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { activityApi } from '@/api/activity'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  name: '',
  type: 0,
  location: '',
  activityStartTime: '',
  activityEndTime: '',
  registrationStartTime: '',
  registrationEndTime: '',
  feedbackDeadline: '',
  description: '',
  minGpa: null,
  maxStudentsPerSchool: null,
  maxTeachersPerSchool: null
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(route.params.id)
    const data = res.data
    if (data) {
      Object.assign(form, {
        name: data.name || '',
        type: data.type ?? 0,
        location: data.location || '',
        activityStartTime: data.activityStartTime || '',
        activityEndTime: data.activityEndTime || '',
        registrationStartTime: data.registrationStartTime || '',
        registrationEndTime: data.registrationEndTime || '',
        feedbackDeadline: data.feedbackDeadline || '',
        description: data.description || '',
        minGpa: data.minGpa ?? null,
        maxStudentsPerSchool: data.maxStudentsPerSchool ?? null,
        maxTeachersPerSchool: data.maxTeachersPerSchool ?? null
      })
    }
  } catch (err) {
    MessagePlugin.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.name.trim()) {
    MessagePlugin.warning('请输入活动名称')
    return
  }
  submitting.value = true
  try {
    const payload = { ...form }
    if (payload.minGpa === null || payload.minGpa === undefined || payload.minGpa === '') {
      delete payload.minGpa
    }
    if (payload.maxStudentsPerSchool === null || payload.maxStudentsPerSchool === undefined || payload.maxStudentsPerSchool === '') {
      delete payload.maxStudentsPerSchool
    }
    if (payload.maxTeachersPerSchool === null || payload.maxTeachersPerSchool === undefined || payload.maxTeachersPerSchool === '') {
      delete payload.maxTeachersPerSchool
    }
    await activityApi.updateActivity(route.params.id, payload)
    MessagePlugin.success('活动更新成功')
    router.push('/h5/school/activity-list')
  } catch (err) {
    console.error('更新活动失败', err)
    MessagePlugin.error(err.response?.data?.message || '更新活动失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async () => {
  const confirm = window.confirm('确定要删除该活动吗？此操作不可撤销。')
  if (!confirm) return
  try {
    await activityApi.deleteActivity(route.params.id)
    MessagePlugin.success('活动已删除')
    router.push('/h5/school/activity-list')
  } catch (err) {
    MessagePlugin.error('删除活动失败')
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.h5-activity-edit {
  min-height: 100vh;
  background: #f5f7fa;
}
.edit-content {
  padding: 16px;
}
.state-tip {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 14px;
}
.form-section {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid #0052d9;
}
.form-item {
  margin-bottom: 16px;
}
.form-item:last-child {
  margin-bottom: 0;
}
.form-label {
  display: block;
  font-size: 14px;
  color: #374151;
  margin-bottom: 8px;
  font-weight: 500;
}
.required {
  color: #ef4444;
}
.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}
.range-sep {
  color: #9ca3af;
  font-size: 13px;
  flex-shrink: 0;
}
.form-actions {
  padding: 8px 0 24px;
}
</style>
