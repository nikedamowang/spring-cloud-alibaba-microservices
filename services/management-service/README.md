# Management Service - Nacos 管理服务

## 概述

`management-service` 是专门为 AI 调用设计的 Nacos 配置和服务管理模块。它提供只读的 REST API 接口，用于获取 Nacos
上的配置信息和服务状态，同时支持本地配置模板管理。

## 主要功能

### 1. 配置管理

- 获取指定配置信息
- 获取所有配置列表
- 搜索配置内容
- 比较 Nacos 配置与本地模板的差异

### 2. 服务管理

- 获取服务实例信息
- 获取所有注册服务列表
- 检查服务健康状态

### 3. 系统监控

- 检查 Nacos 服务器状态
- 提供 API 使用说明

## API 接口

### 配置相关接口

#### 获取指定配置

```
GET /api/nacos/config?dataId={dataId}&group={group}&namespace={namespace}
```

#### 获取所有配置

```
GET /api/nacos/configs?namespace={namespace}
```

#### 搜索配置

```
GET /api/nacos/configs/search?keyword={keyword}&namespace={namespace}
```

#### 比较配置与模板

```
GET /api/nacos/config/compare?dataId={dataId}&group={group}&namespace={namespace}
```

### 服务相关接口

#### 获取服务实例

```
GET /api/nacos/service/instances?serviceName={serviceName}&groupName={groupName}&namespace={namespace}
```

#### 获取所有服务

```
GET /api/nacos/services?namespace={namespace}
```

#### 获取服务健康状态

```
GET /api/nacos/service/health?serviceName={serviceName}&namespace={namespace}
```

### 系统相关接口

#### 获取 Nacos 服务器状态

```
GET /api/nacos/server/status
```

#### 获取 API 帮助

```
GET /api/nacos/help
```

## 配置说明

### 应用配置 (application.properties)

```properties
# 服务端口
server.port=9090
# Nacos 连接配置
nacos.server-addr=localhost:8848
nacos.namespace=
nacos.username=nacos
nacos.password=nacos
# 配置模板路径
nacos.config.template-path=./config-templates
```

### 配置模板管理

在 `config-templates` 目录下维护配置模板文件，命名规则与 Nacos 中的 dataId 保持一致：

- `application.properties` - 应用基础配置模板
- `user-service.properties` - 用户服务配置模板
- `order-service.properties` - 订单服务配置模板

## 启动方式

1. 确保 Nacos 服务器已启动
2. 运行 `ManagementServiceApplication` 主类
3. 访问 `http://localhost:9090/api/nacos/help` 查看所有可用接口

## AI 调用示例

### 获取用户服务配置

```bash
curl "http://localhost:9090/api/nacos/config?dataId=user-service.properties&group=DEFAULT_GROUP"
```

### 检查服务健康状态

```bash
curl "http://localhost:9090/api/nacos/service/health?serviceName=user-service"
```

### 搜索配置

```bash
curl "http://localhost:9090/api/nacos/configs/search?keyword=database"
```

## 安全说明

- **只读权限**: 所有 API 接口均为只读操作，不提供配置修改功能
- **配置管理**: 配置修改需要通过更新本地模板文件进行
- **权限控制**: 建议在生产环境中添加适当的认证和授权机制

## 使用场景

1. **错误排查**: AI 可以查看配置信息来诊断问题
2. **配置检查**: 比较 Nacos 配置与预期模板是否一致
3. **服务监控**: 检查服务实例状态和健康情况
4. **系统巡检**: 定期检查 Nacos 服务器和各个服务的状态

## 注意事项

1. 确保 Nacos 服务器可访问
2. 配置正确的 Nacos 连接参数
3. 定期更新本地配置模板以保持同步
4. 在生产环境中考虑添加访问控制
