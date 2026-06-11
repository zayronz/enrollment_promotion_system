<template>
  <div class="user-list">
    <div class="page-header panel-card">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-desc">统一维护学生、教师和管理端账号信息</p>
      </div>
      <t-button theme="primary" size="large" @click="showCreateDialog()">
        <template #icon><AddIcon /></template>
        新增用户
      </t-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">当前列表</span>
        <strong>{{ pagination.total }}</strong>
      </div>
      <div class="stat-card student">
        <span class="stat-label">学生</span>
        <strong>{{ roleCount.STUDENT }}</strong>
      </div>
      <div class="stat-card teacher">
        <span class="stat-label">教师</span>
        <strong>{{ roleCount.TEACHER }}</strong>
      </div>
      <div class="stat-card admin">
        <span class="stat-label">管理账号</span>
        <strong>{{ roleCount.COLLEGE + roleCount.SCHOOL }}</strong>
      </div>
    </div>

    <div class="panel-card list-panel">
      <div class="filter-bar">
        <t-input
          v-model="keyword"
          placeholder="搜索用户名、姓名、手机号..."
          clearable
          class="filter-input"
          @enter="fetchData"
          @clear="fetchData"
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
        <t-button variant="outline" @click="fetchData">查询</t-button>
      </div>

      <t-table
        :data="records"
        :columns="columns"
        :loading="loading"
        row-key="id"
        hover
        stripe
        bordered
        size="medium"
        table-layout="fixed"
        :pagination="pagination"
        @page-change="handlePageChange"
      >
        <template #username="{ row }">
          <div class="user-cell">
            <t-avatar size="32px" class="user-avatar">{{ row.realName?.charAt(0) || row.username?.charAt(0) || 'U' }}</t-avatar>
            <div class="user-main">
              <span class="user-name">{{ row.username }}</span>
              <span class="user-sub">{{ row.realName || '-' }}</span>
            </div>
          </div>
        </template>
        <template #role="{ row }">
          <t-tag :theme="getRoleTheme(row.role)" variant="light" size="small">
            {{ getRoleLabel(row.role) }}
          </t-tag>
        </template>
        <template #collegeName="{ row }">
          <span class="muted-text">{{ row.collegeName || '-' }}</span>
        </template>
        <template #gpa="{ row }">
          <t-tag v-if="row.role === 'STUDENT' && hasValue(row.gpa)" theme="primary" variant="light">
            {{ Number(row.gpa).toFixed(2) }}
          </t-tag>
          <span v-else class="muted-text">-</span>
        </template>
        <template #status="{ row }">
          <t-tag :theme="getStatusTheme(row.status)" variant="light" size="small">
            {{ getStatusLabel(row.status) }}
          </t-tag>
        </template>
        <template #action="{ row }">
          <t-space size="small">
            <t-button theme="primary" variant="text" size="small" @click="showEditDialog(row)">
              编辑
            </t-button>
            <t-button
              v-if="row.status === 1 || row.status === 'ACTIVE'"
              theme="warning"
              variant="text"
              size="small"
              @click="toggleStatus(row, 0)"
            >
              禁用
            </t-button>
            <t-button
              v-else
              theme="success"
              variant="text"
              size="small"
              @click="toggleStatus(row, 1)"
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
    </div>

    <!-- Create / Edit dialog -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="editMode ? '编辑用户' : '新增用户'"
      width="620px"
      :confirm-btn="{ content: editMode ? '保存修改' : '确认创建', theme: 'primary', loading: saving }"
      @confirm="handleSave"
    >
      <t-form ref="formRef" :data="form" :rules="formRules" label-width="100px" class="user-form">
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
        <t-form-item label="年级" name="grade" v-if="form.role === 'STUDENT'">
          <t-input-number
            v-model="form.grade"
            :min="2000"
            :max="2100"
            placeholder="请输入年级"
            style="width: 100%"
          />
        </t-form-item>
        <t-form-item label="绩点" name="gpa" v-if="form.role === 'STUDENT'">
          <t-input-number
            v-model="form.gpa"
            :min="0"
            :max="5"
            :step="0.1"
            :decimal-places="2"
            placeholder="请输入绩点"
            style="width: 100%"
          />
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
import { computed, ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { SearchIcon, AddIcon } from 'tdesign-icons-vue-next'
import { MessagePlugin } from 'tdesign-vue-next'
import request from '@/utils/request'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const collegeOptions = ref([])

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
  grade: null,
  gpa: null,
  phone: '',
  email: ''
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6, message: '密码不少于6位' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  gpa: [{
    validator: (val) => {
      if (form.value.role !== 'STUDENT' || !hasValue(val)) return { result: true }
      const num = Number(val)
      if (Number.isNaN(num) || num < 0 || num > 5) {
        return { result: false, message: '绩点范围应为 0-5', type: 'error' }
      }
      return { result: true }
    }
  }]
}

const columns = [
  { colKey: 'id', title: '编号', width: 72 },
  { colKey: 'username', title: '用户', width: 170 },
  { colKey: 'realName', title: '姓名', width: 100 },
  { colKey: 'role', title: '角色', width: 110 },
  { colKey: 'collegeName', title: '所属学院', width: 150, ellipsis: true },
  { colKey: 'gpa', title: '绩点', width: 90 },
  { colKey: 'phone', title: '手机号', width: 130 },
  { colKey: 'email', title: '邮箱', minWidth: 150, ellipsis: true },
  { colKey: 'status', title: '状态', width: 80 },
  { colKey: 'createTime', title: '创建时间', width: 160, cell: (_, { row }) => formatDateTime(row.createTime) },
  { colKey: 'action', title: '操作', width: 200 }
]

const roleCount = computed(() => {
  const count = { STUDENT: 0, TEACHER: 0, COLLEGE: 0, SCHOOL: 0 }
  records.value.forEach(item => {
    if (count[item.role] !== undefined) count[item.role] += 1
  })
  return count
})

const hasValue = (value) => value !== null && value !== undefined && value !== ''

const normalizeUserPayload = (source) => {
  const data = { ...source }
  if (hasValue(data.collegeId)) {
    data.collegeId = Number(data.collegeId)
  } else {
    data.collegeId = null
  }

  if (data.role === 'STUDENT') {
    data.grade = hasValue(data.grade) ? Number(data.grade) : null
    data.gpa = hasValue(data.gpa) ? Number(data.gpa) : null
  } else {
    delete data.grade
    delete data.gpa
  }
  return data
}

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

const fetchColleges = async () => {
  try {
    const res = await request.get('/college/list')
    collegeOptions.value = (res.data || []).map(item => ({
      value: String(item.id),
      label: item.name
    }))
  } catch (err) {
    console.error('获取学院列表失败', err)
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
    grade: null,
    gpa: null,
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
    collegeId: row.collegeId ? String(row.collegeId) : '',
    grade: row.grade ?? null,
    gpa: row.gpa ?? null,
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
      const { id, password, ...rest } = form.value
      const updateData = normalizeUserPayload(rest)
      await userApi.updateUser(id, updateData)
      MessagePlugin.success('修改成功')
    } else {
      const createData = normalizeUserPayload(form.value)
      await userApi.createUser(createData)
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
    // status: 1=正常, 0=禁用
    await userApi.updateUser(row.id, { status: status })
    MessagePlugin.success(status === 1 ? '已启用' : '已禁用')
    fetchData()
  } catch (err) {
    console.error('操作失败', err)
  }
}

const getStatusTheme = (status) => {
  // 兼容整数和字符串状态值
  return (status === 1 || status === 'ACTIVE') ? 'success' : 'danger'
}

const getStatusLabel = (status) => {
  // 兼容整数和字符串状态值
  return (status === 1 || status === 'ACTIVE') ? '正常' : '禁用'
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

onMounted(() => {
  fetchData()
  fetchColleges()
})
</script>

<style scoped>
.user-list {
  padding: 0;
}
.panel-card {
  background: var(--td-bg-color-container);
  border: 1px solid var(--td-border-level-1-color);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 22px 24px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--td-text-color-primary);
  margin: 0;
}
.page-desc {
  margin: 6px 0 0;
  color: var(--td-text-color-secondary);
  font-size: 13px;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}
.stat-card {
  position: relative;
  overflow: hidden;
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f7faff, #ffffff);
  border: 1px solid var(--td-border-level-1-color);
}
.stat-card::after {
  content: '';
  position: absolute;
  right: -18px;
  top: -18px;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: rgba(0, 82, 217, 0.08);
}
.stat-card.student::after { background: rgba(0, 82, 217, 0.12); }
.stat-card.teacher::after { background: rgba(237, 123, 47, 0.13); }
.stat-card.admin::after { background: rgba(0, 168, 112, 0.13); }
.stat-label {
  display: block;
  color: var(--td-text-color-secondary);
  font-size: 13px;
  margin-bottom: 8px;
}
.stat-card strong {
  color: var(--td-text-color-primary);
  font-size: 26px;
  line-height: 1;
}
.list-panel {
  padding: 18px;
}
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  align-items: center;
}
.filter-input { flex: 1; }
.filter-select { width: 160px; flex-shrink: 0; }
.filter-select-short { width: 120px; flex-shrink: 0; }
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.user-avatar {
  background: var(--td-brand-color-light);
  color: var(--td-brand-color);
  font-weight: 700;
  flex-shrink: 0;
}
.user-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.user-name {
  color: var(--td-text-color-primary);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-sub {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.muted-text {
  color: var(--td-text-color-placeholder);
}
.user-form {
  padding-top: 4px;
}

@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .stat-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .filter-bar { flex-direction: column; }
  .filter-select, .filter-select-short { width: 100%; }
}
</style>
