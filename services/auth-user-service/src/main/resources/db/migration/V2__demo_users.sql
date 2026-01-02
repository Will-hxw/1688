-- ============================================
-- Auth-User Service 演示数据
-- 从 demodata.sql 拆分的用户数据
-- 启动时删除原有数据（除了xiaoweihua/cqu），然后插入演示数据
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET CHARACTER SET utf8mb4;

-- 删除除管理员外的所有用户
DELETE FROM user WHERE username != 'xiaoweihua';

-- ============================================
-- 用户数据（密码: 123456）
-- ============================================
INSERT INTO user (id, username, password, nickname, avatar, role, status, created_at) VALUES
(2, 'zhangsan', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '张三同学', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', 'USER', 'ACTIVE', '2025-09-01 10:00:00'),
(3, 'lisi', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '李四学长', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', 'USER', 'ACTIVE', '2025-09-02 11:00:00'),
(4, 'wangwu', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '王五学姐', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', 'USER', 'ACTIVE', '2025-09-03 12:00:00'),
(5, 'zhaoliu', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '赵六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu', 'USER', 'ACTIVE', '2025-09-04 13:00:00'),
(6, 'sunqi', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '孙七学弟', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sunqi', 'USER', 'ACTIVE', '2025-09-05 14:00:00'),
(7, 'zhouba', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '周八', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhouba', 'USER', 'ACTIVE', '2025-09-06 15:00:00'),
(8, 'wujiu', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '吴九学妹', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wujiu', 'USER', 'ACTIVE', '2025-09-07 16:00:00'),
(9, 'zhengshi', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '郑十', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhengshi', 'USER', 'ACTIVE', '2025-09-08 17:00:00'),
(10, 'qianyi', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '钱一学长', 'https://api.dicebear.com/7.x/avataaars/svg?seed=qianyi', 'USER', 'ACTIVE', '2025-09-09 18:00:00'),
(11, 'fenger', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '冯二', 'https://api.dicebear.com/7.x/avataaars/svg?seed=fenger', 'USER', 'ACTIVE', '2025-09-10 19:00:00'),
(12, 'chensan', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '陈三学姐', 'https://api.dicebear.com/7.x/avataaars/svg?seed=chensan', 'USER', 'ACTIVE', '2025-09-11 20:00:00'),
(13, 'weisi', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '魏四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=weisi', 'USER', 'ACTIVE', '2025-09-12 21:00:00'),
(14, 'jiangwu', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '蒋五学弟', 'https://api.dicebear.com/7.x/avataaars/svg?seed=jiangwu', 'USER', 'ACTIVE', '2025-09-13 22:00:00'),
(15, 'shenliu', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '沈六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=shenliu', 'USER', 'ACTIVE', '2025-09-14 23:00:00'),
(16, 'hanqi', '$2a$10$HT1UMEwqMecADkDSfe4OqeMj036TFsJaU/p7DDUuUyK282GC4gdwm', '韩七学妹', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hanqi', 'USER', 'ACTIVE', '2025-09-15 10:00:00');

-- 验证数据
SELECT 'Auth-User Service Demo Data Loaded' AS info;
SELECT COUNT(*) AS user_count FROM user;
