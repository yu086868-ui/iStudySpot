CREATE TABLE IF NOT EXISTS `seat_status_log` (
                                                 `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
                                                 `seat_id` BIGINT NOT NULL COMMENT '座位ID',
                                                 `order_id` BIGINT COMMENT '关联订单ID',
                                                 `user_id` BIGINT COMMENT '使用人ID',
                                                 `status` TINYINT NOT NULL COMMENT '状态：1-空闲 2-已预订 3-使用中 4-维护',
                                                 `start_time` DATETIME NOT NULL COMMENT '开始时间',
                                                 `end_time` DATETIME COMMENT '结束时间',
                                                 `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                 FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    INDEX `idx_seat_id` (`seat_id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_time_range` (`start_time`, `end_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位状态流水表';