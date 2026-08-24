package org.example.catplatform.module.adoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
// 领养申请
@Data
@TableName("adoption_application")
public class AdoptionApplication {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long catId;
    private LocalDateTime applyTime;
    // 领养状态
    private Integer status;  // 0-待初审，1-初审通过，2-初审拒绝，3-待回访，4-回访通过，5-回访失败，6-待终审，7-终审通过，8-终审拒绝，9-已签订协议，10-已完成领养，11-已取消

    private String applicationData; // JSON 表单数据

    private Long firstReviewer;
    private LocalDateTime firstReviewTime;
    private String firstReviewRemark;

    private String followUpContent;   // 最后一次回访内容（冗余，便于快速查看）
    private LocalDateTime followUpTime;
    private Long followUpBy;

    private Long finalReviewer;
    private LocalDateTime finalReviewTime;
    private String finalReviewRemark;

    private String agreementUrl;
    private LocalDateTime completeTime;

    // 非数据库字段，用于关联查询
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String catName;
}