<template>
  <div class="h5-navbar" :class="{ transparent: transparent }">
    <div class="nav-left" @click="goBack">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path d="M15 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
    <div class="nav-title">{{ title }}</div>
    <div class="nav-right">
      <slot name="right">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" v-if="showMore">
          <circle cx="6" cy="12" r="2" fill="currentColor"/>
          <circle cx="12" cy="12" r="2" fill="currentColor"/>
          <circle cx="18" cy="12" r="2" fill="currentColor"/>
        </svg>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  title: { type: String, default: '' },
  transparent: { type: Boolean, default: false },
  showMore: { type: Boolean, default: false },
  backPath: { type: String, default: '' }
})

const router = useRouter()

const goBack = () => {
  if (props.backPath) {
    router.push(props.backPath)
  } else {
    router.back()
  }
}
</script>

<style scoped>
.h5-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.h5-navbar.transparent {
  background: transparent;
  border-bottom: none;
  color: #fff;
}
.nav-left, .nav-right {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.nav-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 500;
  color: #1f2937;
}
.h5-navbar.transparent .nav-title {
  color: #fff;
}
</style>
