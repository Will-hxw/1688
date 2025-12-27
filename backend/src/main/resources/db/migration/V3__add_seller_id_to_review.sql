-- V3: 为评价表添加卖家ID字段
-- 修复实体类与数据库表结构不一致的问题

-- 先添加可空列
ALTER TABLE `review` 
ADD COLUMN `seller_id` BIGINT NULL COMMENT '卖家ID' AFTER `buyer_id`;

-- 从订单表回填卖家ID（如果有历史数据）
UPDATE `review` r
INNER JOIN `order` o ON r.order_id = o.id
SET r.seller_id = o.seller_id
WHERE r.seller_id IS NULL;

-- 修改为非空约束
ALTER TABLE `review` 
MODIFY COLUMN `seller_id` BIGINT NOT NULL COMMENT '卖家ID';

-- 添加索引和外键
ALTER TABLE `review`
ADD KEY `idx_seller_id` (`seller_id`),
ADD CONSTRAINT `fk_review_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`);
