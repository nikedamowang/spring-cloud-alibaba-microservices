package com.cloudDemo.orderservice.dto.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 简化创建订单请求DTO
 * 用于测试基础参数校验功能，不使用分组校验
 */
@Data
@Schema(description = "简化创建订单请求")
public class SimpleCreateOrderRequest {

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须是正整数")
    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    @NotNull(message = "订单总金额不能为空")
    @DecimalMin(value = "0.01", message = "订单总金额必须大于0.01")
    @DecimalMax(value = "999999.99", message = "订单总金额不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "订单总金额格式不正确")
    @Schema(description = "订单总金额", example = "299.99")
    private BigDecimal totalAmount;

    @NotNull(message = "实际支付金额不能为空")
    @DecimalMin(value = "0.01", message = "实际支付金额必须大于0.01")
    @DecimalMax(value = "999999.99", message = "实际支付金额不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "实际支付金额格式不正确")
    @Schema(description = "实际支付金额", example = "259.99")
    private BigDecimal paymentAmount;

    @NotBlank(message = "支付方式不能为空")
    @Pattern(regexp = "^(ALIPAY|WECHAT|BANK_CARD)$", message = "支付方式只能是ALIPAY、WECHAT或BANK_CARD")
    @Schema(description = "支付方式", example = "ALIPAY")
    private String paymentType;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址长度不能超过200个字符")
    @Schema(description = "收货地址", example = "北京市朝阳区某某街道123号")
    private String shippingAddress;
}
