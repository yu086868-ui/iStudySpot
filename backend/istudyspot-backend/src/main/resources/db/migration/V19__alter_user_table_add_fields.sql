-- 添加 student_id 和 credit_score 字段到 user 表
ALTER TABLE `user` 
ADD COLUMN `student_id` VARCHAR(20) DEFAULT NULL COMMENT '学号',
ADD COLUMN `credit_score` INT DEFAULT 100 COMMENT '信用分数';