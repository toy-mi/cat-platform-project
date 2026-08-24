<template>
  <div class="adoption-process">
    <div class="container">
      <h1 class="page-title">领养流程</h1>
      <p class="page-subtitle">简单几步，带猫咪回家</p>

      <!-- 申请选择器（若未指定id，显示下拉选择） -->
      <div class="application-selector" v-if="!applicationId && adoptionsList.length > 0">
        <el-select v-model="selectedAppId" placeholder="选择要查看的申请" @change="loadApplication" style="width:300px">
          <el-option
            v-for="app in adoptionsList"
            :key="app.id"
            :label="`申请ID ${app.id} - ${app.catName} (${getStatusText(app.status)})`"
            :value="app.id"
          />
        </el-select>
      </div>
      <div v-else-if="!applicationId && adoptionsList.length === 0" class="no-application">
        <el-empty>
          <template #description>
            <p>暂无领养申请，快去申请一只猫咪吧！</p>
            <p class="guide">点击"查看猫咪"按钮，在猫咪图鉴搜索栏的领养状态里选择"待领养"搜索，点击心仪的猫咪就可以进行领养申请啦！</p>
          </template>
          <!-- <el-button type="primary" @click="goToCats">查看猫咪</el-button> -->
        </el-empty>
      </div>

      <!-- 步骤条（添加 key 强制刷新） -->
      <el-steps
        v-if="currentApplication"
        :key="currentStep"
        :active="currentStep"
        finish-status="success"
        align-center
        class="steps"
      >
        <el-step title="提交申请" description="填写领养申请表" />
        <el-step title="志愿者初审" description="审核申请材料" />
        <el-step title="线下回访" description="志愿者上门回访" />
        <el-step title="管理员终审" description="最终审核确认" />
        <el-step title="签订协议" description="签署领养协议" />
        <el-step title="领养完成" description="带猫咪回家" />
      </el-steps>

      <!-- 失败/取消提示 -->
      <div v-if="isFailedOrCancelled" class="warning-message">
        <el-alert :title="failMessage" type="error" show-icon :closable="false" />
      </div>

      <div class="action">
        <el-button type="primary" size="large" round @click="goToCats">查看猫咪</el-button>
        <el-button size="large" round @click="goToMyAdoptions">我的领养</el-button>
      </div>

      <div class="tips">
        <h3>领养须知</h3>
        <ul>
          <li>领养人需年满18周岁，有固定住所和稳定收入。</li>
          <li>家中需有安全防护措施（如纱窗、封阳台），防止猫咪走失或坠楼。</li>
          <li>愿意接受志愿者回访，定期反馈猫咪近况。</li>
          <li>领养后不得随意遗弃，如有困难可联系我们退回。</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdoptionPage, getAdoptionDetail } from '@/api/adoption'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn, userInfo } = storeToRefs(userStore)

// 状态映射（展示文本）
const getStatusText = (status) => {
  const map = {
    0: '待初审', 1: '初审通过', 2: '初审拒绝', 3: '待回访',
    4: '回访通过', 5: '回访失败', 6: '待终审', 7: '终审通过',
    8: '终审拒绝', 9: '已签订协议', 10: '已完成领养', 11: '已取消'
  }
  return map[status] || '未知'
}

// 步骤条索引映射（0~5）- 活跃步骤表示当前正在进行的阶段，完成时设为6以高亮所有步骤
const getStepByStatus = (status) => {
  const s = Number(status)
  switch (s) {
    case 0: return 1       // 待初审 → 志愿者初审（当前进行）
    case 1: return 2       // 初审通过 → 线下回访（当前进行）
    case 2: return 1       // 初审拒绝 → 志愿者初审（失败，停留在该步）
    case 3: return 2       // 待回访 → 线下回访（当前进行）
    case 4: return 4       // 回访通过 → 签订协议（当前进行）
    case 5: return 2       // 回访失败 → 线下回访（失败，停留在该步）
    case 6: return 3       // 待终审 → 管理员终审（当前进行）
    case 7: return 5       // 终审通过 → 领养完成（当前进行）
    case 8: return 3       // 终审拒绝 → 管理员终审（失败，停留在该步）
    case 9: return 5       // 已签订协议 → 领养完成
    case 10: return 6      // 已完成领养 → 高亮所有步骤（完成状态）
    case 11: return 0      // 已取消 → 提交申请
    default: return 0
  }
}

// 判断是否为失败/取消状态
const isFailedOrCancelled = computed(() => {
  if (!currentApplication.value) return false
  const status = currentApplication.value.status
  return [2, 5, 8, 11].includes(status)
})

// 失败提示信息
const failMessage = computed(() => {
  if (!currentApplication.value) return ''
  const status = currentApplication.value.status
  if (status === 2) return '初审未通过，请查看原因后重新申请。'
  if (status === 5) return '回访未通过，可联系志愿者了解详情。'
  if (status === 8) return '终审未通过，请联系管理员。'
  if (status === 11) return '该申请已被取消。'
  return ''
})

// 数据
const currentApplication = ref(null)
const adoptionsList = ref([])
const selectedAppId = ref(null)
const applicationId = ref(route.query.id ? parseInt(route.query.id) : null)

// 当前步骤
const currentStep = ref(0)

// 加载指定申请详情
const loadApplication = async (id) => {
  if (!id) return
  try {
    const res = await getAdoptionDetail(id)
    if (res.code === 200) {
      currentApplication.value = res.data
      const step = getStepByStatus(res.data.status)
      currentStep.value = step
      console.log('状态码:', res.data.status, '步骤索引:', step)
    } else {
      ElMessage.error(res.message || '获取申请详情失败')
      currentApplication.value = null
    }
  } catch (error) {
    console.error('加载申请失败', error)
    ElMessage.error('网络错误')
  }
}

// 加载当前用户的所有申请（用于下拉选择）
const fetchMyAdoptions = async () => {
  if (!userInfo.value) return
  try {
    const res = await getAdoptionPage({
      pageNum: 1,
      pageSize: 100,
      userId: userInfo.value.id
    })
    if (res.code === 200) {
      adoptionsList.value = res.data.records
      // 如果路由传入了id，直接加载该申请；否则如果有申请且未选择，默认选中第一个
      if (applicationId.value) {
        loadApplication(applicationId.value)
      } else if (adoptionsList.value.length > 0 && !selectedAppId.value) {
        selectedAppId.value = adoptionsList.value[0].id
        loadApplication(selectedAppId.value)
      }
    } else {
      ElMessage.error(res.message || '获取申请列表失败')
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
  }
}

// 跳转
const goToCats = () => router.push('/cats')
const goToMyAdoptions = () => router.push('/profile?tab=adoptions')

onMounted(() => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录查看领养进度')
    router.push('/login')
    return
  }
  fetchMyAdoptions()
})
</script>

<style scoped>
/* 样式保持不变（略） */
.adoption-process {
  padding: 2rem 0;
  background: linear-gradient(135deg, #f4fafc 0%, #ffffff 100%);
  min-height: 100vh;
}
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}
.page-title {
  font-size: 2.2rem;
  font-weight: 700;
  text-align: center;
  color: #0f172a;
  margin-bottom: 0.5rem;
}
.page-subtitle {
  text-align: center;
  color: #475569;
  font-size: 1rem;
  margin-bottom: 2rem;
}
.application-selector {
  text-align: center;
  margin-bottom: 1.5rem;
}
.no-application {
  margin: 2rem 0;
}
.application-info {
  margin-bottom: 2rem;
}
.custom-descriptions {
  background: white;
  border-radius: 16px;
  overflow: hidden;
}
.steps {
  margin-bottom: 2rem;
  background: white;
  padding: 1.5rem 1rem;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.warning-message {
  margin: 1rem 0;
  text-align: center;
}
.action {
  text-align: center;
  margin: 2rem 0 3rem;
}
.action .el-button {
  padding: 10px 28px;
  font-weight: 500;
  transition: all 0.2s;
}
.action .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.1);
}
.tips {
  background: white;
  padding: 1.5rem 2rem;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  backdrop-filter: blur(4px);
}
.tips h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1.2rem;
  color: #0f172a;
}
.guide {
  margin-top: 10px;
  color: #666;
  font-size: 0.9rem;
}
.tips ul {
  padding-left: 1.2rem;
  color: #475569;
  line-height: 1.6;
}
</style>