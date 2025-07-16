package com.cloudDemo.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo负载均衡高级配置
 * 配置权重、粘性会话和自定义负载均衡策略
 */
@Configuration
@EnableDubbo
public class DubboAdvancedLoadBalancerConfig {

    /**
     * Dubbo负载均衡策略说明：
     *
     * 1. random (随机，默认)
     *    - 随机选择提供者
     *    - 在调用量比较大时，各个提供者调用次数比较均匀
     *
     * 2. roundrobin (轮询)
     *    - 轮询选择提供者
     *    - 存在慢的提供者累积请求的问题
     *
     * 3. leastactive (最少活跃调用)
     *    - 最少活跃调用数，相同活跃数的随机
     *    - 活跃数指调用前后计数差，使慢的提供者收到更少请求
     *
     * 4. shortestresponse (最短响应)
     *    - 最短响应优先，响应越快的提供者越容易被选择
     *
     * 5. consistenthash (一致性Hash)
     *    - 相同参数的请求总是发到同一提供者
     *    - 适用于有状态服务
     *
     * 6. adaptivesample (自适应)
     *    - 自适应负载均衡，根据提供者的响应时间自动选择
     */

    /**
     * 集群容错策略说明：
     *
     * 1. failover (失败自动切换，默认)
     *    - 失败自动切换到其他服务器
     *    - 通常用于读操作，但重试会带来更长延迟
     *
     * 2. failfast (快速失败)
     *    - 快速失败，只发起一次调用，失败立即报错
     *    - 通常用于非幂等性的写操作
     *
     * 3. failsafe (失败安全)
     *    - 失败安全，出现异常时忽略
     *    - 通常用于写入审计日志等操作
     *
     * 4. failback (失败自动恢复)
     *    - 失败自动恢复，后台记录失败请求，定时重发
     *    - 通常用于消息通知操作
     *
     * 5. forking (并行调用)
     *    - 并行调用多个服务器，只要一个成功即返回
     *    - 通常用于实时性要求较高的读操作
     *
     * 6. broadcast (广播调用)
     *    - 广播调用所有提供者，逐个调用
     *    - 通常用于通知所有提供者更新缓存或日志等本地资源信息
     */
}
