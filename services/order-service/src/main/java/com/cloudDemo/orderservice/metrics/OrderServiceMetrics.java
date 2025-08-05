package com.cloudDemo.orderservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务自定义业务监控指标
 */
@Component
public class OrderServiceMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter orderCreationCounter;
    private Counter orderPaymentCounter;
    private Counter orderCancelCounter;
    private Timer orderProcessingTimer;
    private AtomicLong pendingOrdersGauge;

    @PostConstruct
    public void init() {
        // 订单创建计数器
        orderCreationCounter = Counter.builder("order.creation.count")
                .description("Total number of orders created")
                .tag("service", "order-service")
                .register(meterRegistry);

        // 订单支付计数器
        orderPaymentCounter = Counter.builder("order.payment.count")
                .description("Total number of orders paid")
                .tag("service", "order-service")
                .register(meterRegistry);

        // 订单取消计数器
        orderCancelCounter = Counter.builder("order.cancel.count")
                .description("Total number of orders cancelled")
                .tag("service", "order-service")
                .register(meterRegistry);

        // 订单处理响应时间
        orderProcessingTimer = Timer.builder("order.processing.duration")
                .description("Time taken to process orders")
                .tag("service", "order-service")
                .register(meterRegistry);

        // 待处理订单数量（实时指标）
        pendingOrdersGauge = new AtomicLong(0);
        Gauge.builder("order.pending.count", pendingOrdersGauge, AtomicLong::get)
                .description("Number of pending orders")
                .tag("service", "order-service")
                .register(meterRegistry);
    }

    public void incrementOrderCreation() {
        orderCreationCounter.increment();
        pendingOrdersGauge.incrementAndGet();
    }

    public void incrementOrderPayment() {
        orderPaymentCounter.increment();
        pendingOrdersGauge.decrementAndGet();
    }

    public void incrementOrderCancel() {
        orderCancelCounter.increment();
        pendingOrdersGauge.decrementAndGet();
    }

    public Timer.Sample startOrderProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopOrderProcessingTimer(Timer.Sample sample) {
        sample.stop(orderProcessingTimer);
    }

    public void setPendingOrdersCount(long count) {
        pendingOrdersGauge.set(count);
    }
}
