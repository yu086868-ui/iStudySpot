-- ============================================
-- 微信小程序接口数据库变更 SQL
-- 执行前请备份数据库
-- ============================================

-- 1. StudyRoom 表新增字段
ALTER TABLE study_room 
    ADD COLUMN floor INT DEFAULT NULL COMMENT '楼层',
    ADD COLUMN capacity INT DEFAULT NULL COMMENT '容量',
    ADD COLUMN facilities TEXT DEFAULT NULL COMMENT '设施列表(JSON格式)';

-- 2. Seat 表新增字段
ALTER TABLE seat 
    ADD COLUMN facilities TEXT DEFAULT NULL COMMENT '设施列表(JSON格式)',
    ADD COLUMN last_used_at DATETIME DEFAULT NULL COMMENT '最后使用时间';

-- 3. 创建签到记录表
CREATE TABLE IF NOT EXISTS check_in_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    study_room_id BIGINT DEFAULT NULL,
    seat_id BIGINT NOT NULL,
    check_in_time DATETIME NOT NULL,
    check_out_time DATETIME DEFAULT NULL,
    duration INT DEFAULT NULL COMMENT '时长(分钟)',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/completed',
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_check_in_time (check_in_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 4. 创建公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type VARCHAR(20) DEFAULT 'notice' COMMENT '类型: notice/maintenance/event/emergency',
    priority VARCHAR(20) DEFAULT 'medium' COMMENT '优先级: low/medium/high',
    publish_time DATETIME NOT NULL COMMENT '发布时间',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    author VARCHAR(50) DEFAULT '管理员' COMMENT '作者',
    status VARCHAR(20) DEFAULT 'published' COMMENT '状态: published/draft/archived',
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_priority (priority),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 5. 创建规则表
CREATE TABLE IF NOT EXISTS rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    study_room_id BIGINT DEFAULT NULL COMMENT '自习室ID(NULL表示通用规则)',
    category VARCHAR(50) NOT NULL COMMENT '分类: booking/usage/penalty/general',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    priority INT DEFAULT 0 COMMENT '优先级(数字越小优先级越高)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_study_room_id (study_room_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则表';

-- 6. 插入默认规则数据
INSERT INTO rule (category, title, content, priority) VALUES
('booking', '预约规则', '每位用户每天最多可预约2个座位，预约时间段为开馆至闭馆时间。请在预约成功后30分钟内到达座位签到，否则预约将自动取消。', 1),
('checkin', '签到规则', '预约成功后，请在预约时间开始后30分钟内完成签到。签到方式为扫描座位二维码或在APP内点击签到按钮。未按时签到将被记录为违规。', 2),
('leave', '离开规则', '暂时离开座位不超过30分钟无需操作。离开超过30分钟需要在APP内申请暂离，每天最多申请3次暂离，每次暂离不超过2小时。', 3),
('violation', '违规处理', '累计3次违规将被禁止预约7天，累计5次违规将被禁止预约30天。违规行为包括：未按时签到、恶意占座、转让座位等。', 4),
('civilized', '文明使用', '请保持座位及周边环境整洁，不得在学习区域大声喧哗。请勿长时间占用座位而不学习。离开时请带走个人物品。', 5);
