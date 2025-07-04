package com.cloudDemo.gateway.controller;

import com.cloudDemo.gateway.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证管理控制器 - 用于管理令牌和用户缓存
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 验证令牌
     */
    @PostMapping("/validate")
    public Mono<Map<String, Object>> validateToken(@RequestParam String token) {
        return authService.validateToken(token)
                .map(userInfo -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("valid", true);
                    result.put("userId", userInfo.getUserId());
                    result.put("username", userInfo.getUsername());
                    result.put("roles", userInfo.getRoles());
                    return result;
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("valid", false);
                    result.put("message", "Invalid or expired token");
                    return result;
                }));
    }

    /**
     * 将令牌加入黑名单
     */
    @PostMapping("/blacklist")
    public Mono<Map<String, Object>> addToBlacklist(@RequestParam String token,
                                                     @RequestParam(defaultValue = "3600") long seconds) {
        return authService.addTokenToBlacklist(token, Duration.ofSeconds(seconds))
                .map(success -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", success);
                    result.put("message", success ? "Token added to blacklist" : "Failed to add token to blacklist");
                    result.put("duration", seconds + " seconds");
                    return result;
                });
    }

    /**
     * 清除用户缓存
     */
    @DeleteMapping("/cache/{userId}")
    public Mono<Map<String, Object>> clearUserCache(@PathVariable String userId) {
        return authService.clearUserCache(userId)
                .map(success -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", success);
                    result.put("message", success ? "User cache cleared" : "Failed to clear user cache");
                    result.put("userId", userId);
                    return result;
                });
    }

    /**
     * 更新用户缓存
     */
    @PutMapping("/cache/{userId}")
    public Mono<Map<String, Object>> updateUserCache(@PathVariable String userId,
                                                      @RequestParam String username,
                                                      @RequestParam(required = false) String roles) {
        AuthService.UserInfo userInfo = AuthService.UserInfo.builder()
                .userId(userId)
                .username(username)
                .roles(roles)
                .build();

        return authService.updateUserCache(userId, userInfo)
                .map(success -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", success);
                    result.put("message", success ? "User cache updated" : "Failed to update user cache");
                    result.put("userInfo", userInfo);
                    return result;
                });
    }

    /**
     * 认证管理状态检查
     */
    @GetMapping("/status")
    public Mono<Map<String, Object>> getAuthStatus() {
        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();
            result.put("service", "Gateway Auth Service");
            result.put("status", "active");
            result.put("features", Map.of(
                    "jwt_validation", true,
                    "redis_cache", true,
                    "token_blacklist", true,
                    "user_info_cache", true
            ));
            return result;
        });
    }
}
