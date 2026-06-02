<template>
  <div class="rich-text-editor">
    <div class="toolbar">
      <t-space>
        <t-button variant="outline" size="small" @click="execCommand('bold')">
          <strong>B</strong>
        </t-button>
        <t-button variant="outline" size="small" @click="execCommand('italic')">
          <em>I</em>
        </t-button>
        <t-button variant="outline" size="small" @click="execCommand('underline')">
          <u>U</u>
        </t-button>
      </t-space>

      <t-divider layout="vertical" />

      <t-space>
        <t-button variant="outline" size="small" @click="execCommand('insertUnorderedList')">
          <template #icon><t-icon name="list" /></template>
          无序列表
        </t-button>
        <t-button variant="outline" size="small" @click="execCommand('insertOrderedList')">
          <template #icon><t-icon name="order-descending" /></template>
          有序列表
        </t-button>
      </t-space>

      <t-divider layout="vertical" />

      <t-space>
        <t-button variant="outline" size="small" @click="execCommand('justifyLeft')">左对齐</t-button>
        <t-button variant="outline" size="small" @click="execCommand('justifyCenter')">居中</t-button>
        <t-button variant="outline" size="small" @click="execCommand('justifyRight')">右对齐</t-button>
      </t-space>

      <t-divider layout="vertical" />

      <t-space>
        <t-button variant="outline" size="small" @click="insertImage">
          <template #icon><t-icon name="image" /></template>
          插入图片
        </t-button>
        <t-button variant="outline" size="small" @click="insertLink">
          <template #icon><t-icon name="link" /></template>
          插入链接
        </t-button>
      </t-space>
    </div>

    <div
      ref="editorRef"
      class="editor-content"
      contenteditable="true"
      @input="onInput"
      @paste="onPaste"
      v-html="modelValue"
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
const isInternalUpdate = ref(false)

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
    const selection = window.getSelection()
    const selectedText = selection ? selection.toString().trim() : ''
    editorRef.value?.focus()
    if (selectedText) {
      document.execCommand('createLink', false, url)
    } else {
      document.execCommand('insertHTML', false, `<a href="${url}" target="_blank">${url}</a>`)
    }
  } else if (url) {
    MessagePlugin.warning('请输入正确的链接地址（以 http:// 或 https:// 开头）')
  }
}

const onInput = () => {
  if (editorRef.value) {
    const html = editorRef.value.innerHTML
    // 清理空白内容（如空 <br> 标签）
    const cleaned = html.replace(/^<br\s*\/?>/i, '')
    if (cleaned !== html) {
      isInternalUpdate.value = true
      editorRef.value.innerHTML = cleaned
      isInternalUpdate.value = false
    }
    emit('update:modelValue', editorRef.value.innerHTML)
  }
}

const onPaste = (e) => {
  e.preventDefault()
  const text = e.clipboardData.getData('text/plain')
  if (text) {
    document.execCommand('insertText', false, text)
  }
}

watch(() => props.modelValue, (newVal) => {
  if (!editorRef.value) return
  if (isInternalUpdate.value) return
  if (newVal !== editorRef.value.innerHTML) {
    editorRef.value.innerHTML = newVal || ''
  }
})

onMounted(() => {
  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.innerHTML = props.modelValue || ''
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
