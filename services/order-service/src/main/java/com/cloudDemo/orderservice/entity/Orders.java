package com.cloudDemo.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
@Schema(description = "订单实体")
public class Orders {
    @TableId(type = IdType.AUTO)
    @Schema(description = "订单ID", example = "1")
    private Long id;

    @NotBlank(message = "订单编号不能为空")
    @Size(max = 32, message = "订单编号长度不能超过32个字符")
    @Pattern(regexp = "^ORDER-\\d{11}$", message = "订单编号格式不正确，应为ORDER-开头的11位数字")
    @TableField("order_no")
    @Schema(description = "订单编号", example = "ORDER-20250722001")
    private String orderNo;

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须是正整数")
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    @NotNull(message = "订单总金额不能为空")
    @DecimalMin(value = "0.01", message = "订单总金额必须大于0.01")
    @DecimalMax(value = "999999.99", message = "订单总金额不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "订单总金额格式不正确")
    @TableField("total_amount")
    @Schema(description = "订单总金额", example = "299.99")
    private BigDecimal totalAmount;

    @NotNull(message = "实际支付金额不能为空")
    @DecimalMin(value = "0.01", message = "实际支付金额必须大于0.01")
    @DecimalMax(value = "999999.99", message = "实际支付金额不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "实际支付金额格式不正确")
    @TableField("payment_amount")
    @Schema(description = "实际支付金额", example = "259.99")
    private BigDecimal paymentAmount;

    @Pattern(regexp = "^(ALIPAY|WECHAT|BANK_CARD)$", message = "支付方式只能是ALIPAY、WECHAT或BANK_CARD")
    @TableField("payment_type")
    @Schema(description = "支付方式", example = "ALIPAY", allowableValues = {"ALIPAY", "WECHAT", "BANK_CARD"})
    private String paymentType;

    @Pattern(regexp = "^(PENDING|PAID|SHIPPED|DELIVERED|CANCELLED)$", message = "订单状态只能是PENDING、PAID、SHIPPED、DELIVERED或CANCELLED")
    @Schema(description = "订单状态", example = "PENDING", allowableValues = {"PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"})
    private String status;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址长度不能超过200个字符")
    @TableField("shipping_address")
    @Schema(description = "收货地址", example = "北京市朝阳区某某街道123号")
    private String shippingAddress;

    @TableField("create_time")
    @Schema(description = "创建时间", example = "2025-07-22T10:30:00")
    private LocalDateTime createTime;
}
