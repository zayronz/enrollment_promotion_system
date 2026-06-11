<template>
  <div class="login-page">
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
        <div class="brand-tag">太原理工大学 · 智慧招生</div>
        <h1 class="brand-title">
          让每一次招生<br />
          <span class="brand-highlight">都更高效</span>
        </h1>
        <p class="brand-desc">
          构建多渠道、全方位、多层次的智慧招生宣传工作平台，助力教育招生事业高质量发展
        </p>
        <div class="brand-features">
          <div class="bf-item" v-for="(f, i) in brandFeats" :key="i">
            <span class="bf-check">✓</span>
            <span>{{ f }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-section">
        <div class="login-card">
          <div class="card-top">
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
              <t-input
                v-model="formData.username"
                size="large"
                placeholder="请输入用户名"
                clearable
                class="form-input"
              >
                <template #prefix-icon><UserIcon /></template>
              </t-input>
            </t-form-item>

            <t-form-item name="password">
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
                登 录
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
  background: #f5f6f8;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===== 顶部导航 ===== */
.top-nav {
  background: #fff;
  border-bottom: 1px solid #eceef2;
  position: sticky;
  top: 0;
  z-index: 10;
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
  gap: 6px;
  background: #e8f3ff;
  color: #0052d9;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 14px;
  border-radius: 20px;
  margin-bottom: 28px;
  letter-spacing: 0.5px;
}
.brand-title {
  font-size: 42px;
  font-weight: 700;
  color: #1d2129;
  line-height: 1.25;
  margin: 0 0 20px;
  letter-spacing: -0.5px;
}
.brand-highlight {
  background: linear-gradient(135deg, #0052d9, #366ef4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.brand-desc {
  font-size: 15px;
  color: #646a73;
  line-height: 1.7;
  margin: 0 0 36px;
  max-width: 440px;
}
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.bf-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #4e5969;
  font-weight: 500;
}
.bf-check {
  width: 22px;
  height: 22px;
  background: #e8f3ff;
  color: #0052d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

/* ===== 右侧登录卡片 ===== */
.login-section {
  width: 400px;
  flex-shrink: 0;
}
.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 44px 40px 36px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid #eceef2;
}
.card-top {
  text-align: center;
  margin-bottom: 32px;
}
.card-title {
  font-size: 26px;
  font-weight: 700;
  color: #1d2129;
  margin: 0 0 8px;
}
.card-sub {
  font-size: 14px;
  color: #8f959e;
  margin: 0;
}

.login-form {
  margin-bottom: 4px;
}
.form-input :deep(.t-input) {
  border-radius: 8px;
  background: #f8f9fa;
  border-color: #e5e6eb;
}
.form-input :deep(.t-input:hover) {
  border-color: #c9cdd4;
}
.form-input :deep(.t-input.t-is-focused) {
  border-color: #0052d9;
  background: #fff;
}

.login-btn {
  margin-top: 8px;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 8px;
  border-radius: 8px;
  background: #0052d9;
  border: none;
  transition: all 0.25s;
}
.login-btn:hover {
  background: #366ef4;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0, 82, 217, 0.3);
}

.card-links {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #8f959e;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.link-divider {
  color: #e5e6eb;
}

/* H5 入口样式 */
.h5-entry {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e5e6eb;
}

.h5-entry :deep(.t-link) {
  font-size: 14px;
  color: #8f959e;
}

.h5-entry :deep(.t-link:hover) {
  color: #0052d9;
}

/* 测试账号 */
.test-accounts {
  margin-top: 28px;
  border-top: 1px solid #f0f1f3;
  padding-top: 16px;
}
.test-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  -webkit-user-select: none;
  user-select: none;
}
.test-label {
  font-size: 12px;
  color: #c9cdd4;
  letter-spacing: 1px;
  transition: color 0.2s;
}
.test-header:hover .test-label {
  color: #0052d9;
}
.test-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.test-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.test-item:hover {
  background: #e8f3ff;
  transform: translateX(4px);
}
.test-item:hover .test-hint {
  opacity: 1;
}
.test-role {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 4px;
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
  transition: opacity 0.2s;
  white-space: nowrap;
}

/* ===== 底部 ===== */
.footer-bar {
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: #c9cdd4;
  letter-spacing: 0.5px;
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
    font-size: 30px;
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
    padding: 32px 24px 28px;
  }
  .card-title { font-size: 22px; }
  .brand-title { font-size: 24px; }
}
</style>
