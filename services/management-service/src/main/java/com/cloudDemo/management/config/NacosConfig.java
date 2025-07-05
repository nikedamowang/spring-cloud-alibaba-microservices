package com.cloudDemo.management.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Nacos 配置类
 */
@Configuration
public class NacosConfig {

    @Value("${nacos.server-addr:localhost:8848}")
    private String serverAddr;

    @Value("${nacos.namespace:}")
    private String namespace;

    @Bean
    public ConfigService configService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }

        return NacosFactory.createConfigService(properties);
    }

    @Bean
    public NamingService namingService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }

        return NacosFactory.createNamingService(properties);
    }
}
