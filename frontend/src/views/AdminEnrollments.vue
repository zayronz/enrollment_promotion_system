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
        <h1>报名管理</h1>
      </div>
      <div class="content">
        <el-table :data="enrollments" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="realName" label="姓名" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="activityTitle" label="活动名称" />
          <el-table-column prop="collegeName" label="所在院校" />
          <el-table-column prop="major" label="专业" />
          <el-table-column prop="approvalStatus" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.approvalStatus)">
                {{ getStatusText(scope.row.approvalStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button v-if="scope.row.approvalStatus === 0" size="small" type="success" @click="approve(scope.row.id)">
                通过
              </el-button>
              <el-button v-if="scope.row.approvalStatus === 0" size="small" type="danger" @click="reject(scope.row.id)">
                拒绝
              </el-button>
              <el-button size="small" type="info" @click="viewDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog title="报名详情" :visible.sync="showDetailDialog" width="600px">
      <div v-if="detail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ detail.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detail.email }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detail.studentNo || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="活动名称">{{ detail.activityTitle }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ formatTime(detail.enrollmentTime) }}</el-descriptions-item>
          <el-descriptions-item label="所在院校" :span="2">{{ detail.collegeName || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="专业" :span="2">{{ detail.major || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">{{ detail.enrollmentData || '未填写' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer">
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Home, Calendar, List } from '@element-plus/icons-vue'
import axios from '../utils/axios'

const router = useRouter()
const activeMenu = ref('enrollments')
const enrollments = ref([])
const showDetailDialog = ref(false)
const detail = ref(null)

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
  const types = { 0: 'warning', 1: 'info', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '审核中', 2: '已通过', 3: '已拒绝' }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const approve = async (id) => {
  try {
    await axios.put(`/approvals/${id}/approve`)
    ElMessage.success('已通过审核')
    fetchEnrollments()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const reject = async (id) => {
  try {
    await axios.put(`/approvals/${id}/reject`)
    ElMessage.success('已拒绝')
    fetchEnrollments()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const viewDetail = (row) => {
  detail.value = row
  showDetailDialog.value = true
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

.content {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.detail-content {
  padding: 10px;
}
</style>
