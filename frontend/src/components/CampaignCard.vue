<template>
  <div class="campaign-card card" @click="$router.push(`/donations/${campaign.id}`)">
    <div class="campaign-header">
      <h3>{{ campaign.title }}</h3>
      <el-tag :type="getStatusTagType(campaign.status)" size="small">{{ getStatusText(campaign.status) }}</el-tag>
    </div>
    <p class="campaign-desc">{{ truncate(campaign.description, 80) }}</p>
    <div class="campaign-progress">
      <el-progress :percentage="getCampaignProgress(campaign)" :show-text="false" />
      <span class="progress-text">{{ getCampaignProgress(campaign).toFixed(1) }}%</span>
    </div>
    <div class="campaign-amount">
      <span>目标: ￥{{ campaign.targetAmount }}</span>
      <span>已筹: ￥{{ campaign.currentAmount || 0 }}</span>
    </div>
    <div class="campaign-time">
      <el-icon><Calendar /></el-icon>
      {{ formatDate(campaign.startDate) }} - {{ formatDate(campaign.endDate) }}
    </div>
  </div>
</template>

<script setup>
import { Calendar } from '@element-plus/icons-vue'

const props = defineProps(['campaign'])

const getStatusText = (status) => {
  const map = { 0: '筹备中', 1: '进行中', 2: '已结束', 3: '已取消', 4: '已完成' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'success' }
  return map[status] || 'info'
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

const truncate = (text, len) => {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}
</script>

<style scoped>
.campaign-card {
  cursor: pointer;
  padding: 16px;
}
.campaign-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.campaign-header h3 {
  margin: 0;
  font-size: 16px;
}
.campaign-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.4;
}
.campaign-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.campaign-progress .el-progress {
  flex: 1;
}
.progress-text {
  font-size: 12px;
  color: #409eff;
}
.campaign-amount {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.campaign-time {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>