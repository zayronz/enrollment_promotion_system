<template>
  <div class="banner-management">
    <div class="page-header">
      <h2 class="page-title">轮播图管理</h2>
      <t-button theme="primary" @click="handleCreate">
        <template #icon><t-icon name="add" /></template>
        添加轮播图
      </t-button>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <t-space>
        <t-select
          v-model="filterActivityId"
          placeholder="选择活动"
          clearable
          style="width: 200px"
          @change="handleFilterChange"
        >
          <t-option
            v-for="activity in activities"
            :key="activity.id"
            :value="activity.id"
            :label="activity.name"
          />
        </t-select>
        
        <t-select
          v-model="filterStatus"
          placeholder="选择状态"
          clearable
          style="width: 120px"
          @change="handleFilterChange"
        >
          <t-option :value="1" label="显示" />
          <t-option :value="0" label="隐藏" />
        </t-select>
        
        <t-button theme="default" @click="handleReset">重置</t-button>
      </t-space>
    </div>

    <!-- 轮播图列表 -->
    <t-table
      :data="tableData"
      :columns="columns"
      :pagination="pagination"
      :loading="loading"
      row-key="id"
      stripe
      hover
      @page-change="handlePageChange"
    >
      <template #imageUrl="{ row }">
        <t-image
          :src="row.fullImageUrl"
          :style="{ width: '80px', height: '60px', objectFit: 'cover' }"
          :lazy="true"
          fit="cover"
        />
      </template>

      <template #status="{ row }">
        <t-tag :theme="row.status === 1 ? 'success' : 'default'" variant="light">
          {{ row.status === 1 ? '显示' : '隐藏' }}
        </t-tag>
      </template>

      <template #sortOrder="{ row }">
        <t-input-number
          v-model="row.sortOrder"
          :min="0"
          size="small"
          style="width: 80px"
          @change="handleSortOrderChange(row)"
        />
      </template>

      <template #operations="{ row }">
        <t-space>
          <t-button size="small" theme="primary" variant="text" @click="handleEdit(row)">
            编辑
          </t-button>
          <t-button size="small" theme="warning" variant="text" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '隐藏' : '显示' }}
          </t-button>
          <t-button size="small" theme="danger" variant="text" @click="handleDelete(row)">
            删除
          </t-button>
        </t-space>
      </template>
    </t-table>

    <!-- 添加/编辑弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogMode === 'create' ? '添加轮播图' : '编辑轮播图'"
      width="600px"
      :confirm-on-enter="true"
      @confirm="handleSubmit"
    >
      <t-form :data="formData" :rules="rules" ref="formRef" label-width="100px">
        <t-form-item label="关联活动" name="activityId">
          <t-select
            v-model="formData.activityId"
            placeholder="请选择活动"
            style="width: 100%"
          >
            <t-option
              v-for="activity in activities"
              :key="activity.id"
              :value="activity.id"
              :label="activity.name"
            />
          </t-select>
        </t-form-item>

        <t-form-item label="轮播图片" name="imageUrl">
          <div class="upload-container">
            <FileUploader
              v-model="formData.imageUrl"
              :multiple="false"
              accept="image/*"
              tip-text="支持 JPG、PNG 格式，建议尺寸 1920x600"
            />
          </div>
        </t-form-item>

        <t-form-item label="跳转链接" name="linkUrl">
          <t-input v-model="formData.linkUrl" placeholder="请输入跳转链接（可选）" />
        </t-form-item>

        <t-form-item label="排序" name="sortOrder">
          <t-input-number
            v-model="formData.sortOrder"
            :min="0"
            :step="1"
            style="width: 200px"
          />
          <span class="tip">数字越小越靠前</span>
        </t-form-item>

        <t-form-item label="状态" name="status">
          <t-switch v-model="statusChecked" />
          <span class="tip">{{ statusChecked ? '显示' : '隐藏' }}</span>
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 删除确认弹窗 -->
    <t-dialog
      v-model:visible="deleteDialogVisible"
      header="确认删除"
      :body="`确定要删除轮播图 ID: ${deleteId} 吗？此操作不可恢复。`"
      theme="danger"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import FileUploader from '@/components/business/FileUploader.vue'
import {
  getBannerPage,
  createBanner,
  updateBanner,
  deleteBanner,
  updateBannerStatus,
  getBannersByActivityId
} from '@/api/banner'
import { activityApi } from '@/api/activity'

const loading = ref(false)
const tableData = ref([])
const activities = ref([])

// 筛选条件
const filterActivityId = ref(null)
const filterStatus = ref(null)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格列配置
const columns = [
  { colKey: 'id', title: 'ID', width: '80' },
  { colKey: 'imageUrl', title: '图片', width: '120' },
  { colDataKey: 'activityName', title: '关联活动', ellipsis: true },
  { colKey: 'sortOrder', title: '排序', width: '120' },
  { colKey: 'status', title: '状态', width: '100' },
  { colKey: 'operations', title: '操作', width: '200' }
]

// 弹窗相关
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const formData = reactive({
  id: null,
  activityId: null,
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1
})
const statusChecked = ref(true)

const rules = {
  activityId: [{ required: true, message: '请选择活动', trigger: 'change' }],
  imageUrl: [{ required: true, message: '请上传轮播图片', trigger: 'change' }]
}

// 删除相关
const deleteDialogVisible = ref(false)
const deleteId = ref(null)

// 加载轮播图列表
const loadBanners = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      activityId: filterActivityId.value,
      status: filterStatus.value
    }
    const res = await getBannerPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载轮播图失败:', error)
    MessagePlugin.error('加载轮播图失败')
  } finally {
    loading.value = false
  }
}

// 加载活动列表
const loadActivities = async () => {
  try {
    const res = await activityApi.getActivityList({ page: 1, size: 100 })
    if (res.code === 200) {
      activities.value = res.data.records
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

// 筛选变化
const handleFilterChange = () => {
  pagination.current = 1
  loadBanners()
}

// 重置筛选
const handleReset = () => {
  filterActivityId.value = null
  filterStatus.value = null
  pagination.current = 1
  loadBanners()
}

// 分页变化
const handlePageChange = (pageInfo) => {
  pagination.current = pageInfo.current
  pagination.pageSize = pageInfo.pageSize
  loadBanners()
}

// 创建
const handleCreate = () => {
  dialogMode.value = 'create'
  Object.assign(formData, {
    id: null,
    activityId: null,
    imageUrl: '',
    linkUrl: '',
    sortOrder: 0,
    status: 1
  })
  statusChecked.value = true
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(formData, {
    id: row.id,
    activityId: row.activityId,
    imageUrl: row.imageUrl,
    linkUrl: row.linkUrl,
    sortOrder: row.sortOrder,
    status: row.status
  })
  statusChecked.value = row.status === 1
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  try {
    formData.status = statusChecked.value ? 1 : 0
    let res
    if (dialogMode.value === 'create') {
      res = await createBanner(formData)
      MessagePlugin.success('添加成功')
    } else {
      res = await updateBanner(formData.id, formData)
      MessagePlugin.success('更新成功')
    }
    
    if (res.code === 200) {
      dialogVisible.value = false
      loadBanners()
    }
  } catch (error) {
    console.error('保存失败:', error)
    MessagePlugin.error('保存失败')
  }
}

// 删除
const handleDelete = (row) => {
  deleteId.value = row.id
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  try {
    const res = await deleteBanner(deleteId.value)
    if (res.code === 200) {
      MessagePlugin.success('删除成功')
      deleteDialogVisible.value = false
      loadBanners()
    }
  } catch (error) {
    console.error('删除失败:', error)
    MessagePlugin.error('删除失败')
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const res = await updateBannerStatus(row.id, newStatus)
    if (res.code === 200) {
      MessagePlugin.success(newStatus === 1 ? '已显示' : '已隐藏')
      loadBanners()
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    MessagePlugin.error('更新状态失败')
  }
}

// 排序更新
const handleSortOrderChange = async (row) => {
  try {
    await updateBanner(row.id, { sortOrder: row.sortOrder })
    MessagePlugin.success('排序已更新')
  } catch (error) {
    console.error('更新排序失败:', error)
    MessagePlugin.error('更新排序失败')
    loadBanners()
  }
}

// 页面加载
onMounted(() => {
  loadBanners()
  loadActivities()
})
</script>

<style scoped>
.banner-management {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 0;
}

.filter-section {
  margin-bottom: 16px;
}

.tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.upload-container {
  width: 100%;
}
</style>
