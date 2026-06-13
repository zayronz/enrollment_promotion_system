<template>
  <div class="h5-profile">
    <H5NavBar title="个人资料" />

    <!-- 顶部用户信息卡片 -->
    <div class="user-header">
      <div class="avatar-section" @click="triggerAvatarUpload">
        <div class="avatar-wrap" :class="{ uploading: avatarUploading }">
          <img v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="avatar-img" />
          <span v-else class="avatar-fallback">{{ userStore.realName?.charAt(0) || 'U' }}</span>
          <div class="avatar-badge">
            <t-icon name="camera" size="14px" />
          </div>
        </div>
        <input ref="avatarInputRef" type="file" accept="image/*" class="hidden-input" @change="handleAvatarUpload" />
      </div>
      <h2 class="user-name">{{ userStore.realName || '用户' }}</h2>
      <div class="user-tags">
        <span class="tag role-tag" :class="roleTagClass">{{ roleLabel }}</span>
        <span v-if="userStore.collegeName" class="tag college-tag">
          <t-icon name="building" size="12px" />
          {{ userStore.collegeName }}
        </span>
      </div>
    </div>

    <!-- 信息展示区域 -->
    <div class="info-section">
      <div class="section-title">基本信息</div>
      <div class="info-list">
        <div class="info-item">
          <div class="item-left">
            <div class="item-icon" style="background: #e0e7ff; color: #4f46e5;">
              <t-icon name="user" size="16px" />
            </div>
            <span class="item-label">账号</span>
          </div>
          <span class="item-value">{{ formData.username }}</span>
        </div>
        <div class="info-item">
          <div class="item-left">
            <div class="item-icon" style="background: #dbeafe; color: #2563eb;">
              <t-icon name="mail" size="16px" />
            </div>
            <span class="item-label">邮箱</span>
          </div>
          <span class="item-value" :class="{ empty: !formData.email }">{{ formData.email || '未设置' }}</span>
        </div>
        <div class="info-item">
          <div class="item-left">
            <div class="item-icon" style="background: #d1fae5; color: #059669;">
              <t-icon name="call" size="16px" />
            </div>
            <span class="item-label">手机</span>
          </div>
          <span class="item-value" :class="{ empty: !formData.phone }">{{ formData.phone || '未设置' }}</span>
        </div>
        <div v-if="isStudent" class="info-item">
          <div class="item-left">
            <div class="item-icon" style="background: #fef3c7; color: #d97706;">
              <t-icon name="education" size="16px" />
            </div>
            <span class="item-label">年级</span>
          </div>
          <span class="item-value" :class="{ empty: !studentGradeText }">{{ studentGradeText || '未设置' }}</span>
        </div>
        <div v-if="isStudent" class="info-item">
          <div class="item-left">
            <div class="item-icon" style="background: #fce7f3; color: #db2777;">
              <t-icon name="chart" size="16px" />
            </div>
            <span class="item-label">绩点</span>
          </div>
          <span class="item-value" :class="{ empty: !studentGpaText }">{{ studentGpaText || '未设置' }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="action-section">
      <div class="section-title">账号安全</div>
      <div class="action-list">
        <div class="action-item" @click="showPasswordDialog = true">
          <div class="action-left">
            <div class="action-icon" style="background: #fef3c7;">
              <t-icon name="lock-on" size="18px" style="color: #d97706;" />
            </div>
            <div class="action-info">
              <span class="action-title">修改密码</span>
              <span class="action-desc">定期更换密码保护账号安全</span>
            </div>
          </div>
          <t-icon name="chevron-right" size="18px" class="action-arrow" />
        </div>
        <div class="action-item" @click="handleLogout">
          <div class="action-left">
            <div class="action-icon" style="background: #fee2e2;">
              <t-icon name="logout" size="18px" style="color: #dc2626;" />
            </div>
            <div class="action-info">
              <span class="action-title" style="color: #dc2626;">退出登录</span>
              <span class="action-desc">退出当前账号</span>
            </div>
          </div>
          <t-icon name="chevron-right" size="18px" class="action-arrow" />
        </div>
      </div>
    </div>

    <!-- 编辑资料浮层 -->
    <t-popup v-model:visible="showEditPopup" placement="bottom" class="edit-popup">
      <div class="popup-header">
        <span class="popup-title">编辑资料</span>
        <t-icon name="close" size="20px" class="popup-close" @click="showEditPopup = false" />
      </div>
      <div class="popup-body">
        <t-form :data="formData" @submit="handleSave">
          <t-form-item label="真实姓名" name="realName">
            <t-input v-model="formData.realName" placeholder="请输入真实姓名" clearable />
          </t-form-item>
          <t-form-item label="邮箱地址" name="email">
            <t-input v-model="formData.email" placeholder="请输入邮箱地址" clearable />
          </t-form-item>
          <t-form-item label="手机号码" name="phone">
            <t-input v-model="formData.phone" placeholder="请输入手机号码" clearable />
          </t-form-item>
          <t-form-item>
            <t-button theme="primary" type="submit" :loading="saving" block size="large">保存修改</t-button>
          </t-form-item>
        </t-form>
      </div>
    </t-popup>

    <!-- 修改密码弹窗 -->
    <t-dialog
      v-model:visible="showPasswordDialog"
      header="修改密码"
      :confirm-btn="{ content: '确认修改', theme: 'primary', loading: passwordSubmitting }"
      :cancel-btn="{ content: '取消' }"
      @confirm="handleChangePassword"
      @close="closePasswordDialog"
    >
      <t-form :data="passwordForm" class="password-form">
        <t-form-item label="当前密码">
          <t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" clearable />
        </t-form-item>
        <t-form-item label="新密码">
          <t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" clearable />
        </t-form-item>
        <t-form-item label="确认新密码">
          <t-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" clearable />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 编辑按钮 -->
    <div class="edit-fab" @click="showEditPopup = true">
      <t-icon name="edit" size="20px" />
      <span>编辑</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { userApi } from '@/api/user'
import { MessagePlugin } from 'tdesign-vue-next'
import H5NavBar from '../components/H5NavBar.vue'
import axios from 'axios'
import { getToken } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const saving = ref(false)
const avatarUploading = ref(false)
const avatarPreviewUrl = ref('')
const avatarInputRef = ref(null)
const showPasswordDialog = ref(false)
const showEditPopup = ref(false)

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

const roleTagClass = computed(() => {
  const map = { STUDENT: 'student', TEACHER: 'teacher', COLLEGE: 'college', SCHOOL: 'school' }
  return map[userStore.role] || 'default'
})

const isStudent = computed(() => userStore.role === 'STUDENT')

const studentGradeText = computed(() => {
  const grade = userStore.userInfo?.grade
  return grade === null || grade === undefined || grade === '' ? '' : String(grade)
})

const studentGpaText = computed(() => {
  const gpa = userStore.userInfo?.gpa
  return gpa === null || gpa === undefined || gpa === '' ? '' : Number(gpa).toFixed(2)
})

const resetForm = () => {
  const info = userStore.userInfo || {}
  formData.username = info.username || ''
  formData.realName = info.realName || ''
  formData.email = info.email || ''
  formData.phone = info.phone || ''
}

const handleSave = async () => {
  if (!formData.realName.trim()) {
    MessagePlugin.warning('请输入真实姓名')
    return
  }
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
    showEditPopup.value = false
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
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    if (res.data?.code === 200 && res.data?.data) {
      const fullAvatarUrl = '/api/file/view/' + res.data.data
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

const handleLogout = () => {
  userStore.logout()
  router.push('/h5/login')
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
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    setTimeout(() => {
      userStore.logout()
      router.push('/h5/login')
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
.h5-profile {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

/* 顶部用户信息 */
.user-header {
  background: linear-gradient(135deg, #0052d9 0%, #1e3a5f 100%);
  padding: 32px 20px 28px;
  text-align: center;
  position: relative;
}
.user-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M30 0L60 30L30 60L0 30z' fill='rgba(255,255,255,0.04)'/%3E%3C/svg%3E") repeat;
}

.avatar-section {
  position: relative;
  display: inline-block;
  margin-bottom: 14px;
}
.avatar-wrap {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.15);
  transition: border-color 0.3s;
}
.avatar-wrap.uploading {
  border-color: rgba(255, 255, 255, 0.6);
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
}
.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  color: #0052d9;
}
.hidden-input {
  display: none;
}

.user-name {
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 10px;
  position: relative;
}
.user-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  position: relative;
}
.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 99px;
  font-size: 12px;
  font-weight: 500;
}
.role-tag.student {
  background: rgba(59, 130, 246, 0.2);
  color: #bfdbfe;
}
.role-tag.teacher {
  background: rgba(245, 158, 11, 0.2);
  color: #fde68a;
}
.role-tag.college {
  background: rgba(16, 185, 129, 0.2);
  color: #a7f3d0;
}
.role-tag.school {
  background: rgba(239, 68, 68, 0.2);
  color: #fecaca;
}
.college-tag {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.9);
}

/* 信息区域 */
.info-section,
.action-section {
  margin: 12px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}
.section-title {
  padding: 16px 16px 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}
.info-list {
  padding: 4px 12px 12px;
}
.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 12px;
  border-radius: 12px;
  transition: background 0.2s;
}
.info-item:active {
  background: #f3f4f6;
}
.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.item-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.item-label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}
.item-value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
  max-width: 50%;
  text-align: right;
  word-break: break-all;
}
.item-value.empty {
  color: #9ca3af;
  font-weight: 400;
}

/* 快捷操作 */
.action-list {
  padding: 4px 12px 12px;
}
.action-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 12px;
  border-radius: 12px;
  transition: background 0.2s;
  cursor: pointer;
}
.action-item:active {
  background: #f3f4f6;
}
.action-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.action-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.action-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.action-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}
.action-desc {
  font-size: 12px;
  color: #9ca3af;
}
.action-arrow {
  color: #d1d5db;
  flex-shrink: 0;
}

/* 编辑浮层 */
.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}
.popup-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}
.popup-close {
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
}
.popup-body {
  padding: 20px;
}

/* 编辑按钮 */
.edit-fab {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 28px;
  background: #0052d9;
  color: #fff;
  border-radius: 99px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(0, 82, 217, 0.35);
  cursor: pointer;
  z-index: 100;
  transition: transform 0.2s, box-shadow 0.2s;
}
.edit-fab:active {
  transform: translateX(-50%) scale(0.96);
  box-shadow: 0 2px 8px rgba(0, 82, 217, 0.25);
}

/* 密码表单 */
.password-form :deep(.t-form__item) {
  margin-bottom: 16px;
}
</style>
