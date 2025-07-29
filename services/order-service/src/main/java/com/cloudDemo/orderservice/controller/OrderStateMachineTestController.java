package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.dto.OrderStateMachineTestRequest;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.enums.OrderStatus;
import com.cloudDemo.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态机测试控制器
 * 用于测试订单状态机的完整流程
 */
@Slf4j
@RestController
@RequestMapping("/api/order/test")
@Tag(name = "订单状态机测试", description = "测试订单状态机完整流程")
public class OrderStateMachineTestController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create-test-order")
    @Operation(summary = "创建测试订单", description = "创建一个用于测试状态机的订单")
    public ResponseEntity<Map<String, Object>> createTestOrder(@RequestBody OrderStateMachineTestRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 生成订单号
            String orderNo = "ORDER-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-" + System.currentTimeMillis() % 1000;

            // 创建订单对象
            Orders order = new Orders();
            order.setOrderNo(orderNo);
            order.setUserId(request.getUserId().intValue());
            order.setTotalAmount(request.getAmount());
            order.setPaymentAmount(request.getAmount());
            order.setPaymentType(request.getPaymentType());
            order.setStatus(OrderStatus.PENDING.getCode()); // 初始状态为待支付
            order.setShippingAddress(request.getShippingAddress());
            order.setCreateTime(LocalDateTime.now());

            // 保存订单
            Orders savedOrder = orderService.createOrder(order);

            result.put("success", true);
            result.put("message", "测试订单创建成功");
            result.put("order", savedOrder);
            result.put("orderNo", orderNo);
            result.put("currentStatus", OrderStatus.PENDING);
            result.put("nextPossibleActions", new String[]{"支付订单", "取消订单"});

            log.info("创建测试订单成功：{}", orderNo);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("创建测试订单失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "创建订单失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/simulate-full-flow")
    @Operation(summary = "模拟完整订单流程", description = "自动模拟订单从创建到完成的完整流程")
    public ResponseEntity<Map<String, Object>> simulateFullOrderFlow(@RequestBody OrderStateMachineTestRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 步骤1：创建订单
            String orderNo = "ORDER-FLOW-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-" + System.currentTimeMillis() % 1000;

            Orders order = new Orders();
            order.setOrderNo(orderNo);
            order.setUserId(request.getUserId().intValue());
            order.setTotalAmount(request.getAmount());
            order.setPaymentAmount(request.getAmount());
            order.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : "ALIPAY");
            order.setStatus(OrderStatus.PENDING.getCode());
            order.setShippingAddress(request.getShippingAddress() != null ? request.getShippingAddress() : "默认测试地址");
            order.setCreateTime(LocalDateTime.now());

            Orders savedOrder = orderService.createOrder(order);

            Map<String, Object> flowSteps = new HashMap<>();
            flowSteps.put("step1_create", "订单创建成功，状态：" + OrderStatus.PENDING.getName());

            result.put("success", true);
            result.put("message", "订单流程模拟完成");
            result.put("orderNo", orderNo);
            result.put("initialOrder", savedOrder);
            result.put("flowSteps", flowSteps);
            result.put("instructions", "订单已创建，请使用以下API继续测试：\n" +
                    "1. POST /api/order/state/pay/" + orderNo + " - 支付订单\n" +
                    "2. POST /api/order/state/ship/" + orderNo + " - 发货\n" +
                    "3. POST /api/order/state/complete/" + orderNo + " - 完成订单\n" +
                    "或者使用 POST /api/order/state/cancel/" + orderNo + " - 取消订单");

            log.info("订单流程模拟启动成功：{}", orderNo);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("订单流程模拟失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "流程模拟失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/demo-data")
    @Operation(summary = "获取演示数据", description = "获取用于测试的示例数据")
    public ResponseEntity<Map<String, Object>> getDemoData() {
        Map<String, Object> result = new HashMap<>();

        // 示例请求数据
        OrderStateMachineTestRequest demoRequest = new OrderStateMachineTestRequest();
        demoRequest.setUserId(1001L);
        demoRequest.setProductName("测试商品-状态机演示");
        demoRequest.setAmount(new BigDecimal("99.99"));
        demoRequest.setShippingAddress("北京市朝阳区测试大街123号");
        demoRequest.setPaymentType("ALIPAY");

        // API测试说明
        Map<String, String> apiGuide = new HashMap<>();
        apiGuide.put("1. 创建订单", "POST /api/order/test/create-test-order");
        apiGuide.put("2. 支付订单", "POST /api/order/state/pay/{orderNo}");
        apiGuide.put("3. 发货", "POST /api/order/state/ship/{orderNo}?trackingNumber=TRK123456");
        apiGuide.put("4. 完成订单", "POST /api/order/state/complete/{orderNo}");
        apiGuide.put("5. 取消订单", "POST /api/order/state/cancel/{orderNo}?reason=测试取消");
        apiGuide.put("6. 支付回调", "POST /api/order/state/payment-callback?orderNo={orderNo}&paymentResult=SUCCESS");
        apiGuide.put("7. 查看状态", "GET /api/order/state/next-statuses/{orderNo}");

        result.put("success", true);
        result.put("demoRequest", demoRequest);
        result.put("apiGuide", apiGuide);
        result.put("orderStatuses", OrderStatus.values());

        return ResponseEntity.ok(result);
    }
}
