package org.example.catplatform.module.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String content;

    private Long relatedId;

    private Integer isRead;  // 0-未读，1-已读

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}