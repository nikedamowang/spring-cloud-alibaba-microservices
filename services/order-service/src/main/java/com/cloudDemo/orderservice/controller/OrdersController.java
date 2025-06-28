package com.cloudDemo.orderservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.mapper.OrdersMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrdersController {
    private final OrdersMapper ordersMapper;

    public OrdersController(OrdersMapper ordersMapper) {
        this.ordersMapper = ordersMapper;
    }

    @GetMapping("/list")
    public List<Orders> list() {
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.last("limit 100");
        return ordersMapper.selectList(wrapper);
    }
}
