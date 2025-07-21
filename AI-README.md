# CloudDemo 微服务项目 - AI参考文档

## 项目架构

### 服务列表

- **user-service** (9000): 用户服务，JWT认证，Redis缓存，Sentinel保护
- **order-service** (8000): 订单服务，Sentinel保护，随机异常模拟
- **gateway-service** (8080): API网关，路由转发，Sentinel限流
- **management-service** (9090): **AI专用管理接口**，配置同步
- **common-api**: 公共DTO和接口定义

### 基础设施

- **Nacos** (8848): 注册中心/配置中心
- **Sentinel Dashboard** (8090): 监控面板
- **Redis** (6379): 分布式缓存
- **MySQL** (3306): 数据库

### 技术栈

- Spring Boot 3.3.4 + Spring Cloud 2023.0.3
- Nacos Discovery/Config + Dubbo 3.2.15 + Sentinel
- MyBatis Plus + Redis + JWT

## AI专用接口 (management-service:9090)

### 配置管理

```
GET /api/nacos/config?dataId={serviceName}.properties    # 获取指定服务配置
GET /api/nacos/configs/search?keyword={keyword}          # 搜索配置
```

### 服务状态

```
GET /api/nacos/service/instances?serviceName={name}      # 获取服务实例信息
```

## 配置修改规范

**重要**: 禁止AI直接修改Nacos配置，必须严格遵循以下四步流程：

### 标准操作流程

#### 第一步：同步Nacos配置到本地

- **必须**首先调用管理服务的sync-all接口将Nacos上的配置文件同步到本地
- 同步接口：`GET /api/nacos/config/sync-all`
- 同步路径：`services/management-service/config-templates/`
- 确保获取最新的Nacos配置作为基础

#### 第二步：差异分析

- 对比本地同步的Nacos配置文件与项目中的本地配置文件
- 识别需要更新的配置项和潜在冲突
- 确定最终需要上传到Nacos的配置内容
- 分析配置变更对服务的影响

#### 第三步：配置验证

- **必须**检查准备上传的配置文件是否存在语法问题
- 验证配置项的格式和数值是否正确
- 检查端口冲突、服务依赖等潜在问题
- 确认配置修改不会导致服务启动失败或功能异常

#### 第四步：修改本地资源文件

- 更新各服务模块的本地配置文件
- 路径：`services/{service}/src/main/resources/application.*`
- 确保本地配置与即将上传的Nacos配置保持一致
- **注意**: 人工确认并手动上传配置到Nacos后，重启相关服务

### 配置文件路径说明

```
Nacos同步配置: services/management-service/config-templates/
  - 专门存放从Nacos同步的当前全部配置文件
  - 保持与Nacos配置中心完全一致
  - 仅包含.properties配置文件，禁止其他杂文件

修改后配置: services/management-service/config-templates-modified/
  - 专门存放修改后准备上传到Nacos的全部配置文件  
  - 包含完整的服务配置集合
  - 仅包含.properties配置文件，禁止其他杂文件

各服务本地配置:
├── services/user-service/src/main/resources/application.properties
├── services/order-service/src/main/resources/application.properties
├── services/gateway-service/src/main/resources/application.yml
└── services/management-service/src/main/resources/application.properties
```

### 配置文件夹管理规范

- **config-templates/** 目录必须保持整洁，只包含从Nacos同步的.properties文件
- **config-templates-modified/** 目录必须保持整洁，只包含修改后的.properties文件
- 两个目录都禁止存放其他类型的文件（如.json、.log、.tmp等）
- 每次配置同步或修改操作前，建议清理目录中的非配置文件

### 安全注意事项

- ⚠️ Nacos配置具有最高优先级，会覆盖本地配置
- ⚠️ 配置错误可能导致服务无法启动
- ⚠️ 修改共享配置需考虑对其他服务的影响
- ⚠️ 端口冲突会导致服务启动失败
- ✅ 建议先在测试环境验证配置修改

## 项目状态

### 已完成功能

- ✅ Sentinel熔断降级 (全部服务)
- ✅ 服务注册发现 (Nacos)
- ✅ 配置中心 (Nacos)
- ✅ RPC调用 (Dubbo)
- ✅ API网关 (Gateway)
- ✅ 认证授权 (JWT)
- ✅ 分布式缓存 (Redis)

### 当前任务

- 🚧 接口文档自动生成 (Swagger/OpenAPI) - 计划书第一阶段第二点

### 关键文档

- `技术组件集成计划书-一年经验优化版.md` - 技术路线图
- `Sentinel熔断降级集成完成报告.md` - 第一阶段完成报告
- `services/management-service/config-templates/` - 当前Nacos配置

## 使用示例

```bash
# 检查所有服务状态
curl "http://localhost:9090/api/services/status"

# 同步配置
curl "http://localhost:9090/api/config/sync-all"

# 获取用户服务配置
curl "http://localhost:9090/api/nacos/config?dataId=user-service.properties"

# 获取网关服务配置
curl "http://localhost:9090/api/nacos/config?dataId=gateway-service.properties"

# 搜索配置
curl "http://localhost:9090/api/nacos/configs/search?keyword=service"

# 获取服务实例
curl "http://localhost:9090/api/nacos/service/instances?serviceName=user-service"

# 通过网关访问
curl "http://localhost:8080/user/list"
```
