<template>
  <div class="cat-detail" v-loading="loading">
    <el-page-header @back="goBack" content="猫咪详情" />

    <el-card v-if="cat" class="cat-info">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-image :src="cat.avatar || defaultImage" fit="cover" style="width: 100%; height: 200px;" lazy>
            <template #error>
              <div class="image-placeholder">暂无图片</div>
            </template>
          </el-image>
        </el-col>
        <el-col :span="16">
          <h2>{{ cat.name }}</h2>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="品种">{{ cat.breed || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ cat.gender === 1 ? '公' : cat.gender === 2 ? '母' : '未知' }}</el-descriptions-item>
            <el-descriptions-item label="性格">{{ cat.personality || '无' }}</el-descriptions-item>
            <el-descriptions-item label="健康状况">{{ cat.healthStatus || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="绝育状态">{{ cat.neuterStatus === 1 ? '已绝育' : cat.neuterStatus === 2 ? '未绝育' : '未知' }}</el-descriptions-item>
            <el-descriptions-item label="领养状态">{{ getStatusText(cat.adoptionStatus) }}</el-descriptions-item>
            <el-descriptions-item label="特征描述" :span="2">{{ cat.description || '无' }}</el-descriptions-item>
          </el-descriptions>
          <!-- 修改：申请领养按钮改为跳转链接 -->
          <div class="action" v-if="cat.adoptionStatus === 1 && isLoggedIn">
            <el-button type="primary" size="large" @click="goToApply">申请领养</el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-tabs v-if="cat" v-model="activeTab" class="detail-tabs" @tab-click="handleTabClick">
      <el-tab-pane label="照片墙" name="photos">
        <PhotoWall
          :cat-id="cat.id"
          :can-manage="canManage"
          @set-cover="handleSetCover"
        />
      </el-tab-pane>
      <el-tab-pane label="健康档案" name="health">
        <HealthRecords :cat-id="cat.id" :can-manage="canManage" />
      </el-tab-pane>
      <el-tab-pane label="位置追踪" name="locations">
        <LocationTracker ref="locationTrackerRef" :cat-id="cat.id" :can-manage="canManage" />
      </el-tab-pane>
    </el-tabs>

    <!-- 移除原有的 AdoptionApplyForm 组件，不再需要弹窗 -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCatById, updateCat } from '@/api/cat'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import PhotoWall from '@/components/PhotoWall.vue'
import HealthRecords from '@/components/HealthRecords.vue'
import LocationTracker from '@/components/LocationTracker.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn, userInfo, isAdmin, isVolunteer } = storeToRefs(userStore)

const cat = ref(null)
const loading = ref(false)
const activeTab = ref('photos')
const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200"%3E%3Crect width="200" height="200" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const canManage = computed(() => isAdmin.value || isVolunteer.value)

// 引用子组件
const locationTrackerRef = ref(null)

// 处理封面更新事件
const handleSetCover = async (photoUrl) => {
  if (!cat.value) return
  try {
    const updatedCat = { ...cat.value, avatar: photoUrl }
    const res = await updateCat(cat.value.id, updatedCat)
    if (res.code === 200) {
      ElMessage.success('封面设置成功')
      cat.value.avatar = photoUrl
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch (error) {
    console.error('设置封面失败', error)
    ElMessage.error('网络错误')
  }
}

// 获取猫咪详情
const fetchCat = async () => {
  loading.value = true
  try {
    const res = await getCatById(route.params.id)
    if (res.code === 200) {
      cat.value = res.data
    } else {
      ElMessage.error(res.message || '获取猫咪详情失败')
    }
  } catch (error) {
    console.error('获取猫咪详情失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

// 状态映射
const getStatusText = (status) => {
  const map = { 0:'在养',1:'待领养',2:'待审核',3:'已领养',4:'失踪',5:'去世' }
  return map[status] || '未知'
}

// 跳转到领养申请页面
const goToApply = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (cat.value?.adoptionStatus !== 1) {
    ElMessage.warning('该猫咪暂不可领养')
    return
  }
  router.push(`/adoption/apply?catId=${cat.value.id}`)
}

// 返回列表页
const goBack = () => {
  router.push('/cats')
}

// 切换标签页时，如果切换到位置追踪，调用子组件方法刷新地图
const handleTabClick = (tab) => {
  if (tab.props.name === 'locations' && locationTrackerRef.value) {
    locationTrackerRef.value.checkMap()
  }
}

onMounted(() => {
  fetchCat()
})
</script>

<style scoped>
.cat-detail {
  padding: 2rem 0;
  max-width: 1000px;
  margin: 0 auto;
}
.cat-info {
  margin-bottom: 20px;
}
.action {
  margin-top: 20px;
  text-align: right;
}
.detail-tabs {
  margin-top: 20px;
}
.image-placeholder {
  width: 100%;
  height: 100%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
</style>