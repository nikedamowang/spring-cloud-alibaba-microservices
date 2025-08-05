package com.cloudDemo.managementservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理服务自定义业务监控指标
 */
@Component
public class ManagementServiceMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter configSyncCounter;
    private Counter configQueryCounter;
    private Counter serviceMonitorCounter;
    private Timer configSyncTimer;

    @PostConstruct
    public void init() {
        // 配置同步计数器
        configSyncCounter = Counter.builder("config.sync.count")
                .description("Total number of config sync operations")
                .tag("service", "management-service")
                .register(meterRegistry);

        // 配置查询计数器
        configQueryCounter = Counter.builder("config.query.count")
                .description("Total number of config query operations")
                .tag("service", "management-service")
                .register(meterRegistry);

        // 服务监控计数器
        serviceMonitorCounter = Counter.builder("service.monitor.count")
                .description("Total number of service monitoring operations")
                .tag("service", "management-service")
                .register(meterRegistry);

        // 配置同步响应时间
        configSyncTimer = Timer.builder("config.sync.duration")
                .description("Time taken to sync configurations")
                .tag("service", "management-service")
                .register(meterRegistry);
    }

    public void incrementConfigSync() {
        configSyncCounter.increment();
    }

    public void incrementConfigQuery() {
        configQueryCounter.increment();
    }

    public void incrementServiceMonitor() {
        serviceMonitorCounter.increment();
    }

    public Timer.Sample startConfigSyncTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopConfigSyncTimer(Timer.Sample sample) {
        sample.stop(configSyncTimer);
    }
}
