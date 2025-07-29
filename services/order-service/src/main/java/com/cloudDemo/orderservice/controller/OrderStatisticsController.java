package com.cloudDemo.orderservice.controller;

import com.cloudDemo.orderservice.service.OrderStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单统计控制器
 * 提供基于时间窗口的订单统计分析API
 */
@Slf4j
@RestController
@RequestMapping("/api/order/statistics")
@Tag(name = "订单统计分析", description = "基于时间窗口的订单实时统计和分析")
public class OrderStatisticsController {

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @GetMapping("/realtime/{minutes}")
    @Operation(summary = "实时订单统计", description = "获取最近N分钟的订单实时统计数据")
    public ResponseEntity<Map<String, Object>> getRealtimeStats(
            @Parameter(description = "时间窗口（分钟）", example = "30") @PathVariable int minutes) {

        Map<String, Object> result = new HashMap<>();
        try {
            OrderStatisticsService.OrderTimeWindowStats stats = orderStatisticsService.getRealtimeStats(minutes);
            result.put("success", true);
            result.put("message", "实时统计获取成功");
            result.put("timeWindow", minutes + "分钟");
            result.put("stats", stats);
            result.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取实时统计失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取统计失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/status-distribution")
    @Operation(summary = "订单状态分布", description = "获取指定时间段内各状态订单的数量分布")
    public ResponseEntity<Map<String, Object>> getStatusDistribution(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Long> distribution = orderStatisticsService.getOrderStatusDistribution(startTime, endTime);
            result.put("success", true);
            result.put("message", "状态分布统计获取成功");
            result.put("startTime", startTime);
            result.put("endTime", endTime);
            result.put("statusDistribution", distribution);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取状态分布失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取状态分布失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/user-activity/{minutes}")
    @Operation(summary = "用户活跃度统计", description = "获取最近N分钟内最活跃的用户排行")
    public ResponseEntity<Map<String, Object>> getUserActivity(
            @Parameter(description = "时间窗口（分钟）", example = "60") @PathVariable int minutes,
            @Parameter(description = "返回前N名用户", example = "10") @RequestParam(defaultValue = "10") int topN) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<Long, Integer> userActivity = orderStatisticsService.getUserActivityStats(minutes, topN);
            result.put("success", true);
            result.put("message", "用户活跃度统计获取成功");
            result.put("timeWindow", minutes + "分钟");
            result.put("topN", topN);
            result.put("userActivity", userActivity);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取用户活跃度失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取用户活跃度失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/amount-stats/{minutes}")
    @Operation(summary = "订单金额统计", description = "获取最近N分钟的订单金额统计分析")
    public ResponseEntity<Map<String, Object>> getAmountStats(
            @Parameter(description = "时间窗口（分钟）", example = "30") @PathVariable int minutes) {

        Map<String, Object> result = new HashMap<>();
        try {
            OrderStatisticsService.OrderAmountStats amountStats = orderStatisticsService.getAmountStats(minutes);
            result.put("success", true);
            result.put("message", "金额统计获取成功");
            result.put("timeWindow", minutes + "分钟");
            result.put("amountStats", amountStats);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取金额统计失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取金额统计失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/high-frequency-users/{minutes}")
    @Operation(summary = "高频下单用户检测", description = "检测可能存在刷单行为的高频下单用户")
    public ResponseEntity<Map<String, Object>> detectHighFrequencyUsers(
            @Parameter(description = "时间窗口（分钟）", example = "30") @PathVariable int minutes,
            @Parameter(description = "阈值（订单数量）", example = "5") @RequestParam(defaultValue = "5") int threshold) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<Long, Integer> highFrequencyUsers = orderStatisticsService.detectHighFrequencyUsers(minutes, threshold);
            result.put("success", true);
            result.put("message", "高频用户检测完成");
            result.put("timeWindow", minutes + "分钟");
            result.put("threshold", threshold + "单");
            result.put("detectedUsers", highFrequencyUsers);
            result.put("riskLevel", highFrequencyUsers.isEmpty() ? "低" : "高");
            result.put("suggestion", highFrequencyUsers.isEmpty() ?
                    "未发现异常下单行为" : "建议人工审核这些用户的订单");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("高频用户检测失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "检测失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/trends/{hours}")
    @Operation(summary = "订单趋势分析", description = "获取最近N小时的订单数量趋势")
    public ResponseEntity<Map<String, Object>> getOrderTrends(
            @Parameter(description = "时间范围（小时）", example = "24") @PathVariable int hours) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Integer> trends = orderStatisticsService.getOrderTrends(hours);
            result.put("success", true);
            result.put("message", "订单趋势分析获取成功");
            result.put("timeRange", hours + "小时");
            result.put("trends", trends);
            result.put("chartType", "line");
            result.put("xAxis", "时间（小时）");
            result.put("yAxis", "订单数量");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取订单趋势失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取趋势失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "统计仪表盘", description = "获取订单统计的综合仪表盘数据")
    public ResponseEntity<Map<String, Object>> getStatsDashboard() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取多个维度的统计数据
            OrderStatisticsService.OrderTimeWindowStats stats30min = orderStatisticsService.getRealtimeStats(30);
            OrderStatisticsService.OrderAmountStats amountStats = orderStatisticsService.getAmountStats(60);
            Map<Long, Integer> topUsers = orderStatisticsService.getUserActivityStats(60, 5);
            Map<String, Integer> trends = orderStatisticsService.getOrderTrends(12);
            Map<Long, Integer> riskUsers = orderStatisticsService.detectHighFrequencyUsers(30, 3);

            result.put("success", true);
            result.put("message", "仪表盘数据获取成功");
            result.put("dashboardData", Map.of(
                    "realtime30min", stats30min,
                    "amountStats60min", amountStats,
                    "topUsers60min", topUsers,
                    "trends12hours", trends,
                    "riskUsers30min", riskUsers
            ));
            result.put("updateTime", LocalDateTime.now());
            result.put("autoRefresh", "建议5分钟刷新一次");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取仪表盘数据失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取仪表盘数据失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
