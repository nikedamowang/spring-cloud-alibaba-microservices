package com.cloudDemo.gateway.service;

import com.cloudDemo.api.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关认证服务
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    private static final String USER_INFO_PREFIX = "gateway:user:";
    private static final String BLACKLIST_PREFIX = "gateway:blacklist:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    /**
     * 验证JWT Token
     */
    public Mono<UserInfo> validateToken(String token) {
        try {
            // 先检查token是否在黑名单中
            return checkTokenBlacklist(token)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("Token is in blacklist: {}", token.substring(0, 10) + "...");
                            return Mono.empty();
                        }

                        // 解析JWT Token
                        Claims claims = JwtUtil.parseToken(token);
                        String userId = claims.getSubject();

                        if (userId == null) {
                            log.warn("Token has no subject: {}", token.substring(0, 10) + "...");
                            return Mono.empty();
                        }

                        // 检查是否过期
                        if (JwtUtil.isTokenExpired(token)) {
                            log.warn("Token expired for user: {}", userId);
                            return Mono.empty();
                        }

                        // 从缓存获取用户信息
                        return getUserInfoFromCache(userId)
                                .switchIfEmpty(
                                        // 如果缓存中没有，创建基本用户信息并缓存
                                        createAndCacheUserInfo(userId, claims)
                                );
                    });
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * 检查token是否在黑名单中
     */
    private Mono<Boolean> checkTokenBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.hasKey(key)
                .onErrorReturn(false);
    }

    /**
     * 将token加入黑名单
     */
    public Mono<Boolean> addTokenToBlacklist(String token, Duration duration) {
        String key = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.opsForValue()
                .set(key, "1", duration)
                .onErrorReturn(false);
    }

    /**
     * 从缓存获取用户信息
     */
    private Mono<UserInfo> getUserInfoFromCache(String userId) {
        String key = USER_INFO_PREFIX + userId;
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(Map.class)
                .map(this::mapToUserInfo)
                .onErrorResume(e -> {
                    log.debug("Failed to get user info from cache for user: {}", userId);
                    return Mono.empty();
                });
    }

    /**
     * 创建并缓存用户信息
     */
    private Mono<UserInfo> createAndCacheUserInfo(String userId, Claims claims) {
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .username(claims.get("username", String.class))
                .roles(claims.get("roles", String.class))
                .build();

        // 缓存用户信息
        String key = USER_INFO_PREFIX + userId;
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userInfo.getUserId());
        userMap.put("username", userInfo.getUsername());
        userMap.put("roles", userInfo.getRoles());

        return reactiveRedisTemplate.opsForValue()
                .set(key, userMap, CACHE_DURATION)
                .thenReturn(userInfo)
                .onErrorReturn(userInfo); // 即使缓存失败也返回用户信息
    }

    /**
     * 更新用户缓存
     */
    public Mono<Boolean> updateUserCache(String userId, UserInfo userInfo) {
        String key = USER_INFO_PREFIX + userId;
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userInfo.getUserId());
        userMap.put("username", userInfo.getUsername());
        userMap.put("roles", userInfo.getRoles());

        return reactiveRedisTemplate.opsForValue()
                .set(key, userMap, CACHE_DURATION)
                .onErrorReturn(false);
    }

    /**
     * 清除用户缓存
     */
    public Mono<Boolean> clearUserCache(String userId) {
        String key = USER_INFO_PREFIX + userId;
        return reactiveRedisTemplate.delete(key)
                .map(count -> count > 0)
                .onErrorReturn(false);
    }

    /**
     * 将Map转换为UserInfo对象
     */
    private UserInfo mapToUserInfo(Map<String, Object> map) {
        return UserInfo.builder()
                .userId((String) map.get("userId"))
                .username((String) map.get("username"))
                .roles((String) map.get("roles"))
                .build();
    }

    /**
     * 用户信息类
     */
    @lombok.Data
    @lombok.Builder
    public static class UserInfo {
        private String userId;
        private String username;
        private String roles;

        public boolean hasRole(String role) {
            return roles != null && roles.contains(role);
        }
    }
}
