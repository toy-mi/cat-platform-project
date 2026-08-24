package org.example.catplatform.module.adoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("adoption_follow_up")
public class AdoptionFollowUp {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;
    private LocalDateTime followUpTime;
    private Long followUpBy;
    private String content;
    private LocalDate nextFollowUpDate;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String followUpByName; // 回访人姓名
}