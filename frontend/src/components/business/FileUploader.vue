<template>
  <div class="file-uploader">
    <t-upload
      v-model:value="fileList"
      :multiple="multiple"
      :max="limit"
      :accept="accept"
      :theme="listType"
      :auto-upload="false"
      @change="handleChange"
      @remove="handleRemove"
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
import { ref, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import request from '@/utils/request'
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

const uploadFile = async (file) => {
  console.log('开始上传文件:', file)
  
  const formData = new FormData()
  formData.append('file', file.raw)
  
  try {
    // 使用我们的 request 实例
    const response = await request({
      url: '/file/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    console.log('上传成功响应:', response)
    
    if (response.code === 200 && response.data) {
      return response.data
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    throw error
  }
}

const handleChange = async ({ files, currentFile }) => {
  console.log('handleChange 事件:', { files, currentFile })
  
  if (!currentFile) return
  
  if (currentFile.status === 'waiting') {
    try {
      // 更新状态为上传中
      currentFile.status = 'progress'
      
      const fileUrl = await uploadFile(currentFile)
      console.log('上传成功，获取到文件路径:', fileUrl)
      
      currentFile.status = 'success'
      currentFile.url = getFileUrl(fileUrl)
      currentFile.urlPath = fileUrl
      
      // 更新 modelValue
      if (!props.multiple) {
        emit('update:modelValue', fileUrl)
      } else {
        const currentUrls = Array.isArray(props.modelValue) ? [...props.modelValue] : []
        if (!currentUrls.includes(fileUrl)) {
          currentUrls.push(fileUrl)
          emit('update:modelValue', currentUrls)
        }
      }
      
      emit('upload-success', fileUrl)
      MessagePlugin.success('上传成功')
    } catch (error) {
      currentFile.status = 'fail'
      const errorMsg = error.response?.data?.message || error.message || '上传失败'
      MessagePlugin.error(errorMsg)
    }
  }
}

const handleRemove = ({ file, index }) => {
  console.log('移除文件:', file, index)
  
  if (!props.multiple) {
    emit('update:modelValue', '')
  } else {
    const currentUrls = Array.isArray(props.modelValue) ? [...props.modelValue] : []
    // 这里需要根据文件名或路径来删除，简化处理：重新同步
    setTimeout(() => {
      syncFileList()
    }, 0)
  }
}

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
