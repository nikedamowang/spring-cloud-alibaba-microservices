package com.cloudDemo.orderservice.dto.validation;

import com.cloudDemo.orderservice.validation.group.UpdateOrderGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新订单请求DTO
 * 专门用于订单更新场景的参数校验
 */
@Data
@Schema(description = "更新订单请求")
public class UpdateOrderRequest {

    @NotNull(message = "订单ID不能为空", groups = UpdateOrderGroup.class)
    @Positive(message = "订单ID必须是正整数", groups = UpdateOrderGroup.class)
    @Schema(description = "订单ID", example = "1")
    private Long id;

    @DecimalMin(value = "0.01", message = "订单总金额必须大于0.01", groups = UpdateOrderGroup.class)
    @DecimalMax(value = "999999.99", message = "订单总金额不能超过999999.99", groups = UpdateOrderGroup.class)
    @Digits(integer = 6, fraction = 2, message = "订单总金额格式不正确", groups = UpdateOrderGroup.class)
    @Schema(description = "订单总金额", example = "299.99")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.01", message = "实际支付金额必须大于0.01", groups = UpdateOrderGroup.class)
    @DecimalMax(value = "999999.99", message = "实际支付金额不能超过999999.99", groups = UpdateOrderGroup.class)
    @Digits(integer = 6, fraction = 2, message = "实际支付金额格式不正确", groups = UpdateOrderGroup.class)
    @Schema(description = "实际支付金额", example = "259.99")
    private BigDecimal paymentAmount;

    @Pattern(regexp = "^(ALIPAY|WECHAT|BANK_CARD)$", message = "支付方式只能是ALIPAY、WECHAT或BANK_CARD", groups = UpdateOrderGroup.class)
    @Schema(description = "支付方式", example = "ALIPAY")
    private String paymentType;

    @Pattern(regexp = "^(PENDING|PAID|SHIPPED|DELIVERED|CANCELLED)$", message = "订单状态只能是PENDING、PAID、SHIPPED、DELIVERED或CANCELLED", groups = UpdateOrderGroup.class)
    @Schema(description = "订单状态", example = "PENDING")
    private String status;

    @Size(max = 200, message = "收货地址长度不能超过200个字符", groups = UpdateOrderGroup.class)
    @Schema(description = "收货地址", example = "北京市朝阳区某某街道123号")
    private String shippingAddress;
}
