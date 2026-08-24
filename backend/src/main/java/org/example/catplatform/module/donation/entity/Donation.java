package org.example.catplatform.module.donation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("donation")
public class Donation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long campaignId;       // 关联活动ID，可为空（不指定活动）
    private String donationType;    // MONEY-资金，GOODS-物资
    private BigDecimal amount;      // 捐赠金额（资金时有效）
    private String goodsName;       // 物资名称
    private Integer goodsQuantity;  // 物资数量
    private String goodsUnit;       // 物资单位（如“袋”、“个”）
    private Integer status;         // 0-待审核，1-已审核，2-已拒绝
    private LocalDateTime donationTime;
    private LocalDateTime auditTime;
    private Long auditBy;
    private String remark;
    private String attachmentUrl;

    // 非数据库字段
    @TableField(exist = false)
    private String userName;        // 捐赠人姓名
    @TableField(exist = false)
    private String campaignTitle;   // 活动标题
}