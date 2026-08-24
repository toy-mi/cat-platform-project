<template>
  <div class="user-public">
    <h2>ta的主页</h2>

    <!-- 用户基本信息卡片（与 UserProfile 结构一致，无编辑按钮） -->
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
            <!-- 没有编辑资料按钮 -->
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 标签页（仅保留动态、评论、捐赠，与个人中心结构一致） -->
    <el-tabs v-model="activeTab" class="user-tabs">
      <el-tab-pane label="动态" name="posts">
        <div v-loading="postsLoading">
          <div v-for="post in posts" :key="post.id" class="post-item">
            <el-card>
              <div class="post-header">
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
                <el-button type="primary" link @click="goToPost(post.id)">查看</el-button>
              </div>
              <div class="post-content">
                <div v-if="post.title" class="post-title">{{ post.title }}</div>
                <div class="post-text">{{ truncate(post.content, 150) }}</div>
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
            @current-change="fetchPosts"
          />
          <el-empty v-if="!postsLoading && posts.length === 0" description="暂无动态" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="评论" name="comments">
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
            @current-change="fetchComments"
          />
          <el-empty v-if="!commentsLoading && comments.length === 0" description="暂无评论" />
        </div>
      </el-tab-pane>

      <!-- 捐赠标签页（仅显示已审核通过的捐赠） -->
      <el-tab-pane label="捐赠" name="donations">
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
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag type="success">已通过</el-tag>
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
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserPublicInfo } from '@/api/user'
import { getPublicUserPosts, getPublicUserComments } from '@/api/community'
import { getUserDonations } from '@/api/donation'

const route = useRoute()
const router = useRouter()
const userId = route.params.userId

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const userInfo = ref(null)

// 动态
const posts = ref([])
const postsTotal = ref(0)
const postsPageNum = ref(1)
const postsPageSize = ref(10)
const postsLoading = ref(false)

// 评论
const comments = ref([])
const commentsTotal = ref(0)
const commentsPageNum = ref(1)
const commentsPageSize = ref(10)
const commentsLoading = ref(false)

// 捐赠
const donations = ref([])
const donationsTotal = ref(0)
const donationsPageNum = ref(1)
const donationsPageSize = ref(10)
const donationsLoading = ref(false)

const activeTab = ref('posts')

// 获取用户信息
const fetchUserInfo = async () => {
  const res = await getUserPublicInfo(userId)
  if (res.code === 200) {
    userInfo.value = res.data
  } else {
    ElMessage.error(res.message || '用户不存在')
    router.push('/')
  }
}

// 获取动态
const fetchPosts = async () => {
  postsLoading.value = true
  try {
    const res = await getPublicUserPosts(userId, { pageNum: postsPageNum.value, pageSize: postsPageSize.value })
    if (res.code === 200) {
      posts.value = res.data.records
      postsTotal.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    postsLoading.value = false
  }
}

// 获取评论
const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const res = await getPublicUserComments(userId, { pageNum: commentsPageNum.value, pageSize: commentsPageSize.value })
    if (res.code === 200) {
      comments.value = res.data.records
      commentsTotal.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    commentsLoading.value = false
  }
}

// 获取捐赠
const fetchDonations = async () => {
  donationsLoading.value = true
  try {
    const res = await getUserDonations(userId, { pageNum: donationsPageNum.value, pageSize: donationsPageSize.value })
    if (res.code === 200) {
      donations.value = res.data.records
      donationsTotal.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    donationsLoading.value = false
  }
}

// 根据标签页加载数据
const loadDataByTab = () => {
  if (activeTab.value === 'posts') fetchPosts()
  else if (activeTab.value === 'comments') fetchComments()
  else if (activeTab.value === 'donations') fetchDonations()
}

watch(activeTab, loadDataByTab)

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()} ${d.getHours()}:${d.getMinutes()}`
}
const truncate = (text, len) => text?.length > len ? text.slice(0, len) + '...' : text || ''
const goToPost = (id) => router.push(`/community/post/${id}`)
const goToCampaign = (id) => router.push(`/donations/${id}`)
const getRoleText = (role) => {
  const map = { 'USER': '普通用户', 'VOLUNTEER': '志愿者', 'ADMIN': '管理员' }
  return map[role] || role
}

onMounted(() => {
  fetchUserInfo()
  loadDataByTab()
})
</script>

<style scoped>
/* 样式完全复制自 UserProfile，保持完全一致 */
.user-public {
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
.post-item, .comment-item {
  margin-bottom: 15px;
}
.post-header, .comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.post-time, .comment-time {
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
</style>