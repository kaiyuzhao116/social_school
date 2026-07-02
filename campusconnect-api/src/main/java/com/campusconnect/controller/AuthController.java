package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.entity.User;
import com.campusconnect.security.JwtUtil;
import com.campusconnect.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 用户名：1-6 个中文字符
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5]{1,6}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]{8,32}$");
    private static final Set<String> WEAK_PASSWORDS = Set.of("123456", "12345678", "123456789", "password", "admin",
            "qwerty");

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest req) {
        String identifier = req.getUsername();
        if (identifier == null || identifier.isBlank()) {
            return Result.error(400, "邮箱或用户名不能为空");
        }
        User user;
        if (identifier.contains("@")) {
            user = userService.findByEmail(identifier);
        } else {
            // 兼容旧用户名登录
            user = userService.findByUsername(identifier);
        }
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        if (!userService.checkPassword(user, req.getPassword())) {
            return Result.error(401, "密码错误");
        }
        if ("BANNED".equals(user.getStatus())) {
            return Result.error(403, "账号已被封禁");
        }

        // 自动修复旧数据的角色名称
        if ("管理员".equals(user.getRole())) {
            user.setRole("ADMIN");
            userService.updateById(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("refreshToken", refreshToken);
        user.setPassword(null);
        data.put("user", user);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest req) {
        // 基础校验
        if (!isValidUsername(req.getUsername())) {
            return Result.error(400, "用户名需为 1-6 个中文字符");
        }
        if (!isValidPassword(req.getPassword())) {
            return Result.error(400, "密码需 8-32 位，包含大写、小写和数字，不能是常见弱口令");
        }
        if (req.getConfirmPassword() == null || !req.getPassword().equals(req.getConfirmPassword())) {
            return Result.error(400, "两次输入的密码不一致");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return Result.error(400, "邮箱不能为空");
        }
        if (!isValidEmail(req.getEmail())) {
            return Result.error(400, "邮箱格式不正确");
        }

        // 唯一性校验
        if (userService.findByUsername(req.getUsername()) != null) {
            return Result.error(400, "用户名已存在");
        }
        if (req.getEmail() != null && !req.getEmail().isBlank() && userService.findByEmail(req.getEmail()) != null) {
            return Result.error(400, "邮箱已被使用");
        }

        // 昵称缺省为用户名
        String nickname = req.getNickname() != null && !req.getNickname().isBlank() ? req.getNickname()
                : req.getUsername();
        User user = userService.createUser(req.getUsername(), req.getPassword(), nickname, req.getEmail());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("refreshToken", refreshToken);
        user.setPassword(null);
        data.put("user", user);
        return Result.success(data);
    }

    @PostMapping("/refresh")
    public Result<?> refresh(@RequestBody RefreshRequest req) {
        if (!jwtUtil.validateToken(req.getRefreshToken())) {
            return Result.unauthorized("RefreshToken无效或已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(req.getRefreshToken());
        User user = userService.getById(userId);
        if (user == null) {
            return Result.unauthorized("用户不存在");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("refreshToken", refreshToken);
        return Result.success(data);
    }

    /**
     * 前端退出登录调用占位接口。
     * 无状态 JWT 不需要服务端销毁会话，这里直接返回成功，避免前端出现无权限的错误提示。
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("已退出");
    }

    private boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            return false;
        }
        return !WEAK_PASSWORDS.contains(password.toLowerCase());
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;
        private String confirmPassword;
        private String email;
    }

    @Data
    static class RefreshRequest {
        private String refreshToken;
    }
}
