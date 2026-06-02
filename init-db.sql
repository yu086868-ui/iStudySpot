SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- V1: 自习室表
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

-- V2: 区域表
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

-- V3: 座位表
CREATE TABLE IF NOT EXISTS `seat` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '座位ID',
    `room_id` BIGINT NOT NULL COMMENT '自习室ID',
    `area_id` BIGINT COMMENT '区域ID',
    `seat_number` VARCHAR(20) NOT NULL COMMENT '座位编号',
    `seat_type` TINYINT COMMENT '类型：1-普通 2-沙发 3-隔间 4-包厢',
    `row_num` INT COMMENT '座位图行坐标',
    `col_num` INT COMMENT '座位图列坐标',
    `has_power` TINYINT(1) DEFAULT 0 COMMENT '是否有电源',
    `has_lamp` TINYINT(1) DEFAULT 0 COMMENT '是否有台灯',
    `is_window` TINYINT(1) DEFAULT 0 COMMENT '是否靠窗',
    `is_quiet` TINYINT(1) DEFAULT 0 COMMENT '是否静音区',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用 2-维护中',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`area_id`) REFERENCES `area` (`id`) ON DELETE SET NULL,
    INDEX `idx_room_id` (`room_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- V4: 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(64) UNIQUE COMMENT '微信openid（可选）',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    `points` INT DEFAULT 0 COMMENT '积分',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `violation_count` INT DEFAULT 0 COMMENT '违规次数',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_openid` (`openid`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- V4+V13: 用户表补充字段（username, password, email, avatar 已合并到建表语句）

ALTER TABLE `user`
    ADD COLUMN `username` VARCHAR(50) UNIQUE COMMENT '用户名' AFTER `id`,
    ADD COLUMN `password` VARCHAR(100) COMMENT '密码(MD5加密)' AFTER `username`;

-- V5: 价格策略表
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

-- V6: 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) UNIQUE NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `seat_id` BIGINT NOT NULL COMMENT '座位ID',
    `seat_number` VARCHAR(20) COMMENT '座位编号（冗余）',
    `room_id` BIGINT NOT NULL COMMENT '自习室ID',
    `study_room_name` VARCHAR(100) COMMENT '自习室名称',
    `seat_position` VARCHAR(20) COMMENT '座位位置',
    `room_name` VARCHAR(100) COMMENT '自习室名称（冗余）',
    `plan_start_time` DATETIME NOT NULL COMMENT '计划开始时间',
    `plan_end_time` DATETIME NOT NULL COMMENT '计划结束时间',
    `actual_start_time` DATETIME COMMENT '实际签到时间',
    `actual_end_time` DATETIME COMMENT '实际签退时间',
    `total_hours` DECIMAL(5,1) COMMENT '总时长（小时）',
    `unit_price` DECIMAL(10,2) COMMENT '平均单价',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
    `total_price` DECIMAL(10,2) COMMENT '总价格',
    `pay_amount` DECIMAL(10,2) COMMENT '实付金额',
    `deposit` DECIMAL(10,2) DEFAULT 0 COMMENT '押金',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：pending, paid, in_use, completed, cancelled',
    `pay_type` TINYINT COMMENT '支付方式：1-余额 2-微信 3-支付宝',
    `pay_time` DATETIME COMMENT '支付时间',
    `cancel_reason` VARCHAR(255) COMMENT '取消原因',
    `cancel_time` DATETIME COMMENT '取消时间',
    `checkin_time` DATETIME COMMENT '签到时间',
    `checkout_time` DATETIME COMMENT '签退时间',
    `actual_duration` INT COMMENT '实际使用分钟数',
    `actual_price` DECIMAL(10,2) COMMENT '实际金额',
    `created_at` DATETIME COMMENT '创建时间',
    `updated_at` DATETIME COMMENT '更新时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_seat_id` (`seat_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_plan_time` (`plan_start_time`, `plan_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- V7: 订单明细表
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

-- V8: 支付流水表
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

-- V9: 座位状态流水表
CREATE TABLE IF NOT EXISTS `seat_status_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
    `seat_id` BIGINT NOT NULL COMMENT '座位ID',
    `order_id` BIGINT COMMENT '关联订单ID',
    `user_id` BIGINT COMMENT '使用人ID',
    `status` TINYINT NOT NULL COMMENT '状态：1-空闲 2-已预订 3-使用中 4-维护',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    INDEX `idx_seat_id` (`seat_id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位状态流水表';

-- V10: 黑名单表
CREATE TABLE IF NOT EXISTS `blacklist` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `room_id` BIGINT COMMENT '自习室ID（null表示全局）',
    `reason` VARCHAR(255) COMMENT '拉黑原因',
    `expire_time` DATETIME COMMENT '解除时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- V11: 初始化数据
INSERT INTO `study_room` (`name`, `address`, `open_time`, `close_time`, `description`) VALUES
    ('iStudySpot 学习空间（五道口店）', '北京市海淀区五道口购物中心3F', '08:00:00', '23:00:00', '临近清华北大，考研党聚集地'),
    ('iStudySpot 静心自习室（中关村店）', '北京市海淀区中关村创业大街B2', '09:00:00', '22:00:00', '互联网人充电好去处'),
    ('iStudySpot 24h自习舱（望京店）', '北京市朝阳区望京SOHO T2-3F', '00:00:00', '23:59:59', '24小时营业，满足深夜学习需求');

INSERT INTO `area` (`room_id`, `name`, `description`, `sort_order`) VALUES
    (1, '沉浸学习区', '完全静音，禁止交谈', 1),
    (1, '轻讨论区', '允许低声讨论', 2),
    (1, 'VIP包厢', '独立空间，适合小组学习', 3),
    (2, '阳光窗景区', '靠窗座位，光线充足', 1),
    (2, '标准学习区', '标准座位', 2),
    (3, '深夜加油区', '24小时开放区域', 1),
    (3, '休息充电区', '配备沙发和充电桩', 2);

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

INSERT INTO `price_strategy` (`room_id`, `area_id`, `seat_type`, `week_days`, `start_time`, `end_time`, `price`, `is_holiday`, `priority`) VALUES
    (1, NULL, NULL, '1,2,3,4,5', '08:00:00', '18:00:00', 15.00, 0, 1),
    (1, NULL, NULL, '1,2,3,4,5', '18:00:00', '23:00:00', 12.00, 0, 1),
    (1, NULL, NULL, '6,7', '08:00:00', '23:00:00', 18.00, 0, 1),
    (1, 3, NULL, NULL, '00:00:00', '23:59:59', 30.00, 0, 0),
    (1, NULL, NULL, NULL, '08:00:00', '23:00:00', 25.00, 1, 0);

-- V12: 视图
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
        WHEN o.id IS NOT NULL AND o.status = 'paid' THEN 'paid'
        WHEN o.id IS NOT NULL AND o.status = 'in_use' THEN 'using'
        WHEN o.id IS NOT NULL AND o.status = 'pending' THEN 'booked'
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
        AND o.status IN ('pending','paid','in_use')
        AND NOW() BETWEEN o.plan_start_time AND o.plan_end_time
WHERE s.status = 1;

CREATE OR REPLACE VIEW v_room_occupancy AS
SELECT
    o.room_id,
    r.name AS room_name,
    DATE(o.create_time) AS date,
    HOUR(o.create_time) AS hour,
    COUNT(*) AS order_count,
    SUM(o.total_hours) AS total_hours,
    SUM(o.pay_amount) AS revenue
FROM `order` o
    JOIN study_room r ON o.room_id = r.id
WHERE o.status = 'completed'
GROUP BY o.room_id, DATE(o.create_time), HOUR(o.create_time);

-- V14: 自习室表补充字段
ALTER TABLE `study_room`
    ADD COLUMN `rules` TEXT COMMENT '自习室规则' AFTER `description`,
    ADD COLUMN `image_url` VARCHAR(500) COMMENT '图片URL' AFTER `images`;

-- V15: 座位表补充字段
ALTER TABLE `seat`
    ADD COLUMN `price_per_hour` DECIMAL(10,2) DEFAULT 0.00 COMMENT '每小时价格' AFTER `status`,
    ADD COLUMN `description` VARCHAR(500) COMMENT '座位描述' AFTER `price_per_hour`;

UPDATE `seat` SET `price_per_hour` = 15.00 WHERE `seat_type` = 1;
UPDATE `seat` SET `price_per_hour` = 30.00 WHERE `seat_type` = 2;

-- V17: 支付表
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

-- V18: 测试用户（密码：123456）
INSERT INTO `user` (`username`, `password`, `nickname`, `balance`) VALUES
    ('test', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', 100.00)
    ON DUPLICATE KEY UPDATE username = username;

-- V19: 用户表补充字段
ALTER TABLE `user`
    ADD COLUMN `student_id` VARCHAR(20) DEFAULT NULL COMMENT '学号',
    ADD COLUMN `credit_score` INT DEFAULT 100 COMMENT '信用分数';

-- V20: AI卡片表
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

-- V21: 管理员用户（密码：admin@123456）
INSERT INTO `user` (`username`, `password`, `nickname`, `balance`, `credit_score`, `status`) VALUES
    ('admin', 'f19b8dc2029cf707939e886e4b164681', '管理员', 0.00, 100, 1)
    ON DUPLICATE KEY UPDATE username = username;

-- V22: 成就定义表
CREATE TABLE IF NOT EXISTS `achievement` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '成就编码',
    `name` VARCHAR(100) NOT NULL COMMENT '成就名称',
    `description` VARCHAR(500) NOT NULL COMMENT '成就描述',
    `icon` VARCHAR(50) DEFAULT '' COMMENT '图标标识',
    `category` VARCHAR(50) DEFAULT 'study' COMMENT '分类',
    `threshold` INT DEFAULT 0 COMMENT '解锁阈值',
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就定义表';

-- 成就初始数据
INSERT INTO `achievement` (`code`, `name`, `description`, `icon`, `category`, `threshold`) VALUES
('early_bird', '早起鸟', '连续3天在7:00-8:00签到', 'wb_sunny', 'study', 3),
('night_owl', '夜猫子', '连续3天在21:00后仍在学习', 'nights_stay', 'study', 3),
('study_master', '学霸', '累计学习100小时', 'school', 'study', 100),
('streak_king', '连击王', '连续打卡7天', 'local_fire_department', 'study', 7),
('punctual', '守时达人', '连续30天按时签到', 'schedule', 'study', 30),
('regular', '常客', '在同一座位学习10次', 'event_seat', 'study', 10),
('social', '社交达人', '推荐3位好友注册', 'people', 'social', 3),
('marathon', '马拉松', '单次学习超过6小时', 'directions_run', 'study', 360);

-- V23: 用户成就解锁记录表
CREATE TABLE IF NOT EXISTS `user_achievement` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `achievement_code` VARCHAR(50) NOT NULL COMMENT '成就编码',
    `unlocked_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_achievement_code` (`achievement_code`),
    UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就解锁记录表';

-- V24: 违规记录表
CREATE TABLE IF NOT EXISTS `violation_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '违规类型: no_show/late_checkin/overstay/unauthorized_transfer/other',
    `description` VARCHAR(500) NOT NULL COMMENT '违规描述',
    `related_order_id` BIGINT COMMENT '关联订单ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/appealing/appeal_approved/appeal_rejected/expired',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `appeal_time` DATETIME COMMENT '申诉时间',
    `appeal_reason` VARCHAR(500) COMMENT '申诉理由',
    `appeal_result` VARCHAR(500) COMMENT '申诉处理结果',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违规记录表';
