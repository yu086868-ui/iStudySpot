SET @room2 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 静心自习室（中关村店）' LIMIT 1);
SET @room3 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 24h自习舱（望京店）' LIMIT 1);
SET @room4 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 安静学习舱（学院路店）' LIMIT 1);
SET @room5 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 城市自习室（西二旗店）' LIMIT 1);
SET @room6 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 深夜学习站（国贸店）' LIMIT 1);
SET @room7 := (SELECT `id` FROM `study_room` WHERE `name` = 'iStudySpot 专注自习馆（望京北店）' LIMIT 1);

DELETE FROM `seat_layout_item`
WHERE `room_id` IN (@room2, @room3, @room4, @room5, @room6, @room7);

INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`)
VALUES
    (@room2, NULL, 'front_desk', 'room2-front-desk', '前台', 1, 1, 2, 1, 0, 10, '{"color":"wood"}'),
    (@room2, NULL, 'door', 'room2-door', '入口', 1, 5, 1, 1, 0, 10, '{"side":"north"}'),
    (@room2, NULL, 'window', 'room2-window-west', '落地窗', 2, 1, 5, 1, 0, 5, '{"side":"west"}'),
    (@room2, NULL, 'aisle', 'room2-main-aisle', '主走道', 3, 5, 4, 5, 0, 1, '{"orientation":"vertical"}'),
    (@room2, NULL, 'table', 'room2-table-a', '共享长桌A', 3, 1, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room2, NULL, 'table', 'room2-table-b', '共享长桌B', 6, 6, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room2, NULL, 'pillar', 'room2-pillar-1', '承重柱', 5, 4, 1, 1, 0, 8, '{"shape":"square"}'),
    (@room2, NULL, 'plant', 'room2-plant-1', '绿植', 8, 1, 1, 1, 0, 8, '{"kind":"ficus"}'),
    (@room2, NULL, 'zone_label', 'room2-zone-a', '窗景区', 2, 2, 2, 1, 0, 9, '{"zone":"window"}'),
    (@room2, NULL, 'zone_label', 'room2-zone-b', '标准学习区', 7, 7, 2, 1, 0, 9, '{"zone":"standard"}'),

    (@room3, NULL, 'front_desk', 'room3-front-desk', '夜间值守台', 1, 2, 2, 1, 0, 10, '{"staffed":"night"}'),
    (@room3, NULL, 'door', 'room3-door', '入口', 1, 7, 1, 1, 0, 10, '{"side":"north"}'),
    (@room3, NULL, 'window', 'room3-window-east', '街景窗', 2, 9, 1, 6, 0, 5, '{"side":"east"}'),
    (@room3, NULL, 'aisle', 'room3-main-aisle', '夜间主通道', 2, 5, 2, 7, 0, 1, '{"orientation":"vertical"}'),
    (@room3, NULL, 'table', 'room3-table-a', '深夜长桌A', 3, 1, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room3, NULL, 'lounge_counter', 'room3-lounge-b', '充电沙发位', 7, 7, 4, 2, 0, 4, '{"shape":"lounge"}'),
    (@room3, NULL, 'pillar', 'room3-pillar-1', '立柱', 4, 4, 1, 1, 0, 8, '{"shape":"round"}'),
    (@room3, NULL, 'plant', 'room3-plant-1', '补给台', 8, 8, 1, 1, 0, 8, '{"kind":"supply"}'),
    (@room3, NULL, 'zone_label', 'room3-zone-a', '深夜加油区', 2, 2, 2, 1, 0, 9, '{"zone":"focus"}'),
    (@room3, NULL, 'zone_label', 'room3-zone-b', '休息充电区', 7, 7, 2, 1, 0, 9, '{"zone":"lounge"}'),

    (@room4, NULL, 'front_desk', 'room4-front-desk', '咨询台', 1, 1, 2, 1, 0, 10, '{"color":"walnut"}'),
    (@room4, NULL, 'door', 'room4-door', '入口', 1, 8, 1, 1, 0, 10, '{"side":"north"}'),
    (@room4, NULL, 'window', 'room4-window', '采光窗', 2, 1, 8, 1, 0, 5, '{"side":"west"}'),
    (@room4, NULL, 'aisle', 'room4-main-aisle', '中心走道', 3, 4, 2, 6, 0, 1, '{"orientation":"vertical"}'),
    (@room4, NULL, 'table', 'room4-table-a', '双人书桌A', 3, 1, 2, 2, 0, 4, '{"shape":"double"}'),
    (@room4, NULL, 'table', 'room4-table-b', '双人书桌B', 3, 6, 2, 2, 0, 4, '{"shape":"double"}'),
    (@room4, NULL, 'booth', 'room4-booth-c', '冲刺隔间', 8, 1, 4, 1, 0, 4, '{"shape":"booth"}'),
    (@room4, NULL, 'pillar', 'room4-pillar-1', '立柱', 6, 3, 1, 1, 0, 8, '{"shape":"square"}'),
    (@room4, NULL, 'zone_label', 'room4-zone-a', '沉浸区', 2, 2, 2, 1, 0, 9, '{"zone":"focus"}'),
    (@room4, NULL, 'zone_label', 'room4-zone-b', '隔间区', 8, 6, 2, 1, 0, 9, '{"zone":"booth"}'),

    (@room5, NULL, 'front_desk', 'room5-front-desk', '服务台', 1, 1, 2, 1, 0, 10, '{"color":"light"}'),
    (@room5, NULL, 'door', 'room5-door', '入口', 1, 9, 1, 1, 0, 10, '{"side":"north"}'),
    (@room5, NULL, 'window', 'room5-window', '通长窗', 2, 1, 9, 1, 0, 5, '{"side":"south"}'),
    (@room5, NULL, 'aisle', 'room5-main-aisle', '中庭走廊', 3, 5, 2, 6, 0, 1, '{"orientation":"vertical"}'),
    (@room5, NULL, 'table', 'room5-table-a', '标准长桌A', 3, 1, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room5, NULL, 'table', 'room5-table-b', '标准长桌B', 3, 6, 4, 2, 0, 4, '{"shape":"long"}'),
    (@room5, NULL, 'table', 'room5-table-c', '电脑桌C', 8, 1, 4, 1, 0, 4, '{"shape":"pc"}'),
    (@room5, NULL, 'plant', 'room5-plant-1', '绿植角', 7, 1, 1, 1, 0, 8, '{"kind":"monstera"}'),
    (@room5, NULL, 'zone_label', 'room5-zone-a', '标准区', 2, 2, 2, 1, 0, 9, '{"zone":"standard"}'),
    (@room5, NULL, 'zone_label', 'room5-zone-b', '讨论区', 8, 7, 2, 1, 0, 9, '{"zone":"discussion"}'),

    (@room6, NULL, 'front_desk', 'room6-front-desk', '夜猫补给台', 1, 1, 2, 1, 0, 10, '{"open":"24h"}'),
    (@room6, NULL, 'door', 'room6-door', '入口', 1, 8, 1, 1, 0, 10, '{"side":"north"}'),
    (@room6, NULL, 'window', 'room6-window', '夜景窗', 2, 8, 1, 6, 0, 5, '{"side":"east"}'),
    (@room6, NULL, 'aisle', 'room6-main-aisle', '补给通道', 3, 4, 2, 6, 0, 1, '{"orientation":"vertical"}'),
    (@room6, NULL, 'table', 'room6-table-a', '夜读长桌A', 3, 1, 3, 2, 0, 4, '{"shape":"long"}'),
    (@room6, NULL, 'booth', 'room6-booth-c', '独立隔间', 8, 6, 3, 1, 0, 4, '{"shape":"booth"}'),
    (@room6, NULL, 'pillar', 'room6-pillar-1', '消防柱', 5, 3, 1, 1, 0, 8, '{"shape":"round"}'),
    (@room6, NULL, 'zone_label', 'room6-zone-a', '深夜专注区', 2, 2, 2, 1, 0, 9, '{"zone":"focus"}'),
    (@room6, NULL, 'zone_label', 'room6-zone-b', '缓冲区', 8, 6, 2, 1, 0, 9, '{"zone":"buffer"}'),

    (@room7, NULL, 'front_desk', 'room7-front-desk', '签到台', 1, 1, 2, 1, 0, 10, '{"color":"dark"}'),
    (@room7, NULL, 'door', 'room7-door', '入口', 1, 8, 1, 1, 0, 10, '{"side":"north"}'),
    (@room7, NULL, 'window', 'room7-window', '北侧采光', 2, 1, 8, 1, 0, 5, '{"side":"north"}'),
    (@room7, NULL, 'aisle', 'room7-main-aisle', '主通道', 3, 4, 2, 6, 0, 1, '{"orientation":"vertical"}'),
    (@room7, NULL, 'table', 'room7-table-a', '静音长桌A', 3, 1, 3, 2, 0, 4, '{"shape":"silent"}'),
    (@room7, NULL, 'table', 'room7-table-b', '静音长桌B', 3, 6, 3, 2, 0, 4, '{"shape":"silent"}'),
    (@room7, NULL, 'table', 'room7-table-c', '小组桌C', 8, 1, 4, 1, 0, 4, '{"shape":"group"}'),
    (@room7, NULL, 'plant', 'room7-plant-1', '静音绿植', 8, 1, 1, 1, 0, 8, '{"kind":"fern"}'),
    (@room7, NULL, 'zone_label', 'room7-zone-a', '静音专注区', 2, 2, 2, 1, 0, 9, '{"zone":"silent"}'),
    (@room7, NULL, 'zone_label', 'room7-zone-b', '小组学习区', 8, 6, 2, 1, 0, 9, '{"zone":"group"}');

INSERT INTO `seat_layout_item` (`room_id`, `area_id`, `seat_id`, `item_type`, `item_key`, `label`, `row_num`, `col_num`, `width_units`, `height_units`, `rotation`, `z_index`, `metadata`)
SELECT s.room_id, s.area_id, s.id, 'seat', CONCAT('seat-', s.id), s.seat_number,
       CASE
           WHEN s.room_id = @room2 AND s.seat_number LIKE 'A%' THEN 3
           WHEN s.room_id = @room2 AND s.seat_number LIKE 'B%' THEN 6
           WHEN s.room_id = @room3 AND s.seat_number LIKE 'A%' THEN 3
           WHEN s.room_id = @room3 AND s.seat_number LIKE 'B%' THEN 7
           WHEN s.room_id = @room4 AND s.seat_number LIKE 'A%' THEN 3
           WHEN s.room_id = @room4 AND s.seat_number LIKE 'B%' THEN 6
           WHEN s.room_id = @room4 AND s.seat_number LIKE 'C%' THEN 8
           WHEN s.room_id = @room5 AND s.seat_number LIKE 'A%' THEN 3
           WHEN s.room_id = @room5 AND s.seat_number LIKE 'B%' THEN 6
           WHEN s.room_id = @room5 AND s.seat_number LIKE 'C%' THEN 8
           WHEN s.room_id = @room6 AND s.seat_number LIKE 'A%' THEN 3
           WHEN s.room_id = @room6 AND s.seat_number LIKE 'B%' THEN 6
           WHEN s.room_id = @room6 AND s.seat_number LIKE 'C%' THEN 8
           WHEN s.room_id = @room7 AND s.seat_number LIKE 'A%' THEN 3
           WHEN s.room_id = @room7 AND s.seat_number LIKE 'B%' THEN 6
           WHEN s.room_id = @room7 AND s.seat_number LIKE 'C%' THEN 8
           ELSE COALESCE(s.row_num, 1)
       END,
       CASE
           WHEN s.room_id = @room2 AND s.seat_number = 'A01' THEN 1
           WHEN s.room_id = @room2 AND s.seat_number = 'A02' THEN 2
           WHEN s.room_id = @room2 AND s.seat_number = 'A03' THEN 3
           WHEN s.room_id = @room2 AND s.seat_number = 'A04' THEN 4
           WHEN s.room_id = @room2 AND s.seat_number = 'A05' THEN 1
           WHEN s.room_id = @room2 AND s.seat_number = 'A06' THEN 2
           WHEN s.room_id = @room2 AND s.seat_number = 'A07' THEN 3
           WHEN s.room_id = @room2 AND s.seat_number = 'A08' THEN 4
           WHEN s.room_id = @room2 AND s.seat_number = 'B01' THEN 6
           WHEN s.room_id = @room2 AND s.seat_number = 'B02' THEN 7
           WHEN s.room_id = @room2 AND s.seat_number = 'B03' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number = 'B04' THEN 9
           WHEN s.room_id = @room2 AND s.seat_number = 'B05' THEN 6
           WHEN s.room_id = @room2 AND s.seat_number = 'B06' THEN 7
           WHEN s.room_id = @room2 AND s.seat_number = 'B07' THEN 8
           WHEN s.room_id = @room2 AND s.seat_number = 'B08' THEN 9
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
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('A01', 'A05') THEN 1
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('A02', 'A06') THEN 2
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('A03', 'A07') THEN 6
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('A04', 'A08') THEN 7
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('B01', 'C01') THEN 1
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('B02', 'C02') THEN 2
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('B03', 'C03') THEN 6
           WHEN s.room_id IN (@room4, @room5, @room6, @room7) AND s.seat_number IN ('B04', 'C04') THEN 7
           ELSE COALESCE(s.col_num, 1)
       END,
       1,
       1,
       0,
       20,
       CONCAT('{"seatType":', COALESCE(s.seat_type, 1), ',"power":', COALESCE(s.has_power, 0), ',"lamp":', COALESCE(s.has_lamp, 0), ',"window":', COALESCE(s.is_window, 0), '}')
FROM `seat` s
WHERE s.room_id IN (@room2, @room3, @room4, @room5, @room6, @room7);
