package com.cloudDemo.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @TableField("order_no")
    @Schema(description = "订单编号", example = "ORDER-20250722001")
    private String orderNo;

    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    @TableField("total_amount")
    @Schema(description = "订单总金额", example = "299.99")
    private BigDecimal totalAmount;

    @TableField("payment_amount")
    @Schema(description = "实际支付金额", example = "259.99")
    private BigDecimal paymentAmount;

    @TableField("payment_type")
    @Schema(description = "支付方式", example = "ALIPAY", allowableValues = {"ALIPAY", "WECHAT", "BANK_CARD"})
    private String paymentType;

    @Schema(description = "订单状态", example = "PENDING", allowableValues = {"PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"})
    private String status;

    @TableField("shipping_address")
    @Schema(description = "收货地址", example = "北京市朝阳区某某街道123号")
    private String shippingAddress;

    @TableField("create_time")
    @Schema(description = "创建时间", example = "2025-07-22T10:30:00")
    private LocalDateTime createTime;
}
