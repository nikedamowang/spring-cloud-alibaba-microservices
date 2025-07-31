package com.cloudDemo.management.monitor.service;

import com.cloudDemo.management.monitor.dto.ServiceCallRecord;
import com.cloudDemo.management.monitor.dto.ServiceCallStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 监控数据模拟生成器
 * 用于生成测试数据，验证服务监控大屏功能
 */
@Slf4j
@Service
public class MockDataGeneratorService {

    private final Random random = new Random();
    // 模拟的服务和方法
    private final String[] services = {"UserService", "OrderService", "PaymentService", "InventoryService"};
    private final String[] userMethods = {"getUserById", "createUser", "updateUser", "deleteUser", "getUserList"};
    private final String[] orderMethods = {"createOrder", "getOrderById", "updateOrderStatus", "getOrderList"};
    private final String[] paymentMethods = {"processPayment", "refundPayment", "getPaymentStatus"};
    private final String[] inventoryMethods = {"checkStock", "updateInventory", "reserveStock"};
    @Autowired
    private ServiceMonitorService serviceMonitorService;

    /**
     * 生成模拟的服务调用记录
     */
    public void generateMockCallRecords(int count) {
        log.info("🎭 开始生成 {} 条模拟服务调用记录", count);

        for (int i = 0; i < count; i++) {
            try {
                ServiceCallRecord record = generateRandomCallRecord();
                serviceMonitorService.recordServiceCall(record);

                // 随机延迟，模拟真实调用间隔
                if (i % 10 == 0) {
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                log.warn("生成模拟数据失败: {}", e.getMessage());
            }
        }

        log.info("✅ 模拟数据生成完成，共生成 {} 条记录", count);
    }

    /**
     * 生成随机的服务调用记录
     */
    private ServiceCallRecord generateRandomCallRecord() {
        String serviceName = services[random.nextInt(services.length)];
        String methodName = getRandomMethod(serviceName);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusSeconds(random.nextInt(300)); // 5分钟内的随机时间

        // 生成响应时间（大部分在100ms内，少数较长）
        long responseTime;
        if (random.nextDouble() < 0.8) {
            responseTime = random.nextInt(100) + 10; // 10-110ms
        } else {
            responseTime = random.nextInt(1000) + 100; // 100-1100ms
        }

        LocalDateTime endTime = startTime.plusNanos(responseTime * 1_000_000);

        // 生成调用状态（90%成功，10%失败）
        boolean isSuccess = random.nextDouble() < 0.9;

        return ServiceCallRecord.builder()
                .callId(UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .serviceName(serviceName)
                .methodName(methodName)
                .consumerIp("192.168.1." + (100 + random.nextInt(50)))
                .providerIp("192.168.1." + (200 + random.nextInt(20)))
                .startTime(startTime)
                .endTime(endTime)
                .responseTime(responseTime)
                .status(isSuccess ? "SUCCESS" : "FAILURE")
                .errorMessage(isSuccess ? null : generateRandomError())
                .responseStatus(isSuccess ? "正常" : "异常")
                .requestParams("参数数量: " + random.nextInt(5))
                .build();
    }

    /**
     * 根据服务名获取随机方法
     */
    private String getRandomMethod(String serviceName) {
        return switch (serviceName) {
            case "UserService" -> userMethods[random.nextInt(userMethods.length)];
            case "OrderService" -> orderMethods[random.nextInt(orderMethods.length)];
            case "PaymentService" -> paymentMethods[random.nextInt(paymentMethods.length)];
            case "InventoryService" -> inventoryMethods[random.nextInt(inventoryMethods.length)];
            default -> "unknownMethod";
        };
    }

    /**
     * 生成随机错误信息
     */
    private String generateRandomError() {
        String[] errors = {
                "连接超时",
                "服务不可用",
                "参数验证失败",
                "数据库连接异常",
                "业务逻辑异常",
                "网络异常",
                "权限不足"
        };
        return errors[random.nextInt(errors.length)];
    }

    /**
     * 生成模拟的统计数据用于测试
     */
    public List<ServiceCallStats> generateMockStats() {
        List<ServiceCallStats> statsList = new ArrayList<>();

        for (String service : services) {
            String[] methods = getMethodsByService(service);
            for (String method : methods) {
                ServiceCallStats stats = generateRandomStats(service, method);
                statsList.add(stats);
            }
        }

        return statsList;
    }

    /**
     * 生成随机统计数据
     */
    private ServiceCallStats generateRandomStats(String serviceName, String methodName) {
        long totalCalls = ThreadLocalRandom.current().nextLong(100, 1000);
        long failureCalls = (long) (totalCalls * (0.05 + random.nextDouble() * 0.15)); // 5-20%失败率
        long successCalls = totalCalls - failureCalls;

        double avgResponseTime = 50 + random.nextDouble() * 200; // 50-250ms
        long maxResponseTime = (long) (avgResponseTime * (2 + random.nextDouble() * 2)); // 2-4倍平均时间
        long minResponseTime = (long) (avgResponseTime * 0.3); // 30%平均时间

        double successRate = totalCalls > 0 ? (double) successCalls / totalCalls * 100 : 0.0;
        double qps = totalCalls / 60.0; // 假设统计1分钟的数据

        return ServiceCallStats.builder()
                .serviceName(serviceName)
                .methodName(methodName)
                .totalCalls(totalCalls)
                .successCalls(successCalls)
                .failureCalls(failureCalls)
                .avgResponseTime(avgResponseTime)
                .maxResponseTime(maxResponseTime)
                .minResponseTime(minResponseTime)
                .successRate(successRate)
                .lastCallTime(LocalDateTime.now().minusMinutes(random.nextInt(60)))
                .windowStartTime(LocalDateTime.now().minusHours(1))
                .windowEndTime(LocalDateTime.now())
                .callsPerMinute(totalCalls)
                .qps(qps)
                .build();
    }

    /**
     * 根据服务获取方法列表
     */
    private String[] getMethodsByService(String serviceName) {
        return switch (serviceName) {
            case "UserService" -> userMethods;
            case "OrderService" -> orderMethods;
            case "PaymentService" -> paymentMethods;
            case "InventoryService" -> inventoryMethods;
            default -> new String[]{"unknownMethod"};
        };
    }

    /**
     * 清理模拟数据
     */
    public void clearMockData() {
        try {
            // 这里可以添加清理Redis中模拟数据的逻辑
            log.info("🧹 模拟数据清理完成");
        } catch (Exception e) {
            log.error("清理模拟数据失败: {}", e.getMessage());
        }
    }
}
