<template>
  <div class="activity-create">
    <h2 class="page-title">创建活动</h2>

    <t-form :data="form" :rules="rules" ref="formRef" label-width="120px">
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

      <t-divider align="left">媒体设置</t-divider>

      <t-form-item label="封面图">
        <FileUploader v-model="form.coverImage" :multiple="false" accept="image/*" />
      </t-form-item>

      <t-form-item label="轮播图">
        <FileUploader v-model="form.bannerImages" :multiple="true" :limit="5" accept="image/*" />
      </t-form-item>

      <t-form-item label="宣传视频">
        <FileUploader v-model="form.videoUrl" :multiple="false" accept="video/*" />
      </t-form-item>

      <t-form-item label="附件">
        <FileUploader v-model="form.attachments" :multiple="true" :limit="10" />
        <span class="form-tip">支持上传文档、图片等附件，最多10个</span>
      </t-form-item>

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
        <t-input-number v-model="form.maxStudentPerSchool" :min="0" placeholder="每校最多学生数" style="width: 180px" />
        <t-input-number v-model="form.maxTeacherPerSchool" :min="0" placeholder="每校最多教师数" style="width: 180px; margin-left: 16px" />
      </t-form-item>

      <t-form-item label="自动分组">
        <t-switch v-model="form.autoGroup" />
        <span class="tip">开启后，系统将按目标学校自动分组并排名</span>
      </t-form-item>

      <t-divider align="left">审批流程</t-divider>

      <t-form-item label="审批流程" name="auditFlow">
        <t-checkbox-group v-model="form.auditFlow">
          <t-checkbox value="college_audit">学院审核</t-checkbox>
          <t-checkbox value="school_audit">学校审核</t-checkbox>
        </t-checkbox-group>
      </t-form-item>

      <t-form-item>
        <t-space>
          <t-button theme="primary" @click="handleSubmit" :loading="submitting">保存为草稿</t-button>
          <t-button theme="success" @click="handlePublish" :loading="publishing">发布活动</t-button>
          <t-button theme="default" @click="goBack">取消</t-button>
        </t-space>
      </t-form-item>
    </t-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { DeleteIcon, AddIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'
import FileUploader from '@/components/business/FileUploader.vue'

const router = useRouter()
const submitting = ref(false)
const publishing = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  type: 0,
  location: '',
  activityTime: [],
  registrationTime: [],
  description: '',
  coverImage: '',
  bannerImages: [],
  videoUrl: '',
  attachments: [],
  customFields: [],
  maxStudentPerSchool: 10,
  maxTeacherPerSchool: 5,
  autoGroup: true,
  auditFlow: ['college_audit', 'school_audit']
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  registrationTime: [{ required: true, message: '请选择报名时间', trigger: 'change' }]
}

const formatDate = (date) => {
  if (!date) return ''
  if (typeof date === 'string') return date
  // 处理日期对象
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
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

const buildSubmitData = () => {
  const data = {
    name: form.name,
    type: form.type,
    location: form.type === 1 ? form.location : null,
    activityStartTime: formatDate(form.activityTime[0]),
    activityEndTime: formatDate(form.activityTime[1]),
    registrationStartTime: formatDate(form.registrationTime[0]),
    registrationEndTime: formatDate(form.registrationTime[1]),
    description: form.description,
    coverImage: form.coverImage || null,
    bannerUrl: form.bannerImages.length > 0 ? form.bannerImages[0] : null,
    videoUrl: form.videoUrl || null,
    attachments: form.attachments,
    customFields: form.customFields.map(f => ({
      ...f,
      options: f.optionsStr ? f.optionsStr.split(',') : []
    })),
    maxStudentPerSchool: form.maxStudentPerSchool,
    maxTeacherPerSchool: form.maxTeacherPerSchool,
    autoGroup: form.autoGroup,
    auditFlow: form.auditFlow
  }
  console.log('构建提交数据:', data)
  return data
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  submitting.value = true
  try {
    const data = buildSubmitData()
    console.log('提交数据:', data)
    const res = await activityApi.createActivity(data)
    console.log('创建活动响应:', res)
    MessagePlugin.success('保存成功')
    router.push('/school/activity/list')
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitting.value = false
  }
}

const handlePublish = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  publishing.value = true
  try {
    const data = buildSubmitData()
    console.log('提交数据:', data)
    const createRes = await activityApi.createActivity(data)
    console.log('创建活动响应:', createRes)
    if (createRes.data) {
      await activityApi.publishActivity(createRes.data)
      MessagePlugin.success('发布成功')
      router.push('/school/activity/list')
    } else {
      MessagePlugin.error('获取活动ID失败')
    }
  } catch (error) {
    console.error('发布失败', error)
  } finally {
    publishing.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.activity-create {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0 0 24px; }
.custom-fields { width: 100%; }
.custom-field-item { display: flex; align-items: center; margin-bottom: 12px; }
.tip { margin-left: 12px; font-size: 12px; color: var(--td-text-color-placeholder); }
</style>
