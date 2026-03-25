CREATE TABLE IF NOT EXISTS `order_detail` (
                                              `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
                                              `order_id` BIGINT NOT NULL COMMENT '订单ID',
                                              `start_time` DATETIME NOT NULL COMMENT '时段开始',
                                              `end_time` DATETIME NOT NULL COMMENT '时段结束',
                                              `hours` DECIMAL(3,1) NOT NULL COMMENT '小时数',
    `price` DECIMAL(10,2) NOT NULL COMMENT '当时价格',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '本时段金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
    INDEX `idx_order_id` (`order_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';