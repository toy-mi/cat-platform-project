<template>
  <div class="adoption-statistics">
    <h2>领养申请数据统计</h2>

    <el-row :gutter="20">
      <!-- 总申请数卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">总申请数</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
        </el-card>
      </el-col>

      <!-- 通过率卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">通过率</div>
          <div class="stat-value">{{ statistics.passRate || 0 }}%</div>
        </el-card>
      </el-col>

      <!-- 已通过申请数 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">已通过申请</div>
          <div class="stat-value">{{ passedCount }}</div>
        </el-card>
      </el-col>

      <!-- 待处理申请数 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">待处理</div>
          <div class="stat-value">{{ pendingCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 状态分布饼图 -->
      <el-col :span="12">
        <el-card>
          <div ref="statusChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 每月申请量柱状图 -->
      <el-col :span="12">
        <el-card>
          <div ref="monthlyChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getAdoptionStatistics } from '@/api/adoption'
import * as echarts from 'echarts'

const statistics = ref({})
const statusChart = ref(null)
const monthlyChart = ref(null)

// 计算通过数、待处理数
const passedCount = computed(() => {
  if (!statistics.value.statusCounts) return 0
  return statistics.value.statusCounts
    .filter(item => [7, 9, 10].includes(item.status))
    .reduce((sum, item) => sum + item.count, 0)
})

const pendingCount = computed(() => {
  if (!statistics.value.statusCounts) return 0
  return statistics.value.statusCounts
    .filter(item => [0, 1, 3, 6].includes(item.status))
    .reduce((sum, item) => sum + item.count, 0)
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getAdoptionStatistics()
    if (res.code === 200) {
      statistics.value = res.data
      initCharts()
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
    ElMessage.error('网络错误')
  }
}

// 初始化图表
const initCharts = () => {
  // 状态分布饼图
  if (statusChart.value && statistics.value.statusCounts) {
    const chart = echarts.init(statusChart.value)
    const statusMap = {
      0: '待初审', 1: '初审通过', 2: '初审拒绝', 3: '待回访',
      4: '回访通过', 5: '回访失败', 6: '待终审', 7: '终审通过',
      8: '终审拒绝', 9: '已签订协议', 10: '已完成领养', 11: '已取消'
    }
    const data = statistics.value.statusCounts.map(item => ({
      name: statusMap[item.status] || `状态${item.status}`,
      value: item.count
    }))
    chart.setOption({
      title: { text: '申请状态分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' }
        }
      }]
    })
  }

  // 每月申请量柱状图
  if (monthlyChart.value && statistics.value.monthlyApplications) {
    const chart = echarts.init(monthlyChart.value)
    const months = statistics.value.monthlyApplications.map(item => item.month)
    const counts = statistics.value.monthlyApplications.map(item => item.count)
    chart.setOption({
      title: { text: '近6个月申请量', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: counts,
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.adoption-statistics {
  padding: 20px;
}
.stat-card {
  text-align: center;
}
.stat-title {
  font-size: 14px;
  color: #999;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-top: 10px;
}
</style>