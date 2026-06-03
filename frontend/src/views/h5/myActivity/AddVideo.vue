<template>
  <div class="add-video">
    <H5NavBar title="添加视频" />
    <div class="upload-content">
      <div class="upload-area">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5">
          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
        </svg>
        <div class="upload-text">上传文件</div>
      </div>
      <div class="upload-tip">仅支持一般视频格式，如MP4、AVI、WMV文件大小100M以下。仅一个。</div>
      <div class="file-item" v-if="fileName">
        <div class="file-info">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          <span>{{ fileName }}</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <span class="progress-text">{{ progress }}%</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="delete-icon" @click="fileName = ''"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </div>
    </div>
    <div class="submit-area">
      <button class="submit-btn" @click="handleSubmit">添加</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const fileName = ref('视频文件.MP4')
const progress = ref(38)

const handleSubmit = () => {
  router.back()
}
</script>

<style scoped>
.add-video {
  min-height: 100vh;
  background: #f5f7fa;
}
.upload-content {
  padding: 12px;
}
.upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.upload-text {
  font-size: 14px;
  color: #6b7280;
}
.upload-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 10px;
  line-height: 1.5;
}
.file-item {
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.file-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #374151;
  flex: 1;
}
.progress-bar {
  flex: 1;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 3px;
  transition: width 0.3s;
}
.progress-text {
  font-size: 12px;
  color: #6b7280;
}
.delete-icon {
  cursor: pointer;
}
.submit-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  background: #f5f7fa;
  padding: 12px 16px 20px;
  z-index: 50;
}
.submit-btn {
  width: 100%;
  height: 46px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}
</style>
