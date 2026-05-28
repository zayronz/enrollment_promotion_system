<template>
  <div class="home-page">
    <header class="header">
      <div class="container">
        <h1 class="logo">2024年高校招生宣传系统</h1>
        <nav class="nav">
          <a href="#activities">宣传活动</a>
          <a href="#enroll">在线报名</a>
          <a href="#contact">联系我们</a>
        </nav>
      </div>
    </header>

    <section class="banner-section">
      <div class="banner-carousel">
        <el-carousel height="500px" :interval="5000" arrow="always">
          <el-carousel-item v-for="banner in banners" :key="banner.id">
            <div class="banner-item" :style="{ backgroundImage: `url(${banner.imageUrl || '/banner-bg.jpg'})` }">
              <div class="banner-content">
                <h2>{{ banner.title }}</h2>
                <p v-if="banner.linkUrl">点击了解详情</p>
                <el-button type="primary" size="large" @click="scrollToEnroll">立即报名</el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <section id="activities" class="activities-section">
      <div class="container">
        <h2 class="section-title">热门活动</h2>
        <div class="activities-grid">
          <div v-for="activity in activities" :key="activity.id" class="activity-card">
            <div class="activity-image">
              <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=college%20enrollment%20event%20banner%20with%20students&image_size=landscape_4_3" alt="活动图片" />
            </div>
            <div class="activity-content">
              <h3>{{ activity.title }}</h3>
              <p class="activity-desc">{{ activity.description || '精彩活动，等你来参加！' }}</p>
              <div class="activity-info">
                <span><i class="el-icon-time"></i> {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</span>
              </div>
              <div class="activity-actions">
                <el-button type="primary" @click="showEnrollForm(activity)">立即报名</el-button>
                <el-button @click="viewDetails(activity)">查看详情</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="enroll" class="enroll-section">
      <div class="container">
        <h2 class="section-title">在线报名</h2>
        <div class="enroll-form-wrapper">
          <el-form ref="enrollFormRef" :model="enrollForm" :rules="enrollRules" label-width="100px" class="enroll-form">
            <el-form-item label="活动名称" v-if="selectedActivity">
              <span class="activity-name">{{ selectedActivity.title }}</span>
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="enrollForm.realName" placeholder="请输入您的真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="enrollForm.phone" placeholder="请输入手机号码" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="enrollForm.email" placeholder="请输入电子邮箱" />
            </el-form-item>
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="enrollForm.studentNo" placeholder="请输入学号（可选）" />
            </el-form-item>
            <el-form-item label="所在院校">
              <el-input v-model="enrollForm.collegeName" placeholder="请输入所在学校/学院" />
            </el-form-item>
            <el-form-item label="专业">
              <el-input v-model="enrollForm.major" placeholder="请输入所学专业" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="enrollForm.intro" type="textarea" :rows="3" placeholder="简单介绍一下自己（可选）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" @click="submitEnrollment" :loading="submitting">
                提交报名
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </section>

    <section id="contact" class="contact-section">
      <div class="container">
        <h2 class="section-title">联系我们</h2>
        <div class="contact-info">
          <div class="contact-item">
            <i class="el-icon-phone"></i>
            <p>招生热线：400-888-8888</p>
          </div>
          <div class="contact-item">
            <i class="el-icon-message"></i>
            <p>邮箱：zs@edu.cn</p>
          </div>
          <div class="contact-item">
            <i class="el-icon-location"></i>
            <p>地址：北京市海淀区中关村大街1号</p>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 高校招生宣传系统 版权所有</p>
      </div>
    </footer>

    <el-dialog title="活动详情" :visible.sync="showDetailsDialog" width="600px">
      <div v-if="detailActivity" class="activity-details">
        <h2>{{ detailActivity.title }}</h2>
        <div class="detail-info">
          <p><strong>活动时间：</strong>{{ formatTime(detailActivity.startTime) }} - {{ formatTime(detailActivity.endTime) }}</p>
          <p><strong>活动地点：</strong>线上/线下同步进行</p>
          <p><strong>活动简介：</strong></p>
          <div class="detail-content">{{ detailActivity.description || '精彩活动，等你来参加！' }}</div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="showDetailsDialog = false">关闭</el-button>
        <el-button type="primary" @click="enrollFromDetail">立即报名</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'

const banners = ref([])
const activities = ref([])
const selectedActivity = ref(null)
const showDetailsDialog = ref(false)
const detailActivity = ref(null)
const submitting = ref(false)

const enrollFormRef = ref(null)
const enrollForm = ref({
  activityId: null,
  activityTitle: '',
  realName: '',
  phone: '',
  email: '',
  studentNo: '',
  collegeName: '',
  major: '',
  intro: ''
})

const enrollRules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

onMounted(() => {
  fetchBanners()
  fetchActivities()
})

const fetchBanners = async () => {
  try {
    const response = await axios.get('/banners')
    if (response.data && response.data.length > 0) {
      banners.value = response.data
    } else {
      banners.value = [
        { id: 1, title: '欢迎参加2024年高校招生宣传活动', imageUrl: '' },
        { id: 2, title: '精彩讲座，名师面对面', imageUrl: '' },
        { id: 3, title: '校园开放日，期待您的到来', imageUrl: '' }
      ]
    }
  } catch (error) {
    banners.value = [
      { id: 1, title: '欢迎参加2024年高校招生宣传活动', imageUrl: '' },
      { id: 2, title: '精彩讲座，名师面对面', imageUrl: '' },
      { id: 3, title: '校园开放日，期待您的到来', imageUrl: '' }
    ]
  }
}

const fetchActivities = async () => {
  try {
    const response = await axios.get('/activities')
    if (response.data) {
      activities.value = response.data.filter(a => a.status === 'ACTIVE' || a.status === 1)
    }
  } catch (error) {
    activities.value = []
  }
}

const formatTime = (time) => {
  if (!time) return '待定'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const scrollToEnroll = () => {
  const element = document.getElementById('enroll')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

const showEnrollForm = (activity) => {
  selectedActivity.value = activity
  enrollForm.value.activityId = activity.id
  enrollForm.value.activityTitle = activity.title
  scrollToEnroll()
}

const viewDetails = (activity) => {
  detailActivity.value = activity
  showDetailsDialog.value = true
}

const enrollFromDetail = () => {
  showDetailsDialog.value = false
  showEnrollForm(detailActivity.value)
}

const submitEnrollment = async () => {
  try {
    await enrollFormRef.value.validate()
    submitting.value = true
    
    const response = await axios.post('/enrollments', enrollForm.value)
    
    if (response.code === 200) {
      ElMessage.success('报名成功！我们将尽快与您联系！')
      enrollFormRef.value.resetFields()
      selectedActivity.value = null
    } else {
      ElMessage.error(response.message || '报名失败，请稍后重试')
    }
  } catch (error) {
    ElMessage.error('请检查输入信息是否正确')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.nav a {
  color: #fff;
  text-decoration: none;
  margin-left: 30px;
  font-size: 16px;
  transition: opacity 0.3s;
}

.nav a:hover {
  opacity: 0.8;
}

.banner-section {
  background: #fff;
}

.banner-carousel {
  width: 100%;
}

.banner-item {
  width: 100%;
  height: 500px;
  background-size: cover;
  background-position: center;
  background-color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #fff;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%);
}

.banner-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  padding: 40px;
}

.banner-content h2 {
  font-size: 48px;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.banner-content p {
  font-size: 20px;
  margin-bottom: 30px;
}

.activities-section,
.enroll-section,
.contact-section {
  padding: 80px 0;
}

.section-title {
  text-align: center;
  font-size: 36px;
  color: #333;
  margin-bottom: 50px;
  position: relative;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 20px auto 0;
  border-radius: 2px;
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
}

.activity-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.activity-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.activity-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.activity-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-content {
  padding: 24px;
}

.activity-content h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 10px;
}

.activity-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  line-height: 1.6;
}

.activity-info {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
}

.activity-actions {
  display: flex;
  gap: 10px;
}

.enroll-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.enroll-section .section-title {
  color: #fff;
}

.enroll-section .section-title::after {
  background: #fff;
}

.enroll-form-wrapper {
  max-width: 600px;
  margin: 0 auto;
  background: #fff;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.enroll-form {
  color: #333;
}

.activity-name {
  color: #667eea;
  font-weight: bold;
  font-size: 16px;
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
}

.contact-section {
  background: #fff;
}

.contact-info {
  display: flex;
  justify-content: center;
  gap: 60px;
  flex-wrap: wrap;
}

.contact-item {
  text-align: center;
  color: #333;
}

.contact-item i {
  font-size: 40px;
  color: #667eea;
  margin-bottom: 15px;
}

.contact-item p {
  font-size: 16px;
  margin: 0;
}

.footer {
  background: #333;
  color: #fff;
  padding: 30px 0;
  text-align: center;
}

.footer p {
  margin: 0;
  font-size: 14px;
}

.activity-details h2 {
  color: #333;
  margin-bottom: 20px;
}

.detail-info p {
  color: #666;
  margin: 10px 0;
  line-height: 1.8;
}

.detail-content {
  color: #666;
  line-height: 1.8;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 10px;
}

:deep(.el-carousel__item h3) {
  color: #475669;
  font-size: 18px;
  opacity: 0.75;
  margin: 0;
  text-align: center;
}
</style>
