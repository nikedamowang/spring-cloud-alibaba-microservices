package com.cloudDemo.orderservice.controller;

import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.service.OrderService;
import com.cloudDemo.orderservice.service.UserRemoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Tag(name = "订单管理", description = "订单相关的API接口，包含订单创建、查询、远程调用等功能")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRemoteService userRemoteService;

    /**
     * 创建订单 - 简化版，不依赖数据库
     */
    @PostMapping("/create")
    @Operation(
            summary = "创建订单",
            description = "创建新订单，会远程调用用户服务验证用户信息。这是一个演示接口，不会真正保存到数据库。"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "订单创建成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "success": true,
                                      "message": "订单创建成功",
                                      "orderId": "ORDER-1690000000000",
                                      "userId": 1,
                                      "userName": "admin",
                                      "userEmail": "admin@example.com",
                                      "productName": "iPhone 15",
                                      "amount": 9999.00,
                                      "createTime": "2025-07-22T10:30:00"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "用户不存在或参数错误",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "success": false,
                                      "message": "用户不存在，无法创建订单"
                                    }
                                    """)
                    )
            )
    })
    public Map<String, Object> createOrder(
            @Parameter(description = "用户ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "产品名称", required = true, example = "iPhone 15")
            @RequestParam String productName,
            @Parameter(description = "订单金额", required = true, example = "9999.00")
            @RequestParam BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();

        // 远程调用用户服务验证用户是否存在
        if (!userRemoteService.userExists(userId)) {
            result.put("success", false);
            result.put("message", "用户不存在，无法创建订单");
            return result;
        }

        // 获取用户信息
        UserDTO user = userRemoteService.getUserById(userId);

        result.put("success", true);
        result.put("message", "订单创建成功");
        result.put("orderId", "ORDER-" + System.currentTimeMillis());
        result.put("userId", userId);
        result.put("userName", user != null ? user.getUsername() : "未知用户");
        result.put("userEmail", user != null ? user.getEmail() : "");
        result.put("productName", productName);
        result.put("amount", amount);
        result.put("createTime", LocalDateTime.now());
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(
            summary = "健康检查",
            description = "检查订单服务运行状态，用于服务监控和负载均衡健康检查"
    )
    @ApiResponse(
            responseCode = "200",
            description = "服务正常运行",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = """
                            {
                              "status": "UP",
                              "service": "order-service",
                              "timestamp": "2025-07-22T10:30:00"
                            }
                            """)
            )
    )
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "order-service");
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    /**
     * 获取orders表的所有数据，最多100条
     */
    @GetMapping("/all")
    @Operation(
            summary = "获取所有订单",
            description = "查询数据库中所有订单信息，最多返回100条记录。用于管理后台订单列表展示。"
    )
    @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = """
                            {
                              "success": true,
                              "message": "查询成功",
                              "total": 2,
                              "data": [
                                {
                                  "id": 1,
                                  "orderNo": "ORDER-20250722001",
                                  "userId": 1001,
                                  "totalAmount": 299.99,
                                  "paymentAmount": 259.99,
                                  "paymentType": "ALIPAY",
                                  "status": "PAID",
                                  "shippingAddress": "北京市朝阳区某某街道123号",
                                  "createTime": "2025-07-22T10:30:00"
                                }
                              ]
                            }
                            """)
            )
    )
    public Map<String, Object> getAllOrders() {
        List<Orders> orders = orderService.getAllOrders();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("total", orders.size());
        result.put("data", orders);
        return result;
    }

    /**
     * 测试远程调用用户服务的接口
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "测试远程调用",
            description = "测试通过Dubbo远程调用用户服务获取用户信息，用于验证微服务间通信"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "远程调用成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "success": true,
                                      "message": "远程调用成功",
                                      "userExists": true,
                                      "userInfo": {
                                        "id": 1,
                                        "username": "admin",
                                        "email": "admin@example.com"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "远程调用失败",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "success": false,
                                      "message": "远程调用失败: Connection timeout"
                                    }
                                    """)
                    )
            )
    })
    public Map<String, Object> testUserRemoteCall(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 测试获取用户信息
            UserDTO user = userRemoteService.getUserById(userId);
            boolean exists = userRemoteService.userExists(userId);

            result.put("success", true);
            result.put("message", "远程调用成功");
            result.put("userExists", exists);
            result.put("userInfo", user);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "远程调用失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取所有用户信息（演示远程调用）
     */
    @GetMapping("/test/users")
    @Operation(
            summary = "获取所有用户",
            description = "通过远程调用获取用户服务的所有用户信息，演示跨服务数据查询"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "success": true,
                                      "message": "获取用户列表成功",
                                      "count": 3,
                                      "users": [
                                        {
                                          "id": 1,
                                          "username": "admin",
                                          "email": "admin@example.com"
                                        },
                                        {
                                          "id": 2,
                                          "username": "user1",
                                          "email": "user1@example.com"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "远程调用失败",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "success": false,
                                      "message": "远程调用失败: Service unavailable"
                                    }
                                    """)
                    )
            )
    })
    public Map<String, Object> testGetAllUsers() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<UserDTO> users = userRemoteService.getAllUsers();
            result.put("success", true);
            result.put("message", "获取用户列表成功");
            result.put("users", users);
            result.put("count", users != null ? users.size() : 0);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "远程调用失败: " + e.getMessage());
        }

        return result;
    }
}
