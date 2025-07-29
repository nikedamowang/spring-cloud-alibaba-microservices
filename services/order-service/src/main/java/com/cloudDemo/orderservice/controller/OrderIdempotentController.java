package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.service.OrderIdempotentService;
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
 * 订单幂等性控制器
 * 提供订单幂等性处理的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/order/idempotent")
@Tag(name = "订单幂等性", description = "防止重复下单的幂等性处理")
public class OrderIdempotentController {

    @Autowired
    private OrderIdempotentService orderIdempotentService;

    @PostMapping("/token/{userId}")
    @Operation(summary = "获取幂等令牌", description = "为用户生成订单幂等性令牌，用于防止重复下单")
    public ResponseEntity<Map<String, Object>> generateIdempotentToken(
            @Parameter(description = "用户ID") @PathVariable Long userId) {

        Map<String, Object> result = new HashMap<>();
        try {
            String token = orderIdempotentService.generateIdempotentToken(userId);
            result.put("success", true);
            result.put("message", "幂等令牌生成成功");
            result.put("userId", userId);
            result.put("idempotentToken", token);
            result.put("expireMinutes", 10);
            result.put("usage", "请在下单时将此令牌包含在请求中，确保订单创建的幂等性");

            log.info("为用户 {} 生成幂等令牌成功", userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("生成幂等令牌失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "生成幂等令牌失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/clean")
    @Operation(summary = "清理过期记录", description = "手动清理过期的幂等性记录（通常由定时任务执行）")
    public ResponseEntity<Map<String, Object>> cleanExpiredRecords() {
        Map<String, Object> result = new HashMap<>();
        try {
            orderIdempotentService.cleanExpiredIdempotentRecords();
            result.put("success", true);
            result.put("message", "过期幂等记录清理完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("清理过期记录失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "清理失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/demo")
    @Operation(summary = "幂等性演示说明", description = "获取幂等性功能的使用说明和演示流程")
    public ResponseEntity<Map<String, Object>> getIdempotentDemo() {
        Map<String, Object> result = new HashMap<>();

        Map<String, String> demoSteps = new HashMap<>();
        demoSteps.put("1. 获取令牌", "POST /api/order/idempotent/token/{userId}");
        demoSteps.put("2. 创建订单", "POST /api/order/create （带上idempotentToken）");
        demoSteps.put("3. 重复请求", "再次发送相同请求，会返回已存在的订单号");
        demoSteps.put("4. 令牌失效", "令牌使用后自动失效，10分钟后过期");

        Map<String, Object> exampleRequest = new HashMap<>();
        exampleRequest.put("userId", 1001);
        exampleRequest.put("productId", "PROD-001");
        exampleRequest.put("amount", "99.99");
        exampleRequest.put("idempotentToken", "从步骤1获取的令牌");

        result.put("success", true);
        result.put("description", "订单幂等性处理演示");
        result.put("demoSteps", demoSteps);
        result.put("exampleRequest", exampleRequest);
        result.put("technicalFeatures", new String[]{
                "基于Redis的分布式幂等性控制",
                "MD5哈希生成幂等键",
                "令牌有效期管理",
                "自动清理过期记录",
                "防止重复下单攻击"
        });

        return ResponseEntity.ok(result);
    }
}
