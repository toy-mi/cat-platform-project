<template>
  <div class="cat-statistics">
    <h2>猫咪数据统计</h2>

    <!-- 关键指标卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">猫咪总数</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">公猫</div>
          <div class="stat-value">{{ genderStats.male || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">母猫</div>
          <div class="stat-value">{{ genderStats.female || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">性别未知</div>
          <div class="stat-value">{{ genderStats.unknown || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <div ref="statusChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div ref="genderChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getCatStatistics } from '@/api/cat'

const statistics = ref({})
const genderStats = reactive({ male: 0, female: 0, unknown: 0 })
const statusChart = ref(null)
const genderChart = ref(null)

const fetchData = async () => {
  try {
    const res = await getCatStatistics()
    if (res.code === 200) {
      statistics.value = res.data
      // 更新性别统计
      if (res.data.genderStats) {
        res.data.genderStats.forEach(item => {
          if (item.gender === '公') genderStats.male = item.count
          else if (item.gender === '母') genderStats.female = item.count
          else genderStats.unknown = item.count
        })
      }
      initCharts()
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
    ElMessage.error('网络错误')
  }
}

const initCharts = () => {
  // 领养状态饼图
  if (statusChart.value && statistics.value.statusStats) {
    const chart = echarts.init(statusChart.value)
    const statusMap = {
      0: '在养',
      1: '待领养',
      2: '待审核',
      3: '已领养',
      4: '失踪',
      5: '去世'
    }
    const data = statistics.value.statusStats.map(item => ({
      name: statusMap[item.status] || `状态${item.status}`,
      value: item.count
    }))
    chart.setOption({
      title: { text: '领养状态分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '50%',
        data,
        emphasis: { itemStyle: { shadowBlur: 10 } }
      }]
    })
  }

  // 性别比例饼图
  if (genderChart.value && statistics.value.genderStats) {
    const chart = echarts.init(genderChart.value)
    const data = statistics.value.genderStats.map(item => ({
      name: item.gender,
      value: item.count
    }))
    chart.setOption({
      title: { text: '性别比例', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '50%',
        data,
        emphasis: { itemStyle: { shadowBlur: 10 } }
      }]
    })
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.cat-statistics {
  padding: 20px;
}
.stat-cards {
  margin-bottom: 20px;
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