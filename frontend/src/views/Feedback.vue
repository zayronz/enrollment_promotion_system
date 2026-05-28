<template>
  <div class="feedback-container">
    <div class="sidebar">
      <div class="logo">
        <h2>招生报名系统</h2>
      </div>
      <el-menu :default-active="activeMenu" class="sidebar-menu">
        <el-menu-item index="home" @click="navigateTo('/')">
          <el-icon><component :is="Home" /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="activities" @click="navigateTo('/activities')">
          <el-icon><component :is="Calendar" /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="enrollments" @click="navigateTo('/enrollments')">
          <el-icon><component :is="List" /></el-icon>
          <span>报名管理</span>
        </el-menu-item>
        <el-menu-item index="feedback" @click="navigateTo('/feedback')">
          <el-icon><component :is="Message" /></el-icon>
          <span>反馈管理</span>
        </el-menu-item>
        <el-menu-item index="banners" @click="navigateTo('/banners')">
          <el-icon><component :is="Image" /></el-icon>
          <span>轮播图管理</span>
        </el-menu-item>
      </el-menu>
      <div class="logout-btn">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    <div class="main-content">
      <div class="header">
        <h1>反馈管理</h1>
        <el-button type="primary" @click="openAddModal">添加反馈</el-button>
      </div>
      <div class="content">
        <el-table :data="feedbacks" border style="width: 100%">
          <el-table-column prop="id" label="ID" />
          <el-table-column prop="content" label="反馈内容" />
          <el-table-column prop="createdAt" label="提交时间" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" type="danger" @click="deleteFeedback(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog title="添加反馈" :visible.sync="showModal" width="500px">
      <el-form :model="feedbackForm" label-width="80px">
        <el-form-item label="反馈内容">
          <el-input v-model="feedbackForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showModal = false">取消</el-button>
        <el-button type="primary" @click="saveFeedback">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Home, Calendar, List, Message, Image } from '@element-plus/icons-vue'
import axios from '../utils/axios'

const router = useRouter()
const activeMenu = ref('feedback')
const feedbacks = ref([])
const showModal = ref(false)
const feedbackForm = ref({
  content: ''
})

onMounted(() => {
  fetchFeedbacks()
})

const fetchFeedbacks = async () => {
  try {
    const response = await axios.get('/feedbacks')
    feedbacks.value = response.data || []
  } catch (error) {
    ElMessage.error('获取反馈列表失败')
  }
}

const openAddModal = () => {
  feedbackForm.value = { content: '' }
  showModal.value = true
}

const saveFeedback = async () => {
  try {
    await axios.post('/feedbacks', feedbackForm.value)
    ElMessage.success('添加成功')
    showModal.value = false
    fetchFeedbacks()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const deleteFeedback = async (id) => {
  try {
    await axios.delete(`/feedbacks/${id}`)
    ElMessage.success('删除成功')
    fetchFeedbacks()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const navigateTo = (path) => {
  router.push(path)
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出成功')
  router.push('/login')
}
</script>

<style scoped>
.feedback-container {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 250px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #fff;
  border-radius: 0;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.1);
}

.logout-btn {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.main-content {
  flex: 1;
  background: #f5f5f5;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  color: #333;
  font-size: 24px;
}

.content {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
