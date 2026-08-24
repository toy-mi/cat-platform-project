<template>
  <div class="notification-page">
    <h2>我的通知</h2>
    <div v-loading="loading">
      <div v-for="item in list" :key="item.id" class="notification-item" @click="handleItemClick(item)">
        <div class="item-title">
          <span>{{ item.title }}</span>
          <el-tag v-if="item.isRead === 0" size="small" type="danger">未读</el-tag>
        </div>
        <div class="item-content">{{ item.content }}</div>
        <div class="item-time">{{ formatTime(item.createTime) }}</div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无通知" />
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNotifications, markAsRead } from '@/api/notification'

const router = useRouter()
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNotifications({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取通知失败')
    }
  } catch (error) {
    console.error('获取通知失败', error)
  } finally {
    loading.value = false
  }
}

const handleItemClick = async (item) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    item.isRead = 1
  }
  // 跳转逻辑（同铃铛组件）
  if (item.type === 'ADOPTION_STATUS_CHANGE' && item.relatedId) {
    router.push(`/adoptions/${item.relatedId}`)
  } else if (item.type === 'POST_COMMENT' && item.relatedId) {
    router.push(`/community/post/${item.relatedId}`)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.notification-page {
  padding: 20px;
}
.notification-item {
  padding: 12px;
  border: 1px solid #eee;
  margin-bottom: 10px;
  border-radius: 4px;
  cursor: pointer;
}
.notification-item:hover {
  background-color: #f5f7fa;
}
.item-title {
  display: flex;
  justify-content: space-between;
  font-weight: 500;
}
.item-content {
  color: #666;
  margin: 8px 0;
}
.item-time {
  font-size: 12px;
  color: #999;
}
</style>