package com.cloudDemo.gateway.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.ReactiveHealthIndicator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 网关路由健康检查（响应式）
 */
@Component("routeHealth")
public class RouteHealthIndicator implements ReactiveHealthIndicator {

    @Autowired
    private RouteLocator routeLocator;

    @Override
    public Mono<Health> health() {
        return routeLocator.getRoutes()
                .collectList()
                .map(routes -> {
                    int routeCount = routes.size();

                    if (routeCount > 0) {
                        return Health.up()
                                .withDetail("gateway", "Routes Available")
                                .withDetail("service", "gateway-service")
                                .withDetail("routeCount", routeCount)
                                .withDetail("status", "All routes configured")
                                .build();
                    } else {
                        return Health.down()
                                .withDetail("gateway", "No Routes")
                                .withDetail("service", "gateway-service")
                                .withDetail("routeCount", 0)
                                .withDetail("status", "No routes configured")
                                .build();
                    }
                })
                .onErrorReturn(
                        Health.down()
                                .withDetail("gateway", "Route Check Failed")
                                .withDetail("service", "gateway-service")
                                .withDetail("status", "Failed to retrieve routes")
                                .build()
                );
    }
}
