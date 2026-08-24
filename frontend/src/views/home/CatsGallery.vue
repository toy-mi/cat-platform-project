<template>
  <div class="cats-gallery">
    <div class="container">
      <h1 class="page-title">🐾 猫咪图鉴 · 萌宠乐园 🐾</h1>
      <p class="page-subtitle">浏览我们的猫咪，找到你的心动伙伴</p>

      <!-- 筛选栏 -->
      <el-form :inline="true" :model="filters" class="filters">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="输入姓名或品种" clearable  style="width:150px"/>
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="filters.breed" placeholder="输入品种" clearable style="width:100px"/>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="filters.gender" placeholder="不限" clearable style="width:80px">
            <el-option label="公" :value="1" />
            <el-option label="母" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="绝育状态">
          <el-select v-model="filters.neuterStatus" placeholder="不限" clearable style="width:100px">
            <el-option label="已绝育" :value="1" />
            <el-option label="未绝育" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="领养状态">
          <el-select v-model="filters.adoptionStatus" placeholder="不限" clearable style="width:100px">
            <el-option label="待领养" :value="1" />
            <el-option label="在养" :value="0" />
            <el-option label="已领养" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-loading="loading" class="cats-grid">
        <CatCard v-for="cat in list" :key="cat.id" :cat="cat" />
        <el-empty v-if="!loading && list.length === 0" description="没有找到符合条件的猫咪" />
      </div>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchCats"
        @current-change="fetchCats"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import CatCard from '@/components/CatCard.vue'
import { getCatPage } from '@/api/cat'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const loading = ref(false)

const filters = reactive({
  keyword: '',
  breed: '',
  gender: null,
  neuterStatus: null,
  adoptionStatus: null,
})

const fetchCats = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      breed: filters.breed || undefined,
      gender: filters.gender,
      neuterStatus: filters.neuterStatus,
      adoptionStatus: filters.adoptionStatus,
    }
    const res = await getCatPage(params)
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取猫咪列表失败')
    }
  } catch (error) {
    console.error('获取猫咪列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchCats()
}
const resetFilters = () => {
  Object.keys(filters).forEach(k => {
    if (k === 'keyword' || k === 'breed') filters[k] = ''
    else filters[k] = null
  })
  filters.orderBy = 'createTime_desc'
  handleSearch()
}

onMounted(() => {
  fetchCats()
})
</script>

<style scoped>
.cats-gallery {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 3rem 20px;   /* 底部增加 3rem 间距 */
}
.page-title {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 0.5rem;
}
.page-subtitle {
  text-align: center;
  color: #64748b;
  margin-bottom: 2rem;
}
.filters {
  background: white;
  padding: 1rem;
  border-radius: 1rem;
  margin-bottom: 2rem;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}
.cats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);  /* 固定三列，每列等宽 */
  gap: 24px;                               /* 卡片间距 */
  margin-bottom: 2rem;
}
.pagination {
  display: flex;
  justify-content: center;
}
</style>