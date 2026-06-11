<template>
  <div class="activity-edit">
    <h2 class="page-title">编辑活动</h2>

    <t-form :data="form" :rules="rules" ref="formRef" label-width="120px">
      <!-- 基础信息 -->
      <t-divider align="left">基础信息</t-divider>

      <t-form-item label="活动名称" name="name">
        <t-input v-model="form.name" placeholder="请输入活动名称" />
      </t-form-item>

      <t-form-item label="活动类型" name="type">
        <t-radio-group v-model="form.type">
          <t-radio :value="0">线上活动</t-radio>
          <t-radio :value="1">线下活动</t-radio>
        </t-radio-group>
      </t-form-item>

      <t-form-item label="活动地点" v-if="form.type === 1" name="location">
        <t-input v-model="form.location" placeholder="请输入活动地点" />
      </t-form-item>

      <t-form-item label="活动时间" name="activityTime">
        <t-date-range-picker
          v-model="form.activityTime"
          enable-time-picker
          format="YYYY-MM-DD HH:mm:ss"
          :placeholder="['开始时间', '结束时间']"
          separator="至"
        />
      </t-form-item>

      <t-form-item label="报名时间" name="registrationTime">
        <t-date-range-picker
          v-model="form.registrationTime"
          enable-time-picker
          format="YYYY-MM-DD HH:mm:ss"
          :placeholder="['开始时间', '结束时间']"
          separator="至"
        />
      </t-form-item>

      <t-form-item label="活动介绍" name="description">
        <t-textarea
          v-model="form.description"
          placeholder="请输入活动介绍..."
          :autosize="{ minRows: 6, maxRows: 12 }"
        />
      </t-form-item>

      <!-- 媒体设置 -->
      <t-divider align="left">媒体设置</t-divider>

      <t-form-item label="封面图">
        <FileUploader v-model="form.coverImage" :multiple="false" accept="image/*" />
      </t-form-item>

      <t-form-item label="轮播图">
        <FileUploader v-model="form.bannerImages" :multiple="false" accept="image/*" tip-text="支持上传一张Banner图片" />
      </t-form-item>

      <t-form-item label="宣传视频">
        <FileUploader v-model="form.videoUrl" :multiple="false" accept="video/*" />
      </t-form-item>

      <!-- 报名设置 -->
      <t-divider align="left">报名设置</t-divider>

      <t-form-item label="自定义字段">
        <div class="custom-fields">
          <div v-for="(field, index) in form.customFields" :key="index" class="custom-field-item">
            <t-input v-model="field.label" placeholder="字段名称" style="width: 150px" />
            <t-select v-model="field.type" placeholder="字段类型" style="width: 120px; margin-left: 8px">
              <t-option label="单行文本" value="text" />
              <t-option label="多行文本" value="textarea" />
              <t-option label="下拉选择" value="select" />
              <t-option label="日期" value="date" />
            </t-select>
            <t-input
              v-if="field.type === 'select'"
              v-model="field.optionsStr"
              placeholder="选项（用逗号分隔）"
              style="width: 200px; margin-left: 8px"
            />
            <t-button theme="danger" variant="outline" shape="circle" size="small" style="margin-left: 8px" @click="removeCustomField(index)">
              <template #icon><DeleteIcon /></template>
            </t-button>
          </div>
          <t-button theme="primary" variant="text" @click="addCustomField">
            <template #icon><AddIcon /></template>
            添加自定义字段
          </t-button>
        </div>
      </t-form-item>

      <t-form-item label="人数限制">
        <div class="quota-box">
          <span class="quota-label">学生</span>
          <t-input-number v-model="form.maxStudentPerSchool" :min="0" placeholder="每校最多人数" style="width: 140px" />
        </div>
        <div class="quota-box">
          <span class="quota-label">老师</span>
          <t-input-number v-model="form.maxTeacherPerSchool" :min="0" placeholder="每校最多人数" style="width: 140px" />
        </div>
      </t-form-item>

      <t-form-item label="自动分组">
        <t-switch v-model="form.autoGroup" />
        <span class="tip">开启后，系统将按目标学校自动分组并排名</span>
      </t-form-item>

      <!-- 审批流程 -->
      <t-divider align="left">审批流程</t-divider>

      <t-form-item label="审批流程" name="auditFlow">
        <t-checkbox-group v-model="form.auditFlow">
          <t-checkbox value="college_audit">学院审核</t-checkbox>
          <t-checkbox value="school_audit">学校审核</t-checkbox>
        </t-checkbox-group>
      </t-form-item>

      <t-form-item label="活动状态">
        <t-radio-group v-model="form.status">
          <t-radio value="DRAFT">草稿</t-radio>
          <t-radio value="PUBLISHED" v-if="!isCreate">已发布</t-radio>
        </t-radio-group>
      </t-form-item>

      <t-form-item>
        <t-space>
          <t-button theme="primary" @click="handleSave" :loading="submitting">保存修改</t-button>
          <t-button theme="success" @click="handleSaveAndPublish" :loading="publishing">保存并发布</t-button>
          <t-button theme="default" @click="goBack">取消</t-button>
        </t-space>
      </t-form-item>
    </t-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { DeleteIcon, AddIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'
import FileUploader from '@/components/business/FileUploader.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const publishing = ref(false)
const isCreate = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  type: 0,
  location: '',
  activityTime: [],
  registrationTime: [],
  description: '',
  coverImage: '',
  bannerImages: '',
  videoUrl: '',
  customFields: [],
  maxStudentPerSchool: 10,
  maxTeacherPerSchool: 5,
  autoGroup: true,
  auditFlow: ['college_audit', 'school_audit'],
  status: 'DRAFT'
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  registrationTime: [{ required: true, message: '请选择报名时间', trigger: 'change' }]
}

const addCustomField = () => {
  form.customFields.push({
    label: '',
    type: 'text',
    options: [],
    optionsStr: '',
    required: false
  })
}

const removeCustomField = (index) => {
  form.customFields.splice(index, 1)
}

const loadActivity = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(id)
    const data = res.data
    form.name = data.title || data.name || ''
    // type: 后端返回数字 0/1，直接使用
    form.type = data.type !== undefined && data.type !== null ? data.type : 0
    form.location = data.location || ''
    
    // 活动时间
    const activityStartTime = data.startTime || data.activityStartTime
    const activityEndTime = data.endTime || data.activityEndTime
    form.activityTime = activityStartTime && activityEndTime ? [activityStartTime, activityEndTime] : []
    
    // 报名时间
    const regStartTime = data.registerStartTime || data.registrationStartTime
    const regEndTime = data.registerEndTime || data.registrationEndTime
    form.registrationTime = regStartTime && regEndTime ? [regStartTime, regEndTime] : []
    
    form.description = data.description || data.content || ''
    form.coverImage = data.coverImage || ''
    
    // 轮播图：支持单个和多个
    if (data.bannerUrls && Array.isArray(data.bannerUrls)) {
      form.bannerImages = data.bannerUrls
    } else if (data.bannerUrl) {
      form.bannerImages = [data.bannerUrl]
    } else {
      form.bannerImages = []
    }
    
    form.videoUrl = data.videoUrl || ''
    
    form.customFields = (data.customFields || []).map(f => ({
      ...f,
      optionsStr: Array.isArray(f.options) ? f.options.join(',') : (f.options || '')
    }))
    
    form.maxStudentPerSchool = data.maxStudentPerSchool !== undefined && data.maxStudentPerSchool !== null ? data.maxStudentPerSchool : 10
    form.maxTeacherPerSchool = data.maxTeacherPerSchool !== undefined && data.maxTeacherPerSchool !== null ? data.maxTeacherPerSchool : 5
    form.autoGroup = data.autoGroup !== false
    
    if (data.auditFlow && Array.isArray(data.auditFlow)) {
      form.auditFlow = data.auditFlow
    } else {
      form.auditFlow = ['college_audit', 'school_audit']
    }
    
    // status: 后端返回数字 0/1/2，需要转换为字符串 'DRAFT'/'PUBLISHED'/'ENDED'
    const statusMap = { 0: 'DRAFT', 1: 'PUBLISHED', 2: 'ENDED' }
    form.status = statusMap[data.status] || 'DRAFT'
    
    console.log('加载的活动数据:', data)
    console.log('表单数据:', form)
  } catch (err) {
    console.error('获取活动详情失败', err)
  } finally {
    loading.value = false
  }
}

const buildSubmitData = () => {
  // 格式化日期时间：将 ISO 格式 (T分隔) 转换为后端期望的格式 (空格分隔)
  const formatDateTime = (dt) => {
    if (!dt) return null
    if (typeof dt === 'string') {
      // "2026-06-08T00:00:00" -> "2026-06-08 00:00:00"
      return dt.replace('T', ' ')
    }
    if (dt instanceof Date) {
      const y = dt.getFullYear()
      const m = String(dt.getMonth() + 1).padStart(2, '0')
      const d = String(dt.getDate()).padStart(2, '0')
      const h = String(dt.getHours()).padStart(2, '0')
      const min = String(dt.getMinutes()).padStart(2, '0')
      const s = String(dt.getSeconds()).padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${min}:${s}`
    }
    return dt
  }

  return {
    id: route.params.id,
    name: form.name,
    type: form.type,
    location: form.location,
    activityStartTime: formatDateTime(form.activityTime[0]),
    activityEndTime: formatDateTime(form.activityTime[1]),
    registrationStartTime: formatDateTime(form.registrationTime[0]),
    registrationEndTime: formatDateTime(form.registrationTime[1]),
    description: form.description,
    coverImage: form.coverImage,
    bannerUrl: Array.isArray(form.bannerImages) && form.bannerImages.length > 0 ? form.bannerImages[0] : (form.bannerImages || null),
    bannerUrls: Array.isArray(form.bannerImages) && form.bannerImages.length > 1 ? form.bannerImages : null,
    videoUrl: form.videoUrl,
    customFields: form.customFields.map(f => ({
      ...f,
      options: f.optionsStr ? f.optionsStr.split(',') : []
    })),
    maxStudentPerSchool: form.maxStudentPerSchool,
    maxTeacherPerSchool: form.maxTeacherPerSchool,
    autoGroup: form.autoGroup,
    auditFlow: form.auditFlow,
    status: form.status
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  submitting.value = true
  try {
    const data = buildSubmitData()
    const activityId = data.id
    delete data.id
    await activityApi.updateActivity(activityId, data)
    MessagePlugin.success('保存成功')
    router.push('/school/activity/list')
  } catch (error) {
    MessagePlugin.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const handleSaveAndPublish = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  publishing.value = true
  try {
    const data = buildSubmitData()
    const activityId = data.id
    delete data.id
    await activityApi.updateActivity(activityId, data)
    await activityApi.publishActivity(activityId)
    MessagePlugin.success('保存并发布成功')
    router.push('/school/activity/list')
  } catch (error) {
    MessagePlugin.error('操作失败')
  } finally {
    publishing.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(loadActivity)
</script>

<style scoped>
.activity-edit {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0 0 24px; }
.custom-fields { width: 100%; }
.custom-field-item { display: flex; align-items: center; margin-bottom: 12px; }
.tip { margin-left: 12px; font-size: 12px; color: var(--td-text-color-placeholder); }
.quota-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px 12px;
  margin-right: 16px;
}
.quota-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--td-text-color-primary);
  min-width: 40px;
}
</style>
