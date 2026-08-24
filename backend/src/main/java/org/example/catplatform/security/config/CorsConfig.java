package org.example.catplatform.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 跨域配置
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    // 添加跨域映射
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 对所有接口生效
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")  // 允许你的前端地址（注意现在是5174）,必须包含你前端实际运行的地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方法
                .allowedMethods("*")
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带凭证（如Cookie、Authorization头）
                .maxAge(3600);  // 预检请求的有效期，单位秒
    }
}