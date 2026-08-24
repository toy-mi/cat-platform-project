package org.example.catplatform.module.cat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cat")
public class Cat {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatar;

    private String breed;

    private Integer gender;      // 0-未知，1-公，2-母

    private String personality;

    private String description;

    private String healthStatus;  // 健康状况摘要

    private Integer neuterStatus; // 0-未知，1-已绝育，2-未绝育

    private Integer adoptionStatus; // 0-在养，1-待领养，2-待审核，3-已领养，4-失踪，5-去世

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long creatorId;       // 创建人ID
}