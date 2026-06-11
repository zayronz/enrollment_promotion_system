<template>
  <div class="identity-page">
    <div class="identity-card">
      <div class="platform-badge">统一身份认证平台</div>
      <h2>密码找回</h2>
      <p class="desc">本页面用于学生个人项目本地演示，模拟学校统一身份认证平台的密码找回流程。</p>

      <div class="account-box">
        <span>找回账号</span>
        <strong>{{ account || '未传入账号' }}</strong>
      </div>

      <div class="steps">
        <div class="step done">
          <span class="step-index">1</span>
          <div>
            <strong>验证注册邮箱/手机号</strong>
            <p>确认账号绑定信息有效</p>
          </div>
        </div>
        <div class="step done">
          <span class="step-index">2</span>
          <div>
            <strong>验证身份信息</strong>
            <p>模拟短信或邮箱验证码校验</p>
          </div>
        </div>
        <div class="step active">
          <span class="step-index">3</span>
          <div>
            <strong>设置新密码</strong>
            <p>正式环境由统一认证平台完成密码修改</p>
          </div>
        </div>
        <div class="step">
          <span class="step-index">4</span>
          <div>
            <strong>完成找回</strong>
            <p>返回招生宣传报名系统重新登录</p>
          </div>
        </div>
      </div>

      <t-alert
        theme="info"
        message="这是统一身份认证平台模拟页。为方便本地演示，这里会直接更新本系统用户密码。"
      />

      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-width="86px"
        class="reset-form"
        @submit="handleReset"
      >
        <t-form-item label="新密码" name="newPassword">
          <t-input
            v-model="formData.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            clearable
          />
        </t-form-item>
        <t-form-item label="确认密码" name="confirmPassword">
          <t-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            clearable
          />
        </t-form-item>
        <t-form-item label-width="0">
          <t-button theme="primary" block size="large" type="submit" :loading="loading">
            设置新密码
          </t-button>
        </t-form-item>
      </t-form>

      <div class="actions">
        <t-button v-if="resetSuccess" theme="success" block size="large" @click="goLogin">
          完成并返回登录
        </t-button>
        <t-button variant="text" block @click="goBack">返回上一页</t-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { userApi } from '@/api/user'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const resetSuccess = ref(false)

const account = computed(() => route.query.account || '')

const formData = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (val) => {
  if (!val) return { result: false, message: '请再次输入新密码', type: 'error' }
  if (val !== formData.newPassword) return { result: false, message: '两次输入的密码不一致', type: 'error' }
  return { result: true }
}

const rules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleReset = async (e) => {
  if (e && e.preventDefault) e.preventDefault()
  if (!account.value) {
    MessagePlugin.warning('缺少注册邮箱或手机号，请返回重新填写')
    return
  }
  const valid = await formRef.value.validate()
  if (valid !== true) return

  loading.value = true
  try {
    await userApi.identityPasswordReset({
      account: account.value,
      newPassword: formData.newPassword
    })
    resetSuccess.value = true
    MessagePlugin.success('密码设置成功，请使用新密码登录')
  } catch (err) {
    console.error('设置新密码失败', err)
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  router.push('/login')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.identity-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f3f7ff 0%, #f8f9fc 55%, #eef4ff 100%);
  padding: 24px;
}

.identity-card {
  width: 460px;
  max-width: 100%;
  background: #fff;
  border-radius: 18px;
  padding: 36px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.1);
}

.platform-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 999px;
  color: var(--td-brand-color);
  background: var(--td-brand-color-light);
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 14px;
}

h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #111827;
}

.desc {
  margin: 0 0 20px;
  color: #6b7280;
  line-height: 1.7;
  font-size: 14px;
}

.account-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f3f6ff;
  margin-bottom: 20px;
}

.account-box span {
  color: #6b7280;
}

.account-box strong {
  color: #111827;
  word-break: break-all;
  text-align: right;
}

.steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.step {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
}

.step-index {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #e5e7eb;
  color: #6b7280;
  font-size: 13px;
  font-weight: 700;
}

.step strong {
  display: block;
  color: #1f2937;
  margin-bottom: 4px;
}

.step p {
  margin: 0;
  color: #9ca3af;
  font-size: 13px;
}

.step.done .step-index {
  background: #e8f7ef;
  color: #00a870;
}

.step.active {
  border-color: var(--td-brand-color);
  background: #f4f8ff;
}

.step.active .step-index {
  background: var(--td-brand-color);
  color: #fff;
}

.actions {
  margin-top: 14px;
}

.reset-form {
  margin-top: 20px;
}

@media (max-width: 480px) {
  .identity-card {
    padding: 28px 22px;
  }

  .account-box {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .account-box strong {
    text-align: left;
  }
}
</style>
