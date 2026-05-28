<template>
  <t-layout class="layout-root">
    <t-header class="app-header">
      <div class="header-inner">
        <div class="header-brand">
          <div class="brand-icon">
            <BookOpenIcon />
          </div>
          <span class="brand-title">招生宣传报名系统</span>
        </div>
        <div class="header-actions">
          <t-dropdown trigger="click" @click="handleCommand">
            <div class="user-trigger">
              <t-avatar size="small">{{ userStore.realName?.charAt(0) || 'U' }}</t-avatar>
              <span class="user-name">{{ userStore.realName || '用户' }}</span>
              <ChevronDownIcon class="chevron" />
            </div>
            <t-dropdown-menu>
              <t-dropdown-item value="profile">
                <UserIcon class="dropdown-icon" />个人资料
              </t-dropdown-item>
              <t-dropdown-item value="password">
                <LockOnIcon class="dropdown-icon" />修改密码
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
      <t-aside class="app-aside">
        <t-menu :default-value="$route.path" theme="light" class="side-menu">
          <t-menu-item value="/student/activities">
            <template #icon><BrowseIcon /></template>
            活动列表
          </t-menu-item>
          <t-menu-item value="/student/my-registrations">
            <template #icon><FileIcon /></template>
            我的报名
          </t-menu-item>
          <t-menu-item value="/student/feedbacks">
            <template #icon><ChatIcon /></template>
            我的反馈
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { BookOpenIcon, ChevronDownIcon, UserIcon, LockOnIcon, LogoutIcon, BrowseIcon, FileIcon, ChatIcon } from 'tdesign-icons-vue-next'
import { DialogPlugin } from 'tdesign-vue-next'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (data) => {
  if (data.value === 'profile') {
    router.push('/profile')
  } else if (data.value === 'password') {
    // TODO: 修改密码
  } else if (data.value === 'logout') {
    const dialog = DialogPlugin.confirm({
      header: '确认退出',
      body: '确定要退出登录吗？',
      confirmBtn: '确定退出',
      cancelBtn: '取消',
      onConfirm: () => {
        userStore.logout()
        router.push('/login')
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
  background: var(--td-brand-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}
.brand-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  letter-spacing: 0.5px;
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
