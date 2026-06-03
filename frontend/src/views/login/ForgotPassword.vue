<template>
  <div class="forgot-page">
    <div class="bg-decoration">
      <div class="bg-circle c1" />
      <div class="bg-circle c2" />
    </div>
    <div class="forgot-card">
      <div class="card-header">
        <h2>找回密码</h2>
        <p class="card-desc">通过用户名和邮箱验证身份后重置密码</p>
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
          >
            <template #prefix-icon><UserIcon /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="绑定邮箱" name="email">
          <t-input
            v-model="formData.email"
            placeholder="请输入注册时绑定的邮箱"
            clearable
          >
            <template #prefix-icon><MailIcon /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="新密码" name="newPassword">
          <t-input
            v-model="formData.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            clearable
          >
            <template #prefix-icon><LockOnIcon /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="确认密码" name="confirmPassword">
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
            重置密码
          </t-button>
        </t-form-item>
      </t-form>

      <div class="card-footer">
        <t-link theme="primary" @click="$router.push('/login')">
          返回登录
        </t-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { UserIcon, MailIcon, LockOnIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  email: '',
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
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async (e) => {
  if (e && e.preventDefault) {
    e.preventDefault()
  }

  const valid = await formRef.value.validate()
  if (valid !== true) return

  loading.value = true
  try {
    const res = await userApi.forgotPassword({
      username: formData.username,
      email: formData.email,
      newPassword: formData.newPassword
    })
    if (res.code === 200 || res === true) {
      MessagePlugin.success('密码重置成功，请使用新密码登录')
      router.push('/login')
    }
  } catch (err) {
    // Error handled by interceptor
  } finally {
    loading.value = false
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

.forgot-form {
  margin-top: 8px;
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
