package com.cloudDemo.gateway.filter;

import com.cloudDemo.gateway.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 认证过滤器 - 集成JWT和Redis的完整认证方案
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private AuthService authService;

    // 不需要认证的路径
    private static final List<String> SKIP_AUTH_PATHS = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/user/list",  // 用户列表接口
            "/api/order/list", // 订单列表接口
            "/redis/ping",     // Redis测试接口
            "/actuator",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Processing request: {} {}", request.getMethod(), path);

        // 检查是否需要跳过认证
        if (shouldSkipAuth(path)) {
            log.debug("Skipping auth for path: {}", path);
            return chain.filter(exchange);
        }

        // 获取token
        String token = getToken(request);

        if (!StringUtils.hasText(token)) {
            log.warn("Access denied: No token provided for path: {}", path);
            return unauthorized(exchange, "Missing authentication token");
        }

        // 使用AuthService验证JWT token
        return authService.validateToken(token)
                .flatMap(userInfo -> {
                    log.debug("Authentication successful for user: {} accessing path: {}",
                             userInfo.getUserId(), path);

                    // 将用户信息添加到请求头中，供下游服务使用
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userInfo.getUserId())
                            .header("X-Username", userInfo.getUsername())
                            .header("X-User-Roles", userInfo.getRoles())
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .switchIfEmpty(
                    Mono.defer(() -> {
                        log.warn("Access denied: Invalid or expired token for path: {}", path);
                        return unauthorized(exchange, "Invalid or expired token");
                    })
                );
    }

    private boolean shouldSkipAuth(String path) {
        return SKIP_AUTH_PATHS.stream().anyMatch(skipPath -> path.startsWith(skipPath));
    }

    private String getToken(ServerHttpRequest request) {
        // 优先从Authorization header获取
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 备选方案：从query参数获取token
        return request.getQueryParams().getFirst("token");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // 添加错误信息到响应头
        response.getHeaders().add("X-Auth-Error", message);

        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 1; // 在日志过滤器之后执行
    }
}
