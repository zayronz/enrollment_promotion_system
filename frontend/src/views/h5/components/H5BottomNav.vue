<template>
  <div class="h5-bottom-nav">
    <div
      v-for="item in navItems"
      :key="item.path"
      class="nav-item"
      :class="{ active: currentPath === item.path }"
      @click="navigate(item.path)"
    >
      <div class="nav-icon" v-html="item.icon"></div>
      <div class="nav-label">{{ item.label }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const currentPath = computed(() => {
  const p = route.path
  if (p.startsWith('/h5/home') || p.startsWith('/h5/activity')) return '/h5/home'
  if (p.startsWith('/h5/more') || p.startsWith('/h5/test') || p.startsWith('/h5/my-activity') || p.startsWith('/h5/approval') || p.startsWith('/h5/team') || p.startsWith('/h5/materials')) return '/h5/more'
  return p
})

const navItems = [
  {
    path: '/h5/home',
    label: '活动广场',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`
  },
  {
    path: '/h5/more',
    label: '更多功能',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>`
  }
]

const navigate = (path) => {
  if (route.path !== path) router.push(path)
}
</script>

<style scoped>
.h5-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 56px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  color: #9ca3af;
}
.nav-item.active {
  color: #2563eb;
}
.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}
.nav-label {
  font-size: 11px;
}
</style>
