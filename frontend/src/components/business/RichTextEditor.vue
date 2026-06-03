<template>
  <div class="rich-text-editor">
    <div class="toolbar">
      <t-space>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('bold')">
          <strong>B</strong>
        </t-button>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('italic')">
          <em>I</em>
        </t-button>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('underline')">
          <u>U</u>
        </t-button>
      </t-space>

      <t-divider layout="vertical" />

      <t-space>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('insertUnorderedList')">
          <template #icon><t-icon name="list" /></template>
          无序列表
        </t-button>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('insertOrderedList')">
          <template #icon><t-icon name="order-descending" /></template>
          有序列表
        </t-button>
      </t-space>

      <t-divider layout="vertical" />

      <t-space>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('justifyLeft')">左对齐</t-button>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('justifyCenter')">居中</t-button>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="execCommand('justifyRight')">右对齐</t-button>
      </t-space>

      <t-divider layout="vertical" />

      <t-space>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="insertImage">
          <template #icon><t-icon name="image" /></template>
          插入图片
        </t-button>
        <t-button variant="outline" size="small" @mousedown.prevent @mousedown.stop @click="insertLink">
          <template #icon><t-icon name="link" /></template>
          插入链接
        </t-button>
      </t-space>
    </div>

    <div
      ref="editorRef"
      class="editor-content"
      contenteditable="true"
      @input="handleInput"
      @paste="handlePaste"
    ></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
const editorRef = ref(null)
let isUpdating = ref(false)

const execCommand = (command) => {
  editorRef.value?.focus()
  document.execCommand(command, false, null)
}

const insertImage = () => {
  const url = prompt('请输入图片URL')
  if (url && /^https?:\/\/.+\.(jpg|jpeg|png|gif|webp|svg)(\?.*)?$/i.test(url)) {
    editorRef.value?.focus()
    document.execCommand('insertImage', false, url)
  } else if (url) {
    MessagePlugin.warning('请输入正确的图片URL')
  }
}

const insertLink = () => {
  const url = prompt('请输入链接地址')
  if (url && /^https?:\/\//.test(url)) {
    editorRef.value?.focus()
    document.execCommand('createLink', false, url)
  } else if (url) {
    MessagePlugin.warning('请输入正确的链接地址（以 http:// 或 https:// 开头）')
  }
}

const handleInput = () => {
  if (editorRef.value && !isUpdating.value) {
    emit('update:modelValue', editorRef.value.innerHTML)
  }
}

const handlePaste = (e) => {
  e.preventDefault()
  const text = e.clipboardData.getData('text/plain')
  if (text) {
    editorRef.value?.focus()
    document.execCommand('insertText', false, text)
  }
}

// 监听外部值变化，只有当值确实不同时才更新
watch(() => props.modelValue, (newVal) => {
  if (!editorRef.value) return
  if (!newVal && !editorRef.value.innerHTML) return
  
  // 只有当编辑器没有焦点时才更新
  if (document.activeElement === editorRef.value) return
  
  // 比较内容是否真的不同（去除空白）
  const currentHtml = editorRef.value.innerHTML || ''
  const newHtml = newVal || ''
  if (currentHtml !== newHtml) {
    isUpdating.value = true
    editorRef.value.innerHTML = newHtml
    nextTick(() => {
      isUpdating.value = false
    })
  }
}, { immediate: false })

onMounted(() => {
  nextTick(() => {
    if (editorRef.value) {
      isUpdating.value = true
      editorRef.value.innerHTML = props.modelValue || ''
      nextTick(() => {
        isUpdating.value = false
      })
    }
  })
})
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-default);
}
.toolbar {
  padding: 8px;
  border-bottom: 1px solid var(--td-component-stroke);
  background: var(--td-bg-color-secondarycontainer);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
.editor-content {
  min-height: 300px;
  padding: 12px;
  outline: none;
  line-height: 1.6;
}
.editor-content:empty::before {
  content: '请输入活动介绍...';
  color: #c0c4cc;
  pointer-events: none;
}
.editor-content :deep(img) {
  max-width: 100%;
}
</style>
