package com.cloudDemo.userservice.fallback;

import com.cloudDemo.api.dto.AuthRequest;
import com.cloudDemo.api.dto.AuthResponse;
import com.cloudDemo.api.dto.Result;
import com.cloudDemo.api.dto.SessionInfo;
import com.cloudDemo.userservice.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 用户服务Sentinel降级处理器 - 升级版分布式会话管理
 */
@Component
public class UserServiceFallbackHandler {

    /**
     * 用户登录降级处理 - 升级版
     */
    public static Result<AuthResponse> userLoginFallback(AuthRequest request, HttpServletRequest httpRequest, Throwable ex) {
        return Result.error("用户登录服务暂时不可用，请稍后重试");
    }

    /**
     * 用户登录降级处理 - 简化版本
     */
    public static Result<AuthResponse> userLoginSimpleFallback(AuthRequest request, Throwable ex) {
        return Result.error("用户登录服务暂时不可用，请稍后重试");
    }

    /**
     * 用户注销降级处理 - 新增
     */
    public static Result<String> userLogoutFallback(String sessionId, Throwable ex) {
        return Result.error("用户注销服务暂时不可用，请稍后重试");
    }

    /**
     * Token验证降级处理 - 新增
     */
    public static Result<SessionInfo> validateTokenFallback(String sessionId, Throwable ex) {
        return Result.error("Token验证服务暂时不可用，请稍后重试");
    }

    /**
     * 用户信息查询降级处理
     */
    public static Result<User> getUserInfoFallback(Integer id, Throwable ex) {
        return Result.error("用户信息查询服务暂时不可用，请稍后重试");
    }

    /**
     * 用户注册降级处理
     */
    public static Result<String> userRegisterFallback(AuthRequest request, Throwable ex) {
        return Result.error("用户注册服务暂时不可用，请稍后重试");
    }

    /**
     * 用户列表查询降级处理
     */
    public static Result<?> getUserListFallback(int page, int size, Throwable ex) {
        return Result.error("用户列表查询服务暂时不可用，请稍后重试");
    }

    /**
     * 用户信息更新降级处理
     */
    public static Result<String> updateUserFallback(User user, Throwable ex) {
        return Result.error("用户信息更新服务暂时不可用，请稍后重试");
    }

    /**
     * 用户删除降级处理
     */
    public static Result<String> deleteUserFallback(Long userId, Throwable ex) {
        return Result.error("用户删除服务暂时不可用，请稍后重试");
    }

    /**
     * Sentinel测试降级处理
     */
    public static Result<String> sentinelTestFallback(int delay, Throwable ex) {
        return Result.error("Sentinel测试服务暂时不可用，请稍后重试。延迟参数: " + delay + "ms");
    }

    /**
     * Sentinel快速失败降级处理
     */
    public static Result<String> sentinelFailFallback(Throwable ex) {
        return Result.error("Sentinel快速失败测试触发降级，原因: " + ex.getMessage());
    }
}
