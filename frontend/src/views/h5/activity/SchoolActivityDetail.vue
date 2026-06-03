<template>
  <div class="activity-detail">
    <H5NavBar title="活动详情" />

    <div class="detail-content">
      <div class="detail-title-row">
        <span class="detail-tag">&#9632;</span>
        <span class="detail-title">2012届毕业生母校游</span>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" class="share-icon">
          <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6M15 3h6v6M10 14L21 3"/>
        </svg>
      </div>

      <div class="info-section">
        <div class="info-label">意向前往中学</div>
        <div class="info-value school-row">
          <span class="school-badge">校</span>
          <span>武汉第一中学</span>
        </div>
      </div>

      <div class="info-section">
        <div class="info-label">行政区域地址</div>
        <div class="info-value">湖北省/武汉市/洪山区</div>
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
        <div class="info-label">活动简介</div>
        <div class="info-value">2012届毕业十年团建项目</div>
      </div>

      <!-- 报名申请 -->
      <div class="form-section">
        <div class="form-title">&#9670; 报名申请</div>
        <div class="readonly-row">
          <span class="readonly-label">姓名：</span>
          <span class="readonly-value">姓名1</span>
        </div>
        <div class="readonly-row">
          <span class="readonly-label">学号/工号：</span>
          <span class="readonly-value">10001</span>
        </div>
        <div class="readonly-row">
          <span class="readonly-label">学院：</span>
          <span class="readonly-value">A学院</span>
        </div>
        <div class="form-item">
          <label>联系方式 <span class="required">*</span></label>
          <input v-model="form.phone" placeholder="请输入联系方式" />
        </div>
        <div class="form-item">
          <label>QQ <span class="required">*</span></label>
          <input v-model="form.qq" placeholder="请输入QQ号码" />
        </div>
        <div class="form-item">
          <label>微信 <span class="required">*</span></label>
          <input v-model="form.wechat" placeholder="请输入微信号码" />
        </div>
        <div class="form-item">
          <label>意向中学 <span class="required">*</span></label>
          <div class="select-box" @click="showPicker = true">
            <span :class="{ placeholder: !form.school }">{{ form.school || '请选择意向前往中学（高中）' }}</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
        </div>
        <div class="form-item">
          <label>是否愿意担任团队负责人 <span class="required">*</span></label>
          <div class="radio-group">
            <label class="radio-item">
              <input type="radio" v-model="form.leader" value="yes" />
              <span>是</span>
            </label>
            <label class="radio-item">
              <input type="radio" v-model="form.leader" value="no" />
              <span>否</span>
            </label>
          </div>
        </div>
        <div class="form-item">
          <label>大学以来担任学生干部经历 <span class="required">*</span></label>
          <textarea v-model="form.experience" placeholder="请输入大学以来担任学生干部经历" rows="3"></textarea>
        </div>
        <div class="form-item">
          <label>大学以来获奖励荣誉经历 <span class="required">*</span></label>
          <textarea v-model="form.honors" placeholder="请输入大学以来获奖励荣誉经历" rows="3"></textarea>
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
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()

const form = reactive({
  phone: '15884418478',
  qq: '',
  wechat: '',
  school: '',
  leader: 'yes',
  experience: '',
  honors: ''
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
.school-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.school-badge {
  width: 22px;
  height: 22px;
  background: #dbeafe;
  color: #2563eb;
  border-radius: 50%;
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
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
.readonly-row {
  display: flex;
  margin-bottom: 10px;
  font-size: 14px;
}
.readonly-label {
  color: #6b7280;
}
.readonly-value {
  color: #374151;
  font-weight: 500;
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
.form-item input, .form-item textarea {
  width: 100%;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  color: #374151;
  box-sizing: border-box;
  resize: none;
}
.form-item input::placeholder, .form-item textarea::placeholder {
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
.radio-group {
  display: flex;
  gap: 20px;
}
.radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}
.radio-item input[type="radio"] {
  width: 16px;
  height: 16px;
  accent-color: #2563eb;
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
