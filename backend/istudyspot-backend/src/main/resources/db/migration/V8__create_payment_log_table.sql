CREATE TABLE IF NOT EXISTS `payment_log` (
                                             `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
                                             `order_id` BIGINT NOT NULL COMMENT '订单ID',
                                             `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                             `pay_no` VARCHAR(64) UNIQUE COMMENT '支付平台流水号',
    `pay_type` TINYINT NOT NULL COMMENT '支付方式：1-余额 2-微信 3-支付宝',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `status` TINYINT NOT NULL COMMENT '状态：0-失败 1-成功 2-退款',
    `pay_time` DATETIME COMMENT '支付时间',
    `refund_time` DATETIME COMMENT '退款时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';