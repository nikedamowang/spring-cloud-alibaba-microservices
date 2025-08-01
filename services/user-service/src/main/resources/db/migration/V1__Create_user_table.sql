-- Flyway数据库版本管理 - 用户表初始化
-- 版本：V1
-- 描述：创建用户表结构和初始数据
-- 作者：CloudDemo项目
-- 日期：2025-08-01

-- 创建用户表
CREATE TABLE `user`
(
    `id`          int                                                           NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '用户名',
    `password`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `email`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
    `phone`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '手机号',
    `status`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT 'active' COMMENT '状态：active-活跃, inactive-非活跃',
    `create_time` datetime(6)                                                   NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime(6)                                                   NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `username` (`username` ASC) USING BTREE COMMENT '用户名唯一索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '用户信息表'
  ROW_FORMAT = Dynamic;

-- 插入初始测试数据
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `status`, `create_time`)
VALUES ('alice', '555', 'alice@example.com', '13800000001', 'active', NOW(6)),
       ('bob', '123456', 'bob@example.com', '13800000002', 'active', NOW(6)),
       ('charlie', '123456', 'charlie@example.com', '13800000003', 'active', NOW(6)),
       ('david', '123456', 'david@example.com', '13800000004', 'active', NOW(6)),
       ('eva', '123456', 'eva@example.com', '13800000005', 'active', NOW(6)),
       ('frank', '123456', 'frank@example.com', '13800000006', 'active', NOW(6)),
       ('grace', '123456', 'grace@example.com', '13800000007', 'active', NOW(6)),
       ('henry', '123456', 'henry@example.com', '13800000008', 'active', NOW(6)),
       ('irene', '123456', 'irene@example.com', '13800000009', 'active', NOW(6)),
       ('jack', '123456', 'jack@example.com', '13800000010', 'active', NOW(6));
