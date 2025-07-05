package com.cloudDemo.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.mapper.OrdersMapper;
import com.cloudDemo.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public List<Orders> getOrdersByUserId(Long userId) {
        return ordersMapper.selectList(
                new QueryWrapper<Orders>().eq("user_id", userId)
        );
    }

    @Override
    public Orders createOrder(Orders order) {
        if (order.getCreateTime() == null) {
            order.setCreateTime(LocalDateTime.now());
        }
        ordersMapper.insert(order);
        return order;
    }

    @Override
    public String createOrder(Long userId, String productName, BigDecimal amount) {
        // 创建订单
        Orders order = new Orders();
        order.setUserId(userId.intValue()); // 根据数据库表结构，userId是Integer类型
        order.setOrderNo("ORDER-" + System.currentTimeMillis());
        order.setTotalAmount(amount);
        order.setPaymentAmount(amount);
        order.setPaymentType("wechat"); // 使用数据库枚举值
        order.setStatus("pending"); // 使用数据库枚举值
        order.setShippingAddress("默认收货地址");
        order.setCreateTime(LocalDateTime.now());

        ordersMapper.insert(order);

        return "订单创建成功，订单号：" + order.getOrderNo() + "，用户ID：" + userId + "，商品：" + productName + "，金额：" + amount;
    }

    @Override
    public String getUserOrderInfo(Long userId) {
        // 查询用户的订单
        List<Orders> orders = getOrdersByUserId(userId);
        return "用户ID：" + userId + " 共有 " + orders.size() + " 个订单";
    }

    @Override
    public List<Orders> getAllOrders() {
        // 使用MyBatis Plus的分页查询，限制最多返回100条记录
        return ordersMapper.selectList(
                new QueryWrapper<Orders>()
                        .orderByDesc("create_time")  // 按创建时间倒序排列
                        .last("LIMIT 100")           // 限制最多100条
        );
    }
}
