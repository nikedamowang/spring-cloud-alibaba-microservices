package com.cloudDemo.gateway.test;

import com.cloudDemo.common.logging.LogContextManager;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

/**
 * 测试类 - 验证所有依赖都能正确导入
 * 如果这个类能编译通过，说明所有依赖问题都已解决
 */
public class LoggingTestClass {

    public void testLogContextManager() {
        // 测试LogContextManager是否可用
        LogContextManager.initTrace();
        String traceId = LogContextManager.getCurrentTraceId();
        LogContextManager.clear();
        System.out.println("LogContextManager测试通过: " + traceId);
    }

    public void testGatewayDependencies() {
        // 测试Gateway相关依赖是否可用
        System.out.println("Gateway依赖测试:");
        System.out.println("- GatewayFilterChain: " + GatewayFilterChain.class.getName());
        System.out.println("- GlobalFilter: " + GlobalFilter.class.getName());
        System.out.println("- Mono: " + Mono.class.getName());
    }
}
