<template>
  <div class="forgot-page">
    <div class="bg-decoration">
      <div class="bg-circle c1" />
      <div class="bg-circle c2" />
    </div>
    <div class="forgot-card">
      <div class="card-header">
        <h2>找回密码</h2>
        <p class="card-desc">
          {{ step === 'verify' ? '请先验证绑定邮箱，再设置新密码' : '验证成功，请设置新密码' }}
        </p>
        <div class="step-bar">
          <div class="step-item" :class="{ active: step === 'verify', done: step === 'reset' }">1 验证邮箱</div>
          <div class="step-line" :class="{ done: step === 'reset' }"></div>
          <div class="step-item" :class="{ active: step === 'reset' }">2 重置密码</div>
        </div>
      </div>
      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-width="80px"
        class="forgot-form"
        @submit="handleSubmit"
      >
        <t-form-item label="用户名" name="username">
          <t-input
            v-model="formData.username"
            placeholder="请输入您的用户名"
            clearable
            :disabled="step === 'reset'"
          >
            <template #prefix-icon><UserIcon /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="绑定邮箱" name="email">
          <t-input
            v-model="formData.email"
            placeholder="请输入注册时绑定的邮箱"
            clearable
            :disabled="step === 'reset'"
          >
            <template #prefix-icon><MailIcon /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="验证码" name="code">
          <div class="code-row">
            <t-input
              v-model="formData.code"
              placeholder="请输入邮箱验证码"
              clearable
              :disabled="step === 'reset'"
            >
              <template #prefix-icon><MailIcon /></template>
            </t-input>
            <t-button
              theme="primary"
              variant="outline"
              class="code-btn"
              :loading="codeLoading"
              :disabled="step === 'reset' || codeLoading || countdown > 0"
              @click="sendCode"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
            </t-button>
          </div>
        </t-form-item>

        <t-form-item v-if="step === 'reset'" label="新密码" name="newPassword">
          <t-input
            v-model="formData.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            clearable
          >
            <template #prefix-icon><LockOnIcon /></template>
          </t-input>
        </t-form-item>

        <t-form-item v-if="step === 'reset'" label="确认密码" name="confirmPassword">
          <t-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            clearable
          >
            <template #prefix-icon><LockOnIcon /></template>
          </t-input>
        </t-form-item>

        <t-form-item label-width="0">
          <t-button
            block
            size="large"
            theme="primary"
            type="submit"
            :loading="loading"
          >
            {{ step === 'verify' ? '验证验证码' : '重置密码' }}
          </t-button>
        </t-form-item>
      </t-form>

      <div class="card-footer">
        <t-link theme="primary" @click="goLogin">
          返回登录
        </t-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { UserIcon, MailIcon, LockOnIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const codeLoading = ref(false)
const countdown = ref(0)
const step = ref('verify')
let countdownTimer = null

const formData = reactive({
  username: '',
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (val) => {
  if (!val) return { result: false, message: '请再次输入新密码', type: 'error' }
  if (val !== formData.newPassword) return { result: false, message: '两次输入的密码不一致', type: 'error' }
  return { result: true }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const validateAccountFields = () => {
  if (!formData.username) {
    MessagePlugin.warning('请先输入用户名')
    return false
  }
  if (!formData.email) {
    MessagePlugin.warning('请先输入绑定邮箱')
    return false
  }
  return true
}

const startCountdown = () => {
  countdown.value = 60
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const sendCode = async () => {
  if (!validateAccountFields()) return

  codeLoading.value = true
  try {
    await userApi.sendForgotPasswordCode({
      username: formData.username,
      email: formData.email
    })
    MessagePlugin.success('验证码已发送，请查看绑定邮箱')
    startCountdown()
  } catch (err) {
    // 错误由拦截器统一提示
  } finally {
    codeLoading.value = false
  }
}

const goLogin = () => {
  router.push(route.path.startsWith('/h5') ? '/h5/login' : '/login')
}

const verifyCode = async () => {
  if (!validateAccountFields()) return
  if (!formData.code) {
    MessagePlugin.warning('请输入邮箱验证码')
    return
  }

  loading.value = true
  try {
    await userApi.verifyForgotPasswordCode({
      username: formData.username,
      email: formData.email,
      code: formData.code
    })
    MessagePlugin.success('验证码验证成功，请设置新密码')
    step.value = 'reset'
  } catch (err) {
    // 错误由拦截器统一提示
  } finally {
    loading.value = false
  }
}

const resetPassword = async () => {
  if (!formData.newPassword) {
    MessagePlugin.warning('请输入新密码')
    return
  }
  if (formData.newPassword.length < 6) {
    MessagePlugin.warning('密码长度不能少于6位')
    return
  }
  if (formData.newPassword !== formData.confirmPassword) {
    MessagePlugin.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const res = await userApi.forgotPassword({
      username: formData.username,
      email: formData.email,
      code: formData.code,
      newPassword: formData.newPassword
    })
    if (res.code === 200 || res === true) {
      MessagePlugin.success('密码重置成功，请使用新密码登录')
      goLogin()
    }
  } catch (err) {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleSubmit = async (e) => {
  if (e && e.preventDefault) {
    e.preventDefault()
  }
  if (step.value === 'verify') {
    await verifyCode()
  } else {
    await resetPassword()
  }
}
</script>

<style scoped>
.forgot-page {
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

.forgot-card {
  position: relative;
  z-index: 1;
  width: 440px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 44px 40px 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 8px 30px rgba(0, 0, 0, 0.06);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}
.card-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}
.card-desc {
  font-size: 13px;
  color: var(--td-text-color-placeholder);
}

.step-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;
}

.step-item {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--td-bg-color-secondarycontainer);
  transition: all 0.2s;
}

.step-item.active,
.step-item.done {
  color: var(--td-brand-color);
  background: var(--td-brand-color-light);
  font-weight: 600;
}

.step-line {
  width: 42px;
  height: 2px;
  background: var(--td-border-level-2-color);
}

.step-line.done {
  background: var(--td-brand-color);
}

.forgot-form {
  margin-top: 8px;
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-row .t-input {
  flex: 1;
}

.code-btn {
  flex-shrink: 0;
  min-width: 118px;
}

.card-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}

@media (max-width: 480px) {
  .forgot-card {
    padding: 36px 24px 28px;
    border-radius: 12px;
  }
  .code-row {
    flex-direction: column;
  }
  .code-btn {
    width: 100%;
  }
}
</style>
