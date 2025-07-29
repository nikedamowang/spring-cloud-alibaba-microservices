package com.cloudDemo.orderservice.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 * 状态流转: PENDING -> PAID -> SHIPPED -> COMPLETED
 * 任何状态都可以转换为 CANCELLED
 */
@Getter
public enum OrderStatus {
    PENDING("PENDING", "待支付", "订单已创建，等待支付"),
    PAID("PAID", "已支付", "订单已支付，等待发货"),
    SHIPPED("SHIPPED", "已发货", "订单已发货，等待收货"),
    COMPLETED("COMPLETED", "已完成", "订单已完成"),
    CANCELLED("CANCELLED", "已取消", "订单已取消");

    private final String code;
    private final String name;
    private final String description;

    OrderStatus(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据状态码获取订单状态
     */
    public static OrderStatus fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Order status code cannot be null");
        }

        for (OrderStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status code: " + code);
    }

    /**
     * 检查是否可以转换到目标状态
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        // 任何状态都可以取消
        if (targetStatus == CANCELLED) {
            return this != CANCELLED && this != COMPLETED;
        }

        // 正常状态流转
        switch (this) {
            case PENDING:
                return targetStatus == PAID;
            case PAID:
                return targetStatus == SHIPPED;
            case SHIPPED:
                return targetStatus == COMPLETED;
            case COMPLETED:
            case CANCELLED:
                return false; // 完成或取消的订单不能再转换
            default:
                return false;
        }
    }

    /**
     * 获取下一个可能的状态
     */
    public OrderStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new OrderStatus[]{PAID, CANCELLED};
            case PAID:
                return new OrderStatus[]{SHIPPED, CANCELLED};
            case SHIPPED:
                return new OrderStatus[]{COMPLETED, CANCELLED};
            case COMPLETED:
            case CANCELLED:
                return new OrderStatus[]{};
            default:
                return new OrderStatus[]{};
        }
    }
}
