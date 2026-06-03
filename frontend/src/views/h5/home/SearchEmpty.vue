<template>
  <div class="search-empty-page">
    <H5NavBar title="活动详情" />
    <EmptyState type="search" title="搜索为空" description="暂时没有您搜索的内容，建议搜索其他查询结果" />
  </div>
</template>

<script setup>
import H5NavBar from '../components/H5NavBar.vue'
import EmptyState from '../components/EmptyState.vue'
</script>

<style scoped>
.search-empty-page {
  min-height: 100vh;
  background: #f5f7fa;
}
</style>
