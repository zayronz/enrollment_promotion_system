<template>
  <div class="activity-list">
    <div class="page-header">
      <h2 class="page-title">活动列表</h2>
      <t-button theme="primary" @click="$router.push('/school/activity/create')">
        <template #icon><AddIcon /></template>
        创建活动
      </t-button>
    </div>

    <!-- Search & filter -->
    <div class="filter-bar">
      <t-input
        v-model="keyword"
        placeholder="搜索活动名称..."
        clearable
        class="filter-input"
        @change="fetchData"
      >
        <template #prefix-icon><SearchIcon /></template>
      </t-input>
      <t-select
        v-model="typeFilter"
        placeholder="活动类型"
        clearable
        class="filter-select"
        @change="fetchData"
      >
        <t-option label="线上活动" value="ONLINE" />
        <t-option label="线下活动" value="OFFLINE" />
      </t-select>
      <t-select
        v-model="statusFilter"
        placeholder="发布状态"
        clearable
        class="filter-select"
        @change="fetchData"
      >
        <t-option label="草稿" value="DRAFT" />
        <t-option label="已发布" value="PUBLISHED" />
        <t-option label="已结束" value="ENDED" />
      </t-select>
    </div>

    <t-table
      :data="records"
      :columns="columns"
      :loading="loading"
      row-key="id"
      hover
      stripe
      :pagination="pagination"
      @page-change="handlePageChange"
    >
      <template #type="{ row }">
        <t-tag :theme="row.type === 'ONLINE' ? 'primary' : 'warning'" variant="light" size="small">
          {{ row.type === 'ONLINE' ? '线上' : '线下' }}
        </t-tag>
      </template>
      <template #showOnHome="{ row }">
        <t-switch
          :value="row.showOnHome"
          size="small"
          @change="(val) => toggleHomeShow(row, val)"
        />
      </template>
      <template #status="{ row }">
        <t-tag :theme="getStatusTheme(row.status)" variant="light" size="small">
          {{ getStatusLabel(row.status) }}
        </t-tag>
      </template>
      <template #action="{ row }">
        <t-space size="small">
          <t-button
            v-if="row.status === 'DRAFT'"
            theme="success"
            variant="text"
            size="small"
            @click="handlePublish(row)"
          >
            发布
          </t-button>
          <t-button
            v-if="row.status !== 'ENDED'"
            theme="primary"
            variant="text"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </t-button>
          <t-button theme="primary" variant="text" size="small" @click="handleDetail(row)">
            详情
          </t-button>
          <t-popconfirm
            content="确定要删除此活动吗？"
            confirm-btn="删除"
            @confirm="handleDelete(row.id)"
          >
            <t-button theme="danger" variant="text" size="small">删除</t-button>
          </t-popconfirm>
        </t-space>
      </template>
    </t-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { SearchIcon, AddIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { colKey: 'id', title: '编号', width: 80 },
  { colKey: 'title', title: '活动名称', minWidth: 180, ellipsis: true },
  { colKey: 'type', title: '类型', width: 100 },
  { colKey: 'showOnHome', title: '首页展示', width: 100 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'createTime', title: '创建时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 220 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityList({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value || undefined
    })
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (err) {
    console.error('获取活动列表失败', err)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchData()
}

const handlePublish = async (row) => {
  try {
    await activityApi.publishActivity(row.id)
    MessagePlugin.success('活动已发布')
    fetchData()
  } catch (err) {
    console.error('发布失败', err)
  }
}

const handleEdit = (row) => {
  router.push(`/school/activity/edit/${row.id}`)
}

const handleDetail = (row) => {
  router.push(`/school/activity/detail/${row.id}`)
}

const handleDelete = async (id) => {
  try {
    await activityApi.deleteActivity(id)
    MessagePlugin.success('删除成功')
    fetchData()
  } catch (err) {
    console.error('删除失败', err)
  }
}

const toggleHomeShow = (row, val) => {
  row.showOnHome = val
  MessagePlugin.success(val ? '已设置首页展示' : '已取消首页展示')
}

const getStatusTheme = (status) => {
  const map = { DRAFT: 'default', PUBLISHED: 'success', ENDED: 'warning' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', ENDED: '已结束' }
  return map[status] || status
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(fetchData)
</script>

<style scoped>
.activity-list { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-input { flex: 1; }
.filter-select { width: 160px; flex-shrink: 0; }

@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .filter-bar { flex-direction: column; }
  .filter-select { width: 100%; }
}
</style>
