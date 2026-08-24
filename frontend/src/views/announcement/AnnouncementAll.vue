<template>
  <div class="announcement-list">
    <h2>全部公告</h2>

    <div v-loading="loading">
      <div v-for="item in list" :key="item.id" class="announcement-card">
        <el-card @click="goToDetail(item.id)">
          <div class="card-header">
            <div class="title">
              <el-tag v-if="item.priority === 1" type="danger" size="small">重要</el-tag>
              <span>{{ item.title }}</span>
            </div>
            <div class="time">{{ formatTime(item.publishTime) }}</div>
          </div>
          <div class="summary">{{ truncate(item.content, 100) }}</div>
        </el-card>
      </div>

      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublishedAnnouncements } from '@/api/announcement'

const router = useRouter()
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPublishedAnnouncements({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取公告失败')
    }
  } catch (error) {
    console.error('获取公告失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/announcements/${id}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()}`
}

const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.announcement-list {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.announcement-card {
  margin-bottom: 15px;
  cursor: pointer;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}
.time {
  color: #999;
  font-size: 12px;
}
.summary {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}
</style>