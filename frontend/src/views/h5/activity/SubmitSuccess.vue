<template>
  <div class="submit-success">
    <H5NavBar title="活动详情" />
    <div class="success-content">
      <div class="success-icon">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <rect x="15" y="10" width="50" height="55" rx="4" fill="#e5e7eb"/>
          <circle cx="55" cy="55" r="14" fill="#10b981"/>
          <path d="M49 55l4 4 8-8" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="30" cy="30" r="4" fill="#d1d5db"/>
          <path d="M38 28h16M38 34h10" stroke="#d1d5db" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <div class="success-title">提交成功</div>
      <div class="success-desc">请耐心等待审核，结果在"我的活动"中查看</div>
      <button class="back-btn" @click="goBack" v-if="showBack">返回列表</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const props = defineProps({
  showBack: { type: Boolean, default: false }
})

const router = useRouter()
const goBack = () => {
  router.push('/h5/home')
}
</script>

<style scoped>
.submit-success {
  min-height: 100vh;
  background: #f5f7fa;
}
.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 80px;
}
.success-icon {
  margin-bottom: 20px;
}
.success-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 10px;
}
.success-desc {
  font-size: 14px;
  color: #9ca3af;
  text-align: center;
}
.back-btn {
  margin-top: 40px;
  width: 200px;
  height: 44px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
}
</style>
