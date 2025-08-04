package com.cloudDemo.common.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 日志上下文管理器
 * 用于管理MDC中的链路追踪信息和其他上下文数据
 */
@Component
public class LogContextManager {

    // MDC键名常量
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_ID = "requestId";
    public static final String CLIENT_IP = "clientIp";
    public static final String METHOD = "method";
    public static final String URI = "uri";
    public static final String RESPONSE_TIME = "responseTime";
    public static final String STATUS = "status";
    public static final String OPERATION = "operation";

    /**
     * 生成新的链路追踪ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成新的Span ID
     */
    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 设置链路追踪信息
     */
    public static void setTraceInfo(String traceId, String spanId) {
        MDC.put(TRACE_ID, traceId);
        MDC.put(SPAN_ID, spanId);
    }

    /**
     * 设置用户信息
     */
    public static void setUserInfo(String userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
    }

    /**
     * 设置请求信息
     */
    public static void setRequestInfo(String requestId, String clientIp, String method, String uri) {
        if (requestId != null) {
            MDC.put(REQUEST_ID, requestId);
        }
        if (clientIp != null) {
            MDC.put(CLIENT_IP, clientIp);
        }
        if (method != null) {
            MDC.put(METHOD, method);
        }
        if (uri != null) {
            MDC.put(URI, uri);
        }
    }

    /**
     * 设置响应信息
     */
    public static void setResponseInfo(long responseTime, int status) {
        MDC.put(RESPONSE_TIME, String.valueOf(responseTime));
        MDC.put(STATUS, String.valueOf(status));
    }

    /**
     * 设置操作信息（用于审计日志）
     */
    public static void setOperationInfo(String operation) {
        if (operation != null) {
            MDC.put(OPERATION, operation);
        }
    }

    /**
     * 获取当前链路追踪ID
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 获取当前Span ID
     */
    public static String getCurrentSpanId() {
        return MDC.get(SPAN_ID);
    }

    /**
     * 初始化新的链路追踪
     * 生成新的traceId和spanId
     */
    public static void initTrace() {
        String traceId = generateTraceId();
        String spanId = generateSpanId();
        setTraceInfo(traceId, spanId);
    }

    /**
     * 继承已有的链路追踪ID
     * @param traceId 已有的链路追踪ID
     */
    public static void inheritTrace(String traceId) {
        String spanId = generateSpanId();
        setTraceInfo(traceId, spanId);
    }

    /**
     * 清理所有MDC上下文
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        return MDC.get(USER_ID);
    }

    /**
     * 获取当前请求ID
     */
    public static String getCurrentRequestId() {
        return MDC.get(REQUEST_ID);
    }
}
