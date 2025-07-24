package com.cloudDemo.api.service;

import com.cloudDemo.api.dto.*;

import java.util.List;

/**
 * 分布式会话管理服务接口
 */
public interface SessionService {

    /**
     * 创建用户会话
     */
    Result<SessionInfo> createSession(Long userId, String deviceInfo, String ipAddress, String userAgent);

    /**
     * 验证会话是否有效
     */
    Result<SessionInfo> validateSession(String sessionId);

    /**
     * 刷新Token
     */
    Result<AuthResponse> refreshToken(TokenRefreshRequest request);

    /**
     * 销毁用户会话
     */
    Result<String> destroySession(String sessionId);

    /**
     * 踢出用户的所有会话
     */
    Result<String> kickOutUser(Long userId);

    /**
     * 踢出用户的指定设备会话
     */
    Result<String> kickOutDevice(Long userId, String deviceInfo);

    /**
     * 获取用户的所有活跃会话
     */
    Result<List<SessionInfo>> getUserSessions(Long userId);

    /**
     * 更新会话活跃时间
     */
    Result<String> updateSessionActivity(String sessionId);

    /**
     * 检查用户是否在线
     */
    Result<Boolean> isUserOnline(Long userId);

    /**
     * 获取所有在线用户
     */
    Result<List<OnlineUserInfo>> getOnlineUsers();

    /**
     * 清理过期会话
     */
    Result<String> cleanExpiredSessions();
}
