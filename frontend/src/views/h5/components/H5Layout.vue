<template>
  <div class="h5-layout">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

onMounted(() => {
  userStore.checkLogin()
})
</script>

<style scoped>
.h5-layout {
  min-height: 100vh;
  background: #f5f7fa;
  max-width: 480px;
  margin: 0 auto;
  position: relative;
  overflow-x: hidden;
}
</style>
