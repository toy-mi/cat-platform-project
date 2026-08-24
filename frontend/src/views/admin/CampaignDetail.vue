<template>
  <div class="campaign-detail" v-loading="loading">
    <el-page-header @back="goBack" content="募捐活动详情" />

    <el-card v-if="campaign" style="margin-top: 20px;">
      <h2>{{ campaign.title }}</h2>
      <p>{{ campaign.description }}</p>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="目标金额">￥{{ campaign.targetAmount }}</el-descriptions-item>
        <el-descriptions-item label="已筹金额">￥{{ campaign.currentAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="进度">{{ getCampaignProgress(campaign).toFixed(1) }}%</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(campaign.status) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDate(campaign.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDate(campaign.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="关联猫咪" v-if="campaign.catName">{{ campaign.catName }}</el-descriptions-item>
      </el-descriptions>
      <!-- <el-progress :percentage="getCampaignProgress(campaign)" :format="formatProgress"></el-progress> -->

      <!-- 管理员操作按钮 -->
      <div class="admin-actions" style="margin-top: 20px;">
        <el-button type="primary" @click="openEditDialog">编辑活动</el-button>
        <el-button type="danger" @click="handleDeleteCampaign">删除活动</el-button>
      </div>

      <!-- 捐赠记录列表（管理员可见） -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>捐赠记录</span>
        </template>
        <donation-list :campaign-id="campaignId" :show-audit="true" @refresh="fetchCampaign" />
      </el-card>
    </el-card>

    <!-- 编辑活动对话框（复用原组件逻辑） -->
    <el-dialog v-model="editDialogVisible" title="编辑活动" width="500px" @close="resetEditForm">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" rows="3"></el-input>
        </el-form-item>
        <el-form-item label="目标金额" prop="targetAmount">
          <el-input-number v-model="editForm.targetAmount" :precision="2" :min="0.01" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="开始时间" prop="startDate">
          <el-date-picker v-model="editForm.startDate" type="datetime" placeholder="选择开始时间" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endDate">
          <el-date-picker v-model="editForm.endDate" type="datetime" placeholder="选择结束时间" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="关联猫咪" prop="catId">
          <el-select v-model="editForm.catId" placeholder="请选择" clearable filterable style="width:100%">
            <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择">
            <el-option label="筹备中" :value="0"></el-option>
            <el-option label="进行中" :value="1"></el-option>
            <el-option label="已结束" :value="2"></el-option>
            <el-option label="已取消" :value="3"></el-option>
            <el-option label="已完成" :value="4"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm" :loading="editLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCampaignDetail, updateCampaign, deleteCampaign } from '@/api/donation'
import { getCatPage } from '@/api/cat'
import DonationList from '@/components/DonationList.vue'

const route = useRoute()
const router = useRouter()
const campaignId = route.params.id
const loading = ref(false)
const campaign = ref(null)
const catList = ref([])

const fetchCampaign = async () => {
  loading.value = true
  try {
    const res = await getCampaignDetail(campaignId)
    if (res.code === 200) {
      campaign.value = res.data
    } else {
      ElMessage.error(res.message || '获取活动失败')
    }
  } catch (error) {
    console.error('获取活动失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const fetchCats = async () => {
  const res = await getCatPage({ pageNum: 1, pageSize: 100 })
  if (res.code === 200) {
    catList.value = res.data.records
  }
}

const getStatusText = (status) => {
  const map = { 0: '筹备中', 1: '进行中', 2: '已结束', 3: '已取消', 4: '已完成' }
  return map[status] || '未知'
}
const getCampaignProgress = (campaign) => {
  const target = Number(campaign.targetAmount || 0)
  const current = Number(campaign.currentAmount || 0)
  if (campaign.progress !== undefined && campaign.progress !== null) {
    return Number(campaign.progress) || 0
  }
  if (target <= 0) return 0
  const p = (current / target) * 100
  return p > 100 ? 100 : p
}
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()}`
}
const formatProgress = (percentage) => {
  return percentage === 0 ? '0%' : percentage.toFixed(1) + '%'
}
const goBack = () => {
  router.push('/admin/campaigns')
}

// 编辑活动相关
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  title: '',
  description: '',
  targetAmount: null,
  startDate: null,
  endDate: null,
  catId: null,
  status: 0
})
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}
const openEditDialog = () => {
  Object.assign(editForm, campaign.value)
  if (editForm.startDate) {
    const dateStr = editForm.startDate.includes('Z') ? editForm.startDate : editForm.startDate + 'Z'
    editForm.startDate = new Date(dateStr)
  }
  if (editForm.endDate) {
    const dateStr = editForm.endDate.includes('Z') ? editForm.endDate : editForm.endDate + 'Z'
    editForm.endDate = new Date(dateStr)
  }
  editDialogVisible.value = true
}
const resetEditForm = () => {
  editFormRef.value?.resetFields()
}
const submitEditForm = async () => {
  await editFormRef.value?.validate()
  editLoading.value = true
  try {
    const res = await updateCampaign(campaignId, editForm)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchCampaign()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败', error)
    ElMessage.error('网络错误')
  } finally {
    editLoading.value = false
  }
}
const handleDeleteCampaign = () => {
  ElMessageBox.confirm('确认删除该活动？若已有捐赠记录则无法删除', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteCampaign(campaignId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        router.push('/admin/campaigns')
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchCats()
  fetchCampaign()
})
</script>

<style scoped>
.campaign-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.admin-actions {
  text-align: right;
}
</style>