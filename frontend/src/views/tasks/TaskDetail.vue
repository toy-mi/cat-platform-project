<template>
  <div class="task-detail" v-loading="loading">
    <div class="container">
      <el-page-header @back="goBack" content="任务详情" />

      <el-card v-if="task" class="task-card-detail">
        <h2 class="task-title">{{ task.title }}</h2>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型">{{ task.taskType }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="task.priority === 1 ? 'danger' : 'info'">
              {{ task.priority === 1 ? '紧急' : '普通' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联猫咪">
            {{ task.catId ? getCatName(task.catId) : '全体猫咪' }}
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatDateTime(task.deadline) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(task.status)">
              {{ getStatusText(task.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ task.description || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="action">
          <!-- 情况1：当前用户是被指派的志愿者（无论任务状态） -->
          <div v-if="isAssignedVolunteer" class="assigned-notice">
            <el-tag type="success" size="large">这是您的任务</el-tag>
          </div>
          <!-- 情况2：志愿者且任务待分配且未报名 -->
          <el-button
            v-else-if="isVolunteer && task.status === 0 && !hasApplied"
            type="primary"
            size="large"
            @click="handleApply"
            :loading="applyLoading"
          >
            报名任务
          </el-button>
          <!-- 情况3：已报名但任务未开始（待分配或已完成等） -->
          <el-tag v-else-if="hasApplied && task.status !== 1 && task.status !== 2" type="success" size="large">已报名</el-tag>
          <!-- 情况4：任务已分配但当前用户不是被指派志愿者 -->
          <el-tag v-else-if="task.status === 1" type="info" size="large">任务已分配</el-tag>
          <!-- 其他情况（如任务进行中且非本人）不显示操作 -->
        </div>
      </el-card>

      <!-- 开始任务按钮 -->
      <el-card v-if="isAssignedVolunteer && task && task.status === 1" style="margin-top: 20px;">
        <div style="text-align: center;">
          <el-button type="primary" size="large" @click="handleStartTask" :loading="startLoading">
            开始任务
          </el-button>
        </div>
      </el-card>

      <!-- 显示上一次提交记录（如果存在） -->
      <el-card v-if="isAssignedVolunteer && task && task.status === 2 && (task.completionDescription || task.completionImages)" style="margin-top: 20px;">
        <template #header>
          <span>上一次提交记录</span>
        </template>
        <div>
          <p><strong>提交时间：</strong>{{ task.completionTime || '未知' }}</p>
          <p><strong>完成描述：</strong>{{ task.completionDescription || '无' }}</p>
          <div v-if="task.completionImages" class="complete-images">
            <el-image
              v-for="img in task.completionImages.split(',')"
              :key="img"
              :src="img"
              style="width:100px; height:100px; margin-right:10px;"
              :preview-src-list="task.completionImages.split(',')"
            />
          </div>
          <p><strong>审核状态：</strong>
            <el-tag :type="getCompletionStatusTagType(task.completionStatus)">
              {{ getCompletionStatusText(task.completionStatus) }}
            </el-tag>
          </p>
          <p v-if="task.completionRemark"><strong>审核意见：</strong>{{ task.completionRemark }}</p>
          <el-button type="warning" size="small" @click="fillPreviousSubmission">基于此重新提交</el-button>
        </div>
      </el-card>

      <!-- 提交完成表单（总是显示，只要任务进行中且用户是被指派志愿者） -->
      <el-card
        v-if="isAssignedVolunteer && task && task.status === 2"
        style="margin-top: 20px;"
      >
        <template #header>
          <span>{{ task.completionStatus === 0 ? '提交任务' : '重新提交' }}</span>
        </template>
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
          <el-form-item>
            <el-button type="primary" @click="submitComplete" :loading="submitting">提交</el-button>
            <el-button @click="resetForm">清空重填</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 管理员审核区域 -->
      <el-card v-if="isAdmin && task && task.completionStatus === 1" style="margin-top: 20px;">
        <template #header>
          <span>审核任务完成</span>
        </template>
        <div>
          <p><strong>志愿者提交的描述：</strong>{{ task.completionDescription || '无' }}</p>
          <div v-if="task.completionImages" class="complete-images">
            <el-image
              v-for="img in task.completionImages.split(',')"
              :key="img"
              :src="img"
              style="width:100px; height:100px; margin-right:10px;"
              :preview-src-list="task.completionImages.split(',')"
            />
          </div>
          <el-input v-model="reviewRemark" type="textarea" rows="2" placeholder="审核意见（选填）"></el-input>
          <div style="margin-top: 10px;">
            <el-button type="success" @click="reviewCompletion(2)">通过</el-button>
            <el-button type="danger" @click="reviewCompletion(3)">拒绝</el-button>
          </div>
        </div>
      </el-card>

      <!-- 完成详情（审核通过后展示） -->
      <el-card v-if="task && task.completionStatus === 2" style="margin-top: 20px;">
        <template #header>
          <span>任务完成详情</span>
        </template>
        <p>{{ task.completionDescription || '无' }}</p>
        <div v-if="task.completionImages" class="complete-images">
          <el-image
            v-for="img in task.completionImages.split(',')"
            :key="img"
            :src="img"
            style="width:100px; height:100px; margin-right:10px;"
            :preview-src-list="task.completionImages.split(',')"
          />
        </div>
        <p v-if="task.completionRemark"><strong>审核意见：</strong>{{ task.completionRemark }}</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getTaskById,
  applyTask,
  getMyAppliedTaskIds,
  startTask,
  completeTask,
  reviewTaskCompletion
} from '@/api/task'
import { getCatPage } from '@/api/cat'
import { uploadTaskImage } from '@/api/task'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isVolunteer, userInfo, isAdmin } = storeToRefs(userStore)

const task = ref(null)
const loading = ref(false)
const applyLoading = ref(false)
const startLoading = ref(false)
const uploadRef = ref(null)

const catList = ref([])
const fetchCats = async () => {
  const res = await getCatPage({ pageNum: 1, pageSize: 100 })
  if (res.code === 200) catList.value = res.data.records
}
const getCatName = (id) => {
  const cat = catList.value.find(c => c.id === id)
  return cat ? cat.name : '未知'
}

const getStatusText = (status) => {
  const map = { 0: '待分配', 1: '已分配', 2: '进行中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

const getCompletionStatusText = (status) => {
  const map = { 0: '未提交', 1: '待审核', 2: '已通过', 3: '已拒绝' }
  return map[status] || '未知'
}
const getCompletionStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const appliedTaskIds = ref(new Set())
const fetchAppliedTaskIds = async () => {
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
const hasApplied = computed(() => appliedTaskIds.value.has(Number(route.params.id)))

const isAssignedVolunteer = computed(() => {
  const currentId = userInfo.value?.id
  const assignedId = task.value?.assignedTo
  if (!currentId || !assignedId) return false
  return Number(currentId) === Number(assignedId)
})

const fetchTaskDetail = async () => {
  loading.value = true
  try {
    const res = await getTaskById(route.params.id)
    if (res.code === 200) {
      task.value = res.data
      console.log('任务详情:', task.value)
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

const handleApply = async () => {
  applyLoading.value = true
  try {
    const res = await applyTask(task.value.id)
    if (res.code === 200) {
      ElMessage.success('报名成功')
      appliedTaskIds.value.add(task.value.id)
      fetchTaskDetail()
    } else {
      ElMessage.error(res.message || '报名失败')
    }
  } catch (error) {
    console.error('报名失败', error)
    ElMessage.error('网络错误')
  } finally {
    applyLoading.value = false
  }
}

const handleStartTask = async () => {
  startLoading.value = true
  try {
    const res = await startTask(task.value.id)
    if (res.code === 200) {
      ElMessage.success('任务已开始，请按时完成')
      fetchTaskDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('开始任务失败', error)
    ElMessage.error('网络错误')
  } finally {
    startLoading.value = false
  }
}

const completeForm = reactive({
  description: '',
  images: []
})
const submitting = ref(false)

const handleImageChange = (file, fileList) => {
  completeForm.images = fileList
}

const fillPreviousSubmission = () => {
  completeForm.description = task.value.completionDescription || ''
  completeForm.images = []
  if (uploadRef.value) uploadRef.value.clearFiles()
  ElMessage.info('已填充上次描述，请重新上传图片（如需保留请重新选择）')
}

const resetForm = () => {
  completeForm.description = ''
  completeForm.images = []
  if (uploadRef.value) uploadRef.value.clearFiles()
}

const submitComplete = async () => {
  if (!completeForm.description.trim()) {
    ElMessage.warning('请填写完成描述')
    return
  }
  // 上传图片
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
          ElMessage.warning(`图片 ${file.name} 上传失败: ${res.message}，将跳过`)
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
    const res = await completeTask(task.value.id, data)
    if (res.code === 200) {
      ElMessage.success('提交成功，等待管理员审核')
      fetchTaskDetail()
      resetForm()
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

const reviewRemark = ref('')
const reviewCompletion = async (status) => {
  try {
    const res = await reviewTaskCompletion(task.value.id, status, reviewRemark.value)
    if (res.code === 200) {
      ElMessage.success(status === 2 ? '已通过' : '已拒绝')
      fetchTaskDetail()
      reviewRemark.value = ''
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('审核失败')
  }
}

const goBack = () => {
  router.push('/tasks-square')
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()} ${d.getHours()}:${d.getMinutes()}`
}

onMounted(() => {
  fetchCats()
  fetchTaskDetail()
  if (isVolunteer.value) {
    fetchAppliedTaskIds()
  }
})
</script>

<style scoped>
.task-detail {
  padding: 2rem 0;
}
.task-card-detail {
  margin-top: 20px;
}
.task-title {
  margin-bottom: 1rem;
}
.action {
  margin-top: 2rem;
  text-align: center;
}
.assigned-notice {
  text-align: center;
}
.complete-images {
  display: flex;
  flex-wrap: wrap;
  margin: 10px 0;
}
</style>