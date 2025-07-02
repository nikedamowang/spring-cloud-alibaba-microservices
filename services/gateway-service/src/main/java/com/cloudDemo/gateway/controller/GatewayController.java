package com.cloudDemo.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RouteLocator routeLocator;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        result.put("gateway", "cloudDemo-gateway");
        return Mono.just(result);
    }

    /**
     * 获取所有注册的服务
     */
    @GetMapping("/services")
    public Mono<Map<String, Object>> getServices() {
        Map<String, Object> result = new HashMap<>();
        result.put("services", discoveryClient.getServices());
        return Mono.just(result);
    }

    /**
     * 获取路由信息
     */
    @GetMapping("/routes")
    public Mono<Map<String, Object>> getRoutes() {
        return routeLocator.getRoutes()
                .collectMap(route -> route.getId(), route -> {
                    Map<String, Object> routeInfo = new HashMap<>();
                    routeInfo.put("id", route.getId());
                    routeInfo.put("uri", route.getUri().toString());
                    routeInfo.put("predicates", route.getPredicate().toString());
                    routeInfo.put("filters", route.getFilters().toString());
                    routeInfo.put("order", route.getOrder());
                    return routeInfo;
                })
                .map(routes -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("routes", routes);
                    result.put("count", routes.size());
                    return result;
                });
    }
}
