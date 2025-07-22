package com.cloudDemo.management.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.cloudDemo.management.fallback.ManagementServiceFallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理服务控制器 - 演示Sentinel熔断降级功能
 */
@RestController
@RequestMapping("/management")
@Slf4j
public class ManagementSentinelController {

    /**
     * 配置管理 - 带熔断降级
     */
    @PostMapping("/config")
    @SentinelResource(
            value = "configManage",
            fallback = "configManageFallback",
            fallbackClass = ManagementServiceFallbackHandler.class
    )
    public String configManage(@RequestBody String configInfo) {
        log.info("配置管理操作，配置信息：{}", configInfo);

        if (configInfo == null || configInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("配置信息不能为空");
        }

        // 模拟配置操作的复杂性和可能的异常
        if (Math.random() > 0.7) {
            throw new RuntimeException("配置管理服务异常");
        }

        // 模拟配置操作的延迟
        try {
            Thread.sleep((long) (Math.random() * 4000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "配置管理操作成功";
    }

    /**
     * 服务监控 - 带熔断降级
     */
    @GetMapping("/monitor/{serviceName}")
    @SentinelResource(
            value = "serviceMonitor",
            fallback = "serviceMonitorFallback",
            fallbackClass = ManagementServiceFallbackHandler.class
    )
    public String serviceMonitor(@PathVariable String serviceName) {
        log.info("服务监控，服务名：{}", serviceName);

        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("服务名不能为空");
        }

        // 模拟监控异常
        if (Math.random() > 0.8) {
            throw new RuntimeException("服务监控异常");
        }

        return "服务监控结果：" + serviceName + " - 状态正常，CPU使用率：45%，内存使用率：60%";
    }

    /**
     * 系统管理 - 带熔断降级
     */
    @PostMapping("/system")
    @SentinelResource(
            value = "systemManage",
            fallback = "systemManageFallback",
            fallbackClass = ManagementServiceFallbackHandler.class
    )
    public String systemManage(@RequestBody String systemInfo) {
        log.info("系统管理操作，系统信息：{}", systemInfo);

        if (systemInfo == null || systemInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("系统信息不能为空");
        }

        // 模拟系统管理异常
        if (Math.random() > 0.6) {
            throw new RuntimeException("系统管理服务异常");
        }

        return "系统管理操作成功";
    }
}
