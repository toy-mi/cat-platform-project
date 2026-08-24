package org.example.catplatform.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.catplatform.module.user.entity.User;
import org.example.catplatform.module.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * @param user 前端传过来的用户信息（不含 id, createTime, updateTime, role, status）
     * @return 注册成功返回用户信息（脱敏），失败抛出异常或返回null
     */
    public User register(User user) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. 补全默认字段
        user.setRole("USER");          // 默认普通用户
        user.setStatus(1);              // 正常状态
        // createTime 和 updateTime 由自动填充处理

        // 4. 插入数据库
        userMapper.insert(user);

        // 5. 返回前清除敏感信息（密码）
        user.setPassword(null);
        return user;
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param rawPassword 明文密码
     * @return 登录成功返回用户信息（脱敏），失败返回null
     */
    public User login(String username, String rawPassword) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return null;
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return null;
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 4. 清除密码后返回
        user.setPassword(null);
        return user;
    }
}