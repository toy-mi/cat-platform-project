<template>
  <div class="home">
    <!-- 装饰性漂浮猫咪元素 -->
    <div class="floating-cat cat-1">🐱</div>
    <div class="floating-cat cat-3">🐈</div>
    <div class="floating-cat cat-4">🐾</div>
    <div class="floating-cat cat-5">🐱‍👤</div>

    <!-- Hero 区域 -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="container">
        <div class="hero-content">
          <h1>校园猫咪养护系统</h1>
          <p>为流浪猫找一个温暖的家，用爱心守护每一个生命</p>
          <div class="hero-buttons">
            <el-button type="primary" size="large" round @click="$router.push('/cats')">查看猫咪</el-button>
            <el-button size="large" round @click="$router.push('/adoption-process')">领养流程</el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据卡片 -->
    <section class="stats">
      <div class="container">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="8" v-for="stat in statsItems" :key="stat.label">
            <div class="stat-card">
              <div class="stat-icon">{{ stat.icon }}</div>
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 热门猫咪推荐 -->
    <section class="featured-cats">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">可爱猫咪推荐</h2>
          <div class="section-line"></div>
          <p class="section-subtitle">这些小家伙正在等待一个温暖的家</p>
        </div>
        <div v-loading="catsLoading" class="cats-grid">
          <CatCard v-for="cat in hotCats" :key="cat.id" :cat="cat" />
        </div>
        <div class="text-center mt-5">
          <el-button type="primary" plain round @click="$router.push('/cats')">查看更多猫咪</el-button>
        </div>
      </div>
    </section>

    <!-- 最新动态 -->
    <section class="latest-posts">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">最新社区动态</h2>
          <div class="section-line"></div>
          <p class="section-subtitle">分享猫咪趣事，交流养宠经验</p>
        </div>
        <div v-loading="postsLoading" class="posts-grid">
          <PostCard v-for="post in latestPosts" :key="post.id" :post="post" />
        </div>
        <div class="text-center mt-5">
          <el-button type="primary" plain round @click="$router.push('/community')">查看更多动态</el-button>
        </div>
      </div>
    </section>

    <!-- 公告轮播 -->
    <section class="announcement">
      <div class="container">
        <div class="section-header">
           <h2 class="section-title">最新公告</h2>
          <div class="section-line"></div>
        </div>
        <AnnouncementCarousel />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import CatCard from '@/components/CatCard.vue'
import PostCard from '@/components/PostCard.vue'
import AnnouncementCarousel from '@/views/announcement/AnnouncementCarousel.vue'
import { getCatStatistics, getCatPage } from '@/api/cat'
import { getPostPage } from '@/api/community'

// 统计数据（从接口获取后填充）
const statsRaw = ref({
  total: 0,
  adoptable: 0,
  adopted: 0
})

// 构建统计卡片数据（带图标）
const statsItems = computed(() => [
  { icon: '🐱', label: '猫咪总数', value: statsRaw.value.total },
  { icon: '🏠', label: '待领养', value: statsRaw.value.adoptable },
  { icon: '❤️', label: '已领养', value: statsRaw.value.adopted }
])

// 热门猫咪
const hotCats = ref([])
const catsLoading = ref(false)

// 最新动态
const latestPosts = ref([])
const postsLoading = ref(false)

// 获取猫咪统计数据
const fetchCatStats = async () => {
  try {
    const res = await getCatStatistics()
    if (res.code === 200) {
      statsRaw.value.total = res.data.total || 0
      const statusStats = res.data.statusStats || []
      const adoptable = statusStats.find(s => s.status === 1)?.count || 0
      const adopted = statusStats.find(s => s.status === 3)?.count || 0
      statsRaw.value.adoptable = adoptable
      statsRaw.value.adopted = adopted
    }
  } catch (error) {
    console.warn('获取猫咪统计异常', error)
  }
}

// 获取热门猫咪（取 4 只待领养猫咪）
const fetchHotCats = async () => {
  catsLoading.value = true
  try {
    const res = await getCatPage({ pageNum: 1, pageSize: 4, adoptionStatus: 1 })
    if (res.code === 200) hotCats.value = res.data.records
  } catch (error) {
    console.warn('获取热门猫咪异常', error)
  } finally {
    catsLoading.value = false
  }
}

// 获取最新动态（取 3 条）
const fetchLatestPosts = async () => {
  postsLoading.value = true
  try {
    const res = await getPostPage({ pageNum: 1, pageSize: 3 })
    if (res.code === 200) latestPosts.value = res.data.records
  } catch (error) {
    console.warn('获取最新动态异常', error)
  } finally {
    postsLoading.value = false
  }
}

onMounted(() => {
  fetchCatStats()
  fetchHotCats()
  fetchLatestPosts()
})
</script>

<style scoped>
.home {
  background: #fffbf5;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  /* 可爱猫爪脚印背景平铺 */
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 60 60'%3E%3Cpath fill='%23FFD8A8' fill-opacity='0.3' d='M15 25c-2.2 0-4 1.8-4 4s1.8 4 4 4 4-1.8 4-4-1.8-4-4-4zm10-5c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 15c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm15-10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-5-5c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 60px 60px;
}

/* 漂浮猫咪装饰 */
.floating-cat {
  position: fixed;
  font-size: 5rem;
  opacity: 0.2;
  pointer-events: none;
  z-index: 0;
  animation: float 12s ease-in-out infinite;
}
.cat-1 {
  top: 15%;
  left: 5%;
  animation-delay: 0s;
  font-size: 6rem;
}
.cat-2 {
  bottom: 20%;
  right: 8%;
  animation-delay: 1s;
  font-size: 7rem;
}
.cat-3 {
  top: 40%;
  right: 15%;
  animation-delay: 2s;
  font-size: 5rem;
}
.cat-4 {
  bottom: 10%;
  left: 20%;
  animation-delay: 3s;
  font-size: 4rem;
}
.cat-5 {
  top: 70%;
  left: 80%;
  animation-delay: 4s;
  font-size: 5.5rem;
}

@keyframes float {
  0% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
  100% {
    transform: translateY(0px) rotate(0deg);
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

/* Hero 区域 */
.hero {
  position: relative;
  /* 图片路径、位置、大小均可在此调整 */
  background: url('/images/3.jpg') 90% top  /cover no-repeat;
  padding: 5rem 0 6rem;
  text-align: center;
  overflow: hidden;
}
.hero::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* background: rgba(231, 231, 230, 0.071);  */
  /* 半透明深色层，增强文字对比度，可调整透明度 */
  z-index: 1;
}
/* .hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle at 10% 20%, rgba(255,245,220,0.4) 2%, transparent 2.5%);
  background-size: 30px 30px;
  pointer-events: none;
  z-index: 2;
} */
.hero-content {
  position: relative;
  z-index: 3;
  max-width: 700px;
  margin: 0 auto;
}
.hero h1 {
  font-size: 3rem;
  font-weight: 800;
  margin-top: 3rem; /* 向下拉近 */
  margin-bottom: 1rem;
  color: #fe851ce5;
  /*text-shadow: 2px 2px 4px rgba(0,0,0,0.2);*/
  /* -webkit-text-stroke: 1px white; */
  /* -webkit-text-stroke: 1px rgba(255, 136, 45); */
  /* -webkit-text-stroke: 1px rgb(255, 121, 18); */
  /* text-stroke: 1px white; */
  font-family: 'Quicksand', cursive;
}
.hero p {
  font-size: 1.5rem;
  margin-top: -1rem; /* 向上拉近 */
  margin-bottom: 2rem;
  color: rgb(255, 158, 32);
  /* -webkit-text-stroke: 1px rgba(255, 219, 18, 0.797); */
  line-height: 2;
}
.hero-buttons {
  display: flex;
  margin-top: 2rem; /* 向上拉近 */
  gap: 2rem;
  justify-content: center;
  flex-wrap: wrap;
}
.hero-buttons .el-button {
  min-width: 140px;
  padding: 10px 24px;
  font-weight: 500;
  transition: transform 0.2s, box-shadow 0.2s;
  background-color: #FFE6C7;
  border-color: #FFD8A8;
  color: #B45F2B;
}
.hero-buttons .el-button--primary {
  background-color: #FF9F4A;
  border-color: #FF8C2E;
  color: white;
}
.hero-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

/* 统计数据 */
.stats {
  padding: 3rem 0;
  background: rgba(255, 249, 239, 0.9);
  backdrop-filter: blur(2px);
}
.stat-card {
  background: white;
  border-radius: 32px;
  padding: 1.5rem 1rem;
  text-align: center;
  box-shadow: 0 8px 20px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
}
.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(0,0,0,0.08);
}
.stat-icon {
  font-size: 2.8rem;
  margin-bottom: 0.5rem;
}
.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #FF9F4A;
  line-height: 1.2;
  margin-bottom: 0.25rem;
}
.stat-label {
  color: #6B4C2C;
  font-size: 0.9rem;
}

/* 通用标题样式 */
.section-header {
  text-align: center;
  margin-bottom: 2.5rem;
}
.section-title {
  font-size: 2rem;
  font-weight: 700;
  color: #B45F2B;
  margin-bottom: 0.5rem;
  position: relative;
  display: inline-block;
}
.section-line {
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #FFB77C, #FF9F4A);
  margin: 0.5rem auto 1rem;
  border-radius: 2px;
}
.section-subtitle {
  color: #8B5A2B;
  font-size: 1rem;
  max-width: 600px;
  margin: 0 auto;
}

/* 猫咪网格 */
.featured-cats {
  padding: 3rem 0;
  background: rgba(255, 251, 245, 0.95);
}
.cats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.8rem;
  margin-bottom: 2rem;
}

/* 动态网格 */
.latest-posts {
  padding: 3rem 0;
  background: rgba(255, 249, 239, 0.95);
}
.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.8rem;
  margin-bottom: 2rem;
}

/* 公告区域 */
.announcement {
  padding: 3rem 0;
  background: rgba(255, 251, 245, 0.95);
}

/* 按钮通用样式 */
.el-button--primary {
  background-color: #FF9F4A;
  border-color: #FF8C2E;
  color: white;
}
.el-button--primary.is-plain {
  background-color: transparent;
  border-color: #FF9F4A;
  color: #FF9F4A;
}
.el-button--primary.is-plain:hover {
  background-color: #FF9F4A;
  border-color: #FF9F4A;
  color: white;
}
.el-button--default {
  background-color: #FFF2E0;
  border-color: #FFE0B5;
  color: #B45F2B;
}
.el-button--default:hover {
  background-color: #FFE6C7;
  border-color: #FFD8A8;
  color: #9B4F1F;
}

.text-center {
  text-align: center;
}
.mt-5 {
  margin-top: 2rem;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero {
    padding: 3rem 0;
  }
  .hero h1 {
    font-size: 2rem;
  }
  .hero p {
    font-size: 1rem;
  }
  .section-title {
    font-size: 1.6rem;
  }
  .stats {
    padding: 2rem 0;
  }
  .stat-card {
    padding: 1rem;
    margin-bottom: 1rem;
  }
  .cats-grid, .posts-grid {
    gap: 1rem;
  }
  .floating-cat {
    font-size: 3rem !important;
    opacity: 0.15;
  }
}
</style>