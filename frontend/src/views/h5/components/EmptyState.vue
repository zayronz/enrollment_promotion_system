<template>
  <div class="empty-state">
    <div class="empty-icon" v-html="iconSvg"></div>
    <div class="empty-title">{{ title }}</div>
    <div class="empty-desc">{{ description }}</div>
    <div class="empty-action" v-if="showRefresh" @click="$emit('refresh')">
      <span class="refresh-btn">刷新重试</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: 'empty' },
  title: { type: String, default: '' },
  description: { type: String, default: '' },
  showRefresh: { type: Boolean, default: false }
})

const iconSvg = computed(() => {
  const icons = {
    empty: `<svg width="80" height="80" viewBox="0 0 80 80" fill="none">
      <rect x="20" y="15" width="40" height="50" rx="4" fill="#e5e7eb"/>
      <circle cx="32" cy="32" r="4" fill="#d1d5db"/>
      <path d="M42 30h12M42 36h8" stroke="#d1d5db" stroke-width="2" stroke-linecap="round"/>
      <circle cx="40" cy="55" r="10" fill="#2563eb"/>
      <path d="M36 55l3 3 6-6" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
    search: `<svg width="80" height="80" viewBox="0 0 80 80" fill="none">
      <rect x="15" y="10" width="50" height="55" rx="4" fill="#e5e7eb"/>
      <circle cx="40" cy="35" r="12" stroke="#d1d5db" stroke-width="3"/>
      <line x1="49" y1="44" x2="58" y2="53" stroke="#d1d5db" stroke-width="3" stroke-linecap="round"/>
      <path d="M30 20h20" stroke="#d1d5db" stroke-width="2" stroke-linecap="round"/>
    </svg>`,
    network: `<svg width="80" height="80" viewBox="0 0 80 80" fill="none">
      <path d="M20 50c0-11 9-20 20-20s20 9 20 20" stroke="#d1d5db" stroke-width="3" stroke-linecap="round"/>
      <path d="M28 42c0-6.6 5.4-12 12-12s12 5.4 12 12" stroke="#d1d5db" stroke-width="3" stroke-linecap="round"/>
      <circle cx="40" cy="56" r="4" fill="#ef4444"/>
      <path d="M55 25l5 5M60 25l-5 5" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/>
      <path d="M25 30c0-8 7-15 15-15" stroke="#d1d5db" stroke-width="2" stroke-linecap="round" stroke-dasharray="4 4"/>
    </svg>`
  }
  return icons[props.type] || icons.empty
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  min-height: 50vh;
}
.empty-icon {
  margin-bottom: 16px;
}
.empty-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 8px;
}
.empty-desc {
  font-size: 13px;
  color: #9ca3af;
  text-align: center;
  line-height: 1.5;
}
.empty-action {
  margin-top: 20px;
}
.refresh-btn {
  display: inline-block;
  padding: 8px 24px;
  background: #2563eb;
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}
</style>
