-- 智能配置管理系统数据库表结构
-- 创建时间: 2025-07-29

-- 配置版本管理表
CREATE TABLE `config_version`
(
    `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `data_id`            varchar(255) NOT NULL COMMENT '配置文件唯一标识',
    `group_name`         varchar(128) NOT NULL DEFAULT 'DEFAULT_GROUP' COMMENT '配置组名',
    `version`            int          NOT NULL COMMENT '版本号',
    `content`            longtext     NOT NULL COMMENT '配置内容',
    `content_md5`        varchar(32)  NOT NULL COMMENT '配置内容MD5值',
    `change_description` varchar(500)          DEFAULT NULL COMMENT '变更描述',
    `operator`           varchar(64)  NOT NULL COMMENT '操作人员',
    `create_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_active`          tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否为当前活跃版本',
    `environment`        varchar(32)  NOT NULL DEFAULT 'PRODUCTION' COMMENT '配置环境',
    `change_type`        varchar(32)  NOT NULL DEFAULT 'UPDATE' COMMENT '变更类型',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_version` (`data_id`, `version`),
    KEY `idx_data_id` (`data_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_operator` (`operator`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='配置版本管理表';

-- 配置变更审计日志表
CREATE TABLE `config_audit_log`
(
    `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `data_id`            varchar(255) NOT NULL COMMENT '配置文件标识',
    `group_name`         varchar(128) NOT NULL DEFAULT 'DEFAULT_GROUP' COMMENT '配置组名',
    `operation_type`     varchar(32)  NOT NULL COMMENT '操作类型',
    `operator`           varchar(64)  NOT NULL COMMENT '操作人员',
    `operator_ip`        varchar(64)           DEFAULT NULL COMMENT '操作人员IP地址',
    `operation_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `old_version`        int                   DEFAULT NULL COMMENT '变更前版本号',
    `new_version`        int                   DEFAULT NULL COMMENT '变更后版本号',
    `change_description` varchar(500)          DEFAULT NULL COMMENT '变更描述',
    `operation_result`   varchar(32)  NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果',
    `failure_reason`     varchar(500)          DEFAULT NULL COMMENT '失败原因',
    `request_source`     varchar(64)           DEFAULT NULL COMMENT '请求来源',
    `environment`        varchar(32)           DEFAULT 'PRODUCTION' COMMENT '环境标识',
    `affected_services`  varchar(1000)         DEFAULT NULL COMMENT '影响的服务列表',
    PRIMARY KEY (`id`),
    KEY `idx_data_id` (`data_id`),
    KEY `idx_operator` (`operator`),
    KEY `idx_operation_time` (`operation_time`),
    KEY `idx_operation_type` (`operation_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='配置变更审计日志表';

-- 配置模板管理表
CREATE TABLE `config_template`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name`        varchar(128) NOT NULL COMMENT '模板名称',
    `template_type`        varchar(64)  NOT NULL COMMENT '模板类型',
    `template_description` varchar(500)          DEFAULT NULL COMMENT '模板描述',
    `template_content`     longtext     NOT NULL COMMENT '模板内容',
    `template_variables`   text                  DEFAULT NULL COMMENT '模板变量定义',
    `environment`          varchar(32)  NOT NULL DEFAULT 'ALL' COMMENT '适用环境',
    `template_version`     varchar(32)  NOT NULL DEFAULT '1.0' COMMENT '模板版本',
    `is_enabled`           tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否启用',
    `creator`              varchar(64)  NOT NULL COMMENT '创建人',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `usage_count`          int          NOT NULL DEFAULT '0' COMMENT '使用次数统计',
    `tags`                 varchar(255)          DEFAULT NULL COMMENT '模板标签',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_name` (`template_name`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_creator` (`creator`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='配置模板管理表';

-- 插入初始配置模板数据
INSERT INTO `config_template` (`template_name`, `template_type`, `template_description`, `template_content`,
                               `template_variables`, `creator`)
VALUES ('微服务基础配置模板', 'SERVICE', '适用于微服务的基础配置模板',
        '# 服务基础配置
        spring.application.name=${serviceName}
        server.port=${serverPort}

        # 数据库配置
        spring.datasource.url=jdbc:mysql://localhost:3306/${databaseName}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
        spring.datasource.username=${dbUsername}
        spring.datasource.password=${dbPassword}
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

        # Dubbo配置
        dubbo.application.name=${serviceName}
        dubbo.registry.address=nacos://127.0.0.1:8848
        dubbo.protocol.name=dubbo
        dubbo.protocol.port=${dubboPort}
        dubbo.scan.base-packages=${basePackage}',
        '{"serviceName": "服务名称", "serverPort": "服务端口", "databaseName": "数据库名", "dbUsername": "数据库用户名", "dbPassword": "数据库密码", "dubboPort": "Dubbo端口", "basePackage": "扫描包路径"}',
        'system'),

       ('网关服务配置模板', 'GATEWAY', '适用于API网关的配置模板',
        '# 网关基础配置
        spring.application.name=${gatewayName}
        server.port=${gatewayPort}

        # 网关路由配置
        spring.cloud.gateway.routes[0].id=${serviceId}
        spring.cloud.gateway.routes[0].uri=lb://${targetService}
        spring.cloud.gateway.routes[0].predicates[0]=Path=${pathPattern}

        # Sentinel配置
        spring.cloud.sentinel.transport.dashboard=localhost:8090
        spring.cloud.sentinel.transport.port=8719',
        '{"gatewayName": "网关名称", "gatewayPort": "网关端口", "serviceId": "路由ID", "targetService": "目标服务", "pathPattern": "路径匹配模式"}',
        'system'),

       ('Redis缓存配置模板', 'CACHE', 'Redis缓存相关配置模板',
        '# Redis配置
        spring.redis.host=${redisHost}
        spring.redis.port=${redisPort}
        spring.redis.password=${redisPassword}
        spring.redis.database=${redisDatabase}
        spring.redis.timeout=2000ms

        # 连接池配置
        spring.redis.lettuce.pool.max-active=8
        spring.redis.lettuce.pool.max-idle=8
        spring.redis.lettuce.pool.min-idle=0
        spring.redis.lettuce.pool.max-wait=-1ms',
        '{"redisHost": "Redis主机", "redisPort": "Redis端口", "redisPassword": "Redis密码", "redisDatabase": "Redis数据库"}',
        'system');
