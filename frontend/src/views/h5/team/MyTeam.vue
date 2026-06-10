<template>
  <div class="my-team">
    <H5NavBar title="我的组员" />

    <div class="tab-bar">
      <div class="tab-item active">
        招宣组1
        <div class="tab-line"></div>
      </div>
      <div class="tab-item">
        招宣组2
      </div>
    </div>

    <div class="team-content">
      <div class="info-section">
        <div class="info-label">活动名称</div>
        <div class="info-value">2012届毕业生母校游</div>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="share-icon"><path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6M15 3h6v6M10 14L21 3"/></svg>
      </div>

      <div class="info-section">
        <div class="info-label">组长</div>
        <div class="info-value">张明</div>
      </div>

      <div class="info-section">
        <div class="info-label">副组长</div>
        <div class="info-value">王小蓝</div>
      </div>

      <div class="info-section">
        <div class="info-label">联络员</div>
        <div class="info-value">张明</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动成员</div>
        <div class="avatars-row">
          <img v-for="i in 5" :key="i" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=team${i}`" class="member-avatar" />
        </div>
        <div class="member-table">
          <div class="table-header">
            <span>姓名</span>
            <span>所在单位</span>
            <span>职务</span>
            <span>联系电话</span>
          </div>
          <div class="table-row" v-for="i in 3" :key="i">
            <span>张晓晶</span>
            <span>武汉一中</span>
            <span>教师</span>
            <span>16788999008</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <div class="info-label">最近的一条活动总结</div>
        <div class="summary-card">
          <div class="summary-title">10001关联活动标题</div>
          <div class="summary-text">数学院团委学生会走进红河州戒毒所,举行了爱心帮教动。此次,在数学学院志愿者与强制戒毒人员的联欢就是他们开展的互动活动之一"正能量传递</div>
          <div class="summary-user">
            <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=summary2" class="user-avatar-sm" />
            <span>李明慧</span>
          </div>
          <div class="summary-time">2023/03/23 10:00:00</div>
        </div>
      </div>
    </div>

    <div class="submit-area">
      <button class="submit-btn" @click="handleExit">退出本组</button>
    </div>
  </div>
</template>

<script setup>
import H5NavBar from '../components/H5NavBar.vue'

const handleExit = () => {
  if (confirm('确定要退出本组吗？')) {
    alert('已退出本组')
  }
}
</script>

<style scoped>
.my-team {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
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
.team-content {
  padding: 12px;
}
.info-section {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  margin-bottom: 10px;
  position: relative;
}
.info-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 6px;
}
.info-label::before {
  content: '*';
  color: #f59e0b;
  margin-right: 2px;
}
.info-value {
  font-size: 14px;
  color: #374151;
}
.share-icon {
  position: absolute;
  right: 14px;
  top: 12px;
  cursor: pointer;
}
.avatars-row {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}
.member-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #fff;
  object-fit: cover;
}
.member-table {
  border-top: 1px solid #f3f4f6;
  padding-top: 10px;
}
.table-header {
  display: grid;
  grid-template-columns: 1fr 1.2fr 0.8fr 1.2fr;
  gap: 8px;
  font-size: 12px;
  color: #9ca3af;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}
.table-row {
  display: grid;
  grid-template-columns: 1fr 1.2fr 0.8fr 1.2fr;
  gap: 8px;
  font-size: 12px;
  color: #374151;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}
.summary-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
}
.summary-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 6px;
}
.summary-text {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 10px;
}
.summary-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
}
.user-avatar-sm {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}
.summary-time {
  font-size: 12px;
  color: #d1d5db;
  margin-top: 4px;
}
.submit-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  background: #f5f7fa;
  padding: 12px 16px 20px;
  z-index: 50;
}
.submit-btn {
  width: 100%;
  height: 46px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}
</style>
