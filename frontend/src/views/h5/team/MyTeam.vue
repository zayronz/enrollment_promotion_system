<template>
  <div class="my-team">
    <H5NavBar title="我的组员" />

    <div v-if="loading" class="state-tip">组员信息加载中...</div>

    <template v-else>
      <div v-if="teams.length === 0" class="state-tip">暂无当前分组，可在下方选择其他招宣组加入</div>

      <template v-else>
      <div class="tab-bar">
        <div
          v-for="(team, index) in teams"
          :key="team.registrationId"
          class="tab-item"
          :class="{ active: activeIndex === index }"
          @click="activeIndex = index"
        >
          {{ team.groupName || `招宣组${index + 1}` }}
          <div v-if="activeIndex === index" class="tab-line"></div>
        </div>
      </div>

      <div class="team-content">
      <div class="info-section">
        <div class="info-label">活动名称</div>
        <div class="info-value">{{ currentTeam.activityTitle }}</div>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="share-icon" @click="goActivity(currentTeam)"><path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6M15 3h6v6M10 14L21 3"/></svg>
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
        <div class="info-value">{{ currentTeam.contact || '-' }}</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动成员</div>
        <div class="avatars-row">
          <img
            v-for="member in currentTeam.members"
            :key="member.userId"
            :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${member.userId || member.realName}`"
            class="member-avatar"
          />
        </div>
        <div class="member-table">
          <div class="table-header">
            <span>姓名</span>
            <span>所在单位</span>
            <span>职务</span>
            <span>联系电话</span>
          </div>
          <div class="table-row" v-for="member in currentTeam.members" :key="member.userId">
            <span>{{ member.realName || '-' }}</span>
            <span>{{ member.unit || '-' }}</span>
            <span>{{ formatRole(member.role) }}</span>
            <span>{{ member.phone || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <div class="info-label">分组说明</div>
        <div class="summary-card">
          <div class="summary-title">{{ currentTeam.groupName }}</div>
          <div class="summary-text">当前分组共 {{ currentTeam.members?.length || 0 }} 人，你在本组的排序为第 {{ currentTeam.groupRank || '-' }} 位。点击活动名称右侧图标可查看活动详情。</div>
          <div class="summary-user">
            <img :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${currentTeam.leader}`" class="user-avatar-sm" />
            <span>{{ currentTeam.leader || '-' }}</span>
          </div>
          <div class="summary-time">{{ formatTime(currentTeam.createTime) }}</div>
        </div>
      </div>
    </div>

    <div class="submit-area">
      <button class="submit-btn" :disabled="exiting" @click="handleExit">
        {{ exiting ? '退出中...' : '退出本组' }}
      </button>
    </div>
      </template>

      <div class="available-section">
        <div class="available-title">可加入的其他招宣组</div>
        <div v-if="availableTeams.length === 0" class="empty-card">
          暂无可加入的其他招宣组。请先报名活动，或等待管理员完成分组。
        </div>
        <div
          v-for="team in availableTeams"
          :key="`${team.joinRegistrationId}-${team.groupName}`"
          class="available-card"
        >
          <div class="available-header">
            <div>
              <div class="available-name">{{ team.groupName }}</div>
              <div class="available-activity">{{ team.activityTitle }}</div>
            </div>
            <button class="join-btn" :disabled="joining" @click="handleJoin(team)">
              加入该组
            </button>
          </div>
          <div class="available-meta">
            <span>组长：{{ team.leader || '-' }}</span>
            <span>成员：{{ team.members?.length || 0 }}人</span>
          </div>
          <div class="available-members">
            <img
              v-for="member in team.members?.slice(0, 6)"
              :key="member.userId"
              :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${member.userId || member.realName}`"
              class="member-avatar"
            />
          </div>
        </div>
      </div>
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
const joining = ref(false)
const activeIndex = ref(0)
const teams = ref([])
const availableTeams = ref([])

const currentTeam = computed(() => teams.value[activeIndex.value] || {})

const loadTeams = async () => {
  loading.value = true
  try {
    const [myTeamsRes, availableTeamsRes] = await Promise.all([
      registrationApi.getMyTeams(),
      registrationApi.getAvailableTeams()
    ])
    teams.value = myTeamsRes.data || []
    availableTeams.value = availableTeamsRes.data || []
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

const handleJoin = async (team) => {
  if (!team?.joinRegistrationId || !team?.groupName) return
  const message = team.currentGroupName
    ? `确定从“${team.currentGroupName}”切换到“${team.groupName}”吗？`
    : `确定加入“${team.groupName}”吗？`
  if (!confirm(message)) return

  joining.value = true
  try {
    await registrationApi.joinTeam(team.joinRegistrationId, team.groupName)
    MessagePlugin.success('已加入该招宣组')
    await loadTeams()
  } catch (error) {
    // 错误由拦截器统一提示
  } finally {
    joining.value = false
  }
}

onMounted(loadTeams)
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
  overflow-x: auto;
}
.tab-item {
  font-size: 15px;
  color: #6b7280;
  padding-bottom: 8px;
  position: relative;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
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
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.state-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
  padding: 60px 20px;
}
.available-section {
  padding: 12px;
  padding-bottom: 92px;
}
.available-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 8px 0 12px;
}
.available-card,
.empty-card {
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.empty-card {
  color: #9ca3af;
  font-size: 14px;
  text-align: center;
}
.available-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.available-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}
.available-activity,
.available-meta {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}
.available-meta {
  display: flex;
  gap: 14px;
  margin-top: 10px;
}
.available-members {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}
.join-btn {
  border: none;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
  font-size: 13px;
  padding: 7px 12px;
  white-space: nowrap;
}
.join-btn:disabled {
  opacity: 0.6;
}
</style>
