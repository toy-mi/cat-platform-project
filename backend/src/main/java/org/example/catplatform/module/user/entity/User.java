package org.example.catplatform.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库表 user
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;                     // 用户ID，主键，自增

    private String username;              // 用户名，唯一索引

    private String password;              // 密码

    private String nickname;               // 昵称

    private String avatar;                 // 头像URL

    private String email;                  // 邮箱

    private String phone;                  // 手机号

    private String role;                   // 角色：USER-普通用户，VOLUNTEER-志愿者，ADMIN-管理员

    private Integer status;                // 状态：0-禁用，1-正常

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;      // 创建时间，插入时填充

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;      // 更新时间，插入和更新时填充
}