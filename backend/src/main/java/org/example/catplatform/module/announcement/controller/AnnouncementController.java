package org.example.catplatform.module.announcement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.announcement.dto.AnnouncementDTO;
import org.example.catplatform.module.announcement.entity.Announcement;
import org.example.catplatform.module.announcement.service.AnnouncementService;
import org.example.catplatform.module.user.entity.User;
import org.example.catplatform.module.user.service.UserService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    /**
     * 获取已发布的公告列表（按时间倒序，置顶重要公告）
     */
    @GetMapping("/published")
    public Result<Page<Announcement>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)  // 只显示已发布
                .orderByDesc(Announcement::getPriority)   // 重要公告优先
                .orderByDesc(Announcement::getPublishTime);
        announcementService.page(page, wrapper);
        return Result.success(page);
    }

    /**
     * 获取单条公告详情（已发布的才公开）
     */
    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        // 未登录用户只能查看已发布的公告
        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            String role = userDetails.getUser().getRole();
            if (announcement.getStatus() != 1 && !"ADMIN".equals(role)) {
                return Result.error("无权查看");
            }
        } catch (Exception e) {
            // 未登录用户，只能查看已发布的
            if (announcement.getStatus() != 1) {
                return Result.error("无权查看");
            }
        }
        // 填充创建人用户名
        if (announcement.getCreateBy() != null) {
            User creator = userService.getById(announcement.getCreateBy());
            if (creator != null) {
                announcement.setCreatorName(creator.getUsername());
            }
        }
        return Result.success(announcement);
    }

    /**
     * 管理员分页查询所有公告（含草稿）
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Announcement>> adminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String title) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like(Announcement::getTitle, title);
        }
        wrapper.orderByDesc(Announcement::getCreateTime);
        announcementService.page(page, wrapper);

        // 填充创建人用户名
        if (!page.getRecords().isEmpty()) {
            List<Long> creatorIds = page.getRecords().stream()
                    .map(Announcement::getCreateBy)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            if (!creatorIds.isEmpty()) {
                Map<Long, String> userMap = userService.listByIds(creatorIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getUsername));
                page.getRecords().forEach(a -> {
                    if (a.getCreateBy() != null) {
                        a.setCreatorName(userMap.get(a.getCreateBy()));
                    }
                });
            }
        }
        return Result.success(page);
    }

    /**
     * 创建公告（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Announcement> create(@RequestBody AnnouncementDTO dto) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        announcement.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        announcement.setPublishTime(dto.getPublishTime());
        announcement.setCreateBy(userDetails.getUserId());
        announcementService.save(announcement);
        return Result.success("创建成功", announcement);
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody AnnouncementDTO dto) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setPriority(dto.getPriority());
        announcement.setStatus(dto.getStatus());
        announcement.setPublishTime(dto.getPublishTime());
        announcementService.updateById(announcement);
        return Result.success("更新成功", announcement);
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success("删除成功", null);
    }
}