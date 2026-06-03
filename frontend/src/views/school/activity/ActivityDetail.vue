<template>
  <div class="activity-detail">
    <div class="page-header">
      <t-button theme="default" variant="outline" @click="$router.back()">
        <template #icon><ArrowLeftIcon /></template>
        返回
      </t-button>
      <t-space>
        <t-button theme="primary" variant="outline" @click="$router.push(`/school/activity/edit/${id}`)">
          <template #icon><EditIcon /></template>
          编辑活动
        </t-button>
      </t-space>
    </div>

    <t-loading :loading="loading" size="large">
      <div v-if="detail" class="detail-content">
        <!-- 封面图 -->
        <div v-if="detail.coverImage" class="cover-section">
          <img :src="detail.coverImage" :alt="detail.name" class="cover-image" />
        </div>

        <!-- 活动信息 -->
        <div class="info-section">
          <h1 class="activity-name">{{ detail.name }}</h1>
          <div class="info-meta">
            <t-tag :theme="detail.type === 0 ? 'primary' : detail.type === 1 ? 'success' : 'warning'" variant="light">
              {{ typeMap[detail.type] || '未知类型' }}
            </t-tag>
            <t-tag :theme="statusTheme" variant="light">
              {{ statusMap[detail.status] || '未知状态' }}
            </t-tag>
          </div>

          <t-descriptions :column="2" bordered class="info-table">
            <t-descriptions-item label="活动地点">{{ detail.location || '-' }}</t-descriptions-item>
            <t-descriptions-item label="活动类型">{{ typeMap[detail.type] || '-' }}</t-descriptions-item>
            <t-descriptions-item label="活动时间">
              {{ formatDate(detail.activityStartTime) }} ~ {{ formatDate(detail.activityEndTime) }}
            </t-descriptions-item>
            <t-descriptions-item label="报名时间">
              {{ formatDate(detail.registrationStartTime) }} ~ {{ formatDate(detail.registrationEndTime) }}
            </t-descriptions-item>
            <t-descriptions-item label="每校学生上限">{{ detail.maxStudentPerSchool || '不限' }}</t-descriptions-item>
            <t-descriptions-item label="每校教师上限">{{ detail.maxTeacherPerSchool || '不限' }}</t-descriptions-item>
            <t-descriptions-item label="自动分组">
              <t-tag :theme="detail.autoGroup ? 'success' : 'default'" variant="light" size="small">
                {{ detail.autoGroup ? '开启' : '关闭' }}
              </t-tag>
            </t-descriptions-item>
          </t-descriptions>

          <!-- 视频 -->
          <div v-if="detail.videoUrl" class="video-section">
            <h3 class="section-title">视频介绍</h3>
            <video :src="detail.videoUrl" controls class="video-player"></video>
          </div>

          <!-- 活动描述 -->
          <div class="desc-section">
            <h3 class="section-title">活动介绍</h3>
            <div class="desc-content" v-html="detail.description"></div>
          </div>

          <!-- 轮播图/Banner -->
          <div v-if="detail.bannerUrl" class="banner-section">
            <h3 class="section-title">Banner 轮播图</h3>
            <img :src="detail.bannerUrl" :alt="detail.name" class="banner-image" />
          </div>

          <!-- 审批流程 -->
          <div v-if="detail.auditFlow" class="flow-section">
            <h3 class="section-title">审批流程</h3>
            <t-steps :current="detail.auditFlow.length" layout="horizontal">
              <t-step-item v-for="(step, idx) in detail.auditFlow" :key="idx" :title="step.name || step" />
            </t-steps>
          </div>

          <!-- 自定义字段 -->
          <div v-if="detail.customFields && detail.customFields.length" class="fields-section">
            <h3 class="section-title">自定义字段</h3>
            <t-table
              :data="detail.customFields"
              :columns="fieldColumns"
              row-key="name"
              size="small"
              bordered
            />
          </div>
        </div>
      </div>
    </t-loading>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { activityApi } from '@/api/activity'
import { MessagePlugin } from 'tdesign-vue-next'
import { ArrowLeftIcon, EditIcon } from 'tdesign-icons-vue-next'

const route = useRoute()
const id = ref(route.params.id)

// ID 无效时提前拦截
if (!id.value) {
  MessagePlugin.warning('活动ID无效')
}

const detail = ref(null)
const loading = ref(false)

const typeMap = { 0: '校内活动', 1: '线上宣讲', 2: '线下招生', 3: '校园开放日', 4: '校外活动' }
const statusMap = { 0: '草稿', 1: '已发布', 2: '已结束' }

const statusTheme = computed(() => {
  const map = { 0: 'default', 1: 'success', 2: 'warning' }
  return map[detail.value?.status] || 'default'
})

const fieldColumns = [
  { colKey: 'name', title: '字段名', width: 120 },
  { colKey: 'label', title: '显示名', width: 120 },
  { colKey: 'type', title: '类型', width: 100 },
  { colKey: 'required', title: '必填', width: 80 }
]

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const fetchDetail = async () => {
  if (!id.value) return
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(id.value)
    if (res.code === 200 && res.data) {
      detail.value = res.data
    }
  } catch (err) {
    console.error('获取活动详情失败', err)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.activity-detail { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }

.cover-section { margin-bottom: 20px; border-radius: 12px; overflow: hidden; }
.cover-image { width: 100%; max-height: 320px; object-fit: cover; display: block; }

.info-section { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid var(--td-border-level-1-color); }
.activity-name { font-size: 22px; font-weight: 700; color: var(--td-text-color-primary); margin: 0 0 12px; }
.info-meta { display: flex; gap: 8px; margin-bottom: 20px; }
.info-table { margin-bottom: 24px; }

.section-title { font-size: 16px; font-weight: 600; color: var(--td-text-color-primary); margin: 24px 0 12px; }

.video-section .video-player { width: 100%; max-height: 400px; border-radius: 8px; }
.desc-section .desc-content { line-height: 1.8; color: var(--td-text-color-secondary); }
.banner-section .banner-image { width: 100%; max-height: 200px; object-fit: cover; border-radius: 8px; }

.flow-section { margin-bottom: 24px; }
.fields-section { margin-bottom: 24px; }

@media (max-width: 640px) {
  .page-header { flex-direction: column; gap: 12px; align-items: flex-start; }
}
</style>
