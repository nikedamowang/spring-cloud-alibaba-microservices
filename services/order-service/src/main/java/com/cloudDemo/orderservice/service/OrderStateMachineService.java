package com.cloudDemo.orderservice.service;

import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.enums.OrderStatus;

/**
 * 订单状态机服务接口
 * 负责管理订单状态的转换和验证
 */
public interface OrderStateMachineService {

    /**
     * 支付订单 - 将状态从PENDING转换为PAID
     *
     * @param orderNo 订单号
     * @return 更新后的订单
     */
    Orders payOrder(String orderNo);

    /**
     * 发货 - 将状态从PAID转换为SHIPPED
     *
     * @param orderNo        订单号
     * @param trackingNumber 物流单号
     * @return 更新后的订单
     */
    Orders shipOrder(String orderNo, String trackingNumber);

    /**
     * 确认收货/完成订单 - 将状态从SHIPPED转换为COMPLETED
     *
     * @param orderNo 订单号
     * @return 更新后的订单
     */
    Orders completeOrder(String orderNo);

    /**
     * 取消订单 - 将订单状态转换为CANCELLED
     *
     * @param orderNo 订单号
     * @param reason  取消原因
     * @return 更新后的订单
     */
    Orders cancelOrder(String orderNo, String reason);

    /**
     * 检查订单状态转换是否合法
     *
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @return 是否可以转换
     */
    boolean canTransition(OrderStatus currentStatus, OrderStatus targetStatus);

    /**
     * 获取订单的所有可能的下一步状态
     *
     * @param orderNo 订单号
     * @return 可能的下一步状态数组
     */
    OrderStatus[] getNextPossibleStatuses(String orderNo);

    /**
     * 模拟异步支付回调处理
     *
     * @param orderNo       订单号
     * @param paymentResult 支付结果 (SUCCESS/FAILED)
     * @return 处理结果
     */
    String handlePaymentCallback(String orderNo, String paymentResult);
}
