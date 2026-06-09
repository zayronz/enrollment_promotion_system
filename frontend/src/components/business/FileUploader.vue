<template>
  <div class="file-uploader">
    <t-upload
      v-model="fileList"
      :action="uploadUrl"
      :headers="headers"
      :multiple="multiple"
      :max="limit"
      :accept="accept"
      :theme="listType"
      @success="handleSuccess"
      @fail="handleError"
      @validate="handleValidate"
    >
      <t-button v-if="listType === 'file'" theme="primary">
        <template #icon><t-icon name="upload" /></template>
        点击上传
      </t-button>
      <t-button v-else variant="outline" theme="primary">
        <template #icon><t-icon name="upload" /></template>
        上传图片
      </t-button>
      <template #tips>
        <div class="upload-tips">
          {{ tipText }}
        </div>
      </template>
    </t-upload>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getToken } from '@/utils/auth'
import { getFileUrl } from '@/utils/file'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: ''
  },
  multiple: {
    type: Boolean,
    default: false
  },
  limit: {
    type: Number,
    default: 5
  },
  accept: {
    type: String,
    default: ''
  },
  listType: {
    type: String,
    default: 'file'
  },
  tipText: {
    type: String,
    default: '支持图片、文档、视频格式，单个文件不超过30MB'
  }
})

const emit = defineEmits(['update:modelValue', 'upload-success'])

const uploadUrl = '/api/file/upload'
const headers = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const fileList = ref([])

const syncFileList = () => {
  if (props.modelValue) {
    if (typeof props.modelValue === 'string' && props.modelValue) {
      fileList.value = [{
        name: props.modelValue.split('/').pop(),
        url: getFileUrl(props.modelValue),
        status: 'success'
      }]
    } else if (Array.isArray(props.modelValue) && props.modelValue.length > 0) {
      fileList.value = props.modelValue.map(url => ({
        name: url.split('/').pop(),
        url: getFileUrl(url),
        status: 'success'
      }))
    } else {
      fileList.value = []
    }
  } else {
    fileList.value = []
  }
}

watch(() => props.modelValue, () => {
  syncFileList()
}, { immediate: true, deep: true })

const handleValidate = (context) => {
  const { file, type } = context
  if (type === 'FILE_OVER_SIZE_LIMIT') {
    MessagePlugin.warning('文件大小不能超过30MB，已自动过滤')
    return false
  }
  if (type === 'FILES_OVER_LENGTH_LIMIT') {
    MessagePlugin.warning(`最多只能上传 ${props.limit} 个文件`)
    return false
  }
  if (type === 'FILTER_FILE_SAME_NAME') {
    MessagePlugin.warning('不能上传同名文件')
    return false
  }
  return true
}

const handleSuccess = (context) => {
  const { response, file } = context
  console.log('上传成功响应:', response)
  if (response && response.code === 200 && response.data) {
    const fileUrl = response.data
    console.log('上传文件路径:', fileUrl)

    if (!props.multiple) {
      fileList.value = [{
        name: file.name,
        url: getFileUrl(fileUrl),
        status: 'success'
      }]
      emit('update:modelValue', fileUrl)
    } else {
      const currentUrls = Array.isArray(props.modelValue) ? [...props.modelValue] : []
      if (!currentUrls.includes(fileUrl)) {
        currentUrls.push(fileUrl)
        fileList.value.push({
          name: file.name,
          url: getFileUrl(fileUrl),
          status: 'success'
        })
        emit('update:modelValue', currentUrls)
      }
    }
    emit('upload-success', fileUrl)
    MessagePlugin.success('上传成功')
  } else {
    MessagePlugin.error(response?.message || '上传失败')
  }
}

const handleError = (context) => {
  console.error('上传失败:', context)
  MessagePlugin.error(context?.response?.message || '上传失败，请重试')
}
</script>

<style scoped>
.file-uploader {
  width: 100%;
}
.upload-tips {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
  margin-top: 4px;
}
</style>
