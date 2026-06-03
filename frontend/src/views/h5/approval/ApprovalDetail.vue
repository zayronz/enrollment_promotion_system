<template>
  <div class="approval-detail">
    <H5NavBar title="活动详情" />

    <div class="detail-content">
      <div class="detail-title-row">
        <span class="detail-tag">&#9632;</span>
        <span class="detail-title">2012届毕业生母校游</span>
      </div>

      <div class="info-section">
        <div class="info-label">审批状态</div>
        <div class="info-value" :class="statusClass">{{ statusText }}</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动来源</div>
        <div class="info-value">派发</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动时间</div>
        <div class="info-value">
          <div>计划开始日期：2023/03/29</div>
          <div>计划结束日期：2023/03/29</div>
          <div>合计时长：1天</div>
        </div>
      </div>

      <div class="info-section">
        <div class="info-label">活动区域</div>
        <div class="info-value">湖北省/武汉市/洪山区</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动牵头宣传组</div>
        <div class="info-value">宣传组001</div>
      </div>

      <!-- 报名信息 -->
      <div class="info-section" v-if="status === 'applying'">
        <div class="info-label">报名申请</div>
        <div class="info-value">
          <div>姓名：姓名1</div>
          <div>学号/工号：10001</div>
          <div>学院：A学院</div>
          <div>QQ：10000</div>
          <div>微信：10000</div>
        </div>
      </div>

      <!-- 审批流程 -->
      <div class="info-section">
        <div class="info-label">审批流程</div>
        <div class="flow-list">
          <div class="flow-item done">
            <div class="flow-avatar">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=flow1" />
              <div class="flow-dot green">&#10003;</div>
            </div>
            <div class="flow-info">
              <div class="flow-role">发起人(招宣成员/全体教职工)</div>
              <div class="flow-name">张晓军</div>
              <div class="flow-time">2023-02-15 11:37:08</div>
            </div>
          </div>
          <div class="flow-line"></div>
          <div class="flow-item" :class="{ done: status !== 'applying', reject: status === 'rejected' }">
            <div class="flow-avatar">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=flow2" />
              <div class="flow-dot" :class="{ green: status === 'approved', orange: status === 'applying', red: status === 'rejected' }">
                {{ status === 'approved' ? '&#10003;' : status === 'rejected' ? '&#10007;' : '' }}
              </div>
            </div>
            <div class="flow-info">
              <div class="flow-role">审批人(活动组长)</div>
              <div class="flow-name">张晓军</div>
              <div class="flow-time">2023-02-15 11:37:08</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="submit-area">
      <button class="submit-btn" @click="handleCancel">撤销申请</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const route = useRoute()
const router = useRouter()

const status = ref('applying')

const statusText = computed(() => {
  const map = { applying: '申请中；等待意见', approved: '审核通过；同意加入', rejected: '审核不通过；不同意加入' }
  return map[status.value]
})

const statusClass = computed(() => {
  return status.value
})

const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.approval-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
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
.info-section {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  margin-bottom: 10px;
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
  line-height: 1.8;
}
.info-value.applying { color: #2563eb; }
.info-value.approved { color: #10b981; }
.info-value.rejected { color: #ef4444; }
.flow-list {
  padding-left: 10px;
}
.flow-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
}
.flow-avatar {
  position: relative;
  flex-shrink: 0;
}
.flow-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}
.flow-dot {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #f59e0b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #fff;
  border: 2px solid #fff;
}
.flow-dot.green { background: #10b981; }
.flow-dot.red { background: #ef4444; }
.flow-line {
  width: 2px;
  height: 30px;
  background: #e5e7eb;
  margin-left: 19px;
}
.flow-role {
  font-size: 13px;
  color: #6b7280;
}
.flow-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin: 2px 0;
}
.flow-time {
  font-size: 12px;
  color: #9ca3af;
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
