<template>
  <div class="add-school">
    <H5NavBar title="添加中学" />
    <div class="form-content">
      <div class="form-item">
        <label>中学名称 <span class="required">*</span></label>
        <input v-model="form.name" placeholder="请输入中学名称" />
      </div>
      <div class="form-item">
        <label>到访日期 <span class="required">*</span></label>
        <input v-model="form.date" placeholder="请输入到访日期" type="date" />
      </div>
      <div class="form-item">
        <label>宣传形式 <span class="required">*</span></label>
        <input v-model="form.method" placeholder="请输入宣传形式" />
      </div>
    </div>
    <div class="submit-area">
      <button class="submit-btn" @click="handleSubmit">添加</button>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import H5NavBar from '../components/H5NavBar.vue'

const router = useRouter()
const form = reactive({ name: '', date: '', method: '' })

const handleSubmit = () => {
  router.back()
}
</script>

<style scoped>
.add-school {
  min-height: 100vh;
  background: #f5f7fa;
}
.form-content {
  padding: 12px;
}
.form-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 10px;
}
.form-item label {
  display: block;
  font-size: 14px;
  color: #374151;
  margin-bottom: 10px;
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
</style>
