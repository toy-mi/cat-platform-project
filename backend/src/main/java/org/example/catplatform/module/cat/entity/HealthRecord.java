package org.example.catplatform.module.cat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long catId;

    private String recordType;     // VACCINE-疫苗，NEUTER-绝育，CHECKUP-体检，OTHER-其他

    private LocalDate recordDate;  // 记录日期

    private String description;    // 描述

    private String vetName;        // 兽医/机构

    private String attachmentUrl;  // 附件URL（暂缓实现）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long createBy;         // 创建人ID
}