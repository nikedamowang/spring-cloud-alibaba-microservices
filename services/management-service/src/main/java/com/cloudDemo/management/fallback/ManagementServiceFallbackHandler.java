package com.cloudDemo.management.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 管理服务降级处理器
 */
@Slf4j
@Component
public class ManagementServiceFallbackHandler {

    /**
     * 配置管理降级方法
     */
    public static String configManageFallback(Object configInfo, Throwable ex) {
        log.warn("配置管理服务降级，异常：{}", ex.getMessage());
        return "配置管理服务暂时不可用，请稍后重试";
    }

    /**
     * 服务监控降级方法
     */
    public static String serviceMonitorFallback(String serviceName, Throwable ex) {
        log.warn("服务监控降级，服务名：{}，异常：{}", serviceName, ex.getMessage());
        return "服务监控功能暂时不可用，请稍后重试";
    }

    /**
     * 系统管理降级方法
     */
    public static String systemManageFallback(Object systemInfo, Throwable ex) {
        log.warn("系统管理服务降级，异常：{}", ex.getMessage());
        return "系统管理服务暂时不可用，请稍后重试";
    }

    /**
     * 通用服务降级方法
     */
    public static String commonFallback(Throwable ex) {
        log.warn("管理服务发生降级，异常：{}", ex.getMessage());
        return "管理服务暂时不可用，请稍后重试";
    }
}
