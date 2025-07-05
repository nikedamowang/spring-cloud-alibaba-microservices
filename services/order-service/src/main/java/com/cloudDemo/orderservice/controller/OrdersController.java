package com.cloudDemo.orderservice.controller;

import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.service.OrderService;
import com.cloudDemo.orderservice.service.UserRemoteService;
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

    @Autowired
    private UserRemoteService userRemoteService;

    /**
     * 创建订单 - 简化版，不依赖数据库
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestParam Long userId,
                                           @RequestParam String productName,
                                           @RequestParam BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();

        // 远程调用用户服务验证用户是否存在
        if (!userRemoteService.userExists(userId)) {
            result.put("success", false);
            result.put("message", "用户不存在，无法创建订单");
            return result;
        }

        // 获取用户信息
        UserDTO user = userRemoteService.getUserById(userId);

        result.put("success", true);
        result.put("message", "订单创建成功");
        result.put("orderId", "ORDER-" + System.currentTimeMillis());
        result.put("userId", userId);
        result.put("userName", user != null ? user.getUsername() : "未知用户");
        result.put("userEmail", user != null ? user.getEmail() : "");
        result.put("productName", productName);
        result.put("amount", amount);
        result.put("createTime", LocalDateTime.now());
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

    /**
     * 测试远程调用用户服务的接口
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> testUserRemoteCall(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 测试获取用户信息
            UserDTO user = userRemoteService.getUserById(userId);
            boolean exists = userRemoteService.userExists(userId);

            result.put("success", true);
            result.put("message", "远程调用成功");
            result.put("userExists", exists);
            result.put("userInfo", user);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "远程调用失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取所有用户信息（演示远程调用）
     */
    @GetMapping("/test/users")
    public Map<String, Object> testGetAllUsers() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<UserDTO> users = userRemoteService.getAllUsers();
            result.put("success", true);
            result.put("message", "获取用户列表成功");
            result.put("users", users);
            result.put("count", users != null ? users.size() : 0);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "远程调用失败: " + e.getMessage());
        }

        return result;
    }
}
