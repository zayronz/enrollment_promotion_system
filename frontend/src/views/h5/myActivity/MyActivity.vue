<template>
  <div class="my-activity">
    <H5NavBar title="我的活动" />

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

    <div class="activity-list">
      <div v-for="item in currentList" :key="item.id" class="activity-card" @click="goDetail(item)">
        <div class="card-header">
          <div class="card-title-row">
            <span v-if="item.type === 'school'" class="school-icon">校</span>
            <span class="card-title">{{ item.title }}</span>
          </div>
          <span class="card-tag" :class="item.type">{{ item.type === 'activity' ? '招宣活动' : '母校行活动' }}</span>
        </div>
        <div class="card-info">
          <div class="info-row">计划开始日期：{{ item.startDate }}</div>
          <div class="info-row">计划结束日期：{{ item.endDate }}</div>
          <div class="info-row">{{ item.type === 'school' ? '行政区域地址' : '活动区域' }}：{{ item.area }}</div>
          <div class="info-row" v-if="item.type === 'activity'">活动牵头宣传组：{{ item.group }}</div>
        </div>
        <div class="card-update" v-if="item.updateInfo">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <span>{{ item.updateInfo }}</span>
        </div>
      </div>
      <div v-if="currentList.length === 0" class="empty-tip">
        暂无{{ activeTab === 'ongoing' ? '进行中' : '已完成' }}的活动
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
    id: 1, title: '2023届招生宣传演讲', type: 'activity',
    startDate: '2023/03/29', endDate: '2023/03/29', area: '湖北省武汉市洪山区', group: '宣传组名称1',
    updateInfo: '李明于2023/03/29 更新了活动照片'
  },
  {
    id: 101, title: '武汉第一中学', type: 'school',
    startDate: '2023/03/29', endDate: '2023/03/29', area: '湖北省武汉市洪山区',
    updateInfo: '李明于2023/03/29 更新了活动照片'
  }
]

const completedList = [
  {
    id: 102, title: '武汉第二中学', type: 'school',
    startDate: '2022/12/15', endDate: '2022/12/15', area: '湖北省武汉市洪山区'
  }
]

const currentList = computed(() => {
  return activeTab.value === 'ongoing' ? ongoingList : completedList
})

const goDetail = (item) => {
  router.push(`/h5/my-activity/${item.id}`)
}
</script>

<style scoped>
.my-activity {
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
.activity-list {
  padding: 12px;
}
.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
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
.card-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  flex-shrink: 0;
}
.card-tag.activity {
  background: #dbeafe;
  color: #2563eb;
}
.card-tag.school {
  background: #fce7f3;
  color: #ec4899;
}
.card-info {
  margin-bottom: 8px;
}
.info-row {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}
.card-update {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
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
