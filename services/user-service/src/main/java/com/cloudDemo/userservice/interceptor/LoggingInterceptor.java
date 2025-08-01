package com.cloudDemo.userservice.interceptor;

import com.cloudDemo.common.logging.LogContextManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户服务日志拦截器
 * 自动管理请求的链路追踪和上下文信息
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTR, startTime);

        // 从请求头获取链路追踪ID，如果没有则生成新的
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            LogContextManager.initTrace();
        } else {
            LogContextManager.inheritTrace(traceId);
        }

        // 设置请求信息
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null) {
            requestId = LogContextManager.generateSpanId();
        }

        String clientIp = getClientIpAddress(request);
        LogContextManager.setRequestInfo(
                requestId,
                clientIp,
                request.getMethod(),
                request.getRequestURI()
        );

        // 设置用户信息（从JWT token中获取）
        String userId = request.getHeader("X-User-Id");
        if (userId != null) {
            LogContextManager.setUserInfo(userId);
        }

        logger.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime != null) {
            long responseTime = System.currentTimeMillis() - startTime;
            LogContextManager.setResponseInfo(responseTime, response.getStatus());

            if (ex != null) {
                logger.error("Request completed with error: {} {} - Status: {} - Time: {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), responseTime, ex);
            } else {
                logger.info("Request completed: {} {} - Status: {} - Time: {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), responseTime);
            }
        }

        // 清理MDC上下文
        LogContextManager.clear();
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
