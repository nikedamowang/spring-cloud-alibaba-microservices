package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单 - 简化版，不依赖数据库
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestParam Long userId,
                                           @RequestParam String productName,
                                           @RequestParam BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "订单创建成功");
        result.put("orderId", "ORDER-" + System.currentTimeMillis());
        result.put("userId", userId);
        result.put("productName", productName);
        result.put("amount", amount);
        result.put("createTime", LocalDateTime.now());
        return result;
    }

    /**
     * 根据用户ID查询订单 - 简化版
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getOrdersByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("orders", "用户 " + userId + " 的订单列表（演示数据）");
        result.put("count", 0);
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "order-service");
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    /**
     * 获取orders表的所有数据，最多100条
     */
    @GetMapping("/all")
    public Map<String, Object> getAllOrders() {
        List<Orders> orders = orderService.getAllOrders();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("total", orders.size());
        result.put("data", orders);
        return result;
    }
}
