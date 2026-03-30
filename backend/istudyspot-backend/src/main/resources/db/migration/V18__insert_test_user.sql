-- 插入测试用户（密码：123456）
INSERT INTO `user` (`username`, `password`, `nickname`, `balance`) VALUES
    ('test', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', 100.00)
    ON DUPLICATE KEY UPDATE username = username;

-- 更新现有用户的 username（如果 openid 存在）
UPDATE `user` SET username = CONCAT('user_', id) WHERE username IS NULL;