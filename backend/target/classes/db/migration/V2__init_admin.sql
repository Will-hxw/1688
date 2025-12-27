-- CQU抽象集市 初始化管理员账户
-- V2: 插入默认管理员
-- 用户名: xiaoweihua, 密码: cqu (BCrypt 10轮加密)

INSERT IGNORE INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES (
    'xiaoweihua',
    '$2a$10$I5MyzYa9/kq8eBVwpncDBuKu6xKvGd0nrfduY3p5gH87ves.RN38C',
    '系统管理员',
    'ADMIN',
    'ACTIVE'
);
