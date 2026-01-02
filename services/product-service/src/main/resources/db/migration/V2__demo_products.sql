-- ============================================
-- Product Service 演示数据
-- 从 demodata.sql 拆分的商品数据
-- 启动时删除原有数据，然后插入演示数据
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- 删除所有商品数据
DELETE FROM product;

-- ============================================
-- 商品数据（40个，使用真实图片）
-- ============================================
INSERT INTO product (id, seller_id, name, description, price, image_url, category, stock, status, created_at) VALUES
(1, 2, '二手iPhone 13 128GB', '9成新国行正品，无磕碰划痕，电池健康92%，配原装充电器', 3800.00, 'https://images.unsplash.com/photo-1632633173522-47456de71b76?w=400&h=400&fit=crop', '电子产品', 1, 'ON_SALE', '2025-10-01 10:00:00'),
(2, 2, 'MacBook Air M1 2020款', '8GB+256GB深空灰，使用一年无问题，送保护壳键盘膜', 5500.00, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop', '电子产品', 1, 'ON_SALE', '2025-10-02 11:00:00'),
(3, 3, 'iPad Air 5 64GB WiFi版', '全新未拆封蓝色，学生优惠购入低价转让带发票', 3600.00, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&h=400&fit=crop', '电子产品', 2, 'ON_SALE', '2025-10-03 12:00:00'),
(4, 3, 'AirPods Pro 2代', '95新降噪效果好，送硅胶保护套原装充电盒', 1200.00, 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=400&h=400&fit=crop', '电子产品', 0, 'SOLD', '2025-10-04 13:00:00'),
(5, 4, '索尼WH-1000XM4头戴耳机', '黑色降噪神器，使用半年功能完好送耳机包', 1500.00, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=400&h=400&fit=crop', '电子产品', 1, 'ON_SALE', '2025-10-05 14:00:00'),
(6, 4, '小米14 Pro 12+256GB', '白色99新，买了两个月换苹果了送手机壳', 3200.00, 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=400&h=400&fit=crop', '电子产品', 1, 'ON_SALE', '2025-10-06 15:00:00'),
(7, 5, '任天堂Switch OLED版', '白色带健身环和3个游戏卡带，9成新送收纳包', 2200.00, 'https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=400&h=400&fit=crop', '电子产品', 1, 'ON_SALE', '2025-10-07 16:00:00'),
(8, 5, '罗技G Pro X机械键盘', 'GX青轴RGB背光，使用3个月手感极佳送键帽', 450.00, 'https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=400&h=400&fit=crop', '电子产品', 3, 'ON_SALE', '2025-10-08 17:00:00'),
(9, 6, '戴尔U2723QE 4K显示器', '27寸Type-C一线连，在保送显示器支架', 3800.00, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=400&h=400&fit=crop', '电子产品', 1, 'ON_SALE', '2025-10-09 18:00:00'),
(10, 6, '佳能EOS M50 Mark II', '带15-45mm镜头，快门数2000送相机包SD卡', 4200.00, 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=400&h=400&fit=crop', '电子产品', 1, 'ON_SALE', '2025-10-10 19:00:00'),
(11, 12, '华为MatePad 11 2023款', '8+128GB星光银，带键盘手写笔学习神器', 2800.00, 'https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?w=400&h=400&fit=crop', '电子产品', 2, 'ON_SALE', '2025-10-11 20:00:00'),
(12, 13, '雷蛇蝰蛇V3专业版鼠标', '无线版黑色，使用2个月送鼠标垫手感好', 380.00, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&h=400&fit=crop', '电子产品', 5, 'ON_SALE', '2025-10-12 21:00:00'),
(13, 2, '高等数学同济第七版上下册', '有少量笔记不影响阅读，考研必备教材送习题册', 35.00, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=400&fit=crop', '书籍教材', 3, 'ON_SALE', '2025-10-13 10:00:00'),
(14, 3, '线性代数同济第六版', '9成新无笔记无划线，送习题解答考研用', 20.00, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=400&fit=crop', '书籍教材', 0, 'SOLD', '2025-10-14 11:00:00'),
(15, 4, '大学物理马文蔚第六版', '上下册全套有部分笔记，内容完整清晰', 40.00, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400&h=400&fit=crop', '书籍教材', 2, 'ON_SALE', '2025-10-15 12:00:00'),
(16, 5, 'C++ Primer Plus第6版', '中文版经典教材，适合入门学习有少量标注', 55.00, 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400&h=400&fit=crop', '书籍教材', 1, 'ON_SALE', '2025-10-16 13:00:00'),
(17, 6, '算法导论第三版', '黑皮书程序员必读，有少量标注内容完整', 80.00, 'https://images.unsplash.com/photo-1550399105-c4db5fb85c18?w=400&h=400&fit=crop', '书籍教材', 2, 'ON_SALE', '2025-10-17 14:00:00'),
(18, 14, '考研英语真题黄皮书2024', '全新未拆买多了一本，原价转让带答案解析', 45.00, 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&h=400&fit=crop', '书籍教材', 4, 'ON_SALE', '2025-10-18 15:00:00'),
(19, 15, '数据结构C语言版严蔚敏', '经典教材有课后习题答案，考研408必备', 25.00, 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&h=400&fit=crop', '书籍教材', 3, 'ON_SALE', '2025-10-19 16:00:00'),
(20, 16, '计算机网络谢希仁第八版', '全新买错版本了低价出，送课后答案', 38.00, 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400&h=400&fit=crop', '书籍教材', 1, 'ON_SALE', '2025-10-20 17:00:00');


INSERT INTO product (id, seller_id, name, description, price, image_url, category, stock, status, created_at) VALUES
(21, 10, '操作系统概念第九版', '英文原版适合考研学习，有笔记标注', 60.00, 'https://images.unsplash.com/photo-1491841550275-ad7854e35ca6?w=400&h=400&fit=crop', '书籍教材', 1, 'ON_SALE', '2025-10-21 18:00:00'),
(22, 11, '深入理解计算机系统CSAPP', '第三版程序员圣经，9成新内容完整', 75.00, 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=400&h=400&fit=crop', '书籍教材', 2, 'ON_SALE', '2025-10-22 19:00:00'),
(23, 4, '优衣库羽绒服男款L码', '黑色去年买的穿过几次，保暖效果好无破损', 280.00, 'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=400&h=400&fit=crop', '服饰鞋包', 1, 'ON_SALE', '2025-10-23 10:00:00'),
(24, 5, 'Nike Air Force 1白色42码', '经典款穿过5次左右，9成新送鞋盒', 450.00, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop', '服饰鞋包', 0, 'SOLD', '2025-10-24 11:00:00'),
(25, 6, 'Adidas运动套装M码', '黑色上衣+裤子，适合健身穿95新', 320.00, 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=400&h=400&fit=crop', '服饰鞋包', 2, 'ON_SALE', '2025-10-25 12:00:00'),
(26, 12, '重庆大学校服外套XL码', '全新买大了原价转让，CQU标志', 150.00, 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=400&h=400&fit=crop', '服饰鞋包', 1, 'ON_SALE', '2025-10-26 13:00:00'),
(27, 13, 'Levis牛仔裤32码', '经典501款穿过几次，版型很好无褪色', 180.00, 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=400&fit=crop', '服饰鞋包', 1, 'ON_SALE', '2025-10-27 14:00:00'),
(28, 14, '北面冲锋衣女款S码', '粉色防风防水，适合户外活动登山', 550.00, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=400&fit=crop', '服饰鞋包', 1, 'ON_SALE', '2025-10-28 15:00:00'),
(29, 15, 'New Balance 574灰色40码', '复古款穿过10次左右，8成新舒适', 280.00, 'https://images.unsplash.com/photo-1539185441755-769473a23570?w=400&h=400&fit=crop', '服饰鞋包', 1, 'ON_SALE', '2025-10-29 16:00:00'),
(30, 16, '优衣库摇粒绒外套女款M码', '浅蓝色超级软糯，穿过2次如新', 120.00, 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=400&h=400&fit=crop', '服饰鞋包', 2, 'ON_SALE', '2025-10-30 17:00:00'),
(31, 2, '小米台灯Pro护眼灯', '亮度可调使用半年，功能完好适合学习', 120.00, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400&h=400&fit=crop', '生活用品', 2, 'ON_SALE', '2025-11-01 10:00:00'),
(32, 3, '美的电饭煲3L智能款', '智能预约使用一年，内胆完好无划痕', 150.00, 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop', '生活用品', 1, 'ON_SALE', '2025-11-02 11:00:00'),
(33, 4, '宜家书架KALLAX白色4格', '可拆卸自提，送收纳盒宿舍必备', 200.00, 'https://images.unsplash.com/photo-1594620302200-9a762244a156?w=400&h=400&fit=crop', '生活用品', 1, 'ON_SALE', '2025-11-03 12:00:00'),
(34, 10, '小熊加湿器4L大容量', '静音设计使用3个月，宿舍办公室都适用', 80.00, 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=400&h=400&fit=crop', '生活用品', 0, 'SOLD', '2025-11-04 13:00:00'),
(35, 11, '飞利浦电动牙刷HX6730', '带2个刷头使用半年，电池续航好', 180.00, 'https://images.unsplash.com/photo-1609840114035-3c981b782dfe?w=400&h=400&fit=crop', '生活用品', 3, 'ON_SALE', '2025-11-05 14:00:00'),
(36, 12, '九阳豆浆机全自动', '可做豆浆米糊果汁，使用一年功能正常', 120.00, 'https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=400&h=400&fit=crop', '生活用品', 1, 'ON_SALE', '2025-11-06 15:00:00'),
(37, 5, '捷安特山地自行车ATX', '21速骑了一年保养良好，送车锁打气筒', 800.00, 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=400&h=400&fit=crop', '运动户外', 1, 'ON_SALE', '2025-11-07 10:00:00'),
(38, 6, '尤克里里23寸桃花心木', '送琴包调音器教程，适合入门学习', 150.00, 'https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=400&h=400&fit=crop', '其他', 2, 'ON_SALE', '2025-11-08 11:00:00'),
(39, 13, '瑜伽垫TPE材质6mm', '紫色送收纳袋，使用过几次无异味', 60.00, 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=400&h=400&fit=crop', '运动户外', 4, 'ON_SALE', '2025-11-09 12:00:00'),
(40, 14, '羽毛球拍尤尼克斯双拍', '送羽毛球拍包，适合业余爱好者', 180.00, 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=400&h=400&fit=crop', '运动户外', 3, 'ON_SALE', '2025-11-10 13:00:00');

-- 验证数据
SELECT 'Product Service Demo Data Loaded' AS info;
SELECT status, COUNT(*) AS count FROM product GROUP BY status;
