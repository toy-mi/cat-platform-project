package org.example.catplatform.module.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;          // 可选标题
    private String content;

    private String images;          // 图片URL列表，可JSON或逗号分隔

    private String locationDesc;    // 位置描述
    private BigDecimal latitude;
    private BigDecimal longitude;

    private Integer likeCount;      // 点赞数（冗余）
    private Integer commentCount;    // 评论数（冗余）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段，用于关联查询
    @TableField(exist = false)
    private String userName;        // 发布者用户名
    @TableField(exist = false)
    private String userAvatar;       // 发布者头像
    @TableField(exist = false)
    private Boolean likedByCurrent;  // 当前用户是否已点赞
}