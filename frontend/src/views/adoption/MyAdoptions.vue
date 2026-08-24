<template>
  <div class="my-adoptions">
    <div class="container">
      <h2>我的领养申请</h2>

      <el-table :data="list" border v-loading="loading" stripe>
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="catName" label="猫咪" width="120" />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="firstReviewRemark" label="初审意见" show-overflow-tooltip />
        <el-table-column prop="finalReviewRemark" label="终审意见" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchList"
      />

      <el-empty v-if="!loading && list.length === 0" description="暂无领养申请" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdoptionPage } from '@/api/adoption'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { userId } = storeToRefs(userStore)

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const getStatusText = (status) => {
  const map = {
    0: '待初审', 1: '初审通过', 2: '初审拒绝', 3: '待回访',
    4: '回访通过', 5: '回访失败', 6: '待终审', 7: '终审通过',
    8: '终审拒绝', 9: '已签订协议', 10: '已完成领养', 11: '已取消'
  }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = {
    0: 'info', 1: 'warning', 2: 'danger', 3: 'info',
    4: 'success', 5: 'danger', 6: 'info', 7: 'success',
    8: 'danger', 9: 'success', 10: 'success', 11: 'info'
  }
  return map[status] || 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getAdoptionPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: userId.value   // 只查自己的申请
    })
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取申请列表失败')
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/adoptions/${id}`)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.my-adoptions {
  padding: 2rem 0;
  background: #f8fafc;
  min-height: 100vh;
}
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}
</style>