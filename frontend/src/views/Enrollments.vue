<template>
  <div class="enrollments-container">
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
        <h1>报名管理</h1>
      </div>
      <div class="content">
        <el-table :data="enrollments" border style="width: 100%">
          <el-table-column prop="id" label="ID" />
          <el-table-column prop="activityName" label="活动名称" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="报名时间" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button v-if="scope.row.status === 'PENDING'" size="small" type="primary" @click="handleApprove(scope.row.id)">
                审核通过
              </el-button>
              <el-button v-if="scope.row.status === 'PENDING'" size="small" type="danger" @click="handleReject(scope.row.id)">
                拒绝
              </el-button>
              <el-button size="small" type="danger" @click="deleteEnrollment(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Home, Calendar, List, Message, Image } from '@element-plus/icons-vue'
import axios from '../utils/axios'

const router = useRouter()
const activeMenu = ref('enrollments')
const enrollments = ref([])

onMounted(() => {
  fetchEnrollments()
})

const fetchEnrollments = async () => {
  try {
    const response = await axios.get('/enrollments')
    enrollments.value = response.data || []
  } catch (error) {
    ElMessage.error('获取报名列表失败')
  }
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return texts[status] || status
}

const handleApprove = async (id) => {
  try {
    await axios.put(`/approvals/${id}/approve`)
    ElMessage.success('审核通过')
    fetchEnrollments()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleReject = async (id) => {
  try {
    await axios.put(`/approvals/${id}/reject`)
    ElMessage.success('已拒绝')
    fetchEnrollments()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteEnrollment = async (id) => {
  try {
    await axios.delete(`/enrollments/${id}`)
    ElMessage.success('删除成功')
    fetchEnrollments()
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
.enrollments-container {
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
