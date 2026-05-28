<template>
  <div class="login-page">
    <!-- Decorative background -->
    <div class="bg-decoration">
      <div class="bg-circle c1" />
      <div class="bg-circle c2" />
      <div class="bg-circle c3" />
    </div>

    <!-- Login card -->
    <div class="login-card">
      <div class="card-header">
        <div class="logo-mark">
          <BookOpenIcon class="logo-icon" />
        </div>
        <h1 class="app-name">招生宣传报名系统</h1>
        <p class="app-subtitle"> 太原理工大学 · 智慧招生管理平台</p>
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

      <div class="card-footer">
        <span class="footer-hint">还没有账号？</span>
        <t-link theme="primary" @click="$router.push('/register')">
          立即注册
        </t-link>
      </div>

      <div class="test-accounts">
        <t-divider>
          <span class="divider-text">测试账号</span>
        </t-divider>
        <div class="account-grid">
          <div class="account-item">
            <span class="account-role student">学生</span>
            <code>student / 123456</code>
          </div>
          <div class="account-item">
            <span class="account-role admin">管理</span>
            <code>admin / 123456</code>
          </div>
        </div>
      </div>
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

const formData = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async (e) => {
  if (e && e.preventDefault) {
    e.preventDefault()
  }

  const valid = await formRef.value.validate()
  if (valid !== true) return

  loading.value = true
  try {
    const success = await userStore.login(formData.username, formData.password)
    if (success) {
      MessagePlugin.success('登录成功')
      const role = userStore.role
      if (role === 'SCHOOL') {
        router.push('/school/dashboard')
      } else if (role === 'COLLEGE') {
        router.push('/college/pending')
      } else if (role === 'TEACHER') {
        router.push('/teacher/activities')
      } else {
        router.push('/student/activities')
      }
    }
  } catch (err) {
    // Error handled by API interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fc;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
}
.c1 {
  width: 600px;
  height: 600px;
  background: var(--td-brand-color);
  top: -200px;
  right: -150px;
}
.c2 {
  width: 400px;
  height: 400px;
  background: var(--td-brand-color);
  bottom: -100px;
  left: -100px;
}
.c3 {
  width: 250px;
  height: 250px;
  background: var(--td-warning-color);
  top: 40%;
  right: 10%;
  opacity: 0.04;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px 36px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 8px 30px rgba(0, 0, 0, 0.06);
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}
.logo-mark {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  background: var(--td-brand-color);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-icon {
  font-size: 28px;
  color: #fff;
}
.app-name {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: 1px;
  margin-bottom: 8px;
}
.app-subtitle {
  font-size: 13px;
  color: var(--td-text-color-placeholder);
  letter-spacing: 0.5px;
}

.login-form {
  margin-top: 8px;
}
.login-btn {
  margin-top: 8px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 10px;
}

.card-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--td-text-color-secondary);
}
.footer-hint {
  margin-right: 4px;
}

.test-accounts {
  margin-top: 28px;
}
.divider-text {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
  letter-spacing: 1px;
}
.account-grid {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.account-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8f9fc;
  border-radius: 8px;
  font-size: 13px;
  color: var(--td-text-color-secondary);
}
.account-item code {
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  font-size: 12px;
  color: var(--td-text-color-primary);
}
.account-role {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.account-role.student {
  background: #dbeafe;
  color: #1e40af;
}
.account-role.admin {
  background: #fef3c7;
  color: #92400e;
}

@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px 28px;
    border-radius: 12px;
  }
}
</style>
