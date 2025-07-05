package com.cloudDemo.userservice.service;

import com.cloudDemo.api.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务
 */
@Slf4j
@Service
public class AuthService {

    private static final String LOGIN_TOKEN_PREFIX = "user:token:";
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final long TOKEN_EXPIRE_TIME = 24; // 24小时
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成并缓存用户Token
     */
    public String generateUserToken(Integer userId, String username, String roles) {
        try {
            // 生成JWT Token
            String token = JwtUtil.generateToken(userId.toString());

            // 将Token缓存到Redis，用于快速验证和注销
            String tokenKey = LOGIN_TOKEN_PREFIX + token;
            String sessionKey = USER_SESSION_PREFIX + userId;

            // 创建用户会话信息
            UserSession userSession = UserSession.builder()
                    .userId(userId)
                    .username(username)
                    .roles(roles)
                    .token(token)
                    .loginTime(System.currentTimeMillis())
                    .build();

            // 缓存Token -> UserSession映射
            redisTemplate.opsForValue().set(tokenKey, userSession, TOKEN_EXPIRE_TIME, TimeUnit.HOURS);

            // 缓存UserId -> Token映射（用于单点登录控制）
            redisTemplate.opsForValue().set(sessionKey, token, TOKEN_EXPIRE_TIME, TimeUnit.HOURS);

            log.info("Generated token for user: {} (ID: {})", username, userId);
            return token;

        } catch (Exception e) {
            log.error("Failed to generate token for user: {} (ID: {})", username, userId, e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * 验证Token有效性
     */
    public UserSession validateToken(String token) {
        try {
            String tokenKey = LOGIN_TOKEN_PREFIX + token;
            UserSession userSession = (UserSession) redisTemplate.opsForValue().get(tokenKey);

            if (userSession == null) {
                log.warn("Token not found in cache: {}", token.substring(0, 10) + "...");
                return null;
            }

            // 验证JWT Token
            if (JwtUtil.isTokenExpired(token)) {
                log.warn("Token expired for user: {}", userSession.getUsername());
                // 清除过期的缓存
                redisTemplate.delete(tokenKey);
                redisTemplate.delete(USER_SESSION_PREFIX + userSession.getUserId());
                return null;
            }

            return userSession;

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 用户注销
     */
    public boolean logout(String token) {
        try {
            String tokenKey = LOGIN_TOKEN_PREFIX + token;
            UserSession userSession = (UserSession) redisTemplate.opsForValue().get(tokenKey);

            if (userSession != null) {
                // 删除Token缓存
                redisTemplate.delete(tokenKey);
                // 删除用户会话缓存
                redisTemplate.delete(USER_SESSION_PREFIX + userSession.getUserId());

                log.info("User logged out: {} (ID: {})", userSession.getUsername(), userSession.getUserId());
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("Logout failed for token: {}", token.substring(0, 10) + "...", e);
            return false;
        }
    }

    /**
     * 用户注销（通过用户ID）
     */
    public boolean logoutByUserId(Integer userId) {
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;
            String token = (String) redisTemplate.opsForValue().get(sessionKey);

            if (token != null) {
                // 删除Token缓存
                redisTemplate.delete(LOGIN_TOKEN_PREFIX + token);
                // 删除用户会话缓存
                redisTemplate.delete(sessionKey);

                log.info("User logged out by userId: {}", userId);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("Logout by userId failed: {}", userId, e);
            return false;
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String oldToken) {
        UserSession userSession = validateToken(oldToken);
        if (userSession != null) {
            // 先注销旧Token
            logout(oldToken);
            // 生成新Token
            return generateUserToken(userSession.getUserId(), userSession.getUsername(), userSession.getRoles());
        }
        return null;
    }

    /**
     * 获取在线用户数量
     */
    public long getOnlineUserCount() {
        try {
            return redisTemplate.keys(USER_SESSION_PREFIX + "*").size();
        } catch (Exception e) {
            log.error("Failed to get online user count", e);
            return 0;
        }
    }

    /**
     * 用户会话信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserSession {
        private Integer userId;
        private String username;
        private String roles;
        private String token;
        private Long loginTime;
    }
}
