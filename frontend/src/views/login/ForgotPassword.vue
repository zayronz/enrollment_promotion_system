<template>
  <div class="forgot-page">
    <div class="bg-decoration">
      <div class="bg-circle c1" />
      <div class="bg-circle c2" />
    </div>
    <div class="forgot-card">
      <div class="card-header">
        <h2>找回密码</h2>
        <p class="card-desc">请输入注册邮箱或手机号，系统将跳转至统一身份认证平台完成密码找回</p>
      </div>

      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-width="110px"
        class="forgot-form"
        @submit="handleSubmit"
      >
        <t-form-item label="注册邮箱/手机号" name="account">
          <t-input
            v-model="formData.account"
            placeholder="请输入注册邮箱或手机号"
            clearable
          >
            <template #prefix-icon><UserIcon /></template>
          </t-input>
        </t-form-item>

        <t-alert
          theme="info"
          message="密码找回由统一身份认证平台处理，本系统不再提供本地验证码重置。"
          class="auth-tip"
        />

        <t-form-item label-width="0">
          <t-button
            block
            size="large"
            theme="primary"
            type="submit"
            :loading="loading"
          >
            前往统一身份认证平台
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
import { UserIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  account: ''
})

const validateAccount = (val) => {
  const value = String(val || '').trim()
  if (!value) return { result: false, message: '请输入注册邮箱或手机号', type: 'error' }
  const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
  const isPhone = /^1[3-9]\d{9}$/.test(value)
  if (!isEmail && !isPhone) {
    return { result: false, message: '请输入正确的邮箱或手机号', type: 'error' }
  }
  return { result: true }
}

const rules = {
  account: [{ validator: validateAccount, trigger: 'blur' }]
}

const goLogin = () => {
  router.push(route.path.startsWith('/h5') ? '/h5/login' : '/login')
}

const buildIdentityResetUrl = () => {
  const account = formData.account.trim()
  const configuredUrl = import.meta.env.VITE_IDENTITY_PASSWORD_RESET_URL || '/identity/password-reset'

  if (configuredUrl.includes('{account}')) {
    return configuredUrl.replace('{account}', encodeURIComponent(account))
  }

  const url = new URL(configuredUrl, window.location.origin)
  url.searchParams.set('account', account)
  url.searchParams.set('source', 'enrollment-promotion-system')
  return url.toString()
}

const redirectToIdentityAuth = () => {
  const targetUrl = buildIdentityResetUrl()
  MessagePlugin.success('即将跳转至统一身份认证平台')
  window.location.href = targetUrl
}

const handleSubmit = async (e) => {
  if (e && e.preventDefault) {
    e.preventDefault()
  }

  const valid = await formRef.value.validate()
  if (valid !== true) {
    MessagePlugin.warning('请先填写正确的注册邮箱或手机号')
    return
  }

  loading.value = true
  redirectToIdentityAuth()
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
  line-height: 1.6;
}

.forgot-form {
  margin-top: 8px;
}

.auth-tip {
  margin-bottom: 18px;
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
}
</style>
