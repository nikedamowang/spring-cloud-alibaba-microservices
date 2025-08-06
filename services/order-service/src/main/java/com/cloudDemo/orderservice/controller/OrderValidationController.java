package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.dto.validation.CreateOrderRequest;
import com.cloudDemo.orderservice.dto.validation.SimpleCreateOrderRequest;
import com.cloudDemo.orderservice.dto.validation.UpdateOrderRequest;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.mapper.OrdersMapper;
import com.cloudDemo.orderservice.validation.group.CreateOrderGroup;
import com.cloudDemo.orderservice.validation.group.UpdateOrderGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单校验增强控制器
 * 提供带有完整参数校验功能的订单管理接口
 */
@Slf4j
@RestController
@RequestMapping("/orders/validation")
@Tag(name = "订单校验增强API", description = "提供完整参数校验功能的订单管理接口")
@Validated
public class OrderValidationController {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 创建订单 - 带完整校验
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建新订单，支持完整的参数校验")
    public ResponseEntity<Map<String, Object>> createOrder(
            @Valid @Validated(CreateOrderGroup.class) @RequestBody CreateOrderRequest request) {

        log.info("🛒 开始创建订单，用户ID: {}, 总金额: {}", request.getUserId(), request.getTotalAmount());

        try {
            // 转换为Orders实体
            Orders order = new Orders();
            BeanUtils.copyProperties(request, order);

            // 生成订单编号
            String orderNo = generateOrderNo();
            order.setOrderNo(orderNo);
            order.setStatus("PENDING");
            order.setCreateTime(LocalDateTime.now());

            // 保存订单
            int result = ordersMapper.insert(order);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("success", true);
                response.put("message", "订单创建成功");
                response.put("data", order);

                log.info("✅ 订单创建成功，ID: {}, 订单号: {}", order.getId(), order.getOrderNo());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "订单创建失败");

                log.error("❌ 订单创建失败，数据库插入返回0");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 创建订单异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 创建订单 - 简化版校验测试
     */
    @PostMapping("/create-simple")
    @Operation(summary = "创建订单(简化校验)", description = "创建新订单，使用简化的参数校验进行测试")
    public ResponseEntity<Map<String, Object>> createOrderSimple(
            @Valid @RequestBody SimpleCreateOrderRequest request) {

        log.info("🛒 开始创建订单(简化版)，用户ID: {}, 总金额: {}", request.getUserId(), request.getTotalAmount());

        try {
            // 转换为Orders实体
            Orders order = new Orders();
            BeanUtils.copyProperties(request, order);

            // 生成订单编号
            String orderNo = generateOrderNo();
            order.setOrderNo(orderNo);
            order.setStatus("PENDING");
            order.setCreateTime(LocalDateTime.now());

            // 保存订单
            int result = ordersMapper.insert(order);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("success", true);
                response.put("message", "订单创建成功");
                response.put("data", order);

                log.info("✅ 订单创建成功，ID: {}, 订单号: {}", order.getId(), order.getOrderNo());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "订单创建失败");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 创建订单异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 更新订单 - 带完整校验
     */
    @PutMapping("/update")
    @Operation(summary = "更新订单", description = "更新订单信息，支持完整的参数校验")
    public ResponseEntity<Map<String, Object>> updateOrder(
            @Valid @Validated(UpdateOrderGroup.class) @RequestBody UpdateOrderRequest request) {

        log.info("🔧 开始更新订单，订单ID: {}", request.getId());

        try {
            // 检查订单是否存在
            Orders existingOrder = ordersMapper.selectById(request.getId());
            if (existingOrder == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "订单不存在");

                log.warn("⚠️ 更新失败，订单不存在，ID: {}", request.getId());
                return ResponseEntity.badRequest().body(response);
            }

            // 更新订单信息
            Orders order = new Orders();
            BeanUtils.copyProperties(request, order);

            int result = ordersMapper.updateById(order);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                // 获取更新后的订单信息
                Orders updatedOrder = ordersMapper.selectById(request.getId());

                response.put("success", true);
                response.put("message", "订单更新成功");
                response.put("data", updatedOrder);

                log.info("✅ 订单更新成功，ID: {}, 订单号: {}", order.getId(), updatedOrder.getOrderNo());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "订单更新失败");

                log.error("❌ 订单更新失败，数据库更新返回0");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 更新订单异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取订单详情 - 带路径参数校验
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    public ResponseEntity<Map<String, Object>> getOrderDetail(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable @NotNull(message = "订单ID不能为空") @Min(value = 1, message = "订单ID必须大于0") Long id) {

        log.info("🔍 查询订单详情，ID: {}", id);

        try {
            Orders order = ordersMapper.selectById(id);

            Map<String, Object> response = new HashMap<>();
            if (order != null) {
                response.put("success", true);
                response.put("message", "查询成功");
                response.put("data", order);

                log.info("✅ 订单查询成功，ID: {}, 订单号: {}", order.getId(), order.getOrderNo());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "订单不存在");

                log.warn("⚠️ 订单不存在，ID: {}", id);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 查询订单异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除订单 - 带路径参数校验
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除订单", description = "根据订单ID删除订单")
    public ResponseEntity<Map<String, Object>> deleteOrder(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable @NotNull(message = "订单ID不能为空") @Min(value = 1, message = "订单ID必须大于0") Long id) {

        log.info("🗑️ 删除订单，ID: {}", id);

        try {
            // 检查订单是否存在
            Orders existingOrder = ordersMapper.selectById(id);
            if (existingOrder == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "订单不存在");

                log.warn("⚠️ 删除失败，订单不存在，ID: {}", id);
                return ResponseEntity.badRequest().body(response);
            }

            int result = ordersMapper.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("success", true);
                response.put("message", "订单删除成功");
                response.put("deletedOrderId", id);

                log.info("✅ 订单删除成功，ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "订单删除失败");

                log.error("❌ 订单删除失败，数据库删除返回0");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ 删除订单异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除订单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return "ORDER-" + System.currentTimeMillis();
    }
}
