package com.cloudDemo.management.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 单次服务调用记录
 * 用于记录每次具体的服务调用详情
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCallRecord {

    /**
     * 调用ID（唯一标识）
     */
    private String callId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 调用者IP
     */
    private String consumerIp;

    /**
     * 提供者IP
     */
    private String providerIp;

    /**
     * 调用开始时间
     */
    private LocalDateTime startTime;

    /**
     * 调用结束时间
     */
    private LocalDateTime endTime;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 调用状态：SUCCESS/FAILURE
     */
    private String status;

    /**
     * 错误信息（如果调用失败）
     */
    private String errorMessage;

    /**
     * 请求参数（简化版）
     */
    private String requestParams;

    /**
     * 响应结果状态
     */
    private String responseStatus;
}
