import request from '@/utils/request'

// 获取动态列表
export function getPostPage(params) {
    return request({
        url: '/api/community/posts',
        method: 'get',
        params
    })
}

// 获取动态详情（含评论）
export function getPostDetail(id) {
    return request({
        url: `/api/community/posts/${id}`,
        method: 'get'
    })
}

// 发布动态
export function createPost(data) {
    return request({
        url: '/api/community/posts',
        method: 'post',
        data
    })
}

// 点赞/取消点赞
export function likePost(id) {
    return request({
        url: `/api/community/posts/${id}/like`,
        method: 'post'
    })
}

// 发表评论
export function addComment(data) {
    return request({
        url: '/api/community/comments',
        method: 'post',
        data
    })
}

// 删除评论
export function deleteComment(id) {
    return request({
        url: `/api/community/comments/${id}`,
        method: 'delete'
    })
}

// 删除动态
export function deletePost(id) {
    return request({
        url: `/api/community/posts/${id}`,
        method: 'delete'
    })
}

// 上传图片
export function uploadCommunityImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/api/community/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 获取用户发布的动态
export function getUserPosts(params) {
    return request({
        url: '/api/community/user/posts',
        method: 'get',
        params
    })
}

// 获取用户评论记录
export function getUserComments(params) {
    return request({
        url: '/api/community/user/comments',
        method: 'get',
        params
    })
}

// 获取用户点赞记录
export function getUserLikes(params) {
    return request({
        url: '/api/community/user/likes',
        method: 'get',
        params
    })
}

// ========== 公开主页相关（获取任意用户的动态/评论） ==========
// 获取指定用户的动态列表
export function getPublicUserPosts(userId, params) {
    return request({
        url: `/api/community/user/${userId}/posts`,
        method: 'get',
        params
    })
}

// 获取指定用户的评论列表
export function getPublicUserComments(userId, params) {
    return request({
        url: `/api/community/user/${userId}/comments`,
        method: 'get',
        params
    })
}