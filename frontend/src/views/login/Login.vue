<template>
  <div class="login-page">
    <!-- 动态背景层 -->
    <div class="bg-layer">
      <div class="bg-gradient"></div>
      <div class="bg-particles">
        <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
      </div>
      <div class="bg-blur"></div>
    </div>

    <!-- 顶部导航 -->
    <header class="top-nav">
      <div class="nav-content">
        <div class="nav-brand">
          <div class="brand-logo">
            <BookOpenIcon class="logo-icon" />
          </div>
          <span class="brand-name">招生宣传报名系统</span>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="main-area">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-tag">
          <span class="tag-dot"></span>
          太原理工大学 · 智慧招生
        </div>
        <h1 class="brand-title">
          让每一次招生<br />
          <span class="brand-highlight">都更高效</span>
        </h1>
        <p class="brand-desc">
          构建多渠道、全方位、多层次的智慧招生宣传工作平台，助力教育招生事业高质量发展
        </p>
        <div class="brand-features">
          <div class="bf-item" v-for="(f, i) in brandFeats" :key="i" :style="{ animationDelay: i * 0.15 + 's' }">
            <span class="bf-check">✓</span>
            <span>{{ f }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-section">
        <div class="login-card">
          <div class="card-glow"></div>
          <div class="card-top">
            <div class="card-icon">
              <UserIcon />
            </div>
            <h2 class="card-title">欢迎登录</h2>
            <p class="card-sub">使用统一身份认证账号</p>
          </div>

          <t-form
            ref="formRef"
            :data="formData"
            :rules="rules"
            label-width="0"
            class="login-form"
            @submit="handleLogin"
          >
            <t-form-item name="username">
              <div class="input-wrapper">
                <t-input
                  v-model="formData.username"
                  size="large"
                  placeholder="请输入用户名"
                  clearable
                  class="form-input"
                >
                  <template #prefix-icon><UserIcon /></template>
                </t-input>
                <div class="input-border"></div>
              </div>
            </t-form-item>

            <t-form-item name="password">
              <div class="input-wrapper">
                <t-input
                  v-model="formData.password"
                  type="password"
                  size="large"
                  placeholder="请输入密码"
                  clearable
                  class="form-input"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix-icon><LockOnIcon /></template>
                </t-input>
                <div class="input-border"></div>
              </div>
            </t-form-item>

            <t-form-item>
              <t-button
                block
                size="large"
                theme="primary"
                type="submit"
                :loading="loading"
                class="login-btn"
              >
                <span class="btn-text">登 录</span>
                <span class="btn-wave"></span>
              </t-button>
            </t-form-item>
          </t-form>

          <div class="card-links">
            <t-link theme="primary" hover="color" @click="$router.push('/register')">立即注册</t-link>
            <span class="link-divider">|</span>
            <t-link theme="default" hover="color" @click="$router.push('/forgot-password')">忘记密码</t-link>
          </div>

          <!-- 测试账号 -->
          <div class="test-accounts" :class="{ open: showTestAccounts }">
            <div class="test-header" @click="showTestAccounts = !showTestAccounts">
              <span class="test-label">测试账号</span>
              <t-icon :name="showTestAccounts ? 'chevron-up' : 'chevron-down'" size="14px" />
            </div>
            <div class="test-list" v-show="showTestAccounts">
              <div
                class="test-item"
                v-for="acc in testAccounts"
                :key="acc.role"
                @click="quickLogin(acc.username, acc.password)"
              >
                <span class="test-role" :class="acc.cls">{{ acc.label }}</span>
                <span class="test-creds">{{ acc.username }} / {{ acc.password }}</span>
                <span class="test-hint">点击登录</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="footer-bar">
      <span>太原理工大学 招生宣传报名系统 v1.0</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { BookOpenIcon, UserIcon, LockOnIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const showTestAccounts = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const brandFeats = [
  '多角色管理：学生、教师、学院、学校四位一体',
  '全流程跟踪：报名→审核→分组→反馈闭环管理',
  '智能化分析：自动分组排名，数据驱动决策'
]

const testAccounts = [
  { role: 'student', label: '学生', cls: 'role-stu', username: 'student', password: '123456' },
  { role: 'teacher', label: '教师', cls: 'role-tea', username: 'teacher', password: '123456' },
  { role: 'college', label: '学院', cls: 'role-col', username: 'college', password: '123456' },
  { role: 'admin', label: '学校', cls: 'role-sch', username: 'admin', password: '123456' }
]

const getParticleStyle = (index) => {
  const size = Math.random() * 6 + 2
  const left = Math.random() * 100
  const delay = Math.random() * 10
  const duration = Math.random() * 20 + 15
  const opacity = Math.random() * 0.5 + 0.1
  return {
    width: size + 'px',
    height: size + 'px',
    left: left + '%',
    animationDelay: delay + 's',
    animationDuration: duration + 's',
    opacity: opacity
  }
}

const quickLogin = (username, password) => {
  formData.username = username
  formData.password = password
  handleLogin()
}

const handleLogin = async (e) => {
  if (e && e.preventDefault) e.preventDefault()

  const valid = await formRef.value.validate()
  if (valid !== true) return

  loading.value = true
  try {
    const success = await userStore.login(formData.username, formData.password)
    if (success) {
      MessagePlugin.success('登录成功')
      const role = userStore.role
      const routes = {
        SCHOOL: '/school/dashboard',
        COLLEGE: '/college/pending',
        TEACHER: '/teacher/activities'
      }
      router.push(routes[role] || '/student/activities')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  position: relative;
  overflow: hidden;
}

/* ===== 动态背景 ===== */
.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.bg-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.particle {
  position: absolute;
  top: -20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: particleFloat linear infinite;
}

@keyframes particleFloat {
  0% {
    transform: translateY(-20px) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(720deg);
    opacity: 0;
  }
}

.bg-blur {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse at 30% 20%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
              radial-gradient(ellipse at 70% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

/* ===== 顶部导航 ===== */
.top-nav {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-logo {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #0052d9, #366ef4);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(0, 82, 217, 0.3);
  transition: transform 0.3s, box-shadow 0.3s;
}

.brand-logo:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 82, 217, 0.4);
}

.logo-icon {
  font-size: 20px;
  color: #fff;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

/* ===== 主内容区 ===== */
.main-area {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 40px 60px;
  display: flex;
  align-items: flex-start;
  gap: 120px;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  z-index: 10;
}

/* ===== 左侧品牌 ===== */
.brand-section {
  flex: 1;
  padding-top: 40px;
  max-width: 520px;
}

.brand-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.9);
  color: #0052d9;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 16px;
  border-radius: 20px;
  margin-bottom: 28px;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.tag-dot {
  width: 6px;
  height: 6px;
  background: #0052d9;
  border-radius: 50%;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
  margin: 0 0 24px;
  letter-spacing: -1px;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-highlight {
  background: linear-gradient(135deg, #ffd700, #ff9500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.brand-highlight::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 0;
  right: 0;
  height: 8px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.3), rgba(255, 149, 0, 0.3));
  border-radius: 4px;
  z-index: -1;
}

.brand-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.8;
  margin: 0 0 40px;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 20px 24px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bf-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  opacity: 0;
  animation: fadeInLeft 0.6s ease-out forwards;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.bf-check {
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  color: #ffd700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* ===== 右侧登录卡片 ===== */
.login-section {
  width: 400px;
  flex-shrink: 0;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 50px 44px 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.login-card:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: 0 35px 60px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(255, 255, 255, 0.2);
}

.card-glow {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(0, 82, 217, 0.1) 0%, transparent 70%);
  pointer-events: none;
}

.card-top {
  text-align: center;
  margin-bottom: 36px;
}

.card-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #0052d9, #366ef4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 25px rgba(0, 82, 217, 0.35);
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.card-icon :deep(.t-icon) {
  font-size: 28px;
  color: #fff;
}

.card-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  margin: 0 0 10px;
}

.card-sub {
  font-size: 14px;
  color: #8f959e;
  margin: 0;
}

.login-form {
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
  margin-bottom: 8px;
}

.input-border {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #0052d9, #366ef4);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  transform-origin: left;
}

.form-input:focus-within + .input-border {
  transform: scaleX(1);
}

.form-input :deep(.t-input) {
  border-radius: 10px;
  background: #f5f6f7;
  border-color: transparent;
  height: 48px;
  font-size: 15px;
  transition: all 0.3s ease;
}

.form-input :deep(.t-input:hover) {
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.form-input :deep(.t-input.t-is-focused) {
  background: #fff;
  border-color: #0052d9;
  box-shadow: 0 0 0 3px rgba(0, 82, 217, 0.1);
}

.login-btn {
  margin-top: 12px;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 10px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0052d9, #366ef4);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 82, 217, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 82, 217, 0.4);
  background: linear-gradient(135deg, #0066ff, #4a8fff);
}

.login-btn:active {
  transform: translateY(0);
}

.btn-text {
  position: relative;
  z-index: 1;
}

.btn-wave {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.login-btn:hover .btn-wave {
  width: 300px;
  height: 300px;
}

.card-links {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #8f959e;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

.link-divider {
  color: #e5e6eb;
}

/* 测试账号 */
.test-accounts {
  margin-top: 32px;
  border-top: 1px solid #f0f1f3;
  padding-top: 20px;
}

.test-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  -webkit-user-select: none;
  user-select: none;
  padding: 8px 16px;
  border-radius: 20px;
  transition: background 0.2s;
}

.test-header:hover {
  background: #f5f6f7;
}

.test-label {
  font-size: 13px;
  color: #8f959e;
  letter-spacing: 1px;
  transition: color 0.2s;
}

.test-header:hover .test-label {
  color: #0052d9;
}

.test-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.test-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 10px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid transparent;
}

.test-item:hover {
  background: #e8f3ff;
  transform: translateX(6px);
  border-color: rgba(0, 82, 217, 0.2);
}

.test-item:hover .test-hint {
  opacity: 1;
  transform: translateX(0);
}

.test-role {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  min-width: 36px;
}

.role-stu { background: #e8f3ff; color: #0052d9; }
.role-tea { background: #fff7e6; color: #d46b08; }
.role-col { background: #e6f7e9; color: #1e8540; }
.role-sch { background: #fde8ec; color: #c92a44; }

.test-creds {
  flex: 1;
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  font-size: 12px;
  color: #646a73;
}

.test-hint {
  font-size: 11px;
  color: #0052d9;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.25s;
  white-space: nowrap;
}

/* ===== 底部 ===== */
.footer-bar {
  text-align: center;
  padding: 24px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.5px;
  position: relative;
  z-index: 10;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .main-area {
    flex-direction: column;
    align-items: center;
    gap: 40px;
    padding: 40px 24px 40px;
  }
  .brand-section {
    text-align: center;
    max-width: 100%;
    padding-top: 0;
  }
  .brand-title {
    font-size: 32px;
  }
  .brand-desc {
    max-width: 100%;
  }
  .brand-features {
    align-items: center;
  }
  .login-section {
    width: 100%;
    max-width: 400px;
  }
}

@media (max-width: 480px) {
  .nav-content { padding: 0 20px; }
  .login-card {
    padding: 36px 24px 28px;
  }
  .card-title { font-size: 24px; }
  .brand-title { font-size: 26px; }
}
</style>