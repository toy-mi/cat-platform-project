package org.example.catplatform.module.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProfileDTO {
    private String nickname;
    private String email;
    private String phone;
    private MultipartFile avatarFile;  // 头像文件，可选
    private String avatar;              // 头像URL，如果不上传文件则用这个
}