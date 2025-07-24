package com.cloudDemo.userservice.service.impl;

import com.cloudDemo.api.dto.*;
import com.cloudDemo.api.service.SessionService;
import com.cloudDemo.api.util.JwtUtil;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分布式会话管理服务实现 - 本地Spring服务（非Dubbo服务）
 */
@Slf4j
@Service("sessionService")  // 明确指定Bean名称
@org.springframework.context.annotation.Primary  // 设置为主要实现
public class SessionServiceImpl implements SessionService {

    // Redis Key前缀常量
    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user:sessions:";
    private static final String ONLINE_USERS_PREFIX = "online:users";
    private static final String USER_DEVICES_PREFIX = "user:devices:";

    // 配置常量
    private static final int MAX_DEVICES_PER_USER = 3; // 最大同时登录设备数
    private static final long ACCESS_TOKEN_EXPIRE_MINUTES = 30; // AccessToken过期时间30分钟
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7; // RefreshToken过期时间7天
    private static final long SESSION_EXPIRE_HOURS = 24; // 会话过期时间24小时

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<SessionInfo> createSession(Long userId, String deviceInfo, String ipAddress, String userAgent) {
        try {
            // 查询用户信息
            User user = userMapper.selectById(userId.intValue());
            if (user == null) {
                return Result.error("用户不存在");
            }

            if (!"active".equals(user.getStatus())) {
                return Result.error("用户已被禁用");
            }

            // 检查现有设备数量，实现单点登录控制
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Set<Object> existingSessions = redisTemplate.opsForSet().members(userSessionsKey);

            if (existingSessions != null && existingSessions.size() >= MAX_DEVICES_PER_USER) {
                // 踢出最旧的会话
                kickOutOldestSession(userId, existingSessions);
            }

            // 生成会话ID和Tokens
            String sessionId = UUID.randomUUID().toString();
            String accessToken = JwtUtil.generateToken(String.valueOf(userId), user.getUsername());
            String refreshToken = JwtUtil.generateRefreshToken(String.valueOf(userId));

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = now.plusHours(SESSION_EXPIRE_HOURS);

            // 创建会话信息
            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setSessionId(sessionId);
            sessionInfo.setUserId(userId);
            sessionInfo.setUsername(user.getUsername());
            sessionInfo.setAccessToken(accessToken);
            sessionInfo.setRefreshToken(refreshToken);
            sessionInfo.setDeviceInfo(deviceInfo);
            sessionInfo.setIpAddress(ipAddress);
            sessionInfo.setUserAgent(userAgent);
            sessionInfo.setLoginTime(now);
            sessionInfo.setLastActiveTime(now);
            sessionInfo.setExpireTime(expireTime);
            sessionInfo.setOnline(true);
            sessionInfo.setStatus("ACTIVE");

            // 存储会话信息到Redis
            String sessionKey = SESSION_PREFIX + sessionId;
            redisTemplate.opsForValue().set(sessionKey, sessionInfo, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            // 将会话ID添加到用户会话集合
            redisTemplate.opsForSet().add(userSessionsKey, sessionId);
            redisTemplate.expire(userSessionsKey, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            // 更新在线用户信息
            updateOnlineUserInfo(userId, user.getUsername(), deviceInfo, ipAddress, now);

            // 存储设备信息
            String userDevicesKey = USER_DEVICES_PREFIX + userId;
            redisTemplate.opsForSet().add(userDevicesKey, deviceInfo);
            redisTemplate.expire(userDevicesKey, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            log.info("创建用户会话成功: userId={}, sessionId={}, device={}", userId, sessionId, deviceInfo);
            return Result.success(sessionInfo);

        } catch (Exception e) {
            log.error("创建用户会话失败", e);
            return Result.error("创建会话失败：" + e.getMessage());
        }
    }

    @Override
    public Result<SessionInfo> validateSession(String sessionId) {
        try {
            if (!StringUtils.hasText(sessionId)) {
                return Result.error("会话ID不能为空");
            }

            String sessionKey = SESSION_PREFIX + sessionId;
            SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);

            if (sessionInfo == null) {
                return Result.error("会话不存在或已过期");
            }

            if (!"ACTIVE".equals(sessionInfo.getStatus())) {
                return Result.error("会话已失效");
            }

            // 验证AccessToken
            if (!JwtUtil.validateToken(sessionInfo.getAccessToken())) {
                return Result.error("Token已过期");
            }

            // 更新最后活跃时间
            sessionInfo.setLastActiveTime(LocalDateTime.now());
            redisTemplate.opsForValue().set(sessionKey, sessionInfo, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            return Result.success(sessionInfo);

        } catch (Exception e) {
            log.error("验证会话失败", e);
            return Result.error("验证会话失败：" + e.getMessage());
        }
    }

    @Override
    public Result<AuthResponse> refreshToken(TokenRefreshRequest request) {
        try {
            if (!StringUtils.hasText(request.getRefreshToken())) {
                return Result.error("RefreshToken不能为空");
            }

            // 验证RefreshToken
            if (!JwtUtil.validateToken(request.getRefreshToken())) {
                return Result.error("RefreshToken无效或已过期");
            }

            String userId = JwtUtil.getUserIdFromToken(request.getRefreshToken());
            if (userId == null) {
                return Result.error("RefreshToken中没有用户信息");
            }

            // 查询用户信息
            User user = userMapper.selectById(Integer.valueOf(userId));
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 查找对应的会话
            SessionInfo sessionInfo = findSessionByRefreshToken(Long.valueOf(userId), request.getRefreshToken());
            if (sessionInfo == null) {
                return Result.error("会话不存在");
            }

            // 生成新的AccessToken
            String newAccessToken = JwtUtil.generateToken(userId, user.getUsername());

            // 更新会话信息
            sessionInfo.setAccessToken(newAccessToken);
            sessionInfo.setLastActiveTime(LocalDateTime.now());

            String sessionKey = SESSION_PREFIX + sessionInfo.getSessionId();
            redisTemplate.opsForValue().set(sessionKey, sessionInfo, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            // 构建响应
            AuthResponse response = new AuthResponse(
                    newAccessToken,
                    request.getRefreshToken(), // RefreshToken保持不变
                    Long.valueOf(userId),
                    user.getUsername(),
                    JwtUtil.getExpirationTime()
            );

            log.info("Token刷新成功: userId={}", userId);
            return Result.success(response);

        } catch (Exception e) {
            log.error("Token刷新失败", e);
            return Result.error("Token刷新失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> destroySession(String sessionId) {
        try {
            if (!StringUtils.hasText(sessionId)) {
                return Result.error("会话ID不能为空");
            }

            String sessionKey = SESSION_PREFIX + sessionId;
            SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);

            if (sessionInfo != null) {
                Long userId = sessionInfo.getUserId();

                // 删除会话信息
                redisTemplate.delete(sessionKey);

                // 从用户会话集合中移除
                String userSessionsKey = USER_SESSIONS_PREFIX + userId;
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);

                // 更新在线状态
                updateUserOnlineStatus(userId);

                log.info("销毁用户会话成功: userId={}, sessionId={}", userId, sessionId);
            }

            return Result.success("会话销毁成功");

        } catch (Exception e) {
            log.error("销毁会话失败", e);
            return Result.error("销毁会话失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> kickOutUser(Long userId) {
        try {
            // 首先验证用户是否存在
            User user = userMapper.selectById(userId.intValue());
            if (user == null) {
                return Result.error("用户不存在，无法踢出");
            }

            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

            // 检查用户是否有活跃会话
            if (sessionIds == null || sessionIds.isEmpty()) {
                return Result.error("用户当前没有活跃会话，无需踢出");
            }

            int kickedSessionCount = 0;
            for (Object sessionId : sessionIds) {
                String sessionKey = SESSION_PREFIX + sessionId;
                SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);
                if (sessionInfo != null && "ACTIVE".equals(sessionInfo.getStatus())) {
                    sessionInfo.setStatus("KICKED");
                    sessionInfo.setOnline(false);
                    redisTemplate.opsForValue().set(sessionKey, sessionInfo, 5, TimeUnit.MINUTES); // 保留5分钟用于通知
                    kickedSessionCount++;
                }
            }

            if (kickedSessionCount == 0) {
                return Result.error("没有找到可踢出的活跃会话");
            }

            // 清除用户会话集合
            redisTemplate.delete(userSessionsKey);

            // 更新在线状态
            redisTemplate.opsForHash().delete(ONLINE_USERS_PREFIX, userId.toString());

            log.info("踢出用户所有会话成功: userId={}, kickedSessionCount={}", userId, kickedSessionCount);
            return Result.success("成功踢出用户的 " + kickedSessionCount + " 个活跃会话");

        } catch (Exception e) {
            log.error("踢出用户失败", e);
            return Result.error("踢出用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> kickOutDevice(Long userId, String deviceInfo) {
        try {
            // 首先验证用户是否存在
            User user = userMapper.selectById(userId.intValue());
            if (user == null) {
                return Result.error("用户不存在，无法踢出设备");
            }

            // 验证设备信息参数
            if (!StringUtils.hasText(deviceInfo)) {
                return Result.error("设备信息不能为空");
            }

            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

            // 检查用户是否有活跃会话
            if (sessionIds == null || sessionIds.isEmpty()) {
                return Result.error("用户当前没有活跃会话");
            }

            boolean deviceFound = false;
            boolean deviceKicked = false;

            for (Object sessionId : sessionIds) {
                String sessionKey = SESSION_PREFIX + sessionId;
                SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);

                if (sessionInfo != null && deviceInfo.equals(sessionInfo.getDeviceInfo())) {
                    deviceFound = true;

                    // 只踢出活跃的会话
                    if ("ACTIVE".equals(sessionInfo.getStatus())) {
                        sessionInfo.setStatus("KICKED");
                        sessionInfo.setOnline(false);
                        redisTemplate.opsForValue().set(sessionKey, sessionInfo, 5, TimeUnit.MINUTES);

                        // 从用户会话集合中移除
                        redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
                        deviceKicked = true;

                        log.info("踢出用户指定设备成功: userId={}, device={}, sessionId={}",
                                userId, deviceInfo, sessionId);
                        break;
                    }
                }
            }

            if (!deviceFound) {
                return Result.error("指定设备 '" + deviceInfo + "' 未找到或已离线");
            }

            if (!deviceKicked) {
                return Result.error("指定设备没有活跃会话可踢出");
            }

            // 更新在线状态
            updateUserOnlineStatus(userId);

            return Result.success("成功踢出设备 '" + deviceInfo + "' 的会话");

        } catch (Exception e) {
            log.error("踢出设备失败", e);
            return Result.error("踢出设备失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<SessionInfo>> getUserSessions(Long userId) {
        try {
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

            List<SessionInfo> sessions = new ArrayList<>();
            if (sessionIds != null) {
                for (Object sessionId : sessionIds) {
                    String sessionKey = SESSION_PREFIX + sessionId;
                    SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);
                    if (sessionInfo != null && "ACTIVE".equals(sessionInfo.getStatus())) {
                        sessions.add(sessionInfo);
                    }
                }
            }

            return Result.success(sessions);

        } catch (Exception e) {
            log.error("获取用户会话失败", e);
            return Result.error("获取用户会话失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> updateSessionActivity(String sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);

            if (sessionInfo != null && "ACTIVE".equals(sessionInfo.getStatus())) {
                sessionInfo.setLastActiveTime(LocalDateTime.now());
                redisTemplate.opsForValue().set(sessionKey, sessionInfo, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

                // 更新在线用户信息
                updateOnlineUserInfo(sessionInfo.getUserId(), sessionInfo.getUsername(),
                        sessionInfo.getDeviceInfo(), sessionInfo.getIpAddress(), LocalDateTime.now());
            }

            return Result.success("活跃时间更新成功");

        } catch (Exception e) {
            log.error("更新会话活跃时间失败", e);
            return Result.error("更新活跃时间失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> isUserOnline(Long userId) {
        try {
            // 获取用户的所有会话
            String userSessionsKey = USER_SESSIONS_PREFIX + userId;
            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

            if (sessionIds == null || sessionIds.isEmpty()) {
                return Result.success(false);
            }

            // 检查是否有有效的活跃会话
            for (Object sessionIdObj : sessionIds) {
                String sessionId = sessionIdObj.toString();
                String sessionKey = SESSION_PREFIX + sessionId;
                Object sessionObj = redisTemplate.opsForValue().get(sessionKey);

                if (sessionObj instanceof SessionInfo) {
                    SessionInfo session = (SessionInfo) sessionObj;
                    // 检查会话是否在线且未过期
                    if (session.getOnline() != null && session.getOnline() &&
                            "ACTIVE".equals(session.getStatus()) &&
                            session.getExpireTime().isAfter(LocalDateTime.now())) {
                        return Result.success(true);
                    }
                }
            }

            // 如果没有找到有效会话，清理在线用户信息
            redisTemplate.opsForHash().delete(ONLINE_USERS_PREFIX, userId.toString());
            return Result.success(false);

        } catch (Exception e) {
            log.error("检查用户在线状态失败", e);
            return Result.error("检查在线状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<OnlineUserInfo>> getOnlineUsers() {
        try {
            Map<Object, Object> onlineUsersMap = redisTemplate.opsForHash().entries(ONLINE_USERS_PREFIX);

            List<OnlineUserInfo> onlineUsers = onlineUsersMap.values().stream()
                    .map(obj -> (OnlineUserInfo) obj)
                    .collect(Collectors.toList());

            return Result.success(onlineUsers);

        } catch (Exception e) {
            log.error("获取在线用户失败", e);
            return Result.error("获取在线用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> cleanExpiredSessions() {
        try {
            // 这里可以实现定时清理过期会话的逻辑
            // 由于Redis本身有TTL机制，大部分过期数据会自动清理
            // 这个方法主要用于清理一些可能残留的数据

            log.info("清理过期会话完成");
            return Result.success("清理完成");

        } catch (Exception e) {
            log.error("清理过期会话失败", e);
            return Result.error("清理过期会话失败：" + e.getMessage());
        }
    }

    // 私有辅助方法

    private void kickOutOldestSession(Long userId, Set<Object> existingSessions) {
        // 找到最旧的会话并踢出
        SessionInfo oldestSession = null;
        LocalDateTime oldestTime = LocalDateTime.now();

        for (Object sessionId : existingSessions) {
            String sessionKey = SESSION_PREFIX + sessionId;
            SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);
            if (sessionInfo != null && sessionInfo.getLoginTime().isBefore(oldestTime)) {
                oldestTime = sessionInfo.getLoginTime();
                oldestSession = sessionInfo;
            }
        }

        if (oldestSession != null) {
            destroySession(oldestSession.getSessionId());
            log.info("踢出最旧会话: userId={}, sessionId={}", userId, oldestSession.getSessionId());
        }
    }

    private SessionInfo findSessionByRefreshToken(Long userId, String refreshToken) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

        if (sessionIds != null) {
            for (Object sessionId : sessionIds) {
                String sessionKey = SESSION_PREFIX + sessionId;
                SessionInfo sessionInfo = (SessionInfo) redisTemplate.opsForValue().get(sessionKey);
                if (sessionInfo != null && refreshToken.equals(sessionInfo.getRefreshToken())) {
                    return sessionInfo;
                }
            }
        }

        return null;
    }

    private void updateOnlineUserInfo(Long userId, String username, String deviceInfo,
                                      String ipAddress, LocalDateTime activeTime) {
        OnlineUserInfo onlineInfo = new OnlineUserInfo();
        onlineInfo.setUserId(userId);
        onlineInfo.setUsername(username);
        onlineInfo.setActiveDevice(deviceInfo);
        onlineInfo.setIpAddress(ipAddress);
        onlineInfo.setLastActiveTime(activeTime);
        onlineInfo.setOnline(true);

        // 计算设备数量
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Long deviceCount = redisTemplate.opsForSet().size(userSessionsKey);
        onlineInfo.setDeviceCount(deviceCount != null ? deviceCount.intValue() : 0);

        redisTemplate.opsForHash().put(ONLINE_USERS_PREFIX, userId.toString(), onlineInfo);
    }

    private void updateUserOnlineStatus(Long userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Long sessionCount = redisTemplate.opsForSet().size(userSessionsKey);

        if (sessionCount == null || sessionCount == 0) {
            // 没有活跃会话，移除在线状态
            redisTemplate.opsForHash().delete(ONLINE_USERS_PREFIX, userId.toString());
        } else {
            // 更新设备数量
            OnlineUserInfo onlineInfo = (OnlineUserInfo) redisTemplate.opsForHash().get(ONLINE_USERS_PREFIX, userId.toString());
            if (onlineInfo != null) {
                onlineInfo.setDeviceCount(sessionCount.intValue());
                redisTemplate.opsForHash().put(ONLINE_USERS_PREFIX, userId.toString(), onlineInfo);
            }
        }
    }
}
