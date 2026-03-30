-- 修改用户表，添加 username 字段（用于用户名密码登录）
ALTER TABLE `user`
    ADD COLUMN `username` VARCHAR(50) UNIQUE COMMENT '用户名' AFTER `id`,
ADD COLUMN `password` VARCHAR(100) COMMENT '密码(MD5加密)' AFTER `username`,
ADD COLUMN `email` VARCHAR(100) COMMENT '邮箱' AFTER `phone`,
ADD COLUMN `avatar` VARCHAR(500) COMMENT '头像URL' AFTER `nickname`,
MODIFY COLUMN `openid` VARCHAR(64) NULL COMMENT '微信openid（可选）';