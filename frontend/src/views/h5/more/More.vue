<template>
  <div class="h5-more">
    <div class="more-header">更多功能</div>

    <div class="menu-list">
      <div class="menu-item" @click="router.push('/h5/test')">
        <div class="menu-icon" style="background: #dbeafe;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        </div>
        <div class="menu-info">
          <span class="menu-name">招生能力测试</span>
          <span class="menu-badge">有更新</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>

      <div class="menu-item" @click="router.push('/h5/my-activity')">
        <div class="menu-icon" style="background: #fce7f3;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ec4899" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
        </div>
        <div class="menu-info">
          <span class="menu-name">我的活动</span>
          <span class="menu-badge">有更新</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>

      <div class="menu-item" @click="router.push('/h5/team')">
        <div class="menu-icon" style="background: #e0e7ff;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#6366f1" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
        </div>
        <div class="menu-info">
          <span class="menu-name">我的组员</span>
          <span class="menu-badge">有更新</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>

      <div class="menu-item" @click="router.push('/h5/approval')">
        <div class="menu-icon" style="background: #fef3c7;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        </div>
        <div class="menu-info">
          <span class="menu-name">报名审批</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>

      <div class="menu-item" @click="router.push('/h5/materials')">
        <div class="menu-icon" style="background: #d1fae5;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        </div>
        <div class="menu-info">
          <span class="menu-name">招宣资料</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
    </div>

    <div class="more-tip">移动端仅显示最新动态，更多内容查看PC端</div>
    <H5BottomNav />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import H5BottomNav from '../components/H5BottomNav.vue'

const router = useRouter()
</script>

<style scoped>
.h5-more {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 70px;
}
.more-header {
  text-align: center;
  padding: 14px;
  font-size: 17px;
  font-weight: 500;
  color: #1f2937;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.menu-list {
  padding: 12px;
}
.menu-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.menu-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}
.menu-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}
.menu-name {
  font-size: 15px;
  color: #1f2937;
  font-weight: 500;
}
.menu-badge {
  font-size: 11px;
  color: #ef4444;
  background: #fef2f2;
  padding: 2px 6px;
  border-radius: 8px;
}
.menu-badge::before {
  content: '';
  display: inline-block;
  width: 5px;
  height: 5px;
  background: #ef4444;
  border-radius: 50%;
  margin-right: 3px;
  vertical-align: middle;
}
.more-tip {
  text-align: center;
  font-size: 12px;
  color: #d1d5db;
  padding: 20px;
}
</style>
