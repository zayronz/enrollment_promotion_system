<template>
  <div class="test-list">
    <H5NavBar title="招生能力测试" />

    <div class="tab-bar">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'ongoing' }"
        @click="activeTab = 'ongoing'"
      >
        进行中
        <div v-if="activeTab === 'ongoing'" class="tab-line"></div>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'completed' }"
        @click="activeTab = 'completed'"
      >
        已完成
        <div v-if="activeTab === 'completed'" class="tab-line"></div>
      </div>
    </div>

    <div class="test-list-content">
      <div v-for="item in currentList" :key="item.id" class="test-card" @click="goPage(item)">
        <div class="test-header">
          <div class="test-title">{{ item.title }}</div>
          <div v-if="item.score" class="test-score">{{ item.score }}分</div>
        </div>
        <div class="test-info">
          <div>开始日期：{{ item.startDate }}</div>
          <div>结束日期：{{ item.endDate }}</div>
          <div v-if="item.countdown" class="countdown">剩余提交时间：<span>{{ item.countdown }}</span></div>
        </div>
        <div class="test-footer">
          <div class="avatars">
            <img v-for="(avatar, idx) in item.avatars" :key="idx" :src="avatar" class="avatar" />
          </div>
          <span class="join-text">{{ item.joinText }}</span>
        </div>
      </div>
      <div v-if="currentList.length === 0" class="empty-tip">
        暂无{{ activeTab === 'ongoing' ? '进行中' : '已完成' }}的测试
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const activeTab = ref('ongoing')

const ongoingList = [
  {
    id: 1, title: '2023届招生能力测试题目',
    startDate: '2023/03/29', endDate: '2023/03/29',
    countdown: '3天4时5分', joinCount: 28,
    joinText: '28人已经完成，赶紧测试吧！',
    avatars: ['https://api.dicebear.com/7.x/avataaars/svg?seed=21', 'https://api.dicebear.com/7.x/avataaars/svg?seed=22', 'https://api.dicebear.com/7.x/avataaars/svg?seed=23', 'https://api.dicebear.com/7.x/avataaars/svg?seed=24']
  },
  {
    id: 2, title: '2023届招生能力测试题目',
    startDate: '2023/03/29', endDate: '2023/03/29',
    countdown: '3天4时5分', joinCount: 28,
    joinText: '28人已经完成，赶紧测试吧！',
    avatars: ['https://api.dicebear.com/7.x/avataaars/svg?seed=25', 'https://api.dicebear.com/7.x/avataaars/svg?seed=26', 'https://api.dicebear.com/7.x/avataaars/svg?seed=27', 'https://api.dicebear.com/7.x/avataaars/svg?seed=28']
  }
]

const completedList = [
  {
    id: 101, title: '2022届招生能力测试题目',
    startDate: '2022/03/15', endDate: '2022/03/20',
    score: 61, joinCount: 156,
    joinText: '156人已完成',
    avatars: ['https://api.dicebear.com/7.x/avataaars/svg?seed=29', 'https://api.dicebear.com/7.x/avataaars/svg?seed=30', 'https://api.dicebear.com/7.x/avataaars/svg?seed=31', 'https://api.dicebear.com/7.x/avataaars/svg?seed=32']
  }
]

const currentList = computed(() => {
  return activeTab.value === 'ongoing' ? ongoingList : completedList
})

const goPage = (item) => {
  if (activeTab.value === 'ongoing') {
    router.push(`/h5/test/start/${item.id}`)
  } else {
    router.push(`/h5/test/result/${item.id}`)
  }
}
</script>

<style scoped>
.test-list {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
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
.test-list-content {
  padding: 12px;
}
.test-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
}
.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.test-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}
.test-score {
  font-size: 20px;
  font-weight: 700;
  color: #ef4444;
  background: #fef2f2;
  padding: 4px 12px;
  border-radius: 8px;
}
.test-info {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
  margin-bottom: 10px;
}
.test-info .countdown span {
  color: #f59e0b;
}
.test-footer {
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
.empty-tip {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
  font-size: 14px;
}
</style>
