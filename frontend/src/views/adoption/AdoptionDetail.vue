<template>
  <div class="adoption-detail" v-loading="loading">
    <el-page-header @back="goBack" content="申请详情" />
    
    <el-card style="margin-top: 20px;" v-if="detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="猫咪">{{ detail.catName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detail.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detail.status)">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请数据" :span="2">
          <pre>{{ formatApplicationData(detail.applicationData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="初审人">{{ detail.firstReviewer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="初审时间">{{ detail.firstReviewTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="初审意见">{{ detail.firstReviewRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="终审人">{{ detail.finalReviewer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="终审时间">{{ detail.finalReviewTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="终审意见">{{ detail.finalReviewRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="协议文件" v-if="detail.agreementUrl">
          <el-link :href="detail.agreementUrl" target="_blank">查看协议</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detail.completeTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 操作按钮区域（根据状态和角色动态显示） -->
      <div style="margin-top: 20px; display: flex; gap: 10px; flex-wrap: wrap;">
        <!-- 普通用户操作 -->
        <template v-if="!isAdmin && !isVolunteer">
          <el-button 
            v-if="[0,1,3,6].includes(detail.status)" 
            type="danger" 
            @click="handleCancel"
          >取消申请</el-button>
        </template>

        <!-- 志愿者/管理员操作 -->
        <template v-if="isAdmin || isVolunteer">
          <!-- 初审 -->
          <el-button 
            v-if="detail.status === 0 && (isAdmin || (isVolunteer && detail.userId != userStore.userId))" 
            type="success" 
            @click="openFirstReviewDialog(1)"
          >初审通过</el-button>
          <el-button 
            v-if="detail.status === 0 && (isAdmin || (isVolunteer && detail.userId != userStore.userId))" 
            type="danger" 
            @click="openFirstReviewDialog(2)"
          >初审拒绝</el-button>

          <!-- 开始回访（初审通过后） -->
<el-button 
  v-if="(isAdmin || isVolunteer) && detail.status === 1" 
  type="primary" 
  @click="handleStartFollowUp"
>开始回访</el-button>

          <!-- 回访 -->
          <el-button 
            v-if="(isAdmin || isVolunteer) && (detail.status === 3 || detail.status === 10)" 
            type="primary" 
            @click="openFollowUpDialog"
          >添加回访</el-button>

          <!-- 回访通过（待回访状态） -->
<el-button 
  v-if="(isAdmin || isVolunteer) && detail.status === 3" 
  type="success" 
  @click="handlePassFollowUp"
>回访通过</el-button>

          <!-- 终审（仅管理员） -->
          <el-button 
            v-if="isAdmin && [4,6].includes(detail.status)" 
            type="success" 
            @click="openFinalReviewDialog(7)"
          >终审通过</el-button>
          <el-button 
            v-if="isAdmin && [4,6].includes(detail.status)" 
            type="danger" 
            @click="openFinalReviewDialog(8)"
          >终审拒绝</el-button>

          <!-- 签订协议 -->
          <el-button 
            v-if="detail.status === 7" 
            type="primary" 
            @click="openSignDialog"
          >上传协议</el-button>

          <!-- 完成领养 -->
          <el-button 
            v-if="detail.status === 9" 
            type="success" 
            @click="handleComplete"
          >完成领养</el-button>

          <!-- 取消（管理员可取消任何状态的申请？根据业务可调整） -->
          <el-button 
            v-if="detail.status !== 10 && detail.status !== 11" 
            type="info" 
            @click="handleCancel"
          >取消申请</el-button>
        </template>
      </div>
    </el-card>

    <!-- 回访记录列表 -->
    <el-card style="margin-top: 20px;" v-if="detail && (isAdmin || isVolunteer)">
      <template #header>
        <span>回访记录</span>
      </template>
      <el-table :data="followUps" border size="small" v-loading="followUpLoading">
        <el-table-column prop="followUpByName" label="回访人" width="120"></el-table-column>
        <el-table-column prop="followUpTime" label="回访时间" width="160"></el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="nextFollowUpDate" label="下次回访日期" width="120"></el-table-column>
      </el-table>
      <el-empty v-if="!followUpLoading && followUps.length === 0" description="暂无回访记录" />
    </el-card>

    <!-- 初审对话框 -->
    <el-dialog v-model="firstReviewVisible" :title="firstReviewTitle" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.remark" type="textarea" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="firstReviewVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFirstReview" :loading="reviewLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 终审对话框（与初审类似，可复用） -->
    <el-dialog v-model="finalReviewVisible" :title="finalReviewTitle" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.remark" type="textarea" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="finalReviewVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFinalReview" :loading="reviewLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 回访对话框 -->
    <el-dialog v-model="followUpVisible" title="添加回访记录" width="500px">
      <el-form :model="followUpForm" label-width="100px">
        <el-form-item label="回访内容">
          <el-input v-model="followUpForm.content" type="textarea" rows="3"></el-input>
        </el-form-item>
        <el-form-item label="下次回访日期">
          <el-date-picker v-model="followUpForm.nextFollowUpDate" type="date" placeholder="选择日期" style="width:100%"></el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="followUpVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFollowUp" :loading="followUpLoadingBtn">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 签订协议对话框（上传文件） -->
    <el-dialog v-model="signVisible" title="上传领养协议" width="500px">
      <el-upload
          class="upload-demo"
          drag
          :action="uploadAction"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :limit="1"
          :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 pdf, doc, docx, jpg, png，大小不超过10M</div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="signVisible = false">取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { 
  getAdoptionDetail, firstReview, finalReview, addFollowUp, 
  getFollowUps, signAgreement, completeAdoption, cancelAdoption,
  startFollowUp,      
  passFollowUp        
} from '@/api/adoption'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isAdmin, isVolunteer } = storeToRefs(userStore)

const id = route.params.id

// 详情数据
const loading = ref(false)
const detail = ref(null)

// 回访记录
const followUps = ref([])
const followUpLoading = ref(false)

// 状态映射函数
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

// 格式化申请数据（JSON转成可读格式）
const formatApplicationData = (data) => {
  if (!data) return '无'
  try {
    const obj = JSON.parse(data)
    const lines = []
    if (obj.houseType) lines.push(`住房类型：${obj.houseType}`)
    if (obj.familyMembers !== undefined) lines.push(`家庭成员数：${obj.familyMembers}人`)
    if (obj.hasChildren !== undefined) lines.push(`是否有小孩：${obj.hasChildren ? '是' : '否'}`)
    if (obj.petExperience) lines.push(`养宠经验：${obj.petExperience}`)
    if (obj.other) lines.push(`其他说明：${obj.other}`)
    // 如果有其他字段，也添加
    for (const [key, value] of Object.entries(obj)) {
      if (!['houseType', 'familyMembers', 'hasChildren', 'petExperience', 'other'].includes(key)) {
        lines.push(`${key}：${value}`)
      }
    }
    return lines.join('\n')
  } catch {
    return data
  }
}

// 获取详情
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getAdoptionDetail(id)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 获取回访记录
const fetchFollowUps = async () => {
  // 如果没有权限，直接返回（防止被意外调用）
  if (!isAdmin.value && !isVolunteer.value) return
  
  followUpLoading.value = true
  try {
    const res = await getFollowUps(id)
    if (res.code === 200) {
      followUps.value = res.data
    } else {
      // 不弹窗，只打印警告
      console.warn('获取回访记录失败', res.message)
    }
  } catch (error) {
    console.error('获取回访记录失败', error)
  } finally {
    followUpLoading.value = false
  }
}

// 开始回访
const handleStartFollowUp = () => {
  ElMessageBox.confirm('确认开始回访？', '提示', { type: 'info' })
    .then(async () => {
      const res = await startFollowUp(id)
      if (res.code === 200) {
        ElMessage.success('已进入待回访状态')
        fetchDetail()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    })
    .catch((error) => {
      // 如果是用户取消确认，error 为 cancel，不显示错误；其他错误显示
      if (error !== 'cancel') {
        console.error('开始回访失败', error)
        ElMessage.error('请求失败，请稍后重试')
      }
    })
}

// 回访通过
const handlePassFollowUp = () => {
  ElMessageBox.confirm('确认回访通过？', '提示', { type: 'info' })
    .then(async () => {
      const res = await passFollowUp(id)
      if (res.code === 200) {
        ElMessage.success('回访通过')
        fetchDetail()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    })
    .catch((error) => {
      // 如果是用户取消确认，error 为 cancel，不显示错误；其他错误显示
      if (error !== 'cancel') {
        console.error('回访失败', error)
        ElMessage.error('请求失败，请稍后重试')
      }
    })
}

// 返回上一页
const goBack = () => {
  router.push('/adoptions')
}

// 初审相关
const firstReviewVisible = ref(false) // 是否显示初审对话框
const firstReviewTitle = ref('') // 初审对话框标题（通过或拒绝）
const reviewForm = reactive({ remark: '' })
const reviewLoading = ref(false)
let currentReviewStatus = null

const openFirstReviewDialog = (status) => {
  currentReviewStatus = status
  firstReviewTitle.value = status === 1 ? '初审通过' : '初审拒绝'
  reviewForm.remark = ''
  firstReviewVisible.value = true
}

const submitFirstReview = async () => {
  reviewLoading.value = true
  try {
    const res = await firstReview(id, currentReviewStatus, reviewForm.remark)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      firstReviewVisible.value = false
      fetchDetail()
      fetchFollowUps()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('初审失败', error)
    ElMessage.error('网络错误')
  } finally {
    reviewLoading.value = false
  }
}

// 终审相关
const finalReviewVisible = ref(false)
const finalReviewTitle = ref('')

const openFinalReviewDialog = (status) => {
  currentReviewStatus = status
  finalReviewTitle.value = status === 7 ? '终审通过' : '终审拒绝'
  reviewForm.remark = ''
  finalReviewVisible.value = true
}

const submitFinalReview = async () => {
  reviewLoading.value = true
  try {
    const res = await finalReview(id, currentReviewStatus, reviewForm.remark)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      finalReviewVisible.value = false
      fetchDetail()
      fetchFollowUps()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('终审失败', error)
    ElMessage.error('网络错误')
  } finally {
    reviewLoading.value = false
  }
}

// 回访相关
const followUpVisible = ref(false)
const followUpForm = reactive({
  content: '',
  nextFollowUpDate: null
})
const followUpLoadingBtn = ref(false)

const openFollowUpDialog = () => {
  followUpForm.content = ''
  followUpForm.nextFollowUpDate = null
  followUpVisible.value = true
}

const submitFollowUp = async () => {
  if (!followUpForm.content) {
    ElMessage.warning('请输入回访内容')
    return
  }
  followUpLoadingBtn.value = true
  try {
    const res = await addFollowUp(id, followUpForm)
    if (res.code === 200) {
      ElMessage.success('回访记录添加成功')
      followUpVisible.value = false
      fetchFollowUps()
      fetchDetail() // 更新申请信息中的最后回访内容
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('添加回访失败', error)
    ElMessage.error('网络错误')
  } finally {
    followUpLoadingBtn.value = false
  }
}

// 签订协议（上传文件）
const signVisible = ref(false)
const fileList = ref([])
const uploadAction = computed(() => {
  // 一个文件上传接口，接收文件并返回URL
  return `http://localhost:8080/api/adoptions/${id}/upload-agreement`
})
const uploadHeaders = computed(() => {
  return {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
const beforeUpload = (file) => {
  const isValidType = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'image/jpeg', 'image/png'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isValidType) {
    ElMessage.error('只能上传 pdf, doc, docx, jpg, png 格式')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('协议上传成功')
    signVisible.value = false
    fetchDetail()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}
const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}
const openSignDialog = () => {
  fileList.value = []
  signVisible.value = true
}

// 完成领养
const handleComplete = () => {
  ElMessageBox.confirm('确认完成领养？此操作不可逆', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await completeAdoption(id)
    if (res.code === 200) {
      ElMessage.success('领养完成')
      fetchDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  }).catch(() => {})
}

// 取消申请
const handleCancel = () => {
  ElMessageBox.confirm('确认取消该申请？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await cancelAdoption(id)
      if (res.code === 200) {
        ElMessage.success('已取消')
        // 跳转到领养申请列表页
        router.push('/adoptions')
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchDetail()
  // 只有管理员或志愿者才获取回访记录
  if (isAdmin.value || isVolunteer.value) {
    fetchFollowUps()
  }
})
</script>

<style scoped>
.adoption-detail {
  padding: 20px;
}
</style>