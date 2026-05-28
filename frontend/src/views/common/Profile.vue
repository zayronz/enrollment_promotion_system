<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">个人资料</h2>
    </div>

    <div class="profile-card">
      <div class="avatar-section">
        <t-avatar size="72px">{{ userStore.realName?.charAt(0) || 'U' }}</t-avatar>
        <div class="avatar-info">
          <h3>{{ userStore.realName || '用户' }}</h3>
          <t-tag :theme="roleTheme" variant="light" size="small">{{ roleLabel }}</t-tag>
        </div>
      </div>

      <t-form
        ref="formRef"
        :data="formData"
        label-width="90px"
        class="profile-form"
        @submit="handleSave"
      >
        <t-form-item label="用户名">
          <t-input v-model="formData.username" disabled />
        </t-form-item>
        <t-form-item label="真实姓名" name="realName" :rules="[{ required: true, message: '请输入姓名' }]">
          <t-input v-model="formData.realName" placeholder="请输入真实姓名" clearable />
        </t-form-item>
        <t-form-item label="邮箱" name="email">
          <t-input v-model="formData.email" placeholder="请输入邮箱" clearable />
        </t-form-item>
        <t-form-item label="手机号" name="phone">
          <t-input v-model="formData.phone" placeholder="请输入手机号" clearable />
        </t-form-item>
        <t-form-item>
          <t-space>
            <t-button theme="primary" type="submit">保存</t-button>
            <t-button theme="default" variant="outline" @click="showPasswordDialog = true">
              修改密码
            </t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </div>

    <!-- Password dialog -->
    <t-dialog
      v-model:visible="showPasswordDialog"
      header="修改密码"
      width="400px"
      :confirm-btn="{ content: '确认修改', theme: 'primary' }"
      @confirm="handlePasswordChange"
    >
      <t-form ref="pwdFormRef" :data="pwdForm" label-width="100px">
        <t-form-item name="oldPassword" label="原密码" :rules="[{ required: true, message: '请输入原密码' }]">
          <t-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" clearable />
        </t-form-item>
        <t-form-item name="newPassword" label="新密码" :rules="[{ required: true, message: '请输入新密码' }]">
          <t-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" clearable />
        </t-form-item>
        <t-form-item name="confirmPassword" label="确认密码" :rules="[{ required: true, message: '请确认新密码' }]">
          <t-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" clearable />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { MessagePlugin } from 'tdesign-vue-next'

const userStore = useUserStore()
const formRef = ref(null)
const pwdFormRef = ref(null)
const showPasswordDialog = ref(false)

const formData = reactive({
  username: '',
  realName: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleLabel = computed(() => {
  const map = { STUDENT: '学生', TEACHER: '教师', COLLEGE: '学院', SCHOOL: '管理员' }
  return map[userStore.role] || userStore.role
})

const roleTheme = computed(() => {
  const map = { STUDENT: 'primary', TEACHER: 'warning', COLLEGE: 'success', SCHOOL: 'danger' }
  return map[userStore.role] || 'default'
})

const handleSave = async () => {
  // TODO: API call
  MessagePlugin.success('保存成功')
}

const handlePasswordChange = async () => {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    MessagePlugin.warning('两次密码输入不一致')
    return
  }
  // TODO: API call
  MessagePlugin.success('密码修改成功')
  showPasswordDialog.value = false
}

onMounted(() => {
  const info = userStore.userInfo || {}
  formData.username = info.username || ''
  formData.realName = info.realName || ''
  formData.email = info.email || ''
  formData.phone = info.phone || ''
})
</script>

<style scoped>
.profile-page {
  padding: 0;
}
.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 0;
}
.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  border: 1px solid var(--td-border-level-1-color);
  max-width: 600px;
}
.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--td-border-level-1-color);
}
.avatar-info h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin-bottom: 4px;
}
.profile-form {
  margin-top: 8px;
}

@media (max-width: 640px) {
  .profile-card {
    padding: 20px;
  }
}
</style>
