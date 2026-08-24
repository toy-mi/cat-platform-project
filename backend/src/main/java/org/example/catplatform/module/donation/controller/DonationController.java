package org.example.catplatform.module.donation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.cat.entity.Cat;
import org.example.catplatform.module.cat.service.CatService;
import org.example.catplatform.module.donation.dto.AuditDTO;
import org.example.catplatform.module.donation.dto.CampaignDTO;
import org.example.catplatform.module.donation.dto.DonationDTO;
import org.example.catplatform.module.donation.entity.Donation;
import org.example.catplatform.module.donation.entity.DonationCampaign;
import org.example.catplatform.module.donation.service.DonationCampaignService;
import org.example.catplatform.module.donation.service.DonationService;
import org.example.catplatform.module.notification.entity.Notification;
import org.example.catplatform.module.notification.service.NotificationService;
import org.example.catplatform.module.user.entity.User;
import org.example.catplatform.module.user.service.UserService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;



@RestController
@RequestMapping("/api/donations")
public class DonationController {

    @Autowired
    private DonationCampaignService campaignService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private CatService catService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Value("${file.upload.donation}")
    private String uploadDonationPath;

    // 捐赠 VO 类
    @Data
    static class DonationVO {
        private Long id;
        private Long userId;
        private Long campaignId;
        private String donationType;
        private BigDecimal amount;
        private String goodsName;
        private Integer goodsQuantity;
        private String goodsUnit;
        private String attachmentUrl;
        private Integer status;
        private LocalDateTime donationTime;
        private String remark;
        private String userName;
        private String campaignTitle;
        private LocalDateTime auditTime;
        private Long auditBy;
    }

    // ========== 募捐活动管理 ==========

    /**
     * 分页查询募捐活动列表（公开）
     */
    @GetMapping("/campaigns")
    public Result<Page<DonationCampaign>> pageCampaigns(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long catId,
            @RequestParam(required = false) String keyword) {
        Page<DonationCampaign> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DonationCampaign> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(DonationCampaign::getStatus, status);
        }
        if (catId != null) {
            wrapper.eq(DonationCampaign::getCatId, catId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(DonationCampaign::getTitle, keyword)
                    .or()
                    .like(DonationCampaign::getDescription, keyword));
        }
        wrapper.orderByDesc(DonationCampaign::getCreateTime);
        campaignService.page(page, wrapper);

        // 填充猫咪名称并计算进度
        if (!page.getRecords().isEmpty()) {
            List<Long> catIds = page.getRecords().stream()
                    .map(DonationCampaign::getCatId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            if (!catIds.isEmpty()) {
                Map<Long, String> catNameMap = catService.listByIds(catIds).stream()
                        .collect(Collectors.toMap(Cat::getId, Cat::getName));
                page.getRecords().forEach(c -> {
                    if (c.getCatId() != null) {
                        c.setCatName(catNameMap.get(c.getCatId()));
                    }
                    // 计算进度
                    if (c.getTargetAmount() != null && c.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal progress = c.getCurrentAmount() == null ? BigDecimal.ZERO
                                : c.getCurrentAmount().divide(c.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        c.setProgress(progress);
                    } else {
                        c.setProgress(BigDecimal.ZERO);
                    }
                });
            }
        }
        return Result.success(page);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/campaigns/{id}")
    public Result<DonationCampaign> getCampaign(@PathVariable Long id) {
        DonationCampaign campaign = campaignService.getById(id);
        if (campaign == null) {
            return Result.error("活动不存在");
        }
        // 填充猫咪名称和进度
        if (campaign.getCatId() != null) {
            Cat cat = catService.getById(campaign.getCatId());
            if (cat != null) campaign.setCatName(cat.getName());
        }
        if (campaign.getTargetAmount() != null && campaign.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = campaign.getCurrentAmount() == null ? BigDecimal.ZERO
                    : campaign.getCurrentAmount().divide(campaign.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            campaign.setProgress(progress);
        }
        return Result.success(campaign);
    }

    /**
     * 创建募捐活动（管理员）
     */
    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DonationCampaign> createCampaign(@RequestBody CampaignDTO dto) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        DonationCampaign campaign = new DonationCampaign();
        campaign.setTitle(dto.getTitle());
        campaign.setDescription(dto.getDescription());
        campaign.setTargetAmount(dto.getTargetAmount());
        campaign.setCurrentAmount(BigDecimal.ZERO);
        campaign.setStartDate(dto.getStartDate());
        campaign.setEndDate(dto.getEndDate());
        campaign.setCatId(dto.getCatId());
        campaign.setStatus(dto.getStatus() != null ? dto.getStatus() : 0); // 默认为筹备中
        campaign.setCreateBy(userDetails.getUserId());
        campaignService.save(campaign);
        return Result.success("创建成功", campaign);
    }

    /**
     * 更新募捐活动（管理员）
     */
    @PutMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DonationCampaign> updateCampaign(@PathVariable Long id, @RequestBody CampaignDTO dto) {
        DonationCampaign campaign = campaignService.getById(id);
        if (campaign == null) {
            return Result.error("活动不存在");
        }
        campaign.setTitle(dto.getTitle());
        campaign.setDescription(dto.getDescription());
        campaign.setTargetAmount(dto.getTargetAmount());
        campaign.setStartDate(dto.getStartDate());
        campaign.setEndDate(dto.getEndDate());
        campaign.setCatId(dto.getCatId());
        if (dto.getStatus() != null) {
            campaign.setStatus(dto.getStatus());
        }
        campaignService.updateById(campaign);
        return Result.success("更新成功", campaign);
    }

    /**
     * 删除募捐活动（管理员）
     */
    @DeleteMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCampaign(@PathVariable Long id) {
        // 检查是否有捐赠记录关联（可选，可根据业务决定）
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Donation::getCampaignId, id);
        if (donationService.count(wrapper) > 0) {
            return Result.error("该活动已有捐赠记录，无法删除");
        }
        campaignService.removeById(id);
        return Result.success("删除成功", null);
    }

    // ========== 捐赠记录管理 ==========

    /**
     * 用户捐赠（资金/物资）
     */
    @PostMapping("/donate")
    @PreAuthorize("isAuthenticated()")
    public Result<Donation> donate(@RequestBody DonationDTO dto) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // 参数校验
        if ("MONEY".equals(dto.getDonationType())) {
            if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("请输入有效的捐赠金额");
            }
        } else if ("GOODS".equals(dto.getDonationType())) {
            if (dto.getGoodsName() == null || dto.getGoodsName().isEmpty()) {
                return Result.error("请输入物资名称");
            }
            if (dto.getGoodsQuantity() == null || dto.getGoodsQuantity() <= 0) {
                return Result.error("请输入有效的物资数量");
            }
        } else {
            return Result.error("无效的捐赠类型");
        }

        // 如果指定了活动，检查活动是否存在且进行中
        if (dto.getCampaignId() != null) {
            DonationCampaign campaign = campaignService.getById(dto.getCampaignId());
            if (campaign == null) {
                return Result.error("募捐活动不存在");
            }
            if (campaign.getStatus() != 1) {
                return Result.error("该活动不在进行中，暂无法捐赠");
            }
        }

        Donation donation = new Donation();
        donation.setUserId(userDetails.getUserId());
        donation.setCampaignId(dto.getCampaignId());
        donation.setDonationType(dto.getDonationType());
        donation.setAttachmentUrl(dto.getAttachmentUrl());
        donation.setAmount("MONEY".equals(dto.getDonationType()) ? dto.getAmount() : null);
        donation.setGoodsName("GOODS".equals(dto.getDonationType()) ? dto.getGoodsName() : null);
        donation.setGoodsQuantity("GOODS".equals(dto.getDonationType()) ? dto.getGoodsQuantity() : null);
        donation.setGoodsUnit(dto.getGoodsUnit());
        donation.setStatus(0);  // 待审核
        donation.setDonationTime(LocalDateTime.now());
        donation.setRemark(dto.getRemark());
        donationService.save(donation);

        return Result.success("捐赠成功，等待管理员审核", donation);
    }

    /**
     * 分页查询捐赠记录（公开，可筛选活动）
     */
    @GetMapping("/records")
    public Result<Page<Donation>> pageDonations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) Integer status) {
        Page<Donation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        if (campaignId != null) {
            wrapper.eq(Donation::getCampaignId, campaignId);
        }
        if (status != null) {
            wrapper.eq(Donation::getStatus, status);
        }
        wrapper.orderByDesc(Donation::getDonationTime);
        donationService.page(page, wrapper);

        // 填充捐赠人姓名和活动标题
        if (!page.getRecords().isEmpty()) {
            List<Long> userIds = page.getRecords().stream().map(Donation::getUserId).collect(Collectors.toList());
            Map<Long, String> userNameMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));

            List<Long> campaignIds = page.getRecords().stream()
                    .map(Donation::getCampaignId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            Map<Long, String> campaignTitleMap = campaignIds.isEmpty() ? Map.of() :
                    campaignService.listByIds(campaignIds).stream()
                            .collect(Collectors.toMap(DonationCampaign::getId, DonationCampaign::getTitle));

            page.getRecords().forEach(d -> {
                d.setUserName(userNameMap.get(d.getUserId()));
                if (d.getCampaignId() != null) {
                    d.setCampaignTitle(campaignTitleMap.get(d.getCampaignId()));
                }
            });
        }
        return Result.success(page);
    }

    /**
     * 获取活动的捐赠记录（按时间倒序）
     */
    @GetMapping("/campaigns/{campaignId}/records")
    public Result<Page<Donation>> getCampaignDonations(
            @PathVariable Long campaignId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<Donation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Donation::getCampaignId, campaignId);
        if (status != null) wrapper.eq(Donation::getStatus, status);
        if (userId != null) wrapper.eq(Donation::getUserId, userId);
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(Donation::getDonationTime, startTime + " 00:00:00");
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(Donation::getDonationTime, endTime + " 23:59:59");
        }
        wrapper.orderByDesc(Donation::getDonationTime);
        donationService.page(page, wrapper);
        // 填充捐赠人姓名
        if (!page.getRecords().isEmpty()) {
            List<Long> userIds = page.getRecords().stream().map(Donation::getUserId).collect(Collectors.toList());
            Map<Long, String> userNameMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
            page.getRecords().forEach(d -> d.setUserName(userNameMap.get(d.getUserId())));
        }
        return Result.success(page);
    }

    // ========== 管理员审核捐赠 ==========

    /**
     * 审核捐赠记录（管理员）
     */
    @PutMapping("/records/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditDonation(@PathVariable Long id, @RequestBody AuditDTO dto) {
        Donation donation = donationService.getById(id);
        if (donation == null) {
            return Result.error("捐赠记录不存在");
        }
        if (donation.getStatus() != 0) {
            return Result.error("该记录已审核");
        }

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        donation.setStatus(dto.getStatus());
        donation.setAuditTime(LocalDateTime.now());
        donation.setAuditBy(userDetails.getUserId());
        donation.setRemark(dto.getRemark());
        donationService.updateById(donation);

        // 如果审核通过且为资金捐赠，更新对应活动的当前金额
        if (dto.getStatus() == 1 && "MONEY".equals(donation.getDonationType()) && donation.getCampaignId() != null) {
            DonationCampaign campaign = campaignService.getById(donation.getCampaignId());
            if (campaign != null) {
                // 更新活动已筹金额
                BigDecimal newCurrent = campaign.getCurrentAmount() == null ? donation.getAmount()
                        : campaign.getCurrentAmount().add(donation.getAmount());
                campaign.setCurrentAmount(newCurrent);
                campaignService.updateById(campaign);

                // 自动状态流转：如果已筹金额 >= 目标金额，且活动状态为“进行中”，则改为“已完成”
                if (campaign.getStatus() == 1 && newCurrent.compareTo(campaign.getTargetAmount()) >= 0) {
                    campaign.setStatus(4); // 已完成
                    campaignService.updateById(campaign);
                }
            }
        }

        // 发送通知给捐赠人
        Notification notification = new Notification();
        notification.setUserId(donation.getUserId());
        notification.setType("DONATION_AUDIT");
        notification.setTitle("捐赠审核结果");
        String statusText = dto.getStatus() == 1 ? "审核通过" : "审核拒绝";
        // 如果是通过，可以改为感谢信
        if (dto.getStatus() == 1) {
            notification.setTitle("感谢您的捐赠");
            notification.setContent("您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：" +
                    ("MONEY".equals(donation.getDonationType()) ? "金额￥" + donation.getAmount() :
                            "物资：" + donation.getGoodsName() + " × " + donation.getGoodsQuantity() + " " + donation.getGoodsUnit()));
        } else {
            notification.setContent("您的捐赠记录已" + statusText + (dto.getRemark() != null ? "，备注：" + dto.getRemark() : ""));
        }
        notification.setRelatedId(donation.getId());
        notification.setIsRead(0);
        notificationService.save(notification);

        return Result.success("审核完成", null);
    }



    /**
     * 删除捐赠记录（管理员，可选）
     */
    @DeleteMapping("/records/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteDonation(@PathVariable Long id) {
        donationService.removeById(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadDonationAttachment(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "donation_" + System.currentTimeMillis() + suffix;
            String uploadPath = uploadDonationPath;
            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            String fileUrl = "/uploads/donations/" + fileName;
            return Result.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 每月捐赠趋势（近12个月）
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Donation::getDonationTime, monthStart.atStartOfDay())
                    .le(Donation::getDonationTime, monthEnd.atTime(23, 59, 59))
                    .eq(Donation::getStatus, 1); // 仅统计已审核的捐赠
            BigDecimal totalAmount = donationService.list(wrapper).stream()
                    .filter(d -> "MONEY".equals(d.getDonationType()))
                    .map(Donation::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> map = new HashMap<>();
            map.put("month", monthStart.getYear() + "-" + monthStart.getMonthValue());
            map.put("amount", totalAmount);
            monthlyTrend.add(map);
        }
        result.put("monthlyTrend", monthlyTrend);

        // 2. 资金/物资捐赠占比（按审核通过统计）
        LambdaQueryWrapper<Donation> wrapperMoney = new LambdaQueryWrapper<>();
        wrapperMoney.eq(Donation::getDonationType, "MONEY").eq(Donation::getStatus, 1);
        long moneyCount = donationService.count(wrapperMoney);
        LambdaQueryWrapper<Donation> wrapperGoods = new LambdaQueryWrapper<>();
        wrapperGoods.eq(Donation::getDonationType, "GOODS").eq(Donation::getStatus, 1);
        long goodsCount = donationService.count(wrapperGoods);
        List<Map<String, Object>> typeRatio = new ArrayList<>();
        Map<String, Object> money = new HashMap<>();
        money.put("name", "资金");
        money.put("value", moneyCount);
        typeRatio.add(money);
        Map<String, Object> goods = new HashMap<>();
        goods.put("name", "物资");
        goods.put("value", goodsCount);
        typeRatio.add(goods);
        result.put("typeRatio", typeRatio);

        return Result.success(result);
    }

    /**
     * 获取当前用户的捐赠记录（分页）
     */
    @GetMapping("/my-donations")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<Donation>> getMyDonations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Page<Donation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Donation::getUserId, userDetails.getUserId())
                .orderByDesc(Donation::getDonationTime);
        donationService.page(page, wrapper);

        // 填充活动标题
        if (!page.getRecords().isEmpty()) {
            List<Long> campaignIds = page.getRecords().stream()
                    .map(Donation::getCampaignId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!campaignIds.isEmpty()) {
                Map<Long, String> campaignTitleMap = campaignService.listByIds(campaignIds).stream()
                        .collect(Collectors.toMap(DonationCampaign::getId, DonationCampaign::getTitle));
                page.getRecords().forEach(d -> {
                    if (d.getCampaignId() != null) {
                        d.setCampaignTitle(campaignTitleMap.get(d.getCampaignId()));
                    }
                });
            }
        }
        return Result.success(page);
    }

    @GetMapping("/goods-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> getGoodsStatistics() {
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Donation::getDonationType, "GOODS")
                .eq(Donation::getStatus, 1);  // 只统计已审核通过的物资
        List<Donation> goodsList = donationService.list(wrapper);

        Map<String, Map<String, Object>> statsMap = new HashMap<>();
        for (Donation d : goodsList) {
            String name = d.getGoodsName();
            if (name == null) continue;
            statsMap.putIfAbsent(name, new HashMap<>());
            Map<String, Object> stat = statsMap.get(name);
            stat.put("goodsName", name);
            Integer totalQuantity = (Integer) stat.getOrDefault("totalQuantity", 0);
            totalQuantity += d.getGoodsQuantity() != null ? d.getGoodsQuantity() : 0;
            stat.put("totalQuantity", totalQuantity);
            Integer count = (Integer) stat.getOrDefault("count", 0);
            count++;
            stat.put("count", count);
            // 记录捐赠人（去重）
            Set<String> donors = (Set<String>) stat.getOrDefault("donors", new HashSet<>());
            User user = userService.getById(d.getUserId());
            if (user != null) donors.add(user.getUsername());
            stat.put("donors", donors);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> stat : statsMap.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("goodsName", stat.get("goodsName"));
            item.put("totalQuantity", stat.get("totalQuantity"));
            item.put("count", stat.get("count"));
            item.put("donors", stat.get("donors"));
            result.add(item);
        }
        result.sort((a, b) -> ((Integer)b.get("totalQuantity")).compareTo((Integer)a.get("totalQuantity")));
        return Result.success(result);
    }

    /**
     * 获取指定用户的捐赠记录（分页）
     */
    @GetMapping("/user/{userId}/donations")
    public Result<Page<DonationVO>> getUserDonations(@PathVariable Long userId,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Donation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Donation::getUserId, userId)
                .eq(Donation::getStatus, 1)  // 只显示已审核通过
                .orderByDesc(Donation::getDonationTime);
        donationService.page(page, wrapper);

        // 转换为 VO，添加活动标题等
        List<DonationVO> voList = page.getRecords().stream().map(donation -> {
            DonationVO vo = new DonationVO();
            BeanUtils.copyProperties(donation, vo);

            // 填充捐赠人姓名
            User user = userService.getById(donation.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
            }

            // 填充活动标题
            if (donation.getCampaignId() != null) {
                DonationCampaign campaign = campaignService.getById(donation.getCampaignId());
                if (campaign != null) {
                    vo.setCampaignTitle(campaign.getTitle());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        Page<DonationVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(voList);
        return Result.success(resultPage);
    }
}