<template>
  <div class="dashboard">
    <h2 class="page-title">数据仪表盘</h2>

    <!-- Stats cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalActivities }}</div>
          <div class="stat-label">总活动数</div>
        </div>
        <div class="stat-icon"><BrowseIcon /></div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalRegistrations }}</div>
          <div class="stat-label">总报名数</div>
        </div>
        <div class="stat-icon"><UserIcon /></div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingAudit }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-icon"><TimeIcon /></div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ stats.passedCount }}</div>
          <div class="stat-label">已通过</div>
        </div>
        <div class="stat-icon"><CheckCircleIcon /></div>
      </div>
    </div>

    <!-- Charts -->
    <div class="charts-row">
      <div class="chart-panel">
        <h3 class="chart-title">报名趋势</h3>
        <div class="chart-placeholder">
          <t-empty description="图表组件需要安装 echarts" />
        </div>
      </div>
      <div class="chart-panel">
        <h3 class="chart-title">活动类型分布</h3>
        <div class="chart-placeholder">
          <t-empty description="图表组件需要安装 echarts" />
        </div>
      </div>
    </div>

    <!-- Recent activities -->
    <div class="recent-panel">
      <h3 class="chart-title">最近活动</h3>
      <t-table
        :data="recentActivities"
        :columns="recentColumns"
        row-key="id"
        hover
        stripe
        size="small"
      >
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'default'" variant="light" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </t-tag>
        </template>
        <template #activityStartTime="{ row }">
          {{ formatDate(row.activityStartTime) }}
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { BrowseIcon, UserIcon, TimeIcon, CheckCircleIcon } from 'tdesign-icons-vue-next'

const stats = ref({
  totalActivities: 0,
  totalRegistrations: 0,
  pendingAudit: 0,
  passedCount: 0
})

const recentActivities = ref([])

const recentColumns = [
  { colKey: 'name', title: '活动名称', ellipsis: true },
  { colKey: 'activityStartTime', title: '活动时间', width: 180 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'registrationCount', title: '报名人数', width: 100 }
]

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

onMounted(() => {
  stats.value = {
    totalActivities: 12,
    totalRegistrations: 156,
    pendingAudit: 8,
    passedCount: 120
  }

  recentActivities.value = [
    { id: 1, name: '2024年寒假招生宣传', activityStartTime: '2024-12-20', status: 1, registrationCount: 45 },
    { id: 2, name: '校园开放日志愿者招募', activityStartTime: '2024-11-15', status: 1, registrationCount: 32 },
    { id: 3, name: '优秀学子母校行', activityStartTime: '2025-01-10', status: 0, registrationCount: 0 }
  ]
})
</script>

<style scoped>
.dashboard { padding: 0; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0 0 24px; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 12px; padding: 24px; position: relative; overflow: hidden; border: 1px solid var(--td-border-level-1-color); }
.stat-content { text-align: center; padding: 8px 0; }
.stat-value { font-size: 32px; font-weight: 700; color: var(--td-brand-color); line-height: 1.2; }
.stat-label { font-size: 14px; color: var(--td-text-color-secondary); margin-top: 8px; }
.stat-icon { position: absolute; right: 20px; bottom: 20px; font-size: 48px; color: rgba(0, 82, 217, 0.12); }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
.chart-panel { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); }
.chart-title { font-size: 15px; font-weight: 600; color: var(--td-text-color-primary); margin: 0 0 16px; }
.chart-placeholder { height: 300px; display: flex; align-items: center; justify-content: center; }

.recent-panel { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); }

@media (max-width: 960px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
