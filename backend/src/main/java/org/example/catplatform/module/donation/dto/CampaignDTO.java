package org.example.catplatform.module.donation.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CampaignDTO {
    private String title;
    private String description;
    private BigDecimal targetAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long catId;
    private Integer status;  // 可选
}