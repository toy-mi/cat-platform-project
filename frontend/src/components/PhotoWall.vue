<template>
  <div class="photo-wall" v-loading="loading">
    <div class="upload-btn" v-if="canManage">
      <el-upload
        :action="uploadAction"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :before-upload="beforeUpload"
        list-type="picture-card"
        multiple
        :limit="9"
        :file-list="fileList"
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
    </div>

    <div class="photo-list" v-if="photos.length">
      <div v-for="(photo, index) in photos" :key="photo.id" class="photo-item">
        <el-image
          :src="photo.photoUrl"
          class="photo-img"
          :preview-src-list="photos.map(p => p.photoUrl)"
          :initial-index="index"
          fit="cover"
          preview-teleported
          lazy
        />
        <div class="photo-actions" v-if="canManage">
          <!-- 设为封面按钮 -->
          <el-button
            type="primary"
            size="small"
            circle
            @click.stop="setAsCover(photo.photoUrl)"
            title="设为封面"
          >
            <el-icon><Star /></el-icon>
          </el-button>
          <!-- 删除按钮 -->
          <el-button
            type="danger"
            size="small"
            circle
            @click.stop="handleDeletePhoto(photo.id)"
            title="删除"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无照片" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Star } from '@element-plus/icons-vue'
import { getCatPhotoList, deleteCatPhoto } from '@/api/cat'

const props = defineProps({
  catId: Number,
  canManage: Boolean
})
const emit = defineEmits(['set-cover'])

const photos = ref([])
const loading = ref(false)
const fileList = ref([])

const uploadAction = computed(() => `/api/cat-photos/upload/${props.catId}`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const fetchPhotos = async () => {
  loading.value = true
  try {
    const res = await getCatPhotoList(props.catId)
    if (res.code === 200) {
      photos.value = res.data
    } else {
      ElMessage.error(res.message || '获取照片失败')
    }
  } catch (error) {
    console.error('获取照片失败', error)
  } finally {
    loading.value = false
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件！')
  if (!isLt5M) ElMessage.error('图片大小不能超过5MB！')
  return isImage && isLt5M
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    fetchPhotos()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleDeletePhoto = async (id) => {
  ElMessageBox.confirm('确认删除该照片？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteCatPhoto(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchPhotos()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

const setAsCover = (photoUrl) => {
  emit('set-cover', photoUrl)
}

onMounted(() => {
  fetchPhotos()
})
</script>

<style scoped>
.photo-wall {
  min-height: 200px;
}
.upload-btn {
  margin-bottom: 20px;
}
.photo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.photo-item {
  position: relative;
  width: 120px;
  height: 120px;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
}
.photo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.photo-actions {
  display: flex;
  gap: 5px;
  position: absolute;
  top: 5px;
  right: 5px;
}
</style>