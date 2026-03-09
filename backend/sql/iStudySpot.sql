-- =====================================================
-- iStudySpot 自习室预订系统数据库脚本
-- 版本：1.0
-- 说明：按依赖顺序创建所有表
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `iseatspace` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `iseatspace`;

-- =====================================================
-- 1. 自习室表 (study_room) - 无依赖
-- =====================================================
DROP TABLE IF EXISTS `study_room`;
CREATE TABLE `study_room` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '自习室ID',
  `name` varchar(100) NOT NULL COMMENT '自习室名称',
  `address` varchar(200) COMMENT '地址',
  `latitude` decimal(10,8) COMMENT '纬度',
  `longitude` decimal(11,8) COMMENT '经度',
  `open_time` time COMMENT '开门时间',
  `close_time` time COMMENT '关门时间',
  `description` text COMMENT '描述',
  `images` varchar(2000) COMMENT '图片URL，逗号分隔',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-关闭 1-营业',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自习室表';

-- =====================================================
-- 2. 区域表 (area) - 依赖 study_room
-- =====================================================
DROP TABLE IF EXISTS `area`;
CREATE TABLE `area` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
  `room_id` bigint NOT NULL COMMENT '所属自习室ID',
  `name` varchar(50) NOT NULL COMMENT '区域名称（如：静音区、讨论区）',
  `description` varchar(200) COMMENT '区域描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
  INDEX `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';

-- =====================================================
-- 3. 座位表 (seat) - 依赖 study_room, area
-- =====================================================
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '座位ID',
  `room_id` bigint NOT NULL COMMENT '自习室ID',
  `area_id` bigint COMMENT '区域ID',
  `seat_number` varchar(20) NOT NULL COMMENT '座位编号（如A01）',
  `seat_type` tinyint COMMENT '类型：1-普通 2-沙发 3-隔间 4-包厢',
  `row_num` int COMMENT '座位图行坐标',
  `col_num` int COMMENT '座位图列坐标',
  `has_power` tinyint(1) DEFAULT 0 COMMENT '是否有电源',
  `has_lamp` tinyint(1) DEFAULT 0 COMMENT '是否有台灯',
  `is_window` tinyint(1) DEFAULT 0 COMMENT '是否靠窗',
  `is_quiet` tinyint(1) DEFAULT 0 COMMENT '是否静音区',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用 1-启用 2-维护中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`area_id`) REFERENCES `area` (`id`) ON DELETE SET NULL,
  INDEX `idx_room_id` (`room_id`),
  INDEX `idx_area_id` (`area_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- =====================================================
-- 4. 用户表 (user) - 无依赖
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(64) UNIQUE NOT NULL COMMENT '微信openid',
  `nickname` varchar(100) COMMENT '昵称',
  `avatar_url` varchar(500) COMMENT '头像URL',
  `phone` varchar(20) COMMENT '手机号',
  `balance` decimal(10,2) DEFAULT 0.00 COMMENT '账户余额',
  `points` int DEFAULT 0 COMMENT '积分',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `violation_count` int DEFAULT 0 COMMENT '违规次数',
  `last_login_time` datetime COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_openid` (`openid`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 5. 价格策略表 (price_strategy) - 依赖 study_room, area
-- =====================================================
DROP TABLE IF EXISTS `price_strategy`;
CREATE TABLE `price_strategy` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '策略ID',
  `room_id` bigint COMMENT '自习室ID（null表示全局）',
  `area_id` bigint COMMENT '区域ID（null表示全部）',
  `seat_type` tinyint COMMENT '座位类型（null表示全部）',
  `week_days` varchar(20) COMMENT '适用星期（1-7，逗号分隔，如"1,2,3,4,5"表示工作日）',
  `start_time` time NOT NULL COMMENT '开始时段',
  `end_time` time NOT NULL COMMENT '结束时段',
  `price` decimal(10,2) NOT NULL COMMENT '每小时价格',
  `is_holiday` tinyint(1) DEFAULT 0 COMMENT '是否节假日价格',
  `priority` int DEFAULT 0 COMMENT '优先级（数字越小越优先）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`area_id`) REFERENCES `area` (`id`) ON DELETE CASCADE,
  INDEX `idx_room_area` (`room_id`, `area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格策略表';

-- =====================================================
-- 6. 订单表 (order) - 依赖 user, seat
-- =====================================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) UNIQUE NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seat_id` bigint NOT NULL COMMENT '座位ID',
  `seat_number` varchar(20) COMMENT '座位编号（冗余）',
  `room_id` bigint NOT NULL COMMENT '自习室ID',
  `room_name` varchar(100) COMMENT '自习室名称（冗余）',
  `plan_start_time` datetime NOT NULL COMMENT '计划开始时间',
  `plan_end_time` datetime NOT NULL COMMENT '计划结束时间',
  `actual_start_time` datetime COMMENT '实际签到时间',
  `actual_end_time` datetime COMMENT '实际签退时间',
  `total_hours` decimal(5,1) COMMENT '总时长（小时）',
  `unit_price` decimal(10,2) COMMENT '平均单价',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `pay_amount` decimal(10,2) COMMENT '实付金额',
  `deposit` decimal(10,2) DEFAULT 0 COMMENT '押金',
  `status` tinyint NOT NULL COMMENT '状态：1-待支付 2-已支付待使用 3-使用中 4-已完成 5-已取消 6-退款中 7-已退款',
  `pay_type` tinyint COMMENT '支付方式：1-余额 2-微信 3-支付宝',
  `pay_time` datetime COMMENT '支付时间',
  `cancel_reason` varchar(255) COMMENT '取消原因',
  `cancel_time` datetime COMMENT '取消时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_seat_id` (`seat_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_plan_time` (`plan_start_time`, `plan_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =====================================================
-- 7. 订单明细表 (order_detail) - 依赖 order
-- =====================================================
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `start_time` datetime NOT NULL COMMENT '时段开始',
  `end_time` datetime NOT NULL COMMENT '时段结束',
  `hours` decimal(3,1) NOT NULL COMMENT '小时数',
  `price` decimal(10,2) NOT NULL COMMENT '当时价格',
  `amount` decimal(10,2) NOT NULL COMMENT '本时段金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- =====================================================
-- 8. 支付流水表 (payment_log) - 依赖 order, user
-- =====================================================
DROP TABLE IF EXISTS `payment_log`;
CREATE TABLE `payment_log` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `pay_no` varchar(64) UNIQUE COMMENT '支付平台流水号',
  `pay_type` tinyint NOT NULL COMMENT '支付方式：1-余额 2-微信 3-支付宝',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` tinyint NOT NULL COMMENT '状态：0-失败 1-成功 2-退款',
  `pay_time` datetime COMMENT '支付时间',
  `refund_time` datetime COMMENT '退款时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';

-- =====================================================
-- 9. 座位状态流水表 (seat_status_log) - 依赖 seat, order, user
-- =====================================================
DROP TABLE IF EXISTS `seat_status_log`;
CREATE TABLE `seat_status_log` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
  `seat_id` bigint NOT NULL COMMENT '座位ID',
  `order_id` bigint COMMENT '关联订单ID',
  `user_id` bigint COMMENT '使用人ID',
  `status` tinyint NOT NULL COMMENT '状态：1-空闲 2-已预订 3-使用中 4-维护',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE SET NULL,
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  INDEX `idx_seat_id` (`seat_id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位状态流水表';

-- =====================================================
-- 10. 黑名单表 (blacklist) - 依赖 user, study_room
-- =====================================================
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `room_id` bigint COMMENT '自习室ID（null表示全局）',
  `reason` varchar(255) COMMENT '拉黑原因',
  `expire_time` datetime COMMENT '解除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- =====================================================
-- 初始化数据：插入示例自习室
-- =====================================================
INSERT INTO `study_room` (`name`, `address`, `open_time`, `close_time`, `description`) VALUES
('iStudySpot 学习空间（五道口店）', '北京市海淀区五道口购物中心3F', '08:00:00', '23:00:00', '临近清华北大，考研党聚集地'),
('iStudySpot 静心自习室（中关村店）', '北京市海淀区中关村创业大街B2', '09:00:00', '22:00:00', '互联网人充电好去处'),
('iStudySpot 24h自习舱（望京店）', '北京市朝阳区望京SOHO T2-3F', '00:00:00', '23:59:59', '24小时营业，满足深夜学习需求');

-- =====================================================
-- 初始化数据：插入区域
-- =====================================================
INSERT INTO `area` (`room_id`, `name`, `description`, `sort_order`) VALUES
(1, '沉浸学习区', '完全静音，禁止交谈', 1),
(1, '轻讨论区', '允许低声讨论', 2),
(1, 'VIP包厢', '独立空间，适合小组学习', 3),
(2, '阳光窗景区', '靠窗座位，光线充足', 1),
(2, '标准学习区', '标准座位', 2),
(3, '深夜加油区', '24小时开放区域', 1),
(3, '休息充电区', '配备沙发和充电桩', 2);

-- =====================================================
-- 初始化数据：插入座位（以自习室1为例）
-- =====================================================
INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`) VALUES
(1, 1, 'A01', 1, 1, 1, 1, 1, 0, 1),
(1, 1, 'A02', 1, 1, 2, 1, 1, 0, 1),
(1, 1, 'A03', 1, 1, 3, 1, 1, 1, 1),
(1, 1, 'A04', 1, 1, 4, 1, 1, 1, 1),
(1, 1, 'A05', 1, 1, 5, 1, 0, 0, 1),
(1, 2, 'B01', 1, 2, 1, 1, 0, 0, 0),
(1, 2, 'B02', 1, 2, 2, 1, 0, 0, 0),
(1, 2, 'B03', 2, 2, 3, 1, 1, 0, 0),
(1, 2, 'B04', 2, 2, 4, 1, 1, 1, 0),
(1, 3, 'C01', 3, 3, 1, 1, 1, 0, 1),
(1, 3, 'C02', 3, 3, 2, 1, 1, 1, 1);

-- =====================================================
-- 初始化数据：插入价格策略
-- =====================================================
INSERT INTO `price_strategy` (`room_id`, `area_id`, `seat_type`, `week_days`, `start_time`, `end_time`, `price`, `is_holiday`, `priority`) VALUES
-- 工作日白天价格
(1, NULL, NULL, '1,2,3,4,5', '08:00:00', '18:00:00', 15.00, 0, 1),
-- 工作日晚上价格
(1, NULL, NULL, '1,2,3,4,5', '18:00:00', '23:00:00', 12.00, 0, 1),
-- 周末全天价格
(1, NULL, NULL, '6,7', '08:00:00', '23:00:00', 18.00, 0, 1),
-- VIP包厢特殊价格
(1, 3, NULL, NULL, '00:00:00', '23:59:59', 30.00, 0, 0),
-- 节假日价格
(1, NULL, NULL, NULL, '08:00:00', '23:00:00', 25.00, 1, 0);

-- =====================================================
-- 创建视图：方便查询座位实时状态
-- =====================================================
CREATE OR REPLACE VIEW v_seat_current_status AS
SELECT 
    s.id AS seat_id,
    s.seat_number,
    s.room_id,
    r.name AS room_name,
    a.name AS area_name,
    s.seat_type,
    s.has_power,
    s.has_lamp,
    s.is_window,
    s.is_quiet,
    s.row_num,
    s.col_num,
    CASE 
        WHEN o.id IS NOT NULL AND o.status = 2 THEN 'paid'        -- 已支付待使用
        WHEN o.id IS NOT NULL AND o.status = 3 THEN 'using'       -- 使用中
        WHEN o.id IS NOT NULL AND o.status = 1 THEN 'booked'      -- 待支付
        ELSE 'free'
    END AS current_status,
    o.id AS current_order_id,
    o.plan_start_time,
    o.plan_end_time,
    o.user_id
FROM seat s
JOIN study_room r ON s.room_id = r.id
LEFT JOIN area a ON s.area_id = a.id
LEFT JOIN `order` o ON s.id = o.seat_id 
    AND o.status IN (1,2,3) 
    AND NOW() BETWEEN o.plan_start_time AND o.plan_end_time
WHERE s.status = 1;

-- =====================================================
-- 创建存储过程：计算订单价格
-- =====================================================
DELIMITER $$

CREATE PROCEDURE sp_calculate_order_price(
    IN p_room_id BIGINT,
    IN p_seat_id BIGINT,
    IN p_start_time DATETIME,
    IN p_end_time DATETIME,
    OUT p_total_amount DECIMAL(10,2)
)
BEGIN
    DECLARE v_area_id BIGINT;
    DECLARE v_seat_type TINYINT;
    DECLARE v_current_time DATETIME;
    DECLARE v_hour_price DECIMAL(10,2);
    DECLARE v_hours DECIMAL(3,1);
    
    -- 获取座位信息
    SELECT area_id, seat_type INTO v_area_id, v_seat_type
    FROM seat WHERE id = p_seat_id;
    
    SET p_total_amount = 0;
    SET v_current_time = p_start_time;
    
    -- 按小时循环计算
    WHILE v_current_time < p_end_time DO
        -- 获取当前小时的价格
        SELECT price INTO v_hour_price
        FROM price_strategy
        WHERE (room_id = p_room_id OR room_id IS NULL)
            AND (area_id = v_area_id OR area_id IS NULL)
            AND (seat_type = v_seat_type OR seat_type IS NULL)
            AND start_time <= TIME(v_current_time)
            AND end_time > TIME(v_current_time)
            AND (week_days IS NULL OR FIND_IN_SET(DAYOFWEEK(v_current_time), week_days))
            AND (is_holiday = 0)  -- 简化处理，实际需判断节假日
        ORDER BY priority ASC, price DESC
        LIMIT 1;
        
        -- 如果没有找到价格，使用默认价格
        IF v_hour_price IS NULL THEN
            SET v_hour_price = 10.00;
        END IF;
        
        -- 计算本小时的价格（可能不足1小时）
        IF DATE_ADD(v_current_time, INTERVAL 1 HOUR) <= p_end_time THEN
            SET p_total_amount = p_total_amount + v_hour_price;
            SET v_current_time = DATE_ADD(v_current_time, INTERVAL 1 HOUR);
        ELSE
            SET v_hours = TIMESTAMPDIFF(MINUTE, v_current_time, p_end_time) / 60;
            SET p_total_amount = p_total_amount + (v_hour_price * v_hours);
            SET v_current_time = p_end_time;
        END IF;
    END WHILE;
END$$

DELIMITER ;

-- =====================================================
-- 创建触发器：更新订单总时长
-- =====================================================
DELIMITER $$

CREATE TRIGGER trg_order_before_update
BEFORE UPDATE ON `order`
FOR EACH ROW
BEGIN
    IF NEW.actual_start_time IS NOT NULL AND NEW.actual_end_time IS NOT NULL THEN
        SET NEW.total_hours = TIMESTAMPDIFF(MINUTE, NEW.actual_start_time, NEW.actual_end_time) / 60;
    END IF;
END$$

DELIMITER ;

-- =====================================================
-- 添加注释说明
-- =====================================================
SELECT 'iStudySpot 数据库初始化完成！' AS '提示';

-- 查看所有表
SHOW TABLES;