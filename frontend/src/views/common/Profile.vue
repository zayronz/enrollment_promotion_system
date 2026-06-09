<template>
  <div class="profile-page">
    <!-- 顶部渐变横幅 -->
    <div class="profile-banner">
      <div class="banner-overlay">
        <div class="avatar-wrapper" @click="triggerAvatarUpload">
          <div class="avatar-ring" :class="{ uploading: avatarUploading }">
            <t-avatar size="96px" class="avatar-main">
              <template v-if="avatarPreviewUrl">
                <img :src="avatarPreviewUrl" alt="头像" class="avatar-img" />
              </template>
              <template v-else>
                <span class="avatar-text">{{ userStore.realName?.charAt(0) || 'U' }}</span>
              </template>
            </t-avatar>
            <div class="avatar-upload-mask">
              <t-icon name="camera" size="22px" />
              <span class="mask-text">更换头像</span>
            </div>
          </div>
          <input
            ref="avatarInputRef"
            type="file"
            accept="image/*"
            class="avatar-file-input"
            @change="handleAvatarUpload"
          />
        </div>
        <h2 class="banner-name">{{ userStore.realName || '用户' }}</h2>
        <div class="banner-meta">
          <t-tag :theme="roleTheme" variant="light" size="medium" class="role-tag">{{ roleLabel }}</t-tag>
          <span class="meta-divider">·</span>
          <span class="meta-text" v-if="userStore.collegeName">
            <t-icon name="building" size="14px" />
            {{ userStore.collegeName }}
          </span>
          <span class="meta-text">
            <t-icon name="user" size="14px" />
            {{ formData.username }}
          </span>
        </div>
      </div>
    </div>

    <!-- 信息卡片网格 -->
    <div class="profile-content">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <t-icon name="user-circle" size="18px" class="card-icon" />
          <span>基本信息</span>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="info-label">邮箱地址</span>
            <span class="info-value" :class="{ empty: !formData.email }">{{ formData.email || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">手机号码</span>
            <span class="info-value" :class="{ empty: !formData.phone }">{{ formData.phone || '未设置' }}</span>
          </div>
          <div class="info-row" v-if="userStore.collegeName">
            <span class="info-label">所属学院</span>
            <span class="info-value">{{ userStore.collegeName }}</span>
          </div>
        </div>
      </div>

      <!-- 编辑资料卡片 -->
      <div class="info-card edit-card">
        <div class="card-header">
          <t-icon name="edit" size="18px" class="card-icon" />
          <span>编辑资料</span>
        </div>
        <div class="card-body">
          <t-form
            ref="formRef"
            :data="formData"
            label-width="80px"
            class="profile-form"
            @submit="handleSave"
          >
            <t-form-item label="真实姓名" name="realName" :rules="[{ required: true, message: '请输入姓名' }]">
              <t-input v-model="formData.realName" placeholder="请输入真实姓名" clearable size="large">
                <template #prefix-icon><t-icon name="user" /></template>
              </t-input>
            </t-form-item>
            <t-form-item label="邮箱地址" name="email">
              <t-input v-model="formData.email" placeholder="请输入邮箱地址" clearable size="large">
                <template #prefix-icon><t-icon name="mail" /></template>
              </t-input>
            </t-form-item>
            <t-form-item label="手机号码" name="phone">
              <t-input v-model="formData.phone" placeholder="请输入手机号码" clearable size="large">
                <template #prefix-icon><t-icon name="call" /></template>
              </t-input>
            </t-form-item>
            <t-form-item class="form-actions">
              <t-button theme="primary" type="submit" :loading="saving" size="large">
                <template #icon><t-icon name="check" /></template>
                保存修改
              </t-button>
              <t-button theme="default" variant="outline" size="large" style="margin-left: 12px" @click="resetForm">
                重置
              </t-button>
            </t-form-item>
          </t-form>
        </div>
      </div>

      <!-- 快捷操作卡片 -->
      <div class="info-card action-card">
        <div class="card-header">
          <t-icon name="tools" size="18px" class="card-icon" />
          <span>快捷操作</span>
        </div>
        <div class="card-body">
          <div class="action-list">
            <div class="action-item" @click="goHome">
              <div class="action-icon"><HomeIcon /></div>
              <div class="action-text">
                <span class="action-title">回到首页</span>
                <span class="action-desc">返回功能主页</span>
              </div>
              <t-icon name="chevron-right" size="16px" class="action-arrow" />
            </div>
            <div class="action-item" @click="showPasswordDialog = true">
              <div class="action-icon"><LockOnIcon /></div>
              <div class="action-text">
                <span class="action-title">修改密码</span>
                <span class="action-desc">更新登录密码</span>
              </div>
              <t-icon name="chevron-right" size="16px" class="action-arrow" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <t-dialog
      v-model:visible="showPasswordDialog"
      header="修改密码"
      width="420px"
      :confirm-btn="{ content: '确认修改', theme: 'primary', loading: passwordSubmitting }"
      :cancel-btn="{ content: '取消' }"
      @confirm="handleChangePassword"
      @close="closePasswordDialog"
    >
      <t-form :data="passwordForm" label-width="100px" class="password-form">
        <t-form-item label="当前密码">
          <t-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            clearable
          />
        </t-form-item>
        <t-form-item label="新密码">
          <t-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            clearable
          />
        </t-form-item>
        <t-form-item label="确认新密码">
          <t-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            clearable
          />
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
import { HomeIcon, LockOnIcon } from 'tdesign-icons-vue-next'
import axios from 'axios'
import { getToken } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const saving = ref(false)
const avatarUploading = ref(false)
const avatarPreviewUrl = ref('')
const avatarInputRef = ref(null)
const showPasswordDialog = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordSubmitting = ref(false)

const initData = {
  username: '',
  realName: '',
  email: '',
  phone: ''
}

const formData = reactive({ ...initData })

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

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    MessagePlugin.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    MessagePlugin.warning('头像图片不能超过5MB')
    return
  }

  const localUrl = URL.createObjectURL(file)
  avatarPreviewUrl.value = localUrl
  avatarUploading.value = true

  try {
    const uploadData = new FormData()
    uploadData.append('file', file)
    const res = await axios.post('/api/file/upload', uploadData, {
      headers: {
        'Authorization': `Bearer ${getToken()}`
      }
    })
    if (res.data?.code === 200 && res.data?.data) {
      const avatarUrl = res.data.data
      // 头像URL需要拼接静态资源访问前缀
      const fullAvatarUrl = '/api/file/view/' + avatarUrl
      await userStore.updateAvatar(fullAvatarUrl)
      avatarPreviewUrl.value = fullAvatarUrl
      MessagePlugin.success('头像更新成功')
    } else {
      MessagePlugin.error('头像上传失败')
      avatarPreviewUrl.value = userStore.avatarUrl || ''
    }
  } catch (err) {
    console.error('头像上传失败', err)
    MessagePlugin.error('头像上传失败')
    avatarPreviewUrl.value = userStore.avatarUrl || ''
  } finally {
    avatarUploading.value = false
    avatarInputRef.value.value = ''
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

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    MessagePlugin.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    MessagePlugin.warning('两次输入的新密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    MessagePlugin.warning('新密码长度不能少于6位')
    return
  }

  passwordSubmitting.value = true
  try {
    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    MessagePlugin.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 退出登录
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } catch (err) {
    console.error('修改密码失败', err)
    MessagePlugin.error(err.response?.data?.message || '修改密码失败')
  } finally {
    passwordSubmitting.value = false
  }
}

const closePasswordDialog = () => {
  showPasswordDialog.value = false
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

onMounted(() => {
  resetForm()
  if (userStore.avatarUrl) {
    avatarPreviewUrl.value = userStore.avatarUrl
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
}

/* ===== 顶部横幅 ===== */
.profile-banner {
  background: linear-gradient(135deg, #0052d9 0%, #1e3a5f 100%);
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 24px;
  position: relative;
}
.profile-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M30 0L60 30L30 60L0 30z' fill='rgba(255,255,255,0.03)'/%3E%3C/svg%3E") repeat;
  opacity: 0.5;
}
.banner-overlay {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 24px 32px;
  text-align: center;
}

/* 头像 */
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  margin-bottom: 16px;
}
.avatar-ring {
  position: relative;
  padding: 3px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  transition: background 0.3s;
}
.avatar-ring:hover,
.avatar-ring.uploading {
  background: rgba(255, 255, 255, 0.45);
}
.avatar-main {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.avatar-main :deep(.t-avatar) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 600;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.avatar-text {
  font-size: 36px;
  color: #fff;
}
.avatar-upload-mask {
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
  gap: 4px;
}
.avatar-ring:hover .avatar-upload-mask {
  opacity: 1;
}
.mask-text {
  font-size: 11px;
  letter-spacing: 0.5px;
}
.avatar-file-input {
  display: none;
}

/* 横幅文字 */
.banner-name {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: 0.5px;
}
.banner-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}
.role-tag {
  font-weight: 600;
}
.meta-divider {
  color: rgba(255, 255, 255, 0.5);
  font-size: 16px;
}
.meta-text {
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* ===== 内容网格 ===== */
.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* ===== 卡片通用 ===== */
.info-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid var(--td-border-level-1-color);
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s;
}
.info-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.edit-card {
  grid-row: span 2;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 24px;
  border-bottom: 1px solid var(--td-border-level-1-color);
  font-size: 15px;
  font-weight: 600;
  color: var(--td-text-color-primary);
}
.card-icon {
  color: var(--td-brand-color);
}
.card-body {
  padding: 20px 24px;
}

/* 信息行 */
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--td-bg-color-secondarycontainer);
  transition: background 0.2s;
}
.info-row + .info-row {
  margin-top: 8px;
}
.info-row:hover {
  background: var(--td-bg-color-container-hover);
}
.info-label {
  font-size: 13px;
  color: var(--td-text-color-placeholder);
  font-weight: 500;
}
.info-value {
  font-size: 13px;
  color: var(--td-text-color-primary);
  font-weight: 500;
  text-align: right;
  word-break: break-all;
}
.info-value.empty {
  color: var(--td-text-color-placeholder);
  font-weight: 400;
}

/* 快捷操作 */
.action-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.action-item:hover {
  background: var(--td-bg-color-container-hover);
}
.action-item:hover .action-arrow {
  transform: translateX(3px);
}
.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--td-bg-color-secondarycontainer);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--td-brand-color);
  flex-shrink: 0;
  transition: background 0.2s, color 0.2s;
}
.action-item:hover .action-icon {
  background: var(--td-brand-color-light);
  color: var(--td-brand-color);
}
.action-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.action-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--td-text-color-primary);
}
.action-desc {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}
.action-arrow {
  color: var(--td-text-color-placeholder);
  transition: transform 0.2s;
}

/* 表单 */
.profile-form {
  padding: 0;
}
.form-actions {
  margin-bottom: 0;
  padding-top: 8px;
}
.form-actions :deep(.t-form__controls) {
  margin-left: 0;
}

::deep(.t-form__item) {
  margin-bottom: 22px;
}
::deep(.t-input) {
  border-radius: 8px;
}
::deep(.t-input.t-is-disabled) {
  background: var(--td-bg-color-secondarycontainer);
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
  .edit-card {
    grid-row: auto;
  }
  .profile-banner .banner-overlay {
    padding: 28px 20px 24px;
  }
  .banner-name {
    font-size: 19px;
  }
}
</style>
