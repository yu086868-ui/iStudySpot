CREATE TABLE IF NOT EXISTS `blacklist` (
                                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
                                           `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                           `room_id` BIGINT COMMENT '自习室ID（null表示全局）',
                                           `reason` VARCHAR(255) COMMENT '拉黑原因',
    `expire_time` DATETIME COMMENT '解除时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_expire_time` (`expire_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';