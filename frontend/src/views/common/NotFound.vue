<template>
  <div class="not-found">
    <div class="nf-content">
      <div class="nf-code">404</div>
      <h1 class="nf-title">页面未找到</h1>
      <p class="nf-desc">您访问的页面不存在或已被移除</p>
      <t-button theme="primary" size="large" @click="$router.push('/')">
        <template #icon><HomeIcon /></template>
        返回首页
      </t-button>
    </div>
  </div>
</template>

<script setup>
import { HomeIcon } from 'tdesign-icons-vue-next'
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fc;
}
.nf-content {
  text-align: center;
}
.nf-code {
  font-size: 120px;
  font-weight: 800;
  color: #e5e7eb;
  line-height: 1;
  letter-spacing: -4px;
}
.nf-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 8px 0 12px;
}
.nf-desc {
  font-size: 15px;
  color: var(--td-text-color-secondary);
  margin-bottom: 32px;
}
</style>
