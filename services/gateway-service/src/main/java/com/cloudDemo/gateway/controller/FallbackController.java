package com.cloudDemo.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback降级处理控制器
 */
@Slf4j
@RestController
public class FallbackController {

    @RequestMapping("/fallback/user-service")
    public Mono<Map<String, Object>> userServiceFallback() {
        log.warn("User service is unavailable, fallback triggered");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 503);
        result.put("message", "用户服务暂时不可用，请稍后重试");
        result.put("service", "user-service");
        result.put("timestamp", System.currentTimeMillis());
        return Mono.just(result);
    }

    @RequestMapping("/fallback/order-service")
    public Mono<Map<String, Object>> orderServiceFallback() {
        log.warn("Order service is unavailable, fallback triggered");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 503);
        result.put("message", "订单服务暂时不可用，请稍后重试");
        result.put("service", "order-service");
        result.put("timestamp", System.currentTimeMillis());
        return Mono.just(result);
    }

    @RequestMapping("/fallback/default")
    public Mono<Map<String, Object>> defaultFallback() {
        log.warn("Service is unavailable, default fallback triggered");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 503);
        result.put("message", "服务暂时不可用，请稍后重试");
        result.put("timestamp", System.currentTimeMillis());
        return Mono.just(result);
    }
}
