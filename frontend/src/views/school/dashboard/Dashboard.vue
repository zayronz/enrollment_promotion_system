<template>
  <div class="dashboard">
    <h2 class="page-title">数据仪表盘</h2>

    <!-- Stats cards -->
    <div class="stats-grid">
      <div class="stat-card stat-activities">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalActivities }}</div>
          <div class="stat-label">总活动数</div>
        </div>
        <div class="stat-icon"><BrowseIcon /></div>
      </div>

      <div class="stat-card stat-registrations">
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalRegistrations }}</div>
          <div class="stat-label">总报名数</div>
        </div>
        <div class="stat-icon"><UserIcon /></div>
      </div>

      <div class="stat-card stat-pending">
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingAudit }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-icon"><TimeIcon /></div>
      </div>

      <div class="stat-card stat-passed">
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
        <div ref="lineChartRef" class="chart-container"></div>
      </div>
      <div class="chart-panel">
        <h3 class="chart-title">活动类型分布</h3>
        <div ref="pieChartRef" class="chart-container"></div>
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statsApi } from '@/api/stats'
import { BrowseIcon, UserIcon, TimeIcon, CheckCircleIcon } from 'tdesign-icons-vue-next'

const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChartInstance = null
let pieChartInstance = null

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
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const chartData = ref({ monthlyTrend: [], typeDistribution: [] })

const initLineChart = () => {
  if (!lineChartRef.value) return
  lineChartInstance = echarts.init(lineChartRef.value)
  const trend = chartData.value.monthlyTrend || []
  const months = trend.map(t => {
    const parts = (t.month || '').split('-')
    return parts.length > 1 ? `${parseInt(parts[1])}月` : t.month
  })
  const counts = trend.map(t => t.count || 0)

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: months.length > 0 ? months : ['暂无数据'],
      axisLine: { lineStyle: { color: 'var(--td-border-level-1-color, #e8e8e8)' } },
      axisLabel: { color: 'var(--td-text-color-secondary, rgba(0,0,0,0.6))' }
    },
    yAxis: {
      type: 'value',
      name: '报名人数',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: 'var(--td-border-level-1-color, #e8e8e8)' } },
      axisLabel: { color: 'var(--td-text-color-secondary, rgba(0,0,0,0.6))' }
    },
    series: [{
      data: counts.length > 0 ? counts : [0],
      type: 'line',
      smooth: true,
      lineStyle: { color: 'var(--td-brand-color, #0052d9)', width: 3 },
      itemStyle: { color: 'var(--td-brand-color, #0052d9)' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 82, 217, 0.2)' },
          { offset: 1, color: 'rgba(0, 82, 217, 0.02)' }
        ])
      },
      symbol: 'circle',
      symbolSize: 6
    }]
  }
  lineChartInstance.setOption(option)
}

const pieColors = ['#366ef4', '#2ba471', '#e37318', '#d54941', '#8b5cf6', '#06b6d4']

const initPieChart = () => {
  if (!pieChartRef.value) return
  pieChartInstance = echarts.init(pieChartRef.value)
  const distribution = (chartData.value.typeDistribution || []).map((item, idx) => ({
    value: item.value || 0,
    name: item.name || '未知',
    itemStyle: { color: pieColors[idx % pieColors.length] }
  }))

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center', itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['45%', '72%'],
      center: ['38%', '50%'],
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: distribution.length > 0 ? distribution : [{ value: 1, name: '暂无数据', itemStyle: { color: '#ccc' } }]
    }]
  }
  pieChartInstance.setOption(option)
}

const handleResize = () => {
  lineChartInstance?.resize()
  pieChartInstance?.resize()
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
      chartData.value = {
        monthlyTrend: data.monthlyTrend || [],
        typeDistribution: data.typeDistribution || []
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

onMounted(async () => {
  await fetchStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChartInstance?.dispose()
  pieChartInstance?.dispose()
})
</script>

<style scoped>
.dashboard { padding: 0; }
.page-title { font-size: 20px; font-weight: 700; color: var(--td-text-color-primary); margin: 0 0 24px; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 12px; padding: 24px; position: relative; overflow: hidden; border: 1px solid var(--td-border-level-1-color); transition: box-shadow 0.3s, transform 0.2s; cursor: default; }
.stat-card:hover { box-shadow: var(--td-shadow-2); transform: translateY(-2px); }
.stat-content { text-align: center; padding: 8px 0; }
.stat-value { font-size: 32px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 14px; color: var(--td-text-color-secondary); margin-top: 8px; }
.stat-icon { position: absolute; right: 20px; bottom: 20px; font-size: 48px; opacity: 0.12; }

.stat-activities .stat-value { color: var(--td-brand-color); }
.stat-registrations .stat-value { color: var(--td-success-color-5, #2ba471); }
.stat-pending .stat-value { color: var(--td-warning-color-5, #e37318); }
.stat-passed .stat-value { color: var(--td-error-color-6, #d54941); }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
.chart-panel { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); }
.chart-title { font-size: 15px; font-weight: 600; color: var(--td-text-color-primary); margin: 0 0 16px; }
.chart-container { height: 320px; width: 100%; }

.recent-panel { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); }

@media (max-width: 960px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
