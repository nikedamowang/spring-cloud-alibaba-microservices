package com.cloudDemo.management.monitor.controller;

import com.cloudDemo.management.monitor.dto.ServiceCallRecord;
import com.cloudDemo.management.monitor.dto.ServiceCallStats;
import com.cloudDemo.management.monitor.service.MockDataGeneratorService;
import com.cloudDemo.management.monitor.service.ServiceMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务监控大屏API控制器
 * 提供服务调用统计和监控数据的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@CrossOrigin(origins = "*")
public class ServiceMonitorController {

    @Autowired
    private ServiceMonitorService serviceMonitorService;

    @Autowired
    private MockDataGeneratorService mockDataGeneratorService;

    /**
     * 获取所有服务调用统计概览
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        try {
            List<ServiceCallStats> serviceStats = serviceMonitorService.getAllServiceStats();
            List<ServiceCallRecord> recentCalls = serviceMonitorService.getRecentCallRecords(20);

            // 计算总体统计
            long totalCalls = serviceStats.stream().mapToLong(ServiceCallStats::getTotalCalls).sum();
            long totalSuccess = serviceStats.stream().mapToLong(ServiceCallStats::getSuccessCalls).sum();
            long totalFailure = serviceStats.stream().mapToLong(ServiceCallStats::getFailureCalls).sum();
            double overallSuccessRate = totalCalls > 0 ? (double) totalSuccess / totalCalls * 100 : 0.0;

            // 计算平均响应时间
            double avgResponseTime = serviceStats.stream()
                    .mapToDouble(ServiceCallStats::getAvgResponseTime)
                    .average()
                    .orElse(0.0);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("overview", Map.of(
                    "totalCalls", totalCalls,
                    "successCalls", totalSuccess,
                    "failureCalls", totalFailure,
                    "successRate", Math.round(overallSuccessRate * 100.0) / 100.0,
                    "avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0,
                    "activeServices", serviceStats.size()
            ));
            dashboard.put("serviceStats", serviceStats);
            dashboard.put("recentCalls", recentCalls);
            dashboard.put("timestamp", System.currentTimeMillis());

            log.info("📊 监控大屏数据获取成功 - 服务数: {}, 总调用: {}, 成功率: {}%",
                    serviceStats.size(), totalCalls, Math.round(overallSuccessRate * 100.0) / 100.0);

            return Map.of(
                    "success", true,
                    "message", "监控数据获取成功",
                    "data", dashboard
            );

        } catch (Exception e) {
            log.error("❌ 获取监控大屏数据失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取监控数据失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取所有服务调用统计
     */
    @GetMapping("/stats")
    public Map<String, Object> getServiceStats() {
        try {
            List<ServiceCallStats> stats = serviceMonitorService.getAllServiceStats();

            log.info("📈 服务统计数据获取成功 - 统计条目数: {}", stats.size());

            return Map.of(
                    "success", true,
                    "message", "服务统计获取成功",
                    "data", stats,
                    "count", stats.size()
            );

        } catch (Exception e) {
            log.error("❌ 获取服务统计失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取服务统计失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取最近的服务调用记录
     */
    @GetMapping("/records")
    public Map<String, Object> getRecentCallRecords(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            if (limit > 500) limit = 500; // 限制最大查询数量

            List<ServiceCallRecord> records = serviceMonitorService.getRecentCallRecords(limit);

            log.info("📋 最近调用记录获取成功 - 记录数: {}", records.size());

            return Map.of(
                    "success", true,
                    "message", "调用记录获取成功",
                    "data", records,
                    "count", records.size(),
                    "limit", limit
            );

        } catch (Exception e) {
            log.error("❌ 获取调用记录失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取调用记录失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取指定服务的详细统计
     */
    @GetMapping("/stats/{serviceName}")
    public Map<String, Object> getServiceDetailStats(@PathVariable String serviceName) {
        try {
            List<ServiceCallStats> allStats = serviceMonitorService.getAllServiceStats();

            // 过滤指定服务的统计数据
            List<ServiceCallStats> serviceStats = allStats.stream()
                    .filter(stat -> serviceName.equals(stat.getServiceName()))
                    .toList();

            if (serviceStats.isEmpty()) {
                return Map.of(
                        "success", false,
                        "message", "未找到服务 " + serviceName + " 的统计数据",
                        "data", null
                );
            }

            // 计算服务总体统计
            long totalCalls = serviceStats.stream().mapToLong(ServiceCallStats::getTotalCalls).sum();
            long successCalls = serviceStats.stream().mapToLong(ServiceCallStats::getSuccessCalls).sum();
            long failureCalls = serviceStats.stream().mapToLong(ServiceCallStats::getFailureCalls).sum();
            double successRate = totalCalls > 0 ? (double) successCalls / totalCalls * 100 : 0.0;

            Map<String, Object> serviceDetail = new HashMap<>();
            serviceDetail.put("serviceName", serviceName);
            serviceDetail.put("methodStats", serviceStats);
            serviceDetail.put("summary", Map.of(
                    "totalCalls", totalCalls,
                    "successCalls", successCalls,
                    "failureCalls", failureCalls,
                    "successRate", Math.round(successRate * 100.0) / 100.0,
                    "methodCount", serviceStats.size()
            ));

            log.info("🔍 服务详细统计获取成功 - 服务: {}, 方法数: {}, 总调用: {}",
                    serviceName, serviceStats.size(), totalCalls);

            return Map.of(
                    "success", true,
                    "message", "服务详细统计获取成功",
                    "data", serviceDetail
            );

        } catch (Exception e) {
            log.error("❌ 获取服务详细统计失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取服务详细统计失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取监控系统健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> getMonitorHealth() {
        try {
            List<ServiceCallStats> stats = serviceMonitorService.getAllServiceStats();
            List<ServiceCallRecord> recentCalls = serviceMonitorService.getRecentCallRecords(10);

            boolean isHealthy = true;
            String status = "HEALTHY";
            String message = "监控系统运行正常";

            // 简单的健康检查逻辑
            if (stats.isEmpty() && recentCalls.isEmpty()) {
                status = "WARNING";
                message = "暂无监控数据";
            }

            Map<String, Object> health = Map.of(
                    "status", status,
                    "message", message,
                    "timestamp", System.currentTimeMillis(),
                    "dataStatus", Map.of(
                            "statsCount", stats.size(),
                            "recentRecordsCount", recentCalls.size(),
                            "isCollecting", !stats.isEmpty() || !recentCalls.isEmpty()
                    )
            );

            return Map.of(
                    "success", true,
                    "data", health
            );

        } catch (Exception e) {
            log.error("❌ 获取监控健康状态失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取监控健康状态失败: " + e.getMessage(),
                    "data", Map.of(
                            "status", "ERROR",
                            "message", "监控系统异常: " + e.getMessage(),
                            "timestamp", System.currentTimeMillis()
                    )
            );
        }
    }

    /**
     * 生成模拟数据用于测试监控功能
     */
    @PostMapping("/mock/generate")
    public Map<String, Object> generateMockData(
            @RequestParam(defaultValue = "100") int count) {
        try {
            if (count > 1000) {
                return Map.of(
                        "success", false,
                        "message", "生成数量不能超过1000条",
                        "data", null
                );
            }

            mockDataGeneratorService.generateMockCallRecords(count);

            log.info("🎭 模拟数据生成成功 - 数量: {}", count);

            return Map.of(
                    "success", true,
                    "message", "模拟数据生成成功",
                    "data", Map.of(
                            "generatedCount", count,
                            "timestamp", System.currentTimeMillis()
                    )
            );

        } catch (Exception e) {
            log.error("❌ 生成模拟数据失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "生成模拟数据失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取模拟统计数据（用于前端展示测试）
     */
    @GetMapping("/mock/stats")
    public Map<String, Object> getMockStats() {
        try {
            List<ServiceCallStats> mockStats = mockDataGeneratorService.generateMockStats();

            log.info("📊 模拟统计数据获取成功 - 条目数: {}", mockStats.size());

            return Map.of(
                    "success", true,
                    "message", "模拟统计数据获取成功",
                    "data", mockStats,
                    "count", mockStats.size()
            );

        } catch (Exception e) {
            log.error("❌ 获取模拟统计数据失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取模拟统计数据失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 清理模拟数据
     */
    @DeleteMapping("/mock/clear")
    public Map<String, Object> clearMockData() {
        try {
            mockDataGeneratorService.clearMockData();

            log.info("🧹 模拟数据清理成功");

            return Map.of(
                    "success", true,
                    "message", "模拟数据清理成功",
                    "data", null
            );

        } catch (Exception e) {
            log.error("❌ 清理模拟数据失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "清理模拟数据失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取监控大屏实时概览（包含模拟数据）
     */
    @GetMapping("/dashboard/demo")
    public Map<String, Object> getDemoDashboard() {
        try {
            // 获取真实统计数据
            List<ServiceCallStats> realStats = serviceMonitorService.getAllServiceStats();
            List<ServiceCallRecord> recentCalls = serviceMonitorService.getRecentCallRecords(10);

            // 如果没有真实数据，使用模拟数据
            List<ServiceCallStats> displayStats = realStats.isEmpty() ?
                    mockDataGeneratorService.generateMockStats() : realStats;

            // 计算总体统计
            long totalCalls = displayStats.stream().mapToLong(ServiceCallStats::getTotalCalls).sum();
            long totalSuccess = displayStats.stream().mapToLong(ServiceCallStats::getSuccessCalls).sum();
            long totalFailure = displayStats.stream().mapToLong(ServiceCallStats::getFailureCalls).sum();
            double overallSuccessRate = totalCalls > 0 ? (double) totalSuccess / totalCalls * 100 : 0.0;

            // 计算平均响应时间
            double avgResponseTime = displayStats.stream()
                    .mapToDouble(ServiceCallStats::getAvgResponseTime)
                    .average()
                    .orElse(0.0);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("overview", Map.of(
                    "totalCalls", totalCalls,
                    "successCalls", totalSuccess,
                    "failureCalls", totalFailure,
                    "successRate", Math.round(overallSuccessRate * 100.0) / 100.0,
                    "avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0,
                    "activeServices", displayStats.size(),
                    "dataSource", realStats.isEmpty() ? "MOCK" : "REAL"
            ));
            dashboard.put("serviceStats", displayStats);
            dashboard.put("recentCalls", recentCalls);
            dashboard.put("timestamp", System.currentTimeMillis());

            log.info("📊 Demo监控大屏数据获取成功 - 服务数: {}, 总调用: {}, 数据源: {}",
                    displayStats.size(), totalCalls, realStats.isEmpty() ? "模拟" : "真实");

            return Map.of(
                    "success", true,
                    "message", "Demo监控数据获取成功",
                    "data", dashboard
            );

        } catch (Exception e) {
            log.error("❌ 获取Demo监控大屏数据失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取Demo监控数据失败: " + e.getMessage(),
                    "data", null
            );
        }
    }
}
