package org.example.catplatform.module.donation.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DonationDTO {
    private Long campaignId;        // 可选
    private String donationType;    // MONEY/GOODS
    private BigDecimal amount;      // 资金金额（当类型为MONEY时必填）
    private String goodsName;       // 物资名称（当类型为GOODS时必填）
    private Integer goodsQuantity;  // 物资数量
    private String goodsUnit;       // 物资单位
    private String remark;          // 备注
    private String attachmentUrl;  // 附件URL
}