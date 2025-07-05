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

 Date: 05/07/2025 23:32:40
 说明:这里是数据库导出来的结构,是真实存在的
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`
(
    `id`               bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
    `user_id`          int                                                          NOT NULL COMMENT '用户ID',
    `total_amount`     decimal(10, 2)                                               NOT NULL COMMENT '订单总金额',
    `payment_amount`   decimal(10, 2)                                               NOT NULL COMMENT '实付金额',
    `payment_type`     enum('credit_card','wechat','alipay','cash') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式',
    `status`           enum('pending','paid','shipped','completed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '订单状态',
    `shipping_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
    `create_time`      datetime                                                     NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
    INDEX              `idx_user_id`(`user_id` ASC) USING BTREE,
    INDEX              `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 78126 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

SET
FOREIGN_KEY_CHECKS = 1;
