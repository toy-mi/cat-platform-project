import request from '@/utils/request'

// 动态管理 ==========
export function getAdminPosts(params) {
    return request({
        url: '/api/community/admin/posts',
        method: 'get',
        params
    })
}

export function adminDeletePost(id) {
    return request({
        url: `/api/community/admin/posts/${id}`,
        method: 'delete'
    })
}

// 评论管理 ==========
export function getAdminComments(params) {
    return request({
        url: '/api/community/admin/comments',
        method: 'get',
        params
    })
}

export function adminDeleteComment(id) {
    return request({
        url: `/api/community/admin/comments/${id}`,
        method: 'delete'
    })
}