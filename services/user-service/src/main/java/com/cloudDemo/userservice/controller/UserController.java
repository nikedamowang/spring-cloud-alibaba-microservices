package com.cloudDemo.userservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.api.dto.AuthRequest;
import com.cloudDemo.api.dto.AuthResponse;
import com.cloudDemo.api.dto.Result;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import com.cloudDemo.userservice.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserMapper userMapper;
    private final AuthService authService;

    public UserController(UserMapper userMapper, AuthService authService) {
        this.userMapper = userMapper;
        this.authService = authService;
    }

    @GetMapping("/list")
    public List<User> list() {
        return userMapper.selectList(new QueryWrapper<>());
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    /**
     * 简单的登录测试方法
     */
    @PostMapping("/test-login")
    public String testLogin(@RequestBody String body) {
        return "Received: " + body;
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authorization) {
        // 从Header中提取token（去掉"Bearer "前缀）
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        return authService.logout(token);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody AuthRequest request) {
        return authService.register(request);
    }

    /**
     * 验证token
     */
    @PostMapping("/validate")
    public Result<User> validateToken(@RequestHeader("Authorization") String authorization) {
        // 从Header中提取token（去掉"Bearer "前缀）
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        return authService.validateToken(token);
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/id/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 不返回密码信息
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 不返回密码信息
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }
}
