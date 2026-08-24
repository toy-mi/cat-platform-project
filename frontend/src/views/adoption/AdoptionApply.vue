<template>
  <div class="adoption-apply" v-loading="loading">
    <div class="container">
      <el-page-header @back="goBack" content="申请领养" />

      <el-card class="apply-card" v-if="cat">
        <h2>申请领养猫咪：{{ cat.name }}</h2>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="品种">{{ cat.breed || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ cat.gender === 1 ? '公' : cat.gender === 2 ? '母' : '未知' }}</el-descriptions-item>
        </el-descriptions>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="apply-form">
          <el-form-item label="住房类型" prop="houseType">
            <el-radio-group v-model="form.houseType">
              <el-radio label="自有住房">自有住房</el-radio>
              <el-radio label="租房">租房</el-radio>
              <el-radio label="其他">其他</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="家庭成员" prop="familyMembers">
            <el-input-number v-model="form.familyMembers" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="是否有小孩" prop="hasChildren">
            <el-radio-group v-model="form.hasChildren">
              <el-radio :label="true">有</el-radio>
              <el-radio :label="false">无</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="养宠经验" prop="petExperience">
            <el-input v-model="form.petExperience" type="textarea" rows="3" placeholder="请描述您的养宠经验" />
          </el-form-item>
          <el-form-item label="其他说明" prop="other">
            <el-input v-model="form.other" type="textarea" rows="2" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting">提交申请</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-empty v-else-if="!loading" description="猫咪不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCatById } from '@/api/cat'
import { applyAdoption } from '@/api/adoption'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn } = storeToRefs(userStore)

const catId = route.query.catId
const cat = ref(null)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  houseType: '自有住房',
  familyMembers: 2,
  hasChildren: false,
  petExperience: '',
  other: ''
})

const rules = {
  houseType: [{ required: true, message: '请选择住房类型', trigger: 'change' }],
  familyMembers: [{ required: true, message: '请输入家庭成员数', trigger: 'blur' }],
  hasChildren: [{ required: true, message: '请选择是否有小孩', trigger: 'change' }]
}

const fetchCat = async () => {
  if (!catId) {
    ElMessage.error('请选择要领养的猫咪')
    router.push('/cats')
    return
  }
  loading.value = true
  try {
    const res = await getCatById(catId)
    if (res.code === 200) {
      cat.value = res.data
      if (cat.value.adoptionStatus !== 1) {
        ElMessage.warning('该猫咪暂不可领养')
        router.push('/cats')
      }
    } else {
      ElMessage.error('获取猫咪信息失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  // 表单验证
  try {
    await formRef.value?.validate()
  } catch (error) {
    return // 验证失败
  }
  submitting.value = true
  try {
    const applicationData = JSON.stringify(form)
    const res = await applyAdoption({ catId: cat.value.id, applicationData })
    if (res.code === 200) {
      ElMessage.success('申请提交成功，请耐心等待审核')
      // 跳转到个人中心，并激活“我的领养”标签页
      router.push('/profile?tab=adoptions')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchCat()
})
</script>

<style scoped>
.adoption-apply {
  padding: 2rem 0;
  background: #f8fafc;
  min-height: 100vh;
}
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}
.apply-card {
  margin-top: 20px;
}
.apply-form {
  margin-top: 20px;
}
</style>