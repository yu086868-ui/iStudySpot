-- 修改自习室表
ALTER TABLE `study_room`
    ADD COLUMN `rules` TEXT COMMENT '自习室规则' AFTER `description`,
ADD COLUMN `image_url` VARCHAR(500) COMMENT '图片URL' AFTER `images`;