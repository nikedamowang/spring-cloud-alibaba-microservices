package com.cloudDemo.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局日志过滤器
 */
@Slf4j
@Component
public class GlobalLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 记录请求信息
        log.info("Gateway Request: {} {}", request.getMethod(), request.getURI());
        log.info("Gateway Request Headers: {}", request.getHeaders());

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    log.info("Gateway Response: {} {} - {}ms",
                            request.getMethod(),
                            request.getURI(),
                            endTime - startTime);
                })
        );
    }

    @Override
    public int getOrder() {
        return -1; // 优先级最高
    }
}
