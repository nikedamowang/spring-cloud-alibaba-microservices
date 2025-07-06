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

在 `config-templates` 目录下维护从Nacos同步的配置文件，在 `config-templates-modified` 目录下维护待上传到Nacos的配置模板文件，命名规则与 Nacos 中的 dataId 保持一致：

**config-templates（Nacos同步目录）：**
- `application.properties` - 从Nacos同步的应用基础配置
- `user-service.properties` - 从Nacos同步的用户服务配置
- `order-service.properties` - 从Nacos同步的订单服务配置

**config-templates-modified（上传模板目录）：**
- `application.properties` - 待上传的应用基础配置模板
- `user-service.properties` - 待上传的用户服务配置模板
- `order-service.properties` - 待上传的订单服务配置模板
- `management-service.properties` - 待上传的管理服务配置模板

## 配置管理最佳实践

### 配置分离原则

**本地配置文件应该只包含：**
- 应用名称 (`spring.application.name`)
- Nacos配置中心连接配置
- Nacos服务发现配置
- 基础框架日志配置（Spring Boot、Nacos连接相关）

**Nacos配置应该包含：**
- 所有业务相关配置（数据库、Redis、MQ等）
- 框架详细配置（MyBatis Plus、Dubbo等）
- 业务日志配置（具体包的日志级别）
- 生产环境配置（文件日志、监控等）

### 配置管理流程

1. **获取现有配置**
   ```bash
   # 同步Nacos上的配置到本地
   curl "http://localhost:9090/api/nacos/config/sync-all"
   ```

2. **配置对比分析**
   - 比较本地配置文件与Nacos同步的配置文件
   - 识别缺失的配置项和冗余配置

3. **创建上传模板**
   - 在 `config-templates-modified` 目录下创建合并后的完整配置
   - 移除循环引用配置（如 `spring.config.import`）
   - 移除Nacos连接配置（这些应该只在本地）

4. **上传到Nacos**
   - 将 `config-templates-modified` 中的配置文件内容复制到Nacos
   - DataID设置为文件名，Group设置为 `DEFAULT_GROUP`

5. **验证配置**
   ```bash
   # 重新同步验证
   curl "http://localhost:9090/api/nacos/config/sync?dataId=user-service.properties"
   ```

### 日志配置策略

**本地日志配置（基础框架）：**
```properties
logging.level.root=WARN
logging.level.org.springframework.boot=WARN
logging.level.com.alibaba.nacos=WARN
logging.pattern.console=%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

**Nacos日志配置（业务详细）：**
```properties
logging.level.com.cloudDemo=INFO
logging.level.com.cloudDemo.userservice=INFO
logging.level.com.baomidou.mybatisplus=DEBUG
logging.level.org.apache.dubbo=WARN
logging.file.name=logs/user-service.log
logging.logback.rollingpolicy.max-file-size=100MB
logging.logback.rollingpolicy.max-history=30
```

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
