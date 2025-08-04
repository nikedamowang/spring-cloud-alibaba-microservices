# Maven Profile环境配置使用指南

## 📋 功能概述

本项目实现了完整的Maven Profile多环境配置管理，支持开发(dev)、测试(test)、生产(prod)三个环境的配置隔离和自动化构建部署。

## 🎯 环境配置详情

### 开发环境 (dev) - 默认激活

- **数据库**: localhost:3306/demo
- **Redis**: localhost:6379/0
- **Nacos**: 127.0.0.1:8848
- **日志级别**: DEBUG详细日志
- **特点**: 本地开发，详细调试信息

### 测试环境 (test)

- **数据库**: test-db-server:3306/demo_test
- **Redis**: test-redis-server:6379/1
- **Nacos**: test-nacos-server:8848/test
- **日志级别**: INFO适中日志
- **特点**: 测试服务器，适中性能配置

### 生产环境 (prod)

- **数据库**: prod-db-cluster:3306/demo_prod
- **Redis**: prod-redis-cluster:6379/0
- **Nacos**: prod-nacos-cluster:8848/prod
- **日志级别**: WARN最小日志
- **特点**: 生产集群，高性能优化配置

## 🚀 使用方法

### 1. Maven命令行构建

#### 开发环境构建（默认）

```bash
# 编译
mvn clean compile

# 打包
mvn clean package -DskipTests

# 带Profile明确指定
mvn clean package -Pdev -DskipTests
```

#### 测试环境构建

```bash
# 编译
mvn clean compile -Ptest

# 打包
mvn clean package -Ptest -DskipTests
```

#### 生产环境构建

```bash
# 编译
mvn clean compile -Pprod

# 打包
mvn clean package -Pprod -DskipTests
```

### 2. IDE配置

#### IDEA Maven配置

1. 打开 Settings → Build, Execution, Deployment → Build Tools → Maven
2. 在 "Profiles" 中选择要激活的Profile（dev/test/prod）
3. 点击 "Apply" 和 "OK"

#### Eclipse Maven配置

1. 右键项目 → Properties → Maven
2. 在 "Active Maven Profiles" 中输入要激活的Profile
3. 点击 "Apply" 和 "Close"

### 3. 服务启动

#### 命令行启动（指定Profile）

```bash
# 用户服务
java -jar -Dspring.profiles.active=dev services/user-service/target/user-service-0.0.1-SNAPSHOT.jar

# 订单服务
java -jar -Dspring.profiles.active=test services/order-service/target/order-service-0.0.1-SNAPSHOT.jar

# 生产环境启动
java -jar -Dspring.profiles.active=prod services/user-service/target/user-service-0.0.1-SNAPSHOT.jar
```

#### 环境变量启动

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=test

# 启动服务（会自动使用test配置）
java -jar services/user-service/target/user-service-0.0.1-SNAPSHOT.jar
```

## 🔧 配置参数说明

### Profile属性变量

每个Profile都包含以下配置变量，通过Maven资源过滤自动替换：

| 变量名                         | 开发环境                | 测试环境                          | 生产环境                           |
|-----------------------------|---------------------|-------------------------------|--------------------------------|
| `@env@`                     | dev                 | test                          | prod                           |
| `@spring.profiles.active@`  | dev                 | test                          | prod                           |
| `@db.url@`                  | localhost:3306/demo | test-db-server:3306/demo_test | prod-db-cluster:3306/demo_prod |
| `@redis.host@`              | localhost           | test-redis-server             | prod-redis-cluster             |
| `@nacos.server-addr@`       | 127.0.0.1:8848      | test-nacos-server:8848        | prod-nacos-cluster:8848        |
| `@log.level.root@`          | INFO                | INFO                          | WARN                           |
| `@log.level.com.cloudDemo@` | DEBUG               | INFO                          | INFO                           |

### 环境特定配置差异

#### 开发环境特点

- ✅ 详细的DEBUG日志输出
- ✅ 显示SQL语句（spring.jpa.show-sql=true）
- ✅ 较小的连接池（10个连接）
- ✅ 完整的异常堆栈信息

#### 测试环境特点

- ✅ 适中的INFO日志级别
- ✅ 中等规模的连接池（15个连接）
- ✅ 开放健康检查和监控端点
- ✅ 独立的数据库实例

#### 生产环境特点

- ✅ 最小的WARN日志输出
- ✅ 高性能连接池（50个连接）
- ✅ 安全的错误信息隐藏
- ✅ 连接泄漏检测机制
- ✅ 集群化配置支持

## 🔍 验证方法

### 1. 检查当前激活的Profile

```bash
mvn help:active-profiles
```

### 2. 查看所有可用的Profile

```bash
mvn help:all-profiles
```

### 3. 验证配置文件内容

```bash
# 查看处理后的配置文件
mvn process-resources -Ptest
cat target/classes/application-test.properties
```

### 4. 服务启动验证

启动服务后检查日志中的配置信息：

```
2025-08-04 15:30:01 INFO - The following profiles are active: test
2025-08-04 15:30:02 INFO - Database URL: jdbc:mysql://test-db-server:3306/demo_test
2025-08-04 15:30:03 INFO - Redis Host: test-redis-server
```

## 🚨 注意事项

### 1. 环境变量优先级

Spring配置优先级（从高到低）：

1. 命令行参数：`-Dspring.profiles.active=prod`
2. 环境变量：`SPRING_PROFILES_ACTIVE=prod`
3. application.properties中的spring.profiles.active
4. Maven Profile默认值

### 2. 生产环境安全

- 生产环境数据库密码使用环境变量：`${DB_USERNAME}`、`${DB_PASSWORD}`
- 避免在代码中硬编码敏感信息
- 启用SSL连接和连接泄漏检测

### 3. 配置文件命名规范

- `application-{profile}.properties`：特定环境的配置
- `application.properties`：通用配置（所有环境共享）

### 4. 资源过滤

Maven会自动替换配置文件中的`@变量名@`占位符，确保：

- ✅ 使用`@变量名@`格式，不是`${变量名}`
- ✅ 变量名在pom.xml的Profile properties中定义
- ✅ 启用Maven资源过滤功能

## 📊 最佳实践

### 1. 开发流程

1. **本地开发**: 使用dev Profile，连接本地数据库和Redis
2. **集成测试**: 使用test Profile，连接测试环境
3. **预发布**: 使用prod Profile配置，但连接预发布环境
4. **生产部署**: 使用prod Profile，连接生产环境

### 2. CI/CD集成

```yaml
# Jenkins Pipeline示例
stages:
  - name: Build Dev
    script: mvn clean package -Pdev -DskipTests
  - name: Build Test
    script: mvn clean package -Ptest -DskipTests
  - name: Build Prod
    script: mvn clean package -Pprod -DskipTests
```

### 3. Docker化部署

```dockerfile
# Dockerfile示例
FROM openjdk:17-jre-slim
ARG PROFILE=prod
ENV SPRING_PROFILES_ACTIVE=${PROFILE}
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🎉 技术价值

### 企业级特性

- ✅ **配置隔离**: 不同环境完全独立的配置管理
- ✅ **安全性**: 生产环境敏感信息保护
- ✅ **自动化**: Maven构建过程自动化配置替换
- ✅ **标准化**: 符合企业级项目配置管理规范

### 面试亮点

- ✅ **多环境管理经验**: 展示对企业级项目部署的理解
- ✅ **DevOps思维**: 体现自动化构建和部署的实践能力
- ✅ **配置管理**: 掌握复杂项目的配置管理最佳实践
- ✅ **安全意识**: 生产环境配置安全和最佳实践的应用
