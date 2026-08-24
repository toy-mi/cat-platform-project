<template>
  <div class="task-square">
    <div class="container">
      <h1 class="page-title">任务广场</h1>
      <p class="page-subtitle">志愿者可报名参与猫咪养护任务</p>

      <!-- 筛选栏 -->
      <div class="filters-wrapper">
        <el-form :inline="true" class="filters">
          <el-form-item label="任务状态">
            <el-select v-model="filters.status" placeholder="全部" clearable @change="handleStatusChange" style="width:150px">
              <el-option label="待分配" :value="0"></el-option>
              <el-option label="已分配" :value="1"></el-option>
              <el-option label="进行中" :value="2"></el-option>
              <el-option label="已完成" :value="3"></el-option>
              <el-option label="已取消" :value="4"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-loading="loading" class="task-grid">
        <el-card v-for="task in list" :key="task.id" class="task-card" @click="goToDetail(task.id)">
          <div class="task-title">{{ task.title }}</div>
          <div class="task-type">{{ task.taskType }}</div>
          <div class="task-cat">🐱 {{ task.catId ? getCatName(task.catId) : '全体猫咪' }}</div>
          <div class="task-priority">
            <el-tag :type="task.priority === 1 ? 'danger' : 'info'">
              {{ task.priority === 1 ? '紧急' : '普通' }}
            </el-tag>
          </div>
          <div class="task-deadline">截止：{{ formatDate(task.deadline) }}</div>
          <div class="task-status">
            <el-tag :type="getStatusTagType(task.status)" size="small">
              {{ getStatusText(task.status) }}
            </el-tag>
          </div>
          <div class="task-actions" @click.stop>
            <!-- 如果任务已经指派给当前志愿者 -->
            <template v-if="isVolunteer && task.assignedTo && Number(task.assignedTo) === Number(userId)">
              <el-tag type="success" size="small">这是您的任务</el-tag>
            </template>
            <!-- 否则按原有报名逻辑 -->
            <template v-else>
              <el-button
                v-if="isVolunteer && task.status === 0 && !hasApplied(task.id)"
                type="primary"
                size="small"
                @click="handleApply(task.id)"
                :loading="applyLoading[task.id]"
              >
                报名
              </el-button>
              <el-tag v-else-if="hasApplied(task.id)" type="success" size="small">已报名</el-tag>
              <el-tag v-else-if="task.status !== 0" type="info" size="small">不可报名</el-tag>
            </template>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchTasks"
        @current-change="fetchTasks"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTaskPage, applyTask, getMyAppliedTaskIds } from '@/api/task'
import { getCatPage } from '@/api/cat'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { isVolunteer, userId } = storeToRefs(userStore)

// 任务列表数据
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(6)
const loading = ref(false)

// 筛选条件
const filters = reactive({
  status: null
})

// 报名状态加载
const applyLoading = ref({})

// 猫咪列表
const catList = ref([])
const fetchCats = async () => {
  const res = await getCatPage({ pageNum: 1, pageSize: 100 })
  if (res.code === 200) catList.value = res.data.records
}
const getCatName = (id) => {
  const cat = catList.value.find(c => c.id === id)
  return cat ? cat.name : '未知'
}

// 已报名任务ID集合
const appliedTaskIds = ref(new Set())
const hasApplied = (taskId) => appliedTaskIds.value.has(taskId)

// 获取用户已报名任务ID列表（持久化）
const fetchAppliedTaskIds = async () => {
  if (!isVolunteer.value) return
  try {
    const res = await getMyAppliedTaskIds()
    if (res.code === 200) {
      appliedTaskIds.value = new Set(res.data)
    } else {
      console.warn('获取已报名任务列表失败', res.message)
    }
  } catch (error) {
    console.error('获取已报名任务列表失败', error)
  }
}

// 获取任务列表（支持状态筛选）
const fetchTasks = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: filters.status !== null ? filters.status : undefined
    }
    const res = await getTaskPage(params)
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取任务失败')
    }
  } catch (error) {
    console.error('获取任务失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 报名
const handleApply = async (taskId) => {
  if (!isVolunteer.value) {
    ElMessage.warning('请先申请成为志愿者')
    return
  }
  applyLoading.value[taskId] = true
  try {
    const res = await applyTask(taskId)
    if (res.code === 200) {
      ElMessage.success('报名成功')
      appliedTaskIds.value.add(taskId)
    } else {
      ElMessage.error(res.message || '报名失败')
    }
  } catch (error) {
    console.error('报名失败', error)
    ElMessage.error('网络错误')
  } finally {
    applyLoading.value[taskId] = false
  }
}

// 状态映射
const getStatusText = (status) => {
  const map = { 0:'待分配',1:'已分配',2:'进行中',3:'已完成',4:'已取消' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 0:'info',1:'warning',2:'primary',3:'success',4:'danger' }
  return map[status] || 'info'
}

// 跳转详情
const goToDetail = (id) => {
  router.push(`/tasks/${id}`)
}

// 日期格式化
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()}`
}

// 筛选相关
const handleStatusChange = () => {
  pageNum.value = 1
  fetchTasks()
}
const handleSearch = () => {
  pageNum.value = 1
  fetchTasks()
}
const resetFilters = () => {
  filters.status = null
  handleSearch()
}

onMounted(() => {
  fetchCats()
  fetchTasks()
  if (isVolunteer.value) {
    fetchAppliedTaskIds()
  }
})
</script>

<style scoped>
.task-square {
  padding: 2rem 0;
  background: #f8fbfc;
  min-height: 100vh;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 统一标题样式 */
.page-title {
  font-size: 2rem;
  font-weight: 700;
  text-align: center;
  color: #0f172a;
  margin-bottom: 0.5rem;
}
.page-subtitle {
  text-align: center;
  color: #64748b;
  font-size: 1rem;
  margin-bottom: 2rem;
}

/* 筛选栏居中 */
.filters-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}
.filters {
  background: white;
  padding: 0.8rem 1.5rem;
  border-radius: 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}
.task-card {
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 20px;
}
.task-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}
.task-title {
  font-size: 1.25rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}
.task-type {
  color: #409EFF;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}
.task-cat {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.5rem;
}
.task-priority {
  margin-bottom: 0.5rem;
}
.task-deadline {
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 0.5rem;
}
.task-status {
  margin-bottom: 0.8rem;
}
.task-actions {
  text-align: right;
}
.pagination {
  display: flex;
  justify-content: center;
}
</style>