import request from '@/utils/request'

// 获取猫咪的健康记录列表
export function getHealthRecords(catId) {
    return request({
        url: `/api/health-records/cat/${catId}`,
        method: 'get'
    })
}

// 新增健康记录
export function addHealthRecord(data) {
    return request({
        url: '/api/health-records',
        method: 'post',
        data
    })
}

// 更新健康记录
export function updateHealthRecord(id, data) {
    return request({
        url: `/api/health-records/${id}`,
        method: 'put',
        data
    })
}

// 删除健康记录
export function deleteHealthRecord(id) {
    return request({
        url: `/api/health-records/${id}`,
        method: 'delete'
    })
}