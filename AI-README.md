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

### 终端

- PowerShell 7.5.2
- 通过 netstat 查找端口（如 443） netstat -ano | Select-String ":443\s"
- 当通过命令行没有得到想要的结果的时候,可以将命令行输出重定向到文件中,然后在文件中查看(terminal_output.txt)
- 这个文件应该使用同一个文件避免产生大量多余的文件
- **强制要求**: 所有命令输出都必须重定向到 `terminal_output.txt` 文件中然后读取该文件查看结果(除了要启动服务时的命令)

#### 中文乱码解决方案

**问题**: curl命令输出中文时会出现乱码，影响结果判断

**解决方法**:

1. **使用 `-s` 参数隐藏curl进度信息**:
   ```bash
   curl -s -X GET "http://localhost:9090/api/endpoint" > terminal_output.txt 2>&1
   ```

2. **使用 `--output` 参数替代重定向**:
   ```bash
   curl -s -X GET "http://localhost:9090/api/endpoint" --output terminal_output.txt
   ```

3. **设置UTF-8编码** (可选):
   ```bash
   chcp 65001 && curl -s -X GET "http://localhost:9090/api/endpoint" > terminal_output.txt 2>&1
   ```

#### 推荐的命令格式

```bash
# 标准GET请求格式
curl -s -X GET "http://localhost:9090/api/endpoint" > terminal_output.txt 2>&1

# 标准POST请求格式
curl -s -X POST "http://localhost:9090/api/endpoint?param=value" \
  -H "Content-Type: text/plain" \
  -d "request_body_content" > terminal_output.txt 2>&1

# JSON POST请求格式
curl -s -X POST "http://localhost:9090/api/endpoint" \
  -H "Content-Type: application/json" \
  -d '{"key":"value"}' > terminal_output.txt 2>&1
```

#### 重要注意事项

- ✅ **必须使用 `-s` 参数**：隐藏curl的进度信息，避免干扰JSON响应
- ✅ **统一使用 `terminal_output.txt`**：避免产生大量临时文件
- ✅ **每次执行完命令后立即读取文件**：确保获取最新结果
- ⚠️ **中文内容可能显示为编码**：但JSON结构和英文部分是正确的
- ⚠️ **重点关注JSON结构和status字段**：判断接口是否正常工作

- 使用同步接口(sync-all)之后需要到对应的文件夹(services/management-service/config-templates)读取同步到本地的配置文件
- 查看文件最后 N 行示例（模拟 tail）:Get-Content -Path "D:\WorkSpace\IDEA\cloudDemo\logs\management-service.log" -Tail 20

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
- 本地配置文件只保留必要的部分,不必要的部分全部上传至nacos
- **注意**: 人工确认并手动上传配置到Nacos后，重启相关服务

#### 第五步：提供需要上传到nacos的配置文件(假如需要修改的话)

- 将修改后的配置文件放入`services/management-service/config-templates-modified/`
- 确保该目录只包含修改后的配置文件
- 禁止存放其他类型的文件（如.json、.log等）
- **注意**: 该目录的内容将被上传到Nacos

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
