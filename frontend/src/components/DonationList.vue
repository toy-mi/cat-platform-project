<template>
  <div>
    <!-- 筛选栏（仅管理员可见） -->
    <div v-if="showAudit" class="filter-bar" style="margin-bottom: 15px;">
      <el-input v-model="filters.userId" placeholder="捐赠人ID" clearable style="width:150px"></el-input>
      <el-date-picker
        v-model="filters.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width:260px; margin:0 10px"
      />
      <el-button type="primary" @click="applyFilters">筛选</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table :data="list" border size="small" v-loading="loading">
      <el-table-column prop="userName" label="捐赠人" width="100"></el-table-column>
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
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <!-- 凭证列 -->
      <el-table-column label="凭证" width="80">
        <template #default="{ row }">
          <el-button v-if="row.attachmentUrl" type="primary" link @click="previewImage(row.attachmentUrl)">查看</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" v-if="showAudit">
        <template #default="{ row }">
          <el-button 
            v-if="row.status === 0" 
            size="small" 
            type="success" 
            @click="openAuditDialog(row, 1)"
          >通过</el-button>
          <el-button 
            v-if="row.status === 0" 
            size="small" 
            type="danger" 
            @click="openAuditDialog(row, 2)"
          >拒绝</el-button>
          <el-button 
            v-if="row.status !== 0" 
            size="small" 
            type="info" 
            disabled
          >已审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-if="total > pageSize"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchData"
    />

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="审核捐赠" width="400px">
      <el-form ref="auditFormRef" :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="auditForm.remark" type="textarea" rows="2"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="auditLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getCampaignDonations, auditDonation } from '@/api/donation'

const props = defineProps({
  campaignId: [String, Number],
  status: Number,          // 筛选状态，不传则全部
  showAudit: Boolean       // 是否显示审核按钮和筛选栏
})
const emit = defineEmits(['refresh'])

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 筛选条件
const filters = reactive({
  userId: null,
  dateRange: null
})

// 应用筛选
const applyFilters = () => {
  pageNum.value = 1
  fetchData()
}

// 重置筛选
const resetFilters = () => {
  filters.userId = null
  filters.dateRange = null
  applyFilters()
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    // 状态筛选
    if (props.status !== undefined) {
      params.status = props.status
    }
    // 添加筛选条件
    if (filters.userId) params.userId = filters.userId
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startTime = filters.dateRange[0]
      params.endTime = filters.dateRange[1]
    }
    const res = await getCampaignDonations(props.campaignId, params)
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取捐赠记录失败')
    }
  } catch (error) {
    console.error('获取捐赠记录失败', error)
  } finally {
    loading.value = false
  }
}

// 状态映射
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}
const getStatusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

// 监听状态变化（外部传入的status）
watch(() => props.status, () => {
  pageNum.value = 1
  fetchData()
}, { immediate: true })

// 审核相关
const auditVisible = ref(false)
const auditLoading = ref(false)
const auditFormRef = ref()
const auditForm = reactive({
  id: null,
  status: 1,
  remark: ''
})
const openAuditDialog = (row, defaultStatus) => {
  auditForm.id = row.id
  auditForm.status = defaultStatus
  auditForm.remark = ''
  auditVisible.value = true
}
const submitAudit = async () => {
  auditLoading.value = true
  try {
    const res = await auditDonation(auditForm.id, {
      status: auditForm.status,
      remark: auditForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('审核成功')
      auditVisible.value = false
      fetchData()
      emit('refresh')  // 通知父组件刷新（如更新活动金额）
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败', error)
    ElMessage.error('网络错误')
  } finally {
    auditLoading.value = false
  }
}

// 预览凭证图片
const previewImage = (url) => {
  window.open(url, '_blank')
}

// 暴露刷新方法给父组件
defineExpose({
  fetchData
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>