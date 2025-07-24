package com.cloudDemo.userservice.controller;

import com.cloudDemo.api.dto.*;
import com.cloudDemo.userservice.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分布式会话管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private AuthService authService;

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        log.info("刷新Token请求: device={}", request.getDeviceInfo());
        return authService.refreshToken(request);
    }

    /**
     * 踢出用户所有会话
     */
    @PostMapping("/kickout/user/{userId}")
    public Result<String> kickOutUser(@PathVariable Long userId) {
        log.info("踢出用户请求: userId={}", userId);
        return authService.kickOutUser(userId);
    }

    /**
     * 踢出用户指定设备
     */
    @PostMapping("/kickout/device")
    public Result<String> kickOutDevice(@RequestParam Long userId, @RequestParam String deviceInfo) {
        log.info("踢出设备请求: userId={}, device={}", userId, deviceInfo);

        try {
            // 安全地获取用户会话列表
            Result<List<SessionInfo>> sessionsResult = authService.getUserSessions(userId);
            if (!sessionsResult.isSuccess() || sessionsResult.getData() == null) {
                return Result.error("获取用户会话失败：" + sessionsResult.getMessage());
            }

            // 查找匹配的设备会话
            return sessionsResult.getData()
                    .stream()
                    .filter(session -> deviceInfo.equals(session.getDeviceInfo()))
                    .findFirst()
                    .map(session -> authService.logout(session.getSessionId()))
                    .orElse(Result.error("指定设备不存在或已离线"));

        } catch (Exception e) {
            log.error("踢出设备失败", e);
            return Result.error("踢出设备失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户所有活跃会话
     */
    @GetMapping("/user/{userId}")
    public Result<List<SessionInfo>> getUserSessions(@PathVariable Long userId) {
        log.info("获取用户会话请求: userId={}", userId);
        return authService.getUserSessions(userId);
    }

    /**
     * 检查用户是否在线
     */
    @GetMapping("/online/{userId}")
    public Result<Boolean> isUserOnline(@PathVariable Long userId) {
        log.info("检查用户在线状态: userId={}", userId);
        return authService.isUserOnline(userId);
    }

    /**
     * 获取所有在线用户
     */
    @GetMapping("/online/users")
    public Result<List<OnlineUserInfo>> getOnlineUsers() {
        log.info("获取所有在线用户");
        return authService.getOnlineUsers();
    }

    /**
     * 验证会话（供内部调用）
     */
    @PostMapping("/validate")
    public Result<SessionInfo> validateSession(@RequestParam String sessionId) {
        log.debug("验证会话请求: sessionId={}", sessionId);
        return authService.validateSession(sessionId);
    }
}
