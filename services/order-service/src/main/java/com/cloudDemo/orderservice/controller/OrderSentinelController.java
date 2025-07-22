package com.cloudDemo.orderservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.cloudDemo.orderservice.fallback.OrderServiceFallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务控制器 - 演示Sentinel熔断降级功能
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderSentinelController {

    /**
     * 创建订单 - 带熔断降级
     */
    @PostMapping("/create")
    @SentinelResource(
            value = "createOrder",
            fallback = "createOrderFallback",
            fallbackClass = OrderServiceFallbackHandler.class
    )
    public String createOrder(@RequestBody String orderInfo) {
        log.info("创建订单，订单信息：{}", orderInfo);

        // 模拟订单创建逻辑
        if (orderInfo == null || orderInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单信息不能为空");
        }

        // 模拟创建订单时的随机异常（用于测试熔断）
        if (Math.random() > 0.5) {
            throw new RuntimeException("订单创建服务异常");
        }

        // 模拟响应时间过长
        try {
            Thread.sleep((long) (Math.random() * 3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "订单创建成功，订单号：ORD" + System.currentTimeMillis();
    }

    /**
     * 查询订单 - 带熔断降级
     */
    @GetMapping("/{orderId}")
    @SentinelResource(
            value = "getOrder",
            fallback = "getOrderFallback",
            fallbackClass = OrderServiceFallbackHandler.class
    )
    public String getOrder(@PathVariable Long orderId) {
        log.info("查询订单，订单ID：{}", orderId);

        if (orderId <= 0) {
            throw new IllegalArgumentException("订单ID无效");
        }

        // 模拟查询异常
        if (Math.random() > 0.6) {
            throw new RuntimeException("订单查询服务异常");
        }

        return "订单详情：订单ID=" + orderId + "，状态=已支付，金额=99.99";
    }

    /**
     * 更新订单状态 - 带熔断降级
     */
    @PutMapping("/{orderId}/status")
    @SentinelResource(
            value = "updateOrderStatus",
            fallback = "updateOrderStatusFallback",
            fallbackClass = OrderServiceFallbackHandler.class
    )
    public String updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        log.info("更新订单状态，订单ID：{}，新状态：{}", orderId, status);

        if (orderId <= 0 || status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("参数无效");
        }

        // 模拟状态更新异常
        if (Math.random() > 0.7) {
            throw new RuntimeException("订单状态更新失败");
        }

        return "订单状态更新成功：" + orderId + " -> " + status;
    }
}
