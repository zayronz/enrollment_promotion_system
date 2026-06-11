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
        <t-tag :theme="row.type === 0 ? 'primary' : 'warning'" variant="light" size="small">
          {{ row.type === 0 ? '线上' : '线下' }}
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
            v-if="row.status === 0"
            theme="success"
            variant="text"
            size="small"
            @click="handlePublish(row)"
          >
            发布
          </t-button>
          <t-button
            v-if="row.status !== 2"
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
          <t-button theme="success" variant="text" size="small" @click="showGroups(row)">
            分组
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

    <t-dialog
      v-model:visible="groupVisible"
      :header="`${currentActivity?.name || '活动'} - 分组与排名`"
      width="820px"
      :footer="false"
    >
      <t-loading v-if="groupLoading" text="加载中..." />
      <div v-else-if="activityGroups.length" class="group-list">
        <div v-for="group in activityGroups" :key="group.targetSchool" class="group-card">
          <div class="group-header">
            <strong>{{ group.groupName }}</strong>
            <t-tag theme="primary" variant="light">{{ group.memberCount }} 人</t-tag>
          </div>
          <t-table
            :data="group.members"
            :columns="groupColumns"
            row-key="registrationId"
            size="small"
            bordered
          />
        </div>
      </div>
      <t-empty v-else description="暂无已通过报名记录，或活动未开启自动分组" />
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registeration'
import { SearchIcon, AddIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const groupVisible = ref(false)
const groupLoading = ref(false)
const currentActivity = ref(null)
const activityGroups = ref([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { colKey: 'rowIndex', title: '序号', width: 80, cell: (_, { row }) => getRowIndex(row) },
  { colKey: 'name', title: '活动名称', minWidth: 180, ellipsis: true },
  { colKey: 'type', title: '类型', width: 100 },
  { colKey: 'showOnHome', title: '首页展示', width: 100 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'createTime', title: '创建时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 260 }
]

const groupColumns = [
  { colKey: 'groupRank', title: '排名', width: 70, cell: (_, { rowIndex }) => rowIndex + 1 },
  { colKey: 'realName', title: '姓名', width: 100 },
  { colKey: 'username', title: '账号', width: 110 },
  { colKey: 'role', title: '角色', width: 90, cell: (_, { row }) => row.role === 'STUDENT' ? '学生' : row.role === 'TEACHER' ? '教师' : row.role },
  { colKey: 'collegeName', title: '学院', ellipsis: true },
  { colKey: 'score', title: '绩点/成绩', width: 100, cell: (_, { row }) => row.score ?? '-' },
  { colKey: 'phone', title: '联系电话', width: 130 }
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
    records.value = (res.data?.records || []).map(item => ({
      ...item,
      showOnHome: item.showOnHome === 1 // 转换为布尔值
    }))
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

const showGroups = async (row) => {
  currentActivity.value = row
  groupVisible.value = true
  groupLoading.value = true
  try {
    const res = await registrationApi.getActivityGroups(row.id)
    activityGroups.value = res.data || []
  } catch (err) {
    console.error('获取活动分组失败', err)
  } finally {
    groupLoading.value = false
  }
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

const toggleHomeShow = async (row, val) => {
  try {
    await activityApi.setHomeShow(row.id, val)
    row.showOnHome = val
    MessagePlugin.success(val ? '已设置首页展示' : '已取消首页展示')
  } catch (err) {
    console.error('设置首页展示失败', err)
    row.showOnHome = !val
  }
}

const getRowIndex = (row) => {
  const index = records.value.findIndex(r => r.id === row.id)
  return index >= 0 ? pagination.value.current * pagination.value.pageSize - pagination.value.pageSize + index + 1 : '-'
}

const getStatusTheme = (status) => {
  const map = { 0: 'default', 1: 'success', 2: 'warning' }
  return map[status] || 'default'
}

const getStatusLabel = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[status] || '未知'
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
.group-list { display: flex; flex-direction: column; gap: 16px; max-height: 65vh; overflow-y: auto; }
.group-card { border: 1px solid var(--td-border-level-1-color); border-radius: 10px; padding: 12px; }
.group-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }

@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .filter-bar { flex-direction: column; }
  .filter-select { width: 100%; }
}
</style>
