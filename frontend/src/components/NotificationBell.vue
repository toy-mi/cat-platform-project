<template>
  <el-popover
    placement="bottom-end"
    :width="350"
    trigger="click"
    v-model:visible="popoverVisible"
    popper-class="notification-popover"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-icon :size="24" style="cursor: pointer;"><Bell /></el-icon>
      </el-badge>
    </template>

    <div class="notification-header">
      <span>通知</span>
      <el-button type="primary" link @click="handleMarkAllRead" v-if="list.length > 0">全部已读</el-button>
    </div>
    <div class="notification-list" v-loading="loading">
      <div v-for="item in list" :key="item.id" class="notification-item" @click="handleItemClick(item)">
        <div class="item-title">
          <span>{{ item.title }}</span>
          <el-tag v-if="item.isRead === 0" size="small" type="danger">未读</el-tag>
        </div>
        <div class="item-content">{{ item.content }}</div>
        <div class="item-time">{{ formatTime(item.createTime) }}</div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无通知" />
      <div v-if="total > list.length" class="view-more">
        <el-button type="primary" link @click="goToNotificationPage">查看更多</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn } = storeToRefs(userStore)

const popoverVisible = ref(false)
const unreadCount = ref(0)
const list = ref([])
const total = ref(0)
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(5)

// 获取未读数量
const fetchUnreadCount = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data
    }
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

// 获取最新通知（前5条）
const fetchNotifications = async () => {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const res = await getNotifications({ pageNum: 1, pageSize: 5 })
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

// 标记为已读
const handleItemClick = async (item) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    unreadCount.value--
  }
  popoverVisible.value = false
  // 根据通知类型跳转
  if (item.type === 'ADOPTION_STATUS_CHANGE' && item.relatedId) {
    router.push(`/adoptions/${item.relatedId}`)
  } else if (item.type === 'POST_COMMENT' && item.relatedId) {
    router.push(`/community/post/${item.relatedId}`)
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  const res = await markAllAsRead()
  if (res.code === 200) {
    unreadCount.value = 0
    list.value.forEach(item => item.isRead = 1)
    ElMessage.success('全部已读')
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

// 跳转到通知列表页
const goToNotificationPage = () => {
  popoverVisible.value = false
  router.push('/notifications')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

// 当用户点击铃铛打开弹窗时，刷新数据
watch(popoverVisible, (val) => {
  if (val) {
    fetchUnreadCount()
    fetchNotifications()
  }
})

// 登录状态变化时刷新
watch(isLoggedIn, (val) => {
  if (val) {
    fetchUnreadCount()
    fetchNotifications()
  } else {
    unreadCount.value = 0
    list.value = []
  }
}, { immediate: true })

// 定时刷新未读数量（可选）
let timer
onMounted(() => {
  if (isLoggedIn.value) {
    timer = setInterval(fetchUnreadCount, 30000) // 30秒轮询
  }
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.notification-badge {
  margin-right: 20px;
  cursor: pointer;
}
.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
}
.notification-item {
  padding: 8px;
  border-bottom: 1px solid #eee;
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
  font-size: 12px;
  color: #666;
  margin: 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-time {
  font-size: 10px;
  color: #999;
}
.view-more {
  text-align: center;
  margin-top: 8px;
}
</style>