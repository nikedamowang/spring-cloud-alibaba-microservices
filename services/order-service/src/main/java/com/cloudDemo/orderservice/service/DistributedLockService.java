package com.cloudDemo.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class DistributedLockService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 执行带分布式锁的操作
     *
     * @param lockKey   锁的键
     * @param waitTime  等待时间（秒）
     * @param leaseTime 锁持有时间（秒）
     * @param supplier  需要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("获取分布式锁失败，锁键: {}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

            log.info("成功获取分布式锁，锁键: {}", lockKey);
            // 执行业务操作
            return supplier.get();

        } catch (InterruptedException e) {
            log.error("获取分布式锁被中断，锁键: {}", lockKey, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("操作被中断");
        } catch (Exception e) {
            log.error("执行分布式锁操作失败，锁键: {}", lockKey, e);
            throw new RuntimeException("操作执行失败: " + e.getMessage());
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放分布式锁，锁键: {}", lockKey);
            }
        }
    }

    /**
     * 简化版本，使用默认超时时间
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, 10, 30, supplier);
    }

    /**
     * 无返回值的锁操作
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 生成库存锁的键
     */
    public String getInventoryLockKey(String productId) {
        return "inventory:lock:" + productId;
    }
}
