<template>
  <div class="campaign-gallery">
    <div class="container">
      <h1 class="page-title">募捐活动</h1>
      <p class="page-subtitle">支持我们的救助行动，为流浪猫提供帮助</p>

      <!-- 筛选栏 -->
      <div class="filters">
        <el-select v-model="filters.status" placeholder="活动状态" clearable style="width:150px">
          <el-option label="进行中" :value="1" />
          <el-option label="已结束" :value="2" />
          <el-option label="已完成" :value="4" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          placeholder="搜索活动标题"
          clearable
          style="width: 200px; margin-left: 10px;"
        />
        <el-button type="primary" @click="handleSearch" style="margin-left: 10px;">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <!-- 活动卡片列表 -->
      <div v-loading="loading" class="campaigns-grid">
        <el-card
          v-for="campaign in list"
          :key="campaign.id"
          class="campaign-card"
          @click="goToDetail(campaign.id)"
        >
          <div class="campaign-header">
            <span class="campaign-title">{{ campaign.title }}</span>
            <el-tag :type="getStatusTagType(campaign.status)">
              {{ getStatusText(campaign.status) }}
            </el-tag>
          </div>
          <div class="campaign-cat" v-if="campaign.catName">
            🐱 关联猫咪：{{ campaign.catName }}
          </div>
          <div class="campaign-target">目标金额：￥{{ campaign.targetAmount }}</div>
          <div class="campaign-current">已筹金额：￥{{ campaign.currentAmount || 0 }}</div>
          <div class="campaign-progress">
            <el-progress :percentage="getCampaignProgress(campaign)" :format="formatProgress" />
            <!-- <span class="progress-text">{{ getCampaignProgress(campaign).toFixed(1) }}%</span> -->
          </div>
          <div class="campaign-time">
            {{ formatDate(campaign.startDate) }} 至 {{ formatDate(campaign.endDate) }}
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchCampaigns"
        @current-change="fetchCampaigns"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCampaignPage } from '@/api/donation'

const router = useRouter()
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(6)
const loading = ref(false)

const filters = reactive({
  status: null,
  keyword: ''
})

const fetchCampaigns = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: filters.status,
      keyword: filters.keyword || undefined
    }
    const res = await getCampaignPage(params)
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
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

const handleSearch = () => {
  pageNum.value = 1
  fetchCampaigns()
}
const resetFilters = () => {
  filters.status = null
  filters.keyword = ''
  handleSearch()
}

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
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()}`
}
const goToDetail = (id) => {
  router.push(`/donations/${id}`)
}

onMounted(() => {
  fetchCampaigns()
})
</script>

<style scoped>
.campaign-gallery {
  padding: 2rem 0;
  background: #f8fafc;
  min-height: 100vh;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.page-title {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 0.5rem;
}
.page-subtitle {
  text-align: center;
  color: #64748b;
  margin-bottom: 2rem;
}
.filters {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}
.campaigns-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}
.campaign-card {
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 16px;
}
.campaign-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}
.campaign-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.campaign-title {
  font-size: 1.2rem;
  font-weight: 600;
}
.campaign-cat {
  font-size: 0.9rem;
  color: #409EFF;
  margin-bottom: 5px;
}
.campaign-target, .campaign-current {
  font-size: 0.9rem;
  margin-bottom: 5px;
}
.campaign-progress {
  margin: 10px 0;
}
.campaign-time {
  font-size: 0.8rem;
  color: #999;
}
.pagination {
  display: flex;
  justify-content: center;
}
</style>