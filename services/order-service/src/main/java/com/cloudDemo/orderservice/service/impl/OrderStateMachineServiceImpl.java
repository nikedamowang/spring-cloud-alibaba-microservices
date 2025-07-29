package com.cloudDemo.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.enums.OrderStatus;
import com.cloudDemo.orderservice.mapper.OrdersMapper;
import com.cloudDemo.orderservice.service.OrderStateMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 订单状态机服务实现类
 * 实现订单状态的安全转换和管理
 */
@Slf4j
@Service
public class OrderStateMachineServiceImpl implements OrderStateMachineService {

    private static final String ORDER_STATUS_CACHE_PREFIX = "order:status:";
    private static final String ORDER_LOCK_PREFIX = "order:lock:";
    private static final int CACHE_EXPIRE_MINUTES = 30;
    private static final int LOCK_EXPIRE_SECONDS = 10;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public Orders payOrder(String orderNo) {
        return changeOrderStatus(orderNo, OrderStatus.PAID, "订单支付成功");
    }

    @Override
    @Transactional
    public Orders shipOrder(String orderNo, String trackingNumber) {
        Orders order = changeOrderStatus(orderNo, OrderStatus.SHIPPED, "订单已发货，物流单号：" + trackingNumber);
        if (order != null) {
            // 可以在这里添加物流信息到订单中
            log.info("订单 {} 已发货，物流单号：{}", orderNo, trackingNumber);
        }
        return order;
    }

    @Override
    @Transactional
    public Orders completeOrder(String orderNo) {
        return changeOrderStatus(orderNo, OrderStatus.COMPLETED, "订单已完成");
    }

    @Override
    @Transactional
    public Orders cancelOrder(String orderNo, String reason) {
        Orders order = changeOrderStatus(orderNo, OrderStatus.CANCELLED, "订单已取消，原因：" + reason);
        if (order != null) {
            log.info("订单 {} 已取消，原因：{}", orderNo, reason);
            // 取消订单时可以处理库存回滚等逻辑
        }
        return order;
    }

    @Override
    public boolean canTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        return currentStatus.canTransitionTo(targetStatus);
    }

    @Override
    public OrderStatus[] getNextPossibleStatuses(String orderNo) {
        Orders order = getOrderByNo(orderNo);
        if (order == null) {
            return new OrderStatus[]{};
        }
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());
        return currentStatus.getNextPossibleStatuses();
    }

    @Override
    @Transactional
    public String handlePaymentCallback(String orderNo, String paymentResult) {
        String lockKey = ORDER_LOCK_PREFIX + orderNo;

        try {
            // 获取分布式锁，防止重复处理
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (!lockAcquired) {
                log.warn("订单 {} 正在处理中，忽略重复支付回调", orderNo);
                return "处理中，请勿重复操作";
            }

            Orders order = getOrderByNo(orderNo);
            if (order == null) {
                return "订单不存在";
            }

            OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

            if ("SUCCESS".equals(paymentResult)) {
                if (currentStatus == OrderStatus.PENDING) {
                    payOrder(orderNo);
                    log.info("支付回调成功，订单 {} 状态已更新为已支付", orderNo);
                    return "支付成功";
                } else {
                    log.warn("订单 {} 当前状态为 {}，无法处理支付成功回调", orderNo, currentStatus.getName());
                    return "订单状态异常，无法处理支付";
                }
            } else {
                if (currentStatus == OrderStatus.PENDING) {
                    cancelOrder(orderNo, "支付失败");
                    log.info("支付回调失败，订单 {} 已自动取消", orderNo);
                    return "支付失败，订单已取消";
                } else {
                    return "订单状态异常，无法处理支付失败";
                }
            }
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 核心方法：安全地改变订单状态
     */
    private Orders changeOrderStatus(String orderNo, OrderStatus targetStatus, String logMessage) {
        Orders order = getOrderByNo(orderNo);
        if (order == null) {
            log.error("订单不存在：{}", orderNo);
            throw new IllegalArgumentException("订单不存在：" + orderNo);
        }

        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        // 检查状态转换是否合法
        if (!currentStatus.canTransitionTo(targetStatus)) {
            log.error("订单 {} 状态转换不合法：{} -> {}", orderNo, currentStatus.getName(), targetStatus.getName());
            throw new IllegalStateException(
                    String.format("订单状态转换不合法：%s -> %s", currentStatus.getName(), targetStatus.getName())
            );
        }

        // 更新数据库
        UpdateWrapper<Orders> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_no", orderNo)
                .eq("status", currentStatus.getCode()) // 乐观锁，确保状态一致性
                .set("status", targetStatus.getCode());

        int updateCount = ordersMapper.update(null, updateWrapper);
        if (updateCount == 0) {
            log.error("订单 {} 状态更新失败，可能存在并发修改", orderNo);
            throw new IllegalStateException("订单状态更新失败，请重试");
        }

        // 更新缓存
        order.setStatus(targetStatus.getCode());
        updateOrderCache(order);

        log.info("订单 {} 状态更新成功：{} -> {}，{}",
                orderNo, currentStatus.getName(), targetStatus.getName(), logMessage);

        return order;
    }

    /**
     * 根据订单号获取订单（先查缓存，再查数据库）
     */
    private Orders getOrderByNo(String orderNo) {
        String cacheKey = ORDER_STATUS_CACHE_PREFIX + orderNo;

        // 先查缓存
        Orders cachedOrder = (Orders) redisTemplate.opsForValue().get(cacheKey);
        if (cachedOrder != null) {
            return cachedOrder;
        }

        // 查数据库
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        Orders order = ordersMapper.selectOne(queryWrapper);

        // 更新缓存
        if (order != null) {
            updateOrderCache(order);
        }

        return order;
    }

    /**
     * 更新订单缓存
     */
    private void updateOrderCache(Orders order) {
        String cacheKey = ORDER_STATUS_CACHE_PREFIX + order.getOrderNo();
        redisTemplate.opsForValue().set(cacheKey, order, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
}
