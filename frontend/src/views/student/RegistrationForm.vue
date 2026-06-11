<template>
  <div class="registration-form">
    <t-loading v-if="loading" text="加载中..." size="small" class="loading-wrap" />

    <div v-else-if="activity" class="form-body">
      <div class="form-header">
        <h2 class="form-title">{{ activity.title }}</h2>
        <t-tag theme="primary" variant="light">活动报名</t-tag>
      </div>

      <div class="form-card">
        <t-form
          ref="formRef"
          :data="formData"
          :rules="rules"
          label-width="100px"
          @submit="handleSubmit"
        >
          <t-form-item label="目标学校" name="targetSchool">
            <t-auto-complete
              v-model="formData.targetSchool"
              :options="schoolSuggestions"
              placeholder="请输入招生对象学校名称"
              clearable
              @input="handleSchoolInput"
            />
          </t-form-item>

          <t-form-item label="成绩/绩点" name="score">
            <t-input
              v-model="formData.score"
              type="number"
              placeholder="请输入成绩或绩点"
              clearable
            />
          </t-form-item>

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

          <t-form-item label="附件上传" name="attachments">
            <FileUploader
              v-model="formData.attachments"
              :multiple="true"
              :limit="5"
              accept="image/*,.pdf,.doc,.docx,.xls,.xlsx"
              list-type="file"
              tip-text="支持图片、PDF、Word、Excel格式，单个文件不超过10MB"
            />
          </t-form-item>

          <t-form-item>
            <t-space size="large">
              <t-button theme="primary" type="submit" size="large" :loading="submitting">
                确认提交
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registeration'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import FileUploader from '@/components/business/FileUploader.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const activity = ref(null)
const loading = ref(true)
const submitting = ref(false)
const customFields = ref([])
const schoolSuggestions = ref([])
const searchTimeout = ref(null)

const formData = reactive({
  targetSchool: '',
  score: null,
  attachments: []
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

    customFields.value.forEach((field, index) => {
      formData['custom_' + index] = ''
    })
  } catch (err) {
    console.error('获取活动详情失败', err)
  } finally {
    loading.value = false
  }
}

const handleSchoolInput = async (value) => {
  if (!value || value.length < 2) {
    schoolSuggestions.value = []
    return
  }

  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }

  searchTimeout.value = setTimeout(async () => {
    try {
      const res = await fetch(`/api/school-dict/search?keyword=${encodeURIComponent(value)}`)
      const data = await res.json()
      if (data.code === 200 && data.data) {
        schoolSuggestions.value = data.data.map(item => ({
          label: item.name,
          value: item.name
        }))
      }
    } catch (err) {
      console.error('搜索学校失败', err)
    }
  }, 300)
}

const handleSubmit = async (e) => {
  if (e && e.preventDefault) e.preventDefault()

  console.log('开始提交报名表单')
  
  const valid = await formRef.value.validate()
  console.log('表单验证结果:', valid)
  
  if (valid !== true) {
    console.log('表单验证失败')
    return
  }

  const dialog = DialogPlugin.confirm({
    header: '确认提交',
    body: '确定要提交此报名吗？提交后需等待审核。',
    confirmBtn: '确认提交',
    cancelBtn: '取消',
    onConfirm: async () => {
      console.log('用户点击确认提交')
      dialog.destroy()
      submitting.value = true
      try {
        const customData = {}
        customFields.value.forEach((field, index) => {
          customData[field.name] = formData['custom_' + index]
        })

        const payload = {
          activityId: route.params.id,
          targetSchool: formData.targetSchool,
          score: formData.score,
          formData: customData
        }
        console.log('提交的数据:', payload)
        
        const result = await registrationApi.submit(payload)
        console.log('提交成功:', result)
        
        MessagePlugin.success('报名提交成功')
        
        setTimeout(() => {
          console.log('跳转到报名列表页')
          router.push('/student/my-registrations')
        }, 1500)
        
      } catch (err) {
        console.error('提交报名失败', err)
        MessagePlugin.error(err.response?.data?.message || err.message || '提交失败')
      } finally {
        submitting.value = false
      }
    },
    onCancel: () => {
      console.log('用户点击取消')
      dialog.destroy()
    }
  })
}

onMounted(fetchActivity)
</script>

<style scoped>
.registration-form {
  padding: 0;
}
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.form-body {
  max-width: 720px;
}
.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.form-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 0;
}
.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  border: 1px solid var(--td-border-level-1-color);
}

@media (max-width: 640px) {
  .form-card {
    padding: 20px;
  }
}
</style>
