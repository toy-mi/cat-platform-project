package org.example.catplatform.module.adoption.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FollowUpDTO {
    private String content;
    private LocalDate nextFollowUpDate; // 下次回访日期
}