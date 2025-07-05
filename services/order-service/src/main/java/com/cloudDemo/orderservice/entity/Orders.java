package com.cloudDemo.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Integer userId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    @TableField("payment_type")
    private String paymentType;

    private String status;

    @TableField("shipping_address")
    private String shippingAddress;

    @TableField("create_time")
    private LocalDateTime createTime;
}
