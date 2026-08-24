<template>
  <div class="cat-card card" @click="$router.push(`/cats/${cat.id}`)">
    <div class="cat-image">
      <el-image :src="cat.avatar || defaultImage" fit="cover">
        <template #error>
          <div class="image-placeholder">暂无图片</div>
        </template>
      </el-image>
    </div>
    <div class="cat-info">
      <h3>😺 {{ cat.name }} </h3>
      <p class="breed">{{ cat.breed || '未知品种' }}</p>
      <div class="tags">
        <el-tag size="small" :type="cat.gender === 1 ? 'primary' : cat.gender === 2 ? 'danger' : 'info'">
          {{ cat.gender === 1 ? '公' : cat.gender === 2 ? '母' : '未知' }}
        </el-tag>
        <el-tag size="small" :type="getStatusTagType(cat.adoptionStatus)">
          {{ getStatusText(cat.adoptionStatus) }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps(['cat'])
const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200"%3E%3Crect width="200" height="200" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="14"%3E暂无图片%3C/text%3E%3C/svg%3E'

const getStatusText = (status) => {
  const map = { 0:'在养',1:'待领养',2:'待审核',3:'已领养',4:'失踪',5:'去世' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 0:'success',1:'warning',2:'info',3:'info',4:'danger',5:'danger' }
  return map[status] || 'info'
}
</script>

<style scoped>
.cat-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  width: 100%;  /* 占满父容器宽度，父容器由网格控制 */
}
.cat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 20px rgba(0,0,0,0.1);
}
.cat-image {
   /* 正方形图片，宽高相等，使用 aspect-ratio 确保比例 */
  aspect-ratio: 1 / 1;     /* 1:1 正方形 */
  width: 100%;             /* 占满卡片宽度 */
  overflow: hidden;
  background: #f5f7fa;     /* 加载时背景色 */
}
.cat-image :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}
.cat-info {
  padding: 12px;
}
.cat-info h3 {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
}
.breed {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 8px;
}
.tags {
  display: flex;
  gap: 6px;
}
</style>