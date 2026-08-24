package org.example.catplatform.module.adoption.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private String remark;   // 审核意见
    // 其他字段视具体操作而定，如初审、终审、回访等
}