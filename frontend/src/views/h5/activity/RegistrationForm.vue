<template>
  <div class="registration-form">
    <H5NavBar title="活动报名" />

    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else-if="activity" class="form-body">
      <div class="form-header">
        <h2 class="form-title">{{ activity.name || activity.title }}</h2>
        <t-tag theme="primary" variant="light">活动报名</t-tag>
      </div>

      <div class="form-card">
        <t-form
          ref="formRef"
          :data="formData"
          :rules="rules"
          label-width="auto"
          @submit="handleSubmit"
        >
          <!-- Target school with autocomplete -->
          <t-form-item label="目标学校" name="targetSchool">
            <t-input
              v-model="formData.targetSchool"
              placeholder="请输入招生对象学校名称"
              clearable
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
              :autosize="{ minRows: 3, maxRows: 6 }"
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
              :size-limit="{ size: 10, unit: 'MB' }"
              theme="file-flow"
              :abridge-name="[8, 6]"
            />
          </t-form-item>

          <t-form-item>
            <t-button theme="primary" type="submit" size="large" :loading="submitting" block>
              确认提交
            </t-button>
            <t-button theme="default" variant="outline" size="large" @click="$router.back()" block style="margin-top: 12px;">
              返回
            </t-button>
          </t-form-item>
        </t-form>
      </div>
    </div>

    <t-empty v-else description="活动不存在" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registeration'
import { getToken } from '@/utils/auth'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const activity = ref(null)
const loading = ref(true)
const submitting = ref(false)
const customFields = ref([])
const fileList = ref([])

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const formData = reactive({
  targetSchool: ''
})

const rules = {
  targetSchool: [{ required: true, message: '请输入目标学校', trigger: 'blur' }]
}

const fetchActivity = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(route.params.id)
    activity.value = res.data
    customFields.value = res.data.customFields || []

    // Init custom field data
    customFields.value.forEach((field, index) => {
      formData['custom_' + index] = ''
    })
  } catch (err) {
    console.error('获取活动详情失败', err)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async (e) => {
  if (e && e.preventDefault) e.preventDefault()

  const valid = await formRef.value.validate()
  if (valid !== true) return

  // Confirm dialog
  DialogPlugin.confirm({
    header: '确认提交',
    body: '确定要提交此报名吗？提交后需等待审核。',
    confirmBtn: '确认提交',
    onConfirm: async () => {
      submitting.value = true
      try {
        const payload = {
          activityId: route.params.id,
          targetSchool: formData.targetSchool,
          customFields: customFields.value.map((field, index) => ({
            name: field.name,
            value: formData['custom_' + index]
          })),
          fileIds: fileList.value.map(f => f.response?.data || f.url).filter(Boolean)
        }
        await registrationApi.submit(payload)
        MessagePlugin.success('报名提交成功')
        router.push('/h5/my-activity')
      } catch (err) {
        console.error('提交报名失败', err)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(fetchActivity)
</script>

<style scoped>
.registration-form {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.form-body {
  padding: 12px;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.form-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}
</style>
