<template>
  <div class="pc-test-start">
    <div class="hero">
      <div>
        <h2>能力测试</h2>
        <p>请认真阅读测试说明，确认后开始答题。</p>
      </div>
    </div>

    <t-card class="start-card" bordered>
      <h3>招生能力测试说明</h3>
      <div class="desc">
        尊敬的各位招宣人员：本次测试用于检验招生宣传政策理解、宣讲组织、报名流程和常见问题处理能力。
        测试共包含单选题、多选题和填空题，请在规定时间内完成提交。若已知晓上述内容，请勾选确认并开始测试。
      </div>
      <t-checkbox v-model="agreed">我已知晓上述内容</t-checkbox>
      <div class="actions">
        <t-button theme="primary" size="large" :disabled="!agreed" @click="startTest">开始测试</t-button>
        <t-button size="large" variant="outline" @click="router.back()">返回</t-button>
      </div>
    </t-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const agreed = ref(false)
const roleBase = computed(() => route.path.startsWith('/teacher') ? '/teacher' : '/student')

const startTest = () => {
  router.push(`${roleBase.value}/test/questions/${route.params.id}`)
}
</script>

<style scoped>
.pc-test-start {
  padding: 0;
}
.hero {
  min-height: 160px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0052d9, #366ef4);
  color: #fff;
  padding: 32px;
  display: flex;
  align-items: center;
  margin-bottom: -36px;
}
.hero h2 {
  margin: 0 0 10px;
  font-size: 28px;
}
.hero p {
  margin: 0;
  opacity: 0.9;
}
.start-card {
  max-width: 820px;
  margin: 0 auto;
  border-radius: 16px;
}
.start-card h3 {
  margin: 0 0 14px;
  text-align: center;
  color: var(--td-text-color-primary);
}
.desc {
  color: var(--td-text-color-secondary);
  line-height: 1.9;
  margin-bottom: 20px;
}
.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
</style>
