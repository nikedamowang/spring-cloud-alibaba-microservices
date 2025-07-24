package com.cloudDemo.userservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.api.dto.*;
import com.cloudDemo.api.service.SessionService;
import com.cloudDemo.userservice.entity.User;
import com.cloudDemo.userservice.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户认证服务 - 升级版分布式会话管理
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Qualifier("sessionService")  // 明确指定注入的Bean名称
    private SessionService sessionService;

    /**
     * 用户登录 - 升级版分布式会话管理
     */
    public Result<AuthResponse> login(AuthRequest request, HttpServletRequest httpRequest) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
                return Result.error("用户名和密码不能为空");
            }

            // 查询用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", request.getUsername());
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证密码
            if (!request.getPassword().equals(user.getPassword())) {
                return Result.error("密码错误");
            }

            // 检查用户状态
            if (!"active".equals(user.getStatus())) {
                return Result.error("用户已被禁用");
            }

            // 获取设备和IP信息
            String deviceInfo = getDeviceInfo(httpRequest);
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            // 创建分布式会话
            Result<SessionInfo> sessionResult = sessionService.createSession(
                    user.getId().longValue(), deviceInfo, ipAddress, userAgent);

            if (!sessionResult.isSuccess()) {
                return Result.error("创建会话失败：" + sessionResult.getMessage());
            }

            SessionInfo sessionInfo = sessionResult.getData();

            // 构建响应
            AuthResponse response = new AuthResponse(
                    sessionInfo.getAccessToken(),
                    sessionInfo.getRefreshToken(),
                    sessionInfo.getUserId(),
                    sessionInfo.getUsername(),
                    sessionInfo.getExpireTime().toInstant(java.time.ZoneOffset.of("+8")).toEpochMilli()
            );

            log.info("用户登录成功: username={}, sessionId={}, device={}",
                    user.getUsername(), sessionInfo.getSessionId(), deviceInfo);
            return Result.success(response);

        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录 - 向后兼容版本（不带HttpServletRequest参数）
     */
    public Result<AuthResponse> login(AuthRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
                return Result.error("用户名和密码不能为空");
            }

            // 查询用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", request.getUsername());
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证密码
            if (!request.getPassword().equals(user.getPassword())) {
                return Result.error("密码错误");
            }

            // 检查用户状态
            if (!"active".equals(user.getStatus())) {
                return Result.error("用户已被禁用");
            }

            // 创建分布式会话（使用默认设备信息）
            Result<SessionInfo> sessionResult = sessionService.createSession(
                    user.getId().longValue(), "Unknown", "Unknown", "Unknown");

            if (!sessionResult.isSuccess()) {
                return Result.error("创建会话失败：" + sessionResult.getMessage());
            }

            SessionInfo sessionInfo = sessionResult.getData();

            // 构建响应
            AuthResponse response = new AuthResponse(
                    sessionInfo.getAccessToken(),
                    sessionInfo.getRefreshToken(),
                    sessionInfo.getUserId(),
                    sessionInfo.getUsername(),
                    sessionInfo.getExpireTime().toInstant(java.time.ZoneOffset.of("+8")).toEpochMilli()
            );

            log.info("用户登录成功: username={}, sessionId={}",
                    user.getUsername(), sessionInfo.getSessionId());
            return Result.success(response);

        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户注销 - 升级版
     */
    public Result<String> logout(String sessionId) {
        try {
            if (!StringUtils.hasText(sessionId)) {
                return Result.error("会话ID不能为空");
            }

            // 首先验证会话是否存在
            Result<SessionInfo> validateResult = sessionService.validateSession(sessionId);
            if (!validateResult.isSuccess()) {
                // 如果会话验证失败，说明会话不存在或已失效
                return Result.error("会话不存在或已失效，无法注销");
            }

            SessionInfo sessionInfo = validateResult.getData();
            if (!"ACTIVE".equals(sessionInfo.getStatus())) {
                return Result.error("会话已失效，无需重复注销");
            }

            // 销毁分布式会话
            Result<String> result = sessionService.destroySession(sessionId);

            if (result.isSuccess()) {
                log.info("用户注销成功: userId={}, sessionId={}", sessionInfo.getUserId(), sessionId);
                return Result.success("注销成功");
            } else {
                log.warn("用户注销失败: sessionId={}, reason={}", sessionId, result.getMessage());
                return result;
            }

        } catch (Exception e) {
            log.error("用户注销失败", e);
            return Result.error("注销失败：" + e.getMessage());
        }
    }

    /**
     * 验证会话是否有效 - 升级版
     */
    public Result<SessionInfo> validateSession(String sessionId) {
        try {
            if (!StringUtils.hasText(sessionId)) {
                return Result.error("会话ID不能为空");
            }

            // 验证分布式会话
            Result<SessionInfo> result = sessionService.validateSession(sessionId);

            if (result.isSuccess()) {
                // 更新会话活跃时间
                sessionService.updateSessionActivity(sessionId);
            }

            return result;

        } catch (Exception e) {
            log.error("验证会话失败", e);
            return Result.error("验证会话失败：" + e.getMessage());
        }
    }

    /**
     * 刷新Token - 新增功能
     */
    public Result<AuthResponse> refreshToken(TokenRefreshRequest request) {
        try {
            return sessionService.refreshToken(request);
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return Result.error("刷新Token失败：" + e.getMessage());
        }
    }

    /**
     * 踢出用户 - 新增功能
     */
    public Result<String> kickOutUser(Long userId) {
        try {
            return sessionService.kickOutUser(userId);
        } catch (Exception e) {
            log.error("踢出用户失败", e);
            return Result.error("踢出用户失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户所有会话 - 新增功能
     */
    public Result<java.util.List<SessionInfo>> getUserSessions(Long userId) {
        try {
            return sessionService.getUserSessions(userId);
        } catch (Exception e) {
            log.error("获取用户会话失败", e);
            return Result.error("获取用户会话失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否在线 - 新增功能
     */
    public Result<Boolean> isUserOnline(Long userId) {
        try {
            return sessionService.isUserOnline(userId);
        } catch (Exception e) {
            log.error("检查用户在线状态失败", e);
            return Result.error("检查在线状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有在线用户 - 新增功能
     */
    public Result<java.util.List<OnlineUserInfo>> getOnlineUsers() {
        try {
            return sessionService.getOnlineUsers();
        } catch (Exception e) {
            log.error("获取在线用户失败", e);
            return Result.error("获取在线用户失败：" + e.getMessage());
        }
    }

    // 私有辅助方法

    /**
     * 获取设备信息
     */
    private String getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }

        // 简单的设备识别逻辑
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
