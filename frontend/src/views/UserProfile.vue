<template>
  <div class="user-profile">
    <h2>个人中心</h2>

    <!-- 用户基本信息卡片 -->
    <el-card class="user-info" v-if="userInfo">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-avatar :size="80" :src="userInfo.avatar || defaultAvatar" />
        </el-col>
        <el-col :span="20">
          <div class="user-detail">
            <div class="user-name">{{ userInfo.nickname || userInfo.username }}</div>
            <div class="user-meta">用户名：{{ userInfo.username }}</div>
            <div class="user-meta">邮箱：{{ userInfo.email || '未设置' }}</div>
            <div class="user-meta">手机：{{ userInfo.phone || '未设置' }}</div>
            <div class="user-meta">角色：{{ getRoleText(userInfo.role) }}</div>
            <el-button type="primary" size="small" @click="openEditDialog" style="margin-top: 10px;">编辑资料</el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="user-tabs">
      <el-tab-pane label="我的动态" name="posts">
        <div v-loading="postsLoading">
          <div v-for="post in posts" :key="post.id" class="post-item">
            <el-card>
              <div class="post-header">
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
                <el-button type="primary" link @click="goToPost(post.id)">查看</el-button>
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
                      class="post-thumb"
                  />
                </div>
              </div>
              <div class="post-stats">
                <span>❤️ {{ post.likeCount || 0 }}</span>
                <span>💬 {{ post.commentCount || 0 }}</span>
              </div>
            </el-card>
          </div>
          <el-pagination
              v-if="postsTotal > postsPageSize"
              v-model:current-page="postsPageNum"
              v-model:page-size="postsPageSize"
              :total="postsTotal"
              layout="prev, pager, next"
              @current-change="fetchUserPosts"
          />
          <el-empty v-if="!postsLoading && posts.length === 0" description="暂无动态" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的评论" name="comments">
        <div v-loading="commentsLoading">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-card>
              <div class="comment-header">
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                <el-button type="primary" link @click="goToPost(comment.postId)">查看原动态</el-button>
              </div>
              <div class="comment-content">
                <strong>评论内容：</strong> {{ comment.content }}
              </div>
              <div v-if="comment.postTitle" class="comment-post">
                动态标题：{{ comment.postTitle }}
              </div>
            </el-card>
          </div>
          <el-pagination
              v-if="commentsTotal > commentsPageSize"
              v-model:current-page="commentsPageNum"
              v-model:page-size="commentsPageSize"
              :total="commentsTotal"
              layout="prev, pager, next"
              @current-change="fetchUserComments"
          />
          <el-empty v-if="!commentsLoading && comments.length === 0" description="暂无评论" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的点赞" name="likes">
        <div v-loading="likesLoading">
          <div v-for="post in likes" :key="post.id" class="like-item">
            <el-card>
              <div class="like-header">
                <span class="like-time">{{ formatTime(post.createTime) }}</span>
                <el-button type="primary" link @click="goToPost(post.id)">查看</el-button>
              </div>
              <div class="like-content">
                <div v-if="post.title" class="post-title">{{ post.title }}</div>
                <div class="post-text">{{ post.content }}</div>
                <div v-if="post.images" class="post-images">
                  <el-image
                      v-for="(img, idx) in post.images.split(',')"
                      :key="idx"
                      :src="img"
                      fit="cover"
                      class="post-thumb"
                  />
                </div>
              </div>
              <div class="post-stats">
                <span>❤️ {{ post.likeCount || 0 }}</span>
                <span>💬 {{ post.commentCount || 0 }}</span>
              </div>
            </el-card>
          </div>
          <el-pagination
              v-if="likesTotal > likesPageSize"
              v-model:current-page="likesPageNum"
              v-model:page-size="likesPageSize"
              :total="likesTotal"
              layout="prev, pager, next"
              @current-change="fetchUserLikes"
          />
          <el-empty v-if="!likesLoading && likes.length === 0" description="暂无点赞" />
        </div>
      </el-tab-pane>

      <!-- 我的捐赠 -->
      <el-tab-pane label="我的捐赠" name="donations">
        <div v-loading="donationsLoading">
          <el-table :data="donations" border stripe style="width: 100%">
            <el-table-column prop="campaignTitle" label="活动名称" min-width="180">
              <template #default="{ row }">
                <el-button type="primary" link @click="goToCampaign(row.campaignId)">
                  {{ row.campaignTitle || '未关联活动' }}
                </el-button>
              </template>
            </el-table-column>
            <el-table-column prop="donationType" label="类型" width="80">
              <template #default="{ row }">
                {{ row.donationType === 'MONEY' ? '资金' : '物资' }}
              </template>
            </el-table-column>
            <el-table-column label="捐赠详情" min-width="150">
              <template #default="{ row }">
                <span v-if="row.donationType === 'MONEY'">￥{{ row.amount }}</span>
                <span v-else>{{ row.goodsName }} × {{ row.goodsQuantity }} {{ row.goodsUnit || '' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="donationTime" label="捐赠时间" width="160"></el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getDonationStatusTag(row.status)">
                  {{ getDonationStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="凭证" width="80">
              <template #default="{ row }">
                <el-button v-if="row.attachmentUrl" type="primary" link @click="previewImage(row.attachmentUrl)">查看</el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
              v-if="donationsTotal > donationsPageSize"
              v-model:current-page="donationsPageNum"
              v-model:page-size="donationsPageSize"
              :total="donationsTotal"
              layout="prev, pager, next"
              @current-change="fetchDonations"
          />
          <el-empty v-if="!donationsLoading && donations.length === 0" description="暂无捐赠记录" />
        </div>
      </el-tab-pane>

      <!-- 我的领养（添加“查看进度”按钮） -->
      <el-tab-pane label="我的领养" name="adoptions">
        <div class="my-adoptions">
          <div style="margin-bottom: 10px; text-align: right;">
            <el-button size="small" @click="refreshAdoptions" :loading="adoptionsLoading">刷新</el-button>
          </div>
          <el-table :data="adoptions" border stripe v-loading="adoptionsLoading">
            <el-table-column prop="id" label="申请ID" width="80" />
            <el-table-column prop="catName" label="猫咪" width="120" />
            <el-table-column prop="applyTime" label="申请时间" width="160" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getAdoptionStatusTag(row.status)">
                  {{ getAdoptionStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="firstReviewRemark" label="初审意见" show-overflow-tooltip />
            <el-table-column prop="finalReviewRemark" label="终审意见" show-overflow-tooltip />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button type="primary" link @click="goToAdoptionDetail(row.id)">查看详情</el-button>
                <el-button type="success" link @click="goToAdoptionProcess(row.id)">查看进度</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="adoptionsTotal > adoptionsPageSize"
            v-model:current-page="adoptionsPageNum"
            v-model:page-size="adoptionsPageSize"
            :total="adoptionsTotal"
            layout="prev, pager, next"
            @current-change="fetchAdoptions"
          />
          <el-empty v-if="!adoptionsLoading && adoptions.length === 0" description="暂无领养申请" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="500px" @close="resetEditForm">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
              class="avatar-uploader"
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">支持 jpg/png，大小不超过 2MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm" :loading="editLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserPosts, getUserComments, getUserLikes } from '@/api/community'
import { getCurrentUser, updateUserProfile } from '@/api/user'
import { getMyDonations } from '@/api/donation'
import { getAdoptionPage } from '@/api/adoption'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const activeTab = ref('posts')

// 用户基本信息
const fetchUserInfo = async () => {
  const res = await getCurrentUser()
  if (res.code === 200) {
    userStore.setUserInfo(res.data)
  }
}

// ========== 我的动态 ==========
const posts = ref([])
const postsTotal = ref(0)
const postsPageNum = ref(1)
const postsPageSize = ref(10)
const postsLoading = ref(false)

const fetchUserPosts = async () => {
  postsLoading.value = true
  try {
    const res = await getUserPosts({ pageNum: postsPageNum.value, pageSize: postsPageSize.value })
    if (res.code === 200) {
      posts.value = res.data.records
      postsTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取动态失败')
    }
  } catch (error) {
    console.error('获取动态失败', error)
    ElMessage.error('网络错误')
  } finally {
    postsLoading.value = false
  }
}

// ========== 我的评论 ==========
const comments = ref([])
const commentsTotal = ref(0)
const commentsPageNum = ref(1)
const commentsPageSize = ref(10)
const commentsLoading = ref(false)

const fetchUserComments = async () => {
  commentsLoading.value = true
  try {
    const res = await getUserComments({ pageNum: commentsPageNum.value, pageSize: commentsPageSize.value })
    if (res.code === 200) {
      comments.value = res.data.records
      commentsTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取评论失败')
    }
  } catch (error) {
    console.error('获取评论失败', error)
    ElMessage.error('网络错误')
  } finally {
    commentsLoading.value = false
  }
}

// ========== 我的点赞 ==========
const likes = ref([])
const likesTotal = ref(0)
const likesPageNum = ref(1)
const likesPageSize = ref(10)
const likesLoading = ref(false)

const fetchUserLikes = async () => {
  likesLoading.value = true
  try {
    const res = await getUserLikes({ pageNum: likesPageNum.value, pageSize: likesPageSize.value })
    if (res.code === 200) {
      likes.value = res.data.records
      likesTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取点赞失败')
    }
  } catch (error) {
    console.error('获取点赞失败', error)
    ElMessage.error('网络错误')
  } finally {
    likesLoading.value = false
  }
}

// ========== 我的捐赠 ==========
const donations = ref([])
const donationsTotal = ref(0)
const donationsPageNum = ref(1)
const donationsPageSize = ref(10)
const donationsLoading = ref(false)

const fetchDonations = async () => {
  donationsLoading.value = true
  try {
    const res = await getMyDonations({ pageNum: donationsPageNum.value, pageSize: donationsPageSize.value })
    if (res.code === 200) {
      donations.value = res.data.records
      donationsTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取捐赠记录失败')
    }
  } catch (error) {
    console.error('获取捐赠记录失败', error)
  } finally {
    donationsLoading.value = false
  }
}

const getDonationStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}
const getDonationStatusTag = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const goToCampaign = (campaignId) => {
  if (campaignId) {
    router.push(`/donations/${campaignId}`)
  } else {
    ElMessage.warning('该捐赠未关联具体活动')
  }
}

const previewImage = (url) => {
  window.open(url, '_blank')
}

// ========== 我的领养 ==========
const adoptions = ref([])
const adoptionsTotal = ref(0)
const adoptionsPageNum = ref(1)
const adoptionsPageSize = ref(10)
const adoptionsLoading = ref(false)

const getAdoptionStatusText = (status) => {
  const map = {
    0: '待初审', 1: '初审通过', 2: '初审拒绝', 3: '待回访',
    4: '回访通过', 5: '回访失败', 6: '待终审', 7: '终审通过',
    8: '终审拒绝', 9: '已签订协议', 10: '已完成领养', 11: '已取消'
  }
  return map[status] || '未知'
}
const getAdoptionStatusTag = (status) => {
  const map = {
    0: 'info', 1: 'warning', 2: 'danger', 3: 'info',
    4: 'success', 5: 'danger', 6: 'info', 7: 'success',
    8: 'danger', 9: 'success', 10: 'success', 11: 'info'
  }
  return map[status] || 'info'
}

const fetchAdoptions = async () => {
  if (!userInfo.value) return
  adoptionsLoading.value = true
  try {
    const res = await getAdoptionPage({
      pageNum: adoptionsPageNum.value,
      pageSize: adoptionsPageSize.value,
      userId: userInfo.value.id
    })
    if (res.code === 200) {
      adoptions.value = res.data.records
      adoptionsTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取领养记录失败')
    }
  } catch (error) {
    console.error('获取领养记录失败', error)
  } finally {
    adoptionsLoading.value = false
  }
}

const refreshAdoptions = () => {
  adoptionsPageNum.value = 1
  fetchAdoptions()
}

const goToAdoptionDetail = (id) => {
  router.push(`/adoptions/${id}`)
}

// 新增：跳转到领养流程页面并携带申请ID
const goToAdoptionProcess = (id) => {
  router.push(`/adoption-process?id=${id}`)
}

// ========== 通用逻辑 ==========
const loadDataByTab = () => {
  if (activeTab.value === 'posts') fetchUserPosts()
  else if (activeTab.value === 'comments') fetchUserComments()
  else if (activeTab.value === 'likes') fetchUserLikes()
  else if (activeTab.value === 'donations') fetchDonations()
  else if (activeTab.value === 'adoptions') fetchAdoptions()
}

// 监听标签页切换
watch(activeTab, (newVal) => {
  // 当切换到某标签页且数据为空时加载，否则不重复加载
  if (newVal === 'adoptions' && adoptions.value.length === 0 && !adoptionsLoading.value) {
    fetchAdoptions()
  } else if (newVal !== 'adoptions') {
    // 非adoptions标签页，按需加载（已有数据不重复加载）
    if (newVal === 'posts' && posts.value.length === 0 && !postsLoading.value) fetchUserPosts()
    else if (newVal === 'comments' && comments.value.length === 0 && !commentsLoading.value) fetchUserComments()
    else if (newVal === 'likes' && likes.value.length === 0 && !likesLoading.value) fetchUserLikes()
    else if (newVal === 'donations' && donations.value.length === 0 && !donationsLoading.value) fetchDonations()
  }
})

const goToPost = (postId) => {
  router.push(`/community/post/${postId}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

const getRoleText = (role) => {
  const map = { 'USER': '普通用户', 'VOLUNTEER': '志愿者', 'ADMIN': '管理员' }
  return map[role] || role
}

// ==================== 编辑资料相关 ====================
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})
const editRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const uploadAction = ref('http://localhost:8080/api/user/avatar')
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + localStorage.getItem('token')
}))
const avatarUrl = ref('')

const openEditDialog = () => {
  editForm.nickname = userInfo.value?.nickname || ''
  editForm.email = userInfo.value?.email || ''
  editForm.phone = userInfo.value?.phone || ''
  avatarUrl.value = userInfo.value?.avatar || ''
  editDialogVisible.value = true
}

const resetEditForm = () => {
  editFormRef.value?.resetFields()
  avatarUrl.value = ''
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    avatarUrl.value = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('上传失败，请重试')
}

const submitEditForm = async () => {
  await editFormRef.value?.validate()
  editLoading.value = true
  try {
    const res = await updateUserProfile(editForm)
    if (res.code === 200) {
      ElMessage.success('资料更新成功')
      userStore.setUserInfo(res.data)
      editDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败', error)
    ElMessage.error('网络错误')
  } finally {
    editLoading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
  // 根据 URL 参数决定激活哪个标签页
  const tab = route.query.tab
  if (tab === 'adoptions') {
    activeTab.value = 'adoptions'
    fetchAdoptions()
  } else {
    loadDataByTab() // 默认加载当前激活标签页（posts）的数据
  }
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
}
.user-info {
  margin-bottom: 20px;
}
.user-detail {
  padding-left: 20px;
}
.user-name {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 10px;
}
.user-meta {
  color: #666;
  margin-bottom: 5px;
}
.post-item, .comment-item, .like-item {
  margin-bottom: 15px;
}
.post-header, .comment-header, .like-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.post-time, .comment-time, .like-time {
  color: #999;
  font-size: 14px;
}
.post-content {
  margin-bottom: 10px;
}
.post-title {
  font-weight: bold;
  margin-bottom: 5px;
}
.post-text {
  white-space: pre-wrap;
}
.post-images {
  display: flex;
  gap: 5px;
  margin-top: 10px;
}
.post-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}
.post-stats {
  display: flex;
  gap: 15px;
  color: #666;
  font-size: 14px;
}
.comment-content {
  margin-bottom: 5px;
}
.comment-post {
  color: #409EFF;
  font-size: 13px;
}
.user-tabs {
  min-height: 500px;
}
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
}
.avatar-uploader:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}
.avatar-preview {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
.el-upload__tip {
  line-height: 1.2;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>