<template>
  <t-layout class="layout-root">
    <t-header class="app-header admin-header">
      <div class="header-inner">
        <div class="header-brand">
          <div class="brand-icon admin">
            <DashboardIcon />
          </div>
          <span class="brand-title admin-title">招生宣传报名系统 · 管理端</span>
        </div>
        <div class="header-actions">
          <t-dropdown trigger="click" @click="handleCommand">
            <div class="user-trigger">
              <t-avatar size="small" :image="userStore.avatarUrl || undefined">{{ userStore.realName?.charAt(0) || 'A' }}</t-avatar>
              <span class="user-name">{{ userStore.realName || '管理员' }}</span>
              <ChevronDownIcon class="chevron" />
            </div>
            <t-dropdown-menu>
              <t-dropdown-item value="profile">
                <UserIcon class="dropdown-icon" />个人资料
              </t-dropdown-item>
              <t-dropdown-item divider value="logout">
                <LogoutIcon class="dropdown-icon" />退出登录
              </t-dropdown-item>
            </t-dropdown-menu>
          </t-dropdown>
        </div>
      </div>
    </t-header>
    <t-layout class="layout-body">
      <t-aside class="app-aside admin-aside">
        <t-menu
          v-model:value="activeMenu"
          :default-expanded="['activity', 'audit', 'user', 'feedback']"
          class="side-menu admin-menu"
          @change="handleMenuChange"
        >
          <t-menu-item value="/school/dashboard">
            <template #icon><DashboardIcon /></template>
            数据仪表盘
          </t-menu-item>
          <t-submenu value="activity" title="活动管理">
            <template #icon><BrowseIcon /></template>
            <t-menu-item value="/school/activity/list">
              <template #icon><ViewListIcon /></template>
              活动列表
            </t-menu-item>
            <t-menu-item value="/school/activity/create">
              <template #icon><AddIcon /></template>
              创建活动
            </t-menu-item>
          </t-submenu>
          <t-submenu value="audit" title="审核管理">
            <template #icon><CheckCircleIcon /></template>
            <t-menu-item value="/school/audit/pending">
              <template #icon><PendingIcon /></template>
              待审核
            </t-menu-item>
          </t-submenu>
          <t-submenu value="user" title="用户管理">
            <template #icon><UsergroupIcon /></template>
            <t-menu-item value="/school/user/list">
              <template #icon><UserListIcon /></template>
              用户列表
            </t-menu-item>
          </t-submenu>
          <t-submenu value="feedback" title="反馈管理">
            <template #icon><ChatIcon /></template>
            <t-menu-item value="/school/feedback/list">
              <template #icon><FileIcon /></template>
              全部反馈
            </t-menu-item>
          </t-submenu>
          <t-menu-item value="/school/materials">
            <template #icon><FileIcon /></template>
            招宣资料
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
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import {
  DashboardIcon, ChevronDownIcon, UserIcon, LogoutIcon,
  BrowseIcon, ViewListIcon, AddIcon, CheckCircleIcon,
  PendingIcon, UsergroupIcon, UserListIcon, ChatIcon, FileIcon
} from 'tdesign-icons-vue-next'
import { DialogPlugin } from 'tdesign-vue-next'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = ref(route.path)

// 监听路由变化，同步菜单高亮
watch(() => route.path, (path) => {
  activeMenu.value = path
})

const handleMenuChange = (value) => {
  if (value && value !== route.path) {
    router.push(value)
  }
}

const handleCommand = (data) => {
  if (data.value === 'profile') {
    router.push('/profile')
  } else if (data.value === 'logout') {
    const dialog = DialogPlugin.confirm({
      header: '确认退出',
      body: '确定要退出登录吗？',
      confirmBtn: '确定退出',
      cancelBtn: '取消',
      onConfirm: () => {
        userStore.logout()
        // H5 页面退出后跳转到 H5 登录页
        const isH5 = window.location.pathname.startsWith('/h5')
        router.push(isH5 ? '/h5/login' : '/login')
        dialog.destroy()
      }
    })
  }
}
</script>

<style scoped>
.layout-root {
  height: 100vh;
  background: var(--td-bg-color-page);
}
.app-header.admin-header {
  background: #fff;
  border-bottom: 1px solid var(--td-border-level-1-color);
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
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}
.brand-icon.admin {
  background: var(--td-brand-color);
}
.brand-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  letter-spacing: 0.5px;
}
.brand-title.admin-title {
  color: var(--td-text-color-primary);
}
.header-actions {
  display: flex;
  align-items: center;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-trigger:hover {
  background: var(--td-bg-color-secondarycontainer);
}
.user-name {
  font-size: 14px;
  color: var(--td-text-color-primary);
  font-weight: 500;
}
.chevron {
  font-size: 14px;
  color: var(--td-text-color-placeholder);
}
.dropdown-icon {
  margin-right: 8px;
  font-size: 16px;
}
.layout-body {
  margin-top: 56px;
  height: calc(100vh - 56px);
}
.app-aside {
  width: 230px;
  border-right: 1px solid var(--td-border-level-1-color);
  overflow-y: auto;
}
.admin-aside {
  background: #fff;
}
.side-menu {
  padding: 8px 0;
  border: none !important;
}
.app-content {
  padding: 24px;
  overflow-y: auto;
  background: var(--td-bg-color-page);
}
.content-wrapper {
  max-width: 1300px;
  margin: 0 auto;
}
</style>
