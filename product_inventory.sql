-- 商品库存表
CREATE TABLE IF NOT EXISTS product_inventory
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    product_id      VARCHAR(64)                                              NOT NULL UNIQUE COMMENT '商品ID',
    product_name    VARCHAR(255)                                             NOT NULL COMMENT '商品名称',
    total_stock     INT                                                      NOT NULL DEFAULT 0 COMMENT '总库存',
    available_stock INT                                                      NOT NULL DEFAULT 0 COMMENT '可用库存',
    reserved_stock  INT                                                      NOT NULL DEFAULT 0 COMMENT '预扣库存',
    sold_stock      INT                                                      NOT NULL DEFAULT 0 COMMENT '已售库存',
    status          ENUM ('NORMAL', 'LOW_STOCK', 'OUT_OF_STOCK', 'DISABLED') NOT NULL DEFAULT 'NORMAL' COMMENT '库存状态',
    create_time     DATETIME                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version         INT                                                      NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品库存表';

-- 插入测试数据
INSERT INTO product_inventory (product_id, product_name, total_stock, available_stock, reserved_stock, sold_stock,
                               status)
VALUES ('PROD-001', 'iPhone 15 Pro Max 256GB', 1000, 950, 30, 20, 'NORMAL'),
       ('PROD-002', 'MacBook Pro 14英寸 M3', 500, 480, 15, 5, 'NORMAL'),
       ('PROD-003', 'AirPods Pro 第二代', 800, 750, 40, 10, 'NORMAL'),
       ('PROD-004', 'iPad Air 第五代', 300, 280, 10, 10, 'NORMAL'),
       ('PROD-005', '小米14 Ultra 512GB', 200, 180, 15, 5, 'NORMAL');
