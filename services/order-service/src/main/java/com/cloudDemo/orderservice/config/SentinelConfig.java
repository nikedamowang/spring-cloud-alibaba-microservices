package com.cloudDemo.orderservice.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务Sentinel熔断降级配置类
 */
@Configuration
public class SentinelConfig {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 订单服务流控规则
        FlowRule orderServiceRule = new FlowRule();
        orderServiceRule.setResource("order-service");
        orderServiceRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderServiceRule.setCount(80); // QPS限制为80
        orderServiceRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        orderServiceRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(orderServiceRule);

        // 创建订单接口流控规则
        FlowRule createOrderRule = new FlowRule();
        createOrderRule.setResource("createOrder");
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        createOrderRule.setCount(30); // QPS限制为30
        createOrderRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        createOrderRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(createOrderRule);

        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 订单服务熔断规则 - 异常数量
        DegradeRule orderServiceDegradeRule = new DegradeRule();
        orderServiceDegradeRule.setResource("order-service");
        orderServiceDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        orderServiceDegradeRule.setCount(10); // 异常数量阈值 10个
        orderServiceDegradeRule.setTimeWindow(60); // 熔断时长 60秒
        orderServiceDegradeRule.setMinRequestAmount(5); // 最小请求数
        orderServiceDegradeRule.setStatIntervalMs(60000); // 统计时长 60秒
        rules.add(orderServiceDegradeRule);

        // 创建订单接口熔断规则 - RT响应时间
        DegradeRule createOrderDegradeRule = new DegradeRule();
        createOrderDegradeRule.setResource("createOrder");
        createOrderDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        createOrderDegradeRule.setCount(2000); // 平均响应时间阈值 2000ms
        createOrderDegradeRule.setTimeWindow(30); // 熔断时长 30秒
        createOrderDegradeRule.setMinRequestAmount(5); // 最小请求数
        createOrderDegradeRule.setStatIntervalMs(1000); // 统计时长 1秒
        rules.add(createOrderDegradeRule);

        DegradeRuleManager.loadRules(rules);
    }
}
