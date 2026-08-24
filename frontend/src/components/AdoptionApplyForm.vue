<template>
  <el-dialog v-model="visible" title="申请领养" width="500px" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="住房类型" prop="houseType">
        <el-radio-group v-model="form.houseType">
          <el-radio label="自有住房">自有住房</el-radio>
          <el-radio label="租房">租房</el-radio>
          <el-radio label="其他">其他</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="家庭成员" prop="familyMembers">
        <el-input-number v-model="form.familyMembers" :min="1" :max="10"></el-input-number>
      </el-form-item>
      <el-form-item label="是否有小孩" prop="hasChildren">
        <el-radio-group v-model="form.hasChildren">
          <el-radio :label="true">有</el-radio>
          <el-radio :label="false">无</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="养宠经验" prop="petExperience">
        <el-input v-model="form.petExperience" type="textarea" rows="3" placeholder="请描述您的养宠经验"></el-input>
      </el-form-item>
      <el-form-item label="其他说明" prop="other">
        <el-input v-model="form.other" type="textarea" rows="2"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">提交申请</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { applyAdoption } from '@/api/adoption'

const props = defineProps({
  catId: {
    type: Number,
    required: true
  }
})
const emit = defineEmits(['success'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref()
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

const open = () => {
  visible.value = true
}
const handleClose = () => {
  formRef.value?.resetFields()
}
const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const applicationData = JSON.stringify(form)
    const res = await applyAdoption({
      catId: props.catId,
      applicationData
    })
    if (res.code === 200) {
      ElMessage.success('申请提交成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('申请提交失败', error)
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>