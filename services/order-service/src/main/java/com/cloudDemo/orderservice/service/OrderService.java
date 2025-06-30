package com.cloudDemo.orderservice.service;

import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.api.service.UserService;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.mapper.OrdersMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务类 - 演示远程调用用户服务
 */
@Service
public class OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    // 远程引用用户服务
    @DubboReference(version = "1.0.0")
    private UserService userService;

    /**
     * 创建订单 - 会调用用户服务验证用户是否存在
     */
    public String createOrder(Long userId, String productName, BigDecimal amount) {
        // 远程调用用户服务，检查用户是否存在
        boolean userExists = userService.userExists(userId);
        if (!userExists) {
            return "订单创建失败：用户不存在";
        }

        // 远程调用用户服务，获取用户信息
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            return "订单创建失败：获取用户信息失败";
        }

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo("ORDER-" + System.currentTimeMillis()); // 生成订单号
        order.setUserId(userId.intValue()); // 你的Orders实体类中userId是Integer类型
        order.setTotalAmount(amount); // 使用你的Orders实体类中的totalAmount字段
        order.setPaymentAmount(amount); // 支付金额等于总金额
        order.setPaymentType("ONLINE"); // 默认在线支付
        order.setStatus("CREATED");
        order.setShippingAddress("默认地址"); // 默认收货地址
        order.setCreateTime(LocalDateTime.now());

        ordersMapper.insert(order);

        return "订单创建成功，用户：" + user.getUsername() + "，商品：" + productName + "，金额：" + amount;
    }

    /**
     * 获取用户的所有订单 - 会调用用户服务获取用户信息
     */
    public String getUserOrderInfo(Long userId) {
        // 远程调用用户服务获取用户信息
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            return "用户不存在";
        }

        // 查询用户的订单
        List<Orders> orders = ordersMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Orders>()
                .eq("user_id", userId)
        );

        return "用户：" + user.getUsername() + "（" + user.getEmail() + "）共有 " + orders.size() + " 个订单";
    }
}
