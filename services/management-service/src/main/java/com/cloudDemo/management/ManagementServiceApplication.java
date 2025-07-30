package com.cloudDemo.management;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.EventListener;

/**
 * 管理服务应用启动类
 * 提供系统管理和监控功能，包括 Nacos 配置信息获取
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.cloudDemo.management")
@EnableDiscoveryClient
public class ManagementServiceApplication {

    @Value("${server.port:9090}")
    private String serverPort;

    public static void main(String[] args) {
        try {
            SpringApplication.run(ManagementServiceApplication.class, args);
        } catch (Exception e) {
            System.err.println("============================================");
            System.err.println("❌ MANAGEMENT-SERVICE 启动失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("============================================");
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("============================================");
        System.out.println("✅ MANAGEMENT-SERVICE 启动成功！");
        System.out.println("🚀 服务端口: " + serverPort);
        System.out.println("🌐 管理界面: http://localhost:" + serverPort);
        System.out.println("📝 服务名称: management-service");
        System.out.println("🔧 功能: Nacos配置管理 & 服务监控");
        System.out.println("⏰ 启动时间: " + java.time.LocalDateTime.now());
        System.out.println("============================================");
        log.info("Management Service 已成功启动并准备提供管理服务");
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onApplicationFailed(ApplicationFailedEvent event) {
        System.err.println("============================================");
        System.err.println("❌ MANAGEMENT-SERVICE 启动失败！");
        System.err.println("失败原因: " + event.getException().getMessage());
        System.err.println("============================================");
        log.error("Management Service 启动失败", event.getException());
    }
}
