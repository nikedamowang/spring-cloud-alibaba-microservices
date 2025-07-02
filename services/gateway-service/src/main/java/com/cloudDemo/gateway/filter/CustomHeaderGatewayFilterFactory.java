package com.cloudDemo.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义请求头过滤器工厂
 */
@Slf4j
@Component
public class CustomHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomHeaderGatewayFilterFactory.Config> {

    public CustomHeaderGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 添加自定义请求头
            exchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header(config.getHeaderName(), config.getHeaderValue())
                            .build())
                    .build();

            log.debug("Added custom header: {} = {}", config.getHeaderName(), config.getHeaderValue());

            return chain.filter(exchange);
        };
    }

    @Data
    public static class Config {
        private String headerName;
        private String headerValue;
    }
}
