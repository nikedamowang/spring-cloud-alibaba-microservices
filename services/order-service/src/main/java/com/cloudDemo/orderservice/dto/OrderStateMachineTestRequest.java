package com.cloudDemo.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单状态机测试请求DTO
 */
@Data
@Schema(description = "订单状态机测试请求")
public class OrderStateMachineTestRequest {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "商品名称", example = "测试商品")
    private String productName;

    @Schema(description = "订单金额", example = "99.99")
    private BigDecimal amount;

    @Schema(description = "收货地址", example = "北京市朝阳区测试地址123号")
    private String shippingAddress;

    @Schema(description = "支付方式 (ALIPAY/WECHAT/BANK_CARD)", example = "ALIPAY")
    private String paymentType;
}
