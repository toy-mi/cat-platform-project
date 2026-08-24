<template>
  <div class="dashboard">
    <h1 class="dashboard-title">数据仪表盘</h1>

    <!-- 关键指标卡片行 -->
    <el-row :gutter="24" class="stats-row">
      <el-col :span="6" v-for="stat in statCards" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon">{{ stat.icon }}</div>
          <div class="stat-content">
            <div class="stat-title">{{ stat.title }}</div>
            <div class="stat-value">{{ stat.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 猫咪统计 -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>猫咪统计</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div ref="catStatusChart" style="height: 320px;"></div>
        </el-col>
        <el-col :span="12">
          <div ref="catGenderChart" style="height: 320px;"></div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 领养统计 -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>领养统计</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div ref="adoptionTrendChart" style="height: 320px;"></div>
        </el-col>
        <el-col :span="12">
          <div ref="adoptionStatusChart" style="height: 320px;"></div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 捐赠统计 -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>捐赠统计</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div ref="donationTrendChart" style="height: 320px;"></div>
        </el-col>
        <el-col :span="12">
          <div ref="donationTypeChart" style="height: 320px;"></div>
        </el-col>
      </el-row>
      <div style="margin-top: 20px;">
        <el-table :data="goodsStats" border size="small" v-loading="goodsLoading" class="goods-table">
          <el-table-column prop="goodsName" label="物资名称" min-width="150" />
          <el-table-column prop="totalQuantity" label="总数量" width="100" />
          <el-table-column prop="count" label="捐赠次数" width="100" />
          <el-table-column label="捐赠人" min-width="200">
            <template #default="{ row }">
              <el-tag
                v-for="donor in row.donors"
                :key="donor"
                size="small"
                class="donor-tag"
              >
                {{ donor }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!goodsLoading && goodsStats.length === 0" description="暂无物资捐赠记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getCatStatistics } from '@/api/cat'
import { getAdoptionStatistics } from '@/api/adoption'
import { getDonationStatistics, getGoodsStatistics } from '@/api/donation'

// 数据
const catStats = ref({})
const adoptionStats = ref({})
const donationStats = ref({})
const goodsStats = ref([])
const goodsLoading = ref(false)

// 图表 DOM 引用
const catStatusChart = ref(null)
const catGenderChart = ref(null)
const adoptionTrendChart = ref(null)
const adoptionStatusChart = ref(null)
const donationTrendChart = ref(null)
const donationTypeChart = ref(null)

// 多巴胺色系配色方案
const dopamineColors = [
  '#FF6B6B', // 珊瑚红
  '#FFD93D', // 芒果黄
  '#6BCB77', // 草绿
  '#4D96FF', // 天蓝
  '#FF9F4A', // 暖橙
  '#C084FC', // 淡紫
  '#FFB5A7', // 蜜桃粉
  '#66CCFF'  // 浅蓝
]

// 构建统计卡片数据
const statCards = computed(() => [
  { icon: '🐱', title: '猫咪总数', value: catStats.value.total || 0 },
  { icon: '📋', title: '领养申请总数', value: adoptionStats.value.total || 0 },
  { icon: '⏳', title: '待处理领养', value: adoptionStats.value.pending || 0 },
  { icon: '💰', title: '待审核捐赠', value: donationStats.value.pending || 0 }
])

// 获取猫咪统计
const fetchCatStats = async () => {
  try {
    const res = await getCatStatistics()
    if (res.code === 200) {
      catStats.value = res.data
      renderCatCharts(res.data)
    } else {
      console.warn('获取猫咪统计失败:', res.message)
    }
  } catch (error) {
    console.warn('获取猫咪统计异常:', error)
  }
}

// 获取领养统计
const fetchAdoptionStats = async () => {
  try {
    const res = await getAdoptionStatistics()
    if (res.code === 200) {
      adoptionStats.value = res.data
      const pending = res.data.statusCounts?.filter(s => [0,1,3,6].includes(s.status)).reduce((sum, s) => sum + s.count, 0) || 0
      adoptionStats.value.pending = pending
      renderAdoptionCharts(res.data)
    } else {
      console.warn('获取领养统计失败:', res.message)
    }
  } catch (error) {
    console.warn('获取领养统计异常:', error)
  }
}

// 获取捐赠统计
const fetchDonationStats = async () => {
  try {
    const res = await getDonationStatistics()
    if (res.code === 200) {
      donationStats.value = res.data
      const pending = res.data.typeRatio?.find(t => t.name === '资金')?.pending || 0
      donationStats.value.pending = pending
      renderDonationCharts(res.data)
    } else {
      console.warn('获取捐赠统计失败:', res.message)
    }
  } catch (error) {
    console.warn('获取捐赠统计异常:', error)
  }
}

// 获取物资统计
const fetchGoodsStats = async () => {
  goodsLoading.value = true
  try {
    const res = await getGoodsStatistics()
    if (res.code === 200) {
      goodsStats.value = res.data
    } else {
      console.warn('获取物资统计失败:', res.message)
    }
  } catch (error) {
    console.warn('获取物资统计异常:', error)
  } finally {
    goodsLoading.value = false
  }
}

// 猫咪图表渲染
const renderCatCharts = (data) => {
  if (catStatusChart.value && data.statusStats) {
    const chart = echarts.init(catStatusChart.value)
    const statusMap = { 0: '在养', 1: '待领养', 2: '待审核', 3: '已领养', 4: '失踪', 5: '去世' }
    const pieData = data.statusStats.map(item => ({
      name: statusMap[item.status] || `状态${item.status}`,
      value: item.count
    }))
    chart.setOption({
      title: { text: '领养状态分布', left: 'center', textStyle: { color: '#2c3e50', fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.95)', borderColor: '#e4e7ed' },
      series: [{ type: 'pie', radius: ['40%', '65%'], data: pieData, label: { show: true }, emphasis: { scale: true } }],
      color: dopamineColors.slice(0, pieData.length)
    })
  }
  if (catGenderChart.value && data.genderStats) {
    const chart = echarts.init(catGenderChart.value)
    const genderData = data.genderStats.map(item => ({ name: item.gender, value: item.count }))
    chart.setOption({
      title: { text: '性别比例', left: 'center', textStyle: { color: '#2c3e50', fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '65%'], data: genderData, label: { show: true } }],
      color: dopamineColors.slice(0, genderData.length)
    })
  }
}

// 领养图表渲染
const renderAdoptionCharts = (data) => {
  if (adoptionTrendChart.value && data.monthlyApplications?.length) {
    const chart = echarts.init(adoptionTrendChart.value)
    chart.setOption({
      title: { text: '每月申请趋势', left: 'center', textStyle: { color: '#2c3e50', fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: data.monthlyApplications.map(item => item.month), axisLine: { lineStyle: { color: '#9ca3af' } } },
      yAxis: { type: 'value', name: '申请数量', nameStyle: { color: '#6b7280' } },
      series: [{ type: 'line', data: data.monthlyApplications.map(item => item.count), smooth: true, areaStyle: { opacity: 0.3, color: dopamineColors[0] }, lineStyle: { color: dopamineColors[0], width: 2 }, symbol: 'circle', symbolSize: 6 }]
    })
  }
  if (adoptionStatusChart.value && data.statusCounts?.length) {
    const chart = echarts.init(adoptionStatusChart.value)
    const statusMap = {
      0: '待初审', 1: '初审通过', 2: '初审拒绝', 3: '待回访',
      4: '回访通过', 5: '回访失败', 6: '待终审', 7: '终审通过',
      8: '终审拒绝', 9: '已签订协议', 10: '已完成领养', 11: '已取消'
    }
    const pieData = data.statusCounts.map(item => ({ name: statusMap[item.status] || `状态${item.status}`, value: item.count }))
    chart.setOption({
      title: { text: '申请状态分布', left: 'center', textStyle: { color: '#2c3e50', fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '65%'], data: pieData }],
      color: dopamineColors.slice(0, pieData.length)
    })
  }
}

// 捐赠图表渲染
const renderDonationCharts = (data) => {
  if (donationTrendChart.value && data.monthlyTrend?.length) {
    const chart = echarts.init(donationTrendChart.value)
    chart.setOption({
      title: { text: '每月捐赠金额趋势', left: 'center', textStyle: { color: '#2c3e50', fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.monthlyTrend.map(item => item.month), axisLine: { lineStyle: { color: '#9ca3af' } } },
      yAxis: { type: 'value', name: '金额（元）', nameStyle: { color: '#6b7280' } },
      series: [{ type: 'line', data: data.monthlyTrend.map(item => item.amount), smooth: true, areaStyle: { opacity: 0.3, color: dopamineColors[2] }, lineStyle: { color: dopamineColors[2], width: 2 } }]
    })
  }
  if (donationTypeChart.value && data.typeRatio?.length) {
    const chart = echarts.init(donationTypeChart.value)
    chart.setOption({
      title: { text: '捐赠类型占比（次数）', left: 'center', textStyle: { color: '#2c3e50', fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '65%'], data: data.typeRatio, label: { show: true } }],
      color: dopamineColors.slice(0, data.typeRatio.length)
    })
  }
}

onMounted(() => {
  fetchCatStats()
  fetchAdoptionStats()
  fetchDonationStats()
  fetchGoodsStats()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}
.dashboard-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1.5rem;
}
.stats-row {
  margin-bottom: 24px;
}
.stat-card {
  background: white;
  border-radius: 16px;
  border: none;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;   /* 改为纵向排列 */
  align-items: center;      /* 水平居中 */
  text-align: center;       /* 文字居中 */
  padding: 20px;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}
.stat-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;      /* 图标与文字间距 */
  margin-right: 0;          /* 移除原右侧边距 */
}
.stat-content {
  width: 100%;              /* 占满宽度，保证内部文字居中 */
}
.stat-title {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}
.chart-card {
  margin-bottom: 24px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  overflow: hidden;
}
.card-header {
  font-weight: 600;
  color: #1f2937;
  border-left: 3px solid #FF6B6B;
  padding-left: 12px;
}
.goods-table {
  border-radius: 12px;
  overflow: hidden;
}
.donor-tag {
  margin-right: 6px;
  margin-bottom: 4px;
  background-color: #f3f4f6;
  border: none;
  color: #374151;
}
</style>