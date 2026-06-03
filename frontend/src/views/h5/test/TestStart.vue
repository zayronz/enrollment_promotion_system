<template>
  <div class="test-start">
    <H5NavBar title="能力测试" :transparent="true" />

    <div class="start-header">
      <div class="start-title">能力测试</div>
      <div class="start-deco">
        <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
          <circle cx="60" cy="60" r="50" stroke="rgba(255,255,255,0.2)" stroke-width="1"/>
          <circle cx="60" cy="60" r="35" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
          <circle cx="60" cy="60" r="20" stroke="rgba(255,255,255,0.4)" stroke-width="1"/>
          <circle cx="60" cy="60" r="8" fill="rgba(255,255,255,0.5)"/>
          <circle cx="30" cy="30" r="3" fill="rgba(255,255,255,0.6)"/>
          <circle cx="90" cy="25" r="2" fill="rgba(255,255,255,0.4)"/>
          <circle cx="95" cy="80" r="2.5" fill="rgba(255,255,255,0.5)"/>
        </svg>
      </div>
    </div>

    <div class="start-card">
      <div class="card-title">季招生能力测试</div>
      <div class="card-desc">
        尊敬的各位招宣人员：<br/>
        本次考试共28题，请您于4月X号18:00-20:00完成测试。请务必在规定的时间内完成，一旦超时分敷记为0。若已知晓上述内容，请开始测试。
      </div>
      <label class="agree-row">
        <input type="checkbox" v-model="agreed" />
        <span>我已知晓上述内容</span>
      </label>
    </div>

    <div class="start-action">
      <button class="start-btn" :disabled="!agreed" @click="startTest">开始测试</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const route = useRoute()
const agreed = ref(false)

const startTest = () => {
  router.push(`/h5/test/questions/${route.params.id}`)
}
</script>

<style scoped>
.test-start {
  min-height: 100vh;
  background: #f5f7fa;
}
.start-header {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  padding: 20px 16px 60px;
  position: relative;
  overflow: hidden;
}
.start-title {
  color: #fff;
  font-size: 24px;
  font-weight: 600;
}
.start-deco {
  position: absolute;
  right: -10px;
  top: 20px;
  opacity: 0.6;
}
.start-card {
  background: #fff;
  border-radius: 16px;
  margin: -40px 12px 0;
  padding: 20px;
  position: relative;
  z-index: 2;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
  text-align: center;
  margin-bottom: 14px;
}
.card-desc {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.8;
  margin-bottom: 20px;
}
.agree-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
}
.agree-row input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #2563eb;
}
.start-action {
  padding: 20px 16px;
}
.start-btn {
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
.start-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}
</style>
