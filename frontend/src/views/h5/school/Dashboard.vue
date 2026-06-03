<template>
  <div class="dashboard">
    <H5NavBar title="数据仪表盘" />

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-activities">
        <div class="stat-value">{{ stats.totalActivities }}</div>
        <div class="stat-label">总活动数</div>
      </div>

      <div class="stat-card stat-registrations">
        <div class="stat-value">{{ stats.totalRegistrations }}</div>
        <div class="stat-label">总报名数</div>
      </div>

      <div class="stat-card stat-pending">
        <div class="stat-value">{{ stats.pendingAudit }}</div>
        <div class="stat-label">待审核</div>
      </div>

      <div class="stat-card stat-passed">
        <div class="stat-value">{{ stats.passedCount }}</div>
        <div class="stat-label">已通过</div>
      </div>
    </div>

    <!-- 报名趋势 -->
    <div class="section-panel">
      <div class="section-title">报名趋势</div>
      <div ref="lineChartRef" class="chart-container"></div>
    </div>

    <!-- 活动类型分布 -->
    <div class="section-panel">
      <div class="section-title">活动类型分布</div>
      <div ref="pieChartRef" class="chart-container"></div>
    </div>

    <!-- 最近活动 -->
    <div class="section-panel">
      <div class="section-title">最近活动</div>
      <div class="recent-list">
        <div v-for="item in recentActivities" :key="item.id" class="recent-item">
          <div class="recent-info">
            <div class="recent-name">{{ item.name }}</div>
            <div class="recent-meta">{{ formatDate(item.activityStartTime) }}</div>
          </div>
          <t-tag :theme="item.status === 1 ? 'success' : 'default'" variant="light" size="small">
            {{ item.status === 1 ? '已发布' : '草稿' }}
          </t-tag>
        </div>
        <div v-if="recentActivities.length === 0" class="empty-text">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statsApi } from '@/api/stats'
import H5NavBar from '../components/H5NavBar.vue'

const lineChartRef = ref(null)
const pieChartRef = ref(null)

const stats = ref({
  totalActivities: 0,
  totalRegistrations: 0,
  pendingAudit: 0,
  passedCount: 0
})

const recentActivities = ref([])

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const initLineChart = () => {
  if (!lineChartRef.value) return
  const chart = echarts.init(lineChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['暂无数据'] },
    yAxis: { type: 'value' },
    series: [{ data: [0], type: 'line', smooth: true }]
  }
  chart.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [{ value: 1, name: '暂无数据' }]
    }]
  }
  chart.setOption(option)
}

const fetchStats = async () => {
  try {
    const res = await statsApi.getDashboard()
    if (res.code === 200 && res.data) {
      const data = res.data
      stats.value = {
        totalActivities: data.totalActivities || 0,
        totalRegistrations: data.totalRegistrations || 0,
        pendingAudit: data.pendingAudit || 0,
        passedCount: data.passedCount || 0
      }
      recentActivities.value = data.recentActivities || []
      await nextTick()
      initLineChart()
      initPieChart()
    }
  } catch (err) {
    console.error('获取仪表盘数据失败', err)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 12px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.stat-activities .stat-value { color: #2563eb; }
.stat-registrations .stat-value { color: #10b981; }
.stat-pending .stat-value { color: #f59e0b; }
.stat-passed .stat-value { color: #ef4444; }

.section-panel {
  background: #fff;
  border-radius: 12px;
  margin: 0 12px 12px;
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.chart-container {
  height: 200px;
  width: 100%;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.recent-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.recent-meta {
  font-size: 12px;
  color: #9ca3af;
}

.empty-text {
  text-align: center;
  color: #9ca3af;
  padding: 20px;
  font-size: 14px;
}
</style>
