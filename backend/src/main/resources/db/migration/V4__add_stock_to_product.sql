-- V4: 为商品表添加库存字段
-- 支持多库存商品销售，库存为0时商品状态变为SOLD

ALTER TABLE `product` 
ADD COLUMN `stock` INT NOT NULL DEFAULT 1 COMMENT '库存数量' AFTER `category`;

-- 为库存字段添加索引（用于搜索有库存的商品）
ALTER TABLE `product` ADD INDEX `idx_stock` (`stock`);

-- 将已售出商品的库存设为0
UPDATE `product` SET `stock` = 0 WHERE `status` = 'SOLD';
