import request from '@/utils/request'

// 获取已发布的公告列表（公开）
export function getPublishedAnnouncements(params) {
    return request({
        url: '/api/announcements/published',
        method: 'get',
        params
    })
}

// 获取公告详情（公开）
export function getAnnouncementDetail(id) {
    return request({
        url: `/api/announcements/${id}`,
        method: 'get'
    })
}

// 管理员分页查询所有公告
export function getAdminAnnouncements(params) {
    return request({
        url: '/api/announcements/admin/page',
        method: 'get',
        params
    })
}

// 创建公告
export function createAnnouncement(data) {
    return request({
        url: '/api/announcements',
        method: 'post',
        data
    })
}

// 更新公告
export function updateAnnouncement(id, data) {
    return request({
        url: `/api/announcements/${id}`,
        method: 'put',
        data
    })
}

// 删除公告
export function deleteAnnouncement(id) {
    return request({
        url: `/api/announcements/${id}`,
        method: 'delete'
    })
}