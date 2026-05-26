CREATE TABLE IF NOT EXISTS `card` (
    `uuid` VARCHAR(36) PRIMARY KEY COMMENT '卡片唯一ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `card_id` VARCHAR(8) NOT NULL COMMENT '卡片模板/生成编号',
    `create_time` DATETIME NOT NULL COMMENT '生成时间',
    `study_duration` INT NOT NULL COMMENT '学习时长（分钟）',
    `rarity` VARCHAR(10) NOT NULL COMMENT '稀有度',
    `border_theme` VARCHAR(50) NOT NULL COMMENT '边框主题',
    `card_theme` VARCHAR(50) NOT NULL COMMENT '卡面主题',
    `theme_category` VARCHAR(50) NOT NULL COMMENT '内容主题',
    `markdown` TEXT NOT NULL COMMENT 'AI文本',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片地址',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI卡片表';