package org.example.catplatform.module.user.dto;

import lombok.Data;

@Data
public class UserPublicVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    // 根据需求决定是否公开邮箱、手机号，这里选择不公开
     private String email;
     private String phone;
}