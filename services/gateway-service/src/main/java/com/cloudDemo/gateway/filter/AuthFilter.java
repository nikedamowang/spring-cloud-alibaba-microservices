package com.cloudDemo.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 认证过滤器
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    // 不需要认证的路径
    private static final List<String> SKIP_AUTH_PATHS = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/user/list",  // 用户列表接口
            "/api/order/list", // 添加订单列表接口到白名单
            "/actuator",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否需要跳过认证
        if (shouldSkipAuth(path)) {
            return chain.filter(exchange);
        }

        // 获取token
        String token = getToken(request);

        if (!StringUtils.hasText(token)) {
            log.warn("Access denied: No token provided for path: {}", path);
            return unauthorized(exchange);
        }

        // 这里可以添加JWT token验证逻辑
        // 现在简单验证token是否存在
        if (!isValidToken(token)) {
            log.warn("Access denied: Invalid token for path: {}", path);
            return unauthorized(exchange);
        }

        // 验证通过，继续执行
        return chain.filter(exchange);
    }

    private boolean shouldSkipAuth(String path) {
        return SKIP_AUTH_PATHS.stream().anyMatch(skipPath -> path.startsWith(skipPath));
    }

    private String getToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 也可以从query参数获取token
        return request.getQueryParams().getFirst("token");
    }

    private boolean isValidToken(String token) {
        // TODO: 实现真正的JWT token验证逻辑
        // 这里仅做示例，实际应该验证JWT签名、过期时间等
        return StringUtils.hasText(token) && token.length() > 10;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0; // 在日志过滤器之后执行
    }
}
