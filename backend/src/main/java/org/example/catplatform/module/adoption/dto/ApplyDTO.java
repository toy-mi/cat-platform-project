package org.example.catplatform.module.adoption.dto;

import lombok.Data;

// 领养申请
//不暴露敏感字段（如密码、身份证号）
//限制用户可以提交哪些字段
@Data
public class ApplyDTO {
    private Long catId;
    private String applicationData; // JSON 字符串，包含家庭情况、住房、收入等信息


}