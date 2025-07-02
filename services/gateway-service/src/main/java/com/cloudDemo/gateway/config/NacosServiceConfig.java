package com.cloudDemo.gateway.config;

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Nacos服务配置
 * 手动创建NamingService和ConfigService Bean
 */
@Configuration
public class NacosServiceConfig {

    @Value("${spring.cloud.nacos.discovery.server-addr:localhost:8848}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.discovery.namespace:}")
    private String namespace;

    @Bean
    public NamingService namingService() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.setProperty("namespace", namespace);
        }
        return NamingFactory.createNamingService(properties);
    }

    @Bean
    public ConfigService configService() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.setProperty("namespace", namespace);
        }
        return ConfigFactory.createConfigService(properties);
    }
}
