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
      <el-progress :percentage="getCampaignProgress(campaign)" :format="formatProgress"></el-progress>
      <div class="action" v-if="campaign.status === 1 && isLoggedIn">
        <el-button type="primary" @click="goToDonate">我要捐赠</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCampaignDetail } from '@/api/donation'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn } = storeToRefs(userStore)

const id = route.params.id
const campaign = ref(null)
const loading = ref(false)

const getStatusText = (status) => {
  const map = { 0: '筹备中', 1: '进行中', 2: '已结束', 3: '已取消', 4: '已完成' }
  return map[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()}`
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

const fetchCampaign = async () => {
  loading.value = true
  try {
    const res = await getCampaignDetail(id)
    if (res.code === 200) {
      campaign.value = res.data
    } else {
      ElMessage.error(res.message || '获取活动详情失败')
    }
  } catch (error) {
    console.error('获取活动详情失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/donations')
}

const goToDonate = () => {
  // 跳转到捐赠页面或弹出捐赠弹窗
  router.push(`/donations/${id}/donate`)
}

onMounted(() => {
  fetchCampaign()
})
</script>

<style scoped>
.campaign-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.action {
  margin-top: 20px;
  text-align: right;
}
</style>