package com.cloudDemo.userservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 自定义业务监控指标
 */
@Component
public class UserServiceMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter userCreationCounter;
    private Counter userLoginCounter;
    private Counter userLoginFailureCounter;
    private Timer userQueryTimer;

    @PostConstruct
    public void init() {
        // 用户创建计数器
        userCreationCounter = Counter.builder("user.creation.count")
                .description("Total number of users created")
                .tag("service", "user-service")
                .register(meterRegistry);

        // 用户登录计数器
        userLoginCounter = Counter.builder("user.login.count")
                .description("Total number of successful logins")
                .tag("service", "user-service")
                .register(meterRegistry);

        // 用户登录失败计数器
        userLoginFailureCounter = Counter.builder("user.login.failure.count")
                .description("Total number of failed logins")
                .tag("service", "user-service")
                .register(meterRegistry);

        // 用户查询响应时间
        userQueryTimer = Timer.builder("user.query.duration")
                .description("Time taken to query users")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    public void incrementUserCreation() {
        userCreationCounter.increment();
    }

    public void incrementUserLogin() {
        userLoginCounter.increment();
    }

    public void incrementUserLoginFailure() {
        userLoginFailureCounter.increment();
    }

    public Timer.Sample startUserQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopUserQueryTimer(Timer.Sample sample) {
        sample.stop(userQueryTimer);
    }
}
