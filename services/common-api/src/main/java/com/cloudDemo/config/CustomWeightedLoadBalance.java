package com.cloudDemo.config;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 自定义权重负载均衡实现
 * 支持动态权重调整和健康检查
 */
public class CustomWeightedLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "customweighted";

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        if (length == 1) {
            return invokers.get(0);
        }

        // 计算总权重和所有权重是否相同
        boolean sameWeight = true;
        int[] weights = new int[length];
        int totalWeight = 0;

        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            weights[i] = weight;
            totalWeight += weight;
            if (sameWeight && i > 0 && weight != weights[i - 1]) {
                sameWeight = false;
            }
        }

        // 如果权重不相同且总权重大于0，则按权重随机选择
        if (totalWeight > 0 && !sameWeight) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int i = 0; i < length; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }

        // 权重相同或总权重为0，随机选择
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }

    /**
     * 获取调用者权重
     * 支持动态权重调整
     */
    @Override
    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        int weight = super.getWeight(invoker, invocation);

        // 根据服务健康状态调整权重
        if (weight > 0) {
            // 检查服务健康状态
            if (isHealthy(invoker)) {
                return weight;
            } else {
                // 不健康的服务降低权重
                return Math.max(1, weight / 4);
            }
        }

        return weight;
    }

    /**
     * 检查服务实例健康状态
     * 可以根据实际需要扩展健康检查逻辑
     */
    private boolean isHealthy(Invoker<?> invoker) {
        // 简单的健康检查：检查invoker是否可用
        return invoker.isAvailable();
    }
}
