-- ============================================
-- Review Service 演示数据
-- 从 demodata.sql 拆分的评价数据
-- 启动时删除原有数据，然后插入演示数据
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- 删除所有评价数据
DELETE FROM review;

-- ============================================
-- 评价数据（5条）
-- ============================================
INSERT INTO review (id, order_id, product_id, buyer_id, seller_id, rating, content, deleted, created_at) VALUES
(1, 1, 4, 7, 3, 5, '耳机成色很新，降噪效果确实不错，卖家发货很快，好评！', 0, '2025-11-05 15:00:00'),
(2, 2, 14, 8, 3, 5, '书本保存完好，没有缺页，考研复习用正合适，感谢学长！', 0, '2025-11-06 16:00:00'),
(3, 3, 24, 9, 5, 4, '鞋子很干净，就是有点小瑕疵，不过价格实惠，总体满意', 0, '2025-11-07 17:00:00'),
(4, 4, 34, 10, 10, 5, '加湿器很好用，静音效果不错，宿舍用刚刚好', 0, '2025-11-08 18:00:00'),
(5, 5, 31, 11, 2, 5, '台灯护眼效果很好，亮度调节方便，学习必备神器！', 0, '2025-11-09 19:00:00');

-- 验证数据
SELECT 'Review Service Demo Data Loaded' AS info;
SELECT COUNT(*) AS review_count, ROUND(AVG(rating), 1) AS avg_rating FROM review WHERE deleted = 0;
