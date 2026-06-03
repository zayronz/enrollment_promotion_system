<template>
  <div class="materials">
    <H5NavBar title="招宣资料" />

    <div class="search-box">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
      </svg>
      <input v-model="searchKey" placeholder="请输入活动名称模糊查询" />
    </div>

    <div class="file-list">
      <div v-for="(file, idx) in fileList" :key="idx" class="file-item">
        <div class="file-icon" :style="{ background: file.bg }">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="white" v-html="file.icon"></svg>
        </div>
        <div class="file-info">
          <div class="file-name">{{ file.name }}</div>
          <div class="file-time">{{ file.time }}</div>
        </div>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="download-icon">
          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import H5NavBar from '../components/H5NavBar.vue'

const searchKey = ref('')

const fileList = [
  { name: '全员共享文件夹', time: '2023/4/23 10:11更新', bg: '#fbbf24', icon: '<path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>' },
  { name: '公文文件.docx', time: '2023/4/23 10:11更新', bg: '#3b82f6', icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>' },
  { name: '公文文件.PPT', time: '2023/4/23 10:11更新', bg: '#f97316', icon: '<rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>' },
  { name: '公文文件.excel', time: '2023/4/23 10:11更新', bg: '#10b981', icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/>' },
  { name: '公文文件.PNG', time: '2023/4/23 10:11更新', bg: '#8b5cf6', icon: '<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>' },
  { name: '公文文件.AVI', time: '2023/4/23 10:11更新', bg: '#ec4899', icon: '<rect x="2" y="2" width="20" height="20" rx="2.18"/><polygon points="10 8 16 12 10 16 10 8"/>' },
  { name: '公文文件.PDF', time: '2023/4/23 10:11更新', bg: '#ef4444', icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>' },
  { name: '公文文件.ZIP', time: '2023/4/23 10:11更新', bg: '#6366f1', icon: '<path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/>' }
]
</script>

<style scoped>
.materials {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}
.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20px;
  margin: 12px;
  padding: 10px 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.search-box input {
  flex: 1;
  border: none;
  outline: none;
  margin-left: 8px;
  font-size: 14px;
  color: #374151;
  background: transparent;
}
.search-box input::placeholder {
  color: #9ca3af;
}
.file-list {
  padding: 0 12px;
}
.file-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.file-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}
.file-info {
  flex: 1;
}
.file-name {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}
.file-time {
  font-size: 12px;
  color: #9ca3af;
}
.download-icon {
  flex-shrink: 0;
  cursor: pointer;
}
</style>
