-- =====================================================
-- 版本: V27
-- 说明: 扩展座位数量与布局复杂度，用于测试复杂渲染场景
-- =====================================================

-- 获取各自习室 ID
SET @room2 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 静心自习室（中关村店）' LIMIT 1);
SET @room3 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 24h自习舱（望京店）' LIMIT 1);
SET @room4 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 安静学习舱（学院路店）' LIMIT 1);
SET @room5 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 城市自习室（西二旗店）' LIMIT 1);
SET @room6 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 深夜学习站（国贸店）' LIMIT 1);
SET @room7 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 专注自习馆（望京北店）' LIMIT 1);

-- 获取区域 ID
SET @room2_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room2 AND `name` = '阳光窗景区' LIMIT 1);
SET @room2_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room2 AND `name` = '标准学习区' LIMIT 1);
SET @room3_area1 := (SELECT `id` FROM `area` WHERE `room_id` = @room3 AND `name` = '深夜加油区' LIMIT 1);
SET @room3_area2 := (SELECT `id` FROM `area` WHERE `room_id` = @room3 AND `name` = '休息充电区' LIMIT 1);
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

-- =====================================================
-- 1. 为 room2（中关村店）增加 C/D/E 三排座位
--    布局: 窗景A排(8座) + 标准B排(8座) + VIP C排(6座) + 静音D排(6座) + 隔间E排(4座)
-- =====================================================
INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`, `price_per_hour`) VALUES
    -- C排: VIP沙发座 (row_num=5)
    (@room2, @room2_area2, 'C01', 2, 5, 1, 1, 1, 0, 1, 1, 30.00),
    (@room2, @room2_area2, 'C02', 2, 5, 2, 1, 1, 0, 1, 1, 30.00),
    (@room2, @room2_area2, 'C03', 2, 5, 3, 1, 1, 0, 1, 1, 30.00),
    (@room2, @room2_area2, 'C04', 2, 5, 4, 1, 1, 0, 1, 1, 30.00),
    (@room2, @room2_area2, 'C05', 2, 5, 5, 1, 1, 0, 1, 1, 30.00),
    (@room2, @room2_area2, 'C06', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    -- D排: 静音区 (row_num=7)
    (@room2, @room2_area2, 'D01', 1, 7, 6, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'D02', 1, 7, 7, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'D03', 1, 7, 8, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'D04', 1, 7, 9, 1, 1, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'D05', 1, 7, 10, 1, 0, 0, 1, 1, 15.00),
    (@room2, @room2_area2, 'D06', 1, 7, 11, 1, 0, 0, 1, 1, 15.00),
    -- E排: 隔间座 (row_num=9)
    (@room2, @room2_area2, 'E01', 4, 9, 6, 1, 1, 0, 1, 1, 40.00),
    (@room2, @room2_area2, 'E02', 4, 9, 7, 1, 1, 0, 1, 1, 40.00),
    (@room2, @room2_area2, 'E03', 4, 9, 8, 1, 1, 0, 1, 1, 40.00),
    (@room2, @room2_area2, 'E04', 4, 9, 9, 1, 1, 0, 1, 1, 40.00);

-- =====================================================
-- 2. 为 room3（望京店）增加 C/D 排座位
--    布局: 深夜A排(8座) + 休息B排(8座) + 隔间C排(4座) + 电脑D排(6座)
-- =====================================================
INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`, `price_per_hour`) VALUES
    -- C排: 隔间座 (row_num=5)
    (@room3, @room3_area1, 'C01', 3, 5, 1, 1, 1, 0, 1, 1, 25.00),
    (@room3, @room3_area1, 'C02', 3, 5, 2, 1, 1, 0, 1, 1, 25.00),
    (@room3, @room3_area1, 'C03', 3, 5, 3, 1, 1, 0, 1, 1, 25.00),
    (@room3, @room3_area1, 'C04', 3, 5, 4, 1, 1, 0, 1, 1, 25.00),
    -- D排: 电脑座 (row_num=9)
    (@room3, @room3_area2, 'D01', 1, 9, 7, 1, 0, 0, 0, 1, 15.00),
    (@room3, @room3_area2, 'D02', 1, 9, 8, 1, 0, 0, 0, 1, 15.00),
    (@room3, @room3_area2, 'D03', 1, 9, 9, 1, 0, 0, 0, 1, 15.00),
    (@room3, @room3_area2, 'D04', 1, 9, 10, 1, 0, 0, 0, 1, 15.00),
    (@room3, @room3_area2, 'D05', 1, 10, 7, 1, 0, 0, 0, 1, 15.00),
    (@room3, @room3_area2, 'D06', 1, 10, 8, 1, 0, 0, 0, 1, 15.00);

-- =====================================================
-- 3. 为 room4（学院路店）增加 D/E 排座位
--    布局: 沉浸A排(8座) + 靠窗B排(4座) + 隔间C排(4座) + VIP D排(6座) + 静音E排(6座)
-- =====================================================
INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`, `price_per_hour`) VALUES
    -- D排: VIP (row_num=5)
    (@room4, @room4_area1, 'D01', 2, 5, 1, 1, 1, 0, 1, 1, 30.00),
    (@room4, @room4_area1, 'D02', 2, 5, 2, 1, 1, 0, 1, 1, 30.00),
    (@room4, @room4_area1, 'D03', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    (@room4, @room4_area1, 'D04', 2, 5, 7, 1, 1, 0, 1, 1, 30.00),
    (@room4, @room4_area1, 'D05', 2, 6, 1, 1, 1, 0, 1, 1, 30.00),
    (@room4, @room4_area1, 'D06', 2, 6, 2, 1, 1, 0, 1, 1, 30.00),
    -- E排: 静音 (row_num=7)
    (@room4, @room4_area1, 'E01', 1, 7, 6, 1, 1, 0, 1, 1, 15.00),
    (@room4, @room4_area1, 'E02', 1, 7, 7, 1, 1, 0, 1, 1, 15.00),
    (@room4, @room4_area1, 'E03', 1, 9, 1, 1, 0, 0, 1, 1, 15.00),
    (@room4, @room4_area1, 'E04', 1, 9, 2, 1, 0, 0, 1, 1, 15.00),
    (@room4, @room4_area1, 'E05', 1, 9, 6, 1, 0, 0, 1, 1, 15.00),
    (@room4, @room4_area1, 'E06', 1, 9, 7, 1, 0, 0, 1, 1, 15.00);

-- =====================================================
-- 4. 为 room5（西二旗店）增加 D/E 排座位
-- =====================================================
INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`, `price_per_hour`) VALUES
    -- D排: VIP沙发 (row_num=5)
    (@room5, @room5_area1, 'D01', 2, 5, 1, 1, 1, 0, 1, 1, 30.00),
    (@room5, @room5_area1, 'D02', 2, 5, 2, 1, 1, 0, 1, 1, 30.00),
    (@room5, @room5_area1, 'D03', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    (@room5, @room5_area1, 'D04', 2, 5, 7, 1, 1, 0, 1, 1, 30.00),
    (@room5, @room5_area1, 'D05', 2, 6, 1, 1, 1, 0, 1, 1, 30.00),
    (@room5, @room5_area1, 'D06', 2, 6, 2, 1, 1, 0, 1, 1, 30.00),
    -- E排: 靠窗 (row_num=7)
    (@room5, @room5_area2, 'E01', 1, 7, 6, 1, 1, 0, 0, 1, 15.00),
    (@room5, @room5_area2, 'E02', 1, 7, 7, 1, 1, 0, 0, 1, 15.00),
    (@room5, @room5_area2, 'E03', 1, 7, 8, 1, 0, 0, 0, 1, 15.00),
    (@room5, @room5_area2, 'E04', 1, 7, 9, 1, 0, 0, 0, 1, 15.00),
    (@room5, @room5_area3, 'E05', 1, 9, 1, 1, 1, 1, 1, 1, 15.00),
    (@room5, @room5_area3, 'E06', 1, 9, 2, 1, 1, 1, 1, 1, 15.00);

-- =====================================================
-- 5. 为 room6（国贸店）增加 D/E 排座位
-- =====================================================
INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`, `price_per_hour`) VALUES
    -- D排: 隔间 (row_num=5)
    (@room6, @room6_area1, 'D01', 3, 5, 1, 1, 1, 0, 1, 1, 25.00),
    (@room6, @room6_area1, 'D02', 3, 5, 2, 1, 1, 0, 1, 1, 25.00),
    (@room6, @room6_area1, 'D03', 3, 5, 6, 1, 1, 0, 1, 1, 25.00),
    (@room6, @room6_area1, 'D04', 3, 5, 7, 1, 1, 0, 1, 1, 25.00),
    -- E排: 休息区 (row_num=9)
    (@room6, @room6_area2, 'E01', 1, 9, 6, 1, 0, 0, 0, 1, 15.00),
    (@room6, @room6_area2, 'E02', 1, 9, 7, 1, 0, 0, 0, 1, 15.00),
    (@room6, @room6_area2, 'E03', 1, 9, 8, 1, 0, 0, 0, 1, 15.00),
    (@room6, @room6_area2, 'E04', 1, 9, 9, 1, 0, 0, 0, 1, 15.00),
    (@room6, @room6_area3, 'E05', 4, 9, 1, 1, 1, 0, 1, 1, 40.00),
    (@room6, @room6_area3, 'E06', 4, 9, 2, 1, 1, 0, 1, 1, 40.00);

-- =====================================================
-- 6. 为 room7（望京北店）增加 D/E 排座位
-- =====================================================
INSERT INTO `seat` (`room_id`, `area_id`, `seat_number`, `seat_type`, `row_num`, `col_num`, `has_power`, `has_lamp`, `is_window`, `is_quiet`, `status`, `price_per_hour`) VALUES
    -- D排: 靠窗VIP (row_num=5)
    (@room7, @room7_area2, 'D01', 2, 5, 1, 1, 1, 1, 1, 1, 30.00),
    (@room7, @room7_area2, 'D02', 2, 5, 2, 1, 1, 1, 1, 1, 30.00),
    (@room7, @room7_area2, 'D03', 2, 5, 6, 1, 1, 0, 1, 1, 30.00),
    (@room7, @room7_area2, 'D04', 2, 5, 7, 1, 1, 0, 1, 1, 30.00),
    (@room7, @room7_area2, 'D05', 2, 6, 1, 1, 1, 1, 1, 1, 30.00),
    (@room7, @room7_area2, 'D06', 2, 6, 2, 1, 1, 1, 1, 1, 30.00),
    -- E排: 小组讨论 (row_num=9)
    (@room7, @room7_area3, 'E01', 1, 9, 6, 1, 0, 0, 0, 1, 15.00),
    (@room7, @room7_area3, 'E02', 1, 9, 7, 1, 0, 0, 0, 1, 15.00),
    (@room7, @room7_area3, 'E03', 1, 9, 8, 1, 0, 0, 0, 1, 15.00),
    (@room7, @room7_area3, 'E04', 1, 9, 9, 1, 0, 0, 0, 1, 15.00),
    (@room7, @room7_area1, 'E05', 1, 9, 1, 1, 1, 0, 1, 1, 15.00),
    (@room7, @room7_area1, 'E06', 1, 9, 2, 1, 1, 0, 1, 1, 15.00);

-- =====================================================
-- 7. 清除旧布局元素，重新插入更复杂的布局
-- =====================================================

DELETE FROM `seat_layout_item`
WHERE `room_id` IN (@room2, @room3, @room4, @room5, @room6, @room7);

-- =====================================================
-- Room2 中关村店: 12列 x 10行
-- 布局: 前台 | 入口 | 落地窗 | 窗景区标签
--       长桌A(4x2) | 主走道(4x5) | VIP沙发区标签
--       长桌B(4x2) | 承重柱 | 静音区标签
--       隔间桌(4x1) | 绿植 | 标准区标签
-- =====================================================
INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`) VALUES
    -- 第1行: 前台 + 入口
    (@room2, NULL, 'front_desk', 'room2-front-desk', '前台', 1, 1, 3, 1, 0, 10, '{"color":"wood"}'),
    (@room2, NULL, 'aisle', 'room2-aisle-top', '通道', 1, 4, 2, 1, 0, 1, '{"orientation":"horizontal"}'),
    (@room2, NULL, 'door', 'room2-door', '入口', 1, 6, 1, 1, 0, 10, '{"side":"north"}'),
    (@room2, NULL, 'plant', 'room2-plant-entrance', '迎宾绿植', 1, 7, 1, 1, 0, 8, '{"kind":"bamboo"}'),
    (@room2, NULL, 'aisle', 'room2-aisle-top2', '通道', 1, 8, 5, 1, 0, 1, '{"orientation":"horizontal"}'),
    -- 第2行: 落地窗 + 窗景区标签
    (@room2, NULL, 'window', 'room2-window-west', '落地窗', 2, 1, 5, 1, 0, 5, '{"side":"west"}'),
    (@room2, NULL, 'zone_label', 'room2-zone-window', '窗景区', 2, 2, 3, 1, 0, 9, '{"zone":"window"}'),
    (@room2, NULL, 'window', 'room2-window-east', '侧窗', 2, 8, 5, 1, 0, 5, '{"side":"east"}'),
    (@room2, NULL, 'zone_label', 'room2-zone-vip', 'VIP区', 2, 9, 3, 1, 0, 9, '{"zone":"vip"}'),
    -- 第3-4行: 窗景长桌A + 主走道
    (@room2, NULL, 'table', 'room2-table-a', '共享长桌A', 3, 1, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room2, NULL, 'aisle', 'room2-main-aisle', '主走道', 3, 5, 2, 5, 0, 1, '{"orientation":"vertical"}'),
    (@room2, NULL, 'table', 'room2-table-b', '共享长桌B', 3, 7, 5, 2, 0, 4, '{"shape":"long"}'),
    -- 第5行: VIP沙发区
    (@room2, NULL, 'zone_label', 'room2-zone-sofa', '沙发区', 5, 7, 3, 1, 0, 9, '{"zone":"sofa"}'),
    -- 第6行: 长桌C
    (@room2, NULL, 'table', 'room2-table-c', '标准长桌C', 6, 7, 5, 2, 0, 4, '{"shape":"long"}'),
    (@room2, NULL, 'pillar', 'room2-pillar-1', '承重柱', 6, 1, 1, 1, 0, 8, '{"shape":"square"}'),
    (@room2, NULL, 'plant', 'room2-plant-1', '绿植', 6, 2, 1, 1, 0, 8, '{"kind":"ficus"}'),
    -- 第7行: 静音区标签
    (@room2, NULL, 'zone_label', 'room2-zone-quiet', '静音区', 7, 7, 3, 1, 0, 9, '{"zone":"quiet"}'),
    -- 第8行: 走道 + 休息角
    (@room2, NULL, 'aisle', 'room2-aisle-mid', '休息走道', 8, 1, 4, 1, 0, 1, '{"orientation":"horizontal"}'),
    (@room2, NULL, 'lounge_counter', 'room2-lounge', '休息角', 8, 7, 3, 1, 0, 4, '{"shape":"lounge"}'),
    (@room2, NULL, 'plant', 'room2-plant-2', '装饰绿植', 8, 10, 1, 1, 0, 8, '{"kind":"succulent"}'),
    -- 第9行: 隔间区
    (@room2, NULL, 'booth', 'room2-booth-a', '独立隔间A', 9, 7, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room2, NULL, 'booth', 'room2-booth-b', '独立隔间B', 9, 9, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room2, NULL, 'zone_label', 'room2-zone-booth', '隔间区', 9, 8, 2, 1, 0, 9, '{"zone":"booth"}'),
    -- 第10行: 底部走道
    (@room2, NULL, 'aisle', 'room2-aisle-bottom', '底部通道', 10, 1, 11, 1, 0, 1, '{"orientation":"horizontal"}');

-- =====================================================
-- Room3 望京店: 11列 x 11行
-- 布局: 值守台 | 入口 | 深夜长桌 | 主通道 | 补给台
--       隔间区 | 电脑区 | 休息沙发 | 补给站
-- =====================================================
INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`) VALUES
    (@room3, NULL, 'front_desk', 'room3-front-desk', '夜间值守台', 1, 1, 2, 1, 0, 10, '{"staffed":"night"}'),
    (@room3, NULL, 'aisle', 'room3-aisle-top', '通道', 1, 3, 3, 1, 0, 1, '{"orientation":"horizontal"}'),
    (@room3, NULL, 'door', 'room3-door', '入口', 1, 6, 1, 1, 0, 10, '{"side":"north"}'),
    (@room3, NULL, 'plant', 'room3-plant-entrance', '夜间绿植', 1, 7, 1, 1, 0, 8, '{"kind":"cactus"}'),
    (@room3, NULL, 'aisle', 'room3-aisle-top2', '通道', 1, 8, 4, 1, 0, 1, '{"orientation":"horizontal"}'),
    -- 第2行: 窗 + 区域标签
    (@room3, NULL, 'window', 'room3-window-east', '街景窗', 2, 9, 1, 6, 0, 5, '{"side":"east"}'),
    (@room3, NULL, 'zone_label', 'room3-zone-focus', '深夜专注区', 2, 1, 3, 1, 0, 9, '{"zone":"focus"}'),
    (@room3, NULL, 'zone_label', 'room3-zone-booth', '隔间区', 2, 5, 2, 1, 0, 9, '{"zone":"booth"}'),
    -- 第3-4行: 深夜长桌
    (@room3, NULL, 'table', 'room3-table-a', '深夜长桌A', 3, 1, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room3, NULL, 'aisle', 'room3-main-aisle', '夜间主通道', 3, 5, 2, 7, 0, 1, '{"orientation":"vertical"}'),
    (@room3, NULL, 'table', 'room3-table-b', '深夜长桌B', 3, 7, 2, 2, 0, 4, '{"shape":"long"}'),
    -- 第5行: 隔间
    (@room3, NULL, 'booth', 'room3-booth-a', '冲刺隔间A', 5, 1, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room3, NULL, 'booth', 'room3-booth-b', '冲刺隔间B', 5, 3, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room3, NULL, 'pillar', 'room3-pillar-1', '立柱', 5, 7, 1, 1, 0, 8, '{"shape":"round"}'),
    -- 第6-7行: 标准区
    (@room3, NULL, 'table', 'room3-table-c', '标准桌C', 6, 7, 2, 2, 0, 4, '{"shape":"standard"}'),
    (@room3, NULL, 'zone_label', 'room3-zone-standard', '标准区', 7, 1, 3, 1, 0, 9, '{"zone":"standard"}'),
    -- 第8行: 休息区
    (@room3, NULL, 'lounge_counter', 'room3-lounge', '充电沙发位', 8, 1, 4, 1, 0, 4, '{"shape":"lounge"}'),
    (@room3, NULL, 'plant', 'room3-plant-1', '补给台', 8, 5, 1, 1, 0, 8, '{"kind":"supply"}'),
    (@room3, NULL, 'zone_label', 'room3-zone-lounge', '休息充电区', 8, 7, 2, 1, 0, 9, '{"zone":"lounge"}'),
    -- 第9-10行: 电脑区
    (@room3, NULL, 'table', 'room3-table-pc', '电脑桌', 9, 7, 4, 2, 0, 4, '{"shape":"pc"}'),
    (@room3, NULL, 'zone_label', 'room3-zone-pc', '电脑区', 10, 1, 3, 1, 0, 9, '{"zone":"pc"}'),
    -- 第11行: 底部
    (@room3, NULL, 'aisle', 'room3-aisle-bottom', '底部通道', 11, 1, 10, 1, 0, 1, '{"orientation":"horizontal"}');

-- =====================================================
-- Room4 学院路店: 9列 x 10行
-- 布局: 咨询台 | 入口 | 采光窗 | 双人书桌 | 中心走道
--       VIP区 | 隔间区 | 静音区 | 冲刺隔间
-- =====================================================
INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`) VALUES
    (@room4, NULL, 'front_desk', 'room4-front-desk', '咨询台', 1, 1, 2, 1, 0, 10, '{"color":"walnut"}'),
    (@room4, NULL, 'aisle', 'room4-aisle-top', '通道', 1, 3, 4, 1, 0, 1, '{"orientation":"horizontal"}'),
    (@room4, NULL, 'door', 'room4-door', '入口', 1, 7, 1, 1, 0, 10, '{"side":"north"}'),
    (@room4, NULL, 'plant', 'room4-plant-entrance', '入口绿植', 1, 8, 1, 1, 0, 8, '{"kind":"fern"}'),
    (@room4, NULL, 'aisle', 'room4-aisle-top2', '通道', 1, 9, 1, 1, 0, 1, '{"orientation":"horizontal"}'),
    -- 第2行: 采光窗 + 区域标签
    (@room4, NULL, 'window', 'room4-window', '采光窗', 2, 1, 8, 1, 0, 5, '{"side":"west"}'),
    (@room4, NULL, 'zone_label', 'room4-zone-focus', '沉浸区', 2, 2, 2, 1, 0, 9, '{"zone":"focus"}'),
    (@room4, NULL, 'zone_label', 'room4-zone-vip', 'VIP区', 2, 6, 2, 1, 0, 9, '{"zone":"vip"}'),
    -- 第3-4行: 双人书桌
    (@room4, NULL, 'table', 'room4-table-a', '双人书桌A', 3, 1, 2, 2, 0, 4, '{"shape":"double"}'),
    (@room4, NULL, 'table', 'room4-table-b', '双人书桌B', 3, 6, 2, 2, 0, 4, '{"shape":"double"}'),
    (@room4, NULL, 'aisle', 'room4-main-aisle', '中心走道', 3, 4, 2, 6, 0, 1, '{"orientation":"vertical"}'),
    -- 第5-6行: VIP区
    (@room4, NULL, 'zone_label', 'room4-zone-vip2', 'VIP沙发区', 5, 1, 2, 1, 0, 9, '{"zone":"vip"}'),
    (@room4, NULL, 'pillar', 'room4-pillar-1', '立柱', 5, 3, 1, 1, 0, 8, '{"shape":"square"}'),
    (@room4, NULL, 'table', 'room4-table-vip', 'VIP书桌', 5, 6, 2, 2, 0, 4, '{"shape":"vip"}'),
    -- 第7行: 静音区标签
    (@room4, NULL, 'zone_label', 'room4-zone-quiet', '静音区', 7, 6, 2, 1, 0, 9, '{"zone":"quiet"}'),
    -- 第8行: 冲刺隔间
    (@room4, NULL, 'booth', 'room4-booth-a', '冲刺隔间A', 8, 1, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room4, NULL, 'booth', 'room4-booth-b', '冲刺隔间B', 8, 3, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room4, NULL, 'zone_label', 'room4-zone-booth', '隔间区', 8, 6, 2, 1, 0, 9, '{"zone":"booth"}'),
    -- 第9行: 静音区
    (@room4, NULL, 'table', 'room4-table-quiet', '静音书桌', 9, 1, 2, 1, 0, 4, '{"shape":"silent"}'),
    (@room4, NULL, 'plant', 'room4-plant-2', '静音绿植', 9, 3, 1, 1, 0, 8, '{"kind":"bamboo"}'),
    (@room4, NULL, 'table', 'room4-table-quiet2', '静音书桌2', 9, 6, 2, 1, 0, 4, '{"shape":"silent"}'),
    -- 第10行: 底部
    (@room4, NULL, 'aisle', 'room4-aisle-bottom', '底部通道', 10, 1, 9, 1, 0, 1, '{"orientation":"horizontal"}');

-- =====================================================
-- Room5 西二旗店: 10列 x 10行
-- 布局: 服务台 | 入口 | 通长窗 | 标准长桌 | 中庭走廊
--       VIP区 | 讨论区 | 电脑桌 | 绿植角
-- =====================================================
INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`) VALUES
    (@room5, NULL, 'front_desk', 'room5-front-desk', '服务台', 1, 1, 2, 1, 0, 10, '{"color":"light"}'),
    (@room5, NULL, 'aisle', 'room5-aisle-top', '通道', 1, 3, 5, 1, 0, 1, '{"orientation":"horizontal"}'),
    (@room5, NULL, 'door', 'room5-door', '入口', 1, 8, 1, 1, 0, 10, '{"side":"north"}'),
    (@room5, NULL, 'plant', 'room5-plant-entrance', '入口装饰', 1, 9, 1, 1, 0, 8, '{"kind":"orchid"}'),
    (@room5, NULL, 'aisle', 'room5-aisle-top2', '通道', 1, 10, 1, 1, 0, 1, '{"orientation":"horizontal"}'),
    -- 第2行: 窗 + 区域标签
    (@room5, NULL, 'window', 'room5-window', '通长窗', 2, 1, 9, 1, 0, 5, '{"side":"south"}'),
    (@room5, NULL, 'zone_label', 'room5-zone-standard', '标准区', 2, 2, 2, 1, 0, 9, '{"zone":"standard"}'),
    (@room5, NULL, 'zone_label', 'room5-zone-vip', 'VIP区', 2, 7, 2, 1, 0, 9, '{"zone":"vip"}'),
    -- 第3-4行: 标准长桌
    (@room5, NULL, 'table', 'room5-table-a', '标准长桌A', 3, 1, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room5, NULL, 'table', 'room5-table-b', '标准长桌B', 3, 6, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room5, NULL, 'aisle', 'room5-main-aisle', '中庭走廊', 3, 5, 1, 6, 0, 1, '{"orientation":"vertical"}'),
    -- 第5-6行: VIP区
    (@room5, NULL, 'zone_label', 'room5-zone-vip2', 'VIP沙发区', 5, 1, 2, 1, 0, 9, '{"zone":"vip"}'),
    (@room5, NULL, 'table', 'room5-table-vip', 'VIP书桌', 5, 6, 4, 2, 0, 4, '{"shape":"vip"}'),
    (@room5, NULL, 'pillar', 'room5-pillar-1', '立柱', 6, 1, 1, 1, 0, 8, '{"shape":"round"}'),
    -- 第7行: 讨论区
    (@room5, NULL, 'zone_label', 'room5-zone-discuss', '讨论区', 7, 6, 3, 1, 0, 9, '{"zone":"discussion"}'),
    (@room5, NULL, 'plant', 'room5-plant-1', '绿植角', 7, 1, 1, 1, 0, 8, '{"kind":"monstera"}'),
    -- 第8行: 休息
    (@room5, NULL, 'lounge_counter', 'room5-lounge', '讨论沙发', 8, 6, 4, 1, 0, 4, '{"shape":"lounge"}'),
    (@room5, NULL, 'aisle', 'room5-aisle-mid', '休息走道', 8, 1, 4, 1, 0, 1, '{"orientation":"horizontal"}'),
    -- 第9行: 电脑桌
    (@room5, NULL, 'table', 'room5-table-c', '电脑桌C', 9, 1, 4, 1, 0, 4, '{"shape":"pc"}'),
    (@room5, NULL, 'window', 'room5-window-bottom', '侧窗', 9, 6, 4, 1, 0, 5, '{"side":"south"}'),
    -- 第10行: 底部
    (@room5, NULL, 'aisle', 'room5-aisle-bottom', '底部通道', 10, 1, 10, 1, 0, 1, '{"orientation":"horizontal"}');

-- =====================================================
-- Room6 国贸店: 10列 x 10行
-- 布局: 补给台 | 入口 | 夜景窗 | 夜读长桌 | 补给通道
--       隔间区 | 休息区 | 电脑区 | 独立隔间
-- =====================================================
INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`) VALUES
    (@room6, NULL, 'front_desk', 'room6-front-desk', '夜猫补给台', 1, 1, 2, 1, 0, 10, '{"open":"24h"}'),
    (@room6, NULL, 'aisle', 'room6-aisle-top', '通道', 1, 3, 4, 1, 0, 1, '{"orientation":"horizontal"}'),
    (@room6, NULL, 'door', 'room6-door', '入口', 1, 7, 1, 1, 0, 10, '{"side":"north"}'),
    (@room6, NULL, 'plant', 'room6-plant-entrance', '夜间绿植', 1, 8, 1, 1, 0, 8, '{"kind":"aloe"}'),
    (@room6, NULL, 'aisle', 'room6-aisle-top2', '通道', 1, 9, 2, 1, 0, 1, '{"orientation":"horizontal"}'),
    -- 第2行: 窗 + 标签
    (@room6, NULL, 'window', 'room6-window', '夜景窗', 2, 8, 2, 6, 0, 5, '{"side":"east"}'),
    (@room6, NULL, 'zone_label', 'room6-zone-focus', '深夜专注区', 2, 1, 3, 1, 0, 9, '{"zone":"focus"}'),
    (@room6, NULL, 'zone_label', 'room6-zone-booth', '隔间区', 2, 5, 2, 1, 0, 9, '{"zone":"booth"}'),
    -- 第3-4行: 夜读长桌
    (@room6, NULL, 'table', 'room6-table-a', '夜读长桌A', 3, 1, 3, 2, 0, 4, '{"shape":"long"}'),
    (@room6, NULL, 'aisle', 'room6-main-aisle', '补给通道', 3, 4, 2, 6, 0, 1, '{"orientation":"vertical"}'),
    (@room6, NULL, 'table', 'room6-table-b', '夜读长桌B', 3, 6, 2, 2, 0, 4, '{"shape":"long"}'),
    -- 第5行: 隔间
    (@room6, NULL, 'booth', 'room6-booth-a', '独立隔间A', 5, 1, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room6, NULL, 'booth', 'room6-booth-b', '独立隔间B', 5, 3, 1, 1, 0, 4, '{"shape":"booth"}'),
    (@room6, NULL, 'pillar', 'room6-pillar-1', '消防柱', 5, 6, 1, 1, 0, 8, '{"shape":"round"}'),
    -- 第6-7行: 标准区
    (@room6, NULL, 'table', 'room6-table-c', '标准桌C', 6, 6, 2, 2, 0, 4, '{"shape":"standard"}'),
    (@room6, NULL, 'zone_label', 'room6-zone-standard', '标准区', 7, 1, 2, 1, 0, 9, '{"zone":"standard"}'),
    -- 第8行: 休息区
    (@room6, NULL, 'lounge_counter', 'room6-lounge', '休息沙发', 8, 1, 3, 1, 0, 4, '{"shape":"lounge"}'),
    (@room6, NULL, 'plant', 'room6-plant-1', '补给绿植', 8, 4, 1, 1, 0, 8, '{"kind":"snake_plant"}'),
    (@room6, NULL, 'zone_label', 'room6-zone-rest', '休息区', 8, 6, 2, 1, 0, 9, '{"zone":"rest"}'),
    -- 第9行: 电脑区 + 独立隔间
    (@room6, NULL, 'table', 'room6-table-pc', '电脑桌', 9, 6, 2, 1, 0, 4, '{"shape":"pc"}'),
    (@room6, NULL, 'booth', 'room6-booth-c', 'VIP隔间', 9, 1, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room6, NULL, 'booth', 'room6-booth-d', 'VIP隔间', 9, 3, 1, 1, 0, 4, '{"shape":"booth"}'),
    -- 第10行: 底部
    (@room6, NULL, 'aisle', 'room6-aisle-bottom', '底部通道', 10, 1, 9, 1, 0, 1, '{"orientation":"horizontal"}');

-- =====================================================
-- Room7 望京北店: 9列 x 10行
-- 布局: 签到台 | 入口 | 北侧采光 | 静音长桌 | 主通道
--       靠窗VIP | 小组桌 | 阳光学习区 | 讨论区
-- =====================================================
INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`) VALUES
    (@room7, NULL, 'front_desk', 'room7-front-desk', '签到台', 1, 1, 2, 1, 0, 10, '{"color":"dark"}'),
    (@room7, NULL, 'aisle', 'room7-aisle-top', '通道', 1, 3, 4, 1, 0, 1, '{"orientation":"horizontal"}'),
    (@room7, NULL, 'door', 'room7-door', '入口', 1, 7, 1, 1, 0, 10, '{"side":"north"}'),
    (@room7, NULL, 'plant', 'room7-plant-entrance', '迎客松', 1, 8, 1, 1, 0, 8, '{"kind":"pine"}'),
    (@room7, NULL, 'aisle', 'room7-aisle-top2', '通道', 1, 9, 1, 1, 0, 1, '{"orientation":"horizontal"}'),
    -- 第2行: 采光窗 + 标签
    (@room7, NULL, 'window', 'room7-window', '北侧采光', 2, 1, 8, 1, 0, 5, '{"side":"north"}'),
    (@room7, NULL, 'zone_label', 'room7-zone-silent', '静音专注区', 2, 2, 2, 1, 0, 9, '{"zone":"silent"}'),
    (@room7, NULL, 'zone_label', 'room7-zone-sunshine', '阳光区', 2, 6, 2, 1, 0, 9, '{"zone":"sunshine"}'),
    -- 第3-4行: 静音长桌
    (@room7, NULL, 'table', 'room7-table-a', '静音长桌A', 3, 1, 3, 2, 0, 4, '{"shape":"silent"}'),
    (@room7, NULL, 'table', 'room7-table-b', '静音长桌B', 3, 6, 3, 2, 0, 4, '{"shape":"silent"}'),
    (@room7, NULL, 'aisle', 'room7-main-aisle', '主通道', 3, 4, 2, 6, 0, 1, '{"orientation":"vertical"}'),
    -- 第5-6行: 靠窗VIP
    (@room7, NULL, 'zone_label', 'room7-zone-vip', '靠窗VIP', 5, 1, 2, 1, 0, 9, '{"zone":"vip"}'),
    (@room7, NULL, 'table', 'room7-table-vip', 'VIP书桌', 5, 6, 3, 2, 0, 4, '{"shape":"vip"}'),
    (@room7, NULL, 'pillar', 'room7-pillar-1', '立柱', 6, 1, 1, 1, 0, 8, '{"shape":"square"}'),
    -- 第7行: 阳光区
    (@room7, NULL, 'zone_label', 'room7-zone-sun', '阳光学习区', 7, 6, 2, 1, 0, 9, '{"zone":"sunshine"}'),
    (@room7, NULL, 'plant', 'room7-plant-1', '静音绿植', 7, 1, 1, 1, 0, 8, '{"kind":"fern"}'),
    -- 第8行: 小组桌
    (@room7, NULL, 'table', 'room7-table-group', '小组桌', 8, 1, 3, 1, 0, 4, '{"shape":"group"}'),
    (@room7, NULL, 'lounge_counter', 'room7-lounge', '讨论沙发', 8, 6, 3, 1, 0, 4, '{"shape":"lounge"}'),
    -- 第9行: 讨论区
    (@room7, NULL, 'zone_label', 'room7-zone-group', '小组讨论区', 9, 6, 2, 1, 0, 9, '{"zone":"group"}'),
    (@room7, NULL, 'booth', 'room7-booth-a', '讨论隔间', 9, 1, 2, 1, 0, 4, '{"shape":"booth"}'),
    (@room7, NULL, 'booth', 'room7-booth-b', '讨论隔间', 9, 3, 1, 1, 0, 4, '{"shape":"booth"}'),
    -- 第10行: 底部
    (@room7, NULL, 'aisle', 'room7-aisle-bottom', '底部通道', 10, 1, 9, 1, 0, 1, '{"orientation":"horizontal"}');

-- =====================================================
-- 8. 重新插入座位布局元素（seat 类型）
--    座位坐标与布局元素对齐，每个座位必须有唯一的 (row, col)
-- =====================================================

INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `seat_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`)
SELECT s.room_id, s.area_id, s.id, 'seat', CONCAT('seat-', s.id), s.seat_number,
       CASE
           -- Room2: A01-A04→row3, A05-A08→row4, C排→row5, B01-B04→row6, B05-B08→row7, D排→row8, E排→row9
           WHEN s.room_id = @room2 AND s.seat_number IN ('A01','A02','A03','A04') THEN 3
           WHEN s.room_id = @room2 AND s.seat_number IN ('A05','A06','A07','A08') THEN 4
           WHEN s.room_id = @room2 AND s.seat_number LIKE 'C%' THEN 5
           WHEN s.room_id = @room2 AND s.seat_number IN ('B01','B02','B03','B04') THEN 6
           WHEN s.room_id = @room2 AND s.seat_number IN ('B05','B06','B07','B08') THEN 7
           WHEN s.room_id = @room2 AND s.seat_number LIKE 'D%' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number LIKE 'E%' THEN 9
           -- Room3: A01-A04→row3, A05-A08→row4, C排→row5, B01-B04→row7, B05-B08→row8, D01-D04→row9, D05-D06→row10
           WHEN s.room_id = @room3 AND s.seat_number IN ('A01','A02','A03','A04') THEN 3
           WHEN s.room_id = @room3 AND s.seat_number IN ('A05','A06','A07','A08') THEN 4
           WHEN s.room_id = @room3 AND s.seat_number LIKE 'C%' THEN 5
           WHEN s.room_id = @room3 AND s.seat_number IN ('B01','B02','B03','B04') THEN 7
           WHEN s.room_id = @room3 AND s.seat_number IN ('B05','B06','B07','B08') THEN 8
           WHEN s.room_id = @room3 AND s.seat_number IN ('D01','D02','D03','D04') THEN 9
           WHEN s.room_id = @room3 AND s.seat_number IN ('D05','D06') THEN 10
           -- Room4: A01-A04→row3, A05-A08→row4, B排→row5, D01-D04→row6, D05-D06→row7, E01-E02→row7, C排→row9, E03-E04→row8, E05-E06→row10
           WHEN s.room_id = @room4 AND s.seat_number IN ('A01','A02','A03','A04') THEN 3
           WHEN s.room_id = @room4 AND s.seat_number IN ('A05','A06','A07','A08') THEN 4
           WHEN s.room_id = @room4 AND s.seat_number IN ('B01','B02','B03','B04') THEN 5
           WHEN s.room_id = @room4 AND s.seat_number IN ('D01','D02','D03','D04') THEN 6
           WHEN s.room_id = @room4 AND s.seat_number IN ('D05','D06','E01','E02') THEN 7
           WHEN s.room_id = @room4 AND s.seat_number IN ('E03','E04') THEN 8
           WHEN s.room_id = @room4 AND s.seat_number LIKE 'C%' THEN 9
           WHEN s.room_id = @room4 AND s.seat_number IN ('E05','E06') THEN 10
           -- Room5: 同 Room4 布局
           WHEN s.room_id = @room5 AND s.seat_number IN ('A01','A02','A03','A04') THEN 3
           WHEN s.room_id = @room5 AND s.seat_number IN ('A05','A06','A07','A08') THEN 4
           WHEN s.room_id = @room5 AND s.seat_number IN ('B01','B02','B03','B04') THEN 5
           WHEN s.room_id = @room5 AND s.seat_number IN ('D01','D02','D03','D04') THEN 6
           WHEN s.room_id = @room5 AND s.seat_number IN ('D05','D06','E01','E02') THEN 7
           WHEN s.room_id = @room5 AND s.seat_number IN ('E03','E04') THEN 8
           WHEN s.room_id = @room5 AND s.seat_number LIKE 'C%' THEN 9
           WHEN s.room_id = @room5 AND s.seat_number IN ('E05','E06') THEN 10
           -- Room6: A01-A04→row3, A05-A08→row4, C排→row5, D排→row6, B排→row7, E01-E04→row9, E05-E06→row10
           WHEN s.room_id = @room6 AND s.seat_number IN ('A01','A02','A03','A04') THEN 3
           WHEN s.room_id = @room6 AND s.seat_number IN ('A05','A06','A07','A08') THEN 4
           WHEN s.room_id = @room6 AND s.seat_number LIKE 'C%' THEN 5
           WHEN s.room_id = @room6 AND s.seat_number LIKE 'D%' THEN 6
           WHEN s.room_id = @room6 AND s.seat_number LIKE 'B%' THEN 7
           WHEN s.room_id = @room6 AND s.seat_number IN ('E01','E02','E03','E04') THEN 9
           WHEN s.room_id = @room6 AND s.seat_number IN ('E05','E06') THEN 10
           -- Room7: A01-A04→row3, A05-A08→row4, B排→row5, D01-D04→row6, D05-D06→row7, C排→row8, E排→row9, E05-E06→row10
           WHEN s.room_id = @room7 AND s.seat_number IN ('A01','A02','A03','A04') THEN 3
           WHEN s.room_id = @room7 AND s.seat_number IN ('A05','A06','A07','A08') THEN 4
           WHEN s.room_id = @room7 AND s.seat_number IN ('B01','B02','B03','B04') THEN 5
           WHEN s.room_id = @room7 AND s.seat_number IN ('D01','D02','D03','D04') THEN 6
           WHEN s.room_id = @room7 AND s.seat_number IN ('D05','D06') THEN 7
           WHEN s.room_id = @room7 AND s.seat_number LIKE 'C%' THEN 8
           WHEN s.room_id = @room7 AND s.seat_number IN ('E01','E02','E03','E04') THEN 9
           WHEN s.room_id = @room7 AND s.seat_number IN ('E05','E06') THEN 10
           ELSE COALESCE(s.row_num, 1)
       END,
       CASE
           -- Room2: A排col1-4, B排col7-10, C排col7-12, D排col6-11, E排col7-10
           WHEN s.room_id = @room2 AND s.seat_number = 'A01' THEN 1
           WHEN s.room_id = @room2 AND s.seat_number = 'A02' THEN 2
           WHEN s.room_id = @room2 AND s.seat_number = 'A03' THEN 3
           WHEN s.room_id = @room2 AND s.seat_number = 'A04' THEN 4
           WHEN s.room_id = @room2 AND s.seat_number = 'A05' THEN 1
           WHEN s.room_id = @room2 AND s.seat_number = 'A06' THEN 2
           WHEN s.room_id = @room2 AND s.seat_number = 'A07' THEN 3
           WHEN s.room_id = @room2 AND s.seat_number = 'A08' THEN 4
           WHEN s.room_id = @room2 AND s.seat_number = 'B01' THEN 7
           WHEN s.room_id = @room2 AND s.seat_number = 'B02' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number = 'B03' THEN 9
           WHEN s.room_id = @room2 AND s.seat_number = 'B04' THEN 10
           WHEN s.room_id = @room2 AND s.seat_number = 'B05' THEN 7
           WHEN s.room_id = @room2 AND s.seat_number = 'B06' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number = 'B07' THEN 9
           WHEN s.room_id = @room2 AND s.seat_number = 'B08' THEN 10
           WHEN s.room_id = @room2 AND s.seat_number = 'C01' THEN 7
           WHEN s.room_id = @room2 AND s.seat_number = 'C02' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number = 'C03' THEN 9
           WHEN s.room_id = @room2 AND s.seat_number = 'C04' THEN 10
           WHEN s.room_id = @room2 AND s.seat_number = 'C05' THEN 11
           WHEN s.room_id = @room2 AND s.seat_number = 'C06' THEN 12
           WHEN s.room_id = @room2 AND s.seat_number = 'D01' THEN 6
           WHEN s.room_id = @room2 AND s.seat_number = 'D02' THEN 7
           WHEN s.room_id = @room2 AND s.seat_number = 'D03' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number = 'D04' THEN 9
           WHEN s.room_id = @room2 AND s.seat_number = 'D05' THEN 10
           WHEN s.room_id = @room2 AND s.seat_number = 'D06' THEN 11
           WHEN s.room_id = @room2 AND s.seat_number = 'E01' THEN 7
           WHEN s.room_id = @room2 AND s.seat_number = 'E02' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number = 'E03' THEN 9
           WHEN s.room_id = @room2 AND s.seat_number = 'E04' THEN 10
           -- Room3: A排col1-4, B排col7-10, C排col1-4, D排col7-10
           WHEN s.room_id = @room3 AND s.seat_number = 'A01' THEN 1
           WHEN s.room_id = @room3 AND s.seat_number = 'A02' THEN 2
           WHEN s.room_id = @room3 AND s.seat_number = 'A03' THEN 3
           WHEN s.room_id = @room3 AND s.seat_number = 'A04' THEN 4
           WHEN s.room_id = @room3 AND s.seat_number = 'A05' THEN 1
           WHEN s.room_id = @room3 AND s.seat_number = 'A06' THEN 2
           WHEN s.room_id = @room3 AND s.seat_number = 'A07' THEN 3
           WHEN s.room_id = @room3 AND s.seat_number = 'A08' THEN 4
           WHEN s.room_id = @room3 AND s.seat_number = 'B01' THEN 7
           WHEN s.room_id = @room3 AND s.seat_number = 'B02' THEN 8
           WHEN s.room_id = @room3 AND s.seat_number = 'B03' THEN 9
           WHEN s.room_id = @room3 AND s.seat_number = 'B04' THEN 10
           WHEN s.room_id = @room3 AND s.seat_number = 'B05' THEN 7
           WHEN s.room_id = @room3 AND s.seat_number = 'B06' THEN 8
           WHEN s.room_id = @room3 AND s.seat_number = 'B07' THEN 9
           WHEN s.room_id = @room3 AND s.seat_number = 'B08' THEN 10
           WHEN s.room_id = @room3 AND s.seat_number = 'C01' THEN 1
           WHEN s.room_id = @room3 AND s.seat_number = 'C02' THEN 2
           WHEN s.room_id = @room3 AND s.seat_number = 'C03' THEN 3
           WHEN s.room_id = @room3 AND s.seat_number = 'C04' THEN 4
           WHEN s.room_id = @room3 AND s.seat_number IN ('D01','D05') THEN 7
           WHEN s.room_id = @room3 AND s.seat_number IN ('D02','D06') THEN 8
           WHEN s.room_id = @room3 AND s.seat_number = 'D03' THEN 9
           WHEN s.room_id = @room3 AND s.seat_number = 'D04' THEN 10
           -- Room4/5/7: 左区col1-2, 右区col6-7 (中间col4-5是走道)
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('A01','A05') THEN 1
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('A02','A06') THEN 2
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('A03','A07') THEN 6
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('A04','A08') THEN 7
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('B01','D01','D05') THEN 1
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('B02','D02','D06') THEN 2
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('B03','D03') THEN 6
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('B04','D04') THEN 7
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('C01','E03','E05') THEN 1
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('C02','E04','E06') THEN 2
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('C03') THEN 6
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('C04') THEN 7
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('E01') THEN 6
           WHEN s.room_id IN (@room4, @room5, @room7) AND s.seat_number IN ('E02') THEN 7
           -- Room6: 左区col1-2, 右区col6-7
           WHEN s.room_id = @room6 AND s.seat_number IN ('A01','A05') THEN 1
           WHEN s.room_id = @room6 AND s.seat_number IN ('A02','A06') THEN 2
           WHEN s.room_id = @room6 AND s.seat_number IN ('A03','A07') THEN 6
           WHEN s.room_id = @room6 AND s.seat_number IN ('A04','A08') THEN 7
           WHEN s.room_id = @room6 AND s.seat_number IN ('B01','B03') THEN 1
           WHEN s.room_id = @room6 AND s.seat_number IN ('B02','B04') THEN 2
           WHEN s.room_id = @room6 AND s.seat_number IN ('B05') THEN 6
           WHEN s.room_id = @room6 AND s.seat_number IN ('B06') THEN 7
           WHEN s.room_id = @room6 AND s.seat_number IN ('C01','D01') THEN 1
           WHEN s.room_id = @room6 AND s.seat_number IN ('C02','D02') THEN 2
           WHEN s.room_id = @room6 AND s.seat_number IN ('C03','D03') THEN 6
           WHEN s.room_id = @room6 AND s.seat_number IN ('C04','D04') THEN 7
           WHEN s.room_id = @room6 AND s.seat_number IN ('E01','E05') THEN 1
           WHEN s.room_id = @room6 AND s.seat_number IN ('E02','E06') THEN 2
           WHEN s.room_id = @room6 AND s.seat_number IN ('E03') THEN 6
           WHEN s.room_id = @room6 AND s.seat_number IN ('E04') THEN 7
           ELSE COALESCE(s.col_num, 1)
       END,
       1,
       1,
       0,
       20,
       CONCAT('{"seatType":', COALESCE(s.seat_type, 1), ',"power":', COALESCE(s.has_power, 0), ',"lamp":', COALESCE(s.has_lamp, 0), ',"window":', COALESCE(s.is_window, 0), '}')
FROM `seat` s
WHERE s.room_id IN (@room2, @room3, @room4, @room5, @room6, @room7);
