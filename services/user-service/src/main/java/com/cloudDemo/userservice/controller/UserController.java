package com.cloudDemo.userservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.api.dto.AuthRequest;
import com.cloudDemo.api.dto.AuthResponse;
import com.cloudDemo.api.dto.Result;
import com.cloudDemo.api.dto.SessionInfo;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.fallback.UserServiceFallbackHandler;
import com.cloudDemo.userservice.mapper.UserMapper;
import com.cloudDemo.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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
     * 用户登录 - 升级版分布式会话管理
     */
    @PostMapping("/login")
    @SentinelResource(
            value = "userLogin",
            fallback = "userLoginFallback",
            fallbackClass = UserServiceFallbackHandler.class
    )
    public Result<AuthResponse> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    /**
     * 用户登录 - 兼容版本（不带HttpServletRequest，用于Sentinel降级）
     */
    @PostMapping("/login-simple")
    @SentinelResource(
            value = "userLoginSimple",
            fallback = "userLoginSimpleFallback",
            fallbackClass = UserServiceFallbackHandler.class
    )
    public Result<AuthResponse> loginSimple(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    /**
     * 用户注销 - 升级版
     */
    @PostMapping("/logout")
    @SentinelResource(
            value = "userLogout",
            fallback = "userLogoutFallback",
            fallbackClass = UserServiceFallbackHandler.class
    )
    public Result<String> logout(@RequestParam String sessionId) {
        return authService.logout(sessionId);
    }

    /**
     * 验证Token - 升级版
     */
    @PostMapping("/validate")
    @SentinelResource(
            value = "validateToken",
            fallback = "validateTokenFallback",
            fallbackClass = UserServiceFallbackHandler.class
    )
    public Result<SessionInfo> validateToken(@RequestParam String sessionId) {
        return authService.validateSession(sessionId);
    }

    /**
     * 简单的登录测试方法
     */
    @PostMapping("/test-login")
    public String testLogin(@RequestBody String body) {
        return "Received: " + body;
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/id/{id}")
    @SentinelResource(
            value = "getUserInfo",
            fallback = "getUserInfoFallback",
            fallbackClass = UserServiceFallbackHandler.class
    )
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

    /**
     * Sentinel熔断降级测试接口
     */
    @GetMapping("/sentinel-test")
    @SentinelResource(
            value = "sentinelTest",
            fallback = "sentinelTestFallback",
            fallbackClass = UserServiceFallbackHandler.class
    )
    public Result<String> sentinelTest(@RequestParam(defaultValue = "0") int delay) {
        try {
            // 模拟业务处理时间
            if (delay > 0) {
                Thread.sleep(delay);
            }
            return Result.success("Sentinel测试成功，延迟: " + delay + "ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("业务处理被中断", e);
        }
    }

    /**
     * Sentinel快速失败测试接口
     */
    @GetMapping("/sentinel-fail")
    @SentinelResource(
            value = "sentinelFail",
            fallback = "sentinelFailFallback",
            fallbackClass = UserServiceFallbackHandler.class
    )
    public Result<String> sentinelFail() {
        // 模拟业务异常
        throw new RuntimeException("模拟业务异常，触发Sentinel熔断");
    }
}
