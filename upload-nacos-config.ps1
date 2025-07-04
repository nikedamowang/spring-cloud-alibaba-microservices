# 上传网关路由配置到Nacos
$nacosUrl = "http://localhost:8848/nacos/v1/cs/configs"
$configContent = @"
spring:
  cloud:
    gateway:
      routes:
        # 用户服务路由
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=0

        # 订单服务路由
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/order/**
          filters:
            - StripPrefix=0

      # 全局过滤器配置
      default-filters:
        - AddResponseHeader=X-Response-Time, now

      # 跨域配置
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

$body = @{
    dataId = "gateway-routes.yml"
    group = "GATEWAY_GROUP"
    content = $configContent
}

try {
    $response = Invoke-RestMethod -Uri $nacosUrl -Method POST -Body $body -ContentType "application/x-www-form-urlencoded"
    Write-Host "配置上传成功: $response"
} catch {
    Write-Host "配置上传失败: $($_.Exception.Message)"
}
