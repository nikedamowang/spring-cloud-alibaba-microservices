package com.cloudDemo.gateway.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 网关服务Nacos连接健康检查（响应式）
 */
@Component("nacosHealth")
public class NacosHealthIndicator implements ReactiveHealthIndicator {

    @Value("${spring.cloud.nacos.discovery.server-addr:127.0.0.1:8848}")
    private String nacosServerAddr;

    @Override
    public Mono<Health> health() {
        return Mono.fromCallable(() -> {
            try {
                String nacosUrl = "http://" + nacosServerAddr + "/nacos/v1/ns/operator/servers";
                URL url = new URL(nacosUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    return Health.up()
                            .withDetail("nacos", "Connected")
                            .withDetail("service", "gateway-service")
                            .withDetail("serverAddr", nacosServerAddr)
                            .withDetail("status", "Available")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("nacos", "Server Error")
                            .withDetail("service", "gateway-service")
                            .withDetail("serverAddr", nacosServerAddr)
                            .withDetail("responseCode", responseCode)
                            .build();
                }
            } catch (IOException e) {
                return Health.down()
                        .withDetail("nacos", "Connection Failed")
                        .withDetail("service", "gateway-service")
                        .withDetail("serverAddr", nacosServerAddr)
                        .withDetail("error", e.getMessage())
                        .build();
            }
        });
    }
}
