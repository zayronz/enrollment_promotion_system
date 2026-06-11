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
        <FileUploader
          v-model="form.bannerImages"
          :multiple="false"
          accept="image/*"
          :crop="true"
          tip-text="建议上传横向图片；上传时可裁剪选择 16:9 轮播显示范围"
        />
      </t-form-item>

      <t-form-item label="宣传视频">
        <FileUploader v-model="form.videoUrl" :multiple="false" accept="video/*" />
      </t-form-item>

      <!-- 报名设置 -->
      <t-divider align="left">报名设置</t-divider>

      <t-form-item label="资格条件">
        <t-space>
          <t-input-number
            v-model="form.minGpa"
            :min="0"
            :max="5"
            :step="0.1"
            :decimal-places="2"
            placeholder="最低绩点"
            style="width: 180px"
          />
          <t-input-number
            v-model="form.minScore"
            :min="0"
            :step="1"
            :decimal-places="2"
            placeholder="最低成绩"
            style="width: 180px"
          />
        </t-space>
        <div class="tip">学生登录后，低于设置要求的活动将不会显示；不填写表示不限制。</div>
      </t-form-item>

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
        <t-input-number v-model="form.maxStudentPerSchool" :min="0" placeholder="每校最多学生数" style="width: 180px" />
        <t-input-number v-model="form.maxTeacherPerSchool" :min="0" placeholder="每校最多教师数" style="width: 180px; margin-left: 16px" />
      </t-form-item>

      <t-form-item label="自动分组">
        <t-switch v-model="form.autoGroup" />
        <span class="tip">开启后，系统将按目标学校自动分组并排名</span>
      </t-form-item>

      <t-form-item label="参与人群">
        <t-select
          v-model="form.allowedCollegeIds"
          multiple
          clearable
          filterable
          placeholder="选择允许参与的学院，不选则不限制学院"
          style="width: 420px"
        >
          <t-option
            v-for="item in collegeOptions"
            :key="item.value"
            :value="item.value"
            :label="item.label"
          />
        </t-select>
        <div class="tip">可用于测试“仅允许信息学院学生和指定老师参加”。</div>
      </t-form-item>

      <t-form-item label="指定用户">
        <t-input
          v-model="form.allowedUsernamesText"
          placeholder="输入用户名或姓名，多个用逗号分隔，例如：teacher01,张三"
          style="width: 420px"
          clearable
        />
      </t-form-item>

      <t-form-item label="反馈截止">
        <t-date-picker
          v-model="form.feedbackDeadline"
          enable-time-picker
          clearable
          format="YYYY-MM-DD HH:mm:ss"
          placeholder="超过该时间后不可提交反馈"
          style="width: 260px"
        />
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
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const publishing = ref(false)
const isCreate = ref(false)
const formRef = ref(null)
const collegeOptions = ref([])

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
  minGpa: null,
  minScore: null,
  maxStudentPerSchool: 10,
  maxTeacherPerSchool: 5,
  autoGroup: true,
  allowedCollegeIds: [],
  allowedUsernamesText: '',
  feedbackDeadline: null,
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
    form.minGpa = data.minGpa ?? null
    form.minScore = data.minScore ?? null
    form.allowedCollegeIds = data.allowedCollegeIds || []
    form.allowedUsernamesText = (data.allowedUsernames || []).join(',')
    form.feedbackDeadline = data.feedbackDeadline || null
    
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
  // 格式化日期时间：解析 ISO 格式并转换为后端期望的 "yyyy-MM-dd HH:mm:ss"
  const formatDateTime = (dt) => {
    if (!dt) return null
    let d
    if (typeof dt === 'string') {
      d = new Date(dt)
    } else if (dt instanceof Date) {
      d = dt
    } else {
      d = new Date(dt)
    }
    if (isNaN(d.getTime())) return null
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    const s = String(d.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${min}:${s}`
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
    minGpa: form.minGpa === '' ? null : form.minGpa,
    minScore: form.minScore === '' ? null : form.minScore,
    maxStudentPerSchool: form.maxStudentPerSchool,
    maxTeacherPerSchool: form.maxTeacherPerSchool,
    autoGroup: form.autoGroup,
    allowedCollegeIds: form.allowedCollegeIds,
    allowedUsernames: form.allowedUsernamesText
      ? form.allowedUsernamesText.split(/[,，]/).map(item => item.trim()).filter(Boolean)
      : [],
    feedbackDeadline: form.feedbackDeadline ? formatDateTime(form.feedbackDeadline) : null,
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

const fetchColleges = async () => {
  try {
    const res = await request.get('/college/list')
    collegeOptions.value = (res.data || []).map(item => ({
      value: item.id,
      label: item.name
    }))
  } catch (err) {
    console.error('获取学院列表失败', err)
  }
}

onMounted(() => {
  fetchColleges()
  loadActivity()
})
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
</style>
