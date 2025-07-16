package com.cloudDemo.management.controller;

import com.cloudDemo.management.config.LoadBalancerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 负载均衡管理控制器
 * 提供负载均衡策略的动态管理API
 */
@RestController
@RequestMapping("/api/loadbalancer")
public class LoadBalancerController {

    @Autowired
    private LoadBalancerManager loadBalancerManager;

    /**
     * 获取所有服务的负载均衡策略
     */
    @GetMapping("/strategies")
    public Map<String, Object> getAllStrategies() {
        Map<String, Object> result = new HashMap<>();
        result.put("strategies", loadBalancerManager.getStrategies());
        result.put("timeouts", loadBalancerManager.getTimeouts());
        result.put("retries", loadBalancerManager.getRetries());
        return result;
    }

    /**
     * 获取指定服务的负载均衡策略
     */
    @GetMapping("/strategies/{serviceName}")
    public Map<String, Object> getServiceStrategy(@PathVariable String serviceName) {
        Map<String, Object> result = new HashMap<>();
        result.put("service", serviceName);
        result.put("strategy", loadBalancerManager.getStrategy(serviceName));
        result.put("timeout", loadBalancerManager.getTimeout(serviceName));
        result.put("retries", loadBalancerManager.getRetries(serviceName));
        result.put("weights", loadBalancerManager.getWeights(serviceName));
        return result;
    }

    /**
     * 更新服务的负载均衡策略
     */
    @PutMapping("/strategies/{serviceName}")
    public Map<String, Object> updateServiceStrategy(
            @PathVariable String serviceName,
            @RequestParam String strategy,
            @RequestParam(required = false) Integer timeout,
            @RequestParam(required = false) Integer retries) {

        loadBalancerManager.updateStrategy(serviceName, strategy, timeout, retries);

        Map<String, Object> result = new HashMap<>();
        result.put("service", serviceName);
        result.put("strategy", strategy);
        result.put("timeout", timeout);
        result.put("retries", retries);
        result.put("message", "负载均衡策略更新成功");
        return result;
    }

    /**
     * 获取可用的负载均衡策略列表
     */
    @GetMapping("/available-strategies")
    public Map<String, Object> getAvailableStrategies() {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> strategies = new HashMap<>();

        strategies.put("random", "随机负载均衡");
        strategies.put("roundrobin", "轮询负载均衡");
        strategies.put("leastactive", "最少活跃调用负载均衡");
        strategies.put("shortestresponse", "最短响应负载均衡");
        strategies.put("consistenthash", "一致性Hash负载均衡");
        strategies.put("adaptivesample", "自适应负载均衡");
        strategies.put("customweighted", "自定义权重负载均衡");

        result.put("strategies", strategies);

        Map<String, String> clusters = new HashMap<>();
        clusters.put("failover", "失败自动切换");
        clusters.put("failfast", "快速失败");
        clusters.put("failsafe", "失败安全");
        clusters.put("failback", "失败自动恢复");
        clusters.put("forking", "并行调用");
        clusters.put("broadcast", "广播调用");

        result.put("clusters", clusters);
        return result;
    }

    /**
     * 获取负载均衡统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getLoadBalancerStats() {
        Map<String, Object> result = new HashMap<>();

        // 这里可以添加实际的统计信息收集逻辑
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", 1000);
        stats.put("successfulRequests", 980);
        stats.put("failedRequests", 20);
        stats.put("averageResponseTime", 150);

        result.put("stats", stats);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("loadBalancerManager", "ACTIVE");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
