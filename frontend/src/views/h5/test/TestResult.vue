<template>
  <div class="test-result">
    <H5NavBar title="测试" />

    <div class="notice-bar">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <span>对招宣知识的掌握程度以及理解分析信息和解决问题的能力，可以更好地武装头脑，指导实践推动工作。</span>
    </div>

    <div class="result-card">
      <div class="result-stamp">已结束</div>
      <div class="result-title">学情（"三考三促"）线上考试试卷</div>
      <div class="result-meta">
        <div>开始日期：2023/03/29</div>
        <div>考试年度：2022年</div>
        <div>评卷人：QIANJIA XIN（10000）</div>
        <div>考生：WANGDAQ IANG（10001）</div>
        <div>结束日期：2023/03/29</div>
        <div>得分：<span class="score">61分</span></div>
      </div>
    </div>

    <div class="questions-review">
      <div class="section">
        <div class="section-tag">一单选题</div>
        <div class="review-item" v-for="(q, idx) in singleQuestions" :key="idx">
          <div class="review-title">{{ idx + 1 }}.{{ q.title }}（ {{ q.userAnswer }} ）</div>
          <div class="review-options">
            <div v-for="(opt, oIdx) in q.options" :key="oIdx" :class="['review-option', getOptionClass(q, opt)]">
              {{ opt.label }}
            </div>
          </div>
          <div class="review-answer">
            正确答案：{{ q.correctAnswer }} <span v-if="q.userAnswer === q.correctAnswer" class="correct-mark">+1分</span>
            <span v-else class="wrong-mark">答错</span>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-tag blue">二多选题</div>
        <div class="review-item" v-for="(q, idx) in multiQuestions" :key="idx">
          <div class="review-title">{{ idx + 1 }}.{{ q.title }}（ {{ q.userAnswer.join(' ') }} ）</div>
          <div class="review-options">
            <div v-for="(opt, oIdx) in q.options" :key="oIdx" :class="['review-option', getMultiClass(q, opt)]">
              {{ opt.label }}
            </div>
          </div>
          <div class="review-answer">
            正确答案：{{ q.correctAnswer.join(' ') }}
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-tag blue">三填空题</div>
        <div class="review-item" v-for="(q, idx) in fillQuestions" :key="idx">
          <div class="review-title">{{ idx + 1 }}.{{ q.title }}</div>
          <div class="fill-answer">{{ q.userAnswer }}</div>
          <div class="review-answer">
            正确答案：{{ q.correctAnswer }} <span class="correct-mark">+1分</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import H5NavBar from '../components/H5NavBar.vue'

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
  },
  {
    title: '燃烧过程中的化学反应十分复杂，有（ ），有分解反应。',
    options: [
      { label: 'A、氧化反应', value: 'A' },
      { label: 'B、化合反应', value: 'B' },
      { label: 'C、聚合反应', value: 'C' },
      { label: 'D、连锁反应', value: 'D' }
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
      { label: 'D、劳动力', value: 'D' },
      { label: 'E、艺术价值', value: 'E' }
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
.test-result {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}
.notice-bar {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: #fef3c7;
  padding: 10px 12px;
  font-size: 12px;
  color: #92400e;
  line-height: 1.5;
}
.result-card {
  background: #fff;
  border-radius: 12px;
  margin: 12px;
  padding: 16px;
  position: relative;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.result-stamp {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 60px;
  height: 60px;
  border: 2px solid #10b981;
  color: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  transform: rotate(-15deg);
  opacity: 0.7;
}
.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 10px;
}
.result-meta {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}
.result-meta .score {
  color: #ef4444;
  font-weight: 600;
}
.questions-review {
  padding: 0 12px;
}
.section {
  margin-bottom: 16px;
}
.section-tag {
  display: inline-block;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  font-size: 13px;
  padding: 4px 14px;
  border-radius: 14px;
  margin-bottom: 10px;
}
.review-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 10px;
}
.review-title {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 10px;
  line-height: 1.6;
}
.review-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}
.review-option {
  font-size: 13px;
  color: #6b7280;
  padding: 6px 10px;
  border-radius: 6px;
  background: #f9fafb;
}
.review-option.correct {
  background: #d1fae5;
  color: #065f46;
}
.review-option.wrong {
  background: #fee2e2;
  color: #991b1b;
}
.review-answer {
  font-size: 13px;
  color: #6b7280;
}
.correct-mark {
  color: #10b981;
  font-weight: 500;
}
.wrong-mark {
  color: #ef4444;
  font-weight: 500;
}
.fill-answer {
  background: #f3f4f6;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  color: #374151;
  margin-bottom: 8px;
}
</style>
