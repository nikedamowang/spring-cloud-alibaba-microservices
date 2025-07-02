package com.cloudDemo.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway路由配置
 * 注意：当使用Nacos动态路由时，建议注释掉这个配置类避免冲突
 */
@Configuration
public class GatewayConfig {

    // 临时启用Java路由配置，确保服务正常工作 - 使用IP地址直接路由
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户服务路由 - 临时使用直接IP地址
                .route("user-service-route", r -> r
                        .path("/api/user/**")
                        .filters(f -> f
                                .stripPrefix(1)  // /api/user/list -> /user/list
                                .addRequestHeader("X-Gateway", "cloudDemo-gateway")
                        )
                        .uri("http://localhost:9000")  // 直接使用IP地址，避免服务发现问题
                )

                // 订单服务路由 - 使用正确的端口8000
                .route("order-service-route", r -> r
                        .path("/api/order/**")
                        .filters(f -> f
                                .stripPrefix(1)  // /api/order/list -> /order/list
                                .addRequestHeader("X-Gateway", "cloudDemo-gateway")
                        )
                        .uri("http://localhost:8000")  // 修正为8000端口
                )

                // 健康检查路由
                .route("health-check", r -> r
                        .path("/health/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:9000")
                )

                .build();
    }
}
