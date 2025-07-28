package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.dto.InventoryInitializeRequest;
import com.cloudDemo.orderservice.dto.InventoryOperationRequest;
import com.cloudDemo.orderservice.dto.StressTestRequest;
import com.cloudDemo.orderservice.entity.ProductInventory;
import com.cloudDemo.orderservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "库存管理", description = "基于Redis分布式锁的商品库存管理")
@Slf4j
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/initialize")
    @Operation(summary = "初始化商品库存", description = "为新商品初始化库存信息")
    public Map<String, Object> initializeInventory(@Valid @RequestBody InventoryInitializeRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProductInventory inventory = inventoryService.initializeInventory(
                    request.getProductId(),
                    request.getProductName(),
                    request.getTotalStock()
            );
            result.put("success", true);
            result.put("message", "库存初始化成功");
            result.put("data", inventory);
            log.info("库存初始化成功: {}", request.getProductId());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "库存初始化失败: " + e.getMessage());
            log.error("库存初始化失败: {}", request.getProductId(), e);
        }
        return result;
    }

    @PostMapping("/reserve")
    @Operation(summary = "预扣库存", description = "使用分布式锁进行库存预扣，防止超卖")
    public Map<String, Object> reserveStock(@Valid @RequestBody InventoryOperationRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = inventoryService.reserveStock(request.getProductId(), request.getQuantity());
            result.put("success", success);
            result.put("message", success ? "库存预扣成功" : "库存预扣失败");
            log.info("库存预扣操作: 商品={}, 数量={}, 结果={}", request.getProductId(), request.getQuantity(), success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "库存预扣失败: " + e.getMessage());
            log.error("库存预扣失败: 商品={}, 数量={}", request.getProductId(), request.getQuantity(), e);
        }
        return result;
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认库存扣减", description = "支付成功后确认扣减库存")
    public Map<String, Object> confirmStockReduction(@Valid @RequestBody InventoryOperationRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = inventoryService.confirmStockReduction(request.getProductId(), request.getQuantity());
            result.put("success", success);
            result.put("message", success ? "库存确认扣减成功" : "库存确认扣减失败");
            log.info("库存确认扣减: 商品={}, 数量={}, 结果={}", request.getProductId(), request.getQuantity(), success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "库存确认扣减失败: " + e.getMessage());
            log.error("库存确认扣减失败: 商品={}, 数量={}", request.getProductId(), request.getQuantity(), e);
        }
        return result;
    }

    @PostMapping("/release")
    @Operation(summary = "释放预扣库存", description = "订单取消时释放预扣的库存")
    public Map<String, Object> releaseStock(@Valid @RequestBody InventoryOperationRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = inventoryService.releaseStock(request.getProductId(), request.getQuantity());
            result.put("success", success);
            result.put("message", success ? "库存释放成功" : "库存释放失败");
            log.info("库存释放: 商品={}, 数量={}, 结果={}", request.getProductId(), request.getQuantity(), success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "库存释放失败: " + e.getMessage());
            log.error("库存释放失败: 商品={}, 数量={}", request.getProductId(), request.getQuantity(), e);
        }
        return result;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "查询库存信息", description = "查询指定商品的库存状态")
    public Map<String, Object> getInventory(
            @Parameter(description = "商品ID") @PathVariable String productId) {

        Map<String, Object> result = new HashMap<>();
        try {
            ProductInventory inventory = inventoryService.getInventory(productId);
            if (inventory != null) {
                result.put("success", true);
                result.put("message", "查询成功");
                result.put("data", inventory);
            } else {
                result.put("success", false);
                result.put("message", "商品不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            log.error("库存查询失败: {}", productId, e);
        }
        return result;
    }

    @PostMapping("/stress-test")
    @Operation(summary = "高并发压力测试", description = "模拟高并发场景测试分布式锁的效果")
    public Map<String, Object> stressTest(@Valid @RequestBody StressTestRequest request) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 简单的压力测试实现
            long startTime = System.currentTimeMillis();
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < request.getThreadCount() * request.getOperationsPerThread(); i++) {
                try {
                    boolean success = inventoryService.reserveStock(request.getProductId(), 1);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }

            long endTime = System.currentTimeMillis();

            result.put("success", true);
            result.put("message", "压力测试完成");
            result.put("data", Map.of(
                    "totalOperations", request.getThreadCount() * request.getOperationsPerThread(),
                    "successCount", successCount,
                    "failCount", failCount,
                    "executionTimeMs", endTime - startTime
            ));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "压力测试失败: " + e.getMessage());
            log.error("压力测试失败", e);
        }
        return result;
    }
}
