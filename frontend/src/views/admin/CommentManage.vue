<template>
  <div class="admin-comment">
    <h2>评论管理</h2>

    <!-- 搜索栏 -->
    <el-row :gutter="10" style="margin-bottom: 20px;">
      <el-col :span="4">
        <el-input v-model="searchPostId" placeholder="动态ID" clearable @keyup.enter="handleSearch" />
      </el-col>
      <el-col :span="4">
        <el-input v-model="searchUserId" placeholder="用户ID" clearable @keyup.enter="handleSearch" />
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
      <el-table-column prop="userName" label="评论人" width="100"></el-table-column>
      <el-table-column prop="postTitle" label="关联动态" width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="content" label="评论内容" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="200"></el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchData"
        @current-change="fetchData"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminComments, adminDeleteComment } from '@/api/admin'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const searchPostId = ref('')
const searchUserId = ref('')

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      postId: searchPostId.value ? Number(searchPostId.value) : undefined,
      userId: searchUserId.value ? Number(searchUserId.value) : undefined
    }
    const res = await getAdminComments(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取评论列表失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

const resetSearch = () => {
  searchPostId.value = ''
  searchUserId.value = ''
  handleSearch()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该评论？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await adminDeleteComment(id)
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
.admin-comment {
  padding: 20px;
}
</style>