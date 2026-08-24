package org.example.catplatform.module.notification.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.notification.entity.Notification;
import org.example.catplatform.module.notification.service.NotificationService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取当前用户的通知列表（分页，按时间倒序）
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Page<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Boolean unreadOnly) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userDetails.getUserId());
        if (Boolean.TRUE.equals(unreadOnly)) {
            wrapper.eq(Notification::getIsRead, 0);
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        notificationService.page(page, wrapper);
        return Result.success(page);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getUnreadCount() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userDetails.getUserId())
                .eq(Notification::getIsRead, 0);
        long count = notificationService.count(wrapper);
        return Result.success(count);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAsRead(@PathVariable Long id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Notification notification = notificationService.getById(id);
        if (notification == null || !notification.getUserId().equals(userDetails.getUserId())) {
            return Result.error("通知不存在或无权限");
        }
        if (notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notificationService.updateById(notification);
        }
        return Result.success("已标记为已读", null);
    }

    /**
     * 标记当前用户所有通知为已读
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAllAsRead() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userDetails.getUserId())
                .eq(Notification::getIsRead, 0);
        Notification update = new Notification();
        update.setIsRead(1);
        notificationService.update(update, wrapper);
        return Result.success("全部已读", null);
    }

    /**
     * 删除单条通知
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Notification notification = notificationService.getById(id);
        if (notification == null || !notification.getUserId().equals(userDetails.getUserId())) {
            return Result.error("通知不存在或无权限");
        }
        notificationService.removeById(id);
        return Result.success("删除成功", null);
    }
}