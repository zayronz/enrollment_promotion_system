<template>
  <div class="no-content-page">
    <H5NavBar title="活动详情" />
    <EmptyState type="empty" title="暂无内容" description="当前暂无活动内容" />
  </div>
</template>

<script setup>
import H5NavBar from '../components/H5NavBar.vue'
import EmptyState from '../components/EmptyState.vue'
</script>

<style scoped>
.no-content-page {
  min-height: 100vh;
  background: #f5f7fa;
}
</style>
