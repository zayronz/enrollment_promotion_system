<template>
  <div class="statistics">
    <div class="page-header">
      <h2 class="page-title">数据统计</h2>
    </div>

    <!-- Stats cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalRegistrations }}</div>
        <div class="stat-label">总报名数</div>
        <div class="stat-trend up">{{ stats.passedCount }} 已通过</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.passedCount }}</div>
        <div class="stat-label">已通过</div>
        <div class="stat-trend up">
          通过率 {{ stats.totalRegistrations > 0 ? Math.round(stats.passedCount / stats.totalRegistrations * 100) : 0 }}%
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.pendingAudit }}</div>
        <div class="stat-label">待审核</div>
        <div class="stat-trend down">需及时处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.schoolCount }}</div>
        <div class="stat-label">涉及学校</div>
        <div class="stat-trend">覆盖学校</div>
      </div>
    </div>

    <!-- Charts -->
    <div class="charts-row">
      <div class="chart-panel">
        <h3 class="chart-title">各学校报名分布</h3>
        <div class="chart-container">
          <t-table
            :data="schoolStats"
            :columns="schoolColumns"
            row-key="name"
            hover
            size="small"
          />
        </div>
      </div>
      <div class="chart-panel">
        <h3 class="chart-title">各活动报名统计</h3>
        <div class="chart-container">
          <t-table
            :data="activityStats"
            :columns="activityColumns"
            row-key="name"
            hover
            size="small"
          />
        </div>
      </div>
    </div>

    <!-- Recent overview -->
    <div class="overview-panel">
      <h3 class="chart-title">报名人员结构</h3>
      <div class="overview-grid">
        <div class="overview-item">
          <div class="overview-label">学生</div>
          <div class="overview-value primary">{{ stats.studentCount }} 人</div>
          <t-progress :percentage="stats.studentPercent" theme="primary" />
        </div>
        <div class="overview-item">
          <div class="overview-label">教师</div>
          <div class="overview-value warning">{{ stats.teacherCount }} 人</div>
          <t-progress :percentage="stats.teacherPercent" theme="warning" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { statsApi } from '@/api/stats'

const stats = ref({
  totalRegistrations: 0,
  passedCount: 0,
  pendingAudit: 0,
  schoolCount: 0,
  studentCount: 0,
  teacherCount: 0,
  studentPercent: 0,
  teacherPercent: 0
})

const schoolStats = ref([])
const activityStats = ref([])

const schoolColumns = [
  { colKey: 'name', title: '学校名称', ellipsis: true },
  { colKey: 'total', title: '总计', width: 80 },
  { colKey: 'approved', title: '通过', width: 80 },
  { colKey: 'rejected', title: '拒绝', width: 80 }
]

const activityColumns = [
  { colKey: 'name', title: '活动名称', ellipsis: true },
  { colKey: 'total', title: '报名数', width: 80 },
  { colKey: 'status', title: '状态', width: 100 }
]

const fetchData = async () => {
  try {
    const res = await statsApi.getCollegeStats()
    if (res.code === 200 && res.data) {
      const data = res.data
      stats.value = {
        totalRegistrations: data.totalRegistrations || 0,
        passedCount: data.passedCount || 0,
        pendingAudit: data.pendingAudit || 0,
        schoolCount: data.schoolCount || 0,
        studentCount: data.studentCount || 0,
        teacherCount: data.teacherCount || 0,
        studentPercent: data.studentPercent || 0,
        teacherPercent: data.teacherPercent || 0
      }
      schoolStats.value = data.schoolStats || []
      activityStats.value = data.activityStats || []
    }
  } catch (err) {
    console.error('获取统计数据失败', err)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.statistics { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); text-align: center; }
.stat-value { font-size: 36px; font-weight: 700; color: var(--td-brand-color); line-height: 1.2; }
.stat-label { font-size: 14px; color: var(--td-text-color-secondary); margin-top: 4px; }
.stat-trend { font-size: 12px; margin-top: 8px; color: var(--td-text-color-placeholder); }
.stat-trend.up { color: var(--td-success-color); }
.stat-trend.down { color: var(--td-warning-color); }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
.chart-panel { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); }
.chart-title { font-size: 15px; font-weight: 600; color: var(--td-text-color-primary); margin: 0 0 16px; }
.chart-container { min-height: 200px; }

.overview-panel { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); }
.overview-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.overview-item { text-align: center; }
.overview-label { font-size: 14px; color: var(--td-text-color-secondary); margin-bottom: 4px; }
.overview-value { font-size: 20px; font-weight: 600; margin-bottom: 12px; }
.overview-value.primary { color: var(--td-brand-color); }
.overview-value.warning { color: var(--td-warning-color); }

@media (max-width: 960px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .overview-grid { grid-template-columns: 1fr; }
}
</style>
