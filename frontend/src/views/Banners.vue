<template>
  <div class="banners-container">
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
        <h1>轮播图管理</h1>
        <el-button type="primary" @click="openAddModal">添加轮播图</el-button>
      </div>
      <div class="content">
        <el-table :data="banners" border style="width: 100%">
          <el-table-column prop="id" label="ID" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="imageUrl" label="图片地址" />
          <el-table-column prop="linkUrl" label="链接地址" />
          <el-table-column prop="sortOrder" label="排序" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'warning'">
                {{ scope.row.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" @click="openEditModal(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteBanner(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog title="添加轮播图" :visible.sync="showModal" width="500px">
      <el-form :model="bannerForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="bannerForm.title" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="bannerForm.imageUrl" />
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="bannerForm.linkUrl" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model="bannerForm.sortOrder" type="number" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showModal = false">取消</el-button>
        <el-button type="primary" @click="saveBanner">确定</el-button>
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
const activeMenu = ref('banners')
const banners = ref([])
const showModal = ref(false)
const bannerForm = ref({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 'ACTIVE'
})
const editId = ref(null)

onMounted(() => {
  fetchBanners()
})

const fetchBanners = async () => {
  try {
    const response = await axios.get('/banners')
    banners.value = response.data || []
  } catch (error) {
    ElMessage.error('获取轮播图列表失败')
  }
}

const openAddModal = () => {
  editId.value = null
  bannerForm.value = {
    title: '',
    imageUrl: '',
    linkUrl: '',
    sortOrder: 0,
    status: 'ACTIVE'
  }
  showModal.value = true
}

const openEditModal = (row) => {
  editId.value = row.id
  bannerForm.value = {
    title: row.title,
    imageUrl: row.imageUrl,
    linkUrl: row.linkUrl,
    sortOrder: row.sortOrder,
    status: row.status
  }
  showModal.value = true
}

const saveBanner = async () => {
  try {
    if (editId.value) {
      await axios.put(`/banners/${editId.value}`, bannerForm.value)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/banners', bannerForm.value)
      ElMessage.success('添加成功')
    }
    showModal.value = false
    fetchBanners()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteBanner = async (id) => {
  try {
    await axios.delete(`/banners/${id}`)
    ElMessage.success('删除成功')
    fetchBanners()
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
.banners-container {
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
