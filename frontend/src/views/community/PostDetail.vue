<template>
  <div class="post-detail" v-loading="loading">
    <el-page-header @back="goBack" content="动态详情" />

    <!-- 动态内容 -->
    <el-card v-if="post" class="post-card">
      <div class="post-header">
        <el-avatar
          :size="40"
          :src="post.userAvatar || defaultAvatar"
          @click.stop="goToUserPage(post)"
          style="cursor: pointer"
        />
        <div class="post-user" @click.stop="goToUserPage(post)" style="cursor: pointer">
          <div class="user-name">{{ post.userName || '匿名' }}</div>
          <div class="post-time">{{ formatTime(post.createTime) }}</div>
        </div>
      </div>

      <div class="post-content">
        <div v-if="post.title" class="post-title">{{ post.title }}</div>
        <div class="post-text">{{ post.content }}</div>
        <div v-if="post.images" class="post-images">
          <el-image
            v-for="(img, idx) in post.images.split(',')"
            :key="idx"
            :src="img"
            :preview-src-list="post.images.split(',')"
            fit="cover"
            class="post-image"
          />
        </div>
        <div v-if="post.locationDesc" class="post-location">
          <el-icon><Location /></el-icon>
          {{ post.locationDesc }}
        </div>
      </div>

      <div class="post-actions">
        <el-button
          :type="post.likedByCurrent ? 'primary' : 'default'"
          size="small"
          @click="handleLike"
          :disabled="!isLoggedIn"
        >
          <!-- <el-icon><Star /></el-icon> -->
           ❤️
          {{ post.likeCount || 0 }}
        </el-button>
        <el-button
          v-if="canDeletePost"
          type="danger"
          size="small"
          @click="handleDeletePost"
        >
          删除动态
        </el-button>
      </div>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="comment-section" v-if="post">
      <template #header>
        <span>评论 ({{ comments.length }})</span>
      </template>

      <div v-loading="commentLoading">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <!-- 头像可点击 -->
          <el-avatar
            :size="32"
            :src="comment.userAvatar || defaultAvatar"
            @click.stop="goToUserPage(comment.userId)"
            style="cursor: pointer"
          />
          <div class="comment-content">
            <!-- 用户名可点击 -->
            <div class="comment-user" @click.stop="goToUserPage(comment.userId)" style="cursor: pointer">
              {{ comment.userName }}
            </div>
            <!-- 评论内容不可点击 -->
            <div class="comment-text">{{ comment.content }}</div>
            <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
          </div>
          <el-button
            v-if="canDeleteComment(comment)"
            type="danger"
            size="small"
            link
            @click.stop="handleDeleteComment(comment.id)"
          >
            删除
          </el-button>
        </div>

        <!-- 发表评论（仅登录用户） -->
        <div class="add-comment" v-if="isLoggedIn">
          <el-input
            v-model="newComment"
            placeholder="写评论..."
            @keyup.enter="submitComment"
          />
          <el-button type="primary" @click="submitComment" :loading="submitting">发表</el-button>
        </div>
        <el-empty v-else-if="comments.length === 0" description="暂无评论，登录后发表" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Star } from '@element-plus/icons-vue'
import { getPostDetail, likePost, addComment, deleteComment, deletePost } from '@/api/community'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn, userInfo, isAdmin } = storeToRefs(userStore)

const postId = route.params.id
const loading = ref(false)
const post = ref(null)
const comments = ref([])
const commentLoading = ref(false)
const newComment = ref('')
const submitting = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取详情
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(postId)
    if (res.code === 200) {
      const postData = res.data.post || {}
      post.value = {
        ...postData,
        userAvatar:
          normalizeAvatarUrl(postData.userAvatar || postData.avatar || (postData.user && postData.user.avatar)) ||
          defaultAvatar,
        userName:
          postData.userName ||
          postData.username ||
          (postData.user && (postData.user.nickname || postData.user.username)) ||
          '匿名'
      }
      comments.value = (res.data.comments || []).map(c => {
        return {
          ...c,
          userAvatar:
            normalizeAvatarUrl(c.userAvatar || c.avatar || (c.user && c.user.avatar)) ||
            defaultAvatar,
          userName:
            c.userName ||
            c.username ||
            (c.user && (c.user.nickname || c.user.username)) ||
            '匿名'
        }
      })
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 点赞/取消点赞
const handleLike = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await likePost(postId)
    if (res.code === 200) {
      post.value.likedByCurrent = !post.value.likedByCurrent
      post.value.likeCount += post.value.likedByCurrent ? 1 : -1
      ElMessage.success(res.message)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞失败', error)
  }
}

// 发表评论
const submitComment = async () => {
  if (!newComment.value.trim()) return
  submitting.value = true
  try {
    const res = await addComment({
      postId,
      content: newComment.value
    })
    if (res.code === 200) {
      newComment.value = ''
      ElMessage.success('评论成功')
      await fetchDetail()
    } else {
      ElMessage.error(res.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败', error)
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  ElMessageBox.confirm('确认删除该评论？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteComment(commentId)
      if (res.code === 200) {
        comments.value = comments.value.filter(c => c.id !== commentId)
        ElMessage.success('删除成功')
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

// 删除动态
const handleDeletePost = () => {
  ElMessageBox.confirm('确认删除该动态？删除后不可恢复', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deletePost(postId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        router.push('/community')
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

// 跳转到用户主页（本人则跳个人中心）
const goToUserPage = (target) => {
  let targetUserId = null
  if (typeof target === 'object' && target !== null) {
    targetUserId = target.userId || target.user?.id || target.user?.userId || target.creatorId || target.authorId
  } else if (typeof target === 'number' || typeof target === 'string') {
    targetUserId = target
  }

  if (!targetUserId) {
    ElMessage.warning('无法获取用户ID，无法跳转')
    return
  }

  const currentUserId = userInfo.value?.id
  if (currentUserId && currentUserId === Number(targetUserId)) {
    router.push('/profile')
  } else {
    router.push(`/user/${targetUserId}`)
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 头像值清洗
const normalizeAvatarValue = (value) => {
  if (!value || value === 'null' || value === 'undefined') return ''
  return value
}

// 头像 URL 归一化
const normalizeAvatarUrl = (input) => {
  const url = normalizeAvatarValue(input)
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  if (url.startsWith('//')) return window.location.protocol + url
  return `http://localhost:8080${url.startsWith('/') ? '' : '/'}${url}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

// 权限判断
const canDeletePost = computed(() => {
  return isAdmin.value || (userInfo.value && post.value && post.value.userId === userInfo.value.id)
})

const canDeleteComment = (comment) => {
  return isAdmin.value || (userInfo.value && comment.userId === userInfo.value.id)
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.post-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 95%;
}

.post-card {
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.post-user {
  margin-left: 10px;
}

.user-name {
  font-weight: bold;
  font-size: 18px;
}

.post-time {
  font-size: 14px;
  color: #999;
}

.post-content {
  margin: 20px 0;
}

.post-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 15px;
}

.post-text {
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 20px;
}

.post-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.post-location {
  margin-top: 20px;
  color: #409EFF;
  font-size: 16px;
}

.post-actions {
  margin-top: 20px;
  display: flex;
  gap: 15px;
}

.comment-section {
  margin-top: 30px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
  cursor: pointer;
  display: inline-block;
}
.comment-user:hover {
  color: #409EFF;
}

.comment-text {
  font-size: 15px;
  color: #333;
  margin-bottom: 4px;
}

.comment-time {
  font-size: 13px;
  color: #999;
}

.add-comment {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.add-comment .el-input {
  flex: 1;
}

.add-comment .el-button {
  width: 80px;
}
</style>