import request from '@/utils/request'

// 分页查询任务
export function getTaskPage(params) {
    return request({
        url: '/api/tasks',
        method: 'get',
        params
    })
}

// 获取任务详情
export function getTaskById(id) {
    return request({
        url: `/api/tasks/${id}`,
        method: 'get'
    })
}

// 发布任务
export function createTask(data) {
    return request({
        url: '/api/tasks',
        method: 'post',
        data
    })
}

// 更新任务
export function updateTask(id, data) {
    return request({
        url: `/api/tasks/${id}`,
        method: 'put',
        data
    })
}

// 删除任务
export function deleteTask(id) {
    return request({
        url: `/api/tasks/${id}`,
        method: 'delete'
    })
}

// 指派志愿者
export function assignTask(taskId, userId) {
    return request({
        url: `/api/tasks/${taskId}/assign/${userId}`,
        method: 'post'
    })
}

// 报名任务
export function applyTask(taskId) {
    return request({
        url: `/api/tasks/${taskId}/apply`,
        method: 'post'
    })
}

// 取消报名
export function cancelApply(taskId) {
    return request({
        url: `/api/tasks/${taskId}/apply`,
        method: 'delete'
    })
}

// 获取任务的报名列表
export function getTaskApplications(taskId) {
    return request({
        url: `/api/tasks/${taskId}/applications`,
        method: 'get'
    })
}

// 审核报名
export function reviewApplication(applicationId, status) {
    return request({
        url: `/api/tasks/applications/${applicationId}/status`,
        method: 'put',
        params: { status }
    })
}

// 开始任务
export function startTask(taskId) {
    return request({
        url: `/api/tasks/${taskId}/start`,
        method: 'post'
    })
}

// // 完成任务
// export function completeTask(taskId) {
//     return request({
//         url: `/api/tasks/${taskId}/complete`,
//         method: 'post'
//     })
// }

// 获取当前志愿者已报名的任务ID列表
export function getMyAppliedTaskIds() {
    return request({
        url: '/api/tasks/my-applications',
        method: 'get'
    })
}

// 自动指派
export function autoAssignTask(taskId) {
    return request({
        url: `/api/tasks/${taskId}/auto-assign`,
        method: 'post'
    })
}

// 提交任务完成
export function completeTask(taskId, data) {
    return request({
        url: `/api/tasks/${taskId}/complete`,
        method: 'post',
        data
    })
}

// 审核任务完成
export function reviewTaskCompletion(taskId, status, remark) {
    return request({
        url: `/api/tasks/${taskId}/review-completion`,
        method: 'put',
        params: { status, remark }
    })
}

// 上传任务相关图片
export function uploadTaskImage(formData) {
    return request({
        url: '/api/tasks/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}