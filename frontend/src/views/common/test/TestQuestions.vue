<template>
  <div class="pc-test-questions">
    <div class="notice">
      对招宣知识的掌握程度以及理解分析信息和解决问题的能力，可以更好地武装头脑，指导实践推动工作。
    </div>

    <t-card class="section-card" title="一、单选题">
      <div v-for="(q, idx) in singleQuestions" :key="idx" class="question-item">
        <div class="question-title">{{ idx + 1 }}. {{ q.title }}</div>
        <t-radio-group v-model="q.answer">
          <t-radio v-for="opt in q.options" :key="opt.value" :value="opt.value">{{ opt.label }}</t-radio>
        </t-radio-group>
      </div>
    </t-card>

    <t-card class="section-card" title="二、多选题">
      <div v-for="(q, idx) in multiQuestions" :key="idx" class="question-item">
        <div class="question-title">{{ idx + 1 }}. {{ q.title }}</div>
        <t-checkbox-group v-model="q.answers">
          <t-checkbox v-for="opt in q.options" :key="opt.value" :value="opt.value">{{ opt.label }}</t-checkbox>
        </t-checkbox-group>
      </div>
    </t-card>

    <t-card class="section-card" title="三、填空题">
      <div v-for="(q, idx) in fillQuestions" :key="idx" class="question-item">
        <div class="question-title">{{ idx + 1 }}. {{ q.title }}</div>
        <t-input v-model="q.answer" placeholder="请输入答案" clearable />
      </div>
    </t-card>

    <div class="action-bar">
      <t-button theme="primary" size="large" @click="handleSubmit">立即提交</t-button>
      <t-button size="large" variant="outline" @click="handleSave">保存记录</t-button>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()
const route = useRoute()
const roleBase = computed(() => route.path.startsWith('/teacher') ? '/teacher' : '/student')

const singleQuestions = reactive([
  {
    title: '燃烧是一种热发光的（ ）',
    options: [
      { label: 'A、化学反应', value: 'A' },
      { label: 'B、物理反应', value: 'B' },
      { label: 'C、广电反应', value: 'C' },
      { label: 'D、分解反应', value: 'D' }
    ],
    answer: ''
  },
  {
    title: '燃烧过程中的化学反应十分复杂，有（ ），有分解反应。',
    options: [
      { label: 'A、氧化反应', value: 'A' },
      { label: 'B、化合反应', value: 'B' },
      { label: 'C、聚合反应', value: 'C' },
      { label: 'D、连锁反应', value: 'D' }
    ],
    answer: ''
  }
])

const multiQuestions = reactive([
  {
    title: '生产力构成要素包括（ ）',
    options: [
      { label: 'A、生产资料', value: 'A' },
      { label: 'B、劳动对象', value: 'B' },
      { label: 'C、技术水平', value: 'C' },
      { label: 'D、劳动力', value: 'D' },
      { label: 'E、艺术价值', value: 'E' }
    ],
    answers: []
  },
  {
    title: '招生宣传活动前需要重点准备哪些内容？',
    options: [
      { label: 'A、学校招生政策', value: 'A' },
      { label: 'B、活动宣讲材料', value: 'B' },
      { label: 'C、报名审核规则', value: 'C' },
      { label: 'D、目标学校名单', value: 'D' }
    ],
    answers: []
  }
])

const fillQuestions = reactive([
  { title: '曾子所著，专讲古代大学教育的是', answer: '' }
])

const handleSubmit = () => {
  MessagePlugin.success('测试提交成功')
  router.push(`${roleBase.value}/test/result/${route.params.id}`)
}

const handleSave = () => {
  MessagePlugin.success('已保存答题记录')
}
</script>

<style scoped>
.pc-test-questions {
  padding-bottom: 24px;
}
.notice {
  background: #fff7e6;
  border: 1px solid #ffe1a6;
  color: #92400e;
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  line-height: 1.6;
}
.section-card {
  margin-bottom: 16px;
  border-radius: 12px;
}
.question-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--td-border-level-1-color);
}
.question-item:last-child {
  border-bottom: none;
}
.question-title {
  color: var(--td-text-color-primary);
  font-weight: 600;
  margin-bottom: 12px;
  line-height: 1.7;
}
.action-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: center;
  gap: 12px;
  background: rgba(245, 247, 250, 0.92);
  backdrop-filter: blur(6px);
  padding: 16px;
  border-radius: 12px;
}
</style>
