package org.example.catplatform.module.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.cat.entity.Cat;
import org.example.catplatform.module.cat.service.CatService;
import org.example.catplatform.module.task.dto.TaskCompleteDTO;
import org.example.catplatform.module.task.dto.TaskDTO;
import org.example.catplatform.module.task.entity.Task;
import org.example.catplatform.module.task.entity.TaskApplication;
import org.example.catplatform.module.task.service.TaskApplicationService;
import org.example.catplatform.module.task.service.TaskService;
import org.example.catplatform.module.user.entity.User;
import org.example.catplatform.module.user.service.UserService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.example.catplatform.module.notification.entity.Notification;
import org.example.catplatform.module.notification.service.NotificationService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskApplicationService taskApplicationService;

    @Autowired
    private CatService catService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Value("${file.upload.task}")
    private String uploadTaskPath;

    // ========== 发送任务指派通知 ==========
    private void sendTaskAssignNotification(Long volunteerId, Long taskId, String taskTitle) {
        Notification notification = new Notification();
        notification.setUserId(volunteerId);
        notification.setType("TASK_ASSIGN");
        notification.setTitle("您被指派了新的养护任务");
        notification.setContent("任务：" + taskTitle);
        notification.setRelatedId(taskId);
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    // ========== 发送通知给所有志愿者（新任务发布） ==========
    private void notifyVolunteersOnNewTask(Task task) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "VOLUNTEER").eq(User::getStatus, 1);
        List<User> volunteers = userService.list(wrapper);
        for (User volunteer : volunteers) {
            Notification notification = new Notification();
            notification.setUserId(volunteer.getId());
            notification.setType("TASK_NEW");
            notification.setTitle("新任务发布");
            notification.setContent("新任务：" + task.getTitle() + " 已发布，请及时报名");
            notification.setRelatedId(task.getId());
            notification.setIsRead(0);
            notificationService.save(notification);
        }
    }

    // ========== 发送通知给所有管理员（志愿者报名） ==========
    private void notifyAdminsOnApplication(Task task, Long volunteerId) {
        User volunteer = userService.getById(volunteerId);
        String volunteerName = volunteer != null ? volunteer.getUsername() : "某志愿者";
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "ADMIN").eq(User::getStatus, 1);
        List<User> admins = userService.list(wrapper);
        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setUserId(admin.getId());
            notification.setType("TASK_APPLY");
            notification.setTitle("志愿者报名任务");
            notification.setContent("志愿者 " + volunteerName + " 报名了任务：" + task.getTitle());
            notification.setRelatedId(task.getId());
            notification.setIsRead(0);
            notificationService.save(notification);
        }
    }

    // ========== 发送通知给被指派的志愿者 ==========
    private void notifyAssignedVolunteer(Task task, Long volunteerId) {
        Notification notification = new Notification();
        notification.setUserId(volunteerId);
        notification.setType("TASK_ASSIGN");
        notification.setTitle("您被指派了新任务");
        notification.setContent("任务：" + task.getTitle() + " 已指派给您，请及时处理");
        notification.setRelatedId(task.getId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    // ========== 发送通知给被选中的志愿者（报名选中） ==========
    private void notifyVolunteerSelected(Task task, Long volunteerId) {
        User volunteer = userService.getById(volunteerId);
        String volunteerName = volunteer != null ? volunteer.getUsername() : "志愿者";
        Notification notification = new Notification();
        notification.setUserId(volunteerId);
        notification.setType("TASK_APPLICATION_SELECTED");
        notification.setTitle("报名被选中");
        notification.setContent("您在任务【" + task.getTitle() + "】的报名已被管理员选中");
        notification.setRelatedId(task.getId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    // ========== 发送通知给提交完成的志愿者（审核结果） ==========
    private void notifyCompletionReview(Long volunteerId, Long taskId, String taskTitle, boolean approved, String remark) {
        Notification notification = new Notification();
        notification.setUserId(volunteerId);
        notification.setType("TASK_COMPLETION_REVIEW");
        notification.setTitle(approved ? "任务完成审核通过" : "任务完成审核未通过");
        String content = approved ? "您提交的任务【" + taskTitle + "】已完成审核，任务已结束。"
                : "您提交的任务【" + taskTitle + "】审核未通过，原因：" + (remark != null ? remark : "请重新提交");
        notification.setContent(content);
        notification.setRelatedId(taskId);
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    /**
     * 分页查询任务列表（公开）
     */
    @GetMapping
    public Result<Page<Task>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Long catId) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        if (catId != null) {
            wrapper.eq(Task::getCatId, catId);
        }
        wrapper.orderByDesc(Task::getCreateTime);
        taskService.page(page, wrapper);

        // 填充猫咪名称
        List<Task> records = page.getRecords();
        if (!records.isEmpty()) {
            List<Long> catIds = records.stream().map(Task::getCatId).filter(id -> id != null).collect(Collectors.toList());
            if (!catIds.isEmpty()) {
                Map<Long, String> catNameMap = catService.listByIds(catIds).stream()
                        .collect(Collectors.toMap(Cat::getId, Cat::getName));
                records.forEach(task -> {
                    if (task.getCatId() != null) {
                        task.setCatName(catNameMap.get(task.getCatId()));
                    }
                });
            }
        }
        return Result.success(page);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        // 填充猫咪名称
        if (task.getCatId() != null) {
            Cat cat = catService.getById(task.getCatId());
            if (cat != null) {
                task.setCatName(cat.getName());
            }
        }
        return Result.success(task);
    }

    /**
     * 发布任务（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Task> create(@RequestBody TaskDTO taskDTO) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setTaskType(taskDTO.getTaskType());
        task.setDescription(taskDTO.getDescription());
        task.setCatId(taskDTO.getCatId());
        task.setPriority(taskDTO.getPriority());
        task.setDeadline(taskDTO.getDeadline());
        task.setStatus(0); // 待分配
        task.setCreateBy(userDetails.getUserId());
        taskService.save(task);

        // 通知所有志愿者
        notifyVolunteersOnNewTask(task);

        return Result.success("发布成功", task);
    }

    /**
     * 更新任务（仅管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        taskService.updateById(task);
        return Result.success("更新成功", task);
    }

    /**
     * 删除任务（仅管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 手动指派志愿者（管理员）
     */
    @PostMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assign(@PathVariable Long taskId, @PathVariable Long userId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        task.setAssignedTo(userId);
        task.setAssignedType(0); // 手动
        task.setStatus(1); // 已分配
        taskService.updateById(task);

        // 可选：将报名表中该用户的报名状态设为“已选中”
        LambdaQueryWrapper<TaskApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskApplication::getTaskId, taskId).eq(TaskApplication::getUserId, userId);
        TaskApplication application = taskApplicationService.getOne(wrapper);
        if (application != null) {
            application.setStatus(1); // 已选中
            taskApplicationService.updateById(application);
        }

        // 通知被指派的志愿者
        notifyAssignedVolunteer(task, userId);

        return Result.success("指派成功", null);
    }

    /**
     * 志愿者报名任务
     */
    @PostMapping("/{taskId}/apply")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<Void> apply(@PathVariable Long taskId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        LambdaQueryWrapper<TaskApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskApplication::getTaskId, taskId).eq(TaskApplication::getUserId, userId);
        if (taskApplicationService.count(wrapper) > 0) {
            return Result.error("您已报名过该任务");
        }

        TaskApplication application = new TaskApplication();
        application.setTaskId(taskId);
        application.setUserId(userId);
        application.setStatus(0); // 待审核
        taskApplicationService.save(application);

        // 获取任务信息
        Task task = taskService.getById(taskId);
        if (task != null) {
            // 通知所有管理员
            notifyAdminsOnApplication(task, userId);
        }

        return Result.success("报名成功", null);
    }

    /**
     * 取消报名（志愿者取消自己的报名）
     */
    @DeleteMapping("/{taskId}/apply")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<Void> cancelApply(@PathVariable Long taskId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        LambdaQueryWrapper<TaskApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskApplication::getTaskId, taskId).eq(TaskApplication::getUserId, userId);
        taskApplicationService.remove(wrapper);
        return Result.success("取消报名成功", null);
    }

    /**
     * 获取某任务的报名列表（管理员查看）
     */
    @GetMapping("/{taskId}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<TaskApplication>> getApplications(@PathVariable Long taskId) {
        LambdaQueryWrapper<TaskApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskApplication::getTaskId, taskId);
        List<TaskApplication> list = taskApplicationService.list(wrapper);

        // 填充用户名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(TaskApplication::getUserId).collect(Collectors.toList());
            Map<Long, String> userNameMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
            list.forEach(app -> app.setUserName(userNameMap.get(app.getUserId())));
        }
        return Result.success(list);
    }

    /**
     * 管理员审核报名（选中/未选中）
     */
    @PutMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reviewApplication(@PathVariable Long applicationId, @RequestParam Integer status) {
        TaskApplication application = taskApplicationService.getById(applicationId);
        if (application == null) {
            return Result.error("报名记录不存在");
        }
        application.setStatus(status);
        taskApplicationService.updateById(application);

        // 如果状态为已选中（1）
        if (status == 1) {
            Task task = taskService.getById(application.getTaskId());
            if (task != null) {
                // 发送选中通知
                notifyVolunteerSelected(task, application.getUserId());

                // 如果任务待分配，则自动指派
                if (task.getStatus() == 0) {
                    task.setAssignedTo(application.getUserId());
                    task.setAssignedType(0);
                    task.setStatus(1);
                    taskService.updateById(task);
                }
            }
        }
        return Result.success("操作成功", null);
    }

    /**
     * 志愿者开始执行任务（将状态设为进行中）
     */
    @PostMapping("/{taskId}/start")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<Void> startTask(@PathVariable Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) return Result.error("任务不存在");
        // 检查是否指派给当前用户
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (!userDetails.getUserId().equals(task.getAssignedTo())) {
            return Result.error("您没有被指派此任务");
        }
        if (task.getStatus() != 1) {
            return Result.error("任务状态不正确");
        }
        task.setStatus(2); // 进行中
        taskService.updateById(task);
        return Result.success("任务已开始", null);
    }

    /**
     * 志愿者提交任务完成（含描述和图片，提交后状态变为待审核）
     * 需要 Task 实体增加字段：
     * - completionDescription VARCHAR(2000)
     * - completionImages TEXT
     * - completionStatus TINYINT DEFAULT 0 (0未提交,1待审核,2通过,3拒绝)
     * - completionRemark VARCHAR(500)
     */
    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<Void> submitCompletion(@PathVariable Long taskId, @RequestBody TaskCompleteDTO dto) {
        Task task = taskService.getById(taskId);
        if (task == null) return Result.error("任务不存在");
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (!userDetails.getUserId().equals(task.getAssignedTo())) {
            return Result.error("您没有被指派此任务");
        }
        if (task.getStatus() != 2) {
            return Result.error("任务未开始或已完成");
        }
//        // 如果已经有待审核的记录，不能重复提交
//        if (task.getCompletionStatus() != null && task.getCompletionStatus() == 1) {
//            return Result.error("已有待审核的完成记录，请等待管理员审核");
//        }

        task.setCompletionDescription(dto.getDescription());
        task.setCompletionImages(dto.getImages());
        task.setCompletionStatus(1); // 待审核
        task.setCompletionTime(LocalDateTime.now());
        taskService.updateById(task);

        // 可选：通知管理员有新完成记录待审核
        // ...

        return Result.success("提交成功，等待管理员审核", null);
    }

    /**
     * 管理员审核任务完成（通过/拒绝）
     * @param taskId 任务ID
     * @param status 2-通过，3-拒绝
     * @param remark 审核意见（选填）
     */
    @PutMapping("/{taskId}/review-completion")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public Result<Void> reviewCompletion(@PathVariable Long taskId,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String remark) {
        Task task = taskService.getById(taskId);
        if (task == null) return Result.error("任务不存在");
        if (task.getCompletionStatus() != 1) {
            return Result.error("该任务不在待审核状态");
        }
        if (status != 2 && status != 3) {
            return Result.error("审核状态无效");
        }

        task.setCompletionStatus(status);
        if (remark != null && !remark.isEmpty()) {
            task.setCompletionRemark(remark);
        }
        if (status == 2) {
            task.setStatus(3); // 已完成
            task.setCompleteTime(LocalDateTime.now());
        }
        taskService.updateById(task);

        // 发送通知给志愿者
        notifyCompletionReview(task.getAssignedTo(), taskId, task.getTitle(), status == 2, remark);
        return Result.success(status == 2 ? "审核通过" : "审核拒绝", null);
    }

    /**
     * 自动指派志愿者（从当前报名中按规则选择）
     */
    @PostMapping("/{taskId}/auto-assign")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> autoAssign(@PathVariable Long taskId) {
        Task task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (task.getStatus() != 0) {
            return Result.error("只有待分配的任务才能自动指派");
        }

        // 获取当前任务的所有报名记录（按报名时间升序）
        LambdaQueryWrapper<TaskApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskApplication::getTaskId, taskId)
                .orderByAsc(TaskApplication::getApplyTime);
        List<TaskApplication> applications = taskApplicationService.list(wrapper);
        if (applications.isEmpty()) {
            return Result.error("暂无志愿者报名，无法自动指派");
        }

        // 规则示例：选择报名最早的志愿者（也可以根据积分等）
        TaskApplication selected = applications.get(0);

        // 更新任务
        task.setAssignedTo(selected.getUserId());
        task.setAssignedType(1); // 自动指派
        task.setStatus(1); // 已分配
        taskService.updateById(task);

        // 更新报名状态：选中的设为“已选中”，其他的设为“未选中”
        for (TaskApplication app : applications) {
            if (app.getId().equals(selected.getId())) {
                app.setStatus(1); // 已选中
            } else {
                app.setStatus(2); // 未选中
            }
            taskApplicationService.updateById(app);
        }
        // 发送通知
        sendTaskAssignNotification(selected.getUserId(), taskId, task.getTitle());

        return Result.success("自动指派成功", null);
    }

    /**
     * 获取当前登录志愿者已申请的任务 ID 列表
     */
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<List<Long>> getMyAppliedTaskIds() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        LambdaQueryWrapper<TaskApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskApplication::getUserId, userDetails.getUserId());
        List<TaskApplication> list = taskApplicationService.list(wrapper);
        List<Long> taskIds = list.stream().map(TaskApplication::getTaskId).collect(Collectors.toList());
        return Result.success(taskIds);
    }

    /**
     * 上传任务完成图片（志愿者/管理员）
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadTaskImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "task_" + System.currentTimeMillis() + suffix;

            // 定义存储路径（根据你的配置，例如 application.yml 中的 file.upload.task）
            String uploadDir = uploadTaskPath;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // 构建访问URL（需配置静态资源映射）
            String fileUrl = "/uploads/tasks/" + fileName;
            return Result.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}