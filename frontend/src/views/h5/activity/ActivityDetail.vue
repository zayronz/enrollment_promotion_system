<template>
  <div class="activity-detail">
    <H5NavBar title="活动详情" />

    <div class="detail-content">
      <!-- 标题 -->
      <div class="detail-title-row">
        <span class="detail-tag">&#9632;</span>
        <span class="detail-title">2023届应届生招生活动</span>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="share-icon">
          <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6M15 3h6v6M10 14L21 3"/>
        </svg>
      </div>

      <!-- 信息卡片 -->
      <div class="info-section">
        <div class="info-label">活动来源</div>
        <div class="info-value">派发</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动时间</div>
        <div class="info-value">
          <div>计划开始日期：2023/03/29</div>
          <div>计划结束日期：2023/03/29</div>
          <div>合计时长：1天</div>
        </div>
      </div>

      <div class="info-section">
        <div class="info-label">活动区域</div>
        <div class="info-value">湖北省/武汉市/洪山区</div>
      </div>

      <div class="info-section">
        <div class="info-label">活动牵头宣传组</div>
        <div class="info-value">宣传组001</div>
      </div>

      <!-- 报名表单 -->
      <div class="form-section">
        <div class="form-title">&#9670; 基本信息填写</div>
        <div class="form-item">
          <label>职工号 <span class="required">*</span></label>
          <input v-model="form.employeeId" placeholder="请输入职工号" />
        </div>
        <div class="form-item">
          <label>姓名 <span class="required">*</span></label>
          <input v-model="form.name" placeholder="请输入姓名" />
        </div>
        <div class="form-item">
          <label>单位 <span class="required">*</span></label>
          <input v-model="form.unit" placeholder="请输入单位" />
        </div>
        <div class="form-item">
          <label>职务 <span class="required">*</span></label>
          <input v-model="form.position" placeholder="请输入职务" />
        </div>
        <div class="form-item">
          <label>职称 <span class="required">*</span></label>
          <input v-model="form.title" placeholder="请输入职称" />
        </div>
        <div class="form-item">
          <label>联系方式 <span class="required">*</span></label>
          <input v-model="form.phone" placeholder="请输入联系方式" />
        </div>
        <div class="form-item">
          <label>毕业中学 <span class="required">*</span></label>
          <div class="select-box" @click="showPicker = true">
            <span :class="{ placeholder: !form.school }">{{ form.school || '请选择毕业中学' }}</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
        </div>
        <div class="form-item">
          <label>籍贯 <span class="required">*</span></label>
          <div class="select-box" @click="showPicker = true">
            <span :class="{ placeholder: !form.origin }">{{ form.origin || '请选择籍贯' }}</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
        </div>
      </div>
    </div>

    <div class="submit-area">
      <button class="submit-btn" @click="handleSubmit">立即报名</button>
      <div class="submit-tip">报名审核通过，可见更多详细活动信息</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const showPicker = ref(false)

const form = reactive({
  employeeId: '10001',
  name: '张梅',
  unit: '国际学院',
  position: '职务01',
  title: '职称01',
  phone: '15884418478',
  school: '',
  origin: ''
})

const handleSubmit = () => {
  router.push('/h5/submit-success')
}
</script>

<style scoped>
.activity-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 100px;
}
.detail-content {
  padding: 12px;
}
.detail-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.detail-tag {
  color: #2563eb;
  font-size: 14px;
}
.detail-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
.share-icon {
  flex-shrink: 0;
  cursor: pointer;
}
.info-section {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
  margin-bottom: 10px;
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
  line-height: 1.8;
}
.form-section {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 10px;
}
.form-title {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 14px;
}
.form-item {
  margin-bottom: 12px;
}
.form-item label {
  display: block;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
}
.required {
  color: #ef4444;
}
.form-item input {
  width: 100%;
  height: 40px;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  color: #374151;
  box-sizing: border-box;
}
.form-item input::placeholder {
  color: #d1d5db;
}
.select-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}
.select-box .placeholder {
  color: #d1d5db;
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
.submit-tip {
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  margin-top: 8px;
}
</style>
