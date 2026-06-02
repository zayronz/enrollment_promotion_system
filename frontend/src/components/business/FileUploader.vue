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
      <t-button theme="primary">
        <template #icon><t-icon name="upload" /></template>
        点击上传
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
import { ref, computed } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getToken } from '@/utils/auth'

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
    default: '支持图片、文档格式，单个文件不超过10MB'
  }
})

const emit = defineEmits(['update:modelValue', 'upload-success'])

const uploadUrl = '/api/file/upload'
const headers = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const fileList = ref([])

// 初始化时同步已有的文件
if (props.modelValue) {
  if (typeof props.modelValue === 'string') {
    fileList.value = [{ url: props.modelValue, name: props.modelValue.split('/').pop() }]
  } else if (Array.isArray(props.modelValue)) {
    fileList.value = props.modelValue.map(url => ({ url, name: url.split('/').pop() }))
  }
}

const handleValidate = (context) => {
  const { file, type } = context
  if (type === 'FILE_OVER_SIZE_LIMIT') {
    MessagePlugin.error('文件大小不能超过10MB，已自动过滤')
    return false
  }
  if (type === 'FILES_OVER_LENGTH_LIMIT') {
    MessagePlugin.error(`最多只能上传 ${props.limit} 个文件`)
    return false
  }
  if (type === 'FILTER_FILE_SAME_NAME') {
    MessagePlugin.error('不能上传同名文件')
    return false
  }
  return true
}

const handleSuccess = (context) => {
  const { response, file } = context
  if (response && response.code === 200 && response.data) {
    const fileUrl = response.data
    if (!props.multiple) {
      emit('update:modelValue', fileUrl)
    } else {
      const currentUrls = Array.isArray(props.modelValue) ? [...props.modelValue] : []
      currentUrls.push(fileUrl)
      emit('update:modelValue', currentUrls)
    }
    emit('upload-success', fileUrl)
    MessagePlugin.success('上传成功')
  } else {
    MessagePlugin.error('上传失败')
  }
}

const handleError = () => {
  MessagePlugin.error('上传失败，请重试')
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
