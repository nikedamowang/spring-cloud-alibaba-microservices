package com.cloudDemo.config;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo负载均衡配置类
 * 配置多种负载均衡策略和故障处理
 */
@Configuration
public class DubboLoadBalancerConfig {

    /**
     * 消费者全局配置
     * 配置默认负载均衡策略
     */
    @Bean
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumer = new ConsumerConfig();

        // 设置默认负载均衡策略为轮询
        consumer.setLoadbalance("roundrobin");

        // 设置集群容错策略
        consumer.setCluster("failover");

        // 设置重试次数
        consumer.setRetries(2);

        // 设置超时时间
        consumer.setTimeout(5000);

        // 启用粘性连接（同一个消费者尽可能调用同一个提供者）
        consumer.setSticky(false);

        return consumer;
    }

    /**
     * 提供者全局配置
     * 配置服务提供者的负载均衡相关设置
     */
    @Bean
    public ProviderConfig providerConfig() {
        ProviderConfig provider = new ProviderConfig();

        // 设置默认超时时间
        provider.setTimeout(5000);

        // 设置默认重试次数
        provider.setRetries(2);

        // 设置集群容错策略
        provider.setCluster("failover");

        return provider;
    }

    /**
     * 应用配置
     * 配置服务治理相关参数
     */
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig application = new ApplicationConfig();

        // 启用QoS（Quality of Service）
        application.setQosEnable(true);
        application.setQosPort(22222);

        return application;
    }
}
