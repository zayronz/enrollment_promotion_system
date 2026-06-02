<template>
  <div class="statistics">
    <div class="page-header">
      <h2 class="page-title">数据统计</h2>
    </div>

    <!-- Stats cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">156</div>
        <div class="stat-label">总报名数</div>
        <div class="stat-trend up">↑ 12% 较上月</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">128</div>
        <div class="stat-label">已通过</div>
        <div class="stat-trend up">↑ 82% 通过率</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">8</div>
        <div class="stat-label">待审核</div>
        <div class="stat-trend down">需及时处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">20</div>
        <div class="stat-label">涉及高中</div>
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
          <div class="overview-value primary">112 人</div>
          <t-progress :percentage="72" theme="primary" />
        </div>
        <div class="overview-item">
          <div class="overview-label">教师</div>
          <div class="overview-value warning">44 人</div>
          <t-progress :percentage="28" theme="warning" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const schoolStats = ref([
  { name: '武汉理工大学', total: 35, approved: 32, rejected: 3 },
  { name: '华中科技大学', total: 28, approved: 25, rejected: 3 },
  { name: '武汉大学', total: 22, approved: 18, rejected: 4 },
  { name: '华中师范大学', total: 18, approved: 15, rejected: 3 },
  { name: '中南财经政法大学', total: 15, approved: 12, rejected: 3 }
])

const schoolColumns = [
  { colKey: 'name', title: '学校名称', ellipsis: true },
  { colKey: 'total', title: '总计', width: 80 },
  { colKey: 'approved', title: '通过', width: 80 },
  { colKey: 'rejected', title: '拒绝', width: 80 }
]

const activityStats = ref([
  { name: '2024年寒假招生宣传', total: 45, status: '进行中' },
  { name: '校园开放日志愿者', total: 32, status: '进行中' },
  { name: '优秀学子母校行', total: 28, status: '即将开始' },
  { name: '春季招生咨询会', total: 20, status: '已结束' },
  { name: '高校联盟展会', total: 18, status: '已结束' }
])

const activityColumns = [
  { colKey: 'name', title: '活动名称', ellipsis: true },
  { colKey: 'total', title: '报名数', width: 80 },
  { colKey: 'status', title: '状态', width: 100 }
]
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
