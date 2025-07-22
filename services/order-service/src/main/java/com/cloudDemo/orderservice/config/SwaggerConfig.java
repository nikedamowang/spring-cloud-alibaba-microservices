package com.cloudDemo.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger配置类
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("订单服务 API")
                        .description("CloudDemo微服务项目 - 订单服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CloudDemo Team")
                                .email("support@clouddemo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8000").description("本地开发环境"),
                        new Server().url("http://localhost:8080/order").description("通过网关访问")
                ));
    }
}
