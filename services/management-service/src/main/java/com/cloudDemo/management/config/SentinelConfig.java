package com.cloudDemo.management.config;

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
 * 管理服务Sentinel熔断降级配置类
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

        // 管理服务流控规则
        FlowRule managementServiceRule = new FlowRule();
        managementServiceRule.setResource("management-service");
        managementServiceRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        managementServiceRule.setCount(50); // QPS限制为50
        managementServiceRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        managementServiceRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(managementServiceRule);

        // 配置管理接口流控规则
        FlowRule configManageRule = new FlowRule();
        configManageRule.setResource("configManage");
        configManageRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        configManageRule.setCount(20); // QPS限制为20
        configManageRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        configManageRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(configManageRule);

        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 管理服务熔断规则 - 异常比例
        DegradeRule managementServiceDegradeRule = new DegradeRule();
        managementServiceDegradeRule.setResource("management-service");
        managementServiceDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        managementServiceDegradeRule.setCount(0.3); // 异常比例阈值 30%
        managementServiceDegradeRule.setTimeWindow(60); // 熔断时长 60秒
        managementServiceDegradeRule.setMinRequestAmount(5); // 最小请求数
        managementServiceDegradeRule.setStatIntervalMs(1000); // 统计时长 1秒
        rules.add(managementServiceDegradeRule);

        // 配置管理接口熔断规则 - RT响应时间
        DegradeRule configManageDegradeRule = new DegradeRule();
        configManageDegradeRule.setResource("configManage");
        configManageDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        configManageDegradeRule.setCount(3000); // 平均响应时间阈值 3000ms
        configManageDegradeRule.setTimeWindow(45); // 熔断时长 45秒
        configManageDegradeRule.setMinRequestAmount(5); // 最小请求数
        configManageDegradeRule.setStatIntervalMs(1000); // 统计时长 1秒
        rules.add(configManageDegradeRule);

        DegradeRuleManager.loadRules(rules);
    }
}
