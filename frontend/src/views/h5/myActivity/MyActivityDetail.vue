<template>
  <div class="my-activity-detail">
    <H5NavBar title="活动详情" />

    <div class="detail-content">
      <div class="detail-title-row">
        <span class="detail-tag">&#9632;</span>
        <span class="detail-title">2012届毕业生母校游</span>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="share-icon">
          <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6M15 3h6v6M10 14L21 3"/>
        </svg>
      </div>

      <div class="info-section">
        <div class="info-label">招宣年度</div>
        <div class="info-value">2023年</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动来源</div>
        <div class="info-value">派发</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动区域</div>
        <div class="info-value">湖北省/武汉市/洪山区</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动牵头宣传组</div>
        <div class="info-value user-row">
          <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=leader" class="user-avatar" />
          <span>招生宣传组</span>
        </div>
      </div>

      <div class="info-section">
        <div class="info-label">活动创建人</div>
        <div class="info-value user-row">
          <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=creator" class="user-avatar" />
          <span>李明慧</span>
        </div>
      </div>

      <div class="info-section">
        <div class="info-label">活动时间</div>
        <div class="info-value">
          <div>计划开始日期：2023/03/29</div>
          <div>计划结束日期：2023/03/29</div>
          <div>合计时长：1天</div>
        </div>
      </div>

      <!-- 活动成员 -->
      <div class="info-section">
        <div class="info-header">
          <span class="info-label">活动成员</span>
          <span class="member-count">10003</span>
        </div>
        <div class="member-list">
          <div class="member-card">
            <div class="member-name">姓名1</div>
            <div class="member-info">
              <div>在职单位：A学院</div>
              <div>职务：主任</div>
              <div>职称：教授</div>
              <div>籍贯：湖北省</div>
              <div>毕业中学：武汉一中</div>
              <div>联系电话：15888888888</div>
            </div>
          </div>
          <div class="member-card">
            <div class="member-name">姓名1</div>
            <div class="member-info">
              <div>在职单位：A学院</div>
              <div>职务：主任</div>
              <div>职称：教授</div>
              <div>籍贯：湖北省</div>
              <div>毕业中学：武汉一中</div>
              <div>联系电话：15888888888</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动信息 -->
      <div class="info-section">
        <div class="info-label">活动信息</div>
        <div class="info-value">
          <div>宣传到访单位名称：A单位</div>
          <div>宣传到访时间：2023/03/29</div>
          <div>宣传到访单位联系人：周彬</div>
          <div>宣传到访单位联系人职务：教导主任</div>
        </div>
      </div>

      <!-- 活动总结 -->
      <div class="info-section">
        <div class="info-header">
          <span class="info-label">活动总结</span>
          <span class="add-link" @click="router.push('/h5/add-school')">添加</span>
        </div>
        <div class="summary-card">
          <div class="summary-text">学校坚持认真贯彻执行全国、省、市关于职业学校招生工作精神,加强招生管理,规范招生行为。</div>
          <div class="summary-user">
            <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=summary" class="user-avatar-sm" />
            <span>李明慧</span>
          </div>
          <div class="summary-tip">展示最新的一条</div>
        </div>
      </div>

      <!-- 活动视频 -->
      <div class="info-section">
        <div class="info-header">
          <span class="info-label">活动视频</span>
          <span class="add-link" @click="router.push('/h5/add-video')">添加</span>
        </div>
        <div class="video-card">
          <div class="video-thumb">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="white"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          </div>
          <div class="video-info">
            <div>2023/3/20 00:00:00</div>
          </div>
        </div>
        <div class="summary-user">
          <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=video" class="user-avatar-sm" />
          <span>李明慧</span>
        </div>
        <div class="summary-tip">展示最新的一条</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
</script>

<style scoped>
.my-activity-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}
.detail-content {
  padding: 12px;
}
.detail-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.detail-tag {
  color: #2563eb;
  font-size: 14px;
}
.detail-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
.share-icon {
  flex-shrink: 0;
  cursor: pointer;
}
.info-section {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  margin-bottom: 10px;
}
.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.info-label {
  font-size: 12px;
  color: #9ca3af;
}
.info-label::before {
  content: '*';
  color: #f59e0b;
  margin-right: 2px;
}
.info-value {
  font-size: 14px;
  color: #374151;
  line-height: 1.8;
}
.user-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}
.member-count {
  font-size: 13px;
  color: #2563eb;
}
.member-list {
  margin-top: 10px;
}
.member-card {
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
}
.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 6px;
}
.member-info {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}
.add-link {
  font-size: 13px;
  color: #2563eb;
  cursor: pointer;
}
.summary-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
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
  margin-top: 8px;
}
.user-avatar-sm {
  width: 20px;
  height: 20px;
  border-radius: 50%;
}
.summary-tip {
  font-size: 12px;
  color: #d1d5db;
  text-align: right;
  margin-top: 4px;
}
.video-card {
  background: #f9fafb;
  border-radius: 8px;
  overflow: hidden;
}
.video-thumb {
  height: 120px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.video-info {
  padding: 8px 12px;
  font-size: 12px;
  color: #9ca3af;
}
</style>
