<template>
  <div class="my-team">
    <H5NavBar title="我的组员" />

    <t-loading v-if="loading" text="组员信息加载中..." size="small" class="loading-wrap" />

    <template v-else>
      <div v-if="teams.length === 0" class="empty-wrap">
        <div class="empty-card">
          <div class="empty-title">暂无招宣组</div>
          <div class="empty-desc">报名审核通过并完成自动分组后，这里会展示你的招宣组成员。</div>
          <t-button theme="primary" size="small" @click="router.push('/h5/my-registrations')">查看报名进度</t-button>
        </div>
      </div>

      <template v-else>
        <div class="tab-bar">
          <div
            v-for="(team, index) in teams"
            :key="team.registrationId"
            class="tab-item"
            :class="{ active: activeIndex === index }"
            @click="activeIndex = index"
          >
            招宣组{{ index + 1 }}
            <div v-if="activeIndex === index" class="tab-line"></div>
          </div>
        </div>

        <div class="team-content">
          <div class="info-section activity-card">
            <div class="info-label">活动名称</div>
            <div class="info-value">{{ currentTeam.activityTitle || '-' }}</div>
            <button class="open-btn" @click="goActivity(currentTeam)" aria-label="查看活动详情">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#111827" stroke-width="2">
                <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
                <path d="M15 3h6v6"/>
                <path d="M10 14L21 3"/>
              </svg>
            </button>
          </div>

          <div class="info-section">
            <div class="info-label">组长</div>
            <div class="info-value">{{ currentTeam.leader || '-' }}</div>
          </div>

          <div class="info-section">
            <div class="info-label">副组长</div>
            <div class="info-value">{{ currentTeam.deputyLeader || '-' }}</div>
          </div>

          <div class="info-section">
            <div class="info-label">联络员</div>
            <div class="info-value">{{ currentTeam.contact || currentTeam.leader || '-' }}</div>
          </div>

          <div class="info-section">
            <div class="info-label">活动成员</div>
            <div class="avatars-row">
              <div
                v-for="member in sortedMembers.slice(0, 6)"
                :key="member.registrationId || member.userId"
                class="avatar-circle"
              >
                {{ getAvatarText(member.realName) }}
              </div>
            </div>
            <div class="member-table">
              <div class="table-header">
                <span>姓名</span>
                <span>所在单位</span>
                <span>职务</span>
                <span>联系电话</span>
              </div>
              <div
                v-for="member in sortedMembers"
                :key="member.registrationId || member.userId"
                class="table-row"
                :class="{ current: member.currentUser }"
              >
                <span>{{ member.realName || '-' }}</span>
                <span>{{ member.unit || member.targetSchool || '-' }}</span>
                <span>{{ formatRole(member.role) }}</span>
                <a v-if="member.phone && member.phone !== '-'" :href="`tel:${member.phone}`">{{ member.phone }}</a>
                <span v-else>-</span>
              </div>
            </div>
          </div>

          <div class="info-section summary-section">
            <div class="info-label">最近的一条活动总结</div>
            <template v-if="currentTeam.latestFeedback">
              <div class="summary-title">{{ currentTeam.latestFeedback.title || '活动总结' }}</div>
              <div class="summary-text">{{ stripHtml(currentTeam.latestFeedback.content) || '-' }}</div>
              <div class="summary-footer">
                <div class="summary-avatar">{{ getAvatarText(currentTeam.latestFeedback.userName) }}</div>
                <div>
                  <div class="summary-time">• {{ formatTime(currentTeam.latestFeedback.createTime) }}</div>
                  <div class="summary-user">{{ currentTeam.latestFeedback.userName || '-' }}</div>
                </div>
              </div>
            </template>
            <div v-else class="summary-empty">
              暂无活动总结。活动结束后，成员提交反馈后会在这里展示最近一条总结。
            </div>
          </div>

          <div class="submit-area">
            <button class="submit-btn" :disabled="exiting" @click="handleExit">
              {{ exiting ? '退出中...' : '退出本组' }}
            </button>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { registrationApi } from '@/api/registeration'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const loading = ref(false)
const exiting = ref(false)
const activeIndex = ref(0)
const teams = ref([])

const currentTeam = computed(() => teams.value[activeIndex.value] || {})
const sortedMembers = computed(() => {
  const members = currentTeam.value.members || []
  return [...members].sort((a, b) => {
    const rankA = a.groupRank ?? 9999
    const rankB = b.groupRank ?? 9999
    return rankA - rankB
  })
})

const loadTeams = async () => {
  loading.value = true
  try {
    const myTeamsRes = await registrationApi.getMyTeams()
    teams.value = myTeamsRes.data || []
    if (activeIndex.value >= teams.value.length) activeIndex.value = 0
  } catch (error) {
    MessagePlugin.error('获取组员信息失败')
  } finally {
    loading.value = false
  }
}

const formatRole = (role) => {
  const roleMap = {
    STUDENT: '学生',
    TEACHER: '教师',
    COLLEGE: '学院',
    SCHOOL: '学校'
  }
  return roleMap[role] || role || '-'
}

const formatTime = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const goActivity = (team) => {
  if (!team?.activityId) return
  router.push(`/h5/activity/${team.activityId}`)
}

const handleExit = async () => {
  const team = currentTeam.value
  if (!team?.registrationId) return
  if (!confirm('确定要退出本组吗？退出后将不再显示该分组成员。')) return

  exiting.value = true
  try {
    await registrationApi.exitTeam(team.registrationId)
    MessagePlugin.success('已退出本组')
    await loadTeams()
  } catch (error) {
    // 错误由拦截器统一提示
  } finally {
    exiting.value = false
  }
}

const getAvatarText = (name) => {
  return name ? String(name).slice(0, 1) : '组'
}

const stripHtml = (value) => {
  if (!value) return ''
  return String(value).replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
}

onMounted(loadTeams)
</script>

<style scoped>
.my-team {
  min-height: 100vh;
  background: #eef3ff;
  padding-bottom: 92px;
}
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.empty-wrap {
  padding: 18px 12px;
}
.empty-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 18px;
  text-align: center;
  box-shadow: 0 4px 14px rgba(0,0,0,0.04);
}
.empty-title {
  color: #1f2937;
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 8px;
}
.empty-desc {
  color: #9ca3af;
  font-size: 13px;
  line-height: 1.7;
  margin-bottom: 18px;
}
.tab-bar {
  display: flex;
  gap: 24px;
  padding: 12px 16px 0;
  background: #eef3ff;
  overflow-x: auto;
}
.tab-item {
  position: relative;
  font-size: 14px;
  color: #6b7280;
  padding: 0 0 8px;
  white-space: nowrap;
  cursor: pointer;
}
.tab-item.active {
  color: #111827;
  font-weight: 600;
}
.tab-line {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: #2563eb;
  border-radius: 999px;
}
.team-content {
  padding: 12px 16px 0;
}
.info-section {
  position: relative;
  background: #fff;
  border-radius: 8px;
  padding: 13px 12px;
  margin-bottom: 12px;
}
.activity-card {
  padding-right: 44px;
}
.info-label {
  color: #8b95a5;
  font-size: 12px;
  margin-bottom: 9px;
}
.info-label::before {
  content: '•';
  margin-right: 4px;
  color: #9ca3af;
}
.info-value {
  color: #111827;
  font-size: 15px;
  line-height: 1.4;
}
.open-btn {
  position: absolute;
  right: 14px;
  top: 28px;
  width: 28px;
  height: 28px;
  border: none;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatars-row {
  display: flex;
  align-items: center;
  gap: 0;
  margin: 2px 0 12px;
}
.avatar-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #eef2ff, #dbeafe);
  color: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  margin-right: -6px;
  font-size: 12px;
  font-weight: 600;
}
.member-table {
  border-top: 1px dashed #e5e7eb;
  padding-top: 10px;
  overflow-x: auto;
}
.table-header,
.table-row {
  display: grid;
  grid-template-columns: 1.1fr 1.3fr 1fr 1.5fr;
  min-width: 318px;
  column-gap: 8px;
  font-size: 12px;
  line-height: 32px;
}
.table-header {
  color: #6b7280;
  background: #eef3ff;
  padding: 0 8px;
}
.table-row {
  color: #111827;
  padding: 0 8px;
  border-bottom: 1px solid #f3f4f6;
}
.table-row.current {
  color: #2563eb;
  font-weight: 600;
}
.table-row a {
  color: #111827;
  text-decoration: none;
}
.summary-section {
  padding-bottom: 14px;
}
.summary-title {
  color: #111827;
  font-size: 15px;
  line-height: 1.4;
  margin-bottom: 8px;
}
.summary-text {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e5e7eb;
}
.summary-empty {
  color: #9ca3af;
  font-size: 13px;
  line-height: 1.6;
}
.summary-footer {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-top: 12px;
}
.summary-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fee2e2, #fce7f3);
  color: #9f1239;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}
.summary-time {
  color: #9ca3af;
  font-size: 12px;
}
.summary-user {
  color: #111827;
  font-size: 14px;
  margin-top: 2px;
}
.submit-area {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  max-width: 480px;
  margin: 0 auto;
  background: #eef3ff;
  padding: 12px 16px 18px;
  z-index: 20;
}
.submit-btn {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 7px;
  background: #2f6bff;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
}
.submit-btn:disabled {
  opacity: 0.6;
}
</style>
