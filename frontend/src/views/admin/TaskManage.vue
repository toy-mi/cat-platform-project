<template>
  <div class="task-list">
    <h2>养护任务系统</h2>

    <!-- 搜索/筛选栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="任务状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 200px;">
          <el-option label="待分配" :value="0"></el-option>
          <el-option label="已分配" :value="1"></el-option>
          <el-option label="进行中" :value="2"></el-option>
          <el-option label="已完成" :value="3"></el-option>
          <el-option label="已取消" :value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="关联猫咪">
        <el-select v-model="searchForm.catId" placeholder="请选择猫咪" clearable filterable style="width: 200px;">
          <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
      <el-form-item style="float: right;">
        <el-button type="success" @click="openAddDialog" v-if="isAdmin">发布任务</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="title" label="标题" width="100"></el-table-column>
      <el-table-column prop="taskType" label="类型" width="100"></el-table-column>
      <el-table-column label="关联猫咪" width="100">
        <template #default="{ row }">
          <span>{{ row.catId ? getCatName(row.catId) : '全体' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="row.priority === 1 ? 'danger' : 'info'">
            {{ row.priority === 1 ? '紧急' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="completionStatus" label="完成状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getCompletionStatusTagType(row.completionStatus)">
            {{ getCompletionStatusText(row.completionStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assignedTo" label="指派给" width="150">
        <template #default="{ row }">
          <span>{{ row.assignedToName || (row.assignedTo ? '志愿者' + row.assignedTo : '-') }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止时间" width="200"></el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="200"></el-table-column>
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="{ row }">
          <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <el-button type="primary" size="small" @click="openDetail(row.id)">详情</el-button>

            <!-- 管理员操作 -->
            <template v-if="isAdmin">
              <el-button type="warning" size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
              <el-button 
                v-if="row.status === 0 || row.status === 1" 
                type="success" 
                size="small" 
                @click="openAssignDialog(row)"
              >指派</el-button>
              <el-button 
                v-if="row.completionStatus === 1" 
                type="info" 
                size="small" 
                @click="openReviewDialog(row)"
              >审核</el-button>
            </template>

            <!-- 志愿者操作 -->
            <template v-else-if="isVolunteer">
              <!-- 报名按钮 -->
              <el-button 
                v-if="row.status === 0 && !hasApplied(row.id)" 
                type="primary" 
                size="small" 
                @click="handleApply(row.id)"
              >报名</el-button>
              <el-button 
                v-if="row.status === 0 && hasApplied(row.id)" 
                type="info" 
                size="small" 
                disabled
              >已报名</el-button>
              <!-- 开始任务按钮 -->
              <el-button 
                v-if="row.assignedTo && Number(row.assignedTo) === Number(userId) && row.status === 1" 
                type="success" 
                size="small" 
                @click="handleStart(row.id)"
              >开始</el-button>
              <!-- 提交/重新提交按钮：进行中 且 被指派 且 (未提交 或 待审核 或 审核拒绝) -->
              <el-button 
                v-if="row.assignedTo && Number(row.assignedTo) === Number(userId) && row.status === 2 && (row.completionStatus === 0 || row.completionStatus === 1 || row.completionStatus === 3)" 
                :type="(row.completionStatus === 1 || row.completionStatus === 3) ? 'warning' : 'success'"
                size="small" 
                @click="openSubmitDialog(row)"
              >
                {{ (row.completionStatus === 1 || row.completionStatus === 3) ? '重新提交' : '提交任务' }}
              </el-button>
            </template>
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

    <!-- 发布/编辑任务对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="taskForm.title"></el-input>
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="taskForm.taskType" placeholder="请选择">
            <el-option label="定时投喂" value="定时投喂"></el-option>
            <el-option label="清洁任务" value="清洁任务"></el-option>
            <el-option label="医疗陪护" value="医疗陪护"></el-option>
            <el-option label="审核捐赠" value="审核捐赠"></el-option>
            <el-option label="补充库存" value="补充库存"></el-option>
            <el-option label="猫咪信息维护" value="猫咪信息维护"></el-option>
            <el-option label="审核领养申请" value="审核领养申请"></el-option>
            <el-option label="定期回访" value="定期回访"></el-option>
            <el-option label="寻猫任务" value="寻猫任务"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联猫咪" prop="catId">
          <el-select v-model="taskForm.catId" placeholder="请选择（留空表示全体）" clearable filterable>
            <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="taskForm.priority">
            <el-radio :label="0">普通</el-radio>
            <el-radio :label="1">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker v-model="taskForm.deadline" type="datetime" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="taskForm.description" type="textarea" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 指派志愿者对话框 -->
    <el-dialog v-model="assignVisible" title="指派志愿者" width="400px">
      <el-select v-model="selectedVolunteerId" placeholder="请选择志愿者" filterable style="width:100%">
        <el-option v-for="user in volunteerList" :key="user.id" :label="user.username" :value="user.id"></el-option>
      </el-select>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssign">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="600px">
      <el-descriptions :column="1" border v-if="currentTask">
        <el-descriptions-item label="ID">{{ currentTask.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentTask.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentTask.taskType }}</el-descriptions-item>
        <el-descriptions-item label="关联猫咪">{{ currentTask.catId ? getCatName(currentTask.catId) : '全体' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ currentTask.priority === 1 ? '紧急' : '普通' }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ getStatusText(currentTask.status) }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ getCompletionStatusText(currentTask.completionStatus) }}</el-descriptions-item>
        <el-descriptions-item label="指派给">{{ currentTask.assignedToName || currentTask.assignedTo || '未指派' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentTask.deadline }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentTask.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="完成描述">{{ currentTask.completionDescription || '无' }}</el-descriptions-item>
        <el-descriptions-item label="完成图片">
          <div v-if="currentTask.completionImages && currentTask.completionImages.trim()">
            <el-image 
              v-for="img in currentTask.completionImages.split(',')" 
              :key="img" 
              :src="img" 
              style="width:80px; height:80px; margin-right:5px;" 
              :preview-src-list="currentTask.completionImages.split(',')" 
            />
          </div>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ currentTask.completionRemark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentTask.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentTask.completeTime || '未完成' }}</el-descriptions-item>
      </el-descriptions>
      <!-- 报名列表（仅管理员可见） -->
      <div v-if="isAdmin && applications.length >= 0" style="margin-top:20px">
        <el-collapse v-model="activeCollapse" accordion>
          <el-collapse-item name="applications">
            <template #title>
              <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                <span>报名列表</span>
                <el-button 
                  v-if="currentTask?.status === 0" 
                  type="primary" 
                  size="small" 
                  @click.stop="handleAutoAssign"
                  :loading="autoAssignLoading"
                >自动指派</el-button>
              </div>
            </template>
            <el-table :data="applications" border size="small">
              <el-table-column prop="userName" label="志愿者" width="120"></el-table-column>
              <el-table-column prop="applyTime" label="报名时间"></el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'">
                    {{ row.status === 0 ? '待审核' : row.status === 1 ? '已选中' : row.status === 2 ? '未选中' : '已取消' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <div style="display: flex; gap: 8px;">
                    <el-button v-if="row.status === 0" size="small" type="success" @click="handleReview(row.id, 1)">选中</el-button>
                    <el-button v-if="row.status === 0" size="small" type="danger" @click="handleReview(row.id, 2)">拒绝</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>

    <!-- 提交完成对话框（增强版） -->
    <el-dialog v-model="submitDialogVisible" title="提交任务" width="600px" @close="resetSubmitForm">
      <!-- 显示上一次提交记录（如果存在） -->
      <div v-if="currentSubmitTask && (currentSubmitTask.completionDescription || currentSubmitTask.completionImages)" class="previous-submission">
        <el-divider content-position="left">上一次提交记录</el-divider>
        <p><strong>提交时间：</strong>{{ currentSubmitTask.completionTime || '未知' }}</p>
        <p><strong>完成描述：</strong>{{ currentSubmitTask.completionDescription || '无' }}</p>
        <div v-if="currentSubmitTask.completionImages" class="complete-images">
          <el-image
            v-for="img in currentSubmitTask.completionImages.split(',')"
            :key="img"
            :src="img"
            style="width:80px; height:80px; margin-right:10px;"
            :preview-src-list="currentSubmitTask.completionImages.split(',')"
          />
        </div>
        <p><strong>审核状态：</strong>
          <el-tag :type="getCompletionStatusTagType(currentSubmitTask.completionStatus)">
            {{ getCompletionStatusText(currentSubmitTask.completionStatus) }}
          </el-tag>
        </p>
        <p v-if="currentSubmitTask.completionRemark"><strong>审核意见：</strong>{{ currentSubmitTask.completionRemark }}</p>
        <el-button type="warning" size="small" @click="fillPreviousSubmission">基于此重新提交</el-button>
        <el-divider />
      </div>

      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="完成描述" required>
          <el-input v-model="completeForm.description" type="textarea" rows="4" placeholder="请描述任务完成情况..."></el-input>
        </el-form-item>
        <el-form-item label="完成图片">
          <el-upload
            ref="uploadRef"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleImageChange"
            :limit="9"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitComplete" :loading="submitting">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="审核任务完成" width="500px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="reviewForm.status">
            <el-radio :label="2">通过</el-radio>
            <el-radio :label="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.remark" type="textarea" rows="3" placeholder="选填"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="reviewLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getTaskPage, getTaskById, createTask, updateTask, deleteTask,
  applyTask, getTaskApplications, reviewApplication,
  assignTask, startTask, completeTask, getMyAppliedTaskIds,
  autoAssignTask, reviewTaskCompletion, uploadTaskImage
} from '@/api/task'
import { getCatPage } from '@/api/cat'
import { getVolunteerList } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const userStore = useUserStore()
const { userId, isAdmin, isVolunteer } = storeToRefs(userStore)

// ==================== 表格与搜索 ====================
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const activeCollapse = ref(['applications'])

const searchForm = reactive({
  status: null,
  catId: null
})

// 猫咪列表
const catList = ref([])
const fetchCats = async () => {
  const res = await getCatPage({ pageNum:1, pageSize:100 })
  if (res.code === 200) catList.value = res.data.records
}
const getCatName = (id) => {
  const cat = catList.value.find(c => c.id === id)
  return cat ? cat.name : '未知'
}

// 志愿者列表映射（用于显示用户名）
const volunteerMap = ref(new Map())
const fetchVolunteerList = async () => {
  try {
    const res = await getVolunteerList()
    if (res.code === 200) {
      volunteerMap.value.clear()
      res.data.forEach(user => {
        volunteerMap.value.set(user.id, user.username)
      })
    }
  } catch (error) {
    console.error('获取志愿者列表失败', error)
  }
}

// 为任务记录填充 assignedToName
const fillAssignedToName = (records) => {
  if (!records || !records.length) return
  records.forEach(record => {
    if (record.assignedTo && volunteerMap.value.has(record.assignedTo)) {
      record.assignedToName = volunteerMap.value.get(record.assignedTo)
    } else {
      record.assignedToName = record.assignedTo ? `志愿者${record.assignedTo}` : '-'
    }
  })
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

// 完成状态映射
const getCompletionStatusText = (status) => {
  const map = { 0: '未提交', 1: '待审核', 2: '已通过', 3: '已拒绝' }
  return map[status] || '未知'
}
const getCompletionStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

// 已报名任务ID集合
const appliedTaskIds = ref(new Set())
const hasApplied = (taskId) => appliedTaskIds.value.has(taskId)

const fetchMyAppliedTasks = async () => {
  if (!isVolunteer.value) return
  try {
    const res = await getMyAppliedTaskIds()
    if (res.code === 200) {
      appliedTaskIds.value = new Set(res.data)
    }
  } catch (error) {
    console.error('获取已报名任务失败', error)
  }
}

// 获取任务列表
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await getTaskPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
      // 填充指派给用户名
      fillAssignedToName(tableData.value)
    } else {
      ElMessage.error(res.message || '获取任务失败')
    }
  } catch (error) {
    console.error('获取任务列表失败', error)
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
  searchForm.status = null
  searchForm.catId = null
  handleSearch()
}

// ==================== 发布/编辑任务 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const taskFormRef = ref()
const taskForm = reactive({
  id: null,
  title: '',
  taskType: '',
  catId: null,
  priority: 0,
  deadline: null,
  description: ''
})
const taskRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

const openAddDialog = () => {
  dialogTitle.value = '发布任务'
  dialogVisible.value = true
}
const openEditDialog = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(taskForm, row)
  if (row.deadline) taskForm.deadline = new Date(row.deadline)
  dialogVisible.value = true
}
const resetForm = () => {
  taskFormRef.value?.resetFields()
  Object.keys(taskForm).forEach(k => {
    if (k === 'priority') taskForm[k] = 0
    else taskForm[k] = null
  })
  taskForm.id = null
}
const submitForm = async () => {
  await taskFormRef.value?.validate()
  submitLoading.value = true
  try {
    let res
    if (taskForm.id) {
      res = await updateTask(taskForm.id, taskForm)
    } else {
      res = await createTask(taskForm)
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
    ElMessage.error('提交失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该任务吗？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteTask(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

// ==================== 任务详情 ====================
const detailVisible = ref(false)
const currentTask = ref(null)
const applications = ref([])
const openDetail = async (id) => {
  const res = await getTaskById(id)
  if (res.code === 200) {
    currentTask.value = res.data
    // 填充指派给用户名
    if (currentTask.value.assignedTo && volunteerMap.value.has(currentTask.value.assignedTo)) {
      currentTask.value.assignedToName = volunteerMap.value.get(currentTask.value.assignedTo)
    } else {
      currentTask.value.assignedToName = currentTask.value.assignedTo ? `志愿者${currentTask.value.assignedTo}` : '未指派'
    }
    detailVisible.value = true
    if (isAdmin.value) {
      const appRes = await getTaskApplications(id)
      if (appRes.code === 200) {
        applications.value = appRes.data
      }
    }
  } else {
    ElMessage.error(res.message || '获取详情失败')
  }
}

// ==================== 指派志愿者 ====================
const assignVisible = ref(false)
const selectedTaskId = ref(null)
const selectedVolunteerId = ref(null)
const volunteerList = ref([])
const openAssignDialog = (row) => {
  selectedTaskId.value = row.id
  getVolunteerList().then(res => {
    if (res.code === 200) volunteerList.value = res.data
  })
  assignVisible.value = true
}
const handleAssign = async () => {
  if (!selectedVolunteerId.value) {
    ElMessage.warning('请选择志愿者')
    return
  }
  const res = await assignTask(selectedTaskId.value, selectedVolunteerId.value)
  if (res.code === 200) {
    ElMessage.success('指派成功')
    assignVisible.value = false
    fetchData()
  } else {
    ElMessage.error(res.message || '指派失败')
  }
}

// ==================== 志愿者报名、开始任务 ====================
const handleApply = async (taskId) => {
  const res = await applyTask(taskId)
  if (res.code === 200) {
    ElMessage.success('报名成功')
    appliedTaskIds.value.add(taskId)
    fetchData()
  } else {
    ElMessage.error(res.message || '报名失败')
  }
}

const handleStart = async (taskId) => {
  const res = await startTask(taskId)
  if (res.code === 200) {
    ElMessage.success('任务已开始')
    fetchData()
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

// ==================== 管理员审核报名 ====================
const handleReview = async (applicationId, status) => {
  const res = await reviewApplication(applicationId, status)
  if (res.code === 200) {
    ElMessage.success('操作成功')
    if (currentTask.value) {
      const appRes = await getTaskApplications(currentTask.value.id)
      if (appRes.code === 200) applications.value = appRes.data
    }
    fetchData()
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

// 自动指派
const autoAssignLoading = ref(false)
const handleAutoAssign = async () => {
  if (!currentTask.value) return
  autoAssignLoading.value = true
  try {
    const res = await autoAssignTask(currentTask.value.id)
    if (res.code === 200) {
      ElMessage.success('自动指派成功')
      await openDetail(currentTask.value.id)
      fetchData()
    } else {
      ElMessage.error(res.message || '自动指派失败')
    }
  } catch (error) {
    console.error('自动指派失败', error)
    ElMessage.error('网络错误')
  } finally {
    autoAssignLoading.value = false
  }
}

// ==================== 志愿者提交完成（增强版） ====================
const submitDialogVisible = ref(false)
const currentSubmitTask = ref(null)
const completeForm = reactive({
  description: '',
  images: []
})
const uploadRef = ref(null)
const submitting = ref(false)

const openSubmitDialog = (row) => {
  currentSubmitTask.value = row
  completeForm.description = ''
  completeForm.images = []
  if (uploadRef.value) uploadRef.value.clearFiles()
  submitDialogVisible.value = true
}

const handleImageChange = (file, fileList) => {
  completeForm.images = fileList
}

const resetSubmitForm = () => {
  completeForm.description = ''
  completeForm.images = []
  if (uploadRef.value) uploadRef.value.clearFiles()
}

// 填充上一次提交内容到表单
const fillPreviousSubmission = () => {
  if (currentSubmitTask.value) {
    completeForm.description = currentSubmitTask.value.completionDescription || ''
    // 图片无法直接复用，需要用户重新上传
    completeForm.images = []
    if (uploadRef.value) uploadRef.value.clearFiles()
    ElMessage.info('已填充上次描述，请重新上传图片（如需保留请重新选择）')
  }
}

const submitComplete = async () => {
  if (!completeForm.description.trim()) {
    ElMessage.warning('请填写完成描述')
    return
  }
  let imageUrls = []
  if (completeForm.images.length > 0) {
    for (const file of completeForm.images) {
      const formData = new FormData()
      formData.append('file', file.raw)
      try {
        const res = await uploadTaskImage(formData)
        if (res.code === 200) {
          imageUrls.push(res.data)
        } else {
          ElMessage.warning(`图片 ${file.name} 上传失败，将跳过`)
        }
      } catch (error) {
        console.error('图片上传出错', error)
        ElMessage.warning(`图片 ${file.name} 上传失败，将跳过`)
      }
    }
  }
  const data = {
    description: completeForm.description,
    images: imageUrls.join(',')
  }
  submitting.value = true
  try {
    const res = await completeTask(currentSubmitTask.value.id, data)
    if (res.code === 200) {
      ElMessage.success('提交成功，等待管理员审核')
      submitDialogVisible.value = false
      fetchData()  // 刷新列表
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

// ==================== 管理员审核完成 ====================
const reviewDialogVisible = ref(false)
const currentReviewTask = ref(null)
const reviewForm = reactive({
  status: 2,
  remark: ''
})
const reviewLoading = ref(false)

const openReviewDialog = (row) => {
  currentReviewTask.value = row
  reviewForm.status = 2
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  reviewLoading.value = true
  try {
    const res = await reviewTaskCompletion(currentReviewTask.value.id, reviewForm.status, reviewForm.remark)
    if (res.code === 200) {
      ElMessage.success(reviewForm.status === 2 ? '已通过' : '已拒绝')
      reviewDialogVisible.value = false
      fetchData()
      // 如果详情对话框打开，刷新详情
      if (detailVisible.value && currentTask.value && currentTask.value.id === currentReviewTask.value.id) {
        await openDetail(currentTask.value.id)
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('审核失败')
  } finally {
    reviewLoading.value = false
  }
}

// ==================== 生命周期 ====================
onMounted(async () => {
  await fetchVolunteerList()
  fetchCats()
  fetchData()
  fetchMyAppliedTasks()
})
</script>

<style scoped>
.task-list {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
.complete-images {
  display: flex;
  flex-wrap: wrap;
  margin: 10px 0;
}
.previous-submission {
  margin-bottom: 20px;
  background: #f9f9f9;
  padding: 10px;
  border-radius: 8px;
}
</style>