package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.dto.OrderCancelRequest;
import com.cloudDemo.orderservice.dto.OrderShipRequest;
import com.cloudDemo.orderservice.dto.PaymentCallbackRequest;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.enums.OrderStatus;
import com.cloudDemo.orderservice.service.OrderStateMachineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态机控制器
 * 提供订单状态转换的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/order/state")
@Tag(name = "订单状态机", description = "订单状态转换管理")
public class OrderStateMachineController {

    @Autowired
    private OrderStateMachineService orderStateMachineService;

    @PostMapping("/pay/{orderNo}")
    @Operation(summary = "支付订单", description = "将订单状态从待支付转换为已支付")
    public ResponseEntity<Map<String, Object>> payOrder(
            @Parameter(description = "订单号") @PathVariable String orderNo) {

        Map<String, Object> result = new HashMap<>();
        try {
            Orders order = orderStateMachineService.payOrder(orderNo);
            result.put("success", true);
            result.put("message", "订单支付成功");
            result.put("order", order);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("订单支付失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/ship/{orderNo}")
    @Operation(summary = "发货", description = "将订单状态从已支付转换为已发货")
    public ResponseEntity<Map<String, Object>> shipOrder(
            @Parameter(description = "订单号") @PathVariable String orderNo,
            @RequestBody(required = false) OrderShipRequest request) {

        Map<String, Object> result = new HashMap<>();
        try {
            String trackingNumber = null;
            if (request != null && request.getTrackingNumber() != null && !request.getTrackingNumber().trim().isEmpty()) {
                trackingNumber = request.getTrackingNumber();
            } else {
                // 如果没有提供物流单号，自动生成一个
                trackingNumber = "TRK" + System.currentTimeMillis();
            }

            Orders order = orderStateMachineService.shipOrder(orderNo, trackingNumber);
            result.put("success", true);
            result.put("message", "订单发货成功");
            result.put("order", order);
            result.put("trackingNumber", trackingNumber);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("订单发货失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/complete/{orderNo}")
    @Operation(summary = "完成订单", description = "将订单状态从已发货转换为已完成")
    public ResponseEntity<Map<String, Object>> completeOrder(
            @Parameter(description = "订单号") @PathVariable String orderNo) {

        Map<String, Object> result = new HashMap<>();
        try {
            Orders order = orderStateMachineService.completeOrder(orderNo);
            result.put("success", true);
            result.put("message", "订单完成成功");
            result.put("order", order);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("订单完成失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/cancel/{orderNo}")
    @Operation(summary = "取消订单", description = "取消订单（任何状态都可以取消，除了已完成）")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @Parameter(description = "订单号") @PathVariable String orderNo,
            @RequestBody(required = false) OrderCancelRequest request) {

        Map<String, Object> result = new HashMap<>();
        try {
            String reason = "用户主动取消";
            if (request != null && request.getReason() != null && !request.getReason().trim().isEmpty()) {
                reason = request.getReason();
            }

            Orders order = orderStateMachineService.cancelOrder(orderNo, reason);
            result.put("success", true);
            result.put("message", "订单取消成功");
            result.put("order", order);
            result.put("reason", reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("订单取消失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/next-statuses/{orderNo}")
    @Operation(summary = "获取可转换状态", description = "获取订单的下一步可能的状态")
    public ResponseEntity<Map<String, Object>> getNextPossibleStatuses(
            @Parameter(description = "订单号") @PathVariable String orderNo) {

        Map<String, Object> result = new HashMap<>();
        try {
            OrderStatus[] nextStatuses = orderStateMachineService.getNextPossibleStatuses(orderNo);
            result.put("success", true);
            result.put("orderNo", orderNo);
            result.put("nextPossibleStatuses", nextStatuses);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取可转换状态失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/payment-callback")
    @Operation(summary = "支付回调", description = "模拟异步支付回调处理")
    public ResponseEntity<Map<String, Object>> handlePaymentCallback(@RequestBody PaymentCallbackRequest request) {

        Map<String, Object> result = new HashMap<>();
        try {
            String message = orderStateMachineService.handlePaymentCallback(request.getOrderNo(), request.getPaymentResult());
            result.put("success", true);
            result.put("message", message);
            result.put("orderNo", request.getOrderNo());
            result.put("paymentResult", request.getPaymentResult());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("支付回调处理失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/status-info")
    @Operation(summary = "获取状态信息", description = "获取所有订单状态的详细信息")
    public ResponseEntity<Map<String, Object>> getStatusInfo() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> statusInfo = new HashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            Map<String, Object> info = new HashMap<>();
            info.put("code", status.getCode());
            info.put("name", status.getName());
            info.put("description", status.getDescription());
            info.put("nextPossibleStatuses", status.getNextPossibleStatuses());
            statusInfo.put(status.getCode(), info);
        }

        result.put("success", true);
        result.put("statusInfo", statusInfo);
        return ResponseEntity.ok(result);
    }
}
