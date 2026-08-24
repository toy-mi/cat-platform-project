package org.example.catplatform.security.config;

import org.example.catplatform.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring Security 配置
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  // 启用方法级权限控制
public class SecurityConfig {
    // JWT 认证过滤器
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    // 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // 创建 AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    // 创建 SecurityFilterChain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（使用 JWT 无需 CSRF）
                .csrf().disable()
                // 无状态会话（不创建 session）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 配置请求授权
                .authorizeRequests()
                // 放行 OPTIONS 预检请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 公开接口：用户注册登录
//                .antMatchers("/api/user/register", "/api/user/login").permitAll()
//                // 公开接口：猫咪照片列表（若需公开）
//                .antMatchers("/api/cat-photos/list/**").permitAll()
//                // 静态资源（上传文件）
//                .antMatchers("/uploads/**").permitAll()
//                // 公开接口：猫咪信息查询（列表、详情等）
//                .antMatchers("/api/cats/**").permitAll()
//                // 公开接口：募捐活动查询（列表、详情等）
//                .antMatchers("/api/donations/campaigns/**").permitAll()
//                // 公开接口：社区动态查询（列表、详情等） – 允许匿名浏览
//                .antMatchers("/api/community/posts/**").permitAll()
//                // 任务接口需要登录（匹配所有 /api/tasks 下的路径）
////                .antMatchers("/api/tasks/**").authenticated()
//                // 管理员接口需要 ADMIN 角色
//                .antMatchers("/api/admin/**").hasRole("ADMIN")
//                // 志愿者接口需要 VOLUNTEER 角色
//                .antMatchers("/api/volunteer/**").hasRole("VOLUNTEER")
//
//                .antMatchers("/api/cats/statistics").permitAll()               // 首页统计
//                .antMatchers("/api/tasks").permitAll()                         // 任务列表（公开）
//                .antMatchers("/api/tasks/**").permitAll()                      // 任务详情（公开）
//                .antMatchers("/api/announcements/published/**").permitAll()
//                .antMatchers("/api/cats/statistics").permitAll()
//                .antMatchers("/api/adoptions/statistics").permitAll()
//                .antMatchers("/api/cats/**").permitAll()
//                .antMatchers("/api/community/posts/**").permitAll()
//                .antMatchers("/api/donations/campaigns/**").permitAll()

                // 公开接口,无需登录即可访问
                .antMatchers("/api/user/register", "/api/user/login").permitAll()
                .antMatchers("/api/cat-photos/list/**").permitAll()
                .antMatchers("/uploads/**").permitAll()
                .antMatchers("/api/cats/**").permitAll()
                .antMatchers("/api/cats/statistics").permitAll()          // 猫咪统计
                .antMatchers("/api/donations/campaigns/**").permitAll()
                .antMatchers("/api/community/posts/**").permitAll()
                .antMatchers("/api/announcements/published/**").permitAll()
                .antMatchers("/api/tasks").permitAll()                    // 任务列表（公开）
                .antMatchers("/api/tasks/**").permitAll()                 // 任务详情
                // 管理员接口需要 ADMIN 角色
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // 志愿者接口需要 VOLUNTEER 角色
                .antMatchers("/api/volunteer/**").hasRole("VOLUNTEER")
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
                .and()
                // 在用户名密码认证前拦截请求，验证 Token,将 JWT 过滤器添加到 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}