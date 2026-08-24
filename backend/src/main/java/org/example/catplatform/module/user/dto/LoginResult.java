package org.example.catplatform.module.user.dto;

import lombok.Data;
import org.example.catplatform.module.user.entity.User;

@Data
public class LoginResult {
    private String token;
    private User userInfo;  // 注意：返回前请清除密码
}