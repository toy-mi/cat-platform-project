import request from '@/utils/request'

// ========== 募捐活动 ==========

// 分页查询活动
export function getCampaignPage(params) {
    return request({
        url: '/api/donations/campaigns',
        method: 'get',
        params
    })
}

// 获取活动详情
export function getCampaignDetail(id) {
    return request({
        url: `/api/donations/campaigns/${id}`,
        method: 'get'
    })
}

// 创建活动
export function createCampaign(data) {
    return request({
        url: '/api/donations/campaigns',
        method: 'post',
        data
    })
}

// 更新活动
export function updateCampaign(id, data) {
    return request({
        url: `/api/donations/campaigns/${id}`,
        method: 'put',
        data
    })
}

// 删除活动
export function deleteCampaign(id) {
    return request({
        url: `/api/donations/campaigns/${id}`,
        method: 'delete'
    })
}

// ========== 捐赠记录 ==========

// 用户捐赠
export function donate(data) {
    return request({
        url: '/api/donations/donate',
        method: 'post',
        data
    })
}

// 分页查询捐赠记录（公开）
export function getDonationPage(params) {
    return request({
        url: '/api/donations/records',
        method: 'get',
        params
    })
}

// 获取活动的捐赠记录
export function getCampaignDonations(campaignId, params) {
    return request({
        url: `/api/donations/campaigns/${campaignId}/records`,
        method: 'get',
        params
    })
}

// 审核捐赠
export function auditDonation(id, data) {
    return request({
        url: `/api/donations/records/${id}/audit`,
        method: 'put',
        data
    })
}

// 删除捐赠记录（管理员）
export function deleteDonation(id) {
    return request({
        url: `/api/donations/records/${id}`,
        method: 'delete'
    })
}

// ========== 捐赠统计 ==========

// 获取捐赠统计数据
export function getDonationStatistics() {
    return request({
        url: '/api/donations/statistics',
        method: 'get'
    })
}

// 获取我的捐赠记录
export function getMyDonations(params) {
    return request({
        url: '/api/donations/my-donations',
        method: 'get',
        params
    })
}

// 获取物资捐赠统计
export function getGoodsStatistics() {
    return request({
        url: '/api/donations/goods-statistics',
        method: 'get'
    })
}

// 获取用户的捐赠记录（分页）
export const getUserDonations = (userId, params) => request({
  url: `/api/donations/user/${userId}/donations`,
  method: 'get',
  params
})