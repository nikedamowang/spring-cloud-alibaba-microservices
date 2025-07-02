package com.cloudDemo.orderservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.api.dto.Result;
import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.api.service.UserService;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.mapper.OrdersMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrdersController {
    private final OrdersMapper ordersMapper;

    // 远程引用用户服务
    @DubboReference(version = "1.0.0")
    private UserService userService;

    public OrdersController(OrdersMapper ordersMapper) {
        this.ordersMapper = ordersMapper;
    }

    @GetMapping("/list")
    public List<Orders> list() {
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.last("limit 100");
        return ordersMapper.selectList(wrapper);
    }

    /**
     * 获取用户订单信息 - 演示远程调用用户服务
     * 示例：GET /order/user/1
     */
    @GetMapping("/user/{userId}")
    public Result<String> getUserOrderInfo(@PathVariable Long userId) {
        try {
            // 远程调用用户服务获取用户信息
            UserDTO user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 查询用户的订单
            QueryWrapper<Orders> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            List<Orders> orders = ordersMapper.selectList(wrapper);

            String result = "用户：" + user.getUsername() + "（" + user.getEmail() + "）共有 " + orders.size() + " 个订单";
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户订单信息失败：" + e.getMessage());
        }
    }

    /**
     * 验证用户是否存在 - 演示远程调用用户服务
     * 示例：GET /order/check-user/1
     */
    @GetMapping("/check-user/{userId}")
    public Result<String> checkUser(@PathVariable Long userId) {
        try {
            // 远程调用用户服务检查用户是否存在
            boolean exists = userService.userExists(userId);
            String message = exists ? "用户存在" : "用户不存在";
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("检查用户失败：" + e.getMessage());
        }
    }
}
