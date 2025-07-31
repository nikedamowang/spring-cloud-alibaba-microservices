package com.cloudDemo.management.monitor.service;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.cloudDemo.management.monitor.dto.ServiceHealthStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 服务健康检查服务
 * 负责监控各个微服务的健康状态
 */
@Slf4j
@Service
public class ServiceHealthCheckService {

    // Redis键前缀
    private static final String HEALTH_STATUS_PREFIX = "service:health:";
    private static final String HEALTH_HISTORY_PREFIX = "service:health:history:";
    // 健康检查超时时间（毫秒）
    private static final int HEALTH_CHECK_TIMEOUT = 3000;
    // 微服务列表
    private static final List<String> MONITORED_SERVICES = Arrays.asList(
            "user-service", "order-service", "gateway-service", "management-service"
    );
    private final RestTemplate restTemplate;
    @Autowired
    private NamingService namingService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ServiceHealthCheckService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 检查所有服务的健康状态
     */
    public List<ServiceHealthStatus> checkAllServicesHealth() {
        List<ServiceHealthStatus> healthStatuses = new ArrayList<>();

        try {
            // 并行检查所有服务
            List<CompletableFuture<List<ServiceHealthStatus>>> futures = new ArrayList<>();

            for (String serviceName : MONITORED_SERVICES) {
                CompletableFuture<List<ServiceHealthStatus>> future =
                        CompletableFuture.supplyAsync(() -> checkServiceHealth(serviceName));
                futures.add(future);
            }

            // 等待所有检查完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(15, TimeUnit.SECONDS)
                    .join();

            // 收集结果
            for (CompletableFuture<List<ServiceHealthStatus>> future : futures) {
                try {
                    healthStatuses.addAll(future.get());
                } catch (Exception e) {
                    log.error("获取服务健康状态失败: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("批量健康检查失败: {}", e.getMessage(), e);
        }

        return healthStatuses;
    }

    /**
     * 检查单个服务的健康状态
     */
    public List<ServiceHealthStatus> checkServiceHealth(String serviceName) {
        List<ServiceHealthStatus> serviceHealthList = new ArrayList<>();

        try {
            // 从Nacos获取服务实例
            List<Instance> instances = namingService.getAllInstances(serviceName);

            if (instances.isEmpty()) {
                // 如果没有实例，创建一个不可用状态记录
                ServiceHealthStatus unavailableStatus = createUnavailableStatus(serviceName);
                serviceHealthList.add(unavailableStatus);
                saveHealthStatus(unavailableStatus);
                return serviceHealthList;
            }

            // 检查每个实例的健康状态
            for (Instance instance : instances) {
                ServiceHealthStatus healthStatus = checkInstanceHealth(serviceName, instance);
                serviceHealthList.add(healthStatus);
                saveHealthStatus(healthStatus);
            }

        } catch (Exception e) {
            log.error("检查服务 {} 健康状态失败: {}", serviceName, e.getMessage());
            ServiceHealthStatus errorStatus = createErrorStatus(serviceName, e.getMessage());
            serviceHealthList.add(errorStatus);
            saveHealthStatus(errorStatus);
        }

        return serviceHealthList;
    }

    /**
     * 检查单个实例的健康状态
     */
    private ServiceHealthStatus checkInstanceHealth(String serviceName, Instance instance) {
        ServiceHealthStatus.ServiceHealthStatusBuilder builder = ServiceHealthStatus.builder()
                .serviceName(serviceName)
                .instanceId(instance.getInstanceId())
                .ipAddress(instance.getIp())
                .port(instance.getPort())
                .weight(instance.getWeight())
                .enabled(instance.isEnabled())
                .clusterName(instance.getClusterName())
                .metadata(instance.getMetadata())
                .lastCheckTime(LocalDateTime.now());

        try {
            // 简化的健康检查：检查服务端口是否可达
            long startTime = System.currentTimeMillis();
            boolean isHealthy = checkServiceReachability(instance);
            long responseTime = System.currentTimeMillis() - startTime;

            builder.responseTime(responseTime);

            if (isHealthy) {
                builder.healthStatus("HEALTHY")
                        .serviceStatus("UP")
                        .consecutiveHealthyChecks(getConsecutiveHealthyChecks(serviceName, instance.getInstanceId()) + 1)
                        .consecutiveFailureChecks(0);
            } else {
                builder.healthStatus("UNHEALTHY")
                        .serviceStatus("DOWN")
                        .consecutiveHealthyChecks(0)
                        .consecutiveFailureChecks(getConsecutiveFailureChecks(serviceName, instance.getInstanceId()) + 1)
                        .errorMessage("服务不可达");
            }

        } catch (Exception e) {
            builder.healthStatus("UNKNOWN")
                    .serviceStatus("DOWN")
                    .consecutiveHealthyChecks(0)
                    .consecutiveFailureChecks(getConsecutiveFailureChecks(serviceName, instance.getInstanceId()) + 1)
                    .errorMessage("健康检查异常: " + e.getMessage())
                    .responseTime((long) HEALTH_CHECK_TIMEOUT);
        }

        // 计算健康检查成功率
        double successRate = calculateHealthCheckSuccessRate(serviceName, instance.getInstanceId());
        builder.healthCheckSuccessRate(successRate);

        return builder.build();
    }

    /**
     * 检查服务可达性（简化版）
     */
    private boolean checkServiceReachability(Instance instance) {
        try {
            // 构建简单的健康检查URL（ping接口）
            String pingUrl = String.format("http://%s:%d/actuator/health",
                    instance.getIp(), instance.getPort());

            // 设置较短的超时时间
            ResponseEntity<String> response = restTemplate.getForEntity(pingUrl, String.class);

            // 检查响应状态
            return response.getStatusCode() == HttpStatus.OK;

        } catch (ResourceAccessException e) {
            // 连接超时或网络不可达
            log.debug("服务不可达 - {}:{}, 错误: {}", instance.getIp(), instance.getPort(), e.getMessage());
            return false;
        } catch (Exception e) {
            // 其他异常也认为不健康
            log.debug("健康检查异常 - {}:{}, 错误: {}", instance.getIp(), instance.getPort(), e.getMessage());
            return false;
        }
    }

    /**
     * 保存健康状态到Redis
     */
    private void saveHealthStatus(ServiceHealthStatus healthStatus) {
        try {
            String key = HEALTH_STATUS_PREFIX + healthStatus.getServiceName() + ":" +
                    (healthStatus.getInstanceId() != null ? healthStatus.getInstanceId() : "unknown");

            // 保存当前状态
            redisTemplate.opsForValue().set(key, healthStatus, 1, TimeUnit.HOURS);

            // 保存历史记录（用于计算成功率）
            String historyKey = HEALTH_HISTORY_PREFIX + healthStatus.getServiceName() + ":" +
                    (healthStatus.getInstanceId() != null ? healthStatus.getInstanceId() : "unknown");

            String historyRecord = String.format("%s:%s:%d",
                    LocalDateTime.now().toString(),
                    healthStatus.getHealthStatus(),
                    healthStatus.getResponseTime() != null ? healthStatus.getResponseTime() : 0);

            // 使用List保存历史记录，限制为最近100条
            redisTemplate.opsForList().leftPush(historyKey, historyRecord);
            redisTemplate.opsForList().trim(historyKey, 0, 99);
            redisTemplate.expire(historyKey, 24, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("保存健康状态失败: {}", e.getMessage());
        }
    }

    /**
     * 获取连续健康检查次数
     */
    private int getConsecutiveHealthyChecks(String serviceName, String instanceId) {
        try {
            String key = HEALTH_STATUS_PREFIX + serviceName + ":" + instanceId;
            ServiceHealthStatus lastStatus = (ServiceHealthStatus) redisTemplate.opsForValue().get(key);

            if (lastStatus != null && "HEALTHY".equals(lastStatus.getHealthStatus())) {
                return lastStatus.getConsecutiveHealthyChecks() != null ?
                        lastStatus.getConsecutiveHealthyChecks() : 0;
            }
        } catch (Exception e) {
            log.debug("获取连续健康检查次数失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 获取连续失败检查次数
     */
    private int getConsecutiveFailureChecks(String serviceName, String instanceId) {
        try {
            String key = HEALTH_STATUS_PREFIX + serviceName + ":" + instanceId;
            ServiceHealthStatus lastStatus = (ServiceHealthStatus) redisTemplate.opsForValue().get(key);

            if (lastStatus != null && !"HEALTHY".equals(lastStatus.getHealthStatus())) {
                return lastStatus.getConsecutiveFailureChecks() != null ?
                        lastStatus.getConsecutiveFailureChecks() : 0;
            }
        } catch (Exception e) {
            log.debug("获取连续失败检查次数失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 计算健康检查成功率
     */
    private double calculateHealthCheckSuccessRate(String serviceName, String instanceId) {
        try {
            String historyKey = HEALTH_HISTORY_PREFIX + serviceName + ":" + instanceId;
            List<Object> history = redisTemplate.opsForList().range(historyKey, 0, 59); // 最近60次检查

            if (history == null || history.isEmpty()) {
                return 100.0; // 没有历史记录时默认100%
            }

            long successCount = history.stream()
                    .filter(record -> record.toString().contains(":HEALTHY:"))
                    .count();

            return (double) successCount / history.size() * 100.0;

        } catch (Exception e) {
            log.debug("计算健康检查成功率失败: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * 创建不可用状态记录
     */
    private ServiceHealthStatus createUnavailableStatus(String serviceName) {
        return ServiceHealthStatus.builder()
                .serviceName(serviceName)
                .instanceId("NO_INSTANCE")
                .healthStatus("UNAVAILABLE")
                .serviceStatus("DOWN")
                .lastCheckTime(LocalDateTime.now())
                .consecutiveHealthyChecks(0)
                .consecutiveFailureChecks(1)
                .healthCheckSuccessRate(0.0)
                .errorMessage("没有可用的服务实例")
                .responseTime(0L)
                .build();
    }

    /**
     * 创建错误状态记录
     */
    private ServiceHealthStatus createErrorStatus(String serviceName, String errorMessage) {
        return ServiceHealthStatus.builder()
                .serviceName(serviceName)
                .instanceId("ERROR")
                .healthStatus("ERROR")
                .serviceStatus("UNKNOWN")
                .lastCheckTime(LocalDateTime.now())
                .consecutiveHealthyChecks(0)
                .consecutiveFailureChecks(1)
                .healthCheckSuccessRate(0.0)
                .errorMessage(errorMessage)
                .responseTime((long) HEALTH_CHECK_TIMEOUT)
                .build();
    }

    /**
     * 获取所有服务的健康状态汇总
     */
    public Map<String, Object> getHealthSummary() {
        List<ServiceHealthStatus> allStatuses = checkAllServicesHealth();

        Map<String, Object> summary = new HashMap<>();

        // 按服务分组统计
        Map<String, List<ServiceHealthStatus>> groupedByService = new HashMap<>();
        for (ServiceHealthStatus status : allStatuses) {
            groupedByService.computeIfAbsent(status.getServiceName(), k -> new ArrayList<>()).add(status);
        }

        // 计算总体统计
        long totalInstances = allStatuses.size();
        long healthyInstances = allStatuses.stream().filter(s -> "HEALTHY".equals(s.getHealthStatus())).count();
        long unhealthyInstances = allStatuses.stream().filter(s -> "UNHEALTHY".equals(s.getHealthStatus())).count();
        long unknownInstances = totalInstances - healthyInstances - unhealthyInstances;

        double overallHealthRate = totalInstances > 0 ? (double) healthyInstances / totalInstances * 100.0 : 0.0;

        summary.put("totalServices", groupedByService.size());
        summary.put("totalInstances", totalInstances);
        summary.put("healthyInstances", healthyInstances);
        summary.put("unhealthyInstances", unhealthyInstances);
        summary.put("unknownInstances", unknownInstances);
        summary.put("overallHealthRate", Math.round(overallHealthRate * 100.0) / 100.0);
        summary.put("lastCheckTime", LocalDateTime.now());
        summary.put("serviceDetails", groupedByService);

        return summary;
    }
}
