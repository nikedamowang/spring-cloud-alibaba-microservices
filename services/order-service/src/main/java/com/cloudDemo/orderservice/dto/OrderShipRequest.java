package com.cloudDemo.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单发货请求DTO
 */
@Data
@Schema(description = "订单发货请求")
public class OrderShipRequest {

    @Schema(description = "物流单号", example = "TRK123456789")
    private String trackingNumber;
}
