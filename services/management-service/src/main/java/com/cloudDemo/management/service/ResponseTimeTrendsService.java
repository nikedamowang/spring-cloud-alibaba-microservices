package com.cloudDemo.management.service;

import java.util.Map;

/**
 * 响应时间趋势分析服务接口
 * 提供接口性能监控、趋势分析和异常检测功能
 */
public interface ResponseTimeTrendsService {

    /**
     * 获取指定接口的响应时间趋势
     */
    Map<String, Object> getInterfaceResponseTimeTrends(String serviceName, String methodName, String timeWindow, Integer limit);

    /**
     * 获取聚合的响应时间趋势
     */
    Map<String, Object> getAggregatedResponseTimeTrends(String timeWindow, Integer limit, String serviceName);

    /**
     * 检测性能异常
     */
    Map<String, Object> detectPerformanceAnomalies(String timeWindow, Double thresholdMultiplier);

    /**
     * 性能基准对比分析
     */
    Map<String, Object> getBaselineComparison(String serviceName, Integer comparisonDays);

    /**
     * 获取性能热点分析
     */
    Map<String, Object> getPerformanceHotspots(Integer topN, String timeWindow);

    /**
     * 性能趋势预测
     */
    Map<String, Object> getPerformancePredictions(String serviceName, String methodName, Integer forecastHours);

    /**
     * 响应时间分布分析
     */
    Map<String, Object> getResponseTimeDistribution(String serviceName, String timeWindow);

    /**
     * 获取性能监控仪表板数据
     */
    Map<String, Object> getPerformanceDashboardData();
}
