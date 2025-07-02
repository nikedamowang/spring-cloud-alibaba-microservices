package com.cloudDemo.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 限流过滤器
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    // 时间窗口：60秒
    private static final long WINDOW_SIZE = 60 * 1000;
    // 限制：每分钟100次请求
    private static final long MAX_REQUESTS = 100;
    private final ConcurrentHashMap<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStart = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = getClientId(exchange);

        if (isRateLimited(clientId)) {
            log.warn("Rate limit exceeded for client: {}", clientId);
            return rateLimitExceeded(exchange);
        }

        return chain.filter(exchange);
    }

    private String getClientId(ServerWebExchange exchange) {
        // 简单使用IP地址作为客户端标识
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        // 如果有认证信息，可以使用用户ID
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "user_" + authHeader.substring(7, Math.min(authHeader.length(), 20));
        }

        return "ip_" + clientIp;
    }

    private boolean isRateLimited(String clientId) {
        long currentTime = System.currentTimeMillis();

        // 清理过期的窗口
        windowStart.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > WINDOW_SIZE);
        requestCounts.entrySet().removeIf(entry ->
                !windowStart.containsKey(entry.getKey()));

        // 检查当前窗口
        long windowStartTime = windowStart.computeIfAbsent(clientId, k -> currentTime);

        // 如果窗口过期，重置计数器
        if (currentTime - windowStartTime > WINDOW_SIZE) {
            windowStart.put(clientId, currentTime);
            requestCounts.put(clientId, new AtomicLong(1));
            return false;
        }

        // 增加请求计数
        AtomicLong count = requestCounts.computeIfAbsent(clientId, k -> new AtomicLong(0));
        long currentCount = count.incrementAndGet();

        return currentCount > MAX_REQUESTS;
    }

    private Mono<Void> rateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
        response.getHeaders().add("X-RateLimit-Window", String.valueOf(WINDOW_SIZE / 1000));
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 1; // 在认证过滤器之后执行
    }
}
