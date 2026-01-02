-- Review Service 测试数据库初始化脚本

CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL UNIQUE,
    `product_id` BIGINT NOT NULL,
    `buyer_id` BIGINT NOT NULL,
    `seller_id` BIGINT NOT NULL,
    `rating` TINYINT NOT NULL,
    `content` TEXT,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_id ON `review` (`product_id`);
CREATE INDEX IF NOT EXISTS idx_buyer_id ON `review` (`buyer_id`);
CREATE INDEX IF NOT EXISTS idx_seller_id ON `review` (`seller_id`);
