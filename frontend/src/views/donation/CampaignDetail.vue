<template>
  <div class="campaign-detail" v-loading="loading">
    <el-page-header @back="goBack" :content="campaign?.title" />

    <!-- 活动信息卡片 -->
    <el-card v-if="campaign" class="campaign-info">
      <div class="info-header">
        <h2>{{ campaign.title }}</h2>
        <el-tag :type="getStatusTagType(campaign.status)">{{ getStatusText(campaign.status) }}</el-tag>
      </div>
      <p class="description">{{ campaign.description }}</p>
      <div v-if="campaign.catName" class="cat-info">🐱 关联猫咪：{{ campaign.catName }}</div>
      <el-row :gutter="20" class="stats">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">目标金额</div>
            <div class="stat-value">￥{{ campaign.targetAmount }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">已筹金额</div>
            <div class="stat-value">￥{{ campaign.currentAmount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">进度</div>
            <div class="stat-value">{{ getCampaignProgress(campaign).toFixed(1) }}%</div>
          </div>
        </el-col>
      </el-row>
      <el-progress :percentage="getCampaignProgress(campaign)" :format="formatProgress"></el-progress>
      <div class="time-range">
        时间：{{ formatDateTime(campaign.startDate) }} 至 {{ formatDateTime(campaign.endDate) }}
      </div>

      <!-- 管理员操作按钮 -->
      <div class="admin-actions" v-if="isAdmin">
        <el-button type="primary" @click="openEditDialog">编辑活动</el-button>
        <el-button type="danger" @click="handleDeleteCampaign">删除活动</el-button>
      </div>

      <!-- 捐赠按钮（仅登录用户） -->
      <div class="donate-actions" v-if="isLoggedIn && campaign.status === 1">
        <el-button type="success" size="large" @click="openDonateDialog">我要捐赠</el-button>
      </div>
    </el-card>

    <!-- 捐赠记录 -->
    <el-card class="donation-section">
      <template #header>
        <span>捐赠公示</span>
      </template>

      <el-tabs v-model="activeDonationTab">
        <el-tab-pane label="全部" name="all">
          <donation-list ref="donationListRef" :campaign-id="campaignId" :show-audit="isAdmin" @refresh="fetchCampaign" />
        </el-tab-pane>
        <el-tab-pane label="待审核" name="pending" v-if="isAdmin">
          <donation-list ref="pendingDonationListRef" v-if="isAdmin":campaign-id="campaignId" :status="0" :show-audit="true" @refresh="fetchCampaign" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑活动对话框 -->
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

    <!-- 捐赠对话框（新增上传组件） -->
    <el-dialog v-model="donateVisible" title="我要捐赠" width="550px" @close="resetDonateForm">
      <el-form ref="donateFormRef" :model="donateForm" :rules="donateRules" label-width="100px">
        <el-form-item label="捐赠类型" prop="donationType">
          <el-radio-group v-model="donateForm.donationType">
            <el-radio label="MONEY">资金</el-radio>
            <el-radio label="GOODS">物资</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 资金捐赠显示收款码 -->
        <div v-if="donateForm.donationType === 'MONEY'" class="qr-code-section">
          <div class="qr-code-title">请使用微信/支付宝扫码支付</div>
          <div style="display: flex; justify-content: center; gap: 20px;">
            <div>
              <div style="text-align: center; font-size: 14px;">微信</div>
              <el-image
                src="/images/payment-qrcode.jpg"
                alt="微信收款码"
                style="width: 200px; height: 200px; display: block; margin: 10px auto;"
                fit="contain"
              />
            </div>
            <div>
              <div style="text-align: center; font-size: 14px;">支付宝</div>
              <el-image
                src="/images/payment-qrcode2.jpg"
                alt="支付宝收款码"
                style="width: 200px; height: 200px; display: block; margin: 10px auto;"
                fit="contain"
              />
            </div>
          </div>
          <div class="qr-code-note">支付后请在下方填写金额并提交，我们将尽快审核</div>
        </div>

        <!-- 资金捐赠：金额和凭证上传 -->
        <el-form-item label="金额" prop="amount" v-if="donateForm.donationType === 'MONEY'">
          <el-input-number v-model="donateForm.amount" :precision="2" :min="0.01" style="width:100%"></el-input-number>
        </el-form-item>

        <!-- 新增：支付凭证上传 -->
        <el-form-item label="支付凭证" v-if="donateForm.donationType === 'MONEY'">
          <el-upload
            class="upload-demo"
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :limit="1"
            :file-list="fileList"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png 图片，大小不超过 5MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 物资捐赠 -->
        <template v-if="donateForm.donationType === 'GOODS'">
          <el-form-item label="物资名称" prop="goodsName">
            <el-input v-model="donateForm.goodsName" placeholder="例如：猫粮"></el-input>
          </el-form-item>
          <el-form-item label="数量" prop="goodsQuantity">
            <el-input-number v-model="donateForm.goodsQuantity" :min="1" style="width:100%"></el-input-number>
          </el-form-item>
          <el-form-item label="单位" prop="goodsUnit">
            <el-input v-model="donateForm.goodsUnit" placeholder="例如：袋、个"></el-input>
          </el-form-item>
        </template>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="donateForm.remark" placeholder="例如：捐赠方式（网购、送货上门）、捐赠时间、联系电话" type="textarea" rows="2"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="donateVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDonate" :loading="donateLoading">提交捐赠</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCampaignDetail, updateCampaign, deleteCampaign, donate } from '@/api/donation'
import { getCatPage } from '@/api/cat'
import DonationList from '@/components/DonationList.vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isAdmin, isLoggedIn } = storeToRefs(userStore)

const campaignId = route.params.id
const loading = ref(false)
const campaign = ref(null)
const catList = ref([])
const donationListRef = ref(null)
const pendingDonationListRef = ref(null)

// 获取活动详情
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

// 获取猫咪列表（用于编辑表单）
const fetchCats = async () => {
  const res = await getCatPage({ pageNum: 1, pageSize: 100 })
  if (res.code === 200) {
    catList.value = res.data.records
  }
}

// 状态映射
const getStatusText = (status) => {
  const map = { 0: '筹备中', 1: '进行中', 2: '已结束', 3: '已取消', 4: '已完成' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'success' }
  return map[status] || 'info'
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
const formatProgress = (percentage) => {
  return percentage === 0 ? '0%' : percentage.toFixed(1) + '%'
}
const formatDateTime = (date) => {
  if (!date) return ''
  const dateStr = date.includes('Z') ? date : date + 'Z'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()} ${d.getHours()}:${d.getMinutes()}`
}
const goBack = () => {
  router.push('/donations')
}

// 编辑活动
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: null,
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

// 删除活动
const handleDeleteCampaign = () => {
  ElMessageBox.confirm('确认删除该活动？若已有捐赠记录则无法删除', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteCampaign(campaignId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        router.push('/donations')
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

// 捐赠相关（新增上传组件）
const donateVisible = ref(false)
const donateLoading = ref(false)
const donateFormRef = ref()
const donateForm = reactive({
  donationType: 'MONEY',
  amount: null,
  goodsName: '',
  goodsQuantity: null,
  goodsUnit: '',
  remark: '',
  attachmentUrl: ''   // 新增：凭证URL
})
const donateRules = {
  donationType: [{ required: true, message: '请选择捐赠类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  goodsName: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  goodsQuantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

// 上传组件相关
const uploadAction = ref('http://localhost:8080/api/donations/upload')
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + localStorage.getItem('token')
}))
const fileList = ref([])  // 用于展示已上传文件列表

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    donateForm.attachmentUrl = response.data
    // 手动更新 fileList 显示（可选）
    fileList.value = [{ name: file.name, url: response.data }]
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

const openDonateDialog = () => {
  donateVisible.value = true
}

// 重置捐赠表单（清空上传相关）
const resetDonateForm = () => {
  donateFormRef.value?.resetFields()
  donateForm.donationType = 'MONEY'
  donateForm.amount = null
  donateForm.goodsName = ''
  donateForm.goodsQuantity = null
  donateForm.goodsUnit = ''
  donateForm.remark = ''
  donateForm.attachmentUrl = ''  // 清空凭证URL
  fileList.value = []            // 清空上传列表
}

// 提交捐赠
const submitDonate = async () => {
  await donateFormRef.value?.validate()
  const payload = {
    campaignId,
    donationType: donateForm.donationType,
    amount: donateForm.donationType === 'MONEY' ? donateForm.amount : undefined,
    goodsName: donateForm.donationType === 'GOODS' ? donateForm.goodsName : undefined,
    goodsQuantity: donateForm.donationType === 'GOODS' ? donateForm.goodsQuantity : undefined,
    goodsUnit: donateForm.goodsUnit,
    remark: donateForm.remark,
    attachmentUrl: donateForm.attachmentUrl   // 新增字段
  }
  donateLoading.value = true
  try {
    const res = await donate(payload)
    if (res.code === 200) {
      ElMessage.success('捐赠成功，等待管理员审核')
      donateVisible.value = false
      fetchCampaign()
      donationListRef.value?.fetchData()
      if (isAdmin.value) {
        pendingDonationListRef.value?.fetchData()
      }
    } else {
      ElMessage.error(res.message || '捐赠失败')
    }
  } catch (error) {
    console.error('捐赠失败', error)
    ElMessage.error('网络错误')
  } finally {
    donateLoading.value = false
  }
}

const handleListRefresh = () => {
  fetchCampaign()
  donationListRef.value?.fetchData()
  if (isAdmin.value) {
    pendingDonationListRef.value?.fetchData()
  }
}

const activeDonationTab = ref('all')

onMounted(() => {
  fetchCats()
  fetchCampaign()
})
</script>

<style scoped>
.campaign-detail {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
.campaign-info {
  margin-bottom: 20px;
}
.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.description {
  color: #666;
  margin-bottom: 15px;
}
.cat-info {
  color: #409EFF;
  margin-bottom: 15px;
}
.stats {
  margin: 15px 0;
}
.stat-item {
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #999;
}
.stat-value {
  font-size: 20px;
  font-weight: bold;
}
.time-range {
  margin-top: 10px;
  color: #999;
  font-size: 14px;
}
.admin-actions, .donate-actions {
  margin-top: 20px;
  text-align: right;
}
.donate-actions {
  text-align: center;
}
.qr-code-section {
  text-align: center;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}
.qr-code-title {
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}
.qr-code-note {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>