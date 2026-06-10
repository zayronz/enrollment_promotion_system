<template>
  <div class="file-uploader">
    <t-upload
      v-model="fileList"
      name="file"
      :action="uploadUrl"
      :headers="headers"
      :multiple="multiple"
      :max="limit"
      :accept="accept"
      :theme="theme"
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

// 根据 accept 属性自动设置 theme
const theme = computed(() => {
  if (props.accept && props.accept.includes('image')) {
    return 'image'
  }
  return props.listType
})

const fileList = ref([])

const syncFileList = () => {
  console.log('syncFileList 被调用，modelValue:', props.modelValue)
  
  if (props.modelValue) {
    if (typeof props.modelValue === 'string' && props.modelValue) {
      const fileName = props.modelValue.split('/').pop()
      const fullUrl = getFileUrl(props.modelValue)
      console.log('单文件模式:', { fileName, fullUrl })
      fileList.value = [{
        name: fileName,
        url: fullUrl,
        status: 'success'
      }]
    } else if (Array.isArray(props.modelValue) && props.modelValue.length > 0) {
      console.log('多文件模式:', props.modelValue)
      fileList.value = props.modelValue.map(url => {
        const fileName = url.split('/').pop()
        const fullUrl = getFileUrl(url)
        console.log('  文件:', { fileName, fullUrl })
        return {
          name: fileName,
          url: fullUrl,
          status: 'success'
        }
      })
    } else {
      console.log('清空文件列表')
      fileList.value = []
    }
  } else {
    console.log('modelValue 为空，清空文件列表')
    fileList.value = []
  }
}

watch(() => props.modelValue, () => {
  console.log('modelValue 变化，触发 syncFileList')
  syncFileList()
}, { immediate: true, deep: true })

const handleValidate = (context) => {
  console.log('文件验证:', context)
  const { type } = context
  if (type === 'FILE_OVER_SIZE_LIMIT') {
    MessagePlugin.warning('文件大小不能超过30MB，已自动过滤')
    return false
  }
  if (type === 'FILES_OVER_LENGTH_LIMIT') {
    MessagePlugin.warning(`最多只能上传 ${props.limit} 个文件`)
    return false
  }
  // 允许同名文件覆盖，直接放行
  if (type === 'FILTER_FILE_SAME_NAME') {
    return true
  }
  return true
}

const handleSuccess = (context) => {
  const { response, file } = context
  console.log('上传成功，原始响应:', response)
  
  // TDesign 的 response 可能已经是解析后的对象或字符串
  let responseData = response
  
  // 处理响应可能是数组的情况
  if (Array.isArray(response)) {
    console.log('响应是数组，尝试提取第一个元素')
    if (response.length > 0) {
      // 检查第一个元素的结构
      const firstItem = response[0]
      if (firstItem && typeof firstItem === 'object' && 'code' in firstItem) {
        responseData = firstItem
      } else if (typeof firstItem === 'string') {
        try {
          responseData = JSON.parse(firstItem)
        } catch (e) {
          console.error('解析响应失败:', e)
          MessagePlugin.error('上传失败：响应格式错误')
          return
        }
      } else {
        // 如果数组第一个元素不是预期的格式，尝试其他方式
        responseData = firstItem
      }
    }
  } else if (typeof response === 'string') {
    try {
      const parsed = JSON.parse(response)
      // 如果解析后是数组，取第一个元素
      if (Array.isArray(parsed)) {
        responseData = parsed[0] || parsed
      } else {
        responseData = parsed
      }
    } catch (e) {
      console.error('解析响应失败:', e)
      MessagePlugin.error('上传失败：响应格式错误')
      return
    }
  }
  
  console.log('处理后的响应数据:', responseData)
  
  // 检查响应格式
  if (responseData && (responseData.code === 200 || responseData.code === 0) && responseData.data) {
    const fileUrl = responseData.data
    console.log('上传文件路径:', fileUrl)
    
    // 确保 fileUrl 是字符串
    if (typeof fileUrl !== 'string') {
      console.error('fileUrl 不是字符串:', fileUrl)
      MessagePlugin.error('上传失败：文件路径格式错误')
      return
    }
    
    const fileName = file?.name || fileUrl.split('/').pop() || '上传文件'
    const fullUrl = getFileUrl(fileUrl)
    
    console.log('处理后的文件信息:', { fileName, fileUrl, fullUrl })
    
    if (!props.multiple) {
      // 单文件模式
      fileList.value = [{
        name: fileName,
        url: fullUrl,
        status: 'success'
      }]
      emit('update:modelValue', fileUrl)
      console.log('单文件模式: emit update:modelValue', fileUrl)
    } else {
      // 多文件模式
      const currentUrls = Array.isArray(props.modelValue) ? [...props.modelValue] : []
      
      if (!currentUrls.includes(fileUrl)) {
        currentUrls.push(fileUrl)
        fileList.value.push({
          name: fileName,
          url: fullUrl,
          status: 'success'
        })
        emit('update:modelValue', currentUrls)
        console.log('多文件模式: emit update:modelValue', currentUrls)
      } else {
        console.log('文件已存在，跳过:', fileUrl)
      }
    }
    emit('upload-success', fileUrl)
    MessagePlugin.success('上传成功')
  } else {
    console.error('上传失败，响应不符合预期:', responseData)
    MessagePlugin.error(responseData?.message || '上传失败')
  }
}

const handleError = (context) => {
  console.error('上传失败:', context)
  
  let errorMsg = '上传失败，请重试'
  
  if (context?.response) {
    const response = context.response
    if (typeof response === 'string') {
      try {
        const parsed = JSON.parse(response)
        errorMsg = parsed?.message || errorMsg
      } catch (e) {
        errorMsg = response || errorMsg
      }
    } else if (response?.message) {
      errorMsg = response.message
    } else if (response?.status === 500) {
      errorMsg = '服务器内部错误，请稍后重试'
    } else if (response?.status === 401) {
      errorMsg = '登录已过期，请重新登录'
    } else if (response?.status === 403) {
      errorMsg = '没有上传权限'
    }
  }
  
  MessagePlugin.error(errorMsg)
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
