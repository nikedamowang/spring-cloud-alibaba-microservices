/*
 Navicat Premium Dump SQL

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : demo

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 05/07/2025 15:46:19
 说明:这里是数据库导出来的结构及数据,是真实存在的
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          int                                                           NOT NULL AUTO_INCREMENT,
    `username`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    `password`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `email`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `phone`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL,
    `status`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT 'active',
    `create_time` datetime(6)                                                   NULL DEFAULT NULL,
    `update_time` datetime(6)                                                   NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `username` (`username` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 11
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user`
VALUES (1, 'alice', '555', 'alice@example.com', '13800000001', 'active', '2025-06-27 22:20:18.000000',
        '2025-07-04 14:45:05.551514');
INSERT INTO `user`
VALUES (2, 'bob', '123456', 'bob@example.com', '13800000002', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (3, 'charlie', '123456', 'charlie@example.com', '13800000003', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (4, 'david', '123456', 'david@example.com', '13800000004', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (5, 'eva', '123456', 'eva@example.com', '13800000005', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (6, 'frank', '123456', 'frank@example.com', '13800000006', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (7, 'grace', '123456', 'grace@example.com', '13800000007', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (8, 'henry', '123456', 'henry@example.com', '13800000008', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (9, 'irene', '123456', 'irene@example.com', '13800000009', 'active', '2025-06-27 22:20:18.000000', NULL);
INSERT INTO `user`
VALUES (10, 'jack', '123456', 'jack@example.com', '13800000010', 'active', '2025-06-27 22:20:18.000000', NULL);

SET FOREIGN_KEY_CHECKS = 1;
