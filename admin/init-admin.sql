INSERT INTO `user` (`username`, `password`, `nickname`, `balance`, `credit_score`, `status`)
VALUES ('admin', 'f19b8dc2029cf707939e886e4b164681', '管理员', 0.00, 100, 1)
ON DUPLICATE KEY UPDATE username = username;
