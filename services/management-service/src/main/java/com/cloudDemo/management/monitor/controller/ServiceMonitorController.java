package com.cloudDemo.management.monitor.controller;

import com.cloudDemo.management.monitor.dto.ResponseTimeTrend;
import com.cloudDemo.management.monitor.dto.ServiceCallRecord;
import com.cloudDemo.management.monitor.dto.ServiceCallStats;
import com.cloudDemo.management.monitor.dto.ServiceHealthStatus;
import com.cloudDemo.management.monitor.service.MockDataGeneratorService;
import com.cloudDemo.management.monitor.service.ResponseTimeTrendService;
import com.cloudDemo.management.monitor.service.ServiceHealthCheckService;
import com.cloudDemo.management.monitor.service.ServiceMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private ServiceHealthCheckService serviceHealthCheckService;

    @Autowired
    private ResponseTimeTrendService responseTimeTrendService;

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

    /**
     * 获取所有服务的健康状态
     */
    @GetMapping("/health/status")
    public Map<String, Object> getAllServicesHealthStatus() {
        try {
            List<ServiceHealthStatus> healthStatuses = serviceHealthCheckService.checkAllServicesHealth();

            log.info("🏥 服务健康状态检查完成 - 检查实例数: {}", healthStatuses.size());

            return Map.of(
                    "success", true,
                    "message", "服务健康状态获取成功",
                    "data", healthStatuses,
                    "count", healthStatuses.size(),
                    "timestamp", System.currentTimeMillis()
            );

        } catch (Exception e) {
            log.error("❌ 获取服务健康状态失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取服务健康状态失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取服务健康状态汇总
     */
    @GetMapping("/health/summary")
    public Map<String, Object> getHealthSummary() {
        try {
            Map<String, Object> summary = serviceHealthCheckService.getHealthSummary();

            log.info("📊 服务健康状态汇总获取成功 - 服务数: {}, 实例数: {}",
                    summary.get("totalServices"), summary.get("totalInstances"));

            return Map.of(
                    "success", true,
                    "message", "服务健康状态汇总获取成功",
                    "data", summary
            );

        } catch (Exception e) {
            log.error("❌ 获取服务健康状态汇总失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取服务健康状态汇总失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取指定服务的健康状态
     */
    @GetMapping("/health/service/{serviceName}")
    public Map<String, Object> getServiceHealthStatus(@PathVariable String serviceName) {
        try {
            List<ServiceHealthStatus> healthStatuses = serviceHealthCheckService.checkServiceHealth(serviceName);

            if (healthStatuses.isEmpty()) {
                return Map.of(
                        "success", false,
                        "message", "未找到服务 " + serviceName + " 的健康状态信息",
                        "data", null
                );
            }

            // 计算服务级别的统计信息
            long healthyCount = healthStatuses.stream()
                    .filter(status -> "HEALTHY".equals(status.getHealthStatus()))
                    .count();

            double healthRate = (double) healthyCount / healthStatuses.size() * 100.0;

            Map<String, Object> serviceHealth = new HashMap<>();
            serviceHealth.put("serviceName", serviceName);
            serviceHealth.put("instances", healthStatuses);
            serviceHealth.put("totalInstances", healthStatuses.size());
            serviceHealth.put("healthyInstances", healthyCount);
            serviceHealth.put("healthRate", Math.round(healthRate * 100.0) / 100.0);
            serviceHealth.put("lastCheckTime", LocalDateTime.now());

            log.info("🔍 服务健康状态获取成功 - 服务: {}, 实例数: {}, 健康率: {}%",
                    serviceName, healthStatuses.size(), Math.round(healthRate * 100.0) / 100.0);

            return Map.of(
                    "success", true,
                    "message", "服务健康状态获取成功",
                    "data", serviceHealth
            );

        } catch (Exception e) {
            log.error("❌ 获取服务 {} 健康状态失败: {}", serviceName, e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取服务健康状态失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 强制刷新所有服务健康状态检查
     */
    @PostMapping("/health/refresh")
    public Map<String, Object> refreshHealthStatus() {
        try {
            List<ServiceHealthStatus> healthStatuses = serviceHealthCheckService.checkAllServicesHealth();
            Map<String, Object> summary = serviceHealthCheckService.getHealthSummary();

            log.info("🔄 强制刷新健康状态完成 - 检查实例数: {}", healthStatuses.size());

            return Map.of(
                    "success", true,
                    "message", "健康状态刷新成功",
                    "data", Map.of(
                            "refreshTime", LocalDateTime.now(),
                            "checkedInstances", healthStatuses.size(),
                            "summary", summary
                    )
            );

        } catch (Exception e) {
            log.error("❌ 刷新健康状态失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "刷新健康状态失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取增强版监控大屏（包含健康状态）
     */
    @GetMapping("/dashboard/enhanced")
    public Map<String, Object> getEnhancedDashboard() {
        try {
            // 获取服务调用统计
            List<ServiceCallStats> serviceStats = serviceMonitorService.getAllServiceStats();
            List<ServiceCallRecord> recentCalls = serviceMonitorService.getRecentCallRecords(10);

            // 获取服务健康状态
            Map<String, Object> healthSummary = serviceHealthCheckService.getHealthSummary();
            List<ServiceHealthStatus> healthStatuses = serviceHealthCheckService.checkAllServicesHealth();

            // 如果没有真实统计数据，使用模拟数据
            List<ServiceCallStats> displayStats = serviceStats.isEmpty() ?
                    mockDataGeneratorService.generateMockStats() : serviceStats;

            // 计算调用统计
            long totalCalls = displayStats.stream().mapToLong(ServiceCallStats::getTotalCalls).sum();
            long totalSuccess = displayStats.stream().mapToLong(ServiceCallStats::getSuccessCalls).sum();
            long totalFailure = displayStats.stream().mapToLong(ServiceCallStats::getFailureCalls).sum();
            double overallSuccessRate = totalCalls > 0 ? (double) totalSuccess / totalCalls * 100 : 0.0;
            double avgResponseTime = displayStats.stream()
                    .mapToDouble(ServiceCallStats::getAvgResponseTime)
                    .average().orElse(0.0);

            // 构建增强版监控大屏数据
            Map<String, Object> enhancedDashboard = new HashMap<>();

            // 概览信息（包含健康状态）- 使用HashMap避免Map.of()参数限制
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalCalls", totalCalls);
            overview.put("successCalls", totalSuccess);
            overview.put("failureCalls", totalFailure);
            overview.put("callSuccessRate", Math.round(overallSuccessRate * 100.0) / 100.0);
            overview.put("avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
            overview.put("activeServices", displayStats.size());
            overview.put("totalInstances", healthSummary.get("totalInstances"));
            overview.put("healthyInstances", healthSummary.get("healthyInstances"));
            overview.put("unhealthyInstances", healthSummary.get("unhealthyInstances"));
            overview.put("overallHealthRate", healthSummary.get("overallHealthRate"));
            overview.put("dataSource", serviceStats.isEmpty() ? "MOCK" : "REAL");

            enhancedDashboard.put("overview", overview);

            // 详细数据
            enhancedDashboard.put("serviceStats", displayStats);
            enhancedDashboard.put("recentCalls", recentCalls);
            enhancedDashboard.put("healthStatuses", healthStatuses);
            enhancedDashboard.put("healthSummary", healthSummary);
            enhancedDashboard.put("timestamp", System.currentTimeMillis());

            log.info("📊 增强版监控大屏数据获取成功 - 服务数: {}, 实例数: {}, 健康率: {}%",
                    displayStats.size(), healthSummary.get("totalInstances"),
                    healthSummary.get("overallHealthRate"));

            return Map.of(
                    "success", true,
                    "message", "增强版监控数据获取成功",
                    "data", enhancedDashboard
            );

        } catch (Exception e) {
            log.error("❌ 获取增强版监控大屏数据失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取增强版监控数据失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取指定服务方法的响应时间趋势分析
     */
    @GetMapping("/trend/response-time")
    public Map<String, Object> getResponseTimeTrend(
            @RequestParam String serviceName,
            @RequestParam String methodName,
            @RequestParam(defaultValue = "HOUR") String timeWindow,
            @RequestParam(defaultValue = "24") int periods) {
        try {
            if (periods > 168) periods = 168; // 最多7天（以小时为单位）

            var trend = responseTimeTrendService.analyzeResponseTimeTrend(
                    serviceName, methodName, timeWindow, periods);

            log.info("📈 响应时间趋势分析获取成功 - 服务: {}, 方法: {}, 趋势: {}, 性能等级: {}",
                    serviceName, methodName, trend.getTrendType(), trend.getPerformanceLevel());

            return Map.of(
                    "success", true,
                    "message", "响应时间趋势分析获取成功",
                    "data", trend
            );

        } catch (Exception e) {
            log.error("❌ 获取响应时间趋势分析失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取响应时间趋势分析失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取所有服务的响应时间趋势概览
     */
    @GetMapping("/trend/overview")
    public Map<String, Object> getAllServicesTrendOverview(
            @RequestParam(defaultValue = "HOUR") String timeWindow,
            @RequestParam(defaultValue = "12") int periods) {
        try {
            if (periods > 72) periods = 72; // 最多3天（以小时为单位）

            var trends = responseTimeTrendService.getAllServicesTrendOverview(timeWindow, periods);

            // 按性能问题分类统计
            Map<String, Long> performanceLevelCounts = trends.stream()
                    .collect(Collectors.groupingBy(
                            trend -> trend.getPerformanceLevel(),
                            Collectors.counting()
                    ));

            // 按趋势类型分类统计
            Map<String, Long> trendTypeCounts = trends.stream()
                    .collect(Collectors.groupingBy(
                            trend -> trend.getTrendType(),
                            Collectors.counting()
                    ));

            // 统计异常接口数量
            long anomalyInterfaces = trends.stream()
                    .filter(trend -> trend.getAnomalyCount() != null && trend.getAnomalyCount() > 0)
                    .count();

            Map<String, Object> overview = new HashMap<>();
            overview.put("totalInterfaces", trends.size());
            overview.put("anomalyInterfaces", anomalyInterfaces);
            overview.put("performanceLevelDistribution", performanceLevelCounts);
            overview.put("trendTypeDistribution", trendTypeCounts);
            overview.put("analysisTimeWindow", timeWindow);
            overview.put("analysisPeriods", periods);

            log.info("📊 所有服务响应时间趋势概览获取成功 - 接口数: {}, 异常接口: {}",
                    trends.size(), anomalyInterfaces);

            return Map.of(
                    "success", true,
                    "message", "响应时间趋势概览获取成功",
                    "data", Map.of(
                            "overview", overview,
                            "trends", trends
                    )
            );

        } catch (Exception e) {
            log.error("❌ 获取响应时间趋势概览失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取响应时间趋势概览失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取性能问题接口列表
     */
    @GetMapping("/trend/performance-issues")
    public Map<String, Object> getPerformanceIssues(
            @RequestParam(defaultValue = "HOUR") String timeWindow,
            @RequestParam(defaultValue = "6") int periods,
            @RequestParam(defaultValue = "POOR") String minLevel) {
        try {
            List<ResponseTimeTrend> allTrends = responseTimeTrendService.getAllServicesTrendOverview(timeWindow, periods);

            // 过滤出有性能问题的接口 - 声明为final避免lambda表达式错误
            final List<String> problemLevels;
            if ("NORMAL".equals(minLevel)) {
                problemLevels = Arrays.asList("NORMAL", "POOR", "CRITICAL");
            } else {
                problemLevels = Arrays.asList("POOR", "CRITICAL");
            }

            List<ResponseTimeTrend> problemTrends = allTrends.stream()
                    .filter(trend -> problemLevels.contains(trend.getPerformanceLevel()))
                    .sorted((t1, t2) -> {
                        // 按问题严重程度排序
                        int score1 = getPerformanceScore(t1.getPerformanceLevel());
                        int score2 = getPerformanceScore(t2.getPerformanceLevel());
                        if (score1 != score2) return Integer.compare(score2, score1);
                        // 相同等级按平均响应时间排序
                        return Double.compare(t2.getAvgResponseTime(), t1.getAvgResponseTime());
                    })
                    .collect(Collectors.toList());

            // 生成问题建议
            List<Map<String, Object>> recommendations = generatePerformanceRecommendations(problemTrends);

            log.info("🚨 性能问题接口分析完成 - 问题接口数: {}/{}, 最低等级: {}",
                    problemTrends.size(), allTrends.size(), minLevel);

            return Map.of(
                    "success", true,
                    "message", "性能问题分析完成",
                    "data", Map.of(
                            "problemInterfaces", problemTrends,
                            "recommendations", recommendations,
                            "summary", Map.of(
                                    "totalProblems", problemTrends.size(),
                                    "totalAnalyzed", allTrends.size(),
                                    "analysisTimeWindow", timeWindow,
                                    "analysisPeriods", periods
                            )
                    )
            );

        } catch (Exception e) {
            log.error("❌ 获取性能问题分析失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取性能问题分析失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取完整版监控大屏（包含趋势分析）
     */
    @GetMapping("/dashboard/complete")
    public Map<String, Object> getCompleteDashboard() {
        try {
            // 获取服务调用统计
            List<ServiceCallStats> serviceStats = serviceMonitorService.getAllServiceStats();
            List<ServiceCallRecord> recentCalls = serviceMonitorService.getRecentCallRecords(10);

            // 获取服务健康状态
            Map<String, Object> healthSummary = serviceHealthCheckService.getHealthSummary();
            List<ServiceHealthStatus> healthStatuses = serviceHealthCheckService.checkAllServicesHealth();

            // 获取响应时间趋势分析
            var trendOverview = responseTimeTrendService.getAllServicesTrendOverview("HOUR", 6);

            // 如果没有真实统计数据，使用模拟数据
            List<ServiceCallStats> displayStats = serviceStats.isEmpty() ?
                    mockDataGeneratorService.generateMockStats() : serviceStats;

            // 计算调用统计
            long totalCalls = displayStats.stream().mapToLong(ServiceCallStats::getTotalCalls).sum();
            long totalSuccess = displayStats.stream().mapToLong(ServiceCallStats::getSuccessCalls).sum();
            long totalFailure = displayStats.stream().mapToLong(ServiceCallStats::getFailureCalls).sum();
            double overallSuccessRate = totalCalls > 0 ? (double) totalSuccess / totalCalls * 100 : 0.0;
            double avgResponseTime = displayStats.stream()
                    .mapToDouble(ServiceCallStats::getAvgResponseTime)
                    .average().orElse(0.0);

            // 分析趋势问题
            long trendAnomalies = trendOverview.stream()
                    .filter(trend -> trend.getAnomalyCount() != null && trend.getAnomalyCount() > 0)
                    .count();

            // 构建完整版监控大屏数据
            Map<String, Object> completeDashboard = new HashMap<>();

            // 综合概览信息
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalCalls", totalCalls);
            overview.put("successCalls", totalSuccess);
            overview.put("failureCalls", totalFailure);
            overview.put("callSuccessRate", Math.round(overallSuccessRate * 100.0) / 100.0);
            overview.put("avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
            overview.put("activeServices", displayStats.size());
            overview.put("totalInstances", healthSummary.get("totalInstances"));
            overview.put("healthyInstances", healthSummary.get("healthyInstances"));
            overview.put("unhealthyInstances", healthSummary.get("unhealthyInstances"));
            overview.put("overallHealthRate", healthSummary.get("overallHealthRate"));
            overview.put("trendAnalyzedInterfaces", trendOverview.size());
            overview.put("trendAnomalies", trendAnomalies);
            overview.put("dataSource", serviceStats.isEmpty() ? "MOCK" : "REAL");

            completeDashboard.put("overview", overview);

            // 详细数据
            completeDashboard.put("serviceStats", displayStats);
            completeDashboard.put("recentCalls", recentCalls);
            completeDashboard.put("healthStatuses", healthStatuses);
            completeDashboard.put("healthSummary", healthSummary);
            completeDashboard.put("trendAnalysis", trendOverview);
            completeDashboard.put("timestamp", System.currentTimeMillis());

            log.info("🎯 完整版监控大屏数据��取成功 - 服务数: {}, 健康率: {}%, 趋势异常: {}",
                    displayStats.size(), healthSummary.get("overallHealthRate"), trendAnomalies);

            return Map.of(
                    "success", true,
                    "message", "完整版监控数据获取成功",
                    "data", completeDashboard
            );

        } catch (Exception e) {
            log.error("❌ 获取完整版监控大屏数据失败: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "获取完整版监控数据失败: " + e.getMessage(),
                    "data", null
            );
        }
    }

    /**
     * 获取性能分数
     */
    private int getPerformanceScore(String level) {
        return switch (level) {
            case "CRITICAL" -> 5;
            case "POOR" -> 4;
            case "NORMAL" -> 3;
            case "GOOD" -> 2;
            case "EXCELLENT" -> 1;
            default -> 0;
        };
    }

    /**
     * 生成性能优化建议
     */
    private List<Map<String, Object>> generatePerformanceRecommendations(List<ResponseTimeTrend> problemTrends) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (ResponseTimeTrend trend : problemTrends) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("serviceName", trend.getServiceName());
            recommendation.put("methodName", trend.getMethodName());
            recommendation.put("performanceLevel", trend.getPerformanceLevel());
            recommendation.put("avgResponseTime", trend.getAvgResponseTime());

            // 生成具体建议
            List<String> suggestions = new ArrayList<>();
            if ("CRITICAL".equals(trend.getPerformanceLevel())) {
                suggestions.add("立即检查服务性能，可能存在严重性能问题");
                suggestions.add("检查数据库查询是否有慢查询");
                suggestions.add("检查是否有死锁或资源竞争");
            } else if ("POOR".equals(trend.getPerformanceLevel())) {
                suggestions.add("建议优化接口性能");
                suggestions.add("检查缓存策略是否合理");
                suggestions.add("考虑添加接口限流");
            } else if ("NORMAL".equals(trend.getPerformanceLevel())) {
                suggestions.add("性能正常，但可以进一步优化");
                suggestions.add("监控趋势变化");
            }

            if (trend.getAnomalyCount() != null && trend.getAnomalyCount() > 0) {
                suggestions.add("检测到响应时间异常波动，建议关注");
            }

            recommendation.put("suggestions", suggestions);
            recommendations.add(recommendation);
        }

        return recommendations;
    }
}
