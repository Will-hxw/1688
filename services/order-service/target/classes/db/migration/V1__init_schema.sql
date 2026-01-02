-- Order Service 数据库初始化脚本
-- 创建订单表（无外键约束，微服务架构下不跨库引用）
-- 包含商品快照字段（product_name, product_image）避免跨服务查询

CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID（不设外键，通过服务间调用验证）',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID（不设外键，通过服务间调用验证）',
    `product_id` BIGINT NOT NULL COMMENT '商品ID（不设外键，通过服务间调用验证）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '成交价格（下单时锁定）',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称快照',
    `product_image` VARCHAR(255) NOT NULL COMMENT '商品图片快照',
    `status` VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT '状态：CREATED/SHIPPED/RECEIVED/REVIEWED/CANCELED',
    `idempotency_key` VARCHAR(64) NOT NULL COMMENT '幂等键',
    `canceled_by` VARCHAR(20) DEFAULT NULL COMMENT '取消方：BUYER/SELLER',
    `canceled_at` DATETIME DEFAULT NULL COMMENT '取消时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uniq_buyer_idem` (`buyer_id`, `idempotency_key`),
    INDEX `idx_buyer_id` (`buyer_id`),
    INDEX `idx_seller_id` (`seller_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
