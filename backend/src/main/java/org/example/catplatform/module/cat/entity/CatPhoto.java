package org.example.catplatform.module.cat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cat_photo")
public class CatPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long catId;

    private String photoUrl;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}