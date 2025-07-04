package com.cloudDemo.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux配置类
 * 配置HTTP消息编解码器
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // 配置最大内存大小，默认256KB，这里设置为1MB
        configurer.defaultCodecs().maxInMemorySize(1024 * 1024);
    }
}
