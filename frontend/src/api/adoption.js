import request from '@/utils/request'

// 提交申请
export function applyAdoption(data) {
    return request({
        url: '/api/adoptions/apply',
        method: 'post',
        data
    })
}

// 分页查询申请
export function getAdoptionPage(params) {
    return request({
        url: '/api/adoptions/page',
        method: 'get',
        params
    })
}

// 获取申请详情
export function getAdoptionDetail(id) {
    return request({
        url: `/api/adoptions/${id}`,
        method: 'get'
    })
}

// 初审
export function firstReview(id, status, remark) {
    return request({
        url: `/api/adoptions/${id}/first-review`,
        method: 'put',
        params: { status },
        data: { remark }
    })
}

// 终审
export function finalReview(id, status, remark) {
    return request({
        url: `/api/adoptions/${id}/final-review`,
        method: 'put',
        params: { status },
        data: { remark }
    })
}

// 添加回访记录
export function addFollowUp(id, data) {
    return request({
        url: `/api/adoptions/${id}/follow-up`,
        method: 'post',
        data
    })
}

// 获取回访记录列表
export function getFollowUps(id) {
    return request({
        url: `/api/adoptions/${id}/follow-ups`,
        method: 'get'
    })
}

// 签订协议
export function signAgreement(id, agreementUrl) {
    return request({
        url: `/api/adoptions/${id}/sign-agreement`,
        method: 'put',
        params: { agreementUrl }
    })
}

// 完成领养
export function completeAdoption(id) {
    return request({
        url: `/api/adoptions/${id}/complete`,
        method: 'put'
    })
}

// 取消申请
export function cancelAdoption(id) {
    return request({
        url: `/api/adoptions/${id}/cancel`,
        method: 'put'
    })
}

// 开始回访
export function startFollowUp(id) {
    return request({
        url: `/api/adoptions/${id}/start-follow-up`,
        method: 'put'
    })
}

// 回访通过
export function passFollowUp(id) {
    return request({
        url: `/api/adoptions/${id}/pass-follow-up`,
        method: 'put'
    })
}

// 获取领养统计数据
export function getAdoptionStatistics() {
    return request({
        url: '/api/adoptions/statistics',
        method: 'get'
    })
}