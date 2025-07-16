# Dubbo负载均衡配置部署说明

## 📋 部署前准备

### 1. 确认Nacos服务状态

```bash
# 检查Nacos服务器状态
curl http://localhost:9090/api/nacos/server/status
```

### 2. 当前配置状态

已创建的配置模板（待上传到Nacos）：

- ✅ `user-service.properties` - 用户服务配置（端口9000，Dubbo端口20881）
- ✅ `order-service.properties` - 订单服务配置（端口8000，Dubbo端口20882）
- ✅ `management-service.properties` - 管理服务配置（端口9090，Dubbo端口20883）

## 🚀 部署步骤

### 配置文件准备（已完成）

按照管理模块文档要求，已完成以下流程：

1. **同步现有配置**：通过管理服务API获取Nacos中的现有配置
2. **生成优化配置**：基于现有配置生成包含负载均衡优化的新配置
3. **配置文件位置**：所有待上传配置已放置在`config-templates-modified`目录

### 第一步：上传配置到Nacos

按照管理模块文档的要求，需要将config-templates-modified目录下的配置文件上传到Nacos配置中心。

**重要提醒**：直接通过Nacos控制台手动上传，或使用管理服务的API上传功能。

#### 配置上传清单：

1. **用户服务配置**
    - DataID: `user-service.properties`
    - Group: `DEFAULT_GROUP`
    - 服务端口: `9000`（确认正确）
    - 配置内容：包含Dubbo负载均衡优化配置

2. **订单服务配置**
    - DataID: `order-service.properties`
    - Group: `DEFAULT_GROUP`
    - 服务端口: `8000`（确认正确，不是9001）
    - 配置内容：包含负载均衡策略配置

3. **管理服务配置**
    - DataID: `management-service.properties`
    - Group: `DEFAULT_GROUP`
    - 服务端口: `9090`（确认正确）
    - 配置内容：包含管理服务相关配置

### 第二步：验证配置上传

```bash
# 检查用户服务配置
curl "http://localhost:9090/api/nacos/config?dataId=user-service.properties&group=DEFAULT_GROUP"

# 检查订单服务配置
curl "http://localhost:9090/api/nacos/config?dataId=order-service.properties&group=DEFAULT_GROUP"

# 检查管理服务配置
curl "http://localhost:9090/api/nacos/config?dataId=management-service.properties&group=DEFAULT_GROUP"
```

### 第三步：启动服务验证

按照以下顺序启动服务：

1. **启动用户服务**
   ```bash
   cd services/user-service
   mvn spring-boot:run
   ```

2. **启动订单服务**
   ```bash
   cd services/order-service
   mvn spring-boot:run
   ```

3. **启动管理服务**
   ```bash
   cd services/management-service
   mvn spring-boot:run
   ```

### 第四步：验证Dubbo负载均衡配置

#### 1. 检查服务注册状态

```bash
# 检查所有注册服务
curl "http://localhost:9090/api/nacos/services"

# 检查用户服务实例
curl "http://localhost:9090/api/nacos/service/instances?serviceName=user-service"

# 检查订单服务实例
curl "http://localhost:9090/api/nacos/service/instances?serviceName=order-service"
```

#### 2. 验证负载均衡策略

```bash
# 检查负载均衡策略配置
curl "http://localhost:9090/api/loadbalancer/strategies"

# 检查用户服务策略
curl "http://localhost:9090/api/loadbalancer/strategies/user-service"

# 检查订单服务策略
curl "http://localhost:9090/api/loadbalancer/strategies/order-service"
```

#### 3. 测试服务调用

```bash
# 测试用户服务调用（通过订单服务）
curl -X POST "http://localhost:9001/api/orders" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productName": "测试商品", "quantity": 2}'
```

## 📊 Dubbo负载均衡配置详情

### 用户服务负载均衡配置

```properties
# 提供者配置
dubbo.provider.timeout=5000
dubbo.provider.retries=2
dubbo.provider.threads=200
dubbo.provider.cluster=failover
dubbo.provider.loadbalance=roundrobin
# 消费者配置
dubbo.consumer.timeout=5000
dubbo.consumer.retries=2
dubbo.consumer.loadbalance=roundrobin
dubbo.consumer.cluster=failover
```

### 订单服务负载均衡配置

```properties
# 提供者配置（响应时间优化）
dubbo.provider.timeout=3000
dubbo.provider.retries=1
dubbo.provider.loadbalance=leastactive
# 消费者配置
dubbo.consumer.timeout=5000
dubbo.consumer.retries=2
dubbo.consumer.loadbalance=roundrobin
```

### 管理服务负载均衡配置

```properties
# 提供者配置（管理操作）
dubbo.provider.timeout=8000
dubbo.provider.retries=3
dubbo.provider.loadbalance=random
# 消费者配置
dubbo.consumer.timeout=8000
dubbo.consumer.retries=3
dubbo.consumer.loadbalance=random
```

## 🔧 故障排除

### 常见问题及解决方案

1. **配置文件格式错误**
    - 确保使用.properties格式，不要使用YAML格式
    - 检查是否有空值配置项

2. **服务启动失败**
    - 检查Nacos配置中心连接是否正常
    - 确认配置文件已正确上传到Nacos

3. **Dubbo服务调用失败**
    - 检查服务注册状态
    - 验证负载均衡策略配置
    - 查看服务日志

### 监控和验证命令

```bash
# 检查服务健康状态
curl "http://localhost:9090/api/nacos/service/health?serviceName=user-service"
curl "http://localhost:9090/api/nacos/service/health?serviceName=order-service"

# 检查负载均衡统计
curl "http://localhost:9090/api/loadbalancer/stats"

# 检查负载均衡健康状态
curl "http://localhost:9090/api/loadbalancer/health"
```

## ✅ 验证清单

部署完成后，请确认以下项目：

- [ ] 所有配置文件已上传到Nacos配置中心
- [ ] 服务能够正常启动并注册到Nacos
- [ ] Dubbo服务调用正常
- [ ] 负载均衡策略生效
- [ ] 负载均衡管理API可用
- [ ] 服务健康检查正常

## 🎯 预期效果

配置完成后，系统将具备以下能力：

1. **多种负载均衡策略**：轮询、最少活跃调用、随机
2. **故障自动处理**：失败自动切换、快速失败
3. **动态策略管理**：运行时调整负载均衡策略
4. **健康状态监控**：实时监控服务实例状态
5. **性能优化**：不同服务采用最适合的负载均衡算法

配置完成后，即可开始下一步的 **Sentinel熔断降级** 集成工作。
