-- Order Service 测试数据库初始化脚本

CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `buyer_id` BIGINT NOT NULL,
    `seller_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `product_image` VARCHAR(255) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    `idempotency_key` VARCHAR(64) NOT NULL,
    `canceled_by` VARCHAR(20) DEFAULT NULL,
    `canceled_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (`buyer_id`, `idempotency_key`)
);

CREATE INDEX IF NOT EXISTS idx_buyer_id ON `order` (`buyer_id`);
CREATE INDEX IF NOT EXISTS idx_seller_id ON `order` (`seller_id`);
CREATE INDEX IF NOT EXISTS idx_product_id ON `order` (`product_id`);
CREATE INDEX IF NOT EXISTS idx_status ON `order` (`status`);
