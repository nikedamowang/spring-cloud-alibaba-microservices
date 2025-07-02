# Nacos配置迁移指南

## 概述

本指南列出了需要上传到Nacos配置中心的配置文件，以实现配置的集中管理和动态刷新。

## 需要上传到Nacos的配置文件

### 1. 通用配置文件

#### common-config.yml

- **DataID**: `common-config.yml`
- **Group**: `COMMON_GROUP`
- **文件位置**: `services/gateway-service/nacos-config-templates/common-config.yml`
- **说明**: 所有服务共享的通用配置，包括Jackson、日志、管理端点等

#### common-database.yml

- **DataID**: `common-database.yml`
- **Group**: `COMMON_GROUP`
- **文件位置**: `services/gateway-service/nacos-config-templates/common-database.yml`
- **说明**: 数据库连接配置和MyBatis Plus配置

### 2. 网关服务配置

#### gateway-service-dev.yml

- **DataID**: `gateway-service-dev.yml`
- **Group**: `GATEWAY_GROUP`
- **文件位置**: `services/gateway-service/nacos-config-templates/gateway-service-dev.yml`
- **说明**: 网关服务的主配置，包括端口、监控、CORS、认证白名单等

#### gateway-routes.yml

- **DataID**: `gateway-routes.yml`
- **Group**: `GATEWAY_GROUP`
- **文件位置**: `services/gateway-service/nacos-config-templates/gateway-routes.yml`
- **说明**: 网关路由配置，支持动态刷新

### 3. 业务服务配置

#### user-service.properties

- **DataID**: `user-service.properties`
- **Group**: `DEFAULT_GROUP`
- **文件位置**: `services/gateway-service/nacos-config-templates/user-service.properties`
- **说明**: 用户服务的完整配置，包括数据库、Dubbo、日志等

#### order-service.properties

- **DataID**: `order-service.properties`
- **Group**: `DEFAULT_GROUP`
- **文件位置**: `services/gateway-service/nacos-config-templates/order-service.properties`
- **说明**: 订单服务的完整配置，包括数据库、Dubbo、日志等

## 配置迁移的优势

### 1. 集中管理

- 所有配置文件在Nacos控制台统一管理
- 避免配置散落在各个服务中
- 便于配置的版本控制和备份

### 2. 动态刷新

- 配置修改后无需重启服务即可生效
- 支持实时调整日志级别、限流参数等
- 提高系统的可维护性

### 3. 环境隔离

- 可以为不同环境（dev、test、prod）配置不同的参数
- 通过namespace或group实现环境隔离
- 降低配置错误的风险

### 4. 配置共享

- 通用配置（如数据库连接）可以被多个服务复用
- 减少重复配置，提高一致性
- 便于统一修改和维护

## 上传步骤

1. **登录Nacos控制台**
   ```
   http://localhost:8848/nacos
   用户名: nacos
   密码: nacos
   ```

2. **进入配置管理 → 配置列表**

3. **创建配置文件**
    - 点击"+"按钮创建新配置
    - 填写DataID、Group和配置内容
    - 选择配置格式（YAML或Properties）

4. **复制配置内容**
    - 从对应的模板文件中复制配置内容
    - 根据实际环境调整参数（如数据库连接信息）

## 配置加载顺序

1. **bootstrap.yml** - 本地引导配置
2. **common-config.yml** - 通用配置
3. **common-database.yml** - 数据库配置
4. **{service-name}.properties** - 服务特定配置
5. **gateway-routes.yml** - 网关路由配置（仅网关服务）

## 注意事项

### 1. 配置优先级

- Nacos配置优先级高于本地配置
- 相同配置项以Nacos中的为准
- 本地配置文件应保持最小化

### 2. 敏感信息处理

- 数据库密码等敏感信息建议使用配置加密
- 可考虑使用环境变量或配置中心的加密功能
- 生产环境密码不要直接写在配置文件中

### 3. 配置变更影响

- 部分配置修改需要重启服务才能生效
- 有些配置支持动态刷新（如日志级别、限流参数）
- 建议在非高峰期进行配置变更

### 4. 备份和版本控制

- 重要配置变更前建议备份
- 利用Nacos的历史版本功能
- 建议将配置文件也纳入Git版本控制

## 验证配置是否生效

1. **查看启动日志**
    - 观察服务启动时是否成功加载Nacos配置
    - 确认配置项是否正确解析

2. **使用管理端点**
    - 访问 `/actuator/configprops` 查看配置属性
    - 访问 `/actuator/env` 查看环境变量

3. **功能测试**
    - 测试相关功能是否正常工作
    - 验证配置变更是否生效

完成配置迁移后，系统将具备更好的可维护性和灵活性。
