<template>
  <div class="network-error-page">
    <H5NavBar title="活动详情" />
    <EmptyState type="network" title="网络断开" description="当前网络延迟，建议刷新重试" :showRefresh="true" @refresh="handleRefresh" />
  </div>
</template>

<script setup>
import H5NavBar from '../components/H5NavBar.vue'
import EmptyState from '../components/EmptyState.vue'

const handleRefresh = () => {
  location.reload()
}
</script>

<style scoped>
.network-error-page {
  min-height: 100vh;
  background: #f5f7fa;
}
</style>
