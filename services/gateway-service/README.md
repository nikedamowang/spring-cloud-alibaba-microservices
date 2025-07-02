# Spring Cloud Gateway 网关服务

## 项目概述

这是一个基于Spring Cloud Gateway的API网关服务，提供了路由转发、负载均衡、熔断降级、限流、认证等功能。

## 主要功能

### 1. 路由转发

- 支持基于路径的路由规则
- 自动服务发现和负载均衡
- 支持动态路由配置

### 2. 安全认证

- JWT Token验证
- 白名单路径配置
- 统一认证拦截

### 3. 熔断降级

- 基于Resilience4j的熔断器
- 自定义降级处理
- 服务健康监控

### 4. 限流保护

- 基于IP和用户的限流
- 滑动窗口算法
- 可配置限流规则

### 5. 请求日志

- 全链路请求日志记录
- 响应时间统计
- 异常日志记录

## 配置说明

### 服务端口

- 默认端口：8080
- 测试端口：8081

### 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=2
            - CircuitBreaker
```

### 熔断器配置

```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service-cb:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
```

## API接口

### 健康检查

```
GET /gateway/health
```

### 获取服务列表

```
GET /gateway/services
```

### 获取路由信息

```
GET /gateway/routes
```

## 使用方法

### 1. 启动网关服务

```bash
java -jar gateway-service.jar
```

### 2. 访问后端服务

- 用户服务：http://localhost:8080/api/user/xxx
- 订单服务：http://localhost:8080/api/order/xxx

### 3. 认证请求

请求头中添加：

```
Authorization: Bearer <your-jwt-token>
```

## 过滤器说明

### 全局过滤器

1. **GlobalLogFilter**：请求日志记录
2. **AuthFilter**：认证验证
3. **RateLimitFilter**：限流保护

### 自定义过滤器

- **CustomHeaderGatewayFilterFactory**：添加自定义请求头

## 监控和运维

### 健康检查

访问 `/gateway/health` 查看服务状态

### 路由信息

访问 `/gateway/routes` 查看当前路由配置

### 日志级别

可通过配置文件调整不同组件的日志级别：

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.cloudDemo.gateway: DEBUG
    io.github.resilience4j: DEBUG
```

## 注意事项

1. 确保Nacos服务已启动
2. 后端服务需要注册到同一个Nacos集群
3. JWT Token验证需要与后端服务保持一致
4. 熔断器参数需要根据实际业务调整
5. 限流规则需要根据系统容量配置
