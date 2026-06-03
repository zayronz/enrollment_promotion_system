<template>
  <div class="h5-home">
    <!-- 头部 -->
    <div class="home-header">
      <div class="header-title">招生宣传</div>
      <div class="header-actions">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
          <circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/>
        </svg>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="3"/>
        </svg>
      </div>
    </div>

    <!-- 搜索 -->
    <div class="search-box">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
      </svg>
      <input v-model="searchKey" placeholder="请输入活动名称模糊查询" />
    </div>

    <!-- Tab -->
    <div class="tab-bar">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'activity' }"
        @click="activeTab = 'activity'"
      >
        招宣活动
        <div class="tab-line" v-if="activeTab === 'activity'"></div>
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'school' }"
        @click="activeTab = 'school'"
      >
        母校行
        <div class="tab-line" v-if="activeTab === 'school'"></div>
      </div>
    </div>

    <!-- 列表 -->
    <div class="activity-list">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="activity-card"
        @click="goDetail(item)"
      >
        <div class="card-header">
          <div class="card-title-row">
            <span v-if="activeTab === 'school'" class="school-icon">校</span>
            <span class="card-title">{{ item.title }}</span>
          </div>
          <span class="card-status" :class="item.status">{{ item.statusText }}</span>
        </div>
        <div class="card-info">
          <div class="info-row">计划开始日期：{{ item.startDate }}</div>
          <div class="info-row">计划结束日期：{{ item.endDate }}</div>
          <div class="info-row">{{ activeTab === 'school' ? '行政区域地址' : '活动区域' }}：{{ item.area }}</div>
          <div class="info-row" v-if="activeTab === 'activity'">活动牵头宣传组：{{ item.group }}</div>
        </div>
        <div class="card-footer">
          <div class="avatars">
            <img v-for="(avatar, idx) in item.avatars" :key="idx" :src="avatar" class="avatar" />
          </div>
          <span class="join-text">{{ item.joinCount }}人已经参与活动，赶紧报名吧！</span>
        </div>
      </div>
    </div>

    <H5BottomNav />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import H5BottomNav from '../components/H5BottomNav.vue'

const router = useRouter()
const activeTab = ref('activity')
const searchKey = ref('')

const activityList = ref([
  {
    id: 1, title: '2023届应届生招生活动', status: 'open', statusText: '开放报名中',
    startDate: '2023/03/29', endDate: '2023/03/29', area: '湖北省武汉市洪山区', group: '宣传组名称1',
    joinCount: 28, type: 'activity',
    avatars: ['https://api.dicebear.com/7.x/avataaars/svg?seed=1', 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', 'https://api.dicebear.com/7.x/avataaars/svg?seed=4']
  },
  {
    id: 2, title: '2023届应届生招生活动', status: 'open', statusText: '开放报名中',
    startDate: '2023/03/29', endDate: '2023/03/29', area: '湖北省武汉市洪山区', group: '宣传组名称1',
    joinCount: 28, type: 'activity',
    avatars: ['https://api.dicebear.com/7.x/avataaars/svg?seed=5', 'https://api.dicebear.com/7.x/avataaars/svg?seed=6', 'https://api.dicebear.com/7.x/avataaars/svg?seed=7', 'https://api.dicebear.com/7.x/avataaars/svg?seed=8']
  }
])

const schoolList = ref([
  {
    id: 101, title: '武汉第一中学', status: 'open', statusText: '开放报名中',
    startDate: '2023/03/29', endDate: '2023/03/29', area: '湖北省武汉市洪山区', group: '宣传组名称1',
    joinCount: 28, type: 'school',
    avatars: ['https://api.dicebear.com/7.x/avataaars/svg?seed=9', 'https://api.dicebear.com/7.x/avataaars/svg?seed=10', 'https://api.dicebear.com/7.x/avataaars/svg?seed=11', 'https://api.dicebear.com/7.x/avataaars/svg?seed=12']
  },
  {
    id: 102, title: '武汉第一中学', status: 'open', statusText: '开放报名中',
    startDate: '2023/03/29', endDate: '2023/03/29', area: '湖北省武汉市洪山区', group: '宣传组名称1',
    joinCount: 28, type: 'school',
    avatars: ['https://api.dicebear.com/7.x/avataaars/svg?seed=13', 'https://api.dicebear.com/7.x/avataaars/svg?seed=14', 'https://api.dicebear.com/7.x/avataaars/svg?seed=15', 'https://api.dicebear.com/7.x/avataaars/svg?seed=16']
  }
])

const filteredList = computed(() => {
  const list = activeTab.value === 'activity' ? activityList.value : schoolList.value
  if (!searchKey.value) return list
  return list.filter(i => i.title.includes(searchKey.value))
})

const goDetail = (item) => {
  const path = item.type === 'school' ? `/h5/school-activity/${item.id}` : `/h5/activity/${item.id}`
  router.push(path)
}
</script>

<style scoped>
.h5-home {
  padding-bottom: 70px;
}
.home-header {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  color: #fff;
  font-size: 18px;
  font-weight: 500;
}
.header-actions {
  display: flex;
  gap: 12px;
}
.search-box {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  padding: 0 16px 16px;
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 0 0 20px 20px;
  margin: 0 12px;
  position: relative;
  top: -8px;
  padding: 10px 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.search-box svg {
  flex-shrink: 0;
}
.search-box input {
  flex: 1;
  border: none;
  outline: none;
  margin-left: 8px;
  font-size: 14px;
  color: #374151;
  background: transparent;
}
.search-box input::placeholder {
  color: #9ca3af;
}
.tab-bar {
  display: flex;
  padding: 12px 16px 0;
  gap: 24px;
  background: #f5f7fa;
}
.tab-item {
  font-size: 15px;
  color: #6b7280;
  padding-bottom: 8px;
  position: relative;
  cursor: pointer;
  font-weight: 500;
}
.tab-item.active {
  color: #1f2937;
}
.tab-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #2563eb;
  border-radius: 2px;
}
.activity-list {
  padding: 12px;
}
.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}
.card-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.school-icon {
  width: 22px;
  height: 22px;
  background: #dbeafe;
  color: #2563eb;
  border-radius: 50%;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}
.card-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #dbeafe;
  color: #2563eb;
  flex-shrink: 0;
}
.card-status.open::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #2563eb;
  border-radius: 50%;
  margin-right: 4px;
  vertical-align: middle;
}
.card-info {
  margin-bottom: 10px;
}
.info-row {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}
.card-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #f3f4f6;
}
.avatars {
  display: flex;
}
.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #fff;
  margin-left: -6px;
}
.avatar:first-child {
  margin-left: 0;
}
.join-text {
  font-size: 12px;
  color: #9ca3af;
}
</style>
