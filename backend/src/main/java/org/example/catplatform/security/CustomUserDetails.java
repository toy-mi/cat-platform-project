package org.example.catplatform.security;

import lombok.Data;
import org.example.catplatform.module.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// 自定义用户详情类
@Data
public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色转换为 GrantedAuthority，注意 Spring Security 角色默认前缀 "ROLE_"
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }// 账户是否未过期

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }// 账户是否未锁定

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }// 密码是否未过期

    @Override
    public boolean isEnabled() {
        return user.getStatus() == 1;  // 账户是否启用
    }

    // 获取用户ID的便捷方法
    public Long getUserId() {
        return user.getId();
    }// 获取用户ID
}