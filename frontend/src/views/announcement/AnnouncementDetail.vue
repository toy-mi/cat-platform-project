<template>
  <div class="announcement-detail" v-loading="loading">
    <el-page-header @back="goBack" content="公告详情" />
    <el-card v-if="detail" style="margin-top: 20px;">
      <h2>{{ detail.title }}</h2>
      <div class="meta">
        <span>发布时间：{{ formatDateTime(detail.publishTime) }}</span>
        <span v-if="detail.creatorName">发布人：{{ detail.creatorName }}</span>
      </div>
      <div class="content" v-html="formattedContent"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAnnouncementDetail } from '@/api/announcement'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const detail = ref(null)
const loading = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementDetail(id)
    if (res.code === 200) {
      detail.value = res.data
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

const goBack = () => {
  router.push('/announcements')
}

const formatDateTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

// 将文本中的换行转换为 <br>，并防止XSS（简单处理）
const formattedContent = computed(() => {
  if (!detail.value?.content) return ''
  return detail.value.content.replace(/\n/g, '<br>')
})

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.announcement-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
  margin: 10px 0 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
.content {
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>