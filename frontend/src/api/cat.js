import request from '@/utils/request'

// 分页查询猫咪
export function getCatPage(params) {
    return request({
        url: '/api/cats',
        method: 'get',
        params
    })
}

// 根据ID查询猫咪详情
export function getCatById(id) {
    return request({
        url: `/api/cats/${id}`,
        method: 'get'
    })
}

// 新增猫咪
export function addCat(data) {
    return request({
        url: '/api/cats',
        method: 'post',
        data
    })
}

// 更新猫咪
export function updateCat(id, data) {
    return request({
        url: `/api/cats/${id}`,
        method: 'put',
        data
    })
}

// 删除猫咪
export function deleteCat(id) {
    return request({
        url: `/api/cats/${id}`,
        method: 'delete'
    })
}

// 上传猫咪照片
export function uploadCatPhoto(catId, file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: `/api/cat-photos/upload/${catId}`,
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 获取猫咪照片列表
export function getCatPhotoList(catId) {
    return request({
        url: `/api/cat-photos/list/${catId}`,
        method: 'get'
    })
}

// 删除猫咪照片
export function deleteCatPhoto(id) {
    return request({
        url: `/api/cat-photos/${id}`,
        method: 'delete'
    })
}

// 获取猫咪统计信息
export function getCatStatistics() {
    return request({
        url: '/api/cats/statistics',
        method: 'get'
    })
}