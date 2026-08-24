package org.example.catplatform.module.announcement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

// 公告
@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer priority;    // 0-普通，1-重要

    private Integer status;      // 0-草稿，1-已发布

    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long createBy;

    // 非数据库字段，用于显示创建人用户名
    @TableField(exist = false)
    private String creatorName;
}