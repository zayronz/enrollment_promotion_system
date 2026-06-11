<template>
  <div class="pc-test-list">
    <div class="page-header">
      <div>
        <h2 class="page-title">招生能力测试</h2>
        <p class="page-desc">完成招宣知识与业务能力测试，系统会记录测试结果。</p>
      </div>
    </div>

    <t-tabs v-model="activeTab" class="test-tabs">
      <t-tab-panel value="ongoing" label="进行中" />
      <t-tab-panel value="completed" label="已完成" />
    </t-tabs>

    <div class="test-grid">
      <t-card
        v-for="item in currentList"
        :key="item.id"
        class="test-card"
        hover-shadow
        @click="goPage(item)"
      >
        <div class="card-top">
          <div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
          <t-tag v-if="item.score" theme="success" variant="light">{{ item.score }} 分</t-tag>
          <t-tag v-else theme="primary" variant="light">待完成</t-tag>
        </div>
        <div class="meta-grid">
          <div><span>开始日期</span><strong>{{ item.startDate }}</strong></div>
          <div><span>结束日期</span><strong>{{ item.endDate }}</strong></div>
          <div><span>参与情况</span><strong>{{ item.joinText }}</strong></div>
          <div v-if="item.countdown"><span>剩余时间</span><strong class="warning">{{ item.countdown }}</strong></div>
        </div>
        <template #footer>
          <div class="card-footer">
            <span>{{ activeTab === 'ongoing' ? '点击进入测试说明' : '点击查看测试结果' }}</span>
            <t-button theme="primary" variant="text">
              {{ activeTab === 'ongoing' ? '开始' : '查看' }}
            </t-button>
          </div>
        </template>
      </t-card>
    </div>

    <t-empty v-if="currentList.length === 0" :description="`暂无${activeTab === 'ongoing' ? '进行中' : '已完成'}的测试`" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activeTab = ref('ongoing')

const ongoingList = [
  {
    id: 1,
    title: '2023届招生能力测试题目',
    desc: '覆盖招生政策理解、宣讲沟通和常见问题处理能力。',
    startDate: '2023/03/29',
    endDate: '2023/03/29',
    countdown: '3天4时5分',
    joinText: '28人已经完成，赶紧测试吧！'
  },
  {
    id: 2,
    title: '招生宣传实务能力测试',
    desc: '用于检验招宣人员对活动流程和报名规则的掌握情况。',
    startDate: '2023/04/01',
    endDate: '2023/04/05',
    countdown: '5天2时10分',
    joinText: '16人已经完成'
  }
]

const completedList = [
  {
    id: 101,
    title: '2022届招生能力测试题目',
    desc: '历史测试记录，可查看答题明细和得分情况。',
    startDate: '2022/03/15',
    endDate: '2022/03/20',
    score: 61,
    joinText: '156人已完成'
  }
]

const currentList = computed(() => activeTab.value === 'ongoing' ? ongoingList : completedList)

const roleBase = computed(() => route.path.startsWith('/teacher') ? '/teacher' : '/student')

const goPage = (item) => {
  if (activeTab.value === 'ongoing') {
    router.push(`${roleBase.value}/test/start/${item.id}`)
  } else {
    router.push(`${roleBase.value}/test/result/${item.id}`)
  }
}
</script>

<style scoped>
.pc-test-list {
  padding: 0;
}
.page-header {
  background: linear-gradient(135deg, #f7faff, #ffffff);
  border: 1px solid var(--td-border-level-1-color);
  border-radius: 14px;
  padding: 22px 24px;
  margin-bottom: 18px;
}
.page-title {
  margin: 0;
  color: var(--td-text-color-primary);
  font-size: 22px;
  font-weight: 700;
}
.page-desc {
  margin: 8px 0 0;
  color: var(--td-text-color-secondary);
}
.test-tabs {
  margin-bottom: 16px;
}
.test-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}
.test-card {
  cursor: pointer;
}
.card-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}
.card-top h3 {
  margin: 0 0 8px;
  color: var(--td-text-color-primary);
  font-size: 17px;
}
.card-top p {
  margin: 0;
  color: var(--td-text-color-secondary);
  line-height: 1.6;
}
.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}
.meta-grid span {
  display: block;
  color: var(--td-text-color-placeholder);
  font-size: 12px;
  margin-bottom: 4px;
}
.meta-grid strong {
  color: var(--td-text-color-primary);
  font-weight: 600;
}
.meta-grid .warning {
  color: #f59e0b;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--td-text-color-secondary);
}
</style>
