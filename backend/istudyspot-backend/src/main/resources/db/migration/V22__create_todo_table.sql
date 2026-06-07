CREATE TABLE IF NOT EXISTS `todo` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `priority` INT NOT NULL DEFAULT 2 COMMENT '1-高 2-中 3-低',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/completed',
    `due_time` DATETIME DEFAULT NULL,
    `order_id` BIGINT DEFAULT NULL,
    `completed_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_todo_user_id` (`user_id`),
    INDEX `idx_todo_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习待办表';
