package com.cloudDemo.management.controller;

import com.cloudDemo.management.service.ResponseTimeTrendsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 接口响应时间趋势分析控制器
 * 提供详细的响应时间监控和趋势分析功能
 */
@Slf4j
@RestController
@RequestMapping("/api/performance/response-times")
@Tag(name = "接口响应时间趋势分析", description = "接口性能监控和趋势分析API")
public class ResponseTimeTrendsController {

    @Autowired
    private ResponseTimeTrendsService responseTimeTrendsService;

    /**
     * 获取指定接口的响应时间趋势
     */
    @GetMapping("/trends")
    @Operation(summary = "获取接口响应时间趋势", description = "获取指定服务接口的详细响应时间趋势数据")
    public Map<String, Object> getResponseTimeTrends(
            @Parameter(description = "服务名称") @RequestParam String serviceName,
            @Parameter(description = "方法名称") @RequestParam String methodName,
            @Parameter(description = "时间窗口类型") @RequestParam(defaultValue = "HOUR") String timeWindow,
            @Parameter(description = "数据点数量") @RequestParam(defaultValue = "12") Integer limit) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> trendsData = responseTimeTrendsService.getInterfaceResponseTimeTrends(
                    serviceName, methodName, timeWindow, limit);

            result.put("success", true);
            result.put("message", "获取响应时间趋势成功");
            result.put("data", trendsData);

        } catch (Exception e) {
            log.error("获取响应时间趋势异常: serviceName={}, methodName={}", serviceName, methodName, e);
            result.put("success", false);
            result.put("message", "获取响应时间趋势失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取聚合的响应时间趋势概览
     */
    @GetMapping("/aggregated")
    @Operation(summary = "聚合响应时间趋势", description = "获取所有服务的聚合响应时间趋势分析")
    public Map<String, Object> getAggregatedResponseTimeTrends(
            @Parameter(description = "时间窗口类型") @RequestParam(defaultValue = "HOUR") String timeWindow,
            @Parameter(description = "时间段数量") @RequestParam(defaultValue = "12") Integer limit,
            @Parameter(description = "服务名称过滤") @RequestParam(required = false) String serviceName) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> aggregatedData = responseTimeTrendsService.getAggregatedResponseTimeTrends(
                    timeWindow, limit, serviceName);

            result.put("success", true);
            result.put("message", "获取聚合响应时间趋势成功");
            result.put("data", aggregatedData);

        } catch (Exception e) {
            log.error("获取聚合响应时间趋势异常: timeWindow={}, limit={}", timeWindow, limit, e);
            result.put("success", false);
            result.put("message", "获取聚合响应时间趋势失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取性能异常检测报告
     */
    @GetMapping("/anomalies")
    @Operation(summary = "性能异常检测", description = "检测并返回响应时间异常的接口列表")
    public Map<String, Object> getPerformanceAnomalies(
            @Parameter(description = "时间窗口类型") @RequestParam(defaultValue = "HOUR") String timeWindow,
            @Parameter(description = "异常阈值倍数") @RequestParam(defaultValue = "3.0") Double thresholdMultiplier) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> anomaliesData = responseTimeTrendsService.detectPerformanceAnomalies(
                    timeWindow, thresholdMultiplier);

            result.put("success", true);
            result.put("message", "性能异常检测完成");
            result.put("data", anomaliesData);

        } catch (Exception e) {
            log.error("性能异常检测异常: timeWindow={}, threshold={}", timeWindow, thresholdMultiplier, e);
            result.put("success", false);
            result.put("message", "性能异常检测失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取性能基准对比分析
     */
    @GetMapping("/baseline-comparison")
    @Operation(summary = "性能基准对比", description = "对比当前性能与历史基准数据")
    public Map<String, Object> getBaselineComparison(
            @Parameter(description = "服务名称") @RequestParam String serviceName,
            @Parameter(description = "对比天数") @RequestParam(defaultValue = "7") Integer comparisonDays) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> comparisonData = responseTimeTrendsService.getBaselineComparison(
                    serviceName, comparisonDays);

            result.put("success", true);
            result.put("message", "性能基准对比分析完成");
            result.put("data", comparisonData);

        } catch (Exception e) {
            log.error("性能基准对比异常: serviceName={}, days={}", serviceName, comparisonDays, e);
            result.put("success", false);
            result.put("message", "性能基准对比失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取性能热点分析
     */
    @GetMapping("/hotspots")
    @Operation(summary = "性能热点分析", description = "识别响应时间最慢的接口热点")
    public Map<String, Object> getPerformanceHotspots(
            @Parameter(description = "返回热点数量") @RequestParam(defaultValue = "10") Integer topN,
            @Parameter(description = "时间窗口类型") @RequestParam(defaultValue = "HOUR") String timeWindow) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> hotspotsData = responseTimeTrendsService.getPerformanceHotspots(
                    topN, timeWindow);

            result.put("success", true);
            result.put("message", "性能热点分析完成");
            result.put("data", hotspotsData);

        } catch (Exception e) {
            log.error("性能热点分析异常: topN={}, timeWindow={}", topN, timeWindow, e);
            result.put("success", false);
            result.put("message", "性能热点分析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取性能趋势预测
     */
    @GetMapping("/predictions")
    @Operation(summary = "性能趋势预测", description = "基于历史数据预测未来响应时间趋势")
    public Map<String, Object> getPerformancePredictions(
            @Parameter(description = "服务名称") @RequestParam String serviceName,
            @Parameter(description = "方法名称") @RequestParam String methodName,
            @Parameter(description = "预测时长(小时)") @RequestParam(defaultValue = "24") Integer forecastHours) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> predictionsData = responseTimeTrendsService.getPerformancePredictions(
                    serviceName, methodName, forecastHours);

            result.put("success", true);
            result.put("message", "性能趋势预测完成");
            result.put("data", predictionsData);

        } catch (Exception e) {
            log.error("性能趋势预测异常: {}.{}, hours={}", serviceName, methodName, forecastHours, e);
            result.put("success", false);
            result.put("message", "性能趋势预测失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取响应时间分布分析
     */
    @GetMapping("/distribution")
    @Operation(summary = "响应时间分布分析", description = "分析服务响应时间的统计分布情况")
    public Map<String, Object> getResponseTimeDistribution(
            @Parameter(description = "服务名称") @RequestParam String serviceName,
            @Parameter(description = "时间窗口类型") @RequestParam(defaultValue = "HOUR") String timeWindow) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> distributionData = responseTimeTrendsService.getResponseTimeDistribution(
                    serviceName, timeWindow);

            result.put("success", true);
            result.put("message", "响应时间分布分析完成");
            result.put("data", distributionData);

        } catch (Exception e) {
            log.error("响应时间分布分析异常: serviceName={}, timeWindow={}", serviceName, timeWindow, e);
            result.put("success", false);
            result.put("message", "响应时间分布分析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取性能监控仪表板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "性能监控仪表板", description = "获取性能监控的综合仪表板数据")
    public Map<String, Object> getPerformanceDashboard() {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> dashboardData = responseTimeTrendsService.getPerformanceDashboardData();

            result.put("success", true);
            result.put("message", "获取性能监控仪表板数据成功");
            result.put("data", dashboardData);

        } catch (Exception e) {
            log.error("获取性能监控仪表板数据异常", e);
            result.put("success", false);
            result.put("message", "获取性能监控仪表板数据失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "服务健康检查", description = "检查响应时间分析服务的健康状态")
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("status", "UP");
        result.put("service", "ResponseTimeTrendsService");
        result.put("timestamp", java.time.LocalDateTime.now());
        return result;
    }
}
