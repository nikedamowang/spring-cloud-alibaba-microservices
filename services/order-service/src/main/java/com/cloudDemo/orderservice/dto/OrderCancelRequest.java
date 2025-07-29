package com.cloudDemo.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单取消请求DTO
 */
@Data
@Schema(description = "订单取消请求")
public class OrderCancelRequest {

    @Schema(description = "取消原因", example = "用户主动取消")
    private String reason;
}
