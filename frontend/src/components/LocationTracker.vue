<template>
  <div>
    <div class="header" v-if="canManage">
      <el-button type="primary" size="small" @click="openAddDialog">添加地点</el-button>
    </div>

    <!-- 地图容器 -->
    <div id="location-map" style="width:100%; height:300px; margin-bottom:15px;"></div>

    <el-table :data="locations" border v-loading="loading">
      <el-table-column prop="locationDesc" label="地点描述" show-overflow-tooltip />
      <el-table-column prop="latitude" label="纬度" width="100" />
      <el-table-column prop="longitude" label="经度" width="100" />
      <el-table-column label="当前地点" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isCurrent === 1 ? 'success' : 'info'">
            {{ row.isCurrent === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" v-if="canManage">
        <template #default="{ row }">
          <div style="display: flex; gap: 8px;">
            <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="row.isCurrent === 0"
              type="success"
              size="small"
              @click="setCurrent(row.id)"
            >设为当前</el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteLocation(row.id)"
            >删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && locations.length === 0" description="暂无位置记录" />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetLocationForm">
      <el-form ref="locationFormRef" :model="locationForm" :rules="locationRules" label-width="100px">
        <el-form-item label="地址">
          <div style="display: flex; gap: 8px;">
            <el-input v-model="addressInput" placeholder="输入地点名称或使用定位" style="flex: 1;" />
            <el-button type="primary" @click="handleAddressResolve" :loading="addressResolving">解析</el-button>
            <el-button type="info" @click="handleGetCurrentLocation" :loading="geoLoading">定位</el-button>
          </div>
        </el-form-item>
        <el-form-item label="地点描述" prop="locationDesc">
          <el-input v-model="locationForm.locationDesc" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model="locationForm.latitude" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model="locationForm.longitude" />
        </el-form-item>
        <el-form-item label="设为当前" prop="isCurrent">
          <el-switch v-model="locationForm.isCurrent" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLocationForm" :loading="locationSubmitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, onUnmounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AMapLoader from '@amap/amap-jsapi-loader'
import {
  getCatLocations,
  addCatLocation,
  updateCatLocation,
  deleteCatLocation,
  setCurrentLocation
} from '@/api/location'

const props = defineProps({
  catId: Number,
  canManage: Boolean
})

// 位置列表数据
const locations = ref([])
const loading = ref(false)

// 高德地图相关
let map = null
const mapInitialized = ref(false)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
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

// 地址解析与定位
const addressInput = ref('')
const addressResolving = ref(false)
const geoLoading = ref(false)

// ---------- 获取位置列表 ----------
const loadLocations = async () => {
  loading.value = true
  try {
    const res = await getCatLocations(props.catId)
    if (res.code === 200) {
      locations.value = res.data
    } else {
      ElMessage.error(res.message || '获取位置记录失败')
    }
  } catch (error) {
    console.error('获取位置记录失败', error)
  } finally {
    loading.value = false
  }
}

// ---------- 地图初始化 ----------
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
    addMarkers()
  } catch (error) {
    console.error('地图加载失败', error)
    ElMessage.error('地图加载失败，请检查网络或 Key 配置')
  }
}

// 添加标记
const addMarkers = () => {
  if (!map || !locations.value.length) return
  map.clearMap()
  const markers = []
  locations.value.forEach(loc => {
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
    } else if (markers.length === 1 && !locations.value.some(l => l.isCurrent === 1)) {
      map.setCenter([lng, lat])
    }
  })
  if (markers.length > 1) {
    const bounds = new AMap.Bounds()
    markers.forEach(m => bounds.extend(m.getPosition()))
    map.setBounds(bounds)
  }
}

// 监听位置变化，更新地图
watch(locations, () => {
  if (mapInitialized.value) addMarkers()
}, { deep: true })

// 暴露方法供父组件调用
const checkMap = () => {
  if (!mapInitialized.value) {
    initMap()
  } else {
    addMarkers()
  }
}
defineExpose({ checkMap })

// ---------- 删除地点（关键方法） ----------
const deleteLocation = async (id) => {
  ElMessageBox.confirm('确认删除该地点？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await deleteCatLocation(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await loadLocations()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
    .catch(() => {})
}

// ---------- 其他方法（编辑、新增、设为当前等） ----------
const openAddDialog = () => {
  dialogTitle.value = '新增地点'
  locationForm.id = null
  locationForm.locationDesc = ''
  locationForm.latitude = ''
  locationForm.longitude = ''
  locationForm.isCurrent = 0
  addressInput.value = ''
  dialogVisible.value = true
}
const openEditDialog = (row) => {
  dialogTitle.value = '编辑地点'
  Object.assign(locationForm, row)
  addressInput.value = row.locationDesc
  dialogVisible.value = true
}
const resetLocationForm = () => {
  locationFormRef.value?.resetFields()
}
const submitLocationForm = async () => {
  await locationFormRef.value?.validate()
  locationSubmitLoading.value = true
  try {
    const data = { ...locationForm, catId: props.catId }
    let res
    if (locationForm.id) {
      res = await updateCatLocation(locationForm.id, data)
    } else {
      res = await addCatLocation(data)
    }
    if (res.code === 200) {
      ElMessage.success(res.message)
      dialogVisible.value = false
      loadLocations()
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
const setCurrent = async (id) => {
  const res = await setCurrentLocation(id)
  if (res.code === 200) {
    ElMessage.success('设置成功')
    loadLocations()
  } else {
    ElMessage.error(res.message || '设置失败')
  }
}

// 地址解析
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

// 定位
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

      if (window.AMap) {
        try {
          const geocoder = new AMap.Geocoder({})
          const address = await new Promise((resolve, reject) => {
            geocoder.getAddress([longitude, latitude], (status, result) => {
              if (status === 'complete' && result.regeocode) {
                resolve(result.regeocode.formattedAddress)
              } else {
                reject(new Error(`逆地理编码失败，状态：${status}`))
              }
            })
          })
          if (!locationForm.locationDesc) {
            locationForm.locationDesc = address
          }
          addressInput.value = address
          ElMessage.success('定位成功，已获取地址')
        } catch (error) {
          console.error('逆地理编码详细错误：', error)
          ElMessage.warning('无法获取详细地址，请手动输入')
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

// 生命周期
onMounted(() => {
  loadLocations()
  initMap()
})
onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
    mapInitialized.value = false
  }
})
</script>

<style scoped>
.header {
  margin-bottom: 10px;
  text-align: right;
}
</style>