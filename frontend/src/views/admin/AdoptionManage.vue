<template>
  <div class="adoption-list">
    <h2>领养申请管理</h2>

    <!-- 搜索/筛选栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="申请状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 200px;">
          <el-option label="待初审" :value="0"></el-option>
          <el-option label="初审通过" :value="1"></el-option>
          <el-option label="初审拒绝" :value="2"></el-option>
          <el-option label="待回访" :value="3"></el-option>
          <el-option label="回访通过" :value="4"></el-option>
          <el-option label="回访失败" :value="5"></el-option>
          <el-option label="待终审" :value="6"></el-option>
          <el-option label="终审通过" :value="7"></el-option>
          <el-option label="终审拒绝" :value="8"></el-option>
          <el-option label="已签订协议" :value="9"></el-option>
          <el-option label="已完成领养" :value="10"></el-option>
          <el-option label="已取消" :value="11"></el-option>
        </el-select>
      </el-form-item>

      <!-- 仅管理员/志愿者显示猫咪和申请人筛选 -->
      <template v-if="isAdmin || isVolunteer">
        <el-form-item label="猫咪">
          <el-select v-model="searchForm.catId" placeholder="" clearable filterable style="width: 200px;">
            <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="申请人">
          <el-select v-model="searchForm.userId" placeholder="请选择用户" clearable filterable style="width: 200px;">
            <el-option v-for="user in userList" :key="user.id" :label="user.username" :value="user.id"></el-option>
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="申请日期">
        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="userName" label="申请人" width="100"></el-table-column>
      <el-table-column prop="catName" label="猫咪" width="100"></el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160"></el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="firstReviewRemark" label="初审意见" show-overflow-tooltip></el-table-column>
      <el-table-column prop="finalReviewRemark" label="终审意见" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDetail(row.id)">详情</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdoptionPage } from '@/api/adoption'
import { getCatPage } from '@/api/cat'            // 用于获取猫咪列表下拉
import { getUserPage } from '@/api/user'          // 用于获取用户列表下拉（需实现）
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { isAdmin, isVolunteer, token } = storeToRefs(userStore)

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 搜索表单（扩展字段）
const searchForm = reactive({
  status: null,
  catId: null,
  userId: null,
  startTime: null,
  endTime: null
})

// 日期范围绑定
const dateRange = ref([])

// 猫咪下拉列表
const catList = ref([])
const fetchCats = async () => {
  try {
    const res = await getCatPage({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      catList.value = res.data.records
    } else {
      console.error('获取猫咪列表失败', res.message)
    }
  } catch (error) {
    console.error('获取猫咪列表异常', error)
  }
}

// 用户下拉列表（申请人）
const userList = ref([])
const fetchUsers = async () => {
  try {
    // 假设 getUserPage 接口存在，且返回 { code:200, data: { records: [...] } }
    const res = await getUserPage({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      userList.value = res.data.records
    } else {
      console.error('获取用户列表失败', res.message)
    }
  } catch (error) {
    console.error('获取用户列表异常', error)
  }
}

// 监听日期范围变化，更新 searchForm 中的起止时间
watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    searchForm.startTime = newVal[0]
    searchForm.endTime = newVal[1]
  } else {
    searchForm.startTime = null
    searchForm.endTime = null
  }
})

// 获取申请列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await getAdoptionPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 查询（重置页码并重新获取）
const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

// 重置所有筛选条件
const resetSearch = () => {
  searchForm.status = null
  searchForm.catId = null
  searchForm.userId = null
  searchForm.startTime = null
  searchForm.endTime = null
  dateRange.value = []          // 清空日期选择
  handleSearch()
}

// 状态映射（保持不变）
const getStatusText = (status) => {
  const map = {
    0: '待初审',
    1: '初审通过',
    2: '初审拒绝',
    3: '待回访',
    4: '回访通过',
    5: '回访失败',
    6: '待终审',
    7: '终审通过',
    8: '终审拒绝',
    9: '已签订协议',
    10: '已完成领养',
    11: '已取消'
  }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'danger',
    3: 'info',
    4: 'success',
    5: 'danger',
    6: 'info',
    7: 'success',
    8: 'danger',
    9: 'success',
    10: 'success',
    11: 'info'
  }
  return map[status] || 'info'
}

// 跳转详情
const openDetail = (id) => {
  router.push(`/adoptions/${id}`)
}

// 监听 token 变化（原代码已有，保留）
watch(token, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal) {
    pageNum.value = 1
    fetchData()
  }
})

// 初始化
onMounted(() => {
  fetchCats()                     // 获取猫咪下拉数据
  if (isAdmin.value || isVolunteer.value) {
    fetchUsers()                  // 仅管理员/志愿者需要获取用户下拉数据
  }
  fetchData()                     // 获取列表数据
})
</script>

<style scoped>
.adoption-list {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>