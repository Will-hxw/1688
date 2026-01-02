-- ============================================
-- Order Service 演示数据
-- 从 demodata.sql 拆分的订单数据
-- 启动时删除原有数据，然后插入演示数据
-- 注意：订单表包含商品快照字段（product_name, product_image）
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- 删除所有订单数据
DELETE FROM `order`;

-- ============================================
-- 订单数据（25个，包含商品快照信息）
-- ============================================
INSERT INTO `order` (id, buyer_id, seller_id, product_id, price, product_name, product_image, status, idempotency_key, canceled_by, canceled_at, created_at, updated_at) VALUES
(1, 7, 3, 4, 1200.00, 'AirPods Pro 2代', 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=400&h=400&fit=crop', 'REVIEWED', 'key-001', NULL, NULL, '2025-11-01 10:00:00', '2025-11-05 15:00:00'),
(2, 8, 3, 14, 20.00, '线性代数同济第六版', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=400&fit=crop', 'REVIEWED', 'key-002', NULL, NULL, '2025-11-02 11:00:00', '2025-11-06 16:00:00'),
(3, 9, 5, 24, 450.00, 'Nike Air Force 1白色42码', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop', 'REVIEWED', 'key-003', NULL, NULL, '2025-11-03 12:00:00', '2025-11-07 17:00:00'),
(4, 10, 10, 34, 80.00, '小熊加湿器4L大容量', 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=400&h=400&fit=crop', 'REVIEWED', 'key-004', NULL, NULL, '2025-11-04 13:00:00', '2025-11-08 18:00:00'),
(5, 11, 2, 31, 120.00, '小米台灯Pro护眼灯', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400&h=400&fit=crop', 'REVIEWED', 'key-005', NULL, NULL, '2025-11-05 14:00:00', '2025-11-09 19:00:00'),
(6, 12, 2, 1, 3800.00, '二手iPhone 13 128GB', 'https://images.unsplash.com/photo-1632633173522-47456de71b76?w=400&h=400&fit=crop', 'RECEIVED', 'key-006', NULL, NULL, '2025-11-10 10:00:00', '2025-11-15 15:00:00'),
(7, 13, 4, 5, 1500.00, '索尼WH-1000XM4头戴耳机', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=400&h=400&fit=crop', 'RECEIVED', 'key-007', NULL, NULL, '2025-11-11 11:00:00', '2025-11-16 16:00:00'),
(8, 14, 5, 7, 2200.00, '任天堂Switch OLED版', 'https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=400&h=400&fit=crop', 'RECEIVED', 'key-008', NULL, NULL, '2025-11-12 12:00:00', '2025-11-17 17:00:00'),
(9, 15, 6, 17, 80.00, '算法导论第三版', 'https://images.unsplash.com/photo-1550399105-c4db5fb85c18?w=400&h=400&fit=crop', 'RECEIVED', 'key-009', NULL, NULL, '2025-11-13 13:00:00', '2025-11-18 18:00:00'),
(10, 16, 5, 16, 55.00, 'C++ Primer Plus第6版', 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&h=400&fit=crop', 'RECEIVED', 'key-010', NULL, NULL, '2025-11-14 14:00:00', '2025-11-19 19:00:00'),
(11, 7, 2, 2, 5500.00, 'MacBook Air M1 2020款', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop', 'SHIPPED', 'key-011', NULL, NULL, '2025-11-20 10:00:00', '2025-11-21 11:00:00'),
(12, 8, 3, 3, 3600.00, 'iPad Air 5 64GB WiFi版', 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&h=400&fit=crop', 'SHIPPED', 'key-012', NULL, NULL, '2025-11-21 11:00:00', '2025-11-22 12:00:00'),
(13, 9, 4, 6, 3200.00, '小米14 Pro 12+256GB', 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=400&h=400&fit=crop', 'SHIPPED', 'key-013', NULL, NULL, '2025-11-22 12:00:00', '2025-11-23 13:00:00'),
(14, 10, 6, 38, 150.00, '尤克里里23寸桃花心木', 'https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=400&h=400&fit=crop', 'SHIPPED', 'key-014', NULL, NULL, '2025-11-23 13:00:00', '2025-11-24 14:00:00'),
(15, 11, 5, 8, 450.00, '罗技G Pro X机械键盘', 'https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=400&h=400&fit=crop', 'SHIPPED', 'key-015', NULL, NULL, '2025-11-24 14:00:00', '2025-11-25 15:00:00'),
(16, 12, 4, 15, 40.00, '大学物理马文蔚第六版', 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400&h=400&fit=crop', 'CREATED', 'key-016', NULL, NULL, '2025-12-01 10:00:00', '2025-12-01 10:00:00'),
(17, 13, 2, 13, 35.00, '高等数学同济第七版上下册', 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=400&fit=crop', 'CREATED', 'key-017', NULL, NULL, '2025-12-02 11:00:00', '2025-12-02 11:00:00'),
(18, 14, 6, 25, 320.00, 'Adidas运动套装M码', 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=400&h=400&fit=crop', 'CREATED', 'key-018', NULL, NULL, '2025-12-03 12:00:00', '2025-12-03 12:00:00'),
(19, 15, 5, 37, 800.00, '捷安特山地自行车ATX', 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=400&h=400&fit=crop', 'CREATED', 'key-019', NULL, NULL, '2025-12-04 13:00:00', '2025-12-04 13:00:00'),
(20, 16, 2, 1, 3800.00, '二手iPhone 13 128GB', 'https://images.unsplash.com/photo-1632633173522-47456de71b76?w=400&h=400&fit=crop', 'CREATED', 'key-020', NULL, NULL, '2025-12-05 14:00:00', '2025-12-05 14:00:00'),
(21, 7, 4, 5, 1500.00, '索尼WH-1000XM4头戴耳机', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=400&h=400&fit=crop', 'CANCELED', 'key-021', 'BUYER', '2025-11-06 10:00:00', '2025-11-05 09:00:00', '2025-11-06 10:00:00'),
(22, 8, 6, 17, 80.00, '算法导论第三版', 'https://images.unsplash.com/photo-1550399105-c4db5fb85c18?w=400&h=400&fit=crop', 'CANCELED', 'key-022', 'SELLER', '2025-11-07 11:00:00', '2025-11-06 10:00:00', '2025-11-07 11:00:00'),
(23, 9, 2, 2, 5500.00, 'MacBook Air M1 2020款', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop', 'CANCELED', 'key-023', 'BUYER', '2025-11-08 12:00:00', '2025-11-07 11:00:00', '2025-11-08 12:00:00'),
(24, 10, 3, 3, 3600.00, 'iPad Air 5 64GB WiFi版', 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&h=400&fit=crop', 'CANCELED', 'key-024', 'SELLER', '2025-11-09 13:00:00', '2025-11-08 12:00:00', '2025-11-09 13:00:00'),
(25, 11, 5, 7, 2200.00, '任天堂Switch OLED版', 'https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=400&h=400&fit=crop', 'CANCELED', 'key-025', 'BUYER', '2025-11-10 14:00:00', '2025-11-09 13:00:00', '2025-11-10 14:00:00');

-- 验证数据
SELECT 'Order Service Demo Data Loaded' AS info;
SELECT status, COUNT(*) AS count FROM `order` GROUP BY status;
