<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>加入招生宣传报名系统</p>
      </div>

      <t-form :data="form" :rules="rules" ref="formRef" label-width="100px" @submit="handleRegister">
        <!-- 角色选择 -->
        <t-form-item label="用户类型" name="role">
          <t-radio-group v-model="form.role">
            <t-radio value="STUDENT">学生</t-radio>
            <t-radio value="TEACHER">教师</t-radio>
          </t-radio-group>
        </t-form-item>

        <!-- 学生专属字段 -->
        <template v-if="form.role === 'STUDENT'">
          <t-form-item label="学号" name="username">
            <t-input v-model="form.username" placeholder="请输入学号">
              <template #prefix-icon><t-icon name="user" /></template>
            </t-input>
          </t-form-item>
          <t-form-item label="学院" name="collegeId">
            <t-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
              <t-option
                v-for="college in colleges"
                :key="college.id"
                :label="college.name"
                :value="college.id"
              />
            </t-select>
          </t-form-item>
          <t-form-item label="年级" name="grade">
            <t-input-number v-model="form.grade" :min="1" :max="5" placeholder="年级" theme="normal" style="width: 100%" />
          </t-form-item>
          <t-form-item label="绩点" name="gpa">
            <t-input-number v-model="form.gpa" :min="0" :max="5" :decimal-places="2" step="0.1" placeholder="绩点" theme="normal" style="width: 100%" />
          </t-form-item>
        </template>

        <!-- 教师专属字段 -->
        <template v-if="form.role === 'TEACHER'">
          <t-form-item label="工号" name="username">
            <t-input v-model="form.username" placeholder="请输入工号">
              <template #prefix-icon><t-icon name="user" /></template>
            </t-input>
          </t-form-item>
          <t-form-item label="所属学院" name="collegeId">
            <t-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
              <t-option
                v-for="college in colleges"
                :key="college.id"
                :label="college.name"
                :value="college.id"
              />
            </t-select>
          </t-form-item>
        </template>

        <!-- 公共字段 -->
        <t-form-item label="姓名" name="realName">
          <t-input v-model="form.realName" placeholder="请输入真实姓名">
            <template #prefix-icon><t-icon name="user" /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="密码" name="password">
          <t-input v-model="form.password" type="password" placeholder="请输入密码（至少6位）">
            <template #prefix-icon><t-icon name="lock-on" /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="确认密码" name="confirmPassword">
          <t-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码">
            <template #prefix-icon><t-icon name="lock-on" /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="邮箱" name="email">
          <t-input v-model="form.email" placeholder="请输入邮箱">
            <template #prefix-icon><t-icon name="mail" /></template>
          </t-input>
        </t-form-item>

        <t-form-item label="手机号" name="phone">
          <t-input v-model="form.phone" placeholder="请输入手机号">
            <template #prefix-icon><t-icon name="call" /></template>
          </t-input>
        </t-form-item>

        <t-form-item>
          <t-button theme="primary" size="large" block type="submit" :loading="loading">
            注 册
          </t-button>
        </t-form-item>

        <div class="register-footer">
          已有账号？
          <t-link theme="primary" @click="goToLogin">立即登录</t-link>
        </div>
      </t-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const colleges = ref([])

const form = reactive({
  role: 'STUDENT',
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  collegeId: null,
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

// 表单校验规则 (TDesign 校验规则格式)
const rules = {
  role: [{ required: true, message: '请选择用户类型', type: 'error' }],
  username: [
    { required: true, message: '请输入学号/工号', type: 'error' },
    { min: 6, message: '长度不能少于6个字符', type: 'error' },
    { max: 20, message: '长度不能超过20个字符', type: 'error' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', type: 'error' },
    { min: 2, message: '长度不能少于2个字符', type: 'error' },
    { max: 20, message: '长度不能超过20个字符', type: 'error' }
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
  collegeId: [{ required: true, message: '请选择学院', type: 'error' }],
  grade: [{ required: true, message: '请输入年级', type: 'error' }],
  gpa: [{ required: true, message: '请输入绩点', type: 'error' }]
}

// 获取学院列表
const fetchColleges = async () => {
  try {
    const res = await request.get('/college/list')
    if (res.code === 200) {
      colleges.value = res.data || []
    }
  } catch (error) {
    console.error('获取学院列表失败', error)
  }
}

// 注册
const handleRegister = async ({ validateResult, firstError }) => {
  if (validateResult !== true) {
    MessagePlugin.warning(firstError || '请完善表单信息')
    return
  }

  loading.value = true
  try {
    const submitData = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      role: form.role,
      collegeId: form.collegeId,
      grade: form.grade,
      gpa: form.gpa
    }

    const res = await request.post('/user/register', submitData)
    if (res.code === 200) {
      MessagePlugin.success('注册成功，请登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      MessagePlugin.error(res.message || '注册失败')
    }
  } catch (error) {
    MessagePlugin.error(error.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  fetchColleges()
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}
.register-card {
  width: 550px;
  padding: 32px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
.register-header {
  text-align: center;
  margin-bottom: 32px;
}
.register-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}
.register-header p {
  color: #666;
  font-size: 14px;
}
.register-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  font-size: 14px;
}
</style>
