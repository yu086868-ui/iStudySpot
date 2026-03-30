-- 创建支付表（新表）
CREATE TABLE IF NOT EXISTS `payment` (
                                         `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
                                         `payment_no` VARCHAR(32) UNIQUE NOT NULL COMMENT '支付流水号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：wechat, alipay, balance',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：pending, success, failed',
    `payment_url` VARCHAR(500) COMMENT '支付链接',
    `pay_time` DATETIME COMMENT '支付时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付表';