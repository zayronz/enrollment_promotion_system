<template>
  <div class="file-uploader">
    <t-upload
      :files="fileList"
      name="file"
      :action="uploadUrl"
      :headers="headers"
      :multiple="multiple"
      :max="limit"
      :accept="accept"
      :theme="theme"
      :allow-upload-duplicate-file="true"
      @success="handleSuccess"
      @fail="handleError"
      @validate="handleValidate"
      @remove="handleRemove"
      @change="handleChange"
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

// 从 modelValue 构建文件列表项
const buildItemsFromModel = (value) => {
  if (!value) return []
  
  if (typeof value === 'string') {
    const fileName = value.split('/').pop()
    const fullUrl = getFileUrl(value)
    return [{
      name: fileName,
      url: fullUrl,
      relativeUrl: value,
      status: 'success'
    }]
  }
  
  if (Array.isArray(value)) {
    return value.map(url => {
      const fileName = url.split('/').pop()
      const fullUrl = getFileUrl(url)
      return {
        name: fileName,
        url: fullUrl,
        relativeUrl: url,
        status: 'success'
      }
    })
  }
  
  return []
}

// 只在初始化时同步一次，避免覆盖用户操作
let isInitialized = false
watch(() => props.modelValue, (newVal) => {
  if (!isInitialized) {
    console.log('初始化 modelValue，同步 fileList')
    fileList.value = buildItemsFromModel(newVal)
    isInitialized = true
  }
}, { immediate: true, deep: true })

// 获取当前文件列表中的相对URL列表
const getCurrentRelativeUrls = () => {
  return fileList.value
    .filter(item => item && item.status === 'success' && item.relativeUrl)
    .map(item => item.relativeUrl)
}

// 文件列表变化处理 - 由组件内部的 v-model 控制
const handleChange = (files) => {
  console.log('handleChange:', files)
}

// 删除文件处理
const handleRemove = (context) => {
  console.log('handleRemove 删除文件:', context)
  const { file } = context
  
  // 获取删除的相对URL
  const removedRelativeUrl = file?.relativeUrl || file?.url || ''
  console.log('  删除的相对URL:', removedRelativeUrl)
  
  // 从当前列表中移除删除的项
  const newItems = fileList.value.filter(item => {
    return item.relativeUrl !== removedRelativeUrl && item.url !== removedRelativeUrl
  })
  
  fileList.value = newItems
  console.log('  更新后的 fileList:', fileList.value)
  
  // 同步到父组件
  if (!props.multiple) {
    emit('update:modelValue', '')
  } else {
    const urls = newItems.map(item => item.relativeUrl).filter(Boolean)
    emit('update:modelValue', urls)
    console.log('  发出的 modelValue:', urls)
  }
}

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
  
  // 允许同名文件，直接放行
  if (type === 'FILTER_FILE_SAME_NAME') {
    console.log('检测到同名文件，允许上传')
    return true
  }
  
  return true
}

const handleSuccess = (context) => {
  const { response, file } = context
  console.log('上传成功，原始响应:', response)
  console.log('上传成功，file 对象:', file)
  
  // TDesign 的 response 可能已经是解析后的对象或字符串
  let responseData = response
  
  // 处理响应可能是数组的情况
  if (Array.isArray(response)) {
    console.log('响应是数组，尝试提取第一个元素')
    if (response.length > 0) {
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
        responseData = firstItem
      }
    }
  } else if (typeof response === 'string') {
    try {
      const parsed = JSON.parse(response)
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
    
    if (typeof fileUrl !== 'string') {
      console.error('fileUrl 不是字符串:', fileUrl)
      MessagePlugin.error('上传失败：文件路径格式错误')
      return
    }
    
    const fileName = file?.name || fileUrl.split('/').pop() || '上传文件'
    const fullUrl = getFileUrl(fileUrl)
    
    console.log('处理后的文件信息:', { fileName, fileUrl, fullUrl })
    
    // 检查是否已存在
    const currentUrls = getCurrentRelativeUrls()
    console.log('当前列表中的URL:', currentUrls)
    
    if (currentUrls.includes(fileUrl)) {
      console.log('URL已存在，跳过:', fileUrl)
      MessagePlugin.success('上传成功')
      return
    }
    
    // 构建新的文件项
    const newItem = {
      name: fileName,
      url: fullUrl,
      relativeUrl: fileUrl,
      status: 'success'
    }
    
    console.log('新文件项:', newItem)
    
    if (!props.multiple) {
      // 单文件模式 - 替换整个列表
      fileList.value = [newItem]
      emit('update:modelValue', fileUrl)
      console.log('单文件模式: 发出 update:modelValue', fileUrl)
    } else {
      // 多文件模式 - 追加到列表
      fileList.value = [...fileList.value, newItem]
      const urls = fileList.value.map(item => item.relativeUrl).filter(Boolean)
      emit('update:modelValue', urls)
      console.log('多文件模式: 发出 update:modelValue', urls)
    }
    
    console.log('更新后的 fileList:', fileList.value)
    
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
