UPDATE `study_room`
SET `status` = 1
WHERE `status` IS NULL OR `status` <> 1;

INSERT INTO `study_room` (`name`, `address`, `open_time`, `close_time`, `description`, `status`) VALUES
    ('iStudySpot 安静学习舱（学院路店）', '北京市海淀区学院路37号研学中心2F', '07:30:00', '23:30:00', '适合考研、考公和论文写作的安静学习空间', 1),
    ('iStudySpot 城市自习室（西二旗店）', '北京市海淀区西二旗大街数字港B座4F', '08:00:00', '22:30:00', '临近地铁，提供标准座、靠窗座和讨论区', 1),
    ('iStudySpot 深夜学习站（国贸店）', '北京市朝阳区建国门外大街国贸写字楼A座B1', '00:00:00', '23:59:59', '24小时开放，适合晚间复习和弹性学习安排', 1),
    ('iStudySpot 专注自习馆（望京北店）', '北京市朝阳区望京北路创意园6号楼1F', '08:30:00', '23:00:00', '安静区和轻讨论区分离，提供充足电源', 1);

SET @room4 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 安静学习舱（学院路店）' LIMIT 1);
SET @room5 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 城市自习室（西二旗店）' LIMIT 1);
SET @room6 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 深夜学习站（国贸店）' LIMIT 1);
SET @room7 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 专注自习馆（望京北店）' LIMIT 1);

INSERT INTO `area` (`room_id`, `name`, `description`, `sort_order`) VALUES
    (@room4, '沉浸自习区', '全程静音，适合长时间专注学习', 1),
    (@room4, '靠窗阅读区', '自然光充足，适合阅读和笔记整理', 2),
    (@room4, '冲刺隔间区', '半封闭隔间，适合备考冲刺', 3),
    (@room5, '标准学习区', '标准桌椅和电源，适合日常学习', 1),
    (@room5, '轻讨论区', '允许低声沟通，适合小组复盘', 2),
    (@room5, '电脑学习区', '桌面更宽，适合携带笔记本电脑', 3),
    (@room6, '深夜专注区', '24小时开放，灯光柔和', 1),
    (@room6, '休息缓冲区', '靠近补给点，适合短时学习', 2),
    (@room6, '单人隔间区', '独立隔间，适合高强度专注', 3),
    (@room7, '静音专注区', '严格静音管理，适合阅读和背诵', 1),
    (@room7, '阳光学习区', '靠窗采光好，座位间距更宽', 2),
    (@room7, '小组学习区', '适合两到四人低声讨论', 3);

SET @room4_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room4 AND `name` = '沉浸自习区' LIMIT 1);
SET @room4_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room4 AND `name` = '靠窗阅读区' LIMIT 1);
SET @room4_area3 := (SELECT `id` FROM `area` WHERE `room_id` = @room4 AND `name` = '冲刺隔间区' LIMIT 1);
SET @room5_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room5 AND `name` = '标准学习区' LIMIT 1);
SET @room5_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room5 AND `name` = '轻讨论区' LIMIT 1);
SET @room5_area3 := (SELECT `id` FROM `area` WHERE `room_id` = @room5 AND `name` = '电脑学习区' LIMIT 1);
SET @room6_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room6 AND `name` = '深夜专注区' LIMIT 1);
SET @room6_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room6 AND `name` = '休息缓冲区' LIMIT 1);
SET @room6_area3 := (SELECT `id` FROM `area` WHERE `room_id` = @room6 AND `name` = '单人隔间区' LIMIT 1);
SET @room7_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room7 AND `name` = '静音专注区' LIMIT 1);
SET @room7_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room7 AND `name` = '阳光学习区' LIMIT 1);
SET @room7_area3 := (SELECT `id` FROM `area` WHERE `room_id` = @room7 AND `name` = '小组学习区' LIMIT 1);

INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`) VALUES
    (@room4, @room4_area1, 'A01', 1, 1, 1, 1, 1, 0, 1, 1), (@room4, @room4_area1, 'A02', 1, 1, 2, 1, 1, 0, 1, 1), (@room4, @room4_area1, 'A03', 1, 1, 3, 1, 1, 0, 1, 1), (@room4, @room4_area1, 'A04', 1, 1, 4, 1, 1, 0, 1, 1),
    (@room4, @room4_area1, 'A05', 1, 2, 1, 1, 1, 0, 1, 1), (@room4, @room4_area1, 'A06', 1, 2, 2, 1, 1, 0, 1, 1), (@room4, @room4_area1, 'A07', 1, 2, 3, 1, 1, 0, 1, 1), (@room4, @room4_area1, 'A08', 1, 2, 4, 1, 1, 0, 1, 1),
    (@room4, @room4_area2, 'B01', 1, 3, 1, 1, 1, 1, 1, 1), (@room4, @room4_area2, 'B02', 1, 3, 2, 1, 1, 1, 1, 1), (@room4, @room4_area2, 'B03', 1, 3, 3, 1, 1, 1, 1, 1), (@room4, @room4_area2, 'B04', 1, 3, 4, 1, 1, 1, 1, 1),
    (@room4, @room4_area3, 'C01', 3, 4, 1, 1, 1, 0, 1, 1), (@room4, @room4_area3, 'C02', 3, 4, 2, 1, 1, 0, 1, 1), (@room4, @room4_area3, 'C03', 3, 4, 3, 1, 1, 0, 1, 1), (@room4, @room4_area3, 'C04', 3, 4, 4, 1, 1, 0, 1, 1),
    (@room5, @room5_area1, 'A01', 1, 1, 1, 1, 1, 0, 1, 1), (@room5, @room5_area1, 'A02', 1, 1, 2, 1, 1, 0, 1, 1), (@room5, @room5_area1, 'A03', 1, 1, 3, 1, 1, 0, 1, 1), (@room5, @room5_area1, 'A04', 1, 1, 4, 1, 1, 0, 1, 1),
    (@room5, @room5_area1, 'A05', 1, 2, 1, 1, 1, 0, 1, 1), (@room5, @room5_area1, 'A06', 1, 2, 2, 1, 1, 0, 1, 1), (@room5, @room5_area1, 'A07', 1, 2, 3, 1, 1, 0, 1, 1), (@room5, @room5_area1, 'A08', 1, 2, 4, 1, 1, 0, 1, 1),
    (@room5, @room5_area2, 'B01', 2, 3, 1, 1, 1, 0, 0, 1), (@room5, @room5_area2, 'B02', 2, 3, 2, 1, 1, 0, 0, 1), (@room5, @room5_area2, 'B03', 2, 3, 3, 1, 1, 0, 0, 1), (@room5, @room5_area2, 'B04', 2, 3, 4, 1, 1, 0, 0, 1),
    (@room5, @room5_area3, 'C01', 1, 4, 1, 1, 1, 1, 1, 1), (@room5, @room5_area3, 'C02', 1, 4, 2, 1, 1, 1, 1, 1), (@room5, @room5_area3, 'C03', 1, 4, 3, 1, 1, 1, 1, 1), (@room5, @room5_area3, 'C04', 1, 4, 4, 1, 1, 1, 1, 1),
    (@room6, @room6_area1, 'A01', 1, 1, 1, 1, 1, 0, 1, 1), (@room6, @room6_area1, 'A02', 1, 1, 2, 1, 1, 0, 1, 1), (@room6, @room6_area1, 'A03', 1, 1, 3, 1, 1, 0, 1, 1), (@room6, @room6_area1, 'A04', 1, 1, 4, 1, 1, 0, 1, 1),
    (@room6, @room6_area1, 'A05', 1, 2, 1, 1, 1, 0, 1, 1), (@room6, @room6_area1, 'A06', 1, 2, 2, 1, 1, 0, 1, 1), (@room6, @room6_area1, 'A07', 1, 2, 3, 1, 1, 0, 1, 1), (@room6, @room6_area1, 'A08', 1, 2, 4, 1, 1, 0, 1, 1),
    (@room6, @room6_area2, 'B01', 2, 3, 1, 1, 0, 0, 0, 1), (@room6, @room6_area2, 'B02', 2, 3, 2, 1, 0, 0, 0, 1), (@room6, @room6_area2, 'B03', 2, 3, 3, 1, 0, 0, 0, 1), (@room6, @room6_area2, 'B04', 2, 3, 4, 1, 0, 0, 0, 1),
    (@room6, @room6_area3, 'C01', 3, 4, 1, 1, 1, 0, 1, 1), (@room6, @room6_area3, 'C02', 3, 4, 2, 1, 1, 0, 1, 1), (@room6, @room6_area3, 'C03', 3, 4, 3, 1, 1, 0, 1, 1), (@room6, @room6_area3, 'C04', 3, 4, 4, 1, 1, 0, 1, 1),
    (@room7, @room7_area1, 'A01', 1, 1, 1, 1, 1, 0, 1, 1), (@room7, @room7_area1, 'A02', 1, 1, 2, 1, 1, 0, 1, 1), (@room7, @room7_area1, 'A03', 1, 1, 3, 1, 1, 0, 1, 1), (@room7, @room7_area1, 'A04', 1, 1, 4, 1, 1, 0, 1, 1),
    (@room7, @room7_area1, 'A05', 1, 2, 1, 1, 1, 0, 1, 1), (@room7, @room7_area1, 'A06', 1, 2, 2, 1, 1, 0, 1, 1), (@room7, @room7_area1, 'A07', 1, 2, 3, 1, 1, 0, 1, 1), (@room7, @room7_area1, 'A08', 1, 2, 4, 1, 1, 0, 1, 1),
    (@room7, @room7_area2, 'B01', 1, 3, 1, 1, 1, 1, 1, 1), (@room7, @room7_area2, 'B02', 1, 3, 2, 1, 1, 1, 1, 1), (@room7, @room7_area2, 'B03', 1, 3, 3, 1, 1, 1, 1, 1), (@room7, @room7_area2, 'B04', 1, 3, 4, 1, 1, 1, 1, 1),
    (@room7, @room7_area3, 'C01', 2, 4, 1, 1, 1, 0, 0, 1), (@room7, @room7_area3, 'C02', 2, 4, 2, 1, 1, 0, 0, 1), (@room7, @room7_area3, 'C03', 2, 4, 3, 1, 1, 0, 0, 1), (@room7, @room7_area3, 'C04', 2, 4, 4, 1, 1, 0, 0, 1);
