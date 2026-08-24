package org.example.catplatform.module.adoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.adoption.dto.ApplyDTO;
import org.example.catplatform.module.adoption.dto.FollowUpDTO;
import org.example.catplatform.module.adoption.dto.ReviewDTO;
import org.example.catplatform.module.adoption.entity.AdoptionApplication;
import org.example.catplatform.module.adoption.entity.AdoptionFollowUp;
import org.example.catplatform.module.adoption.service.AdoptionApplicationService;
import org.example.catplatform.module.adoption.service.AdoptionFollowUpService;
import org.example.catplatform.module.cat.entity.Cat;
import org.example.catplatform.module.cat.service.CatService;
import org.example.catplatform.module.user.entity.User;
import org.example.catplatform.module.user.service.UserService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.catplatform.module.notification.entity.Notification;
import org.example.catplatform.module.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

// 领养申请管理
@RestController
@RequestMapping("/api/adoptions")
public class AdoptionController {

    @Autowired
    private AdoptionApplicationService applicationService;

    @Autowired
    private AdoptionFollowUpService followUpService;

    @Autowired
    private UserService userService;

    @Autowired
    private CatService catService;

    @Autowired
    private NotificationService notificationService;    // 注入通知服务

    @Value("${file.upload.agreements}")
    private String uploadAgreementsPath;

    /**
     * 提交领养申请（普通用户）
     */
    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public Result<AdoptionApplication> apply(@RequestBody ApplyDTO dto) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // 检查猫咪是否存在
        Cat cat = catService.getById(dto.getCatId());
        if (cat == null) {
            return Result.error("猫咪不存在");
        }
        // 检查是否已申请过该猫咪（可选）
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionApplication::getUserId, userDetails.getUserId())
                .eq(AdoptionApplication::getCatId, dto.getCatId())
                .in(AdoptionApplication::getStatus, 0,1,3,4,6,7); // 未完成的申请不能重复提交
        if (applicationService.count(wrapper) > 0) {
            return Result.error("您已经申请过该猫咪，且申请尚未完成");
        }

        AdoptionApplication app = new AdoptionApplication();
        app.setUserId(userDetails.getUserId());
        app.setCatId(dto.getCatId());
        app.setApplicationData(dto.getApplicationData());
        app.setStatus(0); // 待初审
        app.setApplyTime(LocalDateTime.now());
        applicationService.save(app);
        return Result.success("申请提交成功", app);
    }

    /**
     * 分页查询申请列表（管理员/志愿者可查所有，普通用户只能查自己的）
     */
    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<AdoptionApplication>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long catId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        System.out.println("查询参数：status=" + status + ", catId=" + catId +
                ", userId=" + userId + ", startTime=" + startTime + ", endTime=" + endTime);

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String role = userDetails.getUser().getRole();

        Page<AdoptionApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();

        // 普通用户只能看自己的申请
        if (!"ADMIN".equals(role) && !"VOLUNTEER".equals(role)) {
            wrapper.eq(AdoptionApplication::getUserId, userDetails.getUserId());
        }

        // 高级筛选条件
        if (status != null) {
            wrapper.eq(AdoptionApplication::getStatus, status);
        }
        if (catId != null) {
            wrapper.eq(AdoptionApplication::getCatId, catId);
        }
        if (userId != null && ("ADMIN".equals(role) || "VOLUNTEER".equals(role))) {
            // 管理员/志愿者可按申请人筛选
            wrapper.eq(AdoptionApplication::getUserId, userId);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(AdoptionApplication::getApplyTime, startTime + " 00:00:00");
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(AdoptionApplication::getApplyTime, endTime + " 23:59:59");
        }

        wrapper.orderByDesc(AdoptionApplication::getApplyTime);
        applicationService.page(page, wrapper);

        // 填充申请人姓名和猫咪姓名
        if (!page.getRecords().isEmpty()) {
            List<Long> userIds = page.getRecords().stream().map(AdoptionApplication::getUserId).collect(Collectors.toList());
            List<Long> catIds = page.getRecords().stream().map(AdoptionApplication::getCatId).collect(Collectors.toList());

            Map<Long, String> userNameMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
            Map<Long, String> catNameMap = catService.listByIds(catIds).stream()
                    .collect(Collectors.toMap(Cat::getId, Cat::getName));

            page.getRecords().forEach(app -> {
                app.setUserName(userNameMap.get(app.getUserId()));
                app.setCatName(catNameMap.get(app.getCatId()));
            });
        }
        return Result.success(page);
    }

    /**
     * 获取申请详情（权限控制：本人、志愿者、管理员）
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<AdoptionApplication> getById(@PathVariable Long id) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) {
            return Result.error("申请不存在");
        }
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String role = userDetails.getUser().getRole();
        // 普通用户只能看自己的
        if ("USER".equals(role) && !app.getUserId().equals(userDetails.getUserId())) {
            return Result.error("无权查看");
        }
        // 填充用户名和猫咪名
        User user = userService.getById(app.getUserId());
        if (user != null) app.setUserName(user.getUsername());
        Cat cat = catService.getById(app.getCatId());
        if (cat != null) app.setCatName(cat.getName());
        return Result.success(app);
    }

    /**
     * 初审（志愿者/管理员）
     */
    @PutMapping("/{id}/first-review")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> firstReview(@PathVariable Long id, @RequestParam Integer status, @RequestBody ReviewDTO dto) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) return Result.error("申请不存在");
        if (app.getStatus() != 0) {
            return Result.error("只能初审待初审的申请");
        }
        // 更新申请状态
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        app.setFirstReviewer(userDetails.getUserId());
        app.setFirstReviewTime(LocalDateTime.now());
        app.setFirstReviewRemark(dto.getRemark());
        app.setStatus(status);
        applicationService.updateById(app);

        // 发送通知给申请人
        String statusText = status == 1 ? "初审通过" : "初审拒绝";
        sendStatusChangeNotification(app.getUserId(), id, statusText);

        return Result.success("操作成功", null);
    }

    /**
     * 回访记录（可多次，志愿者/管理员）
     */
    @PostMapping("/{id}/follow-up")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<AdoptionFollowUp> addFollowUp(@PathVariable Long id, @RequestBody FollowUpDTO dto) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) return Result.error("申请不存在");
        // 状态应为 3-待回访 或 4-回访通过 等允许回访的状态，根据业务设计，这里允许状态为3时添加回访
        if (app.getStatus() != 3 && app.getStatus() != 10) {
            return Result.error("当前状态不允许回访");
        }

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        AdoptionFollowUp followUp = new AdoptionFollowUp();
        followUp.setApplicationId(id);
        followUp.setFollowUpTime(LocalDateTime.now());
        followUp.setFollowUpBy(userDetails.getUserId());
        followUp.setContent(dto.getContent());
        followUp.setNextFollowUpDate(dto.getNextFollowUpDate());
        followUpService.save(followUp);

        // 更新申请表中的最后一次回访信息（可选）
        app.setFollowUpContent(dto.getContent());
        app.setFollowUpTime(LocalDateTime.now());
        app.setFollowUpBy(userDetails.getUserId());
        applicationService.updateById(app);

        return Result.success("回访记录添加成功", followUp);
    }

    /**
     * 获取申请的所有回访记录
     */
    @GetMapping("/{id}/follow-ups")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<List<AdoptionFollowUp>> getFollowUps(@PathVariable Long id) {
        LambdaQueryWrapper<AdoptionFollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdoptionFollowUp::getApplicationId, id).orderByDesc(AdoptionFollowUp::getCreateTime);
        List<AdoptionFollowUp> list = followUpService.list(wrapper);
        // 填充回访人姓名
        if (!list.isEmpty()) {
            List<Long> userIds = list.stream().map(AdoptionFollowUp::getFollowUpBy).collect(Collectors.toList());
            Map<Long, String> userNameMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
            list.forEach(f -> f.setFollowUpByName(userNameMap.get(f.getFollowUpBy())));
        }
        return Result.success(list);
    }

    /**
     * 终审（管理员）
     */
    @PutMapping("/{id}/final-review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> finalReview(@PathVariable Long id, @RequestParam Integer status, @RequestBody ReviewDTO dto) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) return Result.error("申请不存在");
        if (app.getStatus() != 4 && app.getStatus() != 6) {
            return Result.error("当前状态不允许终审");
        }
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        app.setFinalReviewer(userDetails.getUserId());
        app.setFinalReviewTime(LocalDateTime.now());
        app.setFinalReviewRemark(dto.getRemark());
        app.setStatus(status);
        applicationService.updateById(app);

        // 发送通知
        String statusText = status == 7 ? "终审通过" : "终审拒绝";
        sendStatusChangeNotification(app.getUserId(), id, statusText);

        // 如果终审通过，更新猫咪状态
        if (status == 7) {
            Cat cat = catService.getById(app.getCatId());
            if (cat != null) {
                cat.setAdoptionStatus(3);
                catService.updateById(cat);
            }
        }
        return Result.success("操作成功", null);
    }

    /**
     * 签订协议（管理员/志愿者上传协议文件）
     */
    @PutMapping("/{id}/sign-agreement")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> signAgreement(@PathVariable Long id, @RequestParam String agreementUrl) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) return Result.error("申请不存在");
        if (app.getStatus() != 7) { // 终审通过后才能签协议
            return Result.error("请先完成终审");
        }
        app.setAgreementUrl(agreementUrl);
        app.setStatus(9); // 已签订协议
        applicationService.updateById(app);
        return Result.success("协议上传成功", null);
    }

    /**
     * 完成领养（管理员/志愿者确认领养完成）
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> complete(@PathVariable Long id) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) return Result.error("申请不存在");
        if (app.getStatus() != 9) {
            return Result.error("必须先签订协议");
        }
        app.setStatus(10);
        app.setCompleteTime(LocalDateTime.now());
        applicationService.updateById(app);

        sendStatusChangeNotification(app.getUserId(), id, "领养完成");
        return Result.success("领养完成", null);
    }

    /**
     * 取消申请（用户自己取消，或管理员取消）
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable Long id) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) return Result.error("申请不存在");

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String role = userDetails.getUser().getRole();

        // 普通用户只能取消自己的申请
        if ("USER".equals(role) && !app.getUserId().equals(userDetails.getUserId())) {
            return Result.error("无权取消");
        }
//        // 允许取消的状态：待初审、初审通过、待回访、待终审等，可根据需要设置
//        if (app.getStatus() > 2 && app.getStatus() != 3 && app.getStatus() != 6) {
//            return Result.error("当前状态不可取消");
//        }
//        app.setStatus(11); // 已取消
//        applicationService.updateById(app);
//        return Result.success("已取消", null);
        // 允许删除的状态：可修改，例如允许删除任何状态的申请，或仅允许未完成的
        if (app.getStatus() > 2 && app.getStatus() != 3 && app.getStatus() != 6) {
            return Result.error("当前状态不可删除");
        }
        // 物理删除
        applicationService.removeById(id);
        return Result.success("已删除", null);
    }

    /**
     * 开始回访（将状态从初审通过改为待回访）
     */
    @PutMapping("/{id}/start-follow-up")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> startFollowUp(@PathVariable Long id) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) {
            return Result.error("申请不存在");
        }
        if (app.getStatus() != 1) {
            return Result.error("只有初审通过的申请才能开始回访");
        }
        app.setStatus(3); // 待回访
        applicationService.updateById(app);
        return Result.success("已进入待回访状态", null);
    }

    /**
     * 回访通过（将状态从待回访改为回访通过）
     */
    @PutMapping("/{id}/pass-follow-up")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Void> passFollowUp(@PathVariable Long id) {
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) {
            return Result.error("申请不存在");
        }
        if (app.getStatus() != 3) {
            return Result.error("只有待回访状态才能执行此操作");
        }
        app.setStatus(4); // 回访通过
        applicationService.updateById(app);
        return Result.success("回访通过", null);
    }

    /**
     * 获取领养申请统计数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 总申请数
        long total = applicationService.count();
        result.put("total", total);

        // 2. 各状态数量
        List<Map<String, Object>> statusCounts = new ArrayList<>();
        for (int i = 0; i <= 11; i++) {
            LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdoptionApplication::getStatus, i);
            long count = applicationService.count(wrapper);
            if (count > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", i);
                map.put("count", count);
                statusCounts.add(map);
            }
        }
        result.put("statusCounts", statusCounts);

        // 3. 通过率（终审通过 + 已签订协议 + 已完成领养 的数量 / 总申请数）
        LambdaQueryWrapper<AdoptionApplication> passedWrapper = new LambdaQueryWrapper<>();
        passedWrapper.in(AdoptionApplication::getStatus, 7, 9, 10); // 终审通过、已签订协议、已完成领养
        long passedCount = applicationService.count(passedWrapper);
        double passRate = total == 0 ? 0 : (double) passedCount / total * 100;
        result.put("passRate", Math.round(passRate * 100) / 100.0); // 保留两位小数

        // 4. 每月申请量（最近6个月）
        List<Map<String, Object>> monthlyApplications = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            LambdaQueryWrapper<AdoptionApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(AdoptionApplication::getApplyTime, monthStart.atStartOfDay())
                    .le(AdoptionApplication::getApplyTime, monthEnd.atTime(23, 59, 59));
            long count = applicationService.count(wrapper);

            Map<String, Object> map = new HashMap<>();
            map.put("month", monthStart.getYear() + "-" + monthStart.getMonthValue());
            map.put("count", count);
            monthlyApplications.add(map);
        }
        result.put("monthlyApplications", monthlyApplications);

        return Result.success(result);
    }

    @PostMapping("/{id}/upload-agreement")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<String> uploadAgreement(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file) {
//        System.out.println("收到上传请求，申请ID：" + id);
//        System.out.println("文件名：" + file.getOriginalFilename());
//        System.out.println("文件大小：" + file.getSize());
        // 检查申请是否存在
        AdoptionApplication app = applicationService.getById(id);
        if (app == null) {
            return Result.error("申请不存在");
        }
        // 检查当前状态（可选：只有终审通过（7）才能上传协议）
        if (app.getStatus() != 7) {
            return Result.error("只有终审通过的申请才能上传协议");
        }
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "agreement_" + id + "_" + System.currentTimeMillis() + suffix;

            // 定义存储路径（根据您的实际路径调整）
            String uploadDir = uploadAgreementsPath;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // 构建可访问的URL（需配置静态资源映射）
            String fileUrl = "/uploads/agreements/" + fileName;

            // 更新申请表中的协议URL
            app.setAgreementUrl(fileUrl);
            app.setStatus(9);
            applicationService.updateById(app);

            return Result.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }

    // 在初审通过/拒绝等方法中发送通知
    private void sendStatusChangeNotification(Long userId, Long applicationId, String statusText) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("ADOPTION_STATUS_CHANGE");
        notification.setTitle("领养申请状态更新");
        notification.setContent("您的领养申请状态已变更为：" + statusText);
        notification.setRelatedId(applicationId);
        notification.setIsRead(0);
        notificationService.save(notification);
    }
}