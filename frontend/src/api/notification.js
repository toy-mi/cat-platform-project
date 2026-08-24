import request from '@/utils/request'

// 获取通知列表（分页）
export function getNotifications(params) {
    return request({
        url: '/api/notifications',
        method: 'get',
        params
    })
}

// 获取未读通知数量
export function getUnreadCount() {
    return request({
        url: '/api/notifications/unread-count',
        method: 'get'
    })
}

// 标记单条为已读
export function markAsRead(id) {
    return request({
        url: `/api/notifications/${id}/read`,
        method: 'put'
    })
}

// 标记所有为已读
export function markAllAsRead() {
    return request({
        url: '/api/notifications/read-all',
        method: 'put'
    })
}

// 删除通知
export function deleteNotification(id) {
    return request({
        url: `/api/notifications/${id}`,
        method: 'delete'
    })
}