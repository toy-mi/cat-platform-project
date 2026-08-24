package org.example.catplatform.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


//配置静态资源映射（让上传的文件可以通过 URL 访问）
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.community}")
    private String uploadCommunityPath;

    @Value("${file.upload.avatar}")
    private String uploadAvatarPath;

    @Value("${file.upload.task}")
    private String uploadTaskPath;

    @Value("${file.upload.donation}")
    private String uploadDonationPath;

    @Value("${file.upload.agreements}")
    private String uploadAgreementsPath;

    // 静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("Static resource mapping: /uploads/** -> file:" + uploadPath);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        registry.addResourceHandler("/uploads/agreements/**")
                .addResourceLocations("file:" + uploadAgreementsPath + "/");
        registry.addResourceHandler("/uploads/community/**")
                .addResourceLocations("file:" + uploadCommunityPath + "/");
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + uploadAvatarPath + "/");
        registry.addResourceHandler("/uploads/donations/**")
                .addResourceLocations("file:" + uploadDonationPath + "/");
        registry.addResourceHandler("/uploads/tasks/**")
                .addResourceLocations("file:" + uploadTaskPath + "/");
    }
}