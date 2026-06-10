<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

onMounted(() => {
  userStore.checkLogin()
})
</script>

<style>
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Microsoft YaHei', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--td-bg-color-page);
  color: var(--td-text-color-primary);
}

/* Override TDesign brand palette for a more refined Swiss look */
:root {
  --td-brand-color-7: #1a56db;
  --td-brand-color-6: #3b82f6;
  --td-brand-color-5: #60a5fa;
  --td-brand-color-8: #1e40af;
  --td-radius-default: 8px;
  --td-radius-medium: 10px;
  --td-radius-large: 12px;
}

/* Smooth scrollbar */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
}
::-webkit-scrollbar-track {
  background: transparent;
}

/* Page transition */
.router-enter-active,
.router-leave-active {
  transition: opacity 0.2s ease;
}
.router-enter-from,
.router-leave-to {
  opacity: 0;
}

/* 富文本内容中的图片约束：防止溢出和拉伸 */
.description-body img,
.desc-content img,
.content-full img,
.content-text img {
  max-width: 100%;
  height: auto;
  object-fit: contain;
}
</style>
