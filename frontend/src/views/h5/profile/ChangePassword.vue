<template>
  <div class="h5-change-password">
    <div class="page-header">
      <div class="back-btn" @click="router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <h1 class="page-title">修改密码</h1>
      <div class="header-placeholder"></div>
    </div>

    <div class="form-card">
      <div class="input-group">
        <div class="input-label">当前密码</div>
        <input
          v-model="form.oldPassword"
          type="password"
          class="form-input"
          placeholder="请输入当前密码"
        />
      </div>

      <div class="input-group">
        <div class="input-label">新密码</div>
        <input
          v-model="form.newPassword"
          type="password"
          class="form-input"
          placeholder="请输入新密码（至少6位）"
        />
      </div>

      <div class="input-group">
        <div class="input-label">确认新密码</div>
        <input
          v-model="form.confirmPassword"
          type="password"
          class="form-input"
          placeholder="请再次输入新密码"
        />
      </div>

      <button
        class="submit-btn"
        :class="{ loading: submitting }"
        :disabled="submitting || !canSubmit"
        @click="handleSubmit"
      >
        <span v-if="submitting" class="btn-spinner"></span>
        {{ submitting ? '提交中...' : '确认修改' }}
      </button>
    </div>

    <!-- Toast -->
    <Transition name="toast-fade">
      <div v-if="toast.show" class="toast-overlay">
        <div class="toast-box" :class="toast.type">
          <span>{{ toast.message }}</span>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { userApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const submitting = ref(false)

const toast = ref({ show: false, message: '', type: 'fail' })
let toastTimer = null
const showToast = (message, type = 'fail') => {
  clearTimeout(toastTimer)
  toast.value = { show: true, message, type }
  toastTimer = setTimeout(() => { toast.value.show = false }, 2000)
}

const canSubmit = computed(() => {
  return form.value.oldPassword && form.value.newPassword && form.value.confirmPassword
})

const handleSubmit = async () => {
  if (!canSubmit.value) {
    showToast('请填写完整密码信息')
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    showToast('两次输入的新密码不一致')
    return
  }
  if (form.value.newPassword.length < 6) {
    showToast('新密码长度不能少于6位')
    return
  }

  submitting.value = true
  try {
    await userApi.changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    })
    showToast('密码修改成功', 'success')
    setTimeout(() => {
      userStore.logout()
      router.replace('/h5/login')
    }, 1500)
  } catch (err) {
    showToast(err.response?.data?.message || '修改密码失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.h5-change-password {
  min-height: 100vh;
  background: #f5f7fa;
  max-width: 480px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f2937;
  cursor: pointer;
}
.page-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}
.header-placeholder {
  width: 32px;
}

.form-card {
  margin: 16px 12px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.input-group {
  margin-bottom: 16px;
}
.input-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 6px;
}
.form-input {
  width: 100%;
  height: 46px;
  padding: 0 14px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 15px;
  color: #1f2937;
  background: #f8f9fb;
  outline: none;
  transition: all 0.25s;
  box-sizing: border-box;
}
.form-input:focus {
  background: #fff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
}
.form-input::placeholder {
  color: #c0c6d0;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
}
.submit-btn:active:not(:disabled) {
  transform: scale(0.98);
}
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin-right: 8px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Toast */
.toast-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  pointer-events: none;
}
.toast-box {
  padding: 12px 24px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  font-size: 14px;
  color: #1f2937;
}
.toast-box.success {
  color: #10b981;
}
.toast-box.fail {
  color: #ef4444;
}
.toast-fade-enter-active { transition: all 0.3s ease; }
.toast-fade-leave-active { transition: all 0.2s ease; }
.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: scale(0.85);
}
</style>
