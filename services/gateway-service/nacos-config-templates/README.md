# Gateway Nacos配置中心使用指南（Public命名空间版本）

## 概述

Gateway服务配置为使用public命名空间，通过配置分组来管理不同类型的配置。

## Nacos配置文件列表（全部在public命名空间）

### 1. 主配置文件

**Data ID**: `gateway-service-dev.yml`
**Group**: `GATEWAY_GROUP`
**命名空间**: `public`
**描述**: Gateway主要配置，包括路由、熔断器、CORS等

### 2. 路由配置文件（支持动态刷新）

**Data ID**: `gateway-routes.yml`
**Group**: `GATEWAY_GROUP`
**命名空间**: `public`
**描述**: 专门管理路由配置，支持热更新

### 3. 共享配置文件

**Data ID**: `common-config.yml`
**Group**: `COMMON_GROUP`
**命名空间**: `public`
**描述**: 所有服务共享的通用配置

### 4. 生产环境配置（可选）

**Data ID**: `gateway-service-prod.yml`
**Group**: `GATEWAY_GROUP`
**命名空间**: `public`
**描述**: 生产环境配置，可通过修改bootstrap.yml中的profiles.active切换

## 配置添加步骤

### Step 1: 登录Nacos控制台

访问 http://localhost:8848/nacos （默认用户名密码：nacos/nacos）

### Step 2: 添加配置

在"配置管理" -> "配置列表"中，确保选择"public"命名空间，然后添加以下配置：

#### 配置1: Gateway主配置

- **Data ID**: `gateway-service-dev.yml`
- **Group**: `GATEWAY_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `nacos-config-templates/gateway-service-dev.yml` 内容

#### 配置2: 路由配置（支持热更新）

- **Data ID**: `gateway-routes.yml`
- **Group**: `GATEWAY_GROUP`
- **配置格式**: `YAML`
- **配置内��**: 复制 `nacos-config-templates/gateway-routes.yml` 内容

#### 配置3: 共享配置

- **Data ID**: `common-config.yml`
- **Group**: `COMMON_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `nacos-config-templates/common-config.yml` 内容

## 配置分组说明

使用不同的Group来组织配置：

- **GATEWAY_GROUP**: Gateway专用配置
- **COMMON_GROUP**: 所有服务共享的配置
- **USER_GROUP**: 用户服务专用配置（如需要）
- **ORDER_GROUP**: 订单服务专用配置（如需要）

## 环境区分策略

在单个命名空间中区分环境的方法：

1. **通过Data ID后缀**: `gateway-service-dev.yml` vs `gateway-service-prod.yml`
2. **通过Group**: `GATEWAY_DEV_GROUP` vs `GATEWAY_PROD_GROUP`
3. **通过配置内容**: 在同一个配置文件中使用profile来区分

## 快速开始

1. 启动Nacos服务
2. 在Nacos控制台添加上述3个配置文件
3. 启动Gateway服务
4. 验证配置生效：访问 `/gateway/routes` 查看路由配置

## 动态配置测试

1. 修改Nacos中的 `gateway-routes.yml` 配置
2. 等待几秒钟（自动刷新）
3. 访问 `/gateway/routes` 验证路由配置已更新
4. 无需重启服务即可生效

这样使用单个public命名空间既简单又实用！
