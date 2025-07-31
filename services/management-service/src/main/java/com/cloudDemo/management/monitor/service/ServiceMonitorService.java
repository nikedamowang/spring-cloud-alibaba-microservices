package com.cloudDemo.management.monitor.service;

import com.cloudDemo.management.monitor.dto.ServiceCallRecord;
import com.cloudDemo.management.monitor.dto.ServiceCallStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 服务监控服务
 * 负责处理服务调用统计数据的收集、存储和查询
 */
@Slf4j
@Service
public class ServiceMonitorService {

    // Redis键前缀
    private static final String CALL_STATS_PREFIX = "service:stats:";
    private static final String CALL_RECORDS_PREFIX = "service:records:";
    private static final String RECENT_CALLS_KEY = "service:recent:calls";
    // 时间格式化器
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm");
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 记录服务调用信息
     */
    public void recordServiceCall(ServiceCallRecord record) {
        try {
            // 1. 保存调用记录到Redis（保留最近1000条）
            saveCallRecord(record);

            // 2. 更新统计数据
            updateCallStats(record);

            log.debug("✅ 服务调用统计已记录 - 服务: {}, 方法: {}, 状态: {}",
                    record.getServiceName(), record.getMethodName(), record.getStatus());

        } catch (Exception e) {
            log.error("❌ 记录服务调用统计失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存调用记录
     */
    private void saveCallRecord(ServiceCallRecord record) {
        try {
            // 使用List保存最近的调用记录，限制大小为1000
            redisTemplate.opsForList().leftPush(RECENT_CALLS_KEY, record);
            redisTemplate.opsForList().trim(RECENT_CALLS_KEY, 0, 999);
            redisTemplate.expire(RECENT_CALLS_KEY, 24, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("保存调用记录失败: {}", e.getMessage());
        }
    }

    /**
     * 更新统计数据
     */
    private void updateCallStats(ServiceCallRecord record) {
        String serviceMethod = record.getServiceName() + ":" + record.getMethodName();
        String hourKey = CALL_STATS_PREFIX + serviceMethod + ":" +
                record.getStartTime().format(HOUR_FORMATTER);

        try {
            // 使用RedisCallback明确指定回调类型
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                // 总调用次数
                redisTemplate.opsForHash().increment(hourKey, "totalCalls", 1);

                // 成功/失败次数
                if ("SUCCESS".equals(record.getStatus())) {
                    redisTemplate.opsForHash().increment(hourKey, "successCalls", 1);
                } else {
                    redisTemplate.opsForHash().increment(hourKey, "failureCalls", 1);
                }

                // 响应时间统计
                updateResponseTimeStats(hourKey, record.getResponseTime());

                // 设置过期时间（24小时）
                redisTemplate.expire(hourKey, 24, TimeUnit.HOURS);

                // 保存服务和方法信息
                redisTemplate.opsForHash().put(hourKey, "serviceName", record.getServiceName());
                redisTemplate.opsForHash().put(hourKey, "methodName", record.getMethodName());
                redisTemplate.opsForHash().put(hourKey, "lastCallTime",
                        record.getStartTime().toString());

                return null;
            });

        } catch (Exception e) {
            log.error("更新统计数据失败: {}", e.getMessage());
        }
    }

    /**
     * 更新响应时间统计
     */
    private void updateResponseTimeStats(String hourKey, Long responseTime) {
        try {
            // 获取当前最大值和最小值
            Object maxObj = redisTemplate.opsForHash().get(hourKey, "maxResponseTime");
            Object minObj = redisTemplate.opsForHash().get(hourKey, "minResponseTime");
            Object totalTimeObj = redisTemplate.opsForHash().get(hourKey, "totalResponseTime");

            long maxTime = maxObj != null ? Long.parseLong(maxObj.toString()) : responseTime;
            long minTime = minObj != null ? Long.parseLong(minObj.toString()) : responseTime;
            long totalTime = totalTimeObj != null ? Long.parseLong(totalTimeObj.toString()) : 0L;

            // 更新最大值和最小值
            redisTemplate.opsForHash().put(hourKey, "maxResponseTime", Math.max(maxTime, responseTime));
            redisTemplate.opsForHash().put(hourKey, "minResponseTime", Math.min(minTime, responseTime));
            redisTemplate.opsForHash().increment(hourKey, "totalResponseTime", responseTime);

        } catch (Exception e) {
            log.error("更新响应时间统计失败: {}", e.getMessage());
        }
    }

    /**
     * 获取所有服务的调用统计
     */
    public List<ServiceCallStats> getAllServiceStats() {
        try {
            Set<String> keys = redisTemplate.keys(CALL_STATS_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return new ArrayList<>();
            }

            Map<String, ServiceCallStats> statsMap = new HashMap<>();

            for (String key : keys) {
                ServiceCallStats stats = buildStatsFromRedis(key);
                if (stats != null) {
                    String serviceMethod = stats.getServiceName() + ":" + stats.getMethodName();

                    // 合并同一服务方法的统计数据
                    if (statsMap.containsKey(serviceMethod)) {
                        ServiceCallStats existing = statsMap.get(serviceMethod);
                        mergeStats(existing, stats);
                    } else {
                        statsMap.put(serviceMethod, stats);
                    }
                }
            }

            return new ArrayList<>(statsMap.values());

        } catch (Exception e) {
            log.error("获取服务统计失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 从Redis构建统计对象
     */
    private ServiceCallStats buildStatsFromRedis(String key) {
        try {
            Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
            if (data.isEmpty()) {
                return null;
            }

            long totalCalls = getLongValue(data, "totalCalls", 0L);
            long successCalls = getLongValue(data, "successCalls", 0L);
            long failureCalls = getLongValue(data, "failureCalls", 0L);
            long totalResponseTime = getLongValue(data, "totalResponseTime", 0L);

            double avgResponseTime = totalCalls > 0 ? (double) totalResponseTime / totalCalls : 0.0;
            double successRate = totalCalls > 0 ? (double) successCalls / totalCalls * 100 : 0.0;

            return ServiceCallStats.builder()
                    .serviceName(getStringValue(data, "serviceName", "Unknown"))
                    .methodName(getStringValue(data, "methodName", "Unknown"))
                    .totalCalls(totalCalls)
                    .successCalls(successCalls)
                    .failureCalls(failureCalls)
                    .avgResponseTime(avgResponseTime)
                    .maxResponseTime(getLongValue(data, "maxResponseTime", 0L))
                    .minResponseTime(getLongValue(data, "minResponseTime", 0L))
                    .successRate(successRate)
                    .lastCallTime(parseDateTime(getStringValue(data, "lastCallTime", null)))
                    .build();

        } catch (Exception e) {
            log.error("构建统计对象失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 合并统计数据
     */
    private void mergeStats(ServiceCallStats existing, ServiceCallStats newStats) {
        existing.setTotalCalls(existing.getTotalCalls() + newStats.getTotalCalls());
        existing.setSuccessCalls(existing.getSuccessCalls() + newStats.getSuccessCalls());
        existing.setFailureCalls(existing.getFailureCalls() + newStats.getFailureCalls());

        // 更新响应时间
        existing.setMaxResponseTime(Math.max(existing.getMaxResponseTime(), newStats.getMaxResponseTime()));
        existing.setMinResponseTime(Math.min(existing.getMinResponseTime(), newStats.getMinResponseTime()));

        // 重新计算平均响应时间和成功率
        long totalCalls = existing.getTotalCalls();
        if (totalCalls > 0) {
            double totalTime = existing.getAvgResponseTime() * existing.getTotalCalls() +
                    newStats.getAvgResponseTime() * newStats.getTotalCalls();
            existing.setAvgResponseTime(totalTime / totalCalls);
            existing.setSuccessRate((double) existing.getSuccessCalls() / totalCalls * 100);
        }

        // 更新最后调用时间
        if (newStats.getLastCallTime() != null &&
                (existing.getLastCallTime() == null ||
                        newStats.getLastCallTime().isAfter(existing.getLastCallTime()))) {
            existing.setLastCallTime(newStats.getLastCallTime());
        }
    }

    /**
     * 获取最近的调用记录
     */
    public List<ServiceCallRecord> getRecentCallRecords(int limit) {
        try {
            List<Object> records = redisTemplate.opsForList().range(RECENT_CALLS_KEY, 0, limit - 1);
            if (records == null) {
                return new ArrayList<>();
            }

            return records.stream()
                    .filter(obj -> obj instanceof ServiceCallRecord)
                    .map(obj -> (ServiceCallRecord) obj)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取最近调用记录失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // 辅助方法
    private long getLongValue(Map<Object, Object> data, String key, long defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String getStringValue(Map<Object, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return null;
        }
    }
}
