CREATE TABLE IF NOT EXISTS `price_strategy` (
                                                `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '策略ID',
                                                `room_id` BIGINT COMMENT '自习室ID（null表示全局）',
                                                `area_id` BIGINT COMMENT '区域ID（null表示全部）',
                                                `seat_type` TINYINT COMMENT '座位类型（null表示全部）',
                                                `week_days` VARCHAR(20) COMMENT '适用星期（1-7，逗号分隔）',
    `start_time` TIME NOT NULL COMMENT '开始时段',
    `end_time` TIME NOT NULL COMMENT '结束时段',
    `price` DECIMAL(10,2) NOT NULL COMMENT '每小时价格',
    `is_holiday` TINYINT(1) DEFAULT 0 COMMENT '是否节假日价格',
    `priority` INT DEFAULT 0 COMMENT '优先级（数字越小越优先）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`area_id`) REFERENCES `area` (`id`) ON DELETE CASCADE,
    INDEX `idx_room_area` (`room_id`, `area_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格策略表';