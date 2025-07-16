package com.cloudDemo.test;

import com.cloudDemo.api.dto.UserDTO;
import com.cloudDemo.orderservice.service.UserRemoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dubbo负载均衡测试类
 * 验证不同负载均衡策略的效果
 */
@SpringBootTest
public class DubboLoadBalancerTest {

    @Autowired
    private UserRemoteService userRemoteService;

    /**
     * 测试轮询负载均衡
     */
    @Test
    public void testRoundRobinLoadBalance() throws InterruptedException {
        System.out.println("=== 测试轮询负载均衡 ===");

        int threadCount = 10;
        int requestsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            UserDTO user = userRemoteService.getUserById(1L);
                            if (user != null) {
                                successCount.incrementAndGet();
                                System.out.println(String.format("Thread-%d Request-%d: 成功获取用户 %s",
                                        threadId, j, user.getUsername()));
                            } else {
                                failCount.incrementAndGet();
                                System.out.println(String.format("Thread-%d Request-%d: 获取用户失败", threadId, j));
                            }
                            Thread.sleep(100); // 模拟处理时间
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                            System.out.println(String.format("Thread-%d Request-%d: 异常 %s",
                                    threadId, j, e.getMessage()));
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println(String.format("轮询负载均衡测试完成: 成功=%d, 失败=%d, 总计=%d",
                successCount.get(), failCount.get(), threadCount * requestsPerThread));
    }

    /**
     * 测试最少活跃调用负载均衡
     */
    @Test
    public void testLeastActiveLoadBalance() throws InterruptedException {
        System.out.println("=== 测试最少活跃调用负载均衡 ===");

        int requestCount = 50;
        CountDownLatch latch = new CountDownLatch(requestCount);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < requestCount; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    UserDTO user = userRemoteService.validateUser("testuser", "password");
                    long responseTime = System.currentTimeMillis() - startTime;

                    if (user != null) {
                        successCount.incrementAndGet();
                        System.out.println(String.format("Request-%d: 验证成功, 响应时间=%dms",
                                requestId, responseTime));
                    } else {
                        System.out.println(String.format("Request-%d: 验证失败, 响应时间=%dms",
                                requestId, responseTime));
                    }
                } catch (Exception e) {
                    System.out.println(String.format("Request-%d: 异常 %s", requestId, e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println(String.format("最少活跃调用测试完成: 成功=%d, 总计=%d",
                successCount.get(), requestCount));
    }

    /**
     * 测试服务可用性
     */
    @Test
    public void testServiceAvailability() {
        System.out.println("=== 测试服务可用性 ===");

        try {
            // 测试用户查询服务
            UserDTO user = userRemoteService.getUserById(1L);
            System.out.println("用户查询服务: " + (user != null ? "可用" : "不可用"));

            // 测试用户验证服务
            UserDTO validatedUser = userRemoteService.validateUser("admin", "123456");
            System.out.println("用户验证服务: " + (validatedUser != null ? "可用" : "不可用"));

            // 测试用户存在性检查
            boolean exists = userRemoteService.userExists(1L);
            System.out.println("用户存在性检查: " + (exists ? "可用" : "不可用"));

        } catch (Exception e) {
            System.err.println("服务可用性测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试故障转移
     */
    @Test
    public void testFailoverMechanism() {
        System.out.println("=== 测试故障转移机制 ===");

        // 模拟多次调用，观察故障转移效果
        for (int i = 0; i < 10; i++) {
            try {
                UserDTO user = userRemoteService.getUserById(999L); // 使用一个可能不存在的ID
                System.out.println(String.format("调用-%d: %s", i,
                        user != null ? "成功" : "返回空结果"));
            } catch (Exception e) {
                System.out.println(String.format("调用-%d: 异常 %s", i, e.getMessage()));
            }
        }
    }
}
