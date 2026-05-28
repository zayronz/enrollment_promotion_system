<template>
  <div class="app-container">
    <router-view v-if="!isLoginPage" />
    <router-view v-else />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isLoginPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

.app-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>
