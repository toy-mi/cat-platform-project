package org.example.catplatform.module.cat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cat_location")
public class CatLocation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long catId;

    private String locationDesc;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer isCurrent;  // 0-否，1-是

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}