import request from '@/utils/request'

/**
 * 用户注册
 */
export const register = (data) => {
    return request({
        url: '/api/user/register',
        method: 'post',
        data
    })
}

/**
 * 用户登录
 */
export const login = (data) => {
    return request({
        url: '/api/user/login',
        method: 'post',
        data
    })
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = () => {
    return request({
        url: '/api/user/me',
        method: 'get'
    })
}

// 申请成为志愿者
export const applyVolunteer = () => {
    return request({
        url: '/api/user/apply-volunteer',
        method: 'post'
    })
}

// 获取志愿者列表
export function getVolunteerList() {
    return request({
        url: '/api/user/volunteers',
        method: 'get'
    })
}

// 获取用户分页列表（管理员接口）
export function getUserPage(params) {
    return request({
        url: '/api/user/page',
        method: 'get',
        params
    })
}

// 更新个人信息
export function updateProfile(data) {
    return request({
        url: '/api/user/profile',
        method: 'put',
        data
    })
}

// 上传头像
export function uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/api/user/avatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 更新用户资料
export function updateUserProfile(data) {
    return request({
        url: '/api/user/profile',
        method: 'put',
        data
    })
}

// 管理员分页查询用户
export function getAdminUserPage(params) {
    return request({
        url: '/api/user/admin/page',
        method: 'get',
        params
    })
}

// 新增用户
export function addUser(data) {
    return request({
        url: '/api/user/admin',
        method: 'post',
        data
    })
}

// 更新用户
export function updateUser(id, data) {
    return request({
        url: `/api/user/admin/${id}`,
        method: 'put',
        data
    })
}

// 重置密码
export function resetPassword(id, newPassword) {
    return request({
        url: `/api/user/admin/${id}/reset-password`,
        method: 'put',
        params: { newPassword }
    })
}

// 更新用户状态（禁用/启用）
export function updateUserStatus(id, status) {
    return request({
        url: `/api/user/admin/${id}/status`,
        method: 'put',
        params: { status }
    })
}

// 物理删除用户（可选）
export function deleteUser(id) {
    return request({
        url: `/api/user/admin/${id}`,
        method: 'delete'
    })
}

// 获取用户公开信息（如昵称、头像等）
export const getUserPublicInfo = (userId) => request({
  url: `/api/user/public/${userId}`,
  method: 'get'
})