-- Review Service 数据库初始化脚本
-- 创建评价表（无外键约束，微服务架构下不跨库引用）

CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT NOT NULL UNIQUE COMMENT '订单ID（唯一约束，一个订单只能评价一次）',
    `product_id` BIGINT NOT NULL COMMENT '商品ID（不设外键，通过服务间调用验证）',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID（评价者，不设外键）',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID（不设外键）',
    `rating` TINYINT NOT NULL COMMENT '评分（1-5）',
    `content` TEXT COMMENT '评价内容',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（软删除）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_buyer_id` (`buyer_id`),
    INDEX `idx_seller_id` (`seller_id`),
    CONSTRAINT `chk_rating` CHECK (`rating` >= 1 AND `rating` <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
