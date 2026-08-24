<template>
  <div class="post-card" @click="goToPostDetail">
    <!-- 用户信息区域：独立点击，不触发父级事件 -->
    <div class="user-info" @click.stop="goToUserPage">
      <el-avatar :size="32" :src="post.userAvatar || defaultAvatar" />
      <div class="post-user">
        <div class="user-name">{{ post.userName || '匿名' }}</div>
        <div class="post-time">{{ formatTime(post.createTime) }}</div>
      </div>
    </div>

    <div class="post-content">
      <div v-if="post.title" class="post-title">{{ post.title }}</div>
      <div class="post-text">{{ truncate(post.content, 80) }}</div>
    </div>
    <div class="post-stats">
      <span>❤️{{ post.likeCount || 0 }}</span>
      <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Star, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const props = defineProps(['post'])
const router = useRouter()
const userStore = useUserStore()
const { userInfo, userId: currentUserId } = storeToRefs(userStore)

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40"%3E%3Ccircle cx="20" cy="20" r="20" fill="%23ccc"/%3E%3Ctext x="20" y="28" text-anchor="middle" fill="%23999" font-size="14"%3E?%3C/text%3E%3C/svg%3E'

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()}`
}
const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 跳转动态详情
const goToPostDetail = () => {
  router.push(`/community/post/${props.post.id}`)
}

// 跳转用户页面（本人则跳个人中心）
const goToUserPage = () => {
  const currentUserId = userStore.userId || userInfo.value?.id
  const targetUserId =
    props.post.userId ||
    props.post.user?.id ||
    props.post.user?.userId ||
    props.post.creatorId ||
    props.post.authorId

  if (!targetUserId) {
    console.warn('用户ID缺失，无法跳转', props.post)
    return
  }

  if (currentUserId && String(currentUserId) === String(targetUserId)) {
    router.push('/profile')
  } else {
    router.push(`/user/${targetUserId}`)
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.post-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  cursor: pointer;
  padding: 12px;
}
.post-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 25px -12px rgba(0,0,0,0.15);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}
.user-info:hover .user-name {
  color: #409EFF;
}
.post-user {
  flex: 1;
}
.user-name {
  font-weight: bold;
  font-size: 14px;
  transition: color 0.2s;
}
.post-time {
  font-size: 12px;
  color: #999;
}
.post-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 4px;
}
.post-text {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}
.post-stats {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}
</style>