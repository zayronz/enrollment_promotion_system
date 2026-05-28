<template>
  <div class="user-list">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <t-button theme="primary" @click="showCreateDialog()">
        <template #icon><AddIcon /></template>
        新增用户
      </t-button>
    </div>

    <!-- Search & filter -->
    <div class="filter-bar">
      <t-input
        v-model="keyword"
        placeholder="搜索用户名、姓名、手机号..."
        clearable
        class="filter-input"
        @change="fetchData"
      >
        <template #prefix-icon><SearchIcon /></template>
      </t-input>
      <t-select
        v-model="roleFilter"
        placeholder="用户角色"
        clearable
        class="filter-select"
        @change="fetchData"
      >
        <t-option label="学生" value="STUDENT" />
        <t-option label="教师" value="TEACHER" />
        <t-option label="学院管理员" value="COLLEGE" />
        <t-option label="学校管理员" value="SCHOOL" />
      </t-select>
      <t-select
        v-model="statusFilter"
        placeholder="状态"
        clearable
        class="filter-select-short"
        @change="fetchData"
      >
        <t-option label="正常" value="ACTIVE" />
        <t-option label="禁用" value="DISABLED" />
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
      <template #role="{ row }">
        <t-tag :theme="getRoleTheme(row.role)" variant="light" size="small">
          {{ getRoleLabel(row.role) }}
        </t-tag>
      </template>
      <template #status="{ row }">
        <t-tag :theme="row.status === 'ACTIVE' ? 'success' : 'danger'" variant="light" size="small">
          {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
        </t-tag>
      </template>
      <template #action="{ row }">
        <t-space size="small">
          <t-button theme="primary" variant="text" size="small" @click="showEditDialog(row)">
            编辑
          </t-button>
          <t-button
            v-if="row.status === 'ACTIVE'"
            theme="warning"
            variant="text"
            size="small"
            @click="toggleStatus(row, 'DISABLED')"
          >
            禁用
          </t-button>
          <t-button
            v-else
            theme="success"
            variant="text"
            size="small"
            @click="toggleStatus(row, 'ACTIVE')"
          >
            启用
          </t-button>
          <t-popconfirm
            content="确定要删除此用户吗？"
            confirm-btn="删除"
            @confirm="handleDelete(row.id)"
          >
            <t-button theme="danger" variant="text" size="small">删除</t-button>
          </t-popconfirm>
        </t-space>
      </template>
    </t-table>

    <!-- Create / Edit dialog -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="editMode ? '编辑用户' : '新增用户'"
      width="540px"
      :confirm-btn="{ content: editMode ? '保存修改' : '确认创建', theme: 'primary' }"
      @confirm="handleSave"
    >
      <t-form ref="formRef" :data="form" :rules="formRules" label-width="100px">
        <t-form-item label="用户名" name="username">
          <t-input v-model="form.username" placeholder="请输入用户名" :disabled="editMode" clearable />
        </t-form-item>
        <t-form-item label="姓名" name="realName">
          <t-input v-model="form.realName" placeholder="请输入真实姓名" clearable />
        </t-form-item>
        <t-form-item label="密码" name="password" v-if="!editMode">
          <t-input v-model="form.password" type="password" placeholder="请输入密码" clearable />
        </t-form-item>
        <t-form-item label="角色" name="role">
          <t-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <t-option label="学生" value="STUDENT" />
            <t-option label="教师" value="TEACHER" />
            <t-option label="学院管理员" value="COLLEGE" />
            <t-option label="学校管理员" value="SCHOOL" />
          </t-select>
        </t-form-item>
        <t-form-item
          label="所属学院"
          name="collegeId"
          v-if="form.role === 'STUDENT' || form.role === 'TEACHER'"
        >
          <t-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
            <t-option
              v-for="c in collegeOptions"
              :key="c.value"
              :value="c.value"
              :label="c.label"
            />
          </t-select>
        </t-form-item>
        <t-form-item label="手机号" name="phone">
          <t-input v-model="form.phone" placeholder="请输入手机号" clearable />
        </t-form-item>
        <t-form-item label="邮箱" name="email">
          <t-input v-model="form.email" placeholder="请输入邮箱" clearable />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { SearchIcon, AddIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const collegeOptions = ref([
  { value: '1', label: '计算机科学与技术学院' },
  { value: '2', label: '理学院' },
  { value: '3', label: '管理学院' }
])

const dialogVisible = ref(false)
const editMode = ref(false)
const formRef = ref(null)
const saving = ref(false)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const form = ref({
  username: '',
  realName: '',
  password: '',
  role: 'STUDENT',
  collegeId: '',
  phone: '',
  email: ''
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6, message: '密码不少于6位' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const columns = [
  { colKey: 'id', title: '编号', width: 80 },
  { colKey: 'username', title: '用户名', width: 120 },
  { colKey: 'realName', title: '姓名', width: 100 },
  { colKey: 'role', title: '角色', width: 110 },
  { colKey: 'collegeName', title: '所属学院', width: 150, ellipsis: true },
  { colKey: 'phone', title: '手机号', width: 130 },
  { colKey: 'email', title: '邮箱', minWidth: 150, ellipsis: true },
  { colKey: 'status', title: '状态', width: 80 },
  { colKey: 'createTime', title: '创建时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 200 }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await userApi.getUserList({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      role: roleFilter.value || undefined,
      status: statusFilter.value || undefined
    })
    records.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (err) {
    console.error('获取用户列表失败', err)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  fetchData()
}

const showCreateDialog = () => {
  editMode.value = false
  form.value = {
    username: '',
    realName: '',
    password: '',
    role: 'STUDENT',
    collegeId: '',
    phone: '',
    email: ''
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  editMode.value = true
  form.value = {
    id: row.id,
    username: row.username,
    realName: row.realName,
    password: '',
    role: row.role,
    collegeId: row.collegeId || '',
    phone: row.phone || '',
    email: row.email || ''
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  saving.value = true
  try {
    if (editMode.value) {
      const { password, ...updateData } = form.value
      await userApi.updateUser(updateData)
      MessagePlugin.success('修改成功')
    } else {
      await userApi.createUser(form.value)
      MessagePlugin.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    console.error('操作失败', err)
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row, status) => {
  try {
    await userApi.updateUser({ id: row.id, status })
    MessagePlugin.success(status === 'ACTIVE' ? '已启用' : '已禁用')
    fetchData()
  } catch (err) {
    console.error('操作失败', err)
  }
}

const handleDelete = async (id) => {
  try {
    await userApi.deleteUser(id)
    MessagePlugin.success('删除成功')
    fetchData()
  } catch (err) {
    console.error('删除失败', err)
  }
}

const getRoleTheme = (role) => {
  const map = { STUDENT: 'primary', TEACHER: 'warning', COLLEGE: 'success', SCHOOL: 'danger' }
  return map[role] || 'default'
}

const getRoleLabel = (role) => {
  const map = { STUDENT: '学生', TEACHER: '教师', COLLEGE: '学院管理员', SCHOOL: '学校管理员' }
  return map[role] || role
}

const formatDateTime = (str) => {
  if (!str) return '-'
  return new Date(str).toLocaleString('zh-CN')
}

onMounted(fetchData)
</script>

<style scoped>
.user-list { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--td-text-color-primary); margin: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.filter-input { flex: 1; }
.filter-select { width: 160px; flex-shrink: 0; }
.filter-select-short { width: 120px; flex-shrink: 0; }

@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .filter-bar { flex-direction: column; }
  .filter-select, .filter-select-short { width: 100%; }
}
</style>
