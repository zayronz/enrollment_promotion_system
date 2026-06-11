<template>
  <div class="pc-test-result">
    <t-card class="result-card">
      <div class="result-header">
        <div>
          <h2>招生能力测试结果</h2>
          <p>测试已结束，可查看答题明细和得分情况。</p>
        </div>
        <div class="score-box">
          <strong>61</strong>
          <span>分</span>
        </div>
      </div>
      <t-descriptions :column="3" bordered>
        <t-descriptions-item label="开始日期">2023/03/29</t-descriptions-item>
        <t-descriptions-item label="考试年度">2022年</t-descriptions-item>
        <t-descriptions-item label="评卷人">QIANJIA XIN（10000）</t-descriptions-item>
        <t-descriptions-item label="考生">WANGDAQ IANG（10001）</t-descriptions-item>
        <t-descriptions-item label="结束日期">2023/03/29</t-descriptions-item>
        <t-descriptions-item label="状态">已结束</t-descriptions-item>
      </t-descriptions>
    </t-card>

    <t-card class="section-card" title="一、单选题">
      <div v-for="(q, idx) in singleQuestions" :key="idx" class="review-item">
        <div class="review-title">{{ idx + 1 }}. {{ q.title }}（你的答案：{{ q.userAnswer }}）</div>
        <div class="option-grid">
          <div v-for="opt in q.options" :key="opt.value" :class="['option', getOptionClass(q, opt)]">{{ opt.label }}</div>
        </div>
        <div class="answer">正确答案：{{ q.correctAnswer }}</div>
      </div>
    </t-card>

    <t-card class="section-card" title="二、多选题">
      <div v-for="(q, idx) in multiQuestions" :key="idx" class="review-item">
        <div class="review-title">{{ idx + 1 }}. {{ q.title }}（你的答案：{{ q.userAnswer.join('、') }}）</div>
        <div class="option-grid">
          <div v-for="opt in q.options" :key="opt.value" :class="['option', getMultiClass(q, opt)]">{{ opt.label }}</div>
        </div>
        <div class="answer">正确答案：{{ q.correctAnswer.join('、') }}</div>
      </div>
    </t-card>

    <t-card class="section-card" title="三、填空题">
      <div v-for="(q, idx) in fillQuestions" :key="idx" class="review-item">
        <div class="review-title">{{ idx + 1 }}. {{ q.title }}</div>
        <div class="answer">你的答案：{{ q.userAnswer }}；正确答案：{{ q.correctAnswer }}</div>
      </div>
    </t-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const singleQuestions = ref([
  {
    title: '燃烧是一种热发光的（ ）',
    options: [
      { label: 'A、化学反应', value: 'A' },
      { label: 'B、物理反应', value: 'B' },
      { label: 'C、广电反应', value: 'C' },
      { label: 'D、分解反应', value: 'D' }
    ],
    userAnswer: 'B',
    correctAnswer: 'B'
  }
])

const multiQuestions = ref([
  {
    title: '生产力构成要素包括（ ）',
    options: [
      { label: 'A、生产资料', value: 'A' },
      { label: 'B、劳动对象', value: 'B' },
      { label: 'C、技术水平', value: 'C' },
      { label: 'D、劳动力', value: 'D' }
    ],
    userAnswer: ['A', 'D'],
    correctAnswer: ['A', 'B']
  }
])

const fillQuestions = ref([
  { title: '曾子所著，专讲古代大学教育的是', userAnswer: '大学', correctAnswer: '大学' }
])

const getOptionClass = (q, opt) => {
  if (opt.value === q.correctAnswer) return 'correct'
  if (opt.value === q.userAnswer && q.userAnswer !== q.correctAnswer) return 'wrong'
  return ''
}

const getMultiClass = (q, opt) => {
  const isCorrect = q.correctAnswer.includes(opt.value)
  const isSelected = q.userAnswer.includes(opt.value)
  if (isCorrect) return 'correct'
  if (isSelected && !isCorrect) return 'wrong'
  return ''
}
</script>

<style scoped>
.result-card,
.section-card {
  border-radius: 12px;
  margin-bottom: 16px;
}
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.result-header h2 {
  margin: 0 0 8px;
}
.result-header p {
  margin: 0;
  color: var(--td-text-color-secondary);
}
.score-box {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 4px solid #10b981;
  color: #10b981;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.score-box strong {
  font-size: 36px;
}
.review-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--td-border-level-1-color);
}
.review-item:last-child {
  border-bottom: none;
}
.review-title {
  font-weight: 600;
  margin-bottom: 12px;
}
.option-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 10px;
}
.option {
  padding: 8px 12px;
  border-radius: 8px;
  background: var(--td-bg-color-secondarycontainer);
  color: var(--td-text-color-secondary);
}
.option.correct {
  background: #d1fae5;
  color: #065f46;
}
.option.wrong {
  background: #fee2e2;
  color: #991b1b;
}
.answer {
  color: var(--td-text-color-secondary);
}
</style>
