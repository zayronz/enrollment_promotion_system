<template>
  <div class="admin-container">
    <div class="sidebar">
      <div class="logo">
        <h2>招生管理系统</h2>
      </div>
      <el-menu :default-active="activeMenu" class="sidebar-menu">
        <el-menu-item index="dashboard" @click="navigateTo('/admin')">
          <el-icon><component :is="Home" /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="activities" @click="navigateTo('/admin/activities')">
          <el-icon><component :is="Calendar" /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="enrollments" @click="navigateTo('/admin/enrollments')">
          <el-icon><component :is="List" /></el-icon>
          <span>报名管理</span>
        </el-menu-item>
      </el-menu>
      <div class="logout-section">
        <el-button @click="goHome">返回首页</el-button>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    <div class="main-content">
      <div class="header">
        <h1>控制台</h1>
      </div>
      <div class="dashboard">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon blue">
              <el-icon><component :is="Calendar" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.activities }}</div>
              <div class="stat-label">活动总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon green">
              <el-icon><component :is="List" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.enrollments }}</div>
              <div class="stat-label">报名总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon orange">
              <el-icon><component :is="CheckCircle" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">已通过审核</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon purple">
              <el-icon><component :is="Clock" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </div>
        <div class="recent-section">
          <h3>最新报名</h3>
          <el-table :data="recentEnrollments" border style="width: 100%">
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="activityTitle" label="活动名称" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="approvalStatus" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.approvalStatus)">
                  {{ getStatusText(scope.row.approvalStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Home, Calendar, List, CheckCircle, Clock } from '@element-plus/icons-vue'
import axios from '../utils/axios'

const router = useRouter()
const activeMenu = ref('dashboard')
const stats = ref({
  activities: 0,
  enrollments: 0,
  approved: 0,
  pending: 0
})
const recentEnrollments = ref([])

onMounted(() => {
  fetchStats()
  fetchRecentEnrollments()
})

const fetchStats = async () => {
  try {
    const activitiesRes = await axios.get('/activities')
    const enrollmentsRes = await axios.get('/enrollments')
    
    const enrollments = enrollmentsRes.data || []
    stats.value = {
      activities: activitiesRes.data?.length || 0,
      enrollments: enrollments.length,
      approved: enrollments.filter(e => e.approvalStatus === 2).length,
      pending: enrollments.filter(e => e.approvalStatus === 0).length
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

const fetchRecentEnrollments = async () => {
  try {
    const response = await axios.get('/enrollments')
    recentEnrollments.value = (response.data || []).slice(0, 5)
  } catch (error) {
    console.error('获取报名列表失败', error)
  }
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'info', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '审核中', 2: '已通过', 3: '已拒绝' }
  return texts[status] || '未知'
}

const navigateTo = (path) => {
  router.push(path)
}

const goHome = () => {
  router.push('/')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出成功')
  router.push('/')
}
</script>

<style scoped>
.admin-container {
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
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.1);
}

.logout-section {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
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

.dashboard {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
  color: #fff;
  font-size: 24px;
}

.stat-icon.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.orange { background: linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%); }
.stat-icon.purple { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); color: #666; }

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.recent-section h3 {
  color: #333;
  margin-bottom: 15px;
}
</style>
