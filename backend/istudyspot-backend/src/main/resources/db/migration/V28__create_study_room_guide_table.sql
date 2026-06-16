CREATE TABLE IF NOT EXISTS `study_room_guide` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '导览ID',
    `study_room_id` BIGINT NOT NULL COMMENT '自习室ID',
    `contact_info` TEXT NOT NULL COMMENT '场馆联系方式',
    `learning_areas` TEXT NOT NULL COMMENT '学习区域说明',
    `convenience_facilities` TEXT NOT NULL COMMENT '便利设施说明',
    `transportation_guide` TEXT NOT NULL COMMENT '交通指南',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_guide_study_room_id` (`study_room_id`),
    CONSTRAINT `fk_guide_study_room_id`
        FOREIGN KEY (`study_room_id`) REFERENCES `study_room` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自习室导览信息表';
