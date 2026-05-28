<template>
  <div class="profile-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">个人资料</h2>
      <span class="page-subtitle">管理与维护您的账户信息</span>
    </div>

    <!-- 用户信息卡片 -->
    <div class="profile-content">
      <!-- 左侧：头像区 -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <t-avatar size="80px" class="avatar">{{ userStore.realName?.charAt(0) || 'U' }}</t-avatar>
          <div class="avatar-name">{{ userStore.realName || '用户' }}</div>
          <t-tag :theme="roleTheme" variant="light-outline" size="medium">{{ roleLabel }}</t-tag>
        </div>
        <div class="sidebar-info">
          <div class="info-item">
            <t-icon name="user" class="info-icon" />
            <span class="info-label">用户名</span>
            <span class="info-value">{{ formData.username || '--' }}</span>
          </div>
          <div class="info-item">
            <t-icon name="mail" class="info-icon" />
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ formData.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <t-icon name="call" class="info-icon" />
            <span class="info-label">手机号</span>
            <span class="info-value">{{ formData.phone || '未设置' }}</span>
          </div>
        </div>
        <div class="sidebar-actions">
          <t-button theme="primary" variant="outline" block @click="goHome">
            <template #icon><HomeIcon /></template>
            回到首页
          </t-button>
          <t-button theme="default" variant="outline" block @click="showPasswordDialog = true" style="margin-top: 10px;">
            <template #icon><LockOnIcon /></template>
            修改密码
          </t-button>
        </div>
      </div>

      <!-- 右侧：编辑表单 -->
      <div class="profile-form-card">
        <div class="form-section">
          <div class="section-header">
            <h3 class="section-title">编辑资料</h3>
          </div>
          <t-form
            ref="formRef"
            :data="formData"
            label-width="80px"
            class="profile-form"
            @submit="handleSave"
          >
            <t-form-item label="用户名">
              <t-input v-model="formData.username" disabled>
                <template #prefix-icon><t-icon name="user" /></template>
              </t-input>
            </t-form-item>
            <t-form-item label="真实姓名" name="realName" :rules="[{ required: true, message: '请输入姓名' }]">
              <t-input v-model="formData.realName" placeholder="请输入真实姓名" clearable>
                <template #prefix-icon><t-icon name="user-circle" /></template>
              </t-input>
            </t-form-item>
            <t-form-item label="邮箱地址" name="email">
              <t-input v-model="formData.email" placeholder="请输入邮箱地址" clearable>
                <template #prefix-icon><t-icon name="mail" /></template>
              </t-input>
            </t-form-item>
            <t-form-item label="手机号码" name="phone">
              <t-input v-model="formData.phone" placeholder="请输入手机号码" clearable>
                <template #prefix-icon><t-icon name="call" /></template>
              </t-input>
            </t-form-item>
            <t-form-item>
              <t-button theme="primary" type="submit" :loading="saving">保存修改</t-button>
              <t-button theme="default" variant="outline" style="margin-left: 12px" @click="resetForm">重置</t-button>
            </t-form-item>
          </t-form>
        </div>
      </div>
    </div>

    <!-- 密码修改弹窗 -->
    <t-dialog
      v-model:visible="showPasswordDialog"
      header="修改密码"
      width="440px"
      :confirm-btn="{ content: '确认修改', theme: 'primary' }"
      @confirm="handlePasswordChange"
    >
      <t-form ref="pwdFormRef" :data="pwdForm" label-width="90px">
        <t-form-item name="oldPassword" label="原密码" :rules="[{ required: true, message: '请输入原密码' }]">
          <t-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" clearable>
            <template #prefix-icon><t-icon name="lock-on" /></template>
          </t-input>
        </t-form-item>
        <t-form-item name="newPassword" label="新密码" :rules="[{ required: true, message: '请输入新密码' }]">
          <t-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" clearable>
            <template #prefix-icon><t-icon name="lock-on" /></template>
          </t-input>
        </t-form-item>
        <t-form-item name="confirmPassword" label="确认密码" :rules="[{ required: true, message: '请确认新密码' }]">
          <t-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" clearable>
            <template #prefix-icon><t-icon name="lock-on" /></template>
          </t-input>
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { userApi } from '@/api/user'
import { MessagePlugin } from 'tdesign-vue-next'
import { LockOnIcon, HomeIcon } from 'tdesign-icons-vue-next'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const pwdFormRef = ref(null)
const showPasswordDialog = ref(false)
const saving = ref(false)

const initData = {
  username: '',
  realName: '',
  email: '',
  phone: ''
}

const formData = reactive({ ...initData })

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleLabel = computed(() => {
  const map = { STUDENT: '学生', TEACHER: '教师', COLLEGE: '学院管理员', SCHOOL: '超级管理员' }
  return map[userStore.role] || userStore.role
})

const roleTheme = computed(() => {
  const map = { STUDENT: 'primary', TEACHER: 'warning', COLLEGE: 'success', SCHOOL: 'danger' }
  return map[userStore.role] || 'default'
})

const resetForm = () => {
  const info = userStore.userInfo || {}
  formData.username = info.username || ''
  formData.realName = info.realName || ''
  formData.email = info.email || ''
  formData.phone = info.phone || ''
}

const handleSave = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  saving.value = true
  try {
    const userInfo = userStore.userInfo
    if (!userInfo?.id) {
      MessagePlugin.warning('无法获取用户信息')
      return
    }
    await userApi.updateUser(userInfo.id, {
      realName: formData.realName,
      email: formData.email,
      phone: formData.phone
    })
    if (userStore.userInfo) {
      userStore.userInfo.realName = formData.realName
      userStore.userInfo.email = formData.email
      userStore.userInfo.phone = formData.phone
    }
    MessagePlugin.success('个人信息更新成功')
  } catch (err) {
    console.error('保存失败', err)
  } finally {
    saving.value = false
  }
}

const handlePasswordChange = async () => {
  const valid = await pwdFormRef.value.validate()
  if (valid !== true) return

  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    MessagePlugin.warning('两次密码输入不一致')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    MessagePlugin.warning('新密码长度不能少于6个字符')
    return
  }
  try {
    await userApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    MessagePlugin.success('密码修改成功')
    showPasswordDialog.value = false
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (err) {
    console.error('密码修改失败', err)
  }
}

const goHome = () => {
  const roleHomeMap = {
    STUDENT: '/student/activities',
    TEACHER: '/teacher/activities',
    COLLEGE: '/college/pending',
    SCHOOL: '/school/dashboard'
  }
  const target = roleHomeMap[userStore.role] || '/'
  router.push(target)
}

onMounted(() => {
  resetForm()
  // 如果路由带 action=password 参数，自动弹出修改密码弹窗
  if (route.query.action === 'password') {
    showPasswordDialog.value = true
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 960px;
  margin: 0 auto;
}

/* ===== 页面标题（重复性：统一标题样式） ===== */
.page-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--td-text-color-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

.page-subtitle {
  font-size: 13px;
  color: var(--td-text-color-placeholder);
  letter-spacing: 0.3px;
}

/* ===== 主内容布局（对齐：左右双栏） ===== */
.profile-content {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
  align-items: start;
}

/* ===== 左侧：用户概览卡片 ===== */
.profile-sidebar {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--td-border-level-1-color);
  overflow: hidden;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 24px 24px;
  text-align: center;
}

.avatar {
  margin-bottom: 12px;
  flex-shrink: 0;
}

.avatar-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin-bottom: 8px;
  word-break: break-all;
}

/* 信息概览列表（重复性：统一条目样式） */
.sidebar-info {
  padding: 0 20px 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--td-bg-color-secondarycontainer);
  transition: background 0.2s;
}

.info-item + .info-item {
  margin-top: 8px;
}

.info-item:hover {
  background: var(--td-bg-color-container-hover);
}

.info-icon {
  font-size: 16px;
  color: var(--td-brand-color);
  margin-right: 10px;
  flex-shrink: 0;
}

.info-label {
  font-size: 13px;
  color: var(--td-text-color-placeholder);
  min-width: 48px;
}

.info-value {
  font-size: 13px;
  color: var(--td-text-color-primary);
  font-weight: 500;
  margin-left: auto;
  text-align: right;
  word-break: break-all;
}

/* 按钮操作区 */
.sidebar-actions {
  padding: 0 20px 24px;
}

/* ===== 右侧：编辑表单卡片 ===== */
.profile-form-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--td-border-level-1-color);
  overflow: hidden;
}

/* 表单分区标题（重复性：统一分区标题样式） */
.section-header {
  padding: 20px 24px 0;
  margin-bottom: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 0;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--td-border-level-1-color);
}

/* 表单内容区 */
.profile-form {
  padding: 16px 24px 24px;
}

/* 统一表单项间距（亲密性：相关字段靠近） */
:deep(.t-form__item) {
  margin-bottom: 20px;
}

/* 输入框统一样式（重复性） */
:deep(.t-input),
:deep(.t-select) {
  border-radius: 8px;
}

/* ===== 对比度强化 ===== */
:deep(.t-input.t-is-disabled) {
  background: var(--td-bg-color-secondarycontainer);
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    order: -1;
  }

  .avatar-section {
    padding: 24px 20px 20px;
    flex-direction: row;
    text-align: left;
  }

  .avatar {
    margin-bottom: 0;
    margin-right: 16px;
  }

  .sidebar-info {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .info-item + .info-item {
    margin-top: 0;
  }

  .info-item {
    padding: 8px 10px;
  }
}
</style>
