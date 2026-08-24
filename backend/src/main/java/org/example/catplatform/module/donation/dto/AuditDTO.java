package org.example.catplatform.module.donation.dto;

import lombok.Data;

@Data
public class AuditDTO {
    private Integer status;    // 1-通过，2-拒绝
    private String remark;
}