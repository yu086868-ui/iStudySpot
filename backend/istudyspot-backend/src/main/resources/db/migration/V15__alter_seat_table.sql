-- =====================================================
-- 版本: V15
-- 说明: 为座位表添加新列
-- =====================================================

-- 1. 添加 price_per_hour 列
ALTER TABLE `seat`
    ADD COLUMN `price_per_hour` DECIMAL(10,2) DEFAULT 0.00 COMMENT '每小时价格' AFTER `status`;

-- 2. 添加 description 列
ALTER TABLE `seat`
    ADD COLUMN `description` VARCHAR(500) COMMENT '座位描述' AFTER `price_per_hour`;

-- 3. 更新现有座位的价格
UPDATE `seat` SET `price_per_hour` = 15.00 WHERE `seat_type` = 1;
UPDATE `seat` SET `price_per_hour` = 30.00 WHERE `seat_type` = 2;