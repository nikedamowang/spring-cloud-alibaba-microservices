package com.cloudDemo.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 支付回调请求DTO
 */
@Data
@Schema(description = "支付回调请求")
public class PaymentCallbackRequest {

    @Schema(description = "订单号", example = "ORDER-20250729001")
    private String orderNo;

    @Schema(description = "支付结果 (SUCCESS/FAILED)", example = "SUCCESS")
    private String paymentResult;
}
