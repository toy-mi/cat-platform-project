<template>
  <div class="announcement-carousel">
    <h2 class="page-title">最新公告</h2>
    <p class="page-subtitle">关注我们的最新消息</p>

    <el-carousel :interval="5000" arrow="always" height="220px" trigger="hover" v-loading="loading">
      <el-carousel-item v-for="item in list" :key="item.id">
        <div class="carousel-card" @click="goToDetail(item.id)">
          <div class="card-content">
            <div class="title">
              <el-tag v-if="item.priority === 1" type="danger" size="small">重要</el-tag>
              <span>{{ item.title }}</span>
            </div>
            <div class="summary">{{ truncate(item.content, 100) }}</div>
            <div class="time">{{ formatTime(item.publishTime) }}</div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="more-link" v-if="total > list.length">
      <el-button type="primary" link @click="goToAll">查看更多公告 →</el-button>
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
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPublishedAnnouncements({ pageNum: 1, pageSize: 5 })
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取公告失败')
    }
  } catch (error) {
    console.error('获取公告失败', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/announcements/${id}`)
}

const goToAll = () => {
  router.push('/announcements/all')
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
.announcement-carousel {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px 0;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  text-align: center;
  color: #0f172a;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  text-align: center;
  color: #64748b;
  font-size: 1rem;
  margin-bottom: 2rem;
}

.el-carousel {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.carousel-card {
  background: white;
  height: 100%;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  padding: 0 2rem;
}

.carousel-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -12px rgba(0,0,0,0.15);
}

.card-content {
  width: 100%;
}

.title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2rem;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 0.75rem;
}

.title span {
  line-height: 1.3;
}

.summary {
  color: #475569;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 0.75rem;
}

.time {
  color: #94a3b8;
  font-size: 0.75rem;
}

.more-link {
  text-align: center;
  margin-top: 1.5rem;
}

.more-link .el-button {
  font-size: 0.9rem;
}
</style>