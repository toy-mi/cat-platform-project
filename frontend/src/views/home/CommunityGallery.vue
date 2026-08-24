<template>
  <div class="community-gallery">
    <div class="container">
      <h1 class="page-title">社区动态</h1>
      <p class="page-subtitle">分享猫咪趣事，交流养宠经验</p>

      <!-- 发布动态按钮（仅登录用户） -->
      <div class="publish-btn" v-if="isLoggedIn">
        <el-button type="primary" size="large" @click="openCreateDialog">发布动态</el-button>
      </div>

      <!-- 动态列表 -->
      <div v-loading="loading" class="posts-grid">
        <div v-for="post in posts" :key="post.id" class="post-card card">
          <div class="post-header">
            <el-avatar :size="40" :src="post.userAvatar || defaultAvatar" @click.stop="goToUserPage(post)" style="cursor:pointer" />
            <div class="post-user" @click.stop="goToUserPage(post)" style="cursor:pointer">
              <div class="user-name">{{ post.userName || '匿名' }}</div>
              <div class="post-time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
          <div class="post-content" @click="goToDetail(post.id)">
            <div v-if="post.title" class="post-title">{{ post.title }}</div>
            <div class="post-text">{{ truncate(post.content, 120) }}</div>
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
              <el-icon><Location /></el-icon> {{ post.locationDesc }}
            </div>
          </div>
          <div class="post-actions">
            <el-button
              :type="post.likedByCurrent ? 'primary' : 'default'"
              size="small"
              @click="handleLike(post)"
            >
              <!-- <el-icon><Star /></el-icon>  -->
                ❤️
              {{ post.likeCount || 0 }}
            </el-button>
            <el-button size="small" @click="goToDetail(post.id)">
              <el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}
            </el-button>
            <el-button
              v-if="post.userId === currentUserId || isAdmin"
              type="danger"
              size="small"
              @click="handleDeletePost(post.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchPosts"
        @current-change="fetchPosts"
        class="pagination"
      />
    </div>

    <!-- 发布动态对话框 -->
    <el-dialog v-model="createVisible" title="发布动态" width="600px" @close="resetCreateForm">
      <el-form ref="postFormRef" :model="postForm" :rules="postRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="标题不能超过100个字符" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="postForm.content" type="textarea" rows="4" placeholder="分享猫咪趣事..." maxlength="5000" show-word-limit />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            v-model:file-list="imageList"
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            list-type="picture-card"
            multiple
            :limit="9"
            :on-exceed="handleExceed"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="postForm.locationDesc" placeholder="地点描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPost" :loading="submitLoading">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Star, ChatDotRound, Plus } from '@element-plus/icons-vue'
import { getPostPage, createPost, likePost, deletePost } from '@/api/community'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn, userInfo, userId, isAdmin } = storeToRefs(userStore)

const currentUserId = computed(() => String(userId.value || userInfo.value?.id || ''))

// 列表数据
const posts = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(6)
const loading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 发布动态相关
const createVisible = ref(false)
const submitLoading = ref(false)
const postFormRef = ref()
const postForm = reactive({
  title: '',
  content: '',
  images: '',
  locationDesc: ''
})
const postRules = {
  title: [{ max: 100, message: '标题不能超过100个字符', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }, { max: 5000, message: '内容不能超过5000个字符', trigger: 'blur' }]
}
const imageList = ref([])
const uploadAction = ref('http://localhost:8080/api/community/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

// 获取动态列表
const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await getPostPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) {
      posts.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取动态失败')
    }
  } catch (error) {
    console.error('获取动态失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

// 截取文本
const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 跳转到用户主页（本人则跳个人中心）
const goToUserPage = (post) => {
  const targetUserId =
    post?.userId ||
    post?.user?.id ||
    post?.user?.userId ||
    post?.creatorId ||
    post?.authorId

  if (!targetUserId) {
    ElMessage.warning('该动态未绑定用户ID，无法查看用户主页')
    return
  }

  if (currentUserId.value && currentUserId.value === String(targetUserId)) {
    router.push('/profile')
  } else {
    router.push(`/user/${targetUserId}`)
  }
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/community/post/${id}`)
}

// 点赞/取消点赞
const handleLike = async (post) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await likePost(post.id)
    if (res.code === 200) {
      post.likedByCurrent = !post.likedByCurrent
      post.likeCount += post.likedByCurrent ? 1 : -1
      ElMessage.success(res.message)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞失败', error)
  }
}

// 删除动态
const handleDeletePost = async (id) => {
  ElMessageBox.confirm('确认删除该动态？删除后不可恢复', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deletePost(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchPosts()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

// 打开发布对话框
const openCreateDialog = () => {
  createVisible.value = true
}

// 重置表单
const resetCreateForm = () => {
  postFormRef.value?.resetFields()
  postForm.title = ''
  postForm.content = ''
  postForm.images = ''
  postForm.locationDesc = ''
  imageList.value = []
}

// 图片上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    file.url = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    const index = fileList.findIndex(f => f.uid === file.uid)
    if (index !== -1) fileList.splice(index, 1)
  }
}

const handleUploadError = (err, file, fileList) => {
  ElMessage.error('上传失败')
  const index = fileList.findIndex(f => f.uid === file.uid)
  if (index !== -1) fileList.splice(index, 1)
}

// 超过数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

// 发布动态
const submitPost = async () => {
  await postFormRef.value?.validate()
  const imageUrls = imageList.value
    .filter(item => item.url)
    .map(item => item.url)
    .join(',')
  postForm.images = imageUrls

  submitLoading.value = true
  try {
    const res = await createPost(postForm)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      createVisible.value = false
      fetchPosts()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败', error)
    ElMessage.error('网络错误')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.community-gallery {
  padding: 2rem 0;
  background: #f8fafc;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 2.2rem;
  font-weight: 700;
  text-align: center;
  color: #0f172a;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  text-align: center;
  color: #475569;
  font-size: 1rem;
  margin-bottom: 2rem;
}

.publish-btn {
  text-align: right;
  margin-bottom: 1.5rem;
}

.publish-btn .el-button {
  border-radius: 40px;
  padding: 10px 24px;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  transition: all 0.2s;
}
.publish-btn .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.8rem;
  margin-bottom: 2.5rem;
}

.post-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.post-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 25px -12px rgba(0,0,0,0.15);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px 8px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.post-user {
  flex: 1;
}

.user-name {
  font-weight: 600;
  font-size: 1rem;
  color: #0f172a;
  line-height: 1.4;
}

.post-time {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 2px;
}

.post-content {
  padding: 12px 20px 16px 20px;
  flex: 1;
}

.post-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 8px;
  line-height: 1.4;
}

.post-text {
  font-size: 0.9rem;
  color: #334155;
  line-height: 1.5;
  word-break: break-word;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.post-image {
  width: calc(50% - 6px);  /* 一行两张，减去gap的一半 */
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 12px;
  transition: transform 0.2s;
  cursor: pointer;
}
.post-image:hover {
  transform: scale(1.02);
}

.post-location {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: #3b82f6;
  background: #eff6ff;
  padding: 4px 10px;
  border-radius: 20px;
  width: fit-content;
}

.post-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px 16px;
  border-top: 1px solid #f1f5f9;
  background: #ffffff;
}

.post-actions .el-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 30px;
  transition: all 0.2s;
  font-size: 0.85rem;
  background: #f8fafc;
  border: none;
  color: #475569;
}
.post-actions .el-button:hover {
  background: #eef2ff;
  color: #3b82f6;
}
.post-actions .el-button--primary {
  background: #eff6ff;
  color: #3b82f6;
}
.post-actions .el-button--danger {
  background: #fef2f2;
  color: #ef4444;
}
.post-actions .el-button--danger:hover {
  background: #fee2e2;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

/* 响应式 */
@media (max-width: 768px) {
  .posts-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.2rem;
  }
  .post-header {
    padding: 12px 16px 6px;
  }
  .post-content {
    padding: 8px 16px 12px;
  }
  .post-actions {
    padding: 10px 16px 14px;
  }
  .post-image {
    width: calc(33.33% - 5px);
  }
  .page-title {
    font-size: 1.8rem;
  }
}
</style>