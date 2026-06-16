-- =====================================================
-- H2 数据库初始化脚本（兼容微信小程序本地集成测试）
-- 合并自 MySQL Flyway 迁移脚本 V1~V27
-- =====================================================

-- 清除已有表（按依赖倒序）
DROP TABLE IF EXISTS seat_layout_item;
DROP TABLE IF EXISTS todo;
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS payment_log;
DROP TABLE IF EXISTS order_detail;
DROP TABLE IF EXISTS blacklist;
DROP TABLE IF EXISTS seat_status_log;
DROP TABLE IF EXISTS order;
DROP TABLE IF EXISTS price_strategy;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS area;
DROP TABLE IF EXISTS study_room;
DROP TABLE IF EXISTS user;

-- =====================================================
-- 1. 自习室表（V1 + V14）
-- =====================================================
CREATE TABLE study_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    open_time TIME,
    close_time TIME,
    description VARCHAR(4000),
    rules VARCHAR(4000),
    images VARCHAR(2000),
    image_url VARCHAR(500),
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_study_room_status ON study_room (status);

-- =====================================================
-- 2. 区域表（V2）
-- =====================================================
CREATE TABLE area (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES study_room (id) ON DELETE CASCADE
);

CREATE INDEX idx_area_room_id ON area (room_id);

-- =====================================================
-- 3. 座位表（V3 + V15）
-- =====================================================
CREATE TABLE seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    area_id BIGINT,
    seat_number VARCHAR(20) NOT NULL,
    seat_type SMALLINT,
    row_num INT,
    col_num INT,
    has_power SMALLINT DEFAULT 0,
    has_lamp SMALLINT DEFAULT 0,
    is_window SMALLINT DEFAULT 0,
    is_quiet SMALLINT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    price_per_hour DECIMAL(10,2) DEFAULT 0.00,
    description VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES study_room (id) ON DELETE CASCADE,
    FOREIGN KEY (area_id) REFERENCES area (id) ON DELETE SET NULL
);

CREATE INDEX idx_seat_room_id ON seat (room_id);
CREATE INDEX idx_seat_area_id ON seat (area_id);
CREATE INDEX idx_seat_status ON seat (status);

-- =====================================================
-- 4. 用户表（V4 + V13 + V19）
-- =====================================================
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    openid VARCHAR(64),
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    avatar_url VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    balance DECIMAL(10,2) DEFAULT 0.00,
    points INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    violation_count INT DEFAULT 0,
    student_id VARCHAR(20),
    credit_score INT DEFAULT 100,
    last_login_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_openid ON user (openid);
CREATE INDEX idx_user_status ON user (status);

-- =====================================================
-- 5. 价格策略表（V5）
-- =====================================================
CREATE TABLE price_strategy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT,
    area_id BIGINT,
    seat_type SMALLINT,
    week_days VARCHAR(20),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    is_holiday SMALLINT DEFAULT 0,
    priority INT DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES study_room (id) ON DELETE CASCADE,
    FOREIGN KEY (area_id) REFERENCES area (id) ON DELETE CASCADE
);

CREATE INDEX idx_price_room_area ON price_strategy (room_id, area_id);

-- =====================================================
-- 6. 订单表（V6 + V16）
-- =====================================================
CREATE TABLE order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    seat_number VARCHAR(20),
    room_id BIGINT NOT NULL,
    room_name VARCHAR(100),
    study_room_name VARCHAR(100),
    seat_position VARCHAR(20),
    plan_start_time TIMESTAMP NOT NULL,
    plan_end_time TIMESTAMP NOT NULL,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    total_hours DECIMAL(5,1),
    unit_price DECIMAL(10,2),
    total_amount DECIMAL(10,2) NOT NULL,
    pay_amount DECIMAL(10,2),
    deposit DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    pay_type SMALLINT,
    pay_time TIMESTAMP,
    cancel_reason VARCHAR(255),
    cancel_time TIMESTAMP,
    checkin_time TIMESTAMP,
    checkout_time TIMESTAMP,
    actual_duration INT,
    actual_price DECIMAL(10,2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id),
    FOREIGN KEY (seat_id) REFERENCES seat (id)
);

CREATE INDEX idx_order_user_id ON `order` (user_id);
CREATE INDEX idx_order_seat_id ON `order` (seat_id);
CREATE INDEX idx_order_status ON `order` (status);
CREATE INDEX idx_order_create_time ON `order` (create_time);
CREATE INDEX idx_order_plan_time ON `order` (plan_start_time, plan_end_time);

-- =====================================================
-- 7. 订单明细表（V7）
-- =====================================================
CREATE TABLE order_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    hours DECIMAL(3,1) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES order (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_detail_order_id ON order_detail (order_id);

-- =====================================================
-- 8. 支付流水表（V8）
-- =====================================================
CREATE TABLE payment_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    pay_no VARCHAR(64) UNIQUE,
    pay_type SMALLINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status SMALLINT NOT NULL,
    pay_time TIMESTAMP,
    refund_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES order (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE INDEX idx_payment_log_order_id ON payment_log (order_id);
CREATE INDEX idx_payment_log_user_id ON payment_log (user_id);

-- =====================================================
-- 9. 座位状态流水表（V9）
-- =====================================================
CREATE TABLE seat_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seat_id BIGINT NOT NULL,
    order_id BIGINT,
    user_id BIGINT,
    status SMALLINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seat_id) REFERENCES seat (id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES order (id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE SET NULL
);

CREATE INDEX idx_seat_status_log_seat_id ON seat_status_log (seat_id);
CREATE INDEX idx_seat_status_log_order_id ON seat_status_log (order_id);
CREATE INDEX idx_seat_status_log_time_range ON seat_status_log (start_time, end_time);

-- =====================================================
-- 10. 黑名单表（V10）
-- =====================================================
CREATE TABLE blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    room_id BIGINT,
    reason VARCHAR(255),
    expire_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES study_room (id) ON DELETE CASCADE
);

CREATE INDEX idx_blacklist_user_id ON blacklist (user_id);
CREATE INDEX idx_blacklist_expire_time ON blacklist (expire_time);

-- =====================================================
-- 11. 支付表（V17）
-- =====================================================
CREATE TABLE payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(32) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_url VARCHAR(500),
    pay_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES order (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE INDEX idx_payment_order_id ON payment (order_id);
CREATE INDEX idx_payment_status ON payment (status);

-- =====================================================
-- 12. AI卡片表（V20）
-- =====================================================
CREATE TABLE card (
    uuid VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    card_id VARCHAR(8) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    study_duration INT NOT NULL,
    rarity VARCHAR(10) NOT NULL,
    border_theme VARCHAR(50) NOT NULL,
    card_theme VARCHAR(50) NOT NULL,
    theme_category VARCHAR(50) NOT NULL,
    markdown VARCHAR(4000) NOT NULL,
    image_url VARCHAR(500) NOT NULL
);

CREATE INDEX idx_card_user_id ON card (user_id);
CREATE INDEX idx_card_create_time ON card (create_time);

-- =====================================================
-- 13. 学习待办表（V22）
-- =====================================================
CREATE TABLE todo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    due_time TIMESTAMP,
    order_id BIGINT,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_todo_user_id ON todo (user_id);
CREATE INDEX idx_todo_user_status ON todo (user_id, status);

-- =====================================================
-- 14. 座位布局元素表（V25）
-- =====================================================
CREATE TABLE seat_layout_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    area_id BIGINT,
    seat_id BIGINT,
    item_type VARCHAR(32) NOT NULL,
    item_key VARCHAR(64),
    label VARCHAR(100),
    row_num INT NOT NULL,
    col_num INT NOT NULL,
    width_units INT NOT NULL DEFAULT 1,
    height_units INT NOT NULL DEFAULT 1,
    rotation INT NOT NULL DEFAULT 0,
    z_index INT NOT NULL DEFAULT 0,
    metadata VARCHAR(4000),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES study_room (id) ON DELETE CASCADE,
    FOREIGN KEY (area_id) REFERENCES area (id) ON DELETE SET NULL,
    FOREIGN KEY (seat_id) REFERENCES seat (id) ON DELETE CASCADE,
    CONSTRAINT uk_layout_item_key UNIQUE (room_id, item_key),
    CONSTRAINT uk_layout_seat UNIQUE (seat_id)
);

CREATE INDEX idx_layout_room ON seat_layout_item (room_id, row_num, col_num);
CREATE INDEX idx_layout_type ON seat_layout_item (item_type);


-- =====================================================
-- 初始数据
-- =====================================================

-- -------------------------------------------------
-- 自习室（7个门店）
-- -------------------------------------------------
INSERT INTO study_room (id, name, address, open_time, close_time, description, status) VALUES
    (1, 'iStudySpot 学习空间（五道口店）', '北京市海淀区五道口购物中心3F', '08:00:00', '23:00:00', '临近清华北大，考研党聚集地', 1),
    (2, 'iStudySpot 静心自习室（中关村店）', '北京市海淀区中关村创业大街B2', '09:00:00', '22:00:00', '互联网人充电好去处', 1),
    (3, 'iStudySpot 24h自习舱（望京店）', '北京市朝阳区望京SOHO T2-3F', '00:00:00', '23:59:59', '24小时营业，满足深夜学习需求', 1),
    (4, 'iStudySpot 安静学习舱（学院路店）', '北京市海淀区学院路37号研学中心2F', '07:30:00', '23:30:00', '适合考研、考公和论文写作的安静学习空间', 1),
    (5, 'iStudySpot 城市自习室（西二旗店）', '北京市海淀区西二旗大街数字港B座4F', '08:00:00', '22:30:00', '临近地铁，提供标准座、靠窗座和讨论区', 1),
    (6, 'iStudySpot 深夜学习站（国贸店）', '北京市朝阳区建国门外大街国贸写字楼A座B1', '00:00:00', '23:59:59', '24小时开放，适合晚间复习和弹性学习安排', 1),
    (7, 'iStudySpot 专注自习馆（望京北店）', '北京市朝阳区望京北路创意园6号楼1F', '08:30:00', '23:00:00', '安静区和轻讨论区分离，提供充足电源', 1);

-- -------------------------------------------------
-- 区域
-- -------------------------------------------------
INSERT INTO area (id, room_id, name, description, sort_order) VALUES
    -- 自习室1: 五道口店
    (1,  1, '沉浸学习区', '完全静音，禁止交谈', 1),
    (2,  1, '轻讨论区', '允许低声讨论', 2),
    (3,  1, 'VIP包厢', '独立空间，适合小组学习', 3),
    -- 自习室2: 中关村店
    (4,  2, '阳光窗景区', '靠窗座位，光线充足', 1),
    (5,  2, '标准学习区', '标准座位', 2),
    -- 自习室3: 望京店
    (6,  3, '深夜加油区', '24小时开放区域', 1),
    (7,  3, '休息充电区', '配备沙发和充电桩', 2),
    -- 自习室4: 学院路店
    (8,  4, '沉浸自习区', '全程静音，适合长时间专注学习', 1),
    (9,  4, '靠窗阅读区', '自然光充足，适合阅读和笔记整理', 2),
    (10, 4, '冲刺隔间区', '半封闭隔间，适合备考冲刺', 3),
    -- 自习室5: 西二旗店
    (11, 5, '标准学习区', '标准桌椅和电源，适合日常学习', 1),
    (12, 5, '轻讨论区', '允许低声沟通，适合小组复盘', 2),
    (13, 5, '电脑学习区', '桌面更宽，适合携带笔记本电脑', 3),
    -- 自习室6: 国贸店
    (14, 6, '深夜专注区', '24小时开放，灯光柔和', 1),
    (15, 6, '休息缓冲区', '靠近补给点，适合短时学习', 2),
    (16, 6, '单人隔间区', '独立隔间，适合高强度专注', 3),
    -- 自习室7: 望京北店
    (17, 7, '静音专注区', '严格静音管理，适合阅读和背诵', 1),
    (18, 7, '阳光学习区', '靠窗采光好，座位间距更宽', 2),
    (19, 7, '小组学习区', '适合两到四人低声讨论', 3);

-- -------------------------------------------------
-- 座位 - 自习室1（五道口店）
-- -------------------------------------------------
INSERT INTO seat (room_id, area_id, seat_number, seat_type, row_num, col_num, has_power, has_lamp, is_window, is_quiet, status, price_per_hour) VALUES
    (1, 1, 'A01', 1, 1, 1, 1, 1, 0, 1, 1, 15.00),
    (1, 1, 'A02', 1, 1, 2, 1, 1, 0, 1, 1, 15.00),
    (1, 1, 'A03', 1, 1, 3, 1, 1, 1, 1, 1, 15.00),
    (1, 1, 'A04', 1, 1, 4, 1, 1, 1, 1, 1, 15.00),
    (1, 1, 'A05', 1, 1, 5, 1, 0, 0, 1, 1, 15.00),
    (1, 2, 'B01', 1, 2, 1, 1, 0, 0, 0, 1, 15.00),
    (1, 2, 'B02', 1, 2, 2, 1, 0, 0, 0, 1, 15.00),
    (1, 2, 'B03', 2, 2, 3, 1, 1, 0, 0, 1, 30.00),
    (1, 2, 'B04', 2, 2, 4, 1, 1, 1, 0, 1, 30.00),
    (1, 3, 'C01', 3, 3, 1, 1, 1, 0, 1, 1, 25.00),
    (1, 3, 'C02', 3, 3, 2, 1, 1, 1, 1, 1, 25.00);

-- -------------------------------------------------
-- 座位 - 自习室2（中关村店）
-- -------------------------------------------------
INSERT INTO seat (room_id, area_id, seat_number, seat_type, row_num, col_num, has_power, has_lamp, is_window, is_quiet, status, price_per_hour) VALUES
    (2, 4, 'A01', 1, 1, 1, 1, 1, 1, 1, 1, 15.00),
    (2, 4, 'A02', 1, 1, 2, 1, 1, 1, 1, 1, 15.00),
    (2, 4, 'A03', 1, 1, 3, 1, 1, 1, 1, 1, 15.00),
    (2, 4, 'A04', 1, 1, 4, 1, 1, 1, 1, 1, 15.00),
    (2, 4, 'A05', 1, 2, 1, 1, 1, 1, 1, 1, 15.00),
    (2, 4, 'A06', 1, 2, 2, 1, 1, 1, 1, 1, 15.00),
    (2, 4, 'A07', 1, 2, 3, 1, 1, 1, 1, 1, 15.00),
    (2, 4, 'A08', 1, 2, 4, 1, 1, 1, 1, 1, 15.00),
    (2, 5, 'B01', 1, 3, 1, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'B02', 1, 3, 2, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'B03', 1, 3, 3, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'B04', 1, 3, 4, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'B05', 1, 4, 1, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'B06', 1, 4, 2, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'B07', 1, 4, 3, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'B08', 1, 4, 4, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'C01', 2, 5, 1, 1, 1, 0, 1, 1, 30.00),
    (2, 5, 'C02', 2, 5, 2, 1, 1, 0, 1, 1, 30.00),
    (2, 5, 'C03', 2, 5, 3, 1, 1, 0, 1, 1, 30.00),
    (2, 5, 'C04', 2, 5, 4, 1, 1, 0, 1, 1, 30.00),
    (2, 5, 'C05', 2, 5, 5, 1, 1, 0, 1, 1, 30.00),
    (2, 5, 'C06', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    (2, 5, 'D01', 1, 7, 6, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'D02', 1, 7, 7, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'D03', 1, 7, 8, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'D04', 1, 7, 9, 1, 1, 0, 1, 1, 15.00),
    (2, 5, 'D05', 1, 7, 10, 1, 0, 0, 1, 1, 15.00),
    (2, 5, 'D06', 1, 7, 11, 1, 0, 0, 1, 1, 15.00),
    (2, 5, 'E01', 4, 9, 6, 1, 1, 0, 1, 1, 40.00),
    (2, 5, 'E02', 4, 9, 7, 1, 1, 0, 1, 1, 40.00),
    (2, 5, 'E03', 4, 9, 8, 1, 1, 0, 1, 1, 40.00),
    (2, 5, 'E04', 4, 9, 9, 1, 1, 0, 1, 1, 40.00);

-- -------------------------------------------------
-- 座位 - 自习室3（望京店）
-- -------------------------------------------------
INSERT INTO seat (room_id, area_id, seat_number, seat_type, row_num, col_num, has_power, has_lamp, is_window, is_quiet, status, price_per_hour) VALUES
    (3, 6, 'A01', 1, 1, 1, 1, 1, 0, 1, 1, 15.00),
    (3, 6, 'A02', 1, 1, 2, 1, 1, 0, 1, 1, 15.00),
    (3, 6, 'A03', 1, 1, 3, 1, 1, 0, 1, 1, 15.00),
    (3, 6, 'A04', 1, 1, 4, 1, 1, 0, 1, 1, 15.00),
    (3, 6, 'A05', 1, 2, 1, 1, 1, 0, 1, 1, 15.00),
    (3, 6, 'A06', 1, 2, 2, 1, 1, 0, 1, 1, 15.00),
    (3, 6, 'A07', 1, 2, 3, 1, 1, 0, 1, 1, 15.00),
    (3, 6, 'A08', 1, 2, 4, 1, 1, 0, 1, 1, 15.00),
    (3, 7, 'B01', 2, 3, 1, 1, 0, 0, 0, 1, 30.00),
    (3, 7, 'B02', 2, 3, 2, 1, 0, 0, 0, 1, 30.00),
    (3, 7, 'B03', 2, 3, 3, 1, 0, 0, 0, 1, 30.00),
    (3, 7, 'B04', 2, 3, 4, 1, 0, 0, 0, 1, 30.00),
    (3, 7, 'B05', 2, 4, 1, 1, 0, 0, 0, 1, 30.00),
    (3, 7, 'B06', 2, 4, 2, 1, 0, 0, 0, 1, 30.00),
    (3, 7, 'B07', 2, 4, 3, 1, 0, 0, 0, 1, 30.00),
    (3, 7, 'B08', 2, 4, 4, 1, 0, 0, 0, 1, 30.00),
    (3, 6, 'C01', 3, 5, 1, 1, 1, 0, 1, 1, 25.00),
    (3, 6, 'C02', 3, 5, 2, 1, 1, 0, 1, 1, 25.00),
    (3, 6, 'C03', 3, 5, 3, 1, 1, 0, 1, 1, 25.00),
    (3, 6, 'C04', 3, 5, 4, 1, 1, 0, 1, 1, 25.00),
    (3, 7, 'D01', 1, 9, 7, 1, 0, 0, 0, 1, 15.00),
    (3, 7, 'D02', 1, 9, 8, 1, 0, 0, 0, 1, 15.00),
    (3, 7, 'D03', 1, 9, 9, 1, 0, 0, 0, 1, 15.00),
    (3, 7, 'D04', 1, 9, 10, 1, 0, 0, 0, 1, 15.00),
    (3, 7, 'D05', 1, 10, 7, 1, 0, 0, 0, 1, 15.00),
    (3, 7, 'D06', 1, 10, 8, 1, 0, 0, 0, 1, 15.00);

-- -------------------------------------------------
-- 座位 - 自习室4（学院路店）
-- -------------------------------------------------
INSERT INTO seat (room_id, area_id, seat_number, seat_type, row_num, col_num, has_power, has_lamp, is_window, is_quiet, status, price_per_hour) VALUES
    (4, 8, 'A01', 1, 1, 1, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'A02', 1, 1, 2, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'A03', 1, 1, 3, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'A04', 1, 1, 4, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'A05', 1, 2, 1, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'A06', 1, 2, 2, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'A07', 1, 2, 3, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'A08', 1, 2, 4, 1, 1, 0, 1, 1, 15.00),
    (4, 9, 'B01', 1, 3, 1, 1, 1, 1, 1, 1, 15.00),
    (4, 9, 'B02', 1, 3, 2, 1, 1, 1, 1, 1, 15.00),
    (4, 9, 'B03', 1, 3, 3, 1, 1, 1, 1, 1, 15.00),
    (4, 9, 'B04', 1, 3, 4, 1, 1, 1, 1, 1, 15.00),
    (4, 10, 'C01', 3, 4, 1, 1, 1, 0, 1, 1, 25.00),
    (4, 10, 'C02', 3, 4, 2, 1, 1, 0, 1, 1, 25.00),
    (4, 10, 'C03', 3, 4, 3, 1, 1, 0, 1, 1, 25.00),
    (4, 10, 'C04', 3, 4, 4, 1, 1, 0, 1, 1, 25.00),
    (4, 8, 'D01', 2, 5, 1, 1, 1, 0, 1, 1, 30.00),
    (4, 8, 'D02', 2, 5, 2, 1, 1, 0, 1, 1, 30.00),
    (4, 8, 'D03', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    (4, 8, 'D04', 2, 5, 7, 1, 1, 0, 1, 1, 30.00),
    (4, 8, 'D05', 2, 6, 1, 1, 1, 0, 1, 1, 30.00),
    (4, 8, 'D06', 2, 6, 2, 1, 1, 0, 1, 1, 30.00),
    (4, 8, 'E01', 1, 7, 6, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'E02', 1, 7, 7, 1, 1, 0, 1, 1, 15.00),
    (4, 8, 'E03', 1, 9, 1, 1, 0, 0, 1, 1, 15.00),
    (4, 8, 'E04', 1, 9, 2, 1, 0, 0, 1, 1, 15.00),
    (4, 8, 'E05', 1, 9, 6, 1, 0, 0, 1, 1, 15.00),
    (4, 8, 'E06', 1, 9, 7, 1, 0, 0, 1, 1, 15.00);

-- -------------------------------------------------
-- 座位 - 自习室5（西二旗店）
-- -------------------------------------------------
INSERT INTO seat (room_id, area_id, seat_number, seat_type, row_num, col_num, has_power, has_lamp, is_window, is_quiet, status, price_per_hour) VALUES
    (5, 11, 'A01', 1, 1, 1, 1, 1, 0, 1, 1, 15.00),
    (5, 11, 'A02', 1, 1, 2, 1, 1, 0, 1, 1, 15.00),
    (5, 11, 'A03', 1, 1, 3, 1, 1, 0, 1, 1, 15.00),
    (5, 11, 'A04', 1, 1, 4, 1, 1, 0, 1, 1, 15.00),
    (5, 11, 'A05', 1, 2, 1, 1, 1, 0, 1, 1, 15.00),
    (5, 11, 'A06', 1, 2, 2, 1, 1, 0, 1, 1, 15.00),
    (5, 11, 'A07', 1, 2, 3, 1, 1, 0, 1, 1, 15.00),
    (5, 11, 'A08', 1, 2, 4, 1, 1, 0, 1, 1, 15.00),
    (5, 12, 'B01', 2, 3, 1, 1, 1, 0, 0, 1, 30.00),
    (5, 12, 'B02', 2, 3, 2, 1, 1, 0, 0, 1, 30.00),
    (5, 12, 'B03', 2, 3, 3, 1, 1, 0, 0, 1, 30.00),
    (5, 12, 'B04', 2, 3, 4, 1, 1, 0, 0, 1, 30.00),
    (5, 13, 'C01', 1, 4, 1, 1, 1, 1, 1, 1, 15.00),
    (5, 13, 'C02', 1, 4, 2, 1, 1, 1, 1, 1, 15.00),
    (5, 13, 'C03', 1, 4, 3, 1, 1, 1, 1, 1, 15.00),
    (5, 13, 'C04', 1, 4, 4, 1, 1, 1, 1, 1, 15.00),
    (5, 11, 'D01', 2, 5, 1, 1, 1, 0, 1, 1, 30.00),
    (5, 11, 'D02', 2, 5, 2, 1, 1, 0, 1, 1, 30.00),
    (5, 11, 'D03', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    (5, 11, 'D04', 2, 5, 7, 1, 1, 0, 1, 1, 30.00),
    (5, 11, 'D05', 2, 6, 1, 1, 1, 0, 1, 1, 30.00),
    (5, 11, 'D06', 2, 6, 2, 1, 1, 0, 1, 1, 30.00),
    (5, 12, 'E01', 1, 7, 6, 1, 1, 0, 0, 1, 15.00),
    (5, 12, 'E02', 1, 7, 7, 1, 1, 0, 0, 1, 15.00),
    (5, 12, 'E03', 1, 7, 8, 1, 0, 0, 0, 1, 15.00),
    (5, 12, 'E04', 1, 7, 9, 1, 0, 0, 0, 1, 15.00),
    (5, 13, 'E05', 1, 9, 1, 1, 1, 1, 1, 1, 15.00),
    (5, 13, 'E06', 1, 9, 2, 1, 1, 1, 1, 1, 15.00);

-- -------------------------------------------------
-- 座位 - 自习室6（国贸店）
-- -------------------------------------------------
INSERT INTO seat (room_id, area_id, seat_number, seat_type, row_num, col_num, has_power, has_lamp, is_window, is_quiet, status, price_per_hour) VALUES
    (6, 14, 'A01', 1, 1, 1, 1, 1, 0, 1, 1, 15.00),
    (6, 14, 'A02', 1, 1, 2, 1, 1, 0, 1, 1, 15.00),
    (6, 14, 'A03', 1, 1, 3, 1, 1, 0, 1, 1, 15.00),
    (6, 14, 'A04', 1, 1, 4, 1, 1, 0, 1, 1, 15.00),
    (6, 14, 'A05', 1, 2, 1, 1, 1, 0, 1, 1, 15.00),
    (6, 14, 'A06', 1, 2, 2, 1, 1, 0, 1, 1, 15.00),
    (6, 14, 'A07', 1, 2, 3, 1, 1, 0, 1, 1, 15.00),
    (6, 14, 'A08', 1, 2, 4, 1, 1, 0, 1, 1, 15.00),
    (6, 15, 'B01', 2, 3, 1, 1, 0, 0, 0, 1, 30.00),
    (6, 15, 'B02', 2, 3, 2, 1, 0, 0, 0, 1, 30.00),
    (6, 15, 'B03', 2, 3, 3, 1, 0, 0, 0, 1, 30.00),
    (6, 15, 'B04', 2, 3, 4, 1, 0, 0, 0, 1, 30.00),
    (6, 16, 'C01', 3, 4, 1, 1, 1, 0, 1, 1, 25.00),
    (6, 16, 'C02', 3, 4, 2, 1, 1, 0, 1, 1, 25.00),
    (6, 16, 'C03', 3, 4, 3, 1, 1, 0, 1, 1, 25.00),
    (6, 16, 'C04', 3, 4, 4, 1, 1, 0, 1, 1, 25.00),
    (6, 14, 'D01', 3, 5, 1, 1, 1, 0, 1, 1, 25.00),
    (6, 14, 'D02', 3, 5, 2, 1, 1, 0, 1, 1, 25.00),
    (6, 14, 'D03', 3, 5, 6, 1, 1, 0, 1, 1, 25.00),
    (6, 14, 'D04', 3, 5, 7, 1, 1, 0, 1, 1, 25.00),
    (6, 15, 'E01', 1, 9, 6, 1, 0, 0, 0, 1, 15.00),
    (6, 15, 'E02', 1, 9, 7, 1, 0, 0, 0, 1, 15.00),
    (6, 15, 'E03', 1, 9, 8, 1, 0, 0, 0, 1, 15.00),
    (6, 15, 'E04', 1, 9, 9, 1, 0, 0, 0, 1, 15.00),
    (6, 16, 'E05', 4, 9, 1, 1, 1, 0, 1, 1, 40.00),
    (6, 16, 'E06', 4, 9, 2, 1, 1, 0, 1, 1, 40.00);

-- -------------------------------------------------
-- 座位 - 自习室7（望京北店）
-- -------------------------------------------------
INSERT INTO seat (room_id, area_id, seat_number, seat_type, row_num, col_num, has_power, has_lamp, is_window, is_quiet, status, price_per_hour) VALUES
    (7, 17, 'A01', 1, 1, 1, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'A02', 1, 1, 2, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'A03', 1, 1, 3, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'A04', 1, 1, 4, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'A05', 1, 2, 1, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'A06', 1, 2, 2, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'A07', 1, 2, 3, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'A08', 1, 2, 4, 1, 1, 0, 1, 1, 15.00),
    (7, 18, 'B01', 1, 3, 1, 1, 1, 1, 1, 1, 15.00),
    (7, 18, 'B02', 1, 3, 2, 1, 1, 1, 1, 1, 15.00),
    (7, 18, 'B03', 1, 3, 3, 1, 1, 1, 1, 1, 15.00),
    (7, 18, 'B04', 1, 3, 4, 1, 1, 1, 1, 1, 15.00),
    (7, 19, 'C01', 2, 4, 1, 1, 1, 0, 0, 1, 30.00),
    (7, 19, 'C02', 2, 4, 2, 1, 1, 0, 0, 1, 30.00),
    (7, 19, 'C03', 2, 4, 3, 1, 1, 0, 0, 1, 30.00),
    (7, 19, 'C04', 2, 4, 4, 1, 1, 0, 0, 1, 30.00),
    (7, 18, 'D01', 2, 5, 1, 1, 1, 1, 1, 1, 30.00),
    (7, 18, 'D02', 2, 5, 2, 1, 1, 1, 1, 1, 30.00),
    (7, 18, 'D03', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    (7, 18, 'D04', 2, 5, 7, 1, 1, 0, 1, 1, 30.00),
    (7, 18, 'D05', 2, 6, 1, 1, 1, 1, 1, 1, 30.00),
    (7, 18, 'D06', 2, 6, 2, 1, 1, 1, 1, 1, 30.00),
    (7, 19, 'E01', 1, 9, 6, 1, 0, 0, 0, 1, 15.00),
    (7, 19, 'E02', 1, 9, 7, 1, 0, 0, 0, 1, 15.00),
    (7, 19, 'E03', 1, 9, 8, 1, 0, 0, 0, 1, 15.00),
    (7, 19, 'E04', 1, 9, 9, 1, 0, 0, 0, 1, 15.00),
    (7, 17, 'E05', 1, 9, 1, 1, 1, 0, 1, 1, 15.00),
    (7, 17, 'E06', 1, 9, 2, 1, 1, 0, 1, 1, 15.00);

-- -------------------------------------------------
-- 价格策略
-- -------------------------------------------------
INSERT INTO price_strategy (room_id, area_id, seat_type, week_days, start_time, end_time, price, is_holiday, priority) VALUES
    (1, NULL, NULL, '1,2,3,4,5', '08:00:00', '18:00:00', 15.00, 0, 1),
    (1, NULL, NULL, '1,2,3,4,5', '18:00:00', '23:00:00', 12.00, 0, 1),
    (1, NULL, NULL, '6,7', '08:00:00', '23:00:00', 18.00, 0, 1),
    (1, 3, NULL, NULL, '00:00:00', '23:59:59', 30.00, 0, 0),
    (1, NULL, NULL, NULL, '08:00:00', '23:00:00', 25.00, 1, 0);

-- -------------------------------------------------
-- 用户数据
-- -------------------------------------------------
-- 测试用户（密码：123456，MD5: e10adc3949ba59abbe56e057f20f883e）
INSERT INTO user(id, username, password, openid, nickname, avatar, avatar_url, phone, email, balance, points, status, credit_score) VALUES
    (1, 'test', 'e10adc3949ba59abbe56e057f20f883e', NULL, '测试用户', NULL, NULL, '13800138000', 'test@example.com', 100.00, 0, 1, 100);

-- 管理员用户（密码：admin123，MD5: f19b8dc2029cf707939e886e4b164681）
INSERT INTO user(id, username, password, openid, nickname, avatar, avatar_url, phone, email, balance, points, status, credit_score) VALUES
    (2, 'admin', 'f19b8dc2029cf707939e886e4b164681', NULL, '管理员', NULL, NULL, NULL, NULL, 0.00, 0, 1, 100);

-- 模拟微信用户
INSERT INTO user(id, username, password, openid, nickname, avatar, avatar_url, phone, email, balance, points, status, credit_score) VALUES
    (3, NULL, NULL, 'oTestOpenId001', '微信用户1', 'https://example.com/avatar1.png', 'https://example.com/avatar1.png', NULL, NULL, 50.00, 100, 1, 100);
