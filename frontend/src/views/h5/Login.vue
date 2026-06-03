<template>
  <div class="h5-login">
    <!-- 头部 -->
    <div class="login-header">
      <div class="header-logo">
        <BookOpenIcon class="logo-icon" />
      </div>
      <h1 class="header-title">招生宣传报名系统</h1>
      <p class="header-sub">太原理工大学 · 智慧招生</p>
    </div>

    <!-- 登录表单 -->
    <div class="login-form">
      <div class="form-title">欢迎登录</div>

      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-width="0"
        @submit="handleLogin"
      >
        <div class="input-item">
          <div class="input-label">用户名</div>
          <t-input
            v-model="formData.username"
            placeholder="请输入用户名"
            size="large"
          >
            <template #prefix-icon><UserIcon /></template>
          </t-input>
        </div>

        <div class="input-item">
          <div class="input-label">密码</div>
          <t-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            size="large"
          >
            <template #prefix-icon><LockOnIcon /></template>
          </t-input>
        </div>

        <t-button
          block
          size="large"
          theme="primary"
          type="submit"
          :loading="loading"
          class="login-btn"
        >
          登录
        </t-button>
      </t-form>

      <!-- 测试账号 -->
      <div class="test-accounts">
        <div class="test-title">测试账号</div>
        <div class="test-list">
          <div
            v-for="acc in testAccounts"
            :key="acc.role"
            class="test-item"
            @click="quickLogin(acc.username, acc.password)"
          >
            <span class="test-role">{{ acc.label }}</span>
            <span class="test-creds">{{ acc.username }}</span>
          </div>
        </div>
      </div>

      <div class="back-link">
        <t-link theme="default" hover="color" @click="$router.push('/login')">
          返回PC版登录
        </t-link>
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

const testAccounts = [
  { role: 'student', label: '学生', username: 'student', password: '123456' },
  { role: 'teacher', label: '教师', username: 'teacher', password: '123456' },
  { role: 'college', label: '学院', username: 'college', password: '123456' },
  { role: 'admin', label: '学校', username: 'admin', password: '123456' }
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
      // H5 登录后跳转到 H5 首页
      router.push('/h5/home')
    }
  } catch (err) {
    console.error('登录失败', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.h5-login {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f3ff 0%, #f5f7fa 100%);
  padding: 60px 20px 40px;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.header-logo {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #0052d9, #366ef4);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 24px rgba(0, 82, 217, 0.25);
}

.logo-icon {
  font-size: 32px;
  color: #fff;
}

.header-title {
  font-size: 22px;
  font-weight: 700;
  color: #1d2129;
  margin: 0 0 8px;
}

.header-sub {
  font-size: 14px;
  color: #8f959e;
  margin: 0;
}

.login-form {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.form-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  text-align: center;
  margin-bottom: 24px;
}

.input-item {
  margin-bottom: 16px;
}

.input-label {
  font-size: 14px;
  color: #4e5969;
  margin-bottom: 8px;
  font-weight: 500;
}

.login-btn {
  margin-top: 24px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
}

.test-accounts {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f1f3;
}

.test-title {
  font-size: 12px;
  color: #8f959e;
  text-align: center;
  margin-bottom: 12px;
}

.test-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.test-item {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.test-item:active {
  background: #e8f3ff;
  transform: scale(0.98);
}

.test-role {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #0052d9;
  margin-bottom: 4px;
}

.test-creds {
  display: block;
  font-size: 11px;
  color: #8f959e;
  font-family: 'SF Mono', monospace;
}

.back-link {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
}
</style>
