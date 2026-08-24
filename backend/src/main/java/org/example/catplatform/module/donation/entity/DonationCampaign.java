package org.example.catplatform.module.donation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("donation_campaign")
public class DonationCampaign {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long catId;          // 关联猫咪ID，可为空（全体）
    private Integer status;       // 0-筹备中，1-进行中，2-已结束，3-已取消

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long createBy;        // 创建人ID

    // 非数据库字段，用于前端显示
    @TableField(exist = false)
    private String catName;
    @TableField(exist = false)
    private BigDecimal progress;   // 进度百分比
}