<template>
  <div class="add-photo">
    <H5NavBar title="添加照片" />
    <div class="upload-content">
      <div class="upload-area">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5">
          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
        </svg>
        <div class="upload-text">上传文件</div>
      </div>
      <div class="upload-tip">仅支持一般图片格式，如JPG、PNG。文件大小10M以下。5张以下。</div>
      <div class="photo-grid">
        <div v-for="(photo, idx) in photos" :key="idx" class="photo-item">
          <img :src="photo" />
          <div class="photo-delete" @click="removePhoto(idx)">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="white" stroke="white" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </div>
        </div>
        <div class="photo-item add-btn">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
        </div>
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
const photos = ref([
  'https://picsum.photos/200/200?random=1',
  'https://picsum.photos/200/200?random=2',
  'https://picsum.photos/200/200?random=3'
])

const removePhoto = (idx) => {
  photos.value.splice(idx, 1)
}

const handleSubmit = () => {
  router.back()
}
</script>

<style scoped>
.add-photo {
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
.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 12px;
}
.photo-item {
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  background: #f3f4f6;
}
.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.photo-delete {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 18px;
  height: 18px;
  background: #ef4444;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.photo-item.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d1d5db;
  background: #f9fafb;
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
