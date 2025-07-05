package com.cloudDemo.orderservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.EventListener;

@Slf4j
@EnableDubbo
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.cloudDemo.orderservice.mapper")
public class OrdersServiceApplication {

    @Value("${server.port:8000}")
    private String serverPort;

    public static void main(String[] args) {
        try {
            SpringApplication.run(OrdersServiceApplication.class, args);
        } catch (Exception e) {
            System.err.println("============================================");
            System.err.println("❌ ORDER-SERVICE 启动失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("============================================");
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("============================================");
        System.out.println("✅ ORDER-SERVICE 启动成功！");
        System.out.println("🚀 服务端口: " + serverPort);
        System.out.println("🌐 服务地址: http://localhost:" + serverPort);
        System.out.println("📝 服务名称: order-service");
        System.out.println("⏰ 启动时间: " + java.time.LocalDateTime.now());
        System.out.println("============================================");
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onApplicationFailed(ApplicationFailedEvent event) {
        System.err.println("============================================");
        System.err.println("❌ ORDER-SERVICE 启动失败！");
        System.err.println("错误信息: " + event.getException().getMessage());
        System.err.println("============================================");
    }
}
