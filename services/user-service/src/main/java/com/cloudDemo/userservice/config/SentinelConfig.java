package com.cloudDemo.userservice.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel配置类
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
        initNacosDataSource();
    }

    /**
     * 初始化流控规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 用户登录接口流控规则
        FlowRule loginRule = new FlowRule();
        loginRule.setResource("userLogin");
        loginRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        loginRule.setCount(10); // QPS限制为10
        rules.add(loginRule);

        // 用户信息查询接口流控规则
        FlowRule userInfoRule = new FlowRule();
        userInfoRule.setResource("getUserInfo");
        userInfoRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userInfoRule.setCount(20); // QPS限制为20
        rules.add(userInfoRule);

        // 用户注册接口流控规则
        FlowRule registerRule = new FlowRule();
        registerRule.setResource("userRegister");
        registerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        registerRule.setCount(5); // QPS限制为5
        rules.add(registerRule);

        FlowRuleManager.loadRules(rules);
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 用户登录接口熔断规则
        DegradeRule loginDegradeRule = new DegradeRule();
        loginDegradeRule.setResource("userLogin");
        loginDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        loginDegradeRule.setCount(0.5); // 异常比例阈值50%
        loginDegradeRule.setTimeWindow(10); // 熔断时长10秒
        loginDegradeRule.setMinRequestAmount(5); // 最小请求数
        rules.add(loginDegradeRule);

        // 用户信息查询接口熔断规则
        DegradeRule userInfoDegradeRule = new DegradeRule();
        userInfoDegradeRule.setResource("getUserInfo");
        userInfoDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        userInfoDegradeRule.setCount(1000); // 平均响应时间阈值1000ms
        userInfoDegradeRule.setTimeWindow(10); // 熔断时长10秒
        userInfoDegradeRule.setMinRequestAmount(5); // 最小请求数
        rules.add(userInfoDegradeRule);

        DegradeRuleManager.loadRules(rules);
    }

    /**
     * 初始化Nacos数据源
     */
    private void initNacosDataSource() {
        try {
            // 流控规则数据源
            ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(
                    "127.0.0.1:8848",
                    "DEFAULT_GROUP",
                    "user-service-flow-rules",
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                    })
            );
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

            // 熔断规则数据源
            ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(
                    "127.0.0.1:8848",
                    "DEFAULT_GROUP",
                    "user-service-degrade-rules",
                    source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
                    })
            );
            DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        } catch (Exception e) {
            System.err.println("初始化Nacos数据源失败: " + e.getMessage());
        }
    }
}
