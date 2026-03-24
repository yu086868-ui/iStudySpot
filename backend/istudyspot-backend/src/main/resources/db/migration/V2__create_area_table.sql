CREATE TABLE IF NOT EXISTS `area` (
                                      `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
                                      `room_id` BIGINT NOT NULL COMMENT '所属自习室ID',
                                      `name` VARCHAR(50) NOT NULL COMMENT '区域名称',
    `description` VARCHAR(200) COMMENT '区域描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
    INDEX `idx_room_id` (`room_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';