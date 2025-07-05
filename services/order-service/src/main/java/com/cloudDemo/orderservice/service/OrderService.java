package com.cloudDemo.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudDemo.orderservice.entity.Orders;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService extends IService<Orders> {
    /**
     * 根据用户ID查询订单
     */
    List<Orders> getOrdersByUserId(Long userId);

    /**
     * 创建订单
     */
    Orders createOrder(Orders order);

    /**
     * 创建订单 - 会调用用户服务验证用户是否存在
     */
    String createOrder(Long userId, String productName, BigDecimal amount);

    /**
     * 获取用户的所有订单信息
     */
    String getUserOrderInfo(Long userId);

    /**
     * 获取orders表的数据，最多100条
     */
    List<Orders> getAllOrders();
}
