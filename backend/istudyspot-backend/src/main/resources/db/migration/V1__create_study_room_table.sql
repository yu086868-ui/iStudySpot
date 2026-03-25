CREATE TABLE IF NOT EXISTS `study_room` (
                                            `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自习室ID',
                                            `name` VARCHAR(100) NOT NULL COMMENT '自习室名称',
    `address` VARCHAR(200) COMMENT '地址',
    `latitude` DECIMAL(10,8) COMMENT '纬度',
    `longitude` DECIMAL(11,8) COMMENT '经度',
    `open_time` TIME COMMENT '开门时间',
    `close_time` TIME COMMENT '关门时间',
    `description` TEXT COMMENT '描述',
    `images` VARCHAR(2000) COMMENT '图片URL，逗号分隔',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-关闭 1-营业',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自习室表';