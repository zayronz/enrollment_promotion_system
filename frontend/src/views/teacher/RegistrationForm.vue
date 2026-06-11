<template>
  <div class="registration-form">
    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else-if="activity" class="form-body">
      <div class="form-header">
        <h2 class="form-title">{{ activity.title }}</h2>
        <t-tag theme="warning" variant="light">教师报名</t-tag>
      </div>

      <div class="form-card">
        <div class="basic-info">
          <div class="basic-title">报名人基本信息</div>
          <div class="basic-row"><span>姓名</span><strong>{{ userStore.realName || '-' }}</strong></div>
          <div class="basic-row"><span>工号</span><strong>{{ userStore.userInfo?.username || '-' }}</strong></div>
          <div class="basic-row"><span>学院</span><strong>{{ userStore.collegeName || '-' }}</strong></div>
          <div class="basic-row"><span>手机号</span><strong>{{ userStore.userInfo?.phone || '-' }}</strong></div>
          <div class="basic-row"><span>邮箱</span><strong>{{ userStore.userInfo?.email || '-' }}</strong></div>
          <div class="basic-tip">以上信息自动从个人资料读取，不可在报名时修改。</div>
        </div>

        <t-alert
          v-if="registrationStatus.message && !registrationStatus.canRegister"
          theme="warning"
          :message="registrationStatus.message"
          style="margin-bottom: 16px"
        />

        <t-form
          ref="formRef"
          :data="formData"
          :rules="rules"
          label-width="100px"
          @submit="handleSubmit"
        >
          <!-- Target school with autocomplete -->
          <t-form-item label="目标学校" name="targetSchool">
            <t-auto-complete
              v-model="formData.targetSchool"
              :options="schoolSuggestions"
              placeholder="请输入招生对象学校名称"
              clearable
              @change="handleSchoolInput"
              @focus="handleSchoolInput(formData.targetSchool)"
            />
          </t-form-item>

          <!-- Dynamic custom fields -->
          <t-form-item
            v-for="(field, index) in customFields"
            :key="index"
            :label="field.name"
            :name="'custom_' + index"
            :rules="field.required ? [{ required: true, message: '请填写' + field.name }] : undefined"
          >
            <t-input
              v-if="field.type === 'text'"
              v-model="formData['custom_' + index]"
              :placeholder="'请输入' + field.name"
              clearable
            />
            <t-textarea
              v-else-if="field.type === 'textarea'"
              v-model="formData['custom_' + index]"
              :placeholder="'请输入' + field.name"
              :maxlength="500"
            />
            <t-select
              v-else-if="field.type === 'select'"
              v-model="formData['custom_' + index]"
              :placeholder="'请选择' + field.name"
              clearable
            >
              <t-option
                v-for="opt in (field.options || '').split(',')"
                :key="opt"
                :value="opt.trim()"
                :label="opt.trim()"
              />
            </t-select>
            <t-date-picker
              v-else-if="field.type === 'date'"
              v-model="formData['custom_' + index]"
              :placeholder="'请选择' + field.name"
              style="width:100%"
            />
            <t-input
              v-else
              v-model="formData['custom_' + index]"
              :placeholder="'请输入' + field.name"
              clearable
            />
          </t-form-item>

          <!-- File upload -->
          <t-form-item label="附件上传" name="files">
            <t-upload
              v-model="fileList"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :max="5"
              :accept="attachmentAccept"
              :allow-upload="allowAttachmentUpload"
              :size-limit="{ size: 10, unit: 'MB' }"
              theme="file-flow"
              :abridge-name="[8, 6]"
              tips="仅支持 PDF、Word、Excel 文件，单个文件不超过 10MB"
            />
          </t-form-item>

          <t-form-item>
            <t-space size="large">
              <t-button theme="primary" type="submit" size="large" :loading="submitting" :disabled="!registrationStatus.canRegister">
                {{ registrationStatus.registered ? '已报名' : '确认提交' }}
              </t-button>
              <t-button theme="default" variant="outline" size="large" @click="$router.back()">
                返回
              </t-button>
            </t-space>
          </t-form-item>
        </t-form>
      </div>
    </div>

    <t-empty v-else description="活动不存在" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registeration'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const activity = ref(null)
const loading = ref(true)
const submitting = ref(false)
const customFields = ref([])
const schoolSuggestions = ref([])
const schoolSearchTimer = ref(null)
const schoolSearchSeq = ref(0)
const fileList = ref([])
const registrationStatus = reactive({
  registered: false,
  canRegister: true,
  message: ''
})

const uploadUrl = '/api/file/upload?bizType=registrationAttachment'
const attachmentAccept = '.pdf,.doc,.docx,.xls,.xlsx'
const allowedAttachmentExts = ['pdf', 'doc', 'docx', 'xls', 'xlsx']
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const formData = reactive({
  targetSchool: ''
})

const rules = {
  targetSchool: [{ required: true, message: '请输入目标学校', trigger: 'blur' }]
}

const allowAttachmentUpload = (file) => {
  const fileName = file?.name || ''
  const ext = fileName.split('.').pop()?.toLowerCase()
  if (!allowedAttachmentExts.includes(ext)) {
    MessagePlugin.warning('不支持该文件类型，请上传PDF/Word/Excel')
    return false
  }
  return true
}

const fetchActivity = async () => {
  loading.value = true
  try {
    const [res, statusRes] = await Promise.all([
      activityApi.getActivityDetail(route.params.id),
      registrationApi.getRegistrationStatus(route.params.id)
    ])
    activity.value = res.data
    Object.assign(registrationStatus, statusRes.data || {})
    customFields.value = res.data.customFields || []

    customFields.value.forEach((field, index) => {
      formData['custom_' + index] = ''
    })
  } catch (err) {
    console.error('获取活动详情失败', err)
  } finally {
    loading.value = false
  }
}

const handleSchoolInput = (value) => {
  const keyword = String(value || '').trim()
  if (schoolSearchTimer.value) {
    clearTimeout(schoolSearchTimer.value)
  }
  if (!keyword) {
    schoolSuggestions.value = []
    return
  }
  schoolSearchTimer.value = setTimeout(async () => {
    const currentSeq = ++schoolSearchSeq.value
    try {
      const res = await registrationApi.getSchoolSuggestions({
        activityId: route.params.id,
        keyword
      })
      if (currentSeq !== schoolSearchSeq.value) return
      schoolSuggestions.value = (res.data || []).map(item => ({ label: item, value: item }))
    } catch (err) {
      if (currentSeq === schoolSearchSeq.value) {
        schoolSuggestions.value = []
      }
    }
  }, 300)
}

const handleSubmit = async (e) => {
  if (e && e.preventDefault) e.preventDefault()
  if (!registrationStatus.canRegister) {
    MessagePlugin.warning(registrationStatus.message || '当前无法报名')
    return
  }

  const valid = await formRef.value.validate()
  if (valid !== true) return

  const dialog = DialogPlugin.confirm({
    header: '确认提交',
    body: '确定要提交此报名吗？提交后需等待审核。',
    confirmBtn: '确认提交',
    cancelBtn: '取消',
    onConfirm: async () => {
      dialog.destroy()
      submitting.value = true
      try {
        const payload = {
          activityId: route.params.id,
          targetSchool: normalizeSchoolBeforeSubmit(formData.targetSchool),
          customFields: customFields.value.map((field, index) => ({
            name: field.name,
            type: field.type,
            value: formData['custom_' + index]
          })),
          fileIds: fileList.value.map(f => f.response?.data || f.url).filter(Boolean)
        }
        await registrationApi.submit(payload)
        MessagePlugin.success('报名提交成功')
        setTimeout(() => {
          router.push('/teacher/my-registrations')
        }, 1500)
      } catch (err) {
        console.error('提交报名失败', err)
        MessagePlugin.error(err.response?.data?.message || err.message || '提交失败')
      } finally {
        submitting.value = false
      }
    },
    onCancel: () => {
      dialog.destroy()
    }
  })
}

const normalizeSchoolBeforeSubmit = (value) => {
  const input = String(value || '').trim()
  const exactOption = schoolSuggestions.value.find(item => item.value === input || item.label === input)
  if (exactOption) return exactOption.value
  const prefixOption = schoolSuggestions.value.find(item => item.value?.includes(input) || item.label?.includes(input))
  return prefixOption?.value || input
}

onMounted(fetchActivity)

watch(() => formData.targetSchool, (value) => {
  handleSchoolInput(value)
})

onBeforeUnmount(() => {
  if (schoolSearchTimer.value) {
    clearTimeout(schoolSearchTimer.value)
  }
})
</script>

<style scoped>
.registration-form { padding: 0; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; }
.form-body { max-width: 720px; }
.form-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.form-title { font-size: 20px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.form-card { background: #fff; border-radius: 12px; padding: 32px; border: 1px solid var(--td-border-level-1-color); }
.basic-info { background: var(--td-bg-color-container-hover); border-radius: 10px; padding: 14px 16px; margin-bottom: 18px; }
.basic-title { font-size: 14px; font-weight: 600; color: var(--td-text-color-primary); margin-bottom: 8px; }
.basic-row { display: flex; justify-content: space-between; font-size: 13px; color: var(--td-text-color-secondary); line-height: 26px; }
.basic-row strong { color: var(--td-text-color-primary); font-weight: 500; }
.basic-tip { margin-top: 8px; padding-top: 8px; border-top: 1px dashed var(--td-border-level-2-color); color: var(--td-text-color-placeholder); font-size: 12px; }
@media (max-width: 640px) {
  .form-card { padding: 20px; }
}
</style>
