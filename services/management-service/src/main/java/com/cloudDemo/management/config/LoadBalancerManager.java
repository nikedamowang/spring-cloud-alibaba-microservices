package com.cloudDemo.management.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 负载均衡策略管理器
 * 支持动态切换和配置不同服务的负载均衡策略
 */
@Setter
@Component
@Configuration
@ConfigurationProperties(prefix = "dubbo.loadbalancer")
public class LoadBalancerManager {

    // Getters and Setters
    // 服务负载均衡策略映射
    @Getter
    private Map<String, String> strategies = new HashMap<>();

    // 服务权重配置
    private Map<String, Map<String, Integer>> weights = new HashMap<>();

    // 服务超时配置
    @Getter
    private Map<String, Integer> timeouts = new HashMap<>();

    // 服务重试配置
    @Getter
    private Map<String, Integer> retries = new HashMap<>();

    public LoadBalancerManager() {
        // 初始化默认策略
        initDefaultStrategies();
    }

    /**
     * 初始化默认负载均衡策略
     */
    private void initDefaultStrategies() {
        // 用户服务使用轮询策略
        strategies.put("user-service", "roundrobin");

        // 订单服务使用最少活跃调用策略
        strategies.put("order-service", "leastactive");

        // 管理服务使用随机策略
        strategies.put("management-service", "random");

        // 设置默认超时时间
        timeouts.put("user-service", 5000);
        timeouts.put("order-service", 3000);
        timeouts.put("management-service", 8000);

        // 设置默认重试次数
        retries.put("user-service", 2);
        retries.put("order-service", 1);
        retries.put("management-service", 3);
    }

    /**
     * 获取服务的负载均衡策略
     */
    public String getStrategy(String serviceName) {
        return strategies.getOrDefault(serviceName, "roundrobin");
    }

    /**
     * 设置服务的负载均衡策略
     */
    public void setStrategy(String serviceName, String strategy) {
        strategies.put(serviceName, strategy);
    }

    /**
     * 获取服务的权重配置
     */
    public Map<String, Integer> getWeights(String serviceName) {
        return weights.getOrDefault(serviceName, new HashMap<>());
    }

    /**
     * 获取服务的超时配置
     */
    public Integer getTimeout(String serviceName) {
        return timeouts.getOrDefault(serviceName, 5000);
    }

    /**
     * 获取服务的重试配置
     */
    public Integer getRetries(String serviceName) {
        return retries.getOrDefault(serviceName, 2);
    }

    /**
     * 动态更新负载均衡策略
     */
    public void updateStrategy(String serviceName, String strategy, Integer timeout, Integer retry) {
        strategies.put(serviceName, strategy);
        if (timeout != null) {
            timeouts.put(serviceName, timeout);
        }
        if (retry != null) {
            retries.put(serviceName, retry);
        }
    }

}
