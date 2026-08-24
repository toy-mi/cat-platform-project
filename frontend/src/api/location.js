import request from '@/utils/request'

// 获取猫咪的位置记录
export function getCatLocations(catId) {
    return request({
        url: `/api/cat-locations/cat/${catId}`,
        method: 'get'
    })
}

// 新增位置记录
export function addCatLocation(data) {
    return request({
        url: '/api/cat-locations',
        method: 'post',
        data
    })
}

// 更新位置记录
export function updateCatLocation(id, data) {
    return request({
        url: `/api/cat-locations/${id}`,
        method: 'put',
        data
    })
}

// 删除位置记录
export function deleteCatLocation(id) {
    return request({
        url: `/api/cat-locations/${id}`,
        method: 'delete'
    })
}

// 设置为当前地点
export function setCurrentLocation(id) {
    return request({
        url: `/api/cat-locations/${id}/set-current`,
        method: 'put'
    })
}