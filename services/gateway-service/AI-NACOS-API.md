# AI专用 - Nacos信息查询接口文档

## 概述

本文档专为AI设计，提供了一系列实时获取Nacos服务注册和配置信息的接口。这些接口可以帮助AI快速了解系统当前状态，无需人工手动提供信息。

## 基本信息

- **基础URL**: `http://localhost:8080/admin/nacos`
- **认证**: 无需认证（已加入白名单）
- **数据格式**: JSON
- **更新频率**: 实时获取，无缓存

## 核心接口

### 1. 获取完整快照 (推荐使用)

**接口**: `GET /admin/nacos/snapshot`

**用途**: 一次性获取所有服务和配置信息，最适合AI使用

**返回数据结构**:
```json
{
  "timestamp": 1751426940138,
  "success": true,
  "services": {
    "user-service": {
      "instances": [
        {
          "ip": "192.168.244.1",
          "port": 9000,
          "healthy": true,
          "enabled": true,
          "weight": 1.0,
          "metadata": {
            "preserved.register.source": "SPRING_CLOUD"
          }
        }
      ],
      "instanceCount": 1
    },
    "order-service": {
      "instances": [
        {
          "ip": "192.168.244.1", 
          "port": 8000,
          "healthy": true,
          "enabled": true,
          "weight": 1.0
        }
      ],
      "instanceCount": 1
    }
  },
  "configs": {
    "DEFAULT_GROUP/user-service.properties": {
      "dataId": "user-service.properties",
      "group": "DEFAULT_GROUP", 
      "content": "server.port=9000\nspring.datasource.url=...",
      "contentLength": 485
    }
  },
  "nacosInfo": {
    "serverAddr": "localhost:8848",
    "namespace": "public"
  }
}
```

### 2. 获取服务列表

**接口**: `GET /admin/nacos/services`

**用途**: 获取所有已注册的服务信息

**参数**:
- `pageNo`: 页码（默认1）
- `pageSize`: 每页大小（默认100）

### 3. 获取指定服务详情

**接口**: `GET /admin/nacos/services/{serviceName}`

**用途**: 获取特定服务的详细实例信息

**示例**: `GET /admin/nacos/services/user-service`

### 4. 获取配置信息

**接口**: `GET /admin/nacos/configs`

**用途**: 批量获取预定义的配置文件内容

**参数**:
- `group`: 配置分组过滤（可选）

**包含的配置文件**:
- `user-service.properties` (DEFAULT_GROUP)
- `order-service.properties` (DEFAULT_GROUP)
- `gateway-routes.yml` (GATEWAY_GROUP)
- `common-config.yml` (COMMON_GROUP)

### 5. 获取指定配置

**接口**: `GET /admin/nacos/configs/{group}/{dataId}`

**用途**: 获取特定配置文件的完整内容

**示例**: `GET /admin/nacos/configs/DEFAULT_GROUP/user-service.properties`

### 6. 健康检查

**接口**: `GET /admin/nacos/health`

**用途**: 检查Nacos连接状态

**返回数据**:
```json
{
  "status": "UP",
  "nacos": "Connected", 
  "timestamp": 1751426940138
}
```

## AI使用建议

### 推荐使用场景

1. **系统诊断**: 当用户报告服务问题时，调用 `/snapshot` 快速了解所有服务状态
2. **配置查询**: 当需要了解服务配置时，直接获取配置文件内容
3. **服务发现**: 确认哪些服务在线，它们的IP和端口信息
4. **问题排查**: 通过健康状态判断服务是否正常

### 数据解读

#### 服务实例状态
- `healthy: true` - 服务健康，可以正常处理请求
- `enabled: true` - 服务已启用
- `weight: 1.0` - 负载均衡权重

#### 服务类型识别
- `preserved.register.source: SPRING_CLOUD` - Spring Cloud服务
- 包含dubbo元数据 - Dubbo服务提供者

#### 端口信息
- 用户服务: 9000端口 (HTTP) + 20881端口 (Dubbo)
- 订单服务: 8000端口 (HTTP)
- 网关服务: 8080端口 (HTTP)

## 常见问题处理

### 1. 服务不在线
如果某个服务不在services列表中，说明：
- 服务未启动
- 服务启动失败
- 网络连接问题

### 2. 配置获取失败
如果config内容为null，说明：
- 配置文件在Nacos中不存在
- 权限问题
- Nacos连接异常

### 3. 服务不健康
如果 `healthy: false`，说明：
- 服务启动了但健康检查失败
- 服务负载过高
- 依赖服务异常

## 使用示例

### 快速获取系统状态
```bash
curl -s http://localhost:8080/admin/nacos/snapshot | jq .
```

### 检查特定服务
```bash
curl -s http://localhost:8080/admin/nacos/services/user-service
```

### 获取配置内容
```bash
curl -s http://localhost:8080/admin/nacos/configs/DEFAULT_GROUP/user-service.properties
```

## 注意事项

1. **实时性**: 所有接口都是实时查询，数据绝对准确
2. **无缓存**: 每次调用都会从Nacos获取最新数据
3. **错误处理**: 如果Nacos连接失败，会返回错误信息
4. **性能**: 接口响应速度取决于Nacos服务器性能

这些接口专为AI设计，可以大大提高问题诊断的效率和准确性。
