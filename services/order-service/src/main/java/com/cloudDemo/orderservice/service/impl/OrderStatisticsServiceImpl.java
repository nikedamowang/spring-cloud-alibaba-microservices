package com.cloudDemo.orderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudDemo.orderservice.entity.Orders;
import com.cloudDemo.orderservice.mapper.OrdersMapper;
import com.cloudDemo.orderservice.service.OrderStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单统计服务实现类
 * 基于数据库查询和Redis缓存的高性能统计分析
 */
@Slf4j
@Service
public class OrderStatisticsServiceImpl implements OrderStatisticsService {

    private static final String STATS_CACHE_PREFIX = "order:stats:";
    private static final int CACHE_EXPIRE_MINUTES = 5; // 统计结果缓存5分钟
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public OrderTimeWindowStats getRealtimeStats(int minutes) {
        String cacheKey = STATS_CACHE_PREFIX + "realtime:" + minutes;

        // 先查缓存
        OrderTimeWindowStats cachedStats = (OrderTimeWindowStats) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStats != null) {
            log.debug("从缓存获取{}分钟实时统计数据", minutes);
            return cachedStats;
        }

        // 计算时间窗口
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(minutes);

        // 查询订单数据
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startTime, endTime);
        List<Orders> orders = ordersMapper.selectList(queryWrapper);

        // 统计计算
        int totalOrders = orders.size();
        BigDecimal totalAmount = orders.stream()
                .map(Orders::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAmount = totalOrders > 0 ?
                totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        int uniqueUsers = (int) orders.stream()
                .map(Orders::getUserId)
                .distinct()
                .count();

        double ordersPerMinute = totalOrders > 0 ? (double) totalOrders / minutes : 0.0;

        String timeWindow = String.format("最近%d分钟 (%s ~ %s)",
                minutes,
                startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        OrderTimeWindowStats stats = new OrderTimeWindowStats(
                totalOrders, totalAmount, avgAmount, uniqueUsers, ordersPerMinute, timeWindow);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, stats, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("实时统计完成：{}分钟内订单{}笔，总金额{}", minutes, totalOrders, totalAmount);
        return stats;
    }

    @Override
    public Map<String, Long> getOrderStatusDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        String cacheKey = STATS_CACHE_PREFIX + "status_dist:" +
                startTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "_" +
                endTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        @SuppressWarnings("unchecked")
        Map<String, Long> cachedDist = (Map<String, Long>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedDist != null) {
            return cachedDist;
        }

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startTime, endTime);
        List<Orders> orders = ordersMapper.selectList(queryWrapper);

        Map<String, Long> statusDistribution = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus() != null ? order.getStatus() : "UNKNOWN",
                        Collectors.counting()
                ));

        // 确保所有状态都有值（即使为0）
        String[] allStatuses = {"PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED"};
        for (String status : allStatuses) {
            statusDistribution.putIfAbsent(status, 0L);
        }

        redisTemplate.opsForValue().set(cacheKey, statusDistribution, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return statusDistribution;
    }

    @Override
    public Map<Long, Integer> getUserActivityStats(int minutes, int topN) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time", startTime);
        List<Orders> orders = ordersMapper.selectList(queryWrapper);

        Map<Long, Integer> userActivityMap = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> (long) order.getUserId(),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        return userActivityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public OrderAmountStats getAmountStats(int minutes) {
        String cacheKey = STATS_CACHE_PREFIX + "amount:" + minutes;

        OrderAmountStats cachedStats = (OrderAmountStats) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStats != null) {
            return cachedStats;
        }

        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time", startTime);
        List<Orders> orders = ordersMapper.selectList(queryWrapper);

        if (orders.isEmpty()) {
            return new OrderAmountStats(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        List<BigDecimal> amounts = orders.stream()
                .map(Orders::getTotalAmount)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal totalAmount = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maxAmount = amounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal minAmount = amounts.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal avgAmount = amounts.isEmpty() ? BigDecimal.ZERO :
                totalAmount.divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);

        OrderAmountStats stats = new OrderAmountStats(totalAmount, maxAmount, minAmount, avgAmount, amounts.size());

        redisTemplate.opsForValue().set(cacheKey, stats, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return stats;
    }

    @Override
    public Map<Long, Integer> detectHighFrequencyUsers(int minutes, int threshold) {
        Map<Long, Integer> userActivity = getUserActivityStats(minutes, 1000); // 获取所有用户活动

        return userActivity.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Integer> getOrderTrends(int hours) {
        String cacheKey = STATS_CACHE_PREFIX + "trends:" + hours;

        @SuppressWarnings("unchecked")
        Map<String, Integer> cachedTrends = (Map<String, Integer>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTrends != null) {
            return cachedTrends;
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startTime, endTime);
        List<Orders> orders = ordersMapper.selectList(queryWrapper);

        // 按小时分组统计
        Map<String, Integer> hourlyStats = new LinkedHashMap<>();

        // 初始化所有小时为0
        for (int i = hours - 1; i >= 0; i--) {
            LocalDateTime hourTime = endTime.minusHours(i);
            String hourKey = hourTime.format(DateTimeFormatter.ofPattern("MM-dd HH:00"));
            hourlyStats.put(hourKey, 0);
        }

        // 统计每小时的订单数
        for (Orders order : orders) {
            if (order.getCreateTime() != null) {
                String hourKey = order.getCreateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:00"));
                hourlyStats.put(hourKey, hourlyStats.getOrDefault(hourKey, 0) + 1);
            }
        }

        redisTemplate.opsForValue().set(cacheKey, hourlyStats, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return hourlyStats;
    }
}
