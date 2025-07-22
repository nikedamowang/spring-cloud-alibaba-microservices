package com.cloudDemo.orderservice.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单服务降级处理器
 */
@Slf4j
@Component
public class OrderServiceFallbackHandler {

    /**
     * 创建订单降级方法
     */
    public static String createOrderFallback(Object orderInfo, Throwable ex) {
        log.warn("创建订单服务降级，异常：{}", ex.getMessage());
        return "订单创建服务暂时不可用，请稍后重试";
    }

    /**
     * 查询订单降级方法
     */
    public static String getOrderFallback(Long orderId, Throwable ex) {
        log.warn("查询订单服务降级，订单ID：{}，异常：{}", orderId, ex.getMessage());
        return "订单查询服务暂时不可用，请稍后重试";
    }

    /**
     * 更新订单状态降级方法
     */
    public static String updateOrderStatusFallback(Long orderId, String status, Throwable ex) {
        log.warn("更新订单状态服务降级，订单ID：{}，状态：{}，异常：{}", orderId, status, ex.getMessage());
        return "订单状态更新服务暂时不可用，请稍后重试";
    }

    /**
     * 通用服务降级方法
     */
    public static String commonFallback(Throwable ex) {
        log.warn("订单服务发生降级，异常：{}", ex.getMessage());
        return "订单服务暂时不可用，请稍后重试";
    }
}
