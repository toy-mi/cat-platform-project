<template>
  <div class="cat-list">
    <h2>猫咪信息管理</h2>

    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="猫咪姓名">
        <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable></el-input>
      </el-form-item>
      <el-form-item label="领养状态">
        <el-select v-model="searchForm.adoptionStatus" placeholder="请选择" clearable style="width: 200px;">
          <el-option label="在养" :value="0"></el-option>
          <el-option label="待领养" :value="1"></el-option>
          <el-option label="待审核" :value="2"></el-option>
          <el-option label="已领养" :value="3"></el-option>
          <el-option label="失踪" :value="4"></el-option>
          <el-option label="去世" :value="5"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
      <el-form-item style="float: right;">
        <el-button type="success" @click="openAddDialog" v-if="canManage">新增猫咪</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60"></el-table-column>
      <el-table-column prop="name" label="姓名" width="100"></el-table-column>
      <el-table-column prop="breed" label="品种" width="100"></el-table-column>
      <el-table-column prop="gender" label="性别" width="60">
        <template #default="{ row }">
          <span>{{ row.gender === 1 ? '公' : row.gender === 2 ? '母' : '未知' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="personality" label="性格" show-overflow-tooltip></el-table-column>
      <el-table-column prop="adoptionStatus" label="领养状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.adoptionStatus)">
            {{ getStatusText(row.adoptionStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDetail(row.id)">查看</el-button>
          <el-button type="warning" size="small" @click="openEditDialog(row)" v-if="canManage">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)" v-if="canManage">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchData"
        @current-change="fetchData"
    />

    <!-- 新增/编辑猫咪对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="catFormRef" :model="catForm" :rules="catRules" label-width="100px">
        <!-- 表单项内容保持不变 -->
        <el-form-item label="姓名" prop="name">
          <el-input v-model="catForm.name"></el-input>
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="catForm.breed"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="catForm.gender">
            <el-radio :label="1">公</el-radio>
            <el-radio :label="2">母</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="性格" prop="personality">
          <el-input v-model="catForm.personality" type="textarea" rows="2"></el-input>
        </el-form-item>
        <el-form-item label="特征描述" prop="description">
          <el-input v-model="catForm.description" type="textarea" rows="3"></el-input>
        </el-form-item>
        <el-form-item label="健康状况" prop="healthStatus">
          <el-input v-model="catForm.healthStatus"></el-input>
        </el-form-item>
        <el-form-item label="绝育状态" prop="neuterStatus">
          <el-select v-model="catForm.neuterStatus" placeholder="请选择">
            <el-option label="未知" :value="0"></el-option>
            <el-option label="已绝育" :value="1"></el-option>
            <el-option label="未绝育" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="领养状态" prop="adoptionStatus">
          <el-select v-model="catForm.adoptionStatus" placeholder="请选择">
            <el-option label="在养" :value="0"></el-option>
            <el-option label="待领养" :value="1"></el-option>
            <el-option label="待审核" :value="2"></el-option>
            <el-option label="已领养" :value="3"></el-option>
            <el-option label="失踪" :value="4"></el-option>
            <el-option label="去世" :value="5"></el-option>
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

    <!-- 猫咪详情对话框（带标签页） -->
    <el-dialog v-model="detailVisible" title="猫咪详情" width="700px" destroy-on-close>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <!-- 申请领养按钮：仅当猫咪为待领养时显示 -->
          <div v-if="currentCat?.adoptionStatus === 1" style="margin-bottom: 15px; text-align: right;">
            <el-button type="success" size="default" @click="openApplyDialog">申请领养</el-button>
          </div>

          <el-descriptions :column="2" border v-if="currentCat">
            <el-descriptions-item label="ID">{{ currentCat.id }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ currentCat.name }}</el-descriptions-item>
            <el-descriptions-item label="品种">{{ currentCat.breed || '无' }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ currentCat.gender === 1 ? '公' : currentCat.gender === 2 ? '母' : '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="性格">{{ currentCat.personality || '无' }}</el-descriptions-item>
            <el-descriptions-item label="特征描述">{{ currentCat.description || '无' }}</el-descriptions-item>
            <el-descriptions-item label="健康状况">{{ currentCat.healthStatus || '无' }}</el-descriptions-item>
            <el-descriptions-item label="绝育状态">
              {{ currentCat.neuterStatus === 1 ? '已绝育' : currentCat.neuterStatus === 2 ? '未绝育' : '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="领养状态">
              {{ getStatusText(currentCat.adoptionStatus) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentCat.createTime }}</el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ currentCat.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="创建人ID">{{ currentCat.creatorId || '无' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="照片墙" name="photos">
          <div class="photo-wall" v-loading="photoLoading">
            <!-- 上传按钮（仅管理员/志愿者可见） -->
            <div class="upload-btn" v-if="canManage">
              <el-upload
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  :before-upload="beforeUpload"
                  multiple
                  list-type="picture-card"
                  :file-list="uploadFileList"
                  :show-file-list="false"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </div>

            <!-- 照片列表 -->
            <div class="photo-list" v-if="photoList.length > 0">
              <div v-for="(photo, index) in photoList" :key="photo.id" class="photo-item">
                <el-image 
                  :src="photo.photoUrl" 
                  class="photo-img"
                  :preview-src-list="photoList.map(p => p.photoUrl)"
                  :initial-index="index"
                  fit="cover"
                  preview-teleported
                  lazy
                >
                </el-image>
                <!-- 删除按钮，注意阻止冒泡以免触发预览 -->
                <div class="photo-actions" v-if="canManage">
                  <el-button type="danger" size="small" circle @click.stop="handleDeletePhoto(photo.id)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>

            <el-empty v-else-if="!photoLoading" description="暂无照片" />
          </div>
        </el-tab-pane>

        <!-- 健康档案标签页 -->
        <el-tab-pane label="健康档案" name="health">
          <div class="health-records">
            <div style="margin-bottom: 10px; text-align: right;">
              <el-button type="primary" size="small" @click="openAddHealthDialog" v-if="canManage">新增记录</el-button>
            </div>
            <el-table :data="healthRecords" border size="small" v-loading="healthLoading">
              <el-table-column prop="recordType" label="类型" width="100">
                <template #default="{ row }">
                  <span>{{
                    row.recordType === 'VACCINE' ? '疫苗' :
                    row.recordType === 'NEUTER' ? '绝育' :
                    row.recordType === 'CHECKUP' ? '体检' : '其他'
                  }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="recordDate" label="日期" width="120"></el-table-column>
              <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
              <el-table-column prop="vetName" label="兽医/机构" width="120"></el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="openEditHealthDialog(row)" v-if="canManage">编辑</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteHealth(row.id)" v-if="canManage">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!healthLoading && healthRecords.length === 0" description="暂无健康记录" />
          </div>
        </el-tab-pane>

        <!-- 位置追踪标签页（嵌入地图） -->
        <el-tab-pane label="位置追踪" name="locations">
          <div class="locations">
            <!-- 新增位置按钮 -->
            <div style="margin-bottom: 10px; text-align: right;">
              <el-button type="primary" size="small" @click="openAddLocationDialog" v-if="canManage">新增地点</el-button>
            </div>

            <!-- 地图容器 -->
            <div id="location-map" style="width:100%; height:300px; margin-bottom:15px;"></div>

            <!-- 位置列表表格 -->
            <el-table :data="locationList" border size="small" v-loading="locationLoading">
              <el-table-column prop="locationDesc" label="地点描述" show-overflow-tooltip></el-table-column>
              <el-table-column prop="latitude" label="纬度" width="100"></el-table-column>
              <el-table-column prop="longitude" label="经度" width="100"></el-table-column>
              <el-table-column label="当前地点" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.isCurrent === 1 ? 'success' : 'info'">
                    {{ row.isCurrent === 1 ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="240" fixed="right">
                <template #default="{ row }">
                  <div style="display: flex; gap: 8px; flex-wrap: wrap;">
      <el-button type="primary" size="small" @click="openEditLocationDialog(row)" v-if="canManage">编辑</el-button>
      <el-button 
        v-if="row.isCurrent === 0 && canManage" 
        type="success" 
        size="small" 
        @click="handleSetCurrent(row.id)"
      >设为当前</el-button>
      <el-button type="danger" size="small" @click="handleDeleteLocation(row.id)" v-if="canManage">删除</el-button>
    </div>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!locationLoading && locationList.length === 0" description="暂无位置记录" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 新增/编辑健康记录对话框 -->
    <el-dialog v-model="healthDialogVisible" :title="healthDialogTitle" width="500px" @close="resetHealthForm">
      <el-form ref="healthFormRef" :model="healthForm" :rules="healthRules" label-width="100px">
        <el-form-item label="记录类型" prop="recordType">
          <el-select v-model="healthForm.recordType" placeholder="请选择">
            <el-option label="疫苗" value="VACCINE"></el-option>
            <el-option label="绝育" value="NEUTER"></el-option>
            <el-option label="体检" value="CHECKUP"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="healthForm.recordDate" type="date" placeholder="选择日期" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="healthForm.description" type="textarea" rows="2"></el-input>
        </el-form-item>
        <el-form-item label="兽医/机构" prop="vetName">
          <el-input v-model="healthForm.vetName"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="healthDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHealthForm" :loading="healthSubmitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增/编辑位置记录对话框（已添加地址解析+定位功能） -->
    <el-dialog v-model="locationDialogVisible" :title="locationDialogTitle" width="500px" @close="resetLocationForm">
      <el-form ref="locationFormRef" :model="locationForm" :rules="locationRules" label-width="100px">
        <!-- 地址输入行，增加定位按钮 -->
        <el-form-item label="地址">
          <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <el-input v-model="addressInput" placeholder="输入地点名称或使用定位" style="flex: 1;"></el-input>
            <el-button type="primary" @click="handleAddressResolve" :loading="addressResolving">解析</el-button>
            <el-button type="info" @click="handleGetCurrentLocation" :loading="geoLoading">定位</el-button>
          </div>
        </el-form-item>

        <el-form-item label="地点描述" prop="locationDesc">
          <el-input v-model="locationForm.locationDesc" placeholder="例如：图书馆后花园"></el-input>
        </el-form-item>

        <el-form-item label="纬度" prop="latitude">
          <el-input v-model="locationForm.latitude" placeholder="点击解析或定位自动填充"></el-input>
        </el-form-item>

        <el-form-item label="经度" prop="longitude">
          <el-input v-model="locationForm.longitude" placeholder="点击解析或定位自动填充"></el-input>
        </el-form-item>

        <el-form-item label="设为当前" prop="isCurrent">
          <el-switch v-model="locationForm.isCurrent" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="locationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLocationForm" :loading="locationSubmitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 领养申请表单组件 -->
    <AdoptionApplyForm ref="applyFormRef" :cat-id="currentCat?.id" @success="handleApplySuccess" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import {
  getCatPage, addCat, updateCat, deleteCat, getCatById,
  uploadCatPhoto, getCatPhotoList, deleteCatPhoto
} from '@/api/cat'
import {
  getCatLocations, addCatLocation, updateCatLocation, deleteCatLocation, setCurrentLocation
} from '@/api/location'
import {
  getHealthRecords, addHealthRecord, updateHealthRecord, deleteHealthRecord
} from '@/api/health'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

// 引入领养申请组件
import AdoptionApplyForm from '@/components/AdoptionApplyForm.vue'

const userStore = useUserStore()
const { isAdmin, isVolunteer } = storeToRefs(userStore)
const canManage = computed(() => isAdmin.value || isVolunteer.value)

// 表格数据
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  adoptionStatus: null
})

// 详情弹窗相关
const detailVisible = ref(false)
const currentCat = ref(null)
const activeTab = ref('info')
const photoList = ref([])
const photoLoading = ref(false)
const uploadFileList = ref([])

// 健康档案相关
const healthRecords = ref([])
const healthLoading = ref(false)
const healthDialogVisible = ref(false)
const healthDialogTitle = ref('')
const healthSubmitLoading = ref(false)
const healthFormRef = ref()
const healthForm = reactive({
  id: null,
  recordType: '',
  recordDate: '',
  description: '',
  vetName: ''
})
const healthRules = {
  recordType: [{ required: true, message: '请选择记录类型', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }]
}

// 位置追踪相关
const locationList = ref([])
const locationLoading = ref(false)
const locationDialogVisible = ref(false)
const locationDialogTitle = ref('')
const locationSubmitLoading = ref(false)
const locationFormRef = ref()
const locationForm = reactive({
  id: null,
  locationDesc: '',
  latitude: '',
  longitude: '',
  isCurrent: 0
})
const locationRules = {
  locationDesc: [{ required: true, message: '请输入地点描述', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }]
}

// 高德地图相关
let map = null
const mapInitialized = ref(false)

// 地址解析相关
const addressInput = ref('')
const addressResolving = ref(false)

// 定位相关
const geoLoading = ref(false)

// 领养申请相关
const applyFormRef = ref()

// 上传地址
const uploadAction = computed(() => {
  return `http://localhost:8080/api/cat-photos/upload/${currentCat.value?.id}`
})

// 上传请求头
const uploadHeaders = computed(() => {
  return {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})

// 获取猫咪列表
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await getCatPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取猫咪列表失败', error)
    ElMessage.error('网络错误，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.adoptionStatus = null
  handleSearch()
}

const getStatusText = (status) => {
  const map = {
    0: '在养',
    1: '待领养',
    2: '待审核',
    3: '已领养',
    4: '失踪',
    5: '去世'
  }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = {
    0: 'success',
    1: 'warning',
    2: 'info',
    3: 'info',
    4: 'danger',
    5: 'danger'
  }
  return map[status] || 'info'
}

// 新增/编辑猫咪对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const catFormRef = ref()
const catForm = reactive({
  id: null,
  name: '',
  breed: '',
  gender: 0,
  personality: '',
  description: '',
  healthStatus: '',
  neuterStatus: 0,
  adoptionStatus: 0
})

const catRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  adoptionStatus: [{ required: true, message: '请选择领养状态', trigger: 'change' }]
}

const openAddDialog = () => {
  dialogTitle.value = '新增猫咪'
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑猫咪'
  Object.assign(catForm, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  await catFormRef.value?.validate()
  submitLoading.value = true
  try {
    let res
    if (catForm.id) {
      res = await updateCat(catForm.id, catForm)
    } else {
      res = await addCat(catForm)
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
    if (error.response && error.response.status === 403) {
      ElMessage.error('对不起，您没有权限')
    } else {
      ElMessage.error('提交失败，请检查网络')
    }
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  if (catFormRef.value) {
    catFormRef.value.resetFields()
  }
  Object.keys(catForm).forEach(key => {
    catForm[key] = key === 'id' ? null : ''
  })
  catForm.gender = 0
  catForm.neuterStatus = 0
  catForm.adoptionStatus = 0
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该猫咪吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await deleteCat(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  }).catch(() => {})
}

// 照片墙方法
const loadPhotos = async (catId) => {
  photoLoading.value = true
  try {
    const res = await getCatPhotoList(catId)
    if (res.code === 200) {
      photoList.value = res.data
    } else {
      ElMessage.error(res.message || '获取照片列表失败')
    }
  } catch (error) {
    console.error('获取照片列表失败', error)
    ElMessage.error('网络错误')
  } finally {
    photoLoading.value = false
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    loadPhotos(currentCat.value.id)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

const handleDeletePhoto = (photoId) => {
  ElMessageBox.confirm('确认删除该照片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteCatPhoto(photoId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadPhotos(currentCat.value.id)
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除照片失败', error)
      ElMessage.error('网络错误')
    }
  }).catch(() => {})
}

// 健康档案方法
const loadHealthRecords = async (catId) => {
  healthLoading.value = true
  try {
    const res = await getHealthRecords(catId)
    if (res.code === 200) {
      healthRecords.value = res.data
    } else {
      ElMessage.error(res.message || '获取健康记录失败')
    }
  } catch (error) {
    console.error('获取健康记录失败', error)
  } finally {
    healthLoading.value = false
  }
}

const openAddHealthDialog = () => {
  healthDialogTitle.value = '新增健康记录'
  healthDialogVisible.value = true
}

const openEditHealthDialog = (row) => {
  healthDialogTitle.value = '编辑健康记录'
  Object.assign(healthForm, row)
  healthDialogVisible.value = true
}

const resetHealthForm = () => {
  healthFormRef.value?.resetFields()
  Object.assign(healthForm, {
    id: null,
    recordType: '',
    recordDate: '',
    description: '',
    vetName: ''
  })
}

const submitHealthForm = async () => {
  await healthFormRef.value?.validate()
  healthSubmitLoading.value = true
  try {
    const data = { ...healthForm, catId: currentCat.value.id }
    let res
    if (healthForm.id) {
      res = await updateHealthRecord(healthForm.id, data)
    } else {
      res = await addHealthRecord(data)
    }
    if (res.code === 200) {
      ElMessage.success(res.message)
      healthDialogVisible.value = false
      await loadHealthRecords(currentCat.value.id)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交健康记录失败', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    healthSubmitLoading.value = false
  }
}

const handleDeleteHealth = (id) => {
  ElMessageBox.confirm('确认删除该健康记录吗？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteHealthRecord(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await loadHealthRecords(currentCat.value.id)
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

// 地图初始化（优化版）
const initMap = async () => {
  if (mapInitialized.value) return
  const container = document.getElementById('location-map')
  if (!container) {
    console.warn('地图容器不存在，稍后重试')
    return
  }
  try {
    const AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY,
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_KEY,
      version: '2.0',
      plugins: ['AMap.Geocoder']
    })
    map = new AMap.Map('location-map', {
      zoom: 12,
      center: [116.397428, 39.90923],
      viewMode: '2D'
    })
    mapInitialized.value = true
  } catch (error) {
    console.error('地图加载失败', error)
  }
}

// 在地图上添加标记
const addMarkers = () => {
  if (!map || !locationList.value.length) return
  map.clearMap()
  const markers = []
  locationList.value.forEach(loc => {
    const lng = parseFloat(loc.longitude)
    const lat = parseFloat(loc.latitude)
    if (isNaN(lng) || isNaN(lat)) return
    const marker = new AMap.Marker({
      position: [lng, lat],
      title: loc.locationDesc,
      icon: loc.isCurrent === 1 ? '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png' : undefined
    })
    marker.setMap(map)
    markers.push(marker)
    if (loc.isCurrent === 1) {
      map.setCenter([lng, lat])
    } else if (markers.length === 1 && !locationList.value.some(l => l.isCurrent === 1)) {
      map.setCenter([lng, lat])
    }
  })
  if (markers.length > 1) {
    const bounds = new AMap.Bounds()
    markers.forEach(m => bounds.extend(m.getPosition()))
    map.setBounds(bounds)
  }
}

// 位置追踪方法（只负责获取数据）
const loadLocations = async (catId) => {
  locationLoading.value = true
  try {
    const res = await getCatLocations(catId)
    if (res.code === 200) {
      locationList.value = res.data
    } else {
      ElMessage.error(res.message || '获取位置记录失败')
    }
  } catch (error) {
    console.error('获取位置记录失败', error)
  } finally {
    locationLoading.value = false
  }
}

const openAddLocationDialog = () => {
  locationDialogTitle.value = '新增位置'
  locationDialogVisible.value = true
}

const openEditLocationDialog = (row) => {
  locationDialogTitle.value = '编辑位置'
  Object.assign(locationForm, row)
  locationDialogVisible.value = true
}

// 重置位置表单
const resetLocationForm = () => {
  locationFormRef.value?.resetFields()
  Object.assign(locationForm, {
    id: null,
    locationDesc: '',
    latitude: '',
    longitude: '',
    isCurrent: 0
  })
  addressInput.value = ''
}

const submitLocationForm = async () => {
  await locationFormRef.value?.validate()
  locationSubmitLoading.value = true
  try {
    const data = { ...locationForm, catId: currentCat.value.id }
    let res
    if (locationForm.id) {
      res = await updateCatLocation(locationForm.id, data)
    } else {
      res = await addCatLocation(data)
    }
    if (res.code === 200) {
      ElMessage.success(res.message)
      locationDialogVisible.value = false
      await loadLocations(currentCat.value.id)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交位置记录失败', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    locationSubmitLoading.value = false
  }
}

const handleDeleteLocation = (id) => {
  ElMessageBox.confirm('确认删除该位置记录吗？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteCatLocation(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await loadLocations(currentCat.value.id)
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

const handleSetCurrent = (id) => {
  ElMessageBox.confirm('确认将该地点设为当前常出没点吗？', '提示', { type: 'info' })
    .then(async () => {
      const res = await setCurrentLocation(id)
      if (res.code === 200) {
        ElMessage.success('设置成功')
        await loadLocations(currentCat.value.id)
      } else {
        ElMessage.error(res.message || '设置失败')
      }
    })
    .catch(() => {})
}

// 地址解析函数
const handleAddressResolve = async () => {
  if (!addressInput.value) {
    ElMessage.warning('请输入地址')
    return
  }
  if (!window.AMap) {
    ElMessage.warning('地图服务未加载，请稍后重试')
    return
  }
  addressResolving.value = true
  try {
    const location = await new Promise((resolve, reject) => {
      const geocoder = new AMap.Geocoder({})
      geocoder.getLocation(addressInput.value, (status, result) => {
        if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
          resolve(result.geocodes[0].location)
        } else {
          reject(new Error('地址解析失败，请尝试更详细的位置'))
        }
      })
    })
    locationForm.latitude = location.lat
    locationForm.longitude = location.lng
    if (!locationForm.locationDesc) {
      locationForm.locationDesc = addressInput.value
    }
    ElMessage.success('解析成功')
  } catch (error) {
    console.error('地址解析失败', error)
    ElMessage.error(error.message || '解析失败，请重试')
  } finally {
    addressResolving.value = false
  }
}

// 获取当前位置（优化版）
const handleGetCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持地理位置')
    return
  }
  geoLoading.value = true
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { latitude, longitude } = position.coords
      locationForm.latitude = latitude
      locationForm.longitude = longitude

      // 尝试逆地理编码
      if (window.AMap) {
        try {
          const geocoder = new AMap.Geocoder({})
          // 使用 Promise 包装回调，获取详细地址
          const address = await new Promise((resolve, reject) => {
            geocoder.getAddress([longitude, latitude], (status, result) => {
              if (status === 'complete' && result.regeocode) {
                resolve(result.regeocode.formattedAddress)
              } else {
                reject(new Error(`逆地理编码失败，状态：${status}`))
              }
            })
          })
          // 成功：填充地址
          if (!locationForm.locationDesc) {
            locationForm.locationDesc = address
          }
          addressInput.value = address
          ElMessage.success('定位成功，已获取地址')
        } catch (error) {
          console.error('逆地理编码详细错误：', error)
          ElMessage.warning('无法获取详细地址，请手动输入')
          // 即使逆地理失败，经纬度已填充，可以继续
        }
      } else {
        ElMessage.warning('地图服务未准备好，仅填充经纬度')
      }

      geoLoading.value = false
    },
    (error) => {
      console.error('定位失败', error)
      geoLoading.value = false
      let msg = '定位失败'
      switch (error.code) {
        case 1: msg = '用户拒绝了位置权限'; break
        case 2: msg = '无法获取位置，请检查网络或定位设置'; break
        case 3: msg = '定位超时，请稍后重试'; break
        default: msg = '未知错误'
      }
      ElMessage.error(msg)
      ElMessage.info('您可以手动输入地址并使用“解析”按钮')
    },
    {
      enableHighAccuracy: false,
      timeout: 20000,
      maximumAge: 0
    }
  )
}

// 领养申请相关方法
const openApplyDialog = () => {
  applyFormRef.value?.open()
}

const handleApplySuccess = () => {
  // 可以在这里刷新数据或提示用户，例如刷新猫咪状态或显示成功消息
  // ElMessage.success('申请提交成功')
  // 可选：重新加载猫咪详情以更新状态（如果后端修改了状态）
  if (currentCat.value) {
    loadLocations(currentCat.value.id) // 示例，实际可能需要重新获取猫咪基本信息
  }
}

// 查看详情
const openDetail = async (id) => {
  try {
    const res = await getCatById(id)
    if (res.code === 200) {
      currentCat.value = res.data
      detailVisible.value = true
      activeTab.value = 'info'
      // 地图销毁已在 watch(detailVisible) 中处理，这里不再手动销毁
      await Promise.all([
        loadPhotos(id),
        loadHealthRecords(id),
        loadLocations(id)
      ])
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取猫咪详情失败', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 监听详情弹窗关闭，销毁地图
watch(detailVisible, (val) => {
  if (!val && map) {
    map.destroy()
    map = null
    mapInitialized.value = false
  }
})

// 监听标签页切换，初始化地图并添加标记
watch(activeTab, async (newTab) => {
  if (newTab === 'locations' && currentCat.value) {
    await nextTick()
    if (!mapInitialized.value) {
      await initMap()
    }
    addMarkers()
  }
})

// 监听 locationList 变化，更新地图标记
watch(locationList, () => {
  if (mapInitialized.value) {
    addMarkers()
  }
}, { deep: true })

// 组件卸载时销毁地图
onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
    mapInitialized.value = false
  }
})

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.cat-list {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
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
  width: 300px;
  height: 300px;
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
  position: absolute;
  top: 5px;
  right: 5px;
}
.health-records {
  max-height: 400px;
  overflow-y: auto;
}
.upload-btn .el-upload--picture-card {
  width: 100px;
  height: 100px;
  line-height: 100px;
}
.photo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>