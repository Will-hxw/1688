-- CQU抽象集市 初始化管理员账户
-- V2: 插入默认管理员
-- 用户名: xiaoweihua, 密码: cqu (BCrypt加密)

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES (
    'xiaoweihua',
    -- 密码 'cqu' 的BCrypt加密结果
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    '系统管理员',
    'ADMIN',
    'ACTIVE'
);
