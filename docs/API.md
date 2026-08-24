# 猫咪领养平台 API 文档

## 1. 文档说明

本文档根据前端项目中的接口封装整理，描述平台当前已使用的后端 API。接口统一通过 Axios 实例封装，默认请求基地址为：

```text
http://localhost:8080
```

所有接口均以 `/api` 作为统一前缀。

## 2. 通用约定

### 2.1 请求头

系统会在请求拦截器中自动携带登录令牌：

```http
Authorization: Bearer <token>
```

### 2.2 响应格式

接口响应统一按以下结构处理：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

其中：

- `code = 200` 表示请求成功
- `message` 为提示信息
- `data` 为业务数据

### 2.3 权限说明

- `公开接口`：未登录用户也可访问
- `用户接口`：登录后可用
- `志愿者接口`：志愿者角色可用
- `管理员接口`：管理员角色可用

---

## 3. 用户模块

文件：[src/api/user.js](../src/api/user.js)

### 3.1 接口列表

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 用户注册 | POST | `/api/user/register` | 创建新用户 |
| 用户登录 | POST | `/api/user/login` | 用户登录并获取 token |
| 获取当前用户信息 | GET | `/api/user/me` | 获取当前登录用户信息 |
| 申请成为志愿者 | POST | `/api/user/apply-volunteer` | 提交志愿者申请 |
| 获取志愿者列表 | GET | `/api/user/volunteers` | 查询志愿者用户列表 |
| 获取用户分页列表 | GET | `/api/user/page` | 管理员分页查询用户 |
| 更新个人信息 | PUT | `/api/user/profile` | 修改当前用户资料 |
| 上传头像 | POST | `/api/user/avatar` | 上传用户头像 |
| 更新用户资料 | PUT | `/api/user/profile` | 更新当前用户资料 |
| 管理员分页查询用户 | GET | `/api/user/admin/page` | 管理员查询用户列表 |
| 新增用户 | POST | `/api/user/admin` | 管理员创建用户 |
| 更新用户 | PUT | `/api/user/admin/{id}` | 管理员修改用户信息 |
| 重置密码 | PUT | `/api/user/admin/{id}/reset-password` | 管理员重置密码 |
| 更新用户状态 | PUT | `/api/user/admin/{id}/status` | 管理员启用/禁用用户 |
| 删除用户 | DELETE | `/api/user/admin/{id}` | 管理员删除用户 |
| 获取用户公开信息 | GET | `/api/user/public/{userId}` | 查询公开资料 |

---

## 4. 猫咪模块

文件：[src/api/cat.js](../src/api/cat.js)

### 4.1 接口列表

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 分页查询猫咪 | GET | `/api/cats` | 获取猫咪列表 |
| 根据 ID 查询详情 | GET | `/api/cats/{id}` | 获取猫咪详情 |
| 新增猫咪 | POST | `/api/cats` | 创建猫咪档案 |
| 更新猫咪 | PUT | `/api/cats/{id}` | 更新猫咪信息 |
| 删除猫咪 | DELETE | `/api/cats/{id}` | 删除猫咪 |
| 上传猫咪照片 | POST | `/api/cat-photos/upload/{catId}` | 上传单张照片 |
| 获取猫咪照片列表 | GET | `/api/cat-photos/list/{catId}` | 查询照片列表 |
| 删除猫咪照片 | DELETE | `/api/cat-photos/{id}` | 删除照片 |
| 获取猫咪统计信息 | GET | `/api/cats/statistics` | 获取统计数据 |

---

## 5. 领养模块

文件：[src/api/adoption.js](../src/api/adoption.js)

### 5.1 接口列表

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 提交领养申请 | POST | `/api/adoptions/apply` | 用户提交申请 |
| 分页查询申请 | GET | `/api/adoptions/page` | 查询领养申请列表 |
| 获取申请详情 | GET | `/api/adoptions/{id}` | 查询申请详情 |
| 初审 | PUT | `/api/adoptions/{id}/first-review` | 初审通过或拒绝 |
| 终审 | PUT | `/api/adoptions/{id}/final-review` | 终审通过或拒绝 |
| 添加回访记录 | POST | `/api/adoptions/{id}/follow-up` | 新增回访内容 |
| 获取回访记录列表 | GET | `/api/adoptions/{id}/follow-ups` | 查询回访记录 |
| 签订协议 | PUT | `/api/adoptions/{id}/sign-agreement` | 保存协议地址 |
| 完成领养 | PUT | `/api/adoptions/{id}/complete` | 标记为完成领养 |
| 取消申请 | PUT | `/api/adoptions/{id}/cancel` | 取消申请 |
| 开始回访 | PUT | `/api/adoptions/{id}/start-follow-up` | 将申请推进到回访阶段 |
| 回访通过 | PUT | `/api/adoptions/{id}/pass-follow-up` | 回访审核通过 |
| 获取领养统计数据 | GET | `/api/adoptions/statistics` | 查询领养统计 |

### 5.2 业务流程说明

领养流程通常按以下顺序推进：

1. 用户提交领养申请
2. 志愿者/管理员执行初审
3. 进入回访阶段并记录回访内容
4. 管理员执行终审
5. 上传领养协议
6. 完成领养

---

## 6. 公告模块

文件：[src/api/announcement.js](../src/api/announcement.js)

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 获取已发布公告列表 | GET | `/api/announcements/published` | 公开公告列表 |
| 获取公告详情 | GET | `/api/announcements/{id}` | 公告详情 |
| 管理员分页查询公告 | GET | `/api/announcements/admin/page` | 管理后台列表 |
| 创建公告 | POST | `/api/announcements` | 新增公告 |
| 更新公告 | PUT | `/api/announcements/{id}` | 编辑公告 |
| 删除公告 | DELETE | `/api/announcements/{id}` | 删除公告 |

---

## 7. 社区模块

文件：[src/api/community.js](../src/api/community.js)

### 7.1 动态与评论

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 获取动态列表 | GET | `/api/community/posts` | 查询动态 |
| 获取动态详情 | GET | `/api/community/posts/{id}` | 查询动态和评论 |
| 发布动态 | POST | `/api/community/posts` | 创建动态 |
| 点赞/取消点赞 | POST | `/api/community/posts/{id}/like` | 动态点赞 |
| 发表评论 | POST | `/api/community/comments` | 新增评论 |
| 删除评论 | DELETE | `/api/community/comments/{id}` | 删除评论 |
| 删除动态 | DELETE | `/api/community/posts/{id}` | 删除动态 |
| 上传图片 | POST | `/api/community/upload` | 上传社区图片 |

### 7.2 用户维度数据

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 获取用户发布的动态 | GET | `/api/community/user/posts` | 查询当前用户动态 |
| 获取用户评论记录 | GET | `/api/community/user/comments` | 查询当前用户评论 |
| 获取用户点赞记录 | GET | `/api/community/user/likes` | 查询当前用户点赞 |
| 获取指定用户动态 | GET | `/api/community/user/{userId}/posts` | 公开主页数据 |
| 获取指定用户评论 | GET | `/api/community/user/{userId}/comments` | 公开主页数据 |

### 7.3 管理员接口

文件：[src/api/admin.js](../src/api/admin.js)

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 管理员查询动态 | GET | `/api/community/admin/posts` | 动态管理列表 |
| 删除动态 | DELETE | `/api/community/admin/posts/{id}` | 管理员删除动态 |
| 管理员查询评论 | GET | `/api/community/admin/comments` | 评论管理列表 |
| 删除评论 | DELETE | `/api/community/admin/comments/{id}` | 管理员删除评论 |

---

## 8. 捐赠模块

文件：[src/api/donation.js](../src/api/donation.js)

### 8.1 募捐活动

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 分页查询活动 | GET | `/api/donations/campaigns` | 活动列表 |
| 获取活动详情 | GET | `/api/donations/campaigns/{id}` | 活动详情 |
| 创建活动 | POST | `/api/donations/campaigns` | 新建活动 |
| 更新活动 | PUT | `/api/donations/campaigns/{id}` | 编辑活动 |
| 删除活动 | DELETE | `/api/donations/campaigns/{id}` | 删除活动 |

### 8.2 捐赠记录

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 用户捐赠 | POST | `/api/donations/donate` | 提交捐赠 |
| 分页查询捐赠记录 | GET | `/api/donations/records` | 公开捐赠记录 |
| 获取活动捐赠记录 | GET | `/api/donations/campaigns/{campaignId}/records` | 活动关联记录 |
| 审核捐赠 | PUT | `/api/donations/records/{id}/audit` | 管理员审核 |
| 删除捐赠记录 | DELETE | `/api/donations/records/{id}` | 管理员删除 |
| 获取我的捐赠记录 | GET | `/api/donations/my-donations` | 当前用户记录 |
| 获取用户捐赠记录 | GET | `/api/donations/user/{userId}/donations` | 指定用户记录 |

### 8.3 捐赠统计

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 获取捐赠统计 | GET | `/api/donations/statistics` | 总体统计 |
| 获取物资统计 | GET | `/api/donations/goods-statistics` | 物资捐赠统计 |

---

## 9. 任务模块

文件：[src/api/task.js](../src/api/task.js)

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 分页查询任务 | GET | `/api/tasks` | 任务列表 |
| 获取任务详情 | GET | `/api/tasks/{id}` | 任务详情 |
| 发布任务 | POST | `/api/tasks` | 创建任务 |
| 更新任务 | PUT | `/api/tasks/{id}` | 编辑任务 |
| 删除任务 | DELETE | `/api/tasks/{id}` | 删除任务 |
| 指派志愿者 | POST | `/api/tasks/{taskId}/assign/{userId}` | 手动指派 |
| 报名任务 | POST | `/api/tasks/{taskId}/apply` | 志愿者报名 |
| 取消报名 | DELETE | `/api/tasks/{taskId}/apply` | 撤销报名 |
| 获取报名列表 | GET | `/api/tasks/{taskId}/applications` | 查看报名信息 |
| 审核报名 | PUT | `/api/tasks/applications/{applicationId}/status` | 审核结果 |
| 开始任务 | POST | `/api/tasks/{taskId}/start` | 任务开始 |
| 获取我报名的任务 ID | GET | `/api/tasks/my-applications` | 当前用户报名列表 |
| 自动指派 | POST | `/api/tasks/{taskId}/auto-assign` | 自动匹配志愿者 |
| 提交任务完成 | POST | `/api/tasks/{taskId}/complete` | 提交完成内容 |
| 审核任务完成 | PUT | `/api/tasks/{taskId}/review-completion` | 审核完成结果 |
| 上传任务图片 | POST | `/api/tasks/upload` | 上传任务图片 |

---

## 10. 通知模块

文件：[src/api/notification.js](../src/api/notification.js)

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 获取通知列表 | GET | `/api/notifications` | 分页查询通知 |
| 获取未读数量 | GET | `/api/notifications/unread-count` | 未读消息数 |
| 标记单条已读 | PUT | `/api/notifications/{id}/read` | 将单条通知设为已读 |
| 标记全部已读 | PUT | `/api/notifications/read-all` | 全部已读 |
| 删除通知 | DELETE | `/api/notifications/{id}` | 删除消息 |

---

## 11. 猫咪位置模块

文件：[src/api/location.js](../src/api/location.js)

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 获取猫咪位置记录 | GET | `/api/cat-locations/cat/{catId}` | 查询某只猫的历史位置 |
| 新增位置记录 | POST | `/api/cat-locations` | 添加位置 |
| 更新位置记录 | PUT | `/api/cat-locations/{id}` | 修改位置 |
| 删除位置记录 | DELETE | `/api/cat-locations/{id}` | 删除位置 |
| 设置为当前地点 | PUT | `/api/cat-locations/{id}/set-current` | 标记当前地点 |

---

## 12. 健康记录模块

文件：[src/api/health.js](../src/api/health.js)

| 接口名称 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 获取健康记录列表 | GET | `/api/health-records/cat/{catId}` | 查询某只猫的健康记录 |
| 新增健康记录 | POST | `/api/health-records` | 添加健康信息 |
| 更新健康记录 | PUT | `/api/health-records/{id}` | 修改健康信息 |
| 删除健康记录 | DELETE | `/api/health-records/{id}` | 删除健康记录 |

---

## 13. 接口设计特点

1. 采用统一的 REST 风格路径设计。
2. 统一通过 `/api` 前缀进行版本外封装。
3. 登录态通过 `Authorization` 请求头传递。
4. 后端通过 `code/message/data` 结构返回业务结果。
5. 文件上传接口主要使用 `multipart/form-data`。
6. 业务模块按照角色划分较清晰，适合前台用户、志愿者和管理员分层使用。

本项目后端 API 主要服务于猫咪领养平台的前台展示、用户交互和后台管理三类场景。接口覆盖用户认证、猫咪管理、领养审批、回访记录、公告发布、社区互动、募捐活动、任务调度、通知提醒以及猫咪健康与位置追踪等功能，形成了较完整的业务闭环。
