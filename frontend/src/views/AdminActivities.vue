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
        <h1>活动管理</h1>
        <el-button type="primary" @click="openAddModal">添加活动</el-button>
      </div>
      <div class="content">
        <el-table :data="activities" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="活动名称" />
          <el-table-column prop="description" label="活动描述" />
          <el-table-column prop="startTime" label="开始时间" />
          <el-table-column prop="endTime" label="结束时间" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                {{ scope.row.status === 1 ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" @click="openEditModal(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteActivity(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog :title="editId ? '编辑活动' : '添加活动'" :visible.sync="showModal" width="600px">
      <el-form :model="activityForm" label-width="100px">
        <el-form-item label="活动名称">
          <el-input v-model="activityForm.title" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="activityForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="activityForm.startTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="activityForm.endTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="activityForm.status">
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showModal = false">取消</el-button>
        <el-button type="primary" @click="saveActivity">确定</el-button>
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
const activeMenu = ref('activities')
const activities = ref([])
const showModal = ref(false)
const editId = ref(null)
const activityForm = ref({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  status: 1
})

onMounted(() => {
  fetchActivities()
})

const fetchActivities = async () => {
  try {
    const response = await axios.get('/activities')
    activities.value = response.data || []
  } catch (error) {
    ElMessage.error('获取活动列表失败')
  }
}

const openAddModal = () => {
  editId.value = null
  activityForm.value = { title: '', description: '', startTime: '', endTime: '', status: 1 }
  showModal.value = true
}

const openEditModal = (row) => {
  editId.value = row.id
  activityForm.value = { ...row }
  showModal.value = true
}

const saveActivity = async () => {
  try {
    if (editId.value) {
      await axios.put(`/activities/${editId.value}`, activityForm.value)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/activities', activityForm.value)
      ElMessage.success('添加成功')
    }
    showModal.value = false
    fetchActivities()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteActivity = async (id) => {
  try {
    await axios.delete(`/activities/${id}`)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch (error) {
    ElMessage.error('删除失败')
  }
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
