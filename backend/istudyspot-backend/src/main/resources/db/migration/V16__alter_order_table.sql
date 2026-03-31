-- 修改订单表
ALTER TABLE `order`
    ADD COLUMN `study_room_name` VARCHAR(100) COMMENT '自习室名称' AFTER `room_id`,
ADD COLUMN `seat_position` VARCHAR(20) COMMENT '座位位置' AFTER `seat_number`,
ADD COLUMN `checkin_time` DATETIME COMMENT '签到时间' AFTER `status`,
ADD COLUMN `checkout_time` DATETIME COMMENT '签退时间' AFTER `checkin_time`,
ADD COLUMN `actual_duration` INT COMMENT '实际使用分钟数' AFTER `checkout_time`,
ADD COLUMN `actual_price` DECIMAL(10,2) COMMENT '实际金额' AFTER `actual_duration`,
ADD COLUMN `created_at` DATETIME COMMENT '创建时间' AFTER `actual_price`,
ADD COLUMN `updated_at` DATETIME COMMENT '更新时间' AFTER `created_at`,
MODIFY COLUMN `status` VARCHAR(20) NOT NULL COMMENT '状态：pending, paid, in_use, completed, cancelled';