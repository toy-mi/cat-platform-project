<template>
  <div class="donation-statistics">
    <h2>捐赠统计</h2>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <div ref="trendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div ref="ratioChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 物资统计表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>物资捐赠统计</span>
      </template>
      <el-table :data="goodsStats" border stripe v-loading="goodsLoading">
        <el-table-column prop="goodsName" label="物资名称" min-width="150"></el-table-column>
        <el-table-column prop="totalQuantity" label="总数量" width="100"></el-table-column>
        <el-table-column prop="count" label="捐赠次数" width="100"></el-table-column>
        <el-table-column label="捐赠人" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="donor in row.donors" :key="donor" size="small" style="margin-right: 5px;">
              {{ donor }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!goodsLoading && goodsStats.length === 0" description="暂无物资捐赠记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getDonationStatistics, getGoodsStatistics } from '@/api/donation'

const trendChart = ref(null)
const ratioChart = ref(null)
const goodsStats = ref([])
const goodsLoading = ref(false)

const fetchData = async () => {
  const res = await getDonationStatistics()
  if (res.code === 200) {
    const data = res.data
    // 每月趋势折线图
    const trend = echarts.init(trendChart.value)
    trend.setOption({
      title: { text: '每月捐赠金额趋势' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.monthlyTrend.map(item => item.month) },
      yAxis: { type: 'value', name: '金额（元）' },
      series: [{ type: 'line', data: data.monthlyTrend.map(item => item.amount), smooth: true }]
    })
    // 资金/物资占比饼图（按捐赠次数）
    const ratio = echarts.init(ratioChart.value)
    ratio.setOption({
      title: { text: '捐赠类型占比（次数）' },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: '50%', data: data.typeRatio }]
    })
  }
}

const fetchGoodsStats = async () => {
  goodsLoading.value = true
  try {
    const res = await getGoodsStatistics()
    if (res.code === 200) {
      goodsStats.value = res.data
    } else {
      ElMessage.error(res.message || '获取物资统计失败')
    }
  } catch (error) {
    console.error('获取物资统计失败', error)
  } finally {
    goodsLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchGoodsStats()
})
</script>