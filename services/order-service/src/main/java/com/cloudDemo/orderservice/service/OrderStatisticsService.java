package com.cloudDemo.orderservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单统计服务接口
 * 基于时间窗口的订单实时统计分析
 */
public interface OrderStatisticsService {

    /**
     * 实时订单统计（最近N分钟）
     *
     * @param minutes 时间窗口（分钟）
     * @return 统计结果
     */
    OrderTimeWindowStats getRealtimeStats(int minutes);

    /**
     * 订单状态分布统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 各状态订单数量分布
     */
    Map<String, Long> getOrderStatusDistribution(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 用户订单活跃度统计
     *
     * @param minutes 时间窗口（分钟）
     * @param topN    返回前N名活跃用户
     * @return 用户活跃度统计
     */
    Map<Long, Integer> getUserActivityStats(int minutes, int topN);

    /**
     * 订单金额统计
     *
     * @param minutes 时间窗口（分钟）
     * @return 金额统计结果
     */
    OrderAmountStats getAmountStats(int minutes);

    /**
     * 高频下单用户检测（防刷单）
     *
     * @param minutes   时间窗口（分钟）
     * @param threshold 阈值（订单数量）
     * @return 高频用户列表
     */
    Map<Long, Integer> detectHighFrequencyUsers(int minutes, int threshold);

    /**
     * 订单趋势分析（按小时统计）
     *
     * @param hours 最近N小时
     * @return 每小时订单数量趋势
     */
    Map<String, Integer> getOrderTrends(int hours);

    /**
     * 时间窗口订单统计结果
     */
    class OrderTimeWindowStats {
        private int totalOrders;           // 总订单数
        private BigDecimal totalAmount;    // 总金额
        private BigDecimal avgAmount;      // 平均金额
        private int uniqueUsers;           // 下单用户数
        private double ordersPerMinute;    // 每分钟订单数
        private String timeWindow;         // 时间窗口描述

        // 无参构造函数（Jackson反序列化需要）
        public OrderTimeWindowStats() {
        }

        // 带参构造函数
        public OrderTimeWindowStats(int totalOrders, BigDecimal totalAmount, BigDecimal avgAmount,
                                    int uniqueUsers, double ordersPerMinute, String timeWindow) {
            this.totalOrders = totalOrders;
            this.totalAmount = totalAmount;
            this.avgAmount = avgAmount;
            this.uniqueUsers = uniqueUsers;
            this.ordersPerMinute = ordersPerMinute;
            this.timeWindow = timeWindow;
        }

        // Getters
        public int getTotalOrders() {
            return totalOrders;
        }

        // Setters（Jackson反序列化需要）
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getAvgAmount() {
            return avgAmount;
        }

        public void setAvgAmount(BigDecimal avgAmount) {
            this.avgAmount = avgAmount;
        }

        public int getUniqueUsers() {
            return uniqueUsers;
        }

        public void setUniqueUsers(int uniqueUsers) {
            this.uniqueUsers = uniqueUsers;
        }

        public double getOrdersPerMinute() {
            return ordersPerMinute;
        }

        public void setOrdersPerMinute(double ordersPerMinute) {
            this.ordersPerMinute = ordersPerMinute;
        }

        public String getTimeWindow() {
            return timeWindow;
        }

        public void setTimeWindow(String timeWindow) {
            this.timeWindow = timeWindow;
        }
    }

    /**
     * 订单金额统计结果
     */
    class OrderAmountStats {
        private BigDecimal totalAmount;    // 总金额
        private BigDecimal maxAmount;      // 最大单笔金额
        private BigDecimal minAmount;      // 最小单笔金额
        private BigDecimal avgAmount;      // 平均金额
        private int orderCount;            // 订单数量

        // 无参构造函数（Jackson反序列化需要）
        public OrderAmountStats() {
        }

        public OrderAmountStats(BigDecimal totalAmount, BigDecimal maxAmount, BigDecimal minAmount,
                                BigDecimal avgAmount, int orderCount) {
            this.totalAmount = totalAmount;
            this.maxAmount = maxAmount;
            this.minAmount = minAmount;
            this.avgAmount = avgAmount;
            this.orderCount = orderCount;
        }

        // Getters
        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        // Setters（Jackson反序列化需要）
        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getMaxAmount() {
            return maxAmount;
        }

        public void setMaxAmount(BigDecimal maxAmount) {
            this.maxAmount = maxAmount;
        }

        public BigDecimal getMinAmount() {
            return minAmount;
        }

        public void setMinAmount(BigDecimal minAmount) {
            this.minAmount = minAmount;
        }

        public BigDecimal getAvgAmount() {
            return avgAmount;
        }

        public void setAvgAmount(BigDecimal avgAmount) {
            this.avgAmount = avgAmount;
        }

        public int getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(int orderCount) {
            this.orderCount = orderCount;
        }
    }
}
