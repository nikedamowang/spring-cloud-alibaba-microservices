package com.cloudDemo.management.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 服务调用统计数据模型
 * 用于记录和展示服务间调用的统计信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCallStats {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 调用者服务名称
     */
    private String consumerService;

    /**
     * 提供者服务名称
     */
    private String providerService;

    /**
     * 总调用次数
     */
    private Long totalCalls;

    /**
     * 成功调用次数
     */
    private Long successCalls;

    /**
     * 失败调用次数
     */
    private Long failureCalls;

    /**
     * 平均响应时间(毫秒)
     */
    private Double avgResponseTime;

    /**
     * 最大响应时间(毫秒)
     */
    private Long maxResponseTime;

    /**
     * 最小响应时间(毫秒)
     */
    private Long minResponseTime;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 最后调用时间
     */
    private LocalDateTime lastCallTime;

    /**
     * 统计时间窗口开始时间
     */
    private LocalDateTime windowStartTime;

    /**
     * 统计时间窗口结束时间
     */
    private LocalDateTime windowEndTime;

    /**
     * 每分钟调用次数（用于QPS计算）
     */
    private Long callsPerMinute;

    /**
     * QPS（每秒查询率）
     */
    private Double qps;
}
