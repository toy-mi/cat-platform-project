<template>
  <div>
    <div class="header" v-if="canManage">
      <el-button type="primary" size="small" @click="openAddDialog">添加记录</el-button>
    </div>
    <el-table :data="records" border v-loading="loading">
      <el-table-column prop="recordType" label="类型" width="100">
        <template #default="{ row }">
          {{ recordTypeMap[row.recordType] }}
        </template>
      </el-table-column>
      <el-table-column prop="recordDate" label="日期" width="120" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="vetName" label="兽医/机构" width="120" />
      <el-table-column label="操作" width="150" v-if="canManage">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteRecord(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && records.length === 0" description="暂无健康记录" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="记录类型" prop="recordType">
          <el-select v-model="form.recordType">
            <el-option label="疫苗" value="VACCINE" />
            <el-option label="绝育" value="NEUTER" />
            <el-option label="体检" value="CHECKUP" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="兽医/机构" prop="vetName">
          <el-input v-model="form.vetName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHealthRecords, addHealthRecord, updateHealthRecord, deleteHealthRecord } from '@/api/health'

const props = defineProps({
  catId: Number,
  canManage: Boolean
})

const records = ref([])
const loading = ref(false)
const recordTypeMap = { VACCINE: '疫苗', NEUTER: '绝育', CHECKUP: '体检', OTHER: '其他' }

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  recordType: '',
  recordDate: '',
  description: '',
  vetName: ''
})
const rules = {
  recordType: [{ required: true, message: '请选择记录类型', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }]
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getHealthRecords(props.catId)
    if (res.code === 200) {
      records.value = res.data
    } else {
      ElMessage.error(res.message || '获取健康记录失败')
    }
  } catch (error) {
    console.error('获取健康记录失败', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  dialogTitle.value = '新增健康记录'
  form.id = null
  form.recordType = ''
  form.recordDate = ''
  form.description = ''
  form.vetName = ''
  dialogVisible.value = true
}
const openEditDialog = (row) => {
  dialogTitle.value = '编辑健康记录'
  Object.assign(form, row)
  dialogVisible.value = true
}
const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const data = { ...form, catId: props.catId }
    let res
    if (form.id) {
      res = await updateHealthRecord(form.id, data)
    } else {
      res = await addHealthRecord(data)
    }
    if (res.code === 200) {
      ElMessage.success(res.message)
      dialogVisible.value = false
      fetchRecords()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}
const deleteRecord = (id) => {
  ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteHealthRecord(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchRecords()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.header {
  margin-bottom: 10px;
  text-align: right;
}
</style>