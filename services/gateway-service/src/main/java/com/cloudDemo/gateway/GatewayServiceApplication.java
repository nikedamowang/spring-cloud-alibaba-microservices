package com.cloudDemo.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.EventListener;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayServiceApplication {

    @Value("${server.port:8080}")
    private String serverPort;

    public static void main(String[] args) {
        try {
            SpringApplication.run(GatewayServiceApplication.class, args);
        } catch (Exception e) {
            System.err.println("============================================");
            System.err.println("❌ GATEWAY-SERVICE 启动失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("============================================");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("============================================");
        System.out.println("🚀 GATEWAY-SERVICE 启动成功！");
        System.out.println("📍 端口: " + serverPort);
        System.out.println("🌐 管理地址: http://localhost:" + serverPort + "/actuator");
        System.out.println("============================================");
        log.info("Gateway Service started successfully on port: {}", serverPort);
    }
}
