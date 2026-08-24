package org.example.catplatform.module.user.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.common.utils.JwtUtil;
import org.example.catplatform.module.user.dto.LoginResult;
import org.example.catplatform.module.user.dto.UserUpdateDTO;
import org.example.catplatform.module.user.entity.User;
import org.example.catplatform.module.user.service.UserService;
import org.example.catplatform.security.CustomUserDetails;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.example.catplatform.module.user.dto.UserPublicVO;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.upload.avatar}")
    private String avatarUploadPath;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 用户公开信息 VO
//    @Data
//    static class UserPublicVO {
//        private Long id;
//        private String username;
//        private String nickname;
//        private String avatar;
//        private String role;
//    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        try {
            User registered = userService.register(user);
            return Result.success("注册成功", registered);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setPassword(null);
        LoginResult loginResult = new LoginResult();
        loginResult.setToken(token);
        loginResult.setUserInfo(user);
        return Result.success("登录成功", loginResult);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            User user = ((CustomUserDetails) principal).getUser();
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("无法获取用户信息");
    }

    /**
     * 申请成为志愿者
     */
    @PostMapping("/apply-volunteer")
    @PreAuthorize("isAuthenticated()")
    public Result<String> applyVolunteer() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User user = userDetails.getUser();

        if ("VOLUNTEER".equals(user.getRole()) || "ADMIN".equals(user.getRole())) {
            return Result.error("您已经是志愿者或管理员，无需申请");
        }

        user.setRole("VOLUNTEER");
        userService.updateById(user);
        return Result.success("恭喜，您已成为志愿者！");
    }

    /**
     * 获取所有志愿者用户
     */
    @GetMapping("/volunteers")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public Result<List<User>> getVolunteers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "VOLUNTEER").eq(User::getStatus, 1);
        List<User> list = userService.list(wrapper);
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    /**
     * 分页获取用户（管理员/志愿者用）
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTEER')")
    public Result<Page<User>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        userService.page(page);
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    /**
     * 更新用户资料（昵称、邮箱、手机）
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<User> updateProfile(@RequestBody UserUpdateDTO dto) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User user = userService.getById(userDetails.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 邮箱唯一性校验（可选，如果允许重复则删除）
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, dto.getEmail()).ne(User::getId, user.getId());
            if (userService.count(wrapper) > 0) {
                return Result.error("邮箱已被其他用户使用");
            }
        }
        // 更新字段
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        userService.updateById(user);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "avatar_" + System.currentTimeMillis() + suffix;

            // 确保目录存在
            File dir = new File(avatarUploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // 构建访问URL（需配置静态资源映射）
            String avatarUrl = "/uploads/avatars/" + fileName;

            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            User user = userService.getById(userDetails.getUserId());
            user.setAvatar(avatarUrl);
            userService.updateById(user);

            return Result.success("上传成功", avatarUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }

    // 内部类接收登录请求
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @GetMapping("/admin/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String username,
                                        @RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String email,
                                        @RequestParam(required = false) String role,
                                        @RequestParam(required = false) Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (nickname != null && !nickname.isEmpty()) {
            wrapper.like(User::getNickname, nickname);
        }
        if (email != null && !email.isEmpty()) {
            wrapper.like(User::getEmail, email);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        userService.page(page, wrapper);
        // 脱敏，不返回密码
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    // 添加用户
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> addUser(@RequestBody User user) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 默认值
        if (user.getRole() == null) user.setRole("USER");
        if (user.getStatus() == null) user.setStatus(1);
        userService.save(user);
        user.setPassword(null);
        return Result.success("添加成功", user);
    }

    // 更新用户信息（不含密码）
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existing = userService.getById(id);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        // 只更新允许的字段
        existing.setNickname(user.getNickname());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole());
        existing.setStatus(user.getStatus());
        // 头像等其他字段也可根据需要更新
        userService.updateById(existing);
        existing.setPassword(null);
        return Result.success("更新成功", existing);
    }

    // 重置密码
    @PutMapping("/admin/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        return Result.success("密码重置成功", null);
    }

    // 禁用/启用用户（软删除）
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            return Result.error("状态值无效");
        }
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        return Result.success("状态更新成功", null);
    }

    // 物理删除用户（谨慎，仅作备用）
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // 检查是否有外键关联，如果有，可根据业务决定是否允许删除
        // 这里直接物理删除（注意外键级联）
        userService.removeById(id);
        return Result.success("删除成功", null);
    }

    // 获取公开用户信息
    @GetMapping("/public/{userId}")
    public Result<UserPublicVO> getUserPublicInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) return Result.error("用户不存在");
        UserPublicVO vo = new UserPublicVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        return Result.success(vo);
    }
}