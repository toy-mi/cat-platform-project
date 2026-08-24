<template>
  <div class="campaign-list">
    <h2>募捐活动</h2>

    <!-- 搜索/筛选栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 200px;">
          <el-option label="筹备中" :value="0"></el-option>
          <el-option label="进行中" :value="1"></el-option>
          <el-option label="已结束" :value="2"></el-option>
          <el-option label="已取消" :value="3"></el-option>
          <el-option label="已完成" :value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="关联猫咪">
        <el-select v-model="searchForm.catId" placeholder="请选择猫咪" clearable filterable style="width: 200px;">
          <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
        </el-select>
      </el-form-item>
      <!-- 关键词搜索框 -->
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="输入标题或描述" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
      <el-form-item style="float: right;">
        <el-button type="success" @click="openCreateDialog" v-if="isAdmin">创建活动</el-button>
      </el-form-item>
    </el-form>

    <!-- 活动卡片列表 -->
    <div v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom: 20px;">
          <el-card class="campaign-card" @click="goToDetail(item.id)">
            <div class="campaign-header">
              <span class="campaign-title">{{ item.title }}</span>
              <el-tag :type="getStatusTagType(item.status)">{{ getStatusText(item.status) }}</el-tag>
            </div>
            <div class="campaign-cat" v-if="item.catName">🐱 关联猫咪：{{ item.catName }}</div>
            <div class="campaign-target">目标金额：￥{{ item.targetAmount }}</div>
            <div class="campaign-current">已筹金额：￥{{ item.currentAmount || 0 }}</div>
            <div class="campaign-progress">
              <el-progress :percentage="getCampaignProgress(item)" :format="formatProgress"></el-progress>
              <!-- <span class="progress-text">{{ getCampaignProgress(item).toFixed(1) }}%</span> -->
            </div>
            <div class="campaign-time">时间：{{ formatDate(item.startDate) }} 至 {{ formatDate(item.endDate) }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[6, 12, 24]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
      />
    </div>

    <!-- 创建/编辑活动对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入活动描述"></el-input>
        </el-form-item>
        <el-form-item label="目标金额" prop="targetAmount">
          <el-input-number v-model="form.targetAmount" :precision="2" :min="0.01" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="开始时间" prop="startDate">
          <el-date-picker v-model="form.startDate" type="datetime" placeholder="选择开始时间" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endDate">
          <el-date-picker v-model="form.endDate" type="datetime" placeholder="选择结束时间" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="关联猫咪" prop="catId">
          <el-select v-model="form.catId" placeholder="请选择（留空表示全体）" clearable filterable style="width:100%">
            <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="筹备中" :value="0"></el-option>
            <el-option label="进行中" :value="1"></el-option>
            <el-option label="已结束" :value="2"></el-option>
            <el-option label="已取消" :value="3"></el-option>
            <el-option label="已完成" :value="4"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCampaignPage, createCampaign, updateCampaign, deleteCampaign } from '@/api/donation'
import { getCatPage } from '@/api/cat'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { isAdmin } = storeToRefs(userStore)

// 列表数据
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(6)
const loading = ref(false)

// 搜索表单（添加 keyword 字段）
const searchForm = reactive({
  status: null,
  catId: null,
  keyword: ''   // 新增关键词字段
})

// 猫咪列表（用于筛选）
const catList = ref([])
const fetchCats = async () => {
  const res = await getCatPage({ pageNum: 1, pageSize: 100 })
  if (res.code === 200) {
    catList.value = res.data.records
  }
}

// 获取数据（传递 keyword）
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    // 移除空字段（可选，但 keyword 可能为空字符串）
    if (params.keyword === '') delete params.keyword
    const res = await getCampaignPage(params)
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取活动失败')
    }
  } catch (error) {
    console.error('获取活动失败', error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}
const resetSearch = () => {
  searchForm.status = null
  searchForm.catId = null
  searchForm.keyword = ''   // 清空关键词
  handleSearch()
}

// 状态映射
const getStatusText = (status) => {
  const map = { 0: '筹备中', 1: '进行中', 2: '已结束', 3: '已取消', 4: '已完成' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'success' }
  return map[status] || 'info'
}

// 计算活动进度，优先使用后端提供的 progress，不存在时用 currentAmount/targetAmount 计算
const getCampaignProgress = (campaign) => {
  const target = Number(campaign.targetAmount || 0)
  const current = Number(campaign.currentAmount || 0)
  if (campaign.progress !== undefined && campaign.progress !== null) {
    return Number(campaign.progress) || 0
  }
  if (target <= 0) return 0
  const p = (current / target) * 100
  return p > 100 ? 100 : p
}

// 格式化进度显示
const formatProgress = (percentage) => {
  return percentage === 0 ? '0%' : percentage.toFixed(1) + '%'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()}`
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/admin/campaigns/${id}`)
}

// 创建/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  title: '',
  description: '',
  targetAmount: null,
  startDate: null,
  endDate: null,
  catId: null,
  status: 0
})
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '创建活动'
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑活动'
  Object.assign(form, row)
  // 转换日期为 Date 对象，确保当作 UTC 时间
  if (row.startDate) {
    const dateStr = row.startDate.includes('Z') ? row.startDate : row.startDate + 'Z'
    form.startDate = new Date(dateStr)
  }
  if (row.endDate) {
    const dateStr = row.endDate.includes('Z') ? row.endDate : row.endDate + 'Z'
    form.endDate = new Date(dateStr)
  }
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  form.id = null
  form.title = ''
  form.description = ''
  form.targetAmount = null
  form.startDate = null
  form.endDate = null
  form.catId = null
  form.status = 0
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateCampaign(form.id, form)
    } else {
      res = await createCampaign(form)
    }
    if (res.code === 200) {
      ElMessage.success(res.message)
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('网络错误')
  } finally {
    submitLoading.value = false
  }
}

// 删除活动
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该活动？若已有捐赠记录则无法删除', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteCampaign(id)
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
  fetchCats()
  fetchData()
})
</script>

<style scoped>
.campaign-list {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
.campaign-card {
  cursor: pointer;
  transition: all 0.3s;
}
.campaign-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
.campaign-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.campaign-title {
  font-size: 16px;
  font-weight: bold;
}
.campaign-cat {
  font-size: 14px;
  color: #409EFF;
  margin-bottom: 5px;
}
.campaign-target, .campaign-current {
  font-size: 14px;
  margin-bottom: 5px;
}
.campaign-progress {
  margin: 10px 0;
}
.campaign-time {
  font-size: 12px;
  color: #999;
}
</style>