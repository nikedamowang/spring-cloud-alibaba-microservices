package com.cloudDemo.gateway.filter;

import com.cloudDemo.common.logging.LogContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关全局日志过滤器
 * 处理请求的链路追踪和访问日志记录
 */
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(GlobalLoggingFilter.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        // 初始化链路追踪
        String traceId = request.getHeaders().getFirst("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            LogContextManager.initTrace();
            traceId = LogContextManager.getCurrentTraceId();
        } else {
            LogContextManager.inheritTrace(traceId);
        }

        // 设置请求信息
        String requestId = LogContextManager.generateSpanId();
        String clientIp = getClientIpAddress(request);
        LogContextManager.setRequestInfo(
                requestId,
                clientIp,
                request.getMethod().name(),
                request.getURI().getPath()
        );

        // 在请求头中添加链路追踪ID，传递给下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Trace-Id", traceId)
                .header("X-Request-Id", requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        logger.info("Gateway request started: {} {}", request.getMethod(), request.getURI().getPath());

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> {
                    // 记录响应信息
                    ServerHttpResponse response = exchange.getResponse();
                    long responseTime = System.currentTimeMillis() - startTime;

                    // 安全检查，避免空指针异常
                    int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 500;
                    LogContextManager.setResponseInfo(responseTime, statusCode);

                    // 记录访问日志
                    accessLogger.info("Gateway request completed: {} {} - Status: {} - Time: {}ms",
                            request.getMethod(), request.getURI().getPath(),
                            statusCode, responseTime);

                    logger.info("Gateway request completed: {} {} - Status: {} - Time: {}ms",
                            request.getMethod(), request.getURI().getPath(),
                            statusCode, responseTime);

                    // 清理MDC上下文
                    LogContextManager.clear();
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // WebFlux中获取远程地址的方式
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }
}
