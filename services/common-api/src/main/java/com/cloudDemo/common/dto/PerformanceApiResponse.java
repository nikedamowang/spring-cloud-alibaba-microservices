package com.cloudDemo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 性能测试专用API响应结构
 * 针对JMeter测试优化，确保返回结构标准化
 */
@Data
@Schema(description = "性能测试API响应结构")
public class PerformanceApiResponse<T> {

    /**
     * HTTP状态码：200-成功，400-客户端错误，500-服务器错误
     */
    @JsonProperty("code")
    @Schema(description = "HTTP状态码", example = "200")
    private Integer code;

    /**
     * 操作是否成功
     */
    @JsonProperty("success")
    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;

    /**
     * 响应消息（英文，避免JMeter乱码）
     */
    @JsonProperty("message")
    @Schema(description = "响应消息", example = "Operation successful")
    private String message;

    /**
     * 响应数据
     */
    @JsonProperty("data")
    @Schema(description = "响应数据")
    private T data;

    /**
     * 响应时间戳
     */
    @JsonProperty("timestamp")
    @Schema(description = "响应时间戳", example = "1641234567890")
    private Long timestamp;

    /**
     * 请求处理耗时（毫秒）
     */
    @JsonProperty("duration")
    @Schema(description = "请求处理耗时(毫秒)", example = "45")
    private Long duration;

    public PerformanceApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public PerformanceApiResponse(Integer code, Boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应 - 适用于性能测试
     */
    public static <T> PerformanceApiResponse<T> success(T data) {
        return new PerformanceApiResponse<>(200, true, "Operation successful", data);
    }

    public static <T> PerformanceApiResponse<T> success(String message, T data) {
        return new PerformanceApiResponse<>(200, true, message, data);
    }

    /**
     * 客户端错误响应 - 适用于参数校验测试
     */
    public static <T> PerformanceApiResponse<T> clientError(String message) {
        return new PerformanceApiResponse<>(400, false, message, null);
    }

    public static <T> PerformanceApiResponse<T> clientError(String message, T errorData) {
        return new PerformanceApiResponse<>(400, false, message, errorData);
    }

    /**
     * 服务器错误响应 - 适用于异常测试
     */
    public static <T> PerformanceApiResponse<T> serverError(String message) {
        return new PerformanceApiResponse<>(500, false, message, null);
    }

    /**
     * 自定义状态码响应
     */
    public static <T> PerformanceApiResponse<T> custom(Integer code, Boolean success, String message, T data) {
        return new PerformanceApiResponse<>(code, success, message, data);
    }

    /**
     * 设置处理耗时
     */
    public PerformanceApiResponse<T> withDuration(Long startTime) {
        this.duration = System.currentTimeMillis() - startTime;
        return this;
    }
}
