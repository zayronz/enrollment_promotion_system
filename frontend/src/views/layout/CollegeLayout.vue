<template>
  <t-layout class="layout-root">
    <t-header class="app-header">
      <div class="header-inner">
        <div class="header-brand">
          <div class="brand-icon college">
            <BuildingIcon />
          </div>
          <span class="brand-title">院校端管理系统</span>
        </div>
        <t-button variant="text" theme="default" @click="handleLogout">
          <template #icon><LogoutIcon /></template>
          退出登录
        </t-button>
      </div>
    </t-header>
    <t-layout class="layout-body">
      <t-aside class="app-aside">
        <t-menu v-model:value="activeMenu" theme="light" class="side-menu" @change="handleMenuChange">
          <t-menu-item value="/college/pending">
            <template #icon><CheckCircleIcon /></template>
            报名审核
          </t-menu-item>
          <t-menu-item value="/college/history">
            <template #icon><TimeIcon /></template>
            审核历史
          </t-menu-item>
          <t-menu-item value="/college/feedback">
            <template #icon><ChatIcon /></template>
            反馈管理
          </t-menu-item>
          <t-menu-item value="/profile">
            <template #icon><UserIcon /></template>
            个人中心
          </t-menu-item>
        </t-menu>
      </t-aside>
      <t-content class="app-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </t-content>
    </t-layout>
  </t-layout>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { BuildingIcon, LogoutIcon, CheckCircleIcon, TimeIcon, ChatIcon, UserIcon } from 'tdesign-icons-vue-next'

const router = useRouter()
const route = useRoute()

const activeMenu = ref(route.path)

watch(() => route.path, (path) => {
  activeMenu.value = path
})

const handleMenuChange = (value) => {
  if (value && value !== route.path) {
    router.push(value)
  }
}

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout-root {
  height: 100vh;
  background: var(--td-bg-color-page);
}
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--td-border-level-1-color);
  padding: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.header-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-icon {
  width: 34px;
  height: 34px;
  background: var(--td-success-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}
.brand-icon.college {
  background: #10b981;
}
.brand-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  letter-spacing: 0.5px;
}
.layout-body {
  margin-top: 56px;
  height: calc(100vh - 56px);
}
.app-aside {
  width: 220px;
  background: #fff;
  border-right: 1px solid var(--td-border-level-1-color);
  overflow-y: auto;
}
.side-menu {
  padding: 8px 0;
  border: none !important;
  background: transparent;
}
.app-content {
  padding: 24px;
  overflow-y: auto;
  background: var(--td-bg-color-page);
}
.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
