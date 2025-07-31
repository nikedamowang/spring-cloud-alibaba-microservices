package com.cloudDemo.management.monitor.service;

import com.cloudDemo.management.monitor.dto.ResponseTimeTrend;
import com.cloudDemo.management.monitor.dto.ServiceCallRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 响应时间趋势分析服务
 * 负责分析接口响应时间的变化趋势和性能模式
 */
@Slf4j
@Service
public class ResponseTimeTrendService {

    // Redis键前缀
    private static final String TREND_DATA_PREFIX = "trend:response:";
    // 时间格式化器
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");
    // 性能阈值定义（毫秒）
    private static final double EXCELLENT_THRESHOLD = 50.0;
    private static final double GOOD_THRESHOLD = 100.0;
    private static final double NORMAL_THRESHOLD = 200.0;
    private static final double POOR_THRESHOLD = 500.0;
    // 异常检测阈值
    private static final double ANOMALY_MULTIPLIER = 2.0;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ServiceMonitorService serviceMonitorService;

    /**
     * 分析指定服务方法的响应时间趋势
     */
    public ResponseTimeTrend analyzeResponseTimeTrend(String serviceName, String methodName,
                                                      String timeWindowType, int periodCount) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = calculateStartTime(endTime, timeWindowType, periodCount);

            // 获取时间范围内的调用记录
            List<ServiceCallRecord> records = getCallRecordsInTimeRange(serviceName, methodName, startTime, endTime);

            if (records.isEmpty()) {
                return createEmptyTrend(serviceName, methodName, timeWindowType, startTime, endTime);
            }

            // 按时间窗口聚合数据
            List<ResponseTimeTrend.TimePoint> timePoints = aggregateByTimeWindow(records, timeWindowType, startTime, endTime);

            // 计算统计指标
            ResponseTimeTrend trend = calculateTrendStatistics(serviceName, methodName, timeWindowType,
                    startTime, endTime, timePoints, records);

            // 异常检测
            detectAnomalies(trend);

            // 趋势分析
            analyzeTrendPattern(trend);

            // 性能等级评估
            evaluatePerformanceLevel(trend);

            log.info("📈 响应时间趋势分析完成 - 服务: {}, 方法: {}, 数据点: {}",
                    serviceName, methodName, timePoints.size());

            return trend;

        } catch (Exception e) {
            log.error("❌ 响应时间趋势分析失败: {}", e.getMessage(), e);
            return createErrorTrend(serviceName, methodName, timeWindowType, e.getMessage());
        }
    }

    /**
     * 获取所有服务的响应时间趋势概览
     */
    public List<ResponseTimeTrend> getAllServicesTrendOverview(String timeWindowType, int periodCount) {
        List<ResponseTimeTrend> trends = new ArrayList<>();

        try {
            // 获取所有活跃的服务统计
            var serviceStats = serviceMonitorService.getAllServiceStats();

            for (var stat : serviceStats) {
                ResponseTimeTrend trend = analyzeResponseTimeTrend(
                        stat.getServiceName(), stat.getMethodName(), timeWindowType, periodCount);
                trends.add(trend);
            }

            // 按性能问题排序
            trends.sort((t1, t2) -> {
                int level1 = getPerformanceLevelScore(t1.getPerformanceLevel());
                int level2 = getPerformanceLevelScore(t2.getPerformanceLevel());
                return Integer.compare(level2, level1); // 问题严重的排在前面
            });

        } catch (Exception e) {
            log.error("❌ 获取所有服务趋势概览失败: {}", e.getMessage(), e);
        }

        return trends;
    }

    /**
     * 计算开始时间
     */
    private LocalDateTime calculateStartTime(LocalDateTime endTime, String timeWindowType, int periodCount) {
        return switch (timeWindowType.toUpperCase()) {
            case "MINUTE" -> endTime.minus(periodCount, ChronoUnit.MINUTES);
            case "HOUR" -> endTime.minus(periodCount, ChronoUnit.HOURS);
            case "DAY" -> endTime.minus(periodCount, ChronoUnit.DAYS);
            default -> endTime.minus(1, ChronoUnit.HOURS);
        };
    }

    /**
     * 获取时间范围内的调用记录
     */
    private List<ServiceCallRecord> getCallRecordsInTimeRange(String serviceName, String methodName,
                                                              LocalDateTime startTime, LocalDateTime endTime) {
        // 从Redis获取最近的调用记录
        List<ServiceCallRecord> allRecords = serviceMonitorService.getRecentCallRecords(1000);

        return allRecords.stream()
                .filter(record -> serviceName.equals(record.getServiceName()) &&
                        methodName.equals(record.getMethodName()))
                .filter(record -> record.getStartTime() != null &&
                        record.getStartTime().isAfter(startTime) &&
                        record.getStartTime().isBefore(endTime))
                .collect(Collectors.toList());
    }

    /**
     * 按时间窗口聚合数据
     */
    private List<ResponseTimeTrend.TimePoint> aggregateByTimeWindow(List<ServiceCallRecord> records,
                                                                    String timeWindowType,
                                                                    LocalDateTime startTime,
                                                                    LocalDateTime endTime) {
        Map<String, List<ServiceCallRecord>> groupedRecords = new HashMap<>();

        // 按时间窗口分组
        for (ServiceCallRecord record : records) {
            String timeKey = formatTimeKey(record.getStartTime(), timeWindowType);
            groupedRecords.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(record);
        }

        List<ResponseTimeTrend.TimePoint> timePoints = new ArrayList<>();

        // 生成时间点并计算统计数据
        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            String timeKey = formatTimeKey(current, timeWindowType);
            List<ServiceCallRecord> windowRecords = groupedRecords.getOrDefault(timeKey, new ArrayList<>());

            ResponseTimeTrend.TimePoint timePoint = createTimePoint(current, windowRecords);
            timePoints.add(timePoint);

            current = moveToNextWindow(current, timeWindowType);
        }

        return timePoints;
    }

    /**
     * 格式化时间键
     */
    private String formatTimeKey(LocalDateTime dateTime, String timeWindowType) {
        return switch (timeWindowType.toUpperCase()) {
            case "MINUTE" -> dateTime.format(MINUTE_FORMATTER);
            case "HOUR" -> dateTime.format(HOUR_FORMATTER);
            case "DAY" -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            default -> dateTime.format(HOUR_FORMATTER);
        };
    }

    /**
     * 移动到下一个时间窗口
     */
    private LocalDateTime moveToNextWindow(LocalDateTime current, String timeWindowType) {
        return switch (timeWindowType.toUpperCase()) {
            case "MINUTE" -> current.plus(1, ChronoUnit.MINUTES);
            case "HOUR" -> current.plus(1, ChronoUnit.HOURS);
            case "DAY" -> current.plus(1, ChronoUnit.DAYS);
            default -> current.plus(1, ChronoUnit.HOURS);
        };
    }

    /**
     * 创建时间点数据
     */
    private ResponseTimeTrend.TimePoint createTimePoint(LocalDateTime timestamp, List<ServiceCallRecord> records) {
        if (records.isEmpty()) {
            return ResponseTimeTrend.TimePoint.builder()
                    .timestamp(timestamp)
                    .avgResponseTime(0.0)
                    .maxResponseTime(0L)
                    .minResponseTime(0L)
                    .callCount(0L)
                    .isAnomaly(false)
                    .build();
        }

        List<Long> responseTimes = records.stream()
                .filter(r -> r.getResponseTime() != null)
                .map(ServiceCallRecord::getResponseTime)
                .collect(Collectors.toList());

        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0L);

        return ResponseTimeTrend.TimePoint.builder()
                .timestamp(timestamp)
                .avgResponseTime(avgResponseTime)
                .maxResponseTime(maxResponseTime)
                .minResponseTime(minResponseTime)
                .callCount((long) records.size())
                .isAnomaly(false)
                .build();
    }

    /**
     * 计算趋势统计数据
     */
    private ResponseTimeTrend calculateTrendStatistics(String serviceName, String methodName,
                                                       String timeWindowType, LocalDateTime startTime,
                                                       LocalDateTime endTime,
                                                       List<ResponseTimeTrend.TimePoint> timePoints,
                                                       List<ServiceCallRecord> allRecords) {

        List<Double> allResponseTimes = allRecords.stream()
                .filter(r -> r.getResponseTime() != null)
                .map(r -> r.getResponseTime().doubleValue())
                .collect(Collectors.toList());

        double avgResponseTime = allResponseTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        long maxResponseTime = allResponseTimes.stream().mapToDouble(Double::doubleValue).mapToLong(d -> (long) d).max().orElse(0L);
        long minResponseTime = allResponseTimes.stream().mapToDouble(Double::doubleValue).mapToLong(d -> (long) d).min().orElse(0L);

        // 计算标准差
        double stdDev = calculateStandardDeviation(allResponseTimes, avgResponseTime);

        // 计算百分位数
        double p95 = calculatePercentile(allResponseTimes, 95);
        double p99 = calculatePercentile(allResponseTimes, 99);

        return ResponseTimeTrend.builder()
                .serviceName(serviceName)
                .methodName(methodName)
                .timeWindowType(timeWindowType)
                .startTime(startTime)
                .endTime(endTime)
                .timePoints(timePoints)
                .avgResponseTime(avgResponseTime)
                .maxResponseTime(maxResponseTime)
                .minResponseTime(minResponseTime)
                .responseTimeStdDev(stdDev)
                .totalDataPoints(timePoints.size())
                .p95ResponseTime(p95)
                .p99ResponseTime(p99)
                .build();
    }

    /**
     * 异常检测
     */
    private void detectAnomalies(ResponseTimeTrend trend) {
        if (trend.getTimePoints() == null || trend.getTimePoints().isEmpty()) {
            return;
        }

        double avgResponseTime = trend.getAvgResponseTime();
        double threshold = avgResponseTime * ANOMALY_MULTIPLIER;
        int anomalyCount = 0;

        for (ResponseTimeTrend.TimePoint point : trend.getTimePoints()) {
            if (point.getAvgResponseTime() > threshold) {
                point.setIsAnomaly(true);
                point.setAnomalyReason("响应时间异常高于平均值" + ANOMALY_MULTIPLIER + "倍");
                anomalyCount++;
            }
        }

        trend.setAnomalyCount(anomalyCount);
    }

    /**
     * 趋势模式分析
     */
    private void analyzeTrendPattern(ResponseTimeTrend trend) {
        List<ResponseTimeTrend.TimePoint> points = trend.getTimePoints();
        if (points.size() < 3) {
            trend.setTrendType("INSUFFICIENT_DATA");
            return;
        }

        // 简单的趋势分析：比较前半段和后半段的平均值
        int midPoint = points.size() / 2;
        double firstHalfAvg = points.subList(0, midPoint).stream()
                .mapToDouble(ResponseTimeTrend.TimePoint::getAvgResponseTime)
                .average().orElse(0.0);

        double secondHalfAvg = points.subList(midPoint, points.size()).stream()
                .mapToDouble(ResponseTimeTrend.TimePoint::getAvgResponseTime)
                .average().orElse(0.0);

        double changeRate = (secondHalfAvg - firstHalfAvg) / firstHalfAvg * 100;

        if (Math.abs(changeRate) < 10) {
            trend.setTrendType("STABLE");
        } else if (changeRate > 20) {
            trend.setTrendType("INCREASING");
        } else if (changeRate < -20) {
            trend.setTrendType("DECREASING");
        } else {
            trend.setTrendType("FLUCTUATING");
        }
    }

    /**
     * 性能等级评估
     */
    private void evaluatePerformanceLevel(ResponseTimeTrend trend) {
        double avgResponseTime = trend.getAvgResponseTime();

        if (avgResponseTime <= EXCELLENT_THRESHOLD) {
            trend.setPerformanceLevel("EXCELLENT");
        } else if (avgResponseTime <= GOOD_THRESHOLD) {
            trend.setPerformanceLevel("GOOD");
        } else if (avgResponseTime <= NORMAL_THRESHOLD) {
            trend.setPerformanceLevel("NORMAL");
        } else if (avgResponseTime <= POOR_THRESHOLD) {
            trend.setPerformanceLevel("POOR");
        } else {
            trend.setPerformanceLevel("CRITICAL");
        }
    }

    /**
     * 计算标准差
     */
    private double calculateStandardDeviation(List<Double> values, double mean) {
        if (values.size() <= 1) return 0.0;

        double sumSquaredDiff = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum();

        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }

    /**
     * 计算百分位数
     */
    private double calculatePercentile(List<Double> values, int percentile) {
        if (values.isEmpty()) return 0.0;

        List<Double> sorted = values.stream().sorted().collect(Collectors.toList());
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));

        return sorted.get(index);
    }

    /**
     * 创建空趋势对象
     */
    private ResponseTimeTrend createEmptyTrend(String serviceName, String methodName,
                                               String timeWindowType, LocalDateTime startTime,
                                               LocalDateTime endTime) {
        return ResponseTimeTrend.builder()
                .serviceName(serviceName)
                .methodName(methodName)
                .timeWindowType(timeWindowType)
                .startTime(startTime)
                .endTime(endTime)
                .timePoints(new ArrayList<>())
                .avgResponseTime(0.0)
                .maxResponseTime(0L)
                .minResponseTime(0L)
                .responseTimeStdDev(0.0)
                .trendType("NO_DATA")
                .performanceLevel("UNKNOWN")
                .anomalyCount(0)
                .totalDataPoints(0)
                .p95ResponseTime(0.0)
                .p99ResponseTime(0.0)
                .build();
    }

    /**
     * 创建错误趋势对象
     */
    private ResponseTimeTrend createErrorTrend(String serviceName, String methodName,
                                               String timeWindowType, String errorMessage) {
        return ResponseTimeTrend.builder()
                .serviceName(serviceName)
                .methodName(methodName)
                .timeWindowType(timeWindowType)
                .trendType("ERROR")
                .performanceLevel("ERROR")
                .build();
    }

    /**
     * 获取性能等级分数
     */
    private int getPerformanceLevelScore(String level) {
        return switch (level) {
            case "CRITICAL" -> 5;
            case "POOR" -> 4;
            case "NORMAL" -> 3;
            case "GOOD" -> 2;
            case "EXCELLENT" -> 1;
            default -> 0;
        };
    }
}
