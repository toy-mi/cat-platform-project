<template>
  <div class="user-management">
    <h2>用户管理</h2>

    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.username" placeholder="请输入" clearable></el-input>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="searchForm.nickname" placeholder="请输入" clearable></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="searchForm.email" placeholder="请输入" clearable></el-input>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="searchForm.role" placeholder="请选择" clearable style="width: 150px;">
          <el-option label="普通用户" value="USER"></el-option>
          <el-option label="志愿者" value="VOLUNTEER"></el-option>
          <el-option label="管理员" value="ADMIN"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 100px;">
          <el-option label="正常" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
      <el-form-item style="float: right;">
        <el-button type="success" @click="openAddDialog">新增用户</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="username" label="用户名" width="150"></el-table-column>
      <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
      <el-table-column prop="email" label="邮箱" show-overflow-tooltip width="250"></el-table-column>
      <el-table-column prop="phone" label="手机" width="150"></el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'VOLUNTEER' ? 'success' : 'info'">
            {{ row.role === 'ADMIN' ? '管理员' : row.role === 'VOLUNTEER' ? '志愿者' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="{ row }">
            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
          <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button 
            :type="row.status === 1 ? 'warning' : 'success'" 
            size="small" 
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="info" size="small" @click="openResetPasswordDialog(row)">重置密码</el-button>
          <!-- 物理删除按钮（危险，可保留但隐藏或二次确认） -->
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchData"
        @current-change="fetchData"
    />

    <!-- 新增/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择">
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="志愿者" value="VOLUNTEER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="passwordVisible" title="重置密码" width="400px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPassword" :loading="passwordLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAdminUserPage, addUser, updateUser, resetPassword, 
  updateUserStatus, deleteUser 
} from '@/api/user'

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  nickname: '',
  email: '',
  role: '',
  status: null
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await getAdminUserPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => searchForm[key] = '')
  searchForm.status = null
  handleSearch()
}

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const userFormRef = ref()
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'USER',
  status: 1
})
const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const openAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, row)
  dialogVisible.value = true
}

const resetForm = () => {
  userFormRef.value?.resetFields()
  Object.keys(userForm).forEach(key => {
    if (key === 'status') userForm[key] = 1
    else if (key === 'role') userForm[key] = 'USER'
    else userForm[key] = ''
  })
  userForm.id = null
}

const submitForm = async () => {
  await userFormRef.value?.validate()
  submitLoading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateUser(userForm.id, userForm)
    } else {
      res = await addUser(userForm)
    }
    if (res.code === 200) {
      ElMessage.success(res.message)
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('网络错误')
  } finally {
    submitLoading.value = false
  }
}

// 重置密码
const passwordVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const openResetPasswordDialog = (row) => {
  passwordForm.userId = row.id
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisible.value = true
}

const submitResetPassword = async () => {
  await passwordFormRef.value?.validate()
  passwordLoading.value = true
  try {
    const res = await resetPassword(passwordForm.userId, passwordForm.newPassword)
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
      passwordVisible.value = false
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('重置密码失败', error)
    ElMessage.error('网络错误')
  } finally {
    passwordLoading.value = false
  }
}

// 禁用/启用用户
const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确认${action}用户【${row.username}】？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await updateUserStatus(row.id, newStatus)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        fetchData()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    })
    .catch(() => {})
}

// 物理删除（可选，谨慎）
const handleDelete = (id) => {
  ElMessageBox.confirm('确认永久删除该用户？此操作不可恢复，且会删除关联数据！', '危险操作', { type: 'error' })
    .then(async () => {
      const res = await deleteUser(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>