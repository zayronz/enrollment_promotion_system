<template>
  <div class="test-questions">
    <H5NavBar title="测试" />

    <div class="notice-bar">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <span>对招宣知识的掌握程度以及理解分析信息和解决问题的能力，可以更好地武装头脑，指导实践推动工作。</span>
    </div>

    <div class="questions-content">
      <!-- 单选题 -->
      <div class="section">
        <div class="section-tag">一单选题</div>
        <div class="question-item" v-for="(q, idx) in singleQuestions" :key="idx">
          <div class="question-title">{{ idx + 1 }}.{{ q.title }}</div>
          <div class="options">
            <label v-for="(opt, oIdx) in q.options" :key="oIdx" class="option-item">
              <input type="radio" :name="`single-${idx}`" :value="opt.value" v-model="q.answer" />
              <span>{{ opt.label }}</span>
            </label>
          </div>
        </div>
      </div>

      <!-- 多选题 -->
      <div class="section">
        <div class="section-tag blue">二多选题</div>
        <div class="question-item" v-for="(q, idx) in multiQuestions" :key="idx">
          <div class="question-title">{{ idx + 1 }}.{{ q.title }}</div>
          <div class="options">
            <label v-for="(opt, oIdx) in q.options" :key="oIdx" class="option-item">
              <input type="checkbox" :value="opt.value" v-model="q.answers" />
              <span>{{ opt.label }}</span>
            </label>
          </div>
        </div>
      </div>

      <!-- 填空题 -->
      <div class="section">
        <div class="section-tag blue">三填空题</div>
        <div class="question-item" v-for="(q, idx) in fillQuestions" :key="idx">
          <div class="question-title">{{ idx + 1 }}.{{ q.title }}</div>
          <input v-model="q.answer" class="fill-input" placeholder="请输入答案" />
        </div>
      </div>
    </div>

    <div class="action-area">
      <button class="submit-btn" @click="handleSubmit">立即提交</button>
      <button class="save-btn" @click="handleSave">保存记录</button>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const route = useRoute()

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
    title: '2006年浙江省提出了（ ）口号',
    options: [
      { label: 'A、加强技术', value: 'A' },
      { label: 'B、科技强省', value: 'B' },
      { label: 'C、创业富民', value: 'C' },
      { label: 'D、创新性省份', value: 'D' }
    ],
    answers: []
  }
])

const fillQuestions = reactive([
  { title: '曾子所著，专讲古代大学教育的是', answer: '' }
])

const handleSubmit = () => {
  router.push('/h5/submit-success')
}

const handleSave = () => {
  alert('已保存答题记录')
}
</script>

<style scoped>
.test-questions {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 100px;
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
.questions-content {
  padding: 12px;
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
.question-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 10px;
}
.question-title {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 12px;
  line-height: 1.6;
}
.options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}
.option-item input {
  width: 16px;
  height: 16px;
  accent-color: #2563eb;
}
.fill-input {
  width: 100%;
  height: 40px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  color: #374151;
  box-sizing: border-box;
  background: #f9fafb;
}
.action-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  background: #f5f7fa;
  padding: 12px 16px 20px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  gap: 10px;
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
.save-btn {
  width: 100%;
  height: 46px;
  background: #fff;
  color: #6b7280;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
}
</style>
