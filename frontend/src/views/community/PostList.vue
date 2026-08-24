<template>
  <div class="post-list">
    <h2>社区动态</h2>

    <!-- 搜索栏 -->
    <el-row :gutter="10" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-input 
          v-model="searchKeyword" 
          placeholder="输入标题或内容关键词" 
          clearable 
          @keyup.enter="handleSearch"
        />
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-col>
    </el-row>

    <!-- 发布动态按钮 -->
    <div style="margin-bottom: 20px; text-align: right;">
      <el-button type="primary" @click="openCreateDialog" v-if="isLoggedIn">发布动态</el-button>
    </div>

    <!-- 动态列表 -->
    <div v-loading="loading">
      <div v-for="post in posts" :key="post.id" class="post-card">
        <el-card>
          <div class="post-header">
            <el-avatar :size="40" :src="post.userAvatar || defaultAvatar" @click.stop="goToUserPage(post)" style="cursor:pointer" />
            <div class="post-user" @click.stop="goToUserPage(post)" style="cursor:pointer">
              <div class="user-name">{{ post.userName || '匿名' }}</div>
              <div class="post-time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
          <div class="post-content">
            <div class="post-title" v-if="post.title">{{ post.title }}</div>
            <div class="post-text">{{ post.content }}</div>
            <!-- 图片展示（简单处理，用逗号分隔） -->
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
            <!-- 位置信息 -->
            <div v-if="post.locationDesc" class="post-location">
              <el-icon><Location /></el-icon>
              {{ post.locationDesc }}
            </div>
          </div>
          <div class="post-actions">
            <el-button 
              :type="post.likedByCurrent ? 'primary' : 'default'"
              size="small"
              @click="handleLike(post)"
            >
              <!-- <el-icon><Star /></el-icon> -->
               ❤️
              {{ post.likeCount || 0 }}
            </el-button>
            <el-button size="small" @click="goToDetail(post.id)">
              <el-icon><Comment /></el-icon>
              {{ post.commentCount || 0 }}
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
        </el-card>
      </div>

      <!-- 分页 -->
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

    <!-- 发布动态对话框（多图上传） -->
    <el-dialog v-model="createVisible" title="发布动态" width="600px" @close="resetCreateForm">
      <el-form ref="postFormRef" :model="postForm" :rules="postRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="标题不能超过100个字符" maxlength="100" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="postForm.content" type="textarea" rows="3" placeholder="分享猫咪趣事..." maxlength="8000" show-word-limit></el-input>
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
          <el-input v-model="postForm.locationDesc" placeholder="地点描述"></el-input>
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
import { Plus, Location, Star, Comment } from '@element-plus/icons-vue'
import { getPostPage, createPost, likePost, deletePost } from '@/api/community'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { userId, userInfo, isAdmin, isLoggedIn } = storeToRefs(userStore)

const currentUserId = computed(() => String(userId.value || userInfo.value?.id || ''))

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 列表数据
const posts = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 发布动态
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
  title: [
    { max: 100, message: '标题不能超过100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { max: 5000, message: '内容不能超过8000个字符', trigger: 'blur' }
  ]
}

// 图片上传相关
const uploadAction = ref('http://localhost:8080/api/community/upload')
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + localStorage.getItem('token')
}))
const imageList = ref([])

// 上传前校验
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

const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

const currentUserId = computed(() => userId.value)

// 获取列表（整合搜索关键词）
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    }
    const res = await getPostPage(params)
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

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  fetchData()
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

// 打开发布对话框
const openCreateDialog = () => {
  createVisible.value = true
}

// 重置表单（包含清空图片列表）
const resetCreateForm = () => {
  postFormRef.value?.resetFields()
  postForm.title = ''
  postForm.content = ''
  postForm.images = ''
  postForm.locationDesc = ''
  imageList.value = []
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
      fetchData()
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
const goToDetail = (postId) => {
  router.push(`/community/post/${postId}`)
}

// 删除动态
const handleDeletePost = async (id) => {
  ElMessageBox.confirm('确认删除该动态？删除后不可恢复', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deletePost(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.post-list {
  padding: 20px;
}
.post-card {
  margin-bottom: 20px;
}
.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.post-user {
  margin-left: 10px;
}
.user-name {
  font-weight: bold;
}
.post-time {
  font-size: 12px;
  color: #999;
}
.post-content {
  margin: 10px 0;
}
.post-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}
.post-text {
  white-space: pre-wrap;
}
.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}
.post-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  object-fit: cover;
  cursor: pointer;
}
.post-location {
  margin-top: 10px;
  color: #409EFF;
  font-size: 14px;
}
.post-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
</style>