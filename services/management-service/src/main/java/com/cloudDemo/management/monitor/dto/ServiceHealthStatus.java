package com.cloudDemo.management.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 服务健康状态数据模型
 * 用于记录和展示各个微服务的健康状态信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceHealthStatus {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务实例ID
     */
    private String instanceId;

    /**
     * 服务IP地址
     */
    private String ipAddress;

    /**
     * 服务端口
     */
    private Integer port;

    /**
     * 健康状态：HEALTHY/UNHEALTHY/UNKNOWN
     */
    private String healthStatus;

    /**
     * 服务状态：UP/DOWN/STARTING/STOPPING
     */
    private String serviceStatus;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 连续健康检查次数
     */
    private Integer consecutiveHealthyChecks;

    /**
     * 连续失败检查次数
     */
    private Integer consecutiveFailureChecks;

    /**
     * 健康检查成功率（过去1小时）
     */
    private Double healthCheckSuccessRate;

    /**
     * 服务版本
     */
    private String version;

    /**
     * 服务权重
     */
    private Double weight;

    /**
     * 是否启用状态
     */
    private Boolean enabled;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 错误信息（如果健康检查失败）
     */
    private String errorMessage;

    /**
     * 服务元数据
     */
    private Map<String, String> metadata;

    /**
     * CPU使用率（如果可获取）
     */
    private Double cpuUsage;

    /**
     * 内存使用率（如果可获取）
     */
    private Double memoryUsage;

    /**
     * 活跃连接数
     */
    private Integer activeConnections;

    /**
     * 服务启动时间
     */
    private LocalDateTime startTime;

    /**
     * 健康检查URL
     */
    private String healthCheckUrl;
}
