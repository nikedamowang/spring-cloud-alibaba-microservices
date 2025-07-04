# 修复DEFAULT_GROUP中的网关配置
$nacosUrl = "http://localhost:8848/nacos/v1/cs/configs"

# 完整的路由配置
$routeConfig = @"
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=0
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/order/**
          filters:
            - StripPrefix=0
      default-filters:
        - AddResponseHeader=X-Response-Time, now
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: false
"@

# 主配置文件（添加缺失的端口配置）
$mainConfig = @"
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    gateway:
      enabled: true

logging:
  level:
    org.springframework.cloud.gateway: INFO
    com.cloudDemo.gateway: INFO
    org.springframework.cloud.loadbalancer: WARN
    org.springframework.cloud.gateway.filter: DEBUG
    org.springframework.cloud.gateway.route: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: false
      default-filters:
        - AddResponseHeader=X-Response-Server, gateway-service
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

gateway:
  auth:
    skip-paths:
      - /api/user/login
      - /api/user/register
      - /api/user/list
      - /api/order/list
      - /admin/nacos/**
      - /actuator/**
      - /favicon.ico
  rate-limit:
    enabled: true
    default-burst-capacity: 100
    default-replenish-rate: 10
"@

# 上传路由配置
try {
    $body1 = @{
        dataId = "gateway-routes.yml"
        group = "DEFAULT_GROUP"
        content = $routeConfig
    }
    $response1 = Invoke-RestMethod -Uri $nacosUrl -Method POST -Body $body1 -ContentType "application/x-www-form-urlencoded"
    Write-Host "路由配置修复成功: $response1"
} catch {
    Write-Host "路由配置修复失败: $($_.Exception.Message)"
}

# 上传主配置
try {
    $body2 = @{
        dataId = "gateway-service-dev.yml"
        group = "DEFAULT_GROUP"
        content = $mainConfig
    }
    $response2 = Invoke-RestMethod -Uri $nacosUrl -Method POST -Body $body2 -ContentType "application/x-www-form-urlencoded"
    Write-Host "主配置修复成功: $response2"
} catch {
    Write-Host "主配置修复失败: $($_.Exception.Message)"
}

Write-Host "配置修复完成！请重启网关服务。"
