<template>
  <div class="file-uploader">
    <t-upload
      v-if="!crop"
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

    <div v-else class="crop-uploader">
      <input
        ref="cropInputRef"
        type="file"
        class="crop-file-input"
        :accept="accept"
        @change="handleCropFileChange"
      />
      <t-button variant="outline" theme="primary" :loading="cropUploading" @click="openCropFilePicker">
        <template #icon><t-icon name="upload" /></template>
        上传并裁剪图片
      </t-button>
      <div class="upload-tips">{{ tipText }}</div>

      <div v-if="fileList.length" class="crop-file-list">
        <div v-for="file in fileList" :key="file.relativeUrl || file.url" class="crop-file-item">
          <img :src="file.url" :alt="file.name" />
          <span class="crop-file-name">{{ file.name }}</span>
          <t-button size="small" theme="danger" variant="text" @click="removeCropFile(file)">删除</t-button>
        </div>
      </div>
    </div>

    <t-dialog
      v-model:visible="cropVisible"
      header="框选轮播图显示范围"
      width="820px"
      :confirm-btn="{ content: '确认裁剪并上传', loading: cropUploading }"
      cancel-btn="取消"
      @confirm="handleCropConfirm"
      @close="resetCropDialog"
    >
      <div class="crop-dialog-body">
        <div
          ref="cropStageRef"
          class="crop-stage"
          @mousedown="handleStageMouseDown"
        >
          <img
            v-if="cropImageUrl"
            ref="cropImageRef"
            class="crop-stage-image"
            :src="cropImageUrl"
            alt="原图"
            draggable="false"
            @load="handleImageLoaded"
          />
          <div
            v-if="cropBox.w > 0"
            class="crop-box"
            :style="cropBoxStyle"
            @mousedown.stop="handleBoxMouseDown"
          >
            <span class="crop-box-tip">拖动框体或边角调整 (16:9)</span>
            <div
              v-for="handle in handles"
              :key="handle"
              :class="['crop-handle', `crop-handle-${handle}`]"
              @mousedown.stop="handleResizeStart($event, handle)"
            />
          </div>
        </div>
        <div class="crop-help">
          鼠标在原图上拖动可重新框选，拖动选框可移动，拖动四角可按 16:9 缩放，确认后将按选框范围生成轮播图再上传。
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watchEffect } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getToken } from '@/utils/auth'
import { getFileUrl } from '@/utils/file'
import { fileApi } from '@/api/file'

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
  },
  crop: {
    type: Boolean,
    default: false
  },
  cropAspectRatio: {
    type: Number,
    default: 16 / 9
  },
  cropOutputWidth: {
    type: Number,
    default: 1280
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
const cropInputRef = ref(null)
const cropStageRef = ref(null)
const cropImageRef = ref(null)
const cropVisible = ref(false)
const cropUploading = ref(false)
const cropImageUrl = ref('')
const cropSourceFile = ref(null)
// 选框（基于舞台容器的像素坐标），w/h 为 0 表示未初始化
const cropBox = reactive({ x: 0, y: 0, w: 0, h: 0 })
// 舞台中图片的实际渲染区域（letterbox 后），用于约束选框
const stageImageRect = reactive({ left: 0, top: 0, width: 0, height: 0 })
// 原图自然尺寸
const naturalSize = reactive({ width: 0, height: 0 })
const handles = ['nw', 'ne', 'sw', 'se']
let dragContext = null

const cropBoxStyle = computed(() => ({
  left: `${cropBox.x}px`,
  top: `${cropBox.y}px`,
  width: `${cropBox.w}px`,
  height: `${cropBox.h}px`
}))

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

// 监听 modelValue 变化，同步 fileList
watchEffect(() => {
  const newVal = props.modelValue
  console.log('FileUploader modelValue 变化:', newVal)
  
  // 比较新旧值是否相同，避免不必要的更新
  const oldItems = fileList.value
  const newItems = buildItemsFromModel(newVal)
  
  console.log('旧 fileList:', oldItems)
  console.log('新 items:', newItems)
  
  // 检查是否需要更新
  const oldUrls = oldItems.map(item => item.relativeUrl).sort()
  const newUrls = newItems.map(item => item.relativeUrl).sort()
  const isSame = oldUrls.length === newUrls.length && 
                  oldUrls.every((url, idx) => url === newUrls[idx])
  
  if (!isSame) {
    console.log('更新 fileList')
    fileList.value = newItems
  }
})

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

const syncUploadedFile = (fileUrl, fileName = '上传文件') => {
  const fullUrl = getFileUrl(fileUrl)
  const newItem = {
    name: fileName,
    url: fullUrl,
    relativeUrl: fileUrl,
    status: 'success'
  }

  if (!props.multiple) {
    fileList.value = [newItem]
    emit('update:modelValue', fileUrl)
  } else {
    const exists = fileList.value.some(item => item.relativeUrl === fileUrl)
    if (!exists) {
      fileList.value = [...fileList.value, newItem]
    }
    const urls = fileList.value.map(item => item.relativeUrl).filter(Boolean)
    emit('update:modelValue', urls)
  }

  emit('upload-success', fileUrl)
}

const openCropFilePicker = () => {
  if (props.multiple && fileList.value.length >= props.limit) {
    MessagePlugin.warning(`最多只能上传 ${props.limit} 个文件`)
    return
  }
  cropInputRef.value?.click()
}

const handleCropFileChange = (event) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return

  if (!file.type.startsWith('image/')) {
    MessagePlugin.warning('请选择图片文件')
    return
  }

  if (cropImageUrl.value) {
    URL.revokeObjectURL(cropImageUrl.value)
  }

  cropSourceFile.value = file
  cropImageUrl.value = URL.createObjectURL(file)
  cropBox.x = 0
  cropBox.y = 0
  cropBox.w = 0
  cropBox.h = 0
  cropVisible.value = true
}

// 图片加载后初始化选框（默认覆盖最大可放置的 16:9 区域）
const handleImageLoaded = () => {
  const img = cropImageRef.value
  const stage = cropStageRef.value
  if (!img || !stage) return
  naturalSize.width = img.naturalWidth
  naturalSize.height = img.naturalHeight
  computeStageImageRect()
  resetCropBoxToFit()
}

const computeStageImageRect = () => {
  const stage = cropStageRef.value
  if (!stage || !naturalSize.width || !naturalSize.height) return
  const stageW = stage.clientWidth
  const stageH = stage.clientHeight
  const imgRatio = naturalSize.width / naturalSize.height
  const stageRatio = stageW / stageH
  let width, height, left, top
  if (imgRatio > stageRatio) {
    width = stageW
    height = stageW / imgRatio
    left = 0
    top = (stageH - height) / 2
  } else {
    height = stageH
    width = stageH * imgRatio
    top = 0
    left = (stageW - width) / 2
  }
  stageImageRect.left = left
  stageImageRect.top = top
  stageImageRect.width = width
  stageImageRect.height = height
}

const resetCropBoxToFit = () => {
  const ratio = props.cropAspectRatio
  let w = stageImageRect.width
  let h = w / ratio
  if (h > stageImageRect.height) {
    h = stageImageRect.height
    w = h * ratio
  }
  cropBox.w = w
  cropBox.h = h
  cropBox.x = stageImageRect.left + (stageImageRect.width - w) / 2
  cropBox.y = stageImageRect.top + (stageImageRect.height - h) / 2
}

const clampBox = () => {
  if (cropBox.x < stageImageRect.left) cropBox.x = stageImageRect.left
  if (cropBox.y < stageImageRect.top) cropBox.y = stageImageRect.top
  if (cropBox.x + cropBox.w > stageImageRect.left + stageImageRect.width) {
    cropBox.x = stageImageRect.left + stageImageRect.width - cropBox.w
  }
  if (cropBox.y + cropBox.h > stageImageRect.top + stageImageRect.height) {
    cropBox.y = stageImageRect.top + stageImageRect.height - cropBox.h
  }
}

const getRelativePoint = (event) => {
  const rect = cropStageRef.value.getBoundingClientRect()
  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
}

// 在原图空白处按下时开始重新框选
const handleStageMouseDown = (event) => {
  if (!stageImageRect.width) return
  const point = getRelativePoint(event)
  // 限定起点在图片范围内
  if (
    point.x < stageImageRect.left ||
    point.x > stageImageRect.left + stageImageRect.width ||
    point.y < stageImageRect.top ||
    point.y > stageImageRect.top + stageImageRect.height
  ) {
    return
  }
  dragContext = { type: 'create', startX: point.x, startY: point.y }
  cropBox.x = point.x
  cropBox.y = point.y
  cropBox.w = 0
  cropBox.h = 0
  bindWindowEvents()
}

const handleBoxMouseDown = (event) => {
  const point = getRelativePoint(event)
  dragContext = {
    type: 'move',
    offsetX: point.x - cropBox.x,
    offsetY: point.y - cropBox.y
  }
  bindWindowEvents()
}

const handleResizeStart = (event, handle) => {
  dragContext = {
    type: 'resize',
    handle,
    startBox: { ...cropBox }
  }
  bindWindowEvents()
}

const handleWindowMouseMove = (event) => {
  if (!dragContext) return
  const point = getRelativePoint(event)
  const ratio = props.cropAspectRatio
  if (dragContext.type === 'create') {
    let w = Math.abs(point.x - dragContext.startX)
    let h = w / ratio
    const dirX = point.x >= dragContext.startX ? 1 : -1
    const dirY = point.y >= dragContext.startY ? 1 : -1
    cropBox.w = w
    cropBox.h = h
    cropBox.x = dirX > 0 ? dragContext.startX : dragContext.startX - w
    cropBox.y = dirY > 0 ? dragContext.startY : dragContext.startY - h
    clampBox()
  } else if (dragContext.type === 'move') {
    cropBox.x = point.x - dragContext.offsetX
    cropBox.y = point.y - dragContext.offsetY
    clampBox()
  } else if (dragContext.type === 'resize') {
    const start = dragContext.startBox
    const right = start.x + start.w
    const bottom = start.y + start.h
    let newW
    if (dragContext.handle.includes('e')) {
      newW = Math.max(40, point.x - start.x)
    } else {
      newW = Math.max(40, right - point.x)
    }
    let newH = newW / ratio
    const maxW = stageImageRect.width
    const maxH = stageImageRect.height
    if (newW > maxW) { newW = maxW; newH = newW / ratio }
    if (newH > maxH) { newH = maxH; newW = newH * ratio }
    cropBox.w = newW
    cropBox.h = newH
    if (dragContext.handle.includes('w')) cropBox.x = right - newW
    else cropBox.x = start.x
    if (dragContext.handle.includes('n')) cropBox.y = bottom - newH
    else cropBox.y = start.y
    clampBox()
  }
}

const handleWindowMouseUp = () => {
  if (dragContext && dragContext.type === 'create' && cropBox.w < 20) {
    resetCropBoxToFit()
  }
  dragContext = null
  unbindWindowEvents()
}

const bindWindowEvents = () => {
  window.addEventListener('mousemove', handleWindowMouseMove)
  window.addEventListener('mouseup', handleWindowMouseUp)
}

const unbindWindowEvents = () => {
  window.removeEventListener('mousemove', handleWindowMouseMove)
  window.removeEventListener('mouseup', handleWindowMouseUp)
}

const loadImage = (src) => new Promise((resolve, reject) => {
  const image = new Image()
  image.onload = () => resolve(image)
  image.onerror = reject
  image.src = src
})

const getCropRect = (image) => {
  // 如果未初始化选框，则取整图按 16:9 居中
  if (!cropBox.w || !stageImageRect.width) {
    const targetRatio = props.cropAspectRatio
    let sourceWidth = image.naturalWidth
    let sourceHeight = sourceWidth / targetRatio
    if (sourceHeight > image.naturalHeight) {
      sourceHeight = image.naturalHeight
      sourceWidth = sourceHeight * targetRatio
    }
    const sx = (image.naturalWidth - sourceWidth) / 2
    const sy = (image.naturalHeight - sourceHeight) / 2
    return { sx, sy, sourceWidth, sourceHeight }
  }
  // 把选框相对于舞台图片区域的比例换算到原图坐标
  const scaleX = image.naturalWidth / stageImageRect.width
  const scaleY = image.naturalHeight / stageImageRect.height
  const sx = Math.max(0, (cropBox.x - stageImageRect.left) * scaleX)
  const sy = Math.max(0, (cropBox.y - stageImageRect.top) * scaleY)
  const sourceWidth = cropBox.w * scaleX
  const sourceHeight = cropBox.h * scaleY
  return { sx, sy, sourceWidth, sourceHeight }
}

const createCroppedFile = async () => {
  const image = await loadImage(cropImageUrl.value)
  const outputWidth = props.cropOutputWidth
  const outputHeight = Math.round(outputWidth / props.cropAspectRatio)
  const canvas = document.createElement('canvas')
  canvas.width = outputWidth
  canvas.height = outputHeight
  const ctx = canvas.getContext('2d')
  const { sx, sy, sourceWidth, sourceHeight } = getCropRect(image)

  ctx.drawImage(image, sx, sy, sourceWidth, sourceHeight, 0, 0, outputWidth, outputHeight)

  const blob = await new Promise((resolve) => {
    canvas.toBlob(resolve, 'image/jpeg', 0.92)
  })

  const baseName = cropSourceFile.value?.name?.replace(/\.[^.]+$/, '') || 'banner'
  return new File([blob], `${baseName}-banner.jpg`, { type: 'image/jpeg' })
}

const handleCropConfirm = async () => {
  if (!cropSourceFile.value || !cropImageUrl.value) return

  cropUploading.value = true
  try {
    const croppedFile = await createCroppedFile()
    const res = await fileApi.upload(croppedFile)
    if (res?.data) {
      syncUploadedFile(res.data, croppedFile.name)
      MessagePlugin.success('裁剪并上传成功')
      cropVisible.value = false
      resetCropDialog()
    } else {
      MessagePlugin.error('上传失败：响应格式错误')
    }
  } catch (error) {
    console.error('裁剪上传失败:', error)
    MessagePlugin.error('裁剪上传失败，请重试')
  } finally {
    cropUploading.value = false
  }
}

const resetCropDialog = () => {
  if (cropImageUrl.value) {
    URL.revokeObjectURL(cropImageUrl.value)
  }
  cropImageUrl.value = ''
  cropSourceFile.value = null
}

const removeCropFile = (file) => {
  handleRemove({ file })
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
    console.log('处理后的文件信息:', { fileName, fileUrl, fullUrl: getFileUrl(fileUrl) })
    
    // 检查是否已存在
    const currentUrls = getCurrentRelativeUrls()
    console.log('当前列表中的URL:', currentUrls)
    
    if (currentUrls.includes(fileUrl)) {
      console.log('URL已存在，跳过:', fileUrl)
      MessagePlugin.success('上传成功')
      return
    }
    
    syncUploadedFile(fileUrl, fileName)
    
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
.crop-file-input {
  display: none;
}
.crop-uploader {
  width: 100%;
}
.crop-file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}
.crop-file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border: 1px solid var(--td-component-border);
  border-radius: 8px;
  background: var(--td-bg-color-container);
}
.crop-file-item img {
  width: 120px;
  height: 68px;
  object-fit: cover;
  border-radius: 6px;
  background: #f0f2f5;
}
.crop-file-name {
  flex: 1;
  color: var(--td-text-color-secondary);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.crop-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.crop-stage {
  position: relative;
  width: 100%;
  height: 420px;
  overflow: hidden;
  border-radius: 12px;
  background: #111827;
  user-select: none;
  cursor: crosshair;
}
.crop-stage-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  pointer-events: none;
}
.crop-box {
  position: absolute;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.45);
  cursor: move;
  box-sizing: border-box;
}
.crop-box-tip {
  position: absolute;
  left: 8px;
  top: 8px;
  padding: 2px 8px;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 999px;
  pointer-events: none;
}
.crop-handle {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #ffffff;
  border: 1px solid #1f2937;
  border-radius: 2px;
}
.crop-handle-nw { left: -6px; top: -6px; cursor: nwse-resize; }
.crop-handle-ne { right: -6px; top: -6px; cursor: nesw-resize; }
.crop-handle-sw { left: -6px; bottom: -6px; cursor: nesw-resize; }
.crop-handle-se { right: -6px; bottom: -6px; cursor: nwse-resize; }
.crop-help {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}
</style>
