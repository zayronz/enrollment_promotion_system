<template>
  <div class="h5-login">
    <!-- 顶部品牌区域 -->
    <div class="login-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="logo-area">
          <div class="logo-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="12" fill="rgba(255,255,255,0.2)"/>
              <path d="M14 32V16l10 8-10 8z" fill="#fff"/>
              <path d="M24 32V16l10 8-10 8z" fill="rgba(255,255,255,0.6)"/>
            </svg>
          </div>
          <h1 class="system-title" style="font-size:28px">招生宣传报名系统</h1>
          <p class="system-subtitle">Enrollment Promotion System</p>
        </div>
      </div>
      <div class="header-wave">
        <svg viewBox="0 0 480 40" preserveAspectRatio="none">
          <path d="M0 20 Q120 0 240 20 Q360 40 480 20 L480 40 L0 40 Z" fill="#f5f7fa"/>
        </svg>
      </div>
    </div>

    <!-- 登录表单卡片 -->
    <div class="login-card">
      <div class="card-tab">
        <span class="tab-item active">账号登录</span>
      </div>

      <div class="form-area">
        <!-- 用户名 -->
        <div class="input-group">
          <div class="input-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名/学工号"
            @keyup.enter="handleLogin"
          />
        </div>

        <!-- 密码 -->
        <div class="input-group">
          <div class="input-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0110 0v4"/>
            </svg>
          </div>
          <input
            v-model="form.password"
            :type="showPwd ? 'text' : 'password'"
            class="form-input"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
          <div class="input-suffix" @click="showPwd = !showPwd">
            <svg v-if="!showPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
              <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/>
              <line x1="1" y1="1" x2="23" y2="23"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </div>
        </div>

        <!-- 忘记密码 -->
        <div class="forgot-row">
          <span class="forgot-link" @click="handleForgotPassword">忘记密码？</span>
        </div>

        <!-- 登录按钮 -->
        <button
          class="login-btn"
          :class="{ loading: loading }"
          :disabled="loading || !canLogin"
          @click="handleLogin"
        >
          <span v-if="loading" class="btn-spinner"></span>
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <!-- 注册链接 -->
        <div class="register-row">
          还没有账号？<span class="register-link" @click="handleRegister">立即注册</span>
        </div>
      </div>
    </div>

    <!-- 测试账号快速登录（开发环境） -->
    <div class="demo-section">
      <div class="demo-title">
        <span class="demo-line"></span>
        <span class="demo-text">测试账号快速登录</span>
        <span class="demo-line"></span>
      </div>
      <div class="demo-accounts">
        <button
          v-for="account in demoAccounts"
          :key="account.role"
          class="demo-account-btn"
          @click="quickLogin(account)"
        >
          <span class="demo-role">{{ account.label }}</span>
          <span class="demo-info">{{ account.username }}</span>
        </button>
      </div>
    </div>

    <!-- 版权 -->
    <div class="copyright">太原理工大学招生办 © {{ new Date().getFullYear() }}</div>

    <!-- Toast 提示 -->
    <Transition name="toast-fade">
      <div v-if="toast.show" class="toast-overlay">
        <div class="toast-box" :class="toast.type">
          <svg v-if="toast.type === 'success'" class="toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <svg v-else class="toast-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          <span>{{ toast.message }}</span>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: ''
})
const showPwd = ref(false)
const loading = ref(false)

// 自定义 Toast
const toast = ref({ show: false, message: '', type: 'success' })
let toastTimer = null
const showToast = (message, type = 'fail') => {
  clearTimeout(toastTimer)
  toast.value = { show: true, message, type }
  toastTimer = setTimeout(() => {
    toast.value.show = false
  }, 2000)
}

const canLogin = computed(() => form.value.username.trim() && form.value.password.trim())

const demoAccounts = [
  { label: '学生账号', username: 'student', password: '123456', role: 'STUDENT' },
  { label: '教师账号', username: 'teacher', password: '123456', role: 'TEACHER' },
  { label: '学院账号', username: 'college', password: '123456', role: 'COLLEGE' },
  { label: '学校账号', username: 'admin', password: '123456', role: 'SCHOOL' }
]

const handleLogin = async () => {
  if (!canLogin.value || loading.value) return
  loading.value = true
  try {
    const success = await userStore.login(form.value.username, form.value.password)
    if (success) {
      showToast('登录成功', 'success')
      const redirect = route.query.redirect || '/h5/home'
      setTimeout(() => router.replace(redirect), 500)
    } else {
      showToast('用户名或密码错误', 'fail')
    }
  } catch {
    showToast('登录失败，请稍后重试', 'fail')
  } finally {
    loading.value = false
  }
}

const quickLogin = async (account) => {
  form.value.username = account.username
  form.value.password = account.password
  await handleLogin()
}

const handleForgotPassword = () => {
  router.push('/forgot-password')
}

const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.h5-login {
  min-height: 100vh;
  background: #f5f7fa;
  position: relative;
  overflow-x: hidden;
}

/* ===== 头部品牌区域 ===== */
.login-header {
  position: relative;
  height: 220px;
}
.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 50%, #1d4ed8 100%);
}
.header-bg::after {
  content: '';
  position: absolute;
  top: -60px;
  right: -80px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}
.header-bg::before {
  content: '';
  position: absolute;
  bottom: -40px;
  left: -60px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255,255,255,0.04);
}
.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding-top: 20px;
}
.logo-area {
  text-align: center;
}
.logo-icon {
  margin-bottom: 12px;
  animation: float 3s ease-in-out infinite;
}
.system-title {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
  margin: 0 0 6px;
}
.system-subtitle {
  color: rgba(255,255,255,0.7);
  font-size: 12px;
  letter-spacing: 3px;
  text-transform: uppercase;
  margin: 0;
}
.header-wave {
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 40px;
  z-index: 2;
}
.header-wave svg {
  width: 100%;
  height: 100%;
}

/* ===== 登录卡片 ===== */
.login-card {
  position: relative;
  z-index: 3;
  margin: -20px 16px 0;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
  padding: 0 24px 28px;
}
.card-tab {
  text-align: center;
  padding: 20px 0 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}
.tab-item {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  position: relative;
  padding-bottom: 16px;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
}

/* ===== 表单项 ===== */
.form-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.input-group {
  position: relative;
  display: flex;
  align-items: center;
  background: #f8f9fb;
  border-radius: 10px;
  padding: 0 14px;
  height: 48px;
  border: 1.5px solid transparent;
  transition: all 0.25s;
}
.input-group:focus-within {
  background: #fff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
}
.input-icon {
  display: flex;
  align-items: center;
  color: #9ca3af;
  margin-right: 10px;
  flex-shrink: 0;
}
.input-group:focus-within .input-icon {
  color: #3b82f6;
}
.form-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: #1f2937;
  height: 100%;
}
.form-input::placeholder {
  color: #c0c6d0;
}
.input-suffix {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding-left: 8px;
  flex-shrink: 0;
}

/* 忘记密码 */
.forgot-row {
  display: flex;
  justify-content: flex-end;
}
.forgot-link {
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
}
.forgot-link:hover {
  text-decoration: underline;
}

/* ===== 登录按钮 ===== */
.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
}
.login-btn:active:not(:disabled) {
  transform: scale(0.98);
}
.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.login-btn.loading {
  opacity: 0.8;
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

/* 注册链接 */
.register-row {
  text-align: center;
  font-size: 13px;
  color: #9ca3af;
}
.register-link {
  color: #3b82f6;
  cursor: pointer;
  font-weight: 500;
}

/* ===== 测试账号 ===== */
.demo-section {
  margin: 28px 16px 0;
}
.demo-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.demo-line {
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}
.demo-text {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
}
.demo-accounts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.demo-account-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 4px;
}
.demo-account-btn:active {
  border-color: #3b82f6;
  background: #eff6ff;
}
.demo-role {
  font-size: 12px;
  color: #6b7280;
}
.demo-info {
  font-size: 13px;
  color: #3b82f6;
  font-weight: 500;
}

/* ===== 版权 ===== */
.copyright {
  text-align: center;
  padding: 24px 0 40px;
  font-size: 11px;
  color: #c0c6d0;
}

/* ===== 动画 ===== */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Toast ===== */
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
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  font-size: 14px;
  color: #1f2937;
}
.toast-box.success .toast-icon {
  color: #10b981;
  width: 20px;
  height: 20px;
}
.toast-box.fail .toast-icon {
  color: #ef4444;
  width: 20px;
  height: 20px;
}
.toast-fade-enter-active { transition: all 0.3s ease; }
.toast-fade-leave-active { transition: all 0.2s ease; }
.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: scale(0.85);
}
</style>
