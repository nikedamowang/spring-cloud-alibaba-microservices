package com.cloudDemo.management.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口响应时间趋势数据模型
 * 用于分析和展示接口响应时间的变化趋势
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTimeTrend {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 时间窗口类型：MINUTE/HOUR/DAY
     */
    private String timeWindowType;

    /**
     * 分析开始时间
     */
    private LocalDateTime startTime;

    /**
     * 分析结束时间
     */
    private LocalDateTime endTime;

    /**
     * 时间点数据列表
     */
    private List<TimePoint> timePoints;

    /**
     * 平均响应时间
     */
    private Double avgResponseTime;

    /**
     * 最大响应时间
     */
    private Long maxResponseTime;

    /**
     * 最小响应时间
     */
    private Long minResponseTime;

    /**
     * 响应时间标准差
     */
    private Double responseTimeStdDev;

    /**
     * 趋势类型：STABLE/INCREASING/DECREASING/FLUCTUATING
     */
    private String trendType;

    /**
     * 性能等级：EXCELLENT/GOOD/NORMAL/POOR/CRITICAL
     */
    private String performanceLevel;

    /**
     * 异常时间点数量
     */
    private Integer anomalyCount;

    /**
     * 总数据点数量
     */
    private Integer totalDataPoints;

    /**
     * P95响应时间
     */
    private Double p95ResponseTime;

    /**
     * P99响应时间
     */
    private Double p99ResponseTime;

    /**
     * 时间点数据
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimePoint {
        /**
         * 时间戳
         */
        private LocalDateTime timestamp;

        /**
         * 平均响应时间
         */
        private Double avgResponseTime;

        /**
         * 最大响应时间
         */
        private Long maxResponseTime;

        /**
         * 最小响应时间
         */
        private Long minResponseTime;

        /**
         * 调用次数
         */
        private Long callCount;

        /**
         * 是否异常点
         */
        private Boolean isAnomaly;

        /**
         * 异常原因
         */
        private String anomalyReason;
    }
}
