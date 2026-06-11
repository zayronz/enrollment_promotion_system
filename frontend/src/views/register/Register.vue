<template>
  <div class="register-wrapper">
    <div class="register-card">
      <div class="register-header">
        <div class="header-icon-box">
          <UserAddIcon class="header-icon" />
        </div>
        <h2 class="header-title">创建账号</h2>
        <p class="header-desc">加入招生宣传报名系统</p>
      </div>

      <t-form :data="form" :rules="rules" ref="formRef" label-width="0" @submit="handleRegister">
        <!-- 角色选择 -->
        <div class="role-selector">
          <div
            class="role-option"
            :class="{ active: form.role === 'STUDENT' }"
            @click="form.role = 'STUDENT'"
          >
            <t-icon name="user" class="role-option-icon" />
            <span class="role-option-label">学生注册</span>
          </div>
          <div
            class="role-option"
            :class="{ active: form.role === 'TEACHER' }"
            @click="form.role = 'TEACHER'"
          >
            <t-icon name="user-business" class="role-option-icon" />
            <span class="role-option-label">教师注册</span>
          </div>
        </div>

        <!-- 学生专属字段 -->
        <template v-if="form.role === 'STUDENT'">
          <t-form-item name="username">
            <t-input v-model="form.username" placeholder="请输入学号" size="large" clearable>
              <template #prefix-icon><t-icon name="user" /></template>
            </t-input>
          </t-form-item>
          <t-form-item name="collegeName">
            <t-auto-complete
              v-model="form.collegeName"
              :options="collegeOptions"
              placeholder="请输入或选择学院"
              size="large"
              clearable
              :loading="collegeLoading"
            />
          </t-form-item>
          <t-row :gutter="16">
            <t-col :span="6">
              <t-form-item name="grade">
                <t-select v-model="form.grade" placeholder="请选择年级" size="large" clearable>
                  <t-option v-for="grade in gradeOptions" :key="grade.value" :label="grade.label" :value="grade.value" />
                </t-select>
              </t-form-item>
            </t-col>
            <t-col :span="6">
              <t-form-item name="gpa">
                <t-input v-model="form.gpa" type="number" placeholder="请输入绩点" size="large" clearable />
              </t-form-item>
            </t-col>
          </t-row>
        </template>

        <!-- 教师专属字段 -->
        <template v-if="form.role === 'TEACHER'">
          <t-form-item name="username">
            <t-input v-model="form.username" placeholder="请输入工号" size="large" clearable>
              <template #prefix-icon><t-icon name="user" /></template>
            </t-input>
          </t-form-item>
          <t-form-item name="collegeName">
            <t-auto-complete
              v-model="form.collegeName"
              :options="collegeOptions"
              placeholder="请输入或选择所属学院"
              size="large"
              clearable
              :loading="collegeLoading"
            />
          </t-form-item>
        </template>

        <!-- 公共字段 -->
        <t-form-item name="realName">
          <t-input v-model="form.realName" placeholder="请输入真实姓名" size="large" clearable>
            <template #prefix-icon><t-icon name="user-circle" /></template>
          </t-input>
        </t-form-item>

        <t-form-item name="password">
          <t-input v-model="form.password" type="password" placeholder="请输入密码（至少6位）" size="large" clearable>
            <template #prefix-icon><t-icon name="lock-on" /></template>
          </t-input>
        </t-form-item>

        <t-form-item name="confirmPassword">
          <t-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" size="large" clearable>
            <template #prefix-icon><t-icon name="lock-on" /></template>
          </t-input>
        </t-form-item>

        <t-form-item name="email">
          <t-input v-model="form.email" placeholder="请输入邮箱（选填）" size="large" clearable>
            <template #prefix-icon><t-icon name="mail" /></template>
          </t-input>
        </t-form-item>

        <t-form-item name="phone">
          <t-input v-model="form.phone" placeholder="请输入手机号（选填）" size="large" clearable>
            <template #prefix-icon><t-icon name="call" /></template>
          </t-input>
        </t-form-item>

        <t-form-item>
          <t-button theme="primary" size="large" block type="submit" :loading="loading" class="submit-btn">
            注 册
          </t-button>
        </t-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <t-link theme="primary" hover="color" @click="goToLogin">立即登录</t-link>
        </div>
      </t-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { UserAddIcon } from 'tdesign-icons-vue-next'
import { userApi } from '@/api/user'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const collegeLoading = ref(false)
const colleges = ref([])
const collegeOptions = computed(() =>
  colleges.value.map(college => ({
    label: college.name,
    value: college.name
  }))
)
const gradeOptions = [
  { label: '大一', value: 1 },
  { label: '大二', value: 2 },
  { label: '大三', value: 3 },
  { label: '大四', value: 4 },
  { label: '大五', value: 5 }
]

const form = reactive({
  role: 'STUDENT',
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  collegeId: null,
  collegeName: '',
  grade: null,
  gpa: null
})

// 校验手机号
const validatePhone = (val) => {
  if (!val) return { result: true }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(val)) {
    return { result: false, message: '请输入正确的手机号码', type: 'error' }
  }
  return { result: true }
}

// 校验邮箱
const validateEmail = (val) => {
  if (!val) return { result: true }
  const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailReg.test(val)) {
    return { result: false, message: '请输入正确的邮箱地址', type: 'error' }
  }
  return { result: true }
}

// 校验确认密码
const validateConfirmPassword = (val) => {
  if (val !== form.password) {
    return { result: false, message: '两次输入的密码不一致', type: 'error' }
  }
  return { result: true }
}

// 校验绩点
const validateGpa = (val) => {
  if (form.role !== 'STUDENT') return { result: true }
  if (val === '' || val === null || val === undefined) {
    return { result: false, message: '请输入绩点', type: 'error' }
  }
  const num = Number(val)
  if (Number.isNaN(num) || num < 0 || num > 5) {
    return { result: false, message: '绩点范围应为 0-5', type: 'error' }
  }
  return { result: true }
}

// 动态校验规则：教师不需要 grade/gpa
const rules = computed(() => {
  const baseCommon = {
    role: [{ required: true, message: '请选择用户类型', type: 'error' }],
    username: [
      { required: true, message: form.role === 'STUDENT' ? '请输入学号' : '请输入工号', type: 'error' },
      { min: 6, message: '长度不能少于6个字符', type: 'error' },
      { max: 20, message: '长度不能超过20个字符', type: 'error' }
    ],
    realName: [
      { required: true, message: '请输入真实姓名', type: 'error' },
      { min: 2, message: '姓名至少2个字符', type: 'error' },
      { max: 20, message: '姓名不能超过20个字符', type: 'error' }
    ],
    password: [
      { required: true, message: '请输入密码', type: 'error' },
      { min: 6, message: '密码长度不能少于6个字符', type: 'error' },
      { max: 20, message: '密码长度不能超过20个字符', type: 'error' }
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', type: 'error' },
      { validator: validateConfirmPassword }
    ],
    email: [
      { email: true, message: '请输入正确的邮箱地址', type: 'error' },
      { validator: validateEmail }
    ],
    phone: [{ validator: validatePhone }],
    collegeName: [{ required: true, message: '请输入或选择学院', type: 'error' }]
  }

  if (form.role === 'STUDENT') {
    return {
      ...baseCommon,
      grade: [{ required: true, message: '请输入年级', type: 'error' }],
      gpa: [{ validator: validateGpa }]
    }
  }
  return baseCommon
})

// 获取学院列表
const fetchColleges = async () => {
  collegeLoading.value = true
  try {
    const res = await request.get('/college/list')
    if (res.code === 200) {
      colleges.value = res.data || []
    }
  } catch (error) {
    console.error('获取学院列表失败', error)
    MessagePlugin.warning('学院列表加载失败，请刷新页面重试')
  } finally {
    collegeLoading.value = false
  }
}

// 角色切换时清除校验状态
watch(() => form.role, () => {
  formRef.value?.clearValidate()
  // 切换角色时清空学号/工号
  form.username = ''
  form.grade = null
  form.gpa = null
})

watch(() => form.collegeName, (name) => {
  const matchedCollege = colleges.value.find(college => college.name === String(name || '').trim())
  form.collegeId = matchedCollege ? matchedCollege.id : null
})

// 注册
const handleRegister = async ({ validateResult, firstError }) => {
  if (validateResult !== true) {
    MessagePlugin.warning(firstError || '请完善表单信息')
    return
  }

  loading.value = true
  try {
    const collegeId = await resolveCollegeId()
    const submitData = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      role: form.role,
      collegeId,
      collegeName: form.collegeName.trim()
    }
    // 学生额外提交 grade 和 gpa
    if (form.role === 'STUDENT') {
      submitData.grade = Number(form.grade)
      submitData.gpa = Number(form.gpa)
    }

    const res = await userApi.register(submitData)
    if (res.code === 200) {
      MessagePlugin.success('注册成功，即将跳转登录页')
      setTimeout(() => {
        router.push(route.path.startsWith('/h5') ? '/h5/login' : '/login')
      }, 1500)
    }
  } catch (error) {
    // 响应拦截器已显示错误提示，此处仅补充兜底
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}

const resolveCollegeId = async () => {
  const collegeName = form.collegeName.trim()
  if (!collegeName) {
    throw new Error('学院不能为空')
  }

  const matchedCollege = colleges.value.find(college => college.name === collegeName)
  if (matchedCollege) {
    form.collegeId = matchedCollege.id
    return Number(matchedCollege.id)
  }

  const res = await request.post('/college/resolve', { name: collegeName })
  const college = res.data
  if (!college?.id) {
    throw new Error('学院信息解析失败')
  }
  form.collegeId = college.id
  if (!colleges.value.some(item => item.id === college.id)) {
    colleges.value.push(college)
  }
  return Number(college.id)
}

const goToLogin = () => {
  router.push(route.path.startsWith('/h5') ? '/h5/login' : '/login')
}

onMounted(() => {
  fetchColleges()
})
</script>

<style scoped>
.register-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8eeff 30%, #dce6ff 60%, #f0f4ff 100%);
  padding: 24px 20px;
}

.register-card {
  width: 480px;
  padding: 40px 40px 32px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 82, 217, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--td-brand-color-6), var(--td-brand-color-7));
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 16px rgba(0, 82, 217, 0.2);
}

.header-icon {
  font-size: 28px;
  color: #fff;
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--td-text-color-primary);
  margin: 0 0 8px;
  letter-spacing: 1px;
}

.header-desc {
  font-size: 14px;
  color: var(--td-text-color-secondary);
  margin: 0;
  letter-spacing: 0.5px;
}

/* 角色选择器 */
.role-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 24px;
}

.role-option {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 2px solid var(--td-border-level-1-color);
  background: var(--td-bg-color-secondarycontainer);
  cursor: pointer;
  transition: all 0.25s ease;
  -webkit-user-select: none;
  user-select: none;
}

.role-option:hover {
  border-color: var(--td-brand-color-4);
  background: var(--td-brand-color-light);
}

.role-option.active {
  border-color: var(--td-brand-color);
  background: var(--td-brand-color-light);
  box-shadow: 0 0 0 3px var(--td-brand-color-1);
}

.role-option-icon {
  font-size: 18px;
  color: var(--td-text-color-secondary);
  transition: color 0.25s;
}

.role-option.active .role-option-icon {
  color: var(--td-brand-color);
}

.role-option-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--td-text-color-secondary);
  transition: color 0.25s;
}

.role-option.active .role-option-label {
  color: var(--td-brand-color);
}

/* 表单间距 */
:deep(.t-form__item) {
  margin-bottom: 20px;
}

:deep(.t-input) {
  border-radius: 8px;
}

:deep(.t-select) {
  border-radius: 8px;
}

.submit-btn {
  margin-top: 4px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  height: 48px;
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--td-border-level-1-color);
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.register-footer span {
  margin-right: 4px;
}

@media (max-width: 520px) {
  .register-card {
    width: 100%;
    padding: 28px 20px 24px;
  }
}
</style>
