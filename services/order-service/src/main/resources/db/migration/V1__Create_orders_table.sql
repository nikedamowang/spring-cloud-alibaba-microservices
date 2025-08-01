-- Flyway数据库版本管理 - 订单表初始化
-- 版本：V1
-- 描述：创建订单表结构
-- 作者：CloudDemo项目
-- 日期：2025-08-01

-- 创建订单表
CREATE TABLE `orders`
(
    `id`               bigint                                                                                                     NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci                                               NOT NULL COMMENT '订单编号',
    `user_id`          int                                                                                                        NOT NULL COMMENT '用户ID',
    `total_amount`     decimal(10, 2)                                                                                             NOT NULL COMMENT '订单总金额',
    `payment_amount`   decimal(10, 2)                                                                                             NOT NULL COMMENT '实付金额',
    `payment_type`     enum ('credit_card','wechat','alipay','cash') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci             NULL     DEFAULT NULL COMMENT '支付方式',
    `status`           enum ('pending','paid','shipped','completed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '订单状态',
    `shipping_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci                                              NULL     DEFAULT NULL COMMENT '收货地址',
    `create_time`      datetime                                                                                                   NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `order_no` (`order_no` ASC) USING BTREE COMMENT '订单编号唯一索引',
    INDEX `idx_user_id` (`user_id` ASC) USING BTREE COMMENT '用户ID索引',
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '订单表'
  ROW_FORMAT = Dynamic;
