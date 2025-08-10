# 配置模板说明

## 📋 环境配置要求

### 必需的基础设施
1. **Nacos Server** (端口: 8848)
   - 下载地址: https://nacos.io/
   - 启动命令: `startup.cmd -m standalone`

2. **Redis Server** (端口: 6379)
   - 默认配置即可，无密码

3. **MySQL Database** (端口: 3306)
   - 创建数据库: `clouddemo`
   - 用户名: `root`
   - 密码: `123456`

### 🔧 Nacos配置中心设置

项目运行需要在Nacos配置中心添加以下配置文件：

#### 1. user-service.properties
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/clouddemo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0

# JWT配置
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# MyBatis Plus配置
mybatis-plus.mapper-locations=classpath:mapper/*.xml
mybatis-plus.type-aliases-package=com.cloudDemo.userservice.entity
```

#### 2. order-service.properties
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/clouddemo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Dubbo配置
dubbo.protocol.name=dubbo
dubbo.protocol.port=20881
dubbo.registry.protocol=nacos
dubbo.registry.address=localhost:8848
```

#### 3. gateway-service.properties
```properties
# 网关路由配置
spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=lb://user-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/user/**

spring.cloud.gateway.routes[1].id=order-service
spring.cloud.gateway.routes[1].uri=lb://order-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/order/**
```

#### 4. management-service.properties
```properties
# 服务端口
server.port=9090
spring.application.name=management-service

# Nacos客户端配置
nacos.server.addr=localhost:8848
nacos.namespace=
```

## 🚀 启动顺序

1. 启动 Nacos Server
2. 启动 Redis Server  
3. 启动 MySQL Server
4. 在 Nacos 控制台添加以上配置
5. 按顺序启动微服务：
   - management-service (9090)
   - user-service (9000)
   - order-service (8000)
   - gateway-service (8080)

## 📝 注意事项

- 请根据实际环境修改数据库连接信息
- JWT密钥请使用更安全的随机字符串
- 生产环境请使用更复杂的密码配置
