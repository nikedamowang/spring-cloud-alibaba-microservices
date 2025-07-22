package com.cloudDemo.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.*;

/**
 * Gateway Sentinel熔断降级配置类
 */
@Configuration
public class SentinelGatewayConfig {

    /**
     * 配置SentinelGatewayBlockExceptionHandler，限流后异常处理
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler(
            ObjectProvider<List<ViewResolver>> viewResolversProvider,
            ServerCodecConfigurer serverCodecConfigurer) {
        return new SentinelGatewayBlockExceptionHandler(
                viewResolversProvider.getIfAvailable(Collections::emptyList),
                serverCodecConfigurer);
    }

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void doInit() {
        // 加载网关限流规则
        initGatewayRules();
        // 加载自定义限流异常处理器
        initBlockHandler();
    }

    /**
     * 网关限流规则
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 用户服务路由限流规则
        GatewayFlowRule userServiceRule = new GatewayFlowRule("user-service")
                .setCount(100) // QPS阈值
                .setIntervalSec(1); // 统计时间窗口，单位是秒，默认是 1 秒
        rules.add(userServiceRule);

        // 订单服务路由限流规则
        GatewayFlowRule orderServiceRule = new GatewayFlowRule("order-service")
                .setCount(80) // QPS阈值
                .setIntervalSec(1);
        rules.add(orderServiceRule);

        // 管理服务路由限流规则
        GatewayFlowRule managementServiceRule = new GatewayFlowRule("management-service")
                .setCount(50) // QPS阈值
                .setIntervalSec(1);
        rules.add(managementServiceRule);

        GatewayRuleManager.loadRules(rules);
    }

    /**
     * 自定义限流异常处理器
     */
    private void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = (exchange, ex) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后重试");
            result.put("timestamp", System.currentTimeMillis());

            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(result));
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
